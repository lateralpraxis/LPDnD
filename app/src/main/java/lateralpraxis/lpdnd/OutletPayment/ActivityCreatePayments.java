package lateralpraxis.lpdnd.OutletPayment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityCreatePayments extends Activity {

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
    //</editor-fold>
    private final Context mContext = this;
    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    //</editor-fold>
    String lang = "en";
    private UserSessionManager session;
    private Intent intent;
    //<editor-fold desc="Code for Control Declaration">
    private TextView tvPayableAmount, tvHiddenPayableAmount, tvBalanceData;
    private EditText etCashAmount;
    private Button btnSave;
    //<editor-fold desc="Code to declare Variables">
    private String userId = "";
    private String payableAmount = "";
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_payments);

        //<editor-fold desc="Code for finding controls">
        tvPayableAmount = (TextView) findViewById(R.id.tvPayableAmount);
        tvHiddenPayableAmount = (TextView) findViewById(R.id.tvHiddenPayableAmount);
        tvBalanceData = (TextView) findViewById(R.id.tvBalanceData);
        etCashAmount = (EditText) findViewById(R.id.etCashAmount);
        btnSave = (Button) findViewById(R.id.btnSave);
        //</editor-fold>

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
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

        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        //<editor-fold desc="Code to Set Payable Amount">
        db.openR();
        payableAmount = db.getCreditAmount(userId);
        tvHiddenPayableAmount.setText(payableAmount);
        if (Double.valueOf(payableAmount) < 0) {
            tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
            tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
        } else {
            tvPayableAmount.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
            tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
        }
        //</editor-fold>

        //<editor-fold desc="Code to Set Input Filter">
        etCashAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etCashAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on On Change of Edit Text Cash Amount">
        etCashAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    double cashAmt = 0.0;
                    double totalPayable = 0.0;
                    if (Pattern.matches(fpRegex, etCashAmount.getText())) {
                        if (tvPayableAmount.getText().toString().contains("("))
                            totalPayable = Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
                        else
                            totalPayable = Double.valueOf(tvPayableAmount.getText().toString().replace(",", "")) * -1;
                        if (etCashAmount.getText().toString().trim().equals("") || etCashAmount.getText().toString().trim().equals("."))
                            cashAmt = 0.0;
                        else
                            cashAmt = Double.valueOf(etCashAmount.getText().toString());


                        if (common.stringToTwoDecimal(String.valueOf(totalPayable + (cashAmt))).contains("-"))
                            tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(totalPayable + (cashAmt)))).replace("-", ""));
                        else
                            tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(totalPayable + (cashAmt)))) + ")");
                    } else {
                        etCashAmount.setText("");
                        //<editor-fold desc="Code to Set Payable Amount">
                        db.openR();
                        payableAmount = db.getCreditAmount(userId);
                        tvHiddenPayableAmount.setText(payableAmount);
                        if (Double.valueOf(payableAmount) < 0) {
                            tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                            tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                        } else {
                            tvPayableAmount.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                            tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                        }
                        //</editor-fold>
                    }

                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on change of text">
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                double cashAmt = 0.0;
                double totalPayable = 0.0;
                if (!etCashAmount.getText().toString().equalsIgnoreCase(".")) {
                    if (etCashAmount.getText().toString().equalsIgnoreCase("."))
                        etCashAmount.setText("");
                    if (etCashAmount.getText().toString().trim().length() > 0) {
                        if (Pattern.matches(fpRegex, etCashAmount.getText())) {
                            if (tvPayableAmount.getText().toString().contains("("))
                                totalPayable = Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
                            else
                                totalPayable = Double.valueOf(tvPayableAmount.getText().toString().replace(",", "")) * -1;
                            if (etCashAmount.getText().toString().trim().equals("") || etCashAmount.getText().toString().trim().equals("."))
                                cashAmt = 0.0;
                            else
                                cashAmt = Double.valueOf(etCashAmount.getText().toString());


                            if (common.stringToTwoDecimal(String.valueOf(totalPayable + (cashAmt))).contains("-"))
                                tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(totalPayable + (cashAmt)))).replace("-", ""));
                            else
                                tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(totalPayable + (cashAmt)))) + ")");
                        } else {
                            etCashAmount.setText("");
                            //<editor-fold desc="Code to Set Payable Amount">
                            db.openR();
                            payableAmount = db.getCreditAmount(userId);
                            tvHiddenPayableAmount.setText(payableAmount);
                            if (Double.valueOf(payableAmount) < 0) {
                                tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                                tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                            } else {
                                tvPayableAmount.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                                tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                            }
                            //</editor-fold>
                        }
                    } else {

                        //<editor-fold desc="Code to Set Payable Amount">
                        db.openR();
                        payableAmount = db.getCreditAmount(userId);
                        tvHiddenPayableAmount.setText(payableAmount);
                        if (Double.valueOf(payableAmount) < 0) {
                            tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                            tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                        } else {
                            tvPayableAmount.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                            tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                        }
                        //</editor-fold>
                    }
                } else {
                    etCashAmount.setText("");
                    //<editor-fold desc="Code to Set Payable Amount">
                    db.openR();
                    payableAmount = db.getCreditAmount(userId);
                    tvHiddenPayableAmount.setText(payableAmount);
                    if (Double.valueOf(payableAmount) < 0) {
                        tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                        tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount)).replace("-", "")));
                    } else {
                        tvPayableAmount.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                        tvBalanceData.setText("(" + common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(payableAmount))) + ")");
                    }
                    //</editor-fold>
                }


            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etCashAmount.addTextChangedListener(textWatcher);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Button Save Click">
        btnSave.setOnClickListener(new View.OnClickListener() {
            // When create button click
            @Override
            public void onClick(View arg0) {
                etCashAmount.clearFocus();

                if (etCashAmount.getText().toString().trim().equals("")) {
                    common.showToast(lang.equalsIgnoreCase("hi") ? "कृपया नकद राशि दर्ज करें।" : "Please enter cash amount.");
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप इस भुगतान को जमा करना चाहते हैं??" : "Are you sure, you want to submit this payment?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    db.open();
                                    db.Insert_OutletPaymentReceipt(userId, etCashAmount.getText().toString(), UUID.randomUUID().toString());
                                    db.close();
                                    common.showToast(lang.equalsIgnoreCase("hi") ? "भुगतान सफलतापूर्वक सहेजा गया।" : "payment saved successfully.");
                                    Intent i = new Intent(ActivityCreatePayments.this, ActivityListPayments.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
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
    //</editor-fold>

    //<editor-fold desc="Code to go to intent on selection of menu item">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_go_to_home:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप भुगतान मॉड्यूल छोड़ना चाहते हैं, यह भुगतान लेनदेन को छोड़ देगा?" : "Are you sure, you want to leave payment module it will discard payment transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent homeScreenIntent = new Intent(ActivityCreatePayments.this, ActivityHomeScreen.class);
                                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeScreenIntent);
                                finish();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>


    //<editor-fold desc="Event Triggered on Clicking Back">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
        builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
        builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप भुगतान मॉड्यूल छोड़ना चाहते हैं, यह भुगतान लेनदेन को छोड़ देगा?" : "Are you sure, you want to leave payment module it will discard payment transaction?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent i = new Intent(ActivityCreatePayments.this, ActivityListPayments.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
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
    //</editor-fold>

    //<editor-fold desc="To create menu on inflater">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
    //</editor-fold>
}
