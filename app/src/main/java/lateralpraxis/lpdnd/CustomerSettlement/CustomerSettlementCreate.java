package lateralpraxis.lpdnd.CustomerSettlement;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityCashDeposit;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class CustomerSettlementCreate extends Activity {
    /*Start of code for Variable Declaration*/
    private String lang, userId, id, data, total, responseJSON, compId, custId, compName, custName, mobile, amount, searchText;
    private int listSize = 0;
    String type = "Accepted";
    private EditText etRemarks;
    /*End of code for Variable Declaration*/
	/*Start of code to declare class*/
    DatabaseAdapter db;
    Common common;
    ActivityCashDeposit.CustomAdapter Cadapter;
    private UserSessionManager session;
    private final Context mContext = this;
	/*End of code to declare class*/

    /*Start of code to declare Controls*/
    private TextView tvCustomer, tvMobile, tvCompany, tvAmount;
    private Button btnSubmit;
    /*End of code to declare Controls*/
    //On create method similar to page load
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Code to set layout
        setContentView(R.layout.activity_customer_settlement_create);
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //Code to create instance of classes
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
		/*	Code to get Language	*/
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        lang= session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

		/*Code to get data from posted page*/
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("Id");
            compId= extras.getString("CompId");
            compName= extras.getString("CompName");
            custId= extras.getString("CustId");
            custName= extras.getString("CustName");
            mobile= extras.getString("CustMobile");
            amount= extras.getString("BalanceAmount");
            searchText= extras.getString("SearchText");
        }
        //Start of code to find Controls
        tvCustomer = (TextView) findViewById(R.id.tvCustomer);
        tvMobile = (TextView) findViewById(R.id.tvMobile);
        tvCompany = (TextView) findViewById(R.id.tvCompany);
        tvAmount= (TextView) findViewById(R.id.tvAmount);
        etRemarks= (EditText) findViewById(R.id.etRemarks);
        btnSubmit= (Button) findViewById(R.id.btnSubmit);

        tvCustomer.setText(custName);
        tvMobile.setText(mobile);
        tvAmount.setText(common.convertToTwoDecimal(amount));
        tvCompany.setText(compName);
        // Event hander to Submit Customer Settlment
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            // When create button click
            @Override
            public void onClick(View arg0) {
                if (String.valueOf(etRemarks.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "टिप्पणी अनिवार्य है।" : "Remarks is mandatory.");
                else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                    alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    alertDialogBuilder
                            .setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं,आप इस ग्राहक निपटान को पुष्टि करना चाहते हैं?" : "Are you sure, you want to confirm this customer settlement?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int id) {
                                    if (common.isConnected()) {
                                        AsyncCustomerSettlementWSCall task = new AsyncCustomerSettlementWSCall();
                                        task.execute();
                                    }
                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    alertDialogBuilder.create().show();
                }
            }

        });
    }

    //Async Task To Call Confirm Expense Booking Data
    private class AsyncCustomerSettlementWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                CustomerSettlementCreate.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                HashMap<String, String> user = session.getLoginUserDetails();
                userId = user.get(UserSessionManager.KEY_ID);
                String[] name = {"uniqueId", "customerId", "companyId", "existingAmount", "remarks",  "userId", "ip", "machine" };
                String[] value = { id, custId, compId, amount, etRemarks.getText().toString(), userId, common.getDeviceIPAddress(true), common.getIMEI() };
                responseJSON = "";
                // Call method of web service to Create Customer Settlement from server
                responseJSON = common.CallJsonWS(name, value, "CreateCustomerSettlement",
                        common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to delete cash deposit
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    common.showToast("Customer Settlement successfully");
                    Intent intent = new Intent(CustomerSettlementCreate.this, ActivityAdminHomeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(CustomerSettlementCreate.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(CustomerSettlementCreate.this,e.getMessage(), false);
            }
            Dialog.dismiss();
        }
        // To display Settlement Customer
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Settlement Customer...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }

    //Code to go to intent on selection of menu item
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:

                Intent i = new Intent(CustomerSettlementCreate.this, CustomerSettlementList.class);
                i.putExtra("searchText", searchText);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
                return true;

            case R.id.action_go_to_home:
                Intent homeScreenIntent = new Intent(this, ActivityAdminHomeScreen.class);
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
        Intent homeScreenIntent = new Intent(CustomerSettlementCreate.this, CustomerSettlementList.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }

}
