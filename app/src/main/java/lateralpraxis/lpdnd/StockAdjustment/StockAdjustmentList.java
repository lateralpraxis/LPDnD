package lateralpraxis.lpdnd.StockAdjustment;

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
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class StockAdjustmentList extends Activity {


    private final Context mContext = this;
    HashMap<String, String> map = null;
    //<editor-fold desc="Code to declare Class">
    DatabaseAdapter db;
    Common common;
    /*Start of code for Variable Declaration*/
    private String lang, userId, responseJSON, rdButtonSelectedText;
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
    private TextView tvFromDate, tvEmpty,linkAddStockAdjustment, tvToDate, tvItem, tvAdjDate, tvExistInv, tvAdjQty, tvNewInv, tvReason;
    private ListView listConvert;
    private Button btnGo;
    private TableLayout tableGridHead;
    private RadioGroup RadioType;
    private RadioButton RadioRaw, RadioProduct;
    //</editor-fold>

    //<editor-fold desc="Methods to display the Calendar">
    private DatePickerDialog.OnDateSetListener fromDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    FromDate(dateFormatter_display.format(calendar.getTime()));
                }
            };
    private DatePickerDialog.OnDateSetListener toDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    ToDate(dateFormatter_display.format(calendar.getTime()));
                }
            };
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_stock_adjustment);

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
        tvFromDate = (TextView) findViewById(R.id.tvFromDate);
        tvToDate = (TextView) findViewById(R.id.tvToDate);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        tvAdjDate = (TextView) findViewById(R.id.tvAdjDate);
        tvItem = (TextView) findViewById(R.id.tvItem);
        tvAdjQty = (TextView) findViewById(R.id.tvAdjQty);
        tvExistInv = (TextView) findViewById(R.id.tvExistInv);
        tvNewInv = (TextView) findViewById(R.id.tvNewInv);
        tvReason = (TextView) findViewById(R.id.tvReason);
        linkAddStockAdjustment= (TextView) findViewById(R.id.linkAddStockAdjustment);
        btnGo = (Button) findViewById(R.id.btnGo);
        tableGridHead = (TableLayout) findViewById(R.id.tableGridHead);
        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        dateFormatter_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        RadioType = (RadioGroup) findViewById(R.id.RadioType);
        RadioRaw = (RadioButton) findViewById(R.id.RadioRaw);
        RadioProduct = (RadioButton) findViewById(R.id.RadioProduct);
        //</editor-fold>

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        FromDate(dateFormatter_display.format(calendar.getTime()));
        ToDate(dateFormatter_display.format(calendar.getTime()));
        //<editor-fold desc="Code for clicking ob Link Add Click">
        linkAddStockAdjustment.setOnClickListener(new View.OnClickListener() {
            //On click of view delivery button
            @Override
            public void onClick(View arg0) {
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

                // get selected radio button from radioGroup
                int selectedId = RadioType.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                RadioButton radioButton = (RadioButton) findViewById(selectedId);
                rdButtonSelectedText = radioButton.getText().toString();
                //Call method to get Adjustment List
                if (common.isConnected()) {
                    AsyncStockReturnListWSCall task = new AsyncStockReturnListWSCall();
                    task.execute();
                }
            }
        });
        //</editor-fold>
    }
    //</editor-fold>


    //<editor-fold desc="Methods to Display Selected Date in TextView">
    private void FromDate(String date) {
        tvFromDate.setText(date.replace(" ", "-"));
    }
    private void ToDate(String date) {
        tvToDate.setText(date.replace(" ", "-"));
    }
    //</editor-fold>

    //<editor-fold desc="Methods to open Calendar">
    @SuppressWarnings("deprecation")
    public void setFromDate(View view) {
        showDialog(999);

    }

    public void setToDate(View view) {
        showDialog(998);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, fromDateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }

        else if (id == 998) {

            DatePickerDialog dialog = new DatePickerDialog(this, toDateListener, year, month, day);
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
                i = new Intent(StockAdjustmentList.this, ActivityHomeScreen.class);
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
        Intent i = new Intent(StockAdjustmentList.this, ActivityHomeScreen.class);
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
        TextView tvAdjDate, tvItem, tvAdjQty, tvExistInv, tvNewInv, tvReason;
        TableRow tableHeader;
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
                arg1 = mInflater.inflate(R.layout.activity_list_stock_adjustment_data, null);
                holder = new ViewHolder();
                holder.tvAdjDate = (TextView) arg1.findViewById(R.id.tvAdjDate);
                holder.tvItem = (TextView) arg1.findViewById(R.id.tvItem);
                holder.tvAdjQty = (TextView) arg1.findViewById(R.id.tvAdjQty);
                holder.tvExistInv = (TextView) arg1.findViewById(R.id.tvExistInv);
                holder.tvNewInv = (TextView) arg1.findViewById(R.id.tvNewInv);
                holder.tvReason = (TextView) arg1.findViewById(R.id.tvReason);
                holder.tableHeader = (TableRow) arg1.findViewById(R.id.tableHeader);

                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvAdjDate.setText(common.convertToDisplayDateFormat(list.get(arg0).get("AdjustDate")));
            holder.tvItem.setText(list.get(arg0).get("Item"));
            holder.tvExistInv.setText(list.get(arg0).get("ExistingInventory"));
            holder.tvAdjQty.setText(list.get(arg0).get("Quantity"));
            holder.tvNewInv.setText(list.get(arg0).get("NewInventory"));
            holder.tvReason.setText(Html.fromHtml("<b>Remarks: </b>") + list.get(arg0).get("Reason"));
            if(list.get(arg0).get("Flag").equalsIgnoreCase("1"))
                holder.tableHeader.setVisibility(View.GONE);
            else
                holder.tableHeader.setVisibility(View.VISIBLE);

            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async class Class to handle fetch Adjustment web service call as separate thread">
    private class AsyncStockReturnListWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(StockAdjustmentList.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = { "lang", "fromDate", "toDate", "customerId", "prRm"};
                String fromDateString, toDateString, strRemarks;
                Date fromDate = new Date();
                fromDate = dateFormatter_display.parse(tvFromDate.getText().toString().trim());
                fromDateString = dateFormatter_database.format(fromDate);
                Date toDate = new Date();

                toDate = dateFormatter_display.parse(tvToDate.getText().toString().trim());
                toDateString = dateFormatter_database.format(toDate);
                String[] value = {lang, fromDateString, toDateString, userId, rdButtonSelectedText};
                responseJSON = "";
                //Call method of web service to download data from server
                responseJSON = common.CallJsonWS(name, value, "GetStockAdjustmentData", common.url);
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
                                    .getString("Id"));
                            map.put("AdjustDate", jsonArray.getJSONObject(i)
                                    .getString("AdjustDate"));
                            map.put("Item", jsonArray.getJSONObject(i)
                                    .getString("Item"));
                            map.put("ExistingInventory", jsonArray.getJSONObject(i)
                                    .getString("ExistingInventory"));
                            map.put("Quantity", jsonArray.getJSONObject(i)
                                    .getString("Quantity"));
                            map.put("NewInventory", jsonArray.getJSONObject(i)
                                    .getString("NewInventory"));
                            map.put("Reason", jsonArray.getJSONObject(i)
                                    .getString("Reason"));
                            if(prevName.equalsIgnoreCase(jsonArray.getJSONObject(i)
                                    .getString("Id")))
                                map.put("Flag", "1");
                            else
                                map.put("Flag", "0");
                            prevName=jsonArray.getJSONObject(i)
                                    .getString("Id");
                            wordList.add(map);
                        }

                    } else {
                        if (result.contains("null")) {
                            result = "Server not responding. Please try again later.";
                            common.showAlert(StockAdjustmentList.this, result, false);
                        }
                    }
                    listSize = wordList.size();
                    if (listSize != 0) {
                        listConvert.setAdapter(new CustomAdapter(mContext, wordList));
                        ViewGroup.LayoutParams params = listConvert.getLayoutParams();
                        listConvert.setLayoutParams(params);
                        //listConvert.requestLayout();
                        tvEmpty.setVisibility(View.GONE);
                        listConvert.requestLayout();
                        tableGridHead.setVisibility(View.VISIBLE);

                    } else {
                        listConvert.setAdapter(null);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tableGridHead.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                common.showAlert(StockAdjustmentList.this, "Stock Adjustment Downloading failed: " + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        //To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Stock Adjustment..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Download Pending Delivery Status">
    private class AsyncPendingDeliveryStatusWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                StockAdjustmentList.this);

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
                        common.showToast(lang.equalsIgnoreCase("hi") ? "डिलिवरी पुष्टि के लिए लंबित हैं इसलिए स्टॉक रूपांतरण की अनुमति नहीं है।":"Deliveries are pending for confirmation hence stock adjustment is not allowed.");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(StockAdjustmentList.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(StockAdjustmentList.this,
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
                StockAdjustmentList.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"lang","id" };
                String[] value = { lang, userId };
                // Call method of web service to Read Conversion Details
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value,"GetProductRawMaterial", common.url);
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
                        Intent intent = new Intent(StockAdjustmentList.this,
                                StockAdjustmentCreate.class);
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
                Intent intent = new Intent(mContext, StockAdjustmentList.class);
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
