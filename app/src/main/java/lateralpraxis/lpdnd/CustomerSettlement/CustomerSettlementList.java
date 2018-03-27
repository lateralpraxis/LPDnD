package lateralpraxis.lpdnd.CustomerSettlement;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class CustomerSettlementList extends Activity {

    //<editor-fold desc="Code for Variable Declaration">
    String lang = "en", userId, responseJSON, From, searchText = "";
    HashMap<String, String> map = null;
    Common common;
    private int listSize = 0;
    //</editor-fold>
    private ArrayList<HashMap<String, String>> wordList = null;
    //<editor-fold desc="Code to Declare Controls">
    private EditText etSearchText;
    private TextView tvNoRecord;
    private Button btnGo;
    //</editor-fold>
    private ListView listCustomer;
    //<editor-fold desc="Code for Class Declaration">
    private UserSessionManager session;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on OnCreate event">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_customer_settlement);

        //<editor-fold desc="Code for creating Instance of Class">
        session = new UserSessionManager(getApplicationContext());
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code to Set Language">
        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //</editor-fold>

        //<editor-fold desc="Code to set User Id">
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        //</editor-fold>

        //<editor-fold desc="Code to Find Controls">
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        etSearchText = (EditText) findViewById(R.id.etSearchText);
        listCustomer = (ListView) findViewById(R.id.listCustomer);
        btnGo = (Button) findViewById(R.id.btnGo);
        //</editor-fold>

        //<editor-fold desc="Code to hide controls by Default">
        tvNoRecord.setVisibility(View.GONE);
        listCustomer.setVisibility(View.GONE);
        //</editor-fold>

        	/*Code to get data from posted page*/
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            searchText = extras.getString("searchText");
        }
        if(!searchText.equalsIgnoreCase("")) {
            etSearchText.setText(searchText);
            AsyncSearchCustomerWSCall task = new AsyncSearchCustomerWSCall();
            task.execute();
        }


        //<editor-fold desc="Code to be executed on View Button Click">
        btnGo.setOnClickListener(new View.OnClickListener() {
            // When create button click
            @Override
            public void onClick(View arg0) {
                if (etSearchText.getText().toString().trim().length() == 0)
                    common.showToast("Please enter search text");
                else {
                    AsyncSearchCustomerWSCall task = new AsyncSearchCustomerWSCall();
                    task.execute();
                }
            }
        });
        //</editor-fold>

        //To handle a callback to be invoked when an item in this AdapterView has been clicked
        listCustomer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //On click of list view item
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(CustomerSettlementList.this, CustomerSettlementCreate.class);
                myIntent.putExtra("Id", UUID.randomUUID().toString());
                myIntent.putExtra("CompId", ((TextView)view.findViewById(R.id.tvCompId)).getText());
                myIntent.putExtra("CompName",((TextView)view.findViewById(R.id.tvCompany)).getText());
                myIntent.putExtra("CustId",((TextView)view.findViewById(R.id.tvCustId)).getText());
                myIntent.putExtra("CustName", ((TextView)view.findViewById(R.id.tvCustName)).getText());
                myIntent.putExtra("CustMobile",((TextView)view.findViewById(R.id.tvMob)).getText());
                myIntent.putExtra("BalanceAmount", ((TextView)view.findViewById(R.id.tvBalAmt)).getText());
                myIntent.putExtra("SearchText", etSearchText.getText().toString().trim());

                startActivity(myIntent);
                finish();
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Code to Set Home Button in Action Bar">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Action Bar Menu Item">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_to_home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to b executed on Back Press">
    @Override
    public void onBackPressed() {

        Intent i = new Intent(CustomerSettlementList.this, ActivityAdminHomeScreen.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In List">
    public static class viewHolder {
        TextView tvCustId, tvCompId, tvMobile, tvCustomerName, tvCompany, tvAmount, tvCustName, tvMob, tvBalAmt;
        TableRow tableHeader;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch Customer List for Reconciliation">
    private class AsyncSearchCustomerWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                CustomerSettlementList.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"lang", "remarks", "userId"};
                String[] value = {lang, etSearchText.getText().toString(), userId};
                // Call method of web service to Read Customers For Settlement
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "GetCustomerSettlementCreateData", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }

        }

        // After execution of Search Customer web service
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    String data = "";
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    wordList = new ArrayList<HashMap<String, String>>();
                    String prevName = "";
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            map = new HashMap<String, String>();
                            map.put("CustomerId", jsonArray.getJSONObject(i)
                                    .getString("A"));
                            map.put("CustomerName", jsonArray.getJSONObject(i)
                                    .getString("B"));
                            map.put("CustomerNameLocal", jsonArray.getJSONObject(i)
                                    .getString("C"));
                            map.put("CompanyId", jsonArray.getJSONObject(i)
                                    .getString("D"));
                            map.put("CompanyName", jsonArray.getJSONObject(i)
                                    .getString("E"));
                            map.put("CompanyShortName", jsonArray.getJSONObject(i)
                                    .getString("F"));
                            map.put("BalanceAmount", jsonArray.getJSONObject(i)
                                    .getString("G"));
                            map.put("Mobile", jsonArray.getJSONObject(i)
                                    .getString("H"));
                            if(prevName.equalsIgnoreCase(jsonArray.getJSONObject(i)
                                    .getString("B")))
                                map.put("Flag", "1");
                            else
                                map.put("Flag", "0");
                            prevName=jsonArray.getJSONObject(i)
                                    .getString("B");
                            wordList.add(map);
                        }
                        listSize = wordList.size();
                        if (listSize != 0) {
                            listCustomer.setAdapter(new CustomerListAdapter(CustomerSettlementList.this, wordList));
                            ViewGroup.LayoutParams params = listCustomer.getLayoutParams();
                            listCustomer.setLayoutParams(params);
                            listCustomer.requestLayout();
                            tvNoRecord.setVisibility(View.GONE);
                            listCustomer.setVisibility(View.VISIBLE);
                        } else {
                            listCustomer.setAdapter(null);
                            tvNoRecord.setVisibility(View.VISIBLE);
                        }

                    } else {
                        common.showToast("No customer available for settlement!");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showToast(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Customer Settlement Download failed: " + e.toString());
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Customer Settlement Data..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="List Adapter">
    private class CustomerListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private Context context2;

        public CustomerListAdapter(Context context,
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
                convertView = inflater.inflate(R.layout.list_settlement_customer, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            holder.tvCustId = (TextView) convertView
                    .findViewById(R.id.tvCustId);
            holder.tvCustomerName = (TextView) convertView
                    .findViewById(R.id.tvCustomerName);
            holder.tvCompId = (TextView) convertView
                    .findViewById(R.id.tvCompId);
            holder.tvMobile = (TextView) convertView
                    .findViewById(R.id.tvMobile);
            holder.tvCompany = (TextView) convertView
                    .findViewById(R.id.tvCompany);
            holder.tvAmount = (TextView) convertView
                    .findViewById(R.id.tvAmount);
            holder.tvBalAmt = (TextView) convertView
                    .findViewById(R.id.tvBalAmt);
            holder.tvCustName = (TextView) convertView
                    .findViewById(R.id.tvCustName);
            holder.tvMob = (TextView) convertView
                    .findViewById(R.id.tvMob);
            holder.tableHeader = (TableRow) convertView.findViewById(R.id.tableHeader);
            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvCustId.setText(itemData.get("CustomerId"));
            holder.tvCompId.setText(itemData.get("CompanyId"));
            holder.tvCustomerName.setText(itemData.get("CustomerName"));
            holder.tvCustName.setText(itemData.get("CustomerName"));
            holder.tvMob.setText(itemData.get("Mobile"));
            holder.tvMobile.setText(itemData.get("Mobile"));
            holder.tvCompany.setText(itemData.get("CompanyName"));
            holder.tvAmount.setText(common.convertToTwoDecimal(itemData.get("BalanceAmount")));
            holder.tvBalAmt.setText(itemData.get("BalanceAmount"));
            if(itemData.get("Flag").equalsIgnoreCase("1"))
                holder.tableHeader.setVisibility(View.GONE);
            else
                holder.tableHeader.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }

    }
    //</editor-fold>

}
