package lateralpraxis.lpdnd.ExpenseConfirmation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityCashDeposit;
import lateralpraxis.lpdnd.ActivityCashDepositView;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ExpenseConfirmationCreate extends Activity {
    /*Start of code for Variable Declaration*/
    private String lang, userId, id, data, total, responseJSON;
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
    private TextView tvCustomer, tvDate, tvExpenseHead, tvAmount, tvRemarks;
    private Button btnSubmit;
    private RadioGroup RadioType;
    private RadioButton RadioAccept, RadioReject;
    /*End of code to declare Controls*/
    //On create method similar to page load
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Code to set layout
        setContentView(R.layout.expense_confirmation_view);
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //Code to create instance of classes
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
		/*		*/
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
        }
        //Start of code to find Controls
        tvCustomer = (TextView) findViewById(R.id.tvCustomer);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvExpenseHead = (TextView) findViewById(R.id.tvExpenseHead);
        tvAmount= (TextView) findViewById(R.id.tvAmount);
        tvRemarks= (TextView) findViewById(R.id.tvRemarks);
        etRemarks= (EditText) findViewById(R.id.etRemarks);
        btnSubmit= (Button) findViewById(R.id.btnSubmit);
        RadioType = (RadioGroup) findViewById(R.id.RadioType);
        RadioAccept = (RadioButton) findViewById(R.id.RadioAccept);
        RadioReject = (RadioButton) findViewById(R.id.RadioReject);
        db.openR();
        data = db.getExpenseConfirmationHeaderData(id);
        tvDate.setText(common.convertToDisplayDateFormat(data.split("~")[0].toString()));
        tvCustomer.setText(data.split("~")[1].toString());
        tvExpenseHead.setText(data.split("~")[2].toString());
        tvAmount.setText(common.convertToTwoDecimal(data.split("~")[3].toString()));
        tvRemarks.setText(data.split("~")[4].toString());

        //<editor-fold desc="Code to be exceuted on change of Radio Button">
        RadioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = RadioType.findViewById(checkedId);
                int index = RadioType.indexOfChild(radioButton);
                if (index == 0) {
                    type = "Accepted";
                } else {
                    type = "Rejected";
                }
            }
        });
        //</editor-fold>

        // Event hander to Submit Expense Confirmation
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            // When create button click
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
                alertDialogBuilder
                        .setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं,आप इस व्यय बुकिंग को पुष्टि करना चाहते हैं?":"Are you sure, you want to confirm this expense booking?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int id) {
                                if(common.isConnected())
                                {
                                    AsyncExpenseConfirmationWSCall task = new AsyncExpenseConfirmationWSCall();
                                    task.execute();
                                }
                            }
                        }).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.create().show();
            }

        });
    }

    //Async Task To Call Confirm Expense Booking Data
    private class AsyncExpenseConfirmationWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ExpenseConfirmationCreate.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                HashMap<String, String> user = session.getLoginUserDetails();
                userId = user.get(UserSessionManager.KEY_ID);
                String[] name = {"id", "status", "remarks", "userId", "ipAddress", "machine" };
                String[] value = { id, type, etRemarks.getText().toString() ,userId, common.getDeviceIPAddress(true), common.getIMEI() };
                responseJSON = "";
                // Call method of web service to Confirm cash deporit from server
                responseJSON = common.CallJsonWS(name, value, "UpdateApprovalExpenseBooking",
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
                    common.showToast("Expense Booking Confirmed successfully");
                    Intent intent = new Intent(ExpenseConfirmationCreate.this, ActivityAdminHomeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ExpenseConfirmationCreate.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ExpenseConfirmationCreate.this,e.getMessage(), false);
            }
            Dialog.dismiss();
        }

        // To display Cash deposit deletion message
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Confirming Expense Booking...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }

    //Code to go to intent on selection of menu item
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:

                Intent i = new Intent(ExpenseConfirmationCreate.this,ExpenseConfirmationList.class);
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
        Intent homeScreenIntent = new Intent(ExpenseConfirmationCreate.this, ExpenseConfirmationList.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }

}
