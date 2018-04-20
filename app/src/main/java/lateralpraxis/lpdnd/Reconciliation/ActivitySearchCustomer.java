package lateralpraxis.lpdnd.Reconciliation;

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
import android.widget.TextView;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivitySearchCustomer extends Activity {

    //<editor-fold desc="Code for Variable Declaration">
    String lang = "en", userId, responseJSON, From;
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
        setContentView(R.layout.activity_search_customer);

        //<editor-fold desc="Code for creating Instance of Class">
        session = new UserSessionManager(getApplicationContext());
        common = new Common(this);
        //</editor-fold>

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code to set Data from Previous Intent">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            From = extras.getString("From");
        }
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


        //<editor-fold desc="Code to be executed on click of List View">
        listCustomer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivitySearchCustomer.this, ActivityReconcile.class);
                intent.putExtra("From", From);
                intent.putExtra("Id", String.valueOf(((TextView) item.findViewById(R.id.tvCustId)).getText().toString()));
                intent.putExtra("Name", String.valueOf(((TextView) item.findViewById(R.id.tvCustomerName)).getText().toString()));
                intent.putExtra("Cash", String.valueOf(((TextView) item.findViewById(R.id.tvCashAmount)).getText().toString()));
                intent.putExtra("Credit", String.valueOf(((TextView) item.findViewById(R.id.tvCreditAmount)).getText().toString()));
                intent.putExtra("TotalCashSale", String.valueOf(((TextView) item.findViewById(R.id.tvTotalCashSale)).getText().toString()));
                intent.putExtra("TotalCreditSale", String.valueOf(((TextView) item.findViewById(R.id.tvTotalCreditSale)).getText().toString()));
                intent.putExtra("TotalPayment", String.valueOf(((TextView) item.findViewById(R.id.tvTotalPayment)).getText().toString()));
                intent.putExtra("TotalExpense", String.valueOf(((TextView) item.findViewById(R.id.tvTotalExpense)).getText().toString()));

                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>
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
        Intent i;
        if (From.equalsIgnoreCase("Admin"))
            i = new Intent(ActivitySearchCustomer.this, ActivityAdminHomeScreen.class);
        else
            i = new Intent(ActivitySearchCustomer.this, ActivityHomeScreen.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In List">
    public static class viewHolder {
        TextView tvCustId, tvCashAmount, tvCreditAmount, tvCustomerName,tvTotalCashSale,tvTotalCreditSale,tvTotalPayment, tvTotalExpense;
        int ref;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch Customer List for Reconciliation">
    private class AsyncSearchCustomerWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivitySearchCustomer.this);

        @Override
        protected String doInBackground(String... params) {
            try {


                String[] name = {"searchStr", "userId"};
                String[] value = {etSearchText.getText().toString(), userId};
                // Call method of web service to Read Customers For Reconciliation
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadReconciliationCustomer", common.url);
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
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            map = new HashMap<String, String>();
                            map.put("CustomerId", jsonArray.getJSONObject(i)
                                    .getString("A"));
                            map.put("CustomerName", jsonArray.getJSONObject(i)
                                    .getString("B"));
                            map.put("CustomerNameLocal", jsonArray.getJSONObject(i)
                                    .getString("C"));
                            map.put("CashAmount", jsonArray.getJSONObject(i)
                                    .getString("D"));
                            map.put("CreditAmount", jsonArray.getJSONObject(i)
                                    .getString("E"));

                            map.put("TotalCashSale", jsonArray.getJSONObject(i)
                                    .getString("F"));
                            map.put("TotalCreditSale", jsonArray.getJSONObject(i)
                                    .getString("G"));
                            map.put("TotalPayment", jsonArray.getJSONObject(i)
                                    .getString("H"));
                            map.put("TotalExpense", jsonArray.getJSONObject(i)
                                    .getString("I"));
                            wordList.add(map);
                        }
                        listSize = wordList.size();
                        if (listSize != 0) {
                            listCustomer.setAdapter(new CustomerListAdapter(ActivitySearchCustomer.this, wordList));

                            ViewGroup.LayoutParams params = listCustomer.getLayoutParams();
                            //params.height = 500;
                            listCustomer.setLayoutParams(params);
                            listCustomer.requestLayout();
                            tvNoRecord.setVisibility(View.GONE);
                            listCustomer.setVisibility(View.VISIBLE);
                        } else {
                            listCustomer.setAdapter(null);
                            tvNoRecord.setVisibility(View.VISIBLE);
                        }

                    } else {
                        common.showToast("No customer available for reconciliation!");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showToast(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Customer Download failed: " + e.toString());
                /*Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
                startActivity(intent);
				finish();*/
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Customer Data..");
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
                convertView = inflater.inflate(R.layout.list_reconciliation_customer, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;

            holder.tvCustId = (TextView) convertView
                    .findViewById(R.id.tvCustId);
            holder.tvCashAmount = (TextView) convertView
                    .findViewById(R.id.tvCashAmount);
            holder.tvCreditAmount = (TextView) convertView
                    .findViewById(R.id.tvCreditAmount);
            holder.tvCustomerName = (TextView) convertView
                    .findViewById(R.id.tvCustomerName);

            holder.tvTotalCashSale = (TextView) convertView
                    .findViewById(R.id.tvTotalCashSale);
            holder.tvTotalCreditSale = (TextView) convertView
                    .findViewById(R.id.tvTotalCreditSale);
            holder.tvTotalPayment = (TextView) convertView
                    .findViewById(R.id.tvTotalPayment);
            holder.tvTotalExpense = (TextView) convertView
                    .findViewById(R.id.tvTotalExpense);

            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvCustId.setText(itemData.get("CustomerId"));
            holder.tvCashAmount.setText(itemData.get("CashAmount"));
            holder.tvCreditAmount.setText(itemData.get("CreditAmount"));
            holder.tvCustomerName.setText(itemData.get("CustomerName"));

            holder.tvTotalCashSale.setText(itemData.get("TotalCashSale"));
            holder.tvTotalCreditSale.setText(itemData.get("TotalCreditSale"));
            holder.tvTotalPayment.setText(itemData.get("TotalPayment"));
            holder.tvTotalExpense.setText(itemData.get("TotalExpense"));

            convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }

    }
    //</editor-fold>

}
