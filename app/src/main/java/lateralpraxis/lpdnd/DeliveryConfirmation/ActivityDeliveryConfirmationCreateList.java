package lateralpraxis.lpdnd.DeliveryConfirmation;

//<editor-fold desc="Import">

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

//</editor-fold>
public class ActivityDeliveryConfirmationCreateList extends Activity {

    //<editor-fold desc="Code for Variable Declaration">
    /*Code for Variable Declaration*/
    private final Context mContext = this;
    HashMap<String, String> map = null;
    DatabaseAdapter db;
    Common common;
    private String lang, userId, responseJSON;
    private int listSize = 0;
    private ArrayList<HashMap<String, String>> wordList = null;
    private UserSessionManager session;
    private ListView listViewConfirm;
    private TextView tvNoRecord;
    private View tvDivider;
    /*End of Code for Variable Declaration*/
    //</editor-fold>

    //<editor-fold desc="On create method to load page ">
    //On create method to load page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Code to set layout
        setContentView(R.layout.activity_delivery_confirmation_create_list);

        //Code to create instance of classes
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        //Start of code to find Controls
        listViewConfirm = (ListView) findViewById(R.id.listViewConfirm);
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        tvDivider = findViewById(R.id.tvDivider);
        tvDivider.setVisibility(View.GONE);
        if (common.isConnected()) {
            String[] params = {userId};
            AsyncDeliveryDetailWSCall task = new AsyncDeliveryDetailWSCall();
            task.execute(params);
        } else {
            Intent intent = new Intent(mContext, ActivityHomeScreen.class);
            startActivity(intent);
            finish();
        }

