package lateralpraxis.lpdnd.StockAdjustment;

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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.types.CustomType;

public class CentreStockAdjustmentCreate extends Activity {

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
    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    String lang = "en";
    String responseJSON, sendJSon;
    private UserSessionManager session;
    //</editor-fold>
    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> wordListCons = null;
    private ArrayList<HashMap<String, String>> listCons;
    private ArrayList<HashMap<String, String>> wordListProd = null;
    private ArrayList<HashMap<String, String>> listProd;
    private int prodlistSize = 0;
    private String uniqueId = "";
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private Spinner spCentre, spSKU;
    private LinearLayout llSKU, llCentre, llMain ;
    private EditText etAdjustedQty, etRemarks;
    private Button btnAdd, btnGo;
    private TextView tvInventory, tvAvailable, tvAdjusted, tvAdjustedLabel, tvCentreId, tvCentreName;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centre_create_stock_adjustment);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("UniqueId");
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

        //<editor-fold desc="Code to find controls">
        llCentre = (LinearLayout) findViewById(R.id.llCentre);
        llMain = (LinearLayout) findViewById(R.id.llMain);
        llSKU = (LinearLayout) findViewById(R.id.llSKU);
        spCentre = (Spinner) findViewById(R.id.spCentre);
        spSKU = (Spinner) findViewById(R.id.spSKU);
        etAdjustedQty = (EditText) findViewById(R.id.etAdjustedQty);
        etRemarks = (EditText) findViewById(R.id.etRemarks);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnGo = (Button) findViewById(R.id.btnGo);
        tvInventory = (TextView) findViewById(R.id.tvInventory);
        tvAvailable = (TextView) findViewById(R.id.tvAvailable);
        tvAdjusted = (TextView) findViewById(R.id.tvAdjusted);
        tvAdjustedLabel = (TextView) findViewById(R.id.tvAdjustedLabel);
        tvCentreId = (TextView) findViewById(R.id.tvCentreId);
        tvCentreName = (TextView) findViewById(R.id.tvCentreName);
        tvInventory.setText("0");
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        etAdjustedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
        etAdjustedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to Bind Spinners">
        spCentre.setAdapter(DataAdapter("centreusercentre", ""));
        //</editor-fold>


        //<editor-fold desc="Code to be executed on Button Go Click">
        btnGo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (spCentre.getSelectedItemPosition() == 0) {
                    common.showToast(lang.equalsIgnoreCase("hi") ? "कृपया केंद्र का चयन करें" : "Please select centre.");
                } else {
                    llCentre.setVisibility(View.GONE);
                    llMain.setVisibility(View.VISIBLE);
                    tvCentreId.setText(((CustomType) spCentre.getSelectedItem()).getId());
                    tvCentreName.setText(((CustomType) spCentre.getSelectedItem()).getName());
                    spSKU.setAdapter(DataAdapter("centreskuinv", ((CustomType) spCentre.getSelectedItem()).getId()));
                }

            }
        });
        //</editor-fold>


        //<editor-fold desc="Code to be executed on Selected Index change on Consumed SKU Spinner">
        spSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etAdjustedQty.setText("");
                tvAdjustedLabel.setText("");
                tvAvailable.setText("");
                db.open();
                tvInventory.setText(db.getCentreSkuInventory(((CustomType) spSKU.getSelectedItem()).getId()).replace(".0",""));
                tvAvailable.setText(db.getCentreSkuInventory(((CustomType) spSKU.getSelectedItem()).getId()).replace(".0",""));
                db.close();
                if (((CustomType) spSKU.getSelectedItem()).getId().split("-")[1].equalsIgnoreCase("0")) {
                    etAdjustedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etAdjustedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 5;
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    etAdjustedQty.setFilters(FilterArray);
                    etAdjustedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>


        //On change of Return Quantity
        etAdjustedQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                // onFocus
                if (gainFocus) {
                }
                // onBlur
                else {
                    if (Pattern.matches(fpRegex, etAdjustedQty.getText())) {
                        if (etAdjustedQty.getText().toString().trim().length() > 0) {
                            if(TextUtils.isEmpty(tvInventory.getText().toString().trim()))
                                tvInventory.setText("0");
                            if (Double.valueOf(etAdjustedQty.getText().toString().trim()) > Double.valueOf(tvInventory.getText().toString().trim())) {
                                tvAdjusted.setText(common.stringToOneNewDecimal(String.valueOf((Double.valueOf(etAdjustedQty.getText().toString().trim()) -
                                        Double.valueOf(tvInventory.getText().toString().trim())))).replace(".0",""));
                                tvAdjustedLabel.setText("Gain");
                                tvAdjustedLabel.setVisibility(View.VISIBLE);
                            } else {
                                tvAdjusted.setText(common.stringToOneNewDecimal(String.valueOf((Double.valueOf(tvInventory.getText().toString().trim()) -
                                        Double.valueOf(etAdjustedQty.getText().toString().trim())))).replace(".0",""));
                                tvAdjustedLabel.setText("Loss");
                                tvAdjustedLabel.setVisibility(View.VISIBLE);
                            }
                        }
                        else {
                            tvAdjusted.setText("");
                            tvAdjustedLabel.setText("");
                            tvAdjustedLabel.setVisibility(View.GONE);
                        }
                    }
                    else {
                        tvAdjusted.setText("");
                        tvAdjustedLabel.setText("");
                        tvAdjustedLabel.setVisibility(View.GONE);
                    }
                }
            }
        });

        //<editor-fold desc="Code to be executed on change of text">
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!etAdjustedQty.getText().toString().equalsIgnoreCase(".")) {
                    if (etAdjustedQty.getText().toString().equalsIgnoreCase("."))
                        etAdjustedQty.setText("");

                } else {
                    etAdjustedQty.setText("");
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etAdjustedQty.addTextChangedListener(textWatcher);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click of Button ">
        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (spSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पाद अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etAdjustedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "मात्रा अनिवार्य है।" : "Quantity is mandatory.");
                else if (String.valueOf(etRemarks.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "टिप्पणी अनिवार्य है।" : "Remarks is mandatory.");
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक समायोजन सुरक्षित करना चाहते हैं?" : "Are you sure, you want to save stock adjustment transaction?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    if (common.isConnected()) {
                                        AsyncCreateCentreStockAdjustmentWSCall task = new AsyncCreateCentreStockAdjustmentWSCall();
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
    //</editor-fold>

    //<editor-fold desc="Code for Binding Data In Spinner">
    private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter) {
        db.open();
        List<CustomType> lables = db.GetCustomerMasterDetails(masterType, filter);
        ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this, android.R.layout.simple_spinner_item, lables);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        db.close();
        return dataAdapter;
    }
    //</editor-fold>

    //<editor-fold desc="Code to Set Home Button in Action Bar">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    //<editor-fold desc="Code to be executed on Action Bar Menu Item">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक समायोजन मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक समायोजन को छोड़ देगा?" : "Are you sure, you want to leave stock adjustment module it will discard stock adjustment transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent i = new Intent(CentreStockAdjustmentCreate.this, ActivityAdminHomeScreen.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
        builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
        builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक समायोजन मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock adjustment module it will discard stock adjustment transaction?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent i = new Intent(CentreStockAdjustmentCreate.this, ActivityAdminHomeScreen.class);
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

    //<editor-fold desc="Async Method to Post Stock Adjustment Details">
    private class AsyncCreateCentreStockAdjustmentWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(CentreStockAdjustmentCreate.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String userId = "";
                HashMap<String, String> user = session.getLoginUserDetails();
                userId = user.get(UserSessionManager.KEY_ID);
                String skuId = "";
                skuId = ((CustomType) spSKU.getSelectedItem()).getId().split("-")[0];
                String[] name = {"uniqueId", "centreId", "skuId", "currentInventory", "newInventory", "remarks", "userId", "ipAddress", "machine"};
                String[] value = {uniqueId, tvCentreId.getText().toString(), skuId, tvInventory.getText().toString(), etAdjustedQty.getText().toString(), etRemarks.getText().toString(), userId, common.getDeviceIPAddress(true), common.getIMEI()};
                responseJSON = "";
                // Call method of web service to stock adjustment from server
                responseJSON = common.CallJsonWS(name, value, "CreateCentreStockAdjustment", common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to Posting Stock Adjustment Details
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    if (responseJSON.equalsIgnoreCase("success")) {
                        common.showToast(lang.equalsIgnoreCase("hi") ? "स्टॉक समायोजन सफलतापूर्वक सहेजा गया" : "Stock Adjustment saved successfully.");
                        Intent i = new Intent(CentreStockAdjustmentCreate.this, CentreStockAdjustmentList.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(CentreStockAdjustmentCreate.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(CentreStockAdjustmentCreate.this, e.getMessage(), false);
            }
            Dialog.dismiss();
        }

        // To display Posting Stock Adjustment Message
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Posting Centre Stock Adjustment...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
//</editor-fold>
}
