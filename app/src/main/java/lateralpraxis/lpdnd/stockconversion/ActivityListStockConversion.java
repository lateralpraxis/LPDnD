package lateralpraxis.lpdnd.stockconversion;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityListStockConversion extends Activity {

    private final Context mContext = this;
    HashMap<String, String> map = null;
    //<editor-fold desc="Code to declare Class">
    DatabaseAdapter db;
    Common common;
    /*Start of code for Variable Declaration*/
    private String lang, userId, responseJSON;
    private int listSize = 0;
    /*End of code for Variable Declaration*/
    private int year, month, day;
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>> wordList = null;
    private UserSessionManager session;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter_display, dateFormatter_database;
    //</editor-fold>

    //<editor-fold desc="Code to declare Controls">
    private TextView tvDate, tvEmpty,linkAddStockConversion;
    private ListView listConvert;
    private Button btnGo;
    private TableLayout tableGridHead;
    //</editor-fold>

    //<editor-fold desc="Methods to display the Calendar">
    private DatePickerDialog.OnDateSetListener dateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    showDate(dateFormatter_display.format(calendar.getTime()));
                }
            };
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_stock_conversion);

        //<editor-fold desc="Code to set Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());


        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        //<editor-fold desc="Code to find Controls">
        listConvert = (ListView) findViewById(R.id.listConvert);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        linkAddStockConversion= (TextView) findViewById(R.id.linkAddStockConversion);
        btnGo = (Button) findViewById(R.id.btnGo);
        tableGridHead = (TableLayout) findViewById(R.id.tableGridHead);
        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        dateFormatter_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        //</editor-fold>
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(dateFormatter_display.format(calendar.getTime()));
        //<editor-fold desc="Code for clicking ob Link Add Click">
        linkAddStockConversion.setOnClickListener(new View.OnClickListener() {
            //On click of view delivery button
            @Override
            public void onClick(View arg0) {
               /*Intent intent = new Intent(ActivityListStockConversion.this,
                        ActivityCreateStockConversion.class);
                intent.putExtra("UniqueId", UUID.randomUUID().toString());
                startActivity(intent);
                finish();*/
               if(common.isConnected())
               {
                   AsyncPendingDeliveryStatusWSCall task= new AsyncPendingDeliveryStatusWSCall();
                   task.execute();
               }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to set default language">
        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click of Go Button">
        btnGo.setOnClickListener(new View.OnClickListener() {
            //On click of add button
            @Override
            public void onClick(View arg0) {
                if (common.isConnected()) {
                    AsyncStockReturnListListWSCall task = new AsyncStockReturnListListWSCall();
                    task.execute();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of List View">
        listConvert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivityListStockConversion.this, ActivityStockConversionDetail.class);
                intent.putExtra("Id", String.valueOf(((TextView) item.findViewById(R.id.tvId)).getText().toString()));
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>
    }
    //</editor-fold>


    //<editor-fold desc="Methods to Display Selected Date in TextView">
    private void showDate(String date) {
        tvDate.setText(date.replace(" ", "-"));
    }
    //</editor-fold>

    //<editor-fold desc="Methods to open Calendar">
    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, dateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back and Home Button">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_go_to_home:
                String userRole = "";
                final HashMap<String, String> user = session.getLoginUserDetails();
                userRole = user.get(UserSessionManager.KEY_ROLES);
                Intent i;
                i = new Intent(ActivityListStockConversion.this, ActivityHomeScreen.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Event Triggered on Clicking Back
    @Override
    public void onBackPressed() {
        Intent i = new Intent(ActivityListStockConversion.this, ActivityHomeScreen.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be Bind Data in list view">
    public static class ViewHolder {
        TextView tvCode, tvDate,tvId;
    }

    public class CustomAdapter extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
            this.docContext = context;
            mInflater = LayoutInflater.from(docContext);
            list = lvList;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {

            return getCount();
        }

        @Override
        public int getItemViewType(int position) {

            return position;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {


            final ViewHolder holder;
            if (arg1 == null) {
                arg1 = mInflater.inflate(R.layout.list_stock_conversion, null);
                holder = new ViewHolder();
                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvCode = (TextView) arg1.findViewById(R.id.tvCode);
                holder.tvDate = (TextView) arg1.findViewById(R.id.tvDate);


                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvId.setText(list.get(arg0).get("Id"));
            holder.tvCode.setText(list.get(arg0).get("Code"));
            holder.tvDate.setText(common.convertToDisplayDateFormat(list.get(arg0).get("Date")));

            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async class Class to handle fetch Adjustment web service call as separate thread">
    private class AsyncStockReturnListListWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityListStockConversion.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "lang", "userId", "date"};
                String dateString;
                Date date = new Date();
                date = dateFormatter_display.parse(tvDate.getText().toString().trim());
                dateString = dateFormatter_database.format(date);
                String[] value = {"ReadSCMaster", lang, userId, dateString};
                responseJSON = "";
                //Call method of web service to download data from server
                responseJSON = common.CallJsonWS(name, value, "ReadDateAndUserWiseReport", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            }
        }

        //After execution of web service
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR: ")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    wordList = new ArrayList<HashMap<String, String>>();
                    String prevName = "";
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            map = new HashMap<String, String>();
                            map.put("Id", jsonArray.getJSONObject(i)
                                    .getString("A"));
                            map.put("Code", jsonArray.getJSONObject(i)
                                    .getString("B"));
                            map.put("Date", jsonArray.getJSONObject(i)
                                    .getString("C"));
                            wordList.add(map);
                        }

                    } else {
                        if (result.contains("null")) {
                            result = "Server not responding. Please try again later.";
                            common.showAlert(ActivityListStockConversion.this, result, false);
                        }
                    }
                    listSize = wordList.size();
                    if (listSize != 0) {
                        listConvert.setAdapter(new CustomAdapter(mContext, wordList));

                        ViewGroup.LayoutParams params = listConvert.getLayoutParams();
                        listConvert.setLayoutParams(params);
                        listConvert.requestLayout();
                        tvEmpty.setVisibility(View.GONE);
                        tableGridHead.setVisibility(View.VISIBLE);
                    } else {
                        listConvert.setAdapter(null);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tableGridHead.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                common.showAlert(ActivityListStockConversion.this, "Stock Conversion Downloading failed: " + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        //To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Stock Conversion..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Download Pending Delivery Status">
    private class AsyncPendingDeliveryStatusWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityListStockConversion.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = { "action", "userId", "role" };
                String[] value = { "CheckPendingDelivery", userId, "Customer" };
                responseJSON = "";
                // Call method of web service to download Reatil Outlet Inventory from
                // server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            }
        }

        // After execution of web service to download Retail Outlet Inventory
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    db.open();
                    db.DeleteMasterData("DeliveryConfirmStatus");
                    String status="";
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        db.Insert_DeliveryConfirmStatus(jsonArray.getJSONObject(i)
                                .getString("A"));
                        status=jsonArray.getJSONObject(i).getString("A");
                    }
                    db.close();
                    if(status.equalsIgnoreCase("0"))
                    {
                        if(common.isConnected()) {
                            AsyncLiveInventoryDetailWSCall task = new AsyncLiveInventoryDetailWSCall();
                            task.execute();
                        }
                    }
                    else
                    {
                        common.showToast(lang.equalsIgnoreCase("hi") ? "डिलिवरी पुष्टि के लिए लंबित हैं इसलिए स्टॉक रूपांतरण की अनुमति नहीं है।":"Deliveries are pending for confirmation hence stock conversion is not allowed.");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityListStockConversion.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityListStockConversion.this,
                        "Pending Delivery Status Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Pending Delivery Status..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch LiveInventory Fro Retail Outlet">
    private class AsyncLiveInventoryDetailWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityListStockConversion.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action","lang","customerId" };
                String[] value = { "GetProductRawMaterial",lang,userId };
                // Call method of web service to Read Conversion Details
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value,"ReadRetailOutletLiveInventory", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    String data="";
                    // To display message after response from server
                    JSONArray jsonSKU = new JSONArray(responseJSON.split("~")[0]);
                    JSONArray jsonRaw = new JSONArray(responseJSON.split("~")[1]);
                    if(jsonSKU.length() > 0 || jsonRaw.length() > 0) {
                        if (jsonSKU.length() > 0) {
                            db.open();
                            db.DeleteMasterData("SKULiveInventory");
                            db.close();
                            if (jsonSKU.length() > 0) {
                                for (int i = 0; i < jsonSKU.length(); ++i) {
                                    db.open();
                                    db.Insert_SKULiveInventory(jsonSKU.getJSONObject(i)
                                            .getString("A"), jsonSKU.getJSONObject(i)
                                            .getString("B"), jsonSKU.getJSONObject(i)
                                            .getString("C").replace(".00", ""));
                                    db.close();

                                }

                            }
                        }
                        if (jsonRaw.length() > 0) {
                            db.open();
                            db.DeleteMasterData("RawMaterialLiveInventory");
                            db.close();
                            for (int i = 0; i < jsonRaw.length(); ++i) {
                                db.open();
                                db.Insert_RawMaterialLiveInventory(jsonRaw.getJSONObject(i)
                                        .getString("A"), jsonRaw.getJSONObject(i)
                                        .getString("B"), jsonRaw.getJSONObject(i)
                                        .getString("C").replace(".00", ""));
                                db.close();
                            }

                        }
                        Intent intent = new Intent(ActivityListStockConversion.this,
                                ActivityCreateStockConversion.class);
                        intent.putExtra("UniqueId", UUID.randomUUID().toString());
                        startActivity(intent);
                        finish();
                    }
                     else {
                        common.showToast("There is no data available for Inventory!");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Outlet Inventory Downloading failed: " + e.toString());
                Intent intent = new Intent(mContext, ActivityListStockConversion.class);
                startActivity(intent);
                finish();
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Outlet Inventory..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>
}