        listViewConfirm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivityDeliveryConfirmationCreateList.this, ActivityDeliveryConfirmationCreateView.class);
                intent.putExtra("DeliveryId", String.valueOf(((TextView) item.findViewById(R.id.tvId)).getText().toString()));
                intent.putExtra("Name", String.valueOf(((TextView) item.findViewById(R.id.tvName)).getText().toString()));
                intent.putExtra("Date", String.valueOf(((TextView) item.findViewById(R.id.tvDate)).getText().toString()));
                intent.putExtra("Invoice", String.valueOf(((TextView) item.findViewById(R.id.tvInvoice)).getText().toString()));
                intent.putExtra("Vehicle", String.valueOf(((TextView) item.findViewById(R.id.tvVehicle)).getText().toString()));
                startActivity(intent);
                finish();
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Code to go to intent on selection of menu item">
    //Code to go to intent on selection of menu item
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_go_to_home:
                Intent homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // To create menu on inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    // When press back button go to home screen
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ActivityDeliveryConfirmationCreateList.this, ActivityHomeScreen.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In List">
    public static class viewHolder {
        TextView tvId, tvDate, tvName, tvVehicle, tvInvoice;
        LinearLayout llDate, llVehicle, llName;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Web Service to Fetch Delivery Detail">
    // Web Service to Fetch Delivery Detail
    private class AsyncDeliveryDetailWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "id"};
                String[] value = {"ReadDeliveryList", params[0]};
                // Call method of web service to Read Data
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadDeliveryData", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }

        }

        // After execution of product web service
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    String data = "";
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    wordList = new ArrayList<HashMap<String, String>>();
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            map = new HashMap<String, String>();
                            map.put("DeliveryId", jsonArray.getJSONObject(i)
                                    .getString("DeliveryId"));
                            map.put("FullName", jsonArray.getJSONObject(i)
                                    .getString("FullName"));
                            map.put("VehicleNo", jsonArray.getJSONObject(i)
                                    .getString("VehicleNo"));
                            map.put("DeliveryDate", jsonArray.getJSONObject(i)
                                    .getString("DeliveryDate"));
                            map.put("InvoiceNo", jsonArray.getJSONObject(i)
                                    .getString("InvoiceNo"));
                            map.put("CustomerName", jsonArray.getJSONObject(i)
                                    .getString("CustomerName"));

                            if (data.equalsIgnoreCase(jsonArray.getJSONObject(i)
                                    .getString("FullName") + "~" + jsonArray.getJSONObject(i)
                                    .getString("VehicleNo") + "~" + jsonArray.getJSONObject(i)
                                    .getString("DeliveryDate")))
                                map.put("Flag", "1");
                            else
                                map.put("Flag", "0");
                            data = jsonArray.getJSONObject(i)
                                    .getString("FullName") + "~" + jsonArray.getJSONObject(i)
                                    .getString("VehicleNo") + "~" + jsonArray.getJSONObject(i)
                                    .getString("DeliveryDate");
                            wordList.add(map);
                        }
                        listSize = wordList.size();
                        if (listSize != 0) {
                            listViewConfirm.setAdapter(new ReportListAdapter(mContext, wordList));

                            ViewGroup.LayoutParams params = listViewConfirm.getLayoutParams();
                            //params.height = 500;
                            listViewConfirm.setLayoutParams(params);
                            listViewConfirm.requestLayout();
                            tvNoRecord.setVisibility(View.GONE);
                            tvDivider.setVisibility(View.VISIBLE);
                        } else {
                            listViewConfirm.setAdapter(null);
                            tvNoRecord.setVisibility(View.VISIBLE);
                            tvDivider.setVisibility(View.GONE);
                        }
                    } else {
                        if (lang.equalsIgnoreCase("hi"))
                            common.showToast("वितरण की पुष्टि के लिए कोई डेटा उपलब्ध नहीं है!");
                        else
                            common.showToast("There is no data available for delivery confirmation!");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showToast(result);
                    /*Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
                    startActivity(intent);
					finish();*/
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Delivery Downloading failed: " + e.toString());
                /*Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
				startActivity(intent);
				finish();*/
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Delivery Data..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }

    private class ReportListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private Context context2;

        public ReportListAdapter(Context context,
                                 ArrayList<HashMap<String, String>> listData) {
            this.context2 = context;
            inflater = LayoutInflater.from(context2);
            _listData = listData;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return _listData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.activity_delivery_confirmation_create_list_item, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;
            holder.tvId = (TextView) convertView
                    .findViewById(R.id.tvId);
            holder.tvDate = (TextView) convertView
                    .findViewById(R.id.tvDate);
            holder.tvName = (TextView) convertView
                    .findViewById(R.id.tvName);
            holder.tvVehicle = (TextView) convertView
                    .findViewById(R.id.tvVehicle);
            holder.tvInvoice = (TextView) convertView
                    .findViewById(R.id.tvInvoice);

            holder.llDate = (LinearLayout) convertView
                    .findViewById(R.id.llDate);
            holder.llVehicle = (LinearLayout) convertView
                    .findViewById(R.id.llVehicle);
            holder.llName = (LinearLayout) convertView
                    .findViewById(R.id.llName);

            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvId.setText(itemData.get("DeliveryId"));
            holder.tvDate.setText(common.convertToDisplayDateFormat(itemData.get("DeliveryDate")));
            holder.tvName.setText(itemData.get("FullName"));
            holder.tvVehicle.setText(itemData.get("VehicleNo"));
            //holder.tvInvoice.setText(itemData.get("InvoiceNo")+" - "+itemData.get("CustomerName"));
            holder.tvInvoice.setText(itemData.get("InvoiceNo"));
            if (itemData.get("Flag").equalsIgnoreCase("1")) {
                holder.llDate.setVisibility(View.GONE);
                holder.llVehicle.setVisibility(View.GONE);
                holder.llName.setVisibility(View.GONE);
            } else {
                holder.llDate.setVisibility(View.VISIBLE);
                holder.llVehicle.setVisibility(View.VISIBLE);
                holder.llName.setVisibility(View.VISIBLE);
            }
            convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }

    }
    //</editor-fold>
}
