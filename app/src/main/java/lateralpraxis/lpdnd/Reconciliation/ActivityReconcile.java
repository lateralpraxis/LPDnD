package lateralpraxis.lpdnd.Reconciliation;

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
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
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
import java.util.UUID;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityReconcile extends Activity {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";

    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    final String fpRegex =
            ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                    "[+-]?(" +         // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from the Java Language Specification, 2nd
                    // edition, section 3.10.2.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");
    private final Context mContext = this;
    //<editor-fold desc="Code for Class Declaration">
    Common common;
    //</editor-fold>
    //<editor-fold desc="Code for Variable Declaration">
    String lang = "en", userId, custId, custName, cashAmt, creditAmt, From, responseJSON;
    private UserSessionManager session;
    //</editor-fold>
    //<editor-fold desc="Code to Declare Controls">
    private TextView tvCustomerId, tvCustomer, tvCash, tvCredit;
    private EditText etReconcileAmount, etRemarks;
    private Button btnSave;
    //</editor-fold>


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconcile);

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
            custId = extras.getString("Id");
            custName = extras.getString("Name");
            cashAmt = extras.getString("Cash");
            creditAmt = extras.getString("Credit");
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
        tvCustomerId = (TextView) findViewById(R.id.tvCustomerId);
        tvCustomer = (TextView) findViewById(R.id.tvCustomer);
        tvCash = (TextView) findViewById(R.id.tvCash);
        tvCredit = (TextView) findViewById(R.id.tvCredit);
        etReconcileAmount = (EditText) findViewById(R.id.etReconcileAmount);
        etRemarks = (EditText) findViewById(R.id.etRemarks);
        btnSave = (Button) findViewById(R.id.btnSave);
        etReconcileAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etReconcileAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to executed on On Focus Changed">
        etReconcileAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    double reconcileAmt = 0.0;
                    if (Pattern.matches(fpRegex, etReconcileAmount.getText())) {

                        if (etReconcileAmount.getText().toString().trim().equals("") || etReconcileAmount.getText().toString().trim().equals("."))
                            reconcileAmt = 0.0;
                        else
                            reconcileAmt = Double.valueOf(etReconcileAmount.getText().toString());
                    } else
                        etReconcileAmount.setText("");

                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on change of text">
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                double reconcileAmt = 0.0;
                if (!etReconcileAmount.getText().toString().equalsIgnoreCase(".")) {
                    if (etReconcileAmount.getText().toString().equalsIgnoreCase("."))
                        etReconcileAmount.setText("");
                    if (etReconcileAmount.getText().toString().trim().length() > 0) {
                        if (Pattern.matches(fpRegex, etReconcileAmount.getText())) {
                            if (etReconcileAmount.getText().toString().trim().equals("") || etReconcileAmount.getText().toString().trim().equals("."))
                                reconcileAmt = 0.0;
                            else
                                reconcileAmt = Double.valueOf(etReconcileAmount.getText().toString());

                        } else {
                            etReconcileAmount.setText("");
                        }
                    } else {


                    }
                } else {
                    etReconcileAmount.setText("");
                }


            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etReconcileAmount.addTextChangedListener(textWatcher);
        //</editor-fold>

        //<editor-fold desc="Code to Set Values in Controls">
        tvCustomerId.setText(custId);
        tvCustomer.setText(custName);
        tvCash.setText(common.convertToTwoDecimal(cashAmt));
        tvCredit.setText(common.convertToTwoDecimal(creditAmt));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Save Button">
        btnSave.setOnClickListener(new View.OnClickListener() {
            //When go button click
            @Override
            public void onClick(View arg0) {
                if (etReconcileAmount.getText().toString().trim().length() <= 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "कृपया राशि दर्ज करें" : "Please enter amount.");
                else if (Double.valueOf(etReconcileAmount.getText().toString()) <= 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "राशि शून्य नहीं हो सकती" : "Amount cannot be zero.");
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप सामंजस्य विवरण जमा करना चाहते हैं?" : "Are you sure, you want to submit reconciliation details?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (common.isConnected()) {
                                        AsyncReconciliationWSCall task = new AsyncReconciliationWSCall();
                                        task.execute();
                                    }

                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertnew = builder1.create();
                    alertnew.show();
                }
            }
        });
        //</editor-fold>
    }


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
                Intent i;
                if (From.equalsIgnoreCase("Admin"))
                    i = new Intent(ActivityReconcile.this, ActivityAdminHomeScreen.class);
                else
                    i = new Intent(ActivityReconcile.this, ActivityHomeScreen.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to b executed on Back Press">
    @Override
    public void onBackPressed() {
        Intent i = new Intent(ActivityReconcile.this, ActivitySearchCustomer.class);
        i.putExtra("From", From);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>

    //<editor-fold desc="Code to Post Reconciliation Data">
    private class AsyncReconciliationWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityReconcile.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"customerId", "existingAmount", "newAmount", "remarks", "uniqueId", "userId", "ip", "machine"};
                String[] value = {custId, cashAmt, Double.valueOf(etReconcileAmount.getText().toString()).toString(), etRemarks.getText().toString(), UUID.randomUUID().toString(), userId, common.getDeviceIPAddress(true), common.getIMEI()};
                // Call method of web service to Read Customers For Reconciliation
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "CreateReconciliation", common.url);
                return responseJSON;
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
                if (result.contains("SUCCESS")) {
                    common.showToast(lang.equalsIgnoreCase("hi") ? "सामंजस्य विवरण सफलतापूर्वक सहेजे गए" : "Reconciliation details saved successfully.");
                    Intent intent = new Intent(ActivityReconcile.this, ActivitySearchCustomer.class);
                    intent.putExtra("From", From);
                    startActivity(intent);
                    finish();
                } else if (result.contains("ERROR:")) {
                    common.showToast(result);
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showToast(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Posting Reconciliation failed: " + e.toString());
                /*Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
                startActivity(intent);
				finish();*/
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Posting Reconciliation Data..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

}
