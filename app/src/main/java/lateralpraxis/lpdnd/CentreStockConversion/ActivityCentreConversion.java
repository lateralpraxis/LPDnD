package lateralpraxis.lpdnd.CentreStockConversion;

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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

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

public class ActivityCentreConversion extends Activity {
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
    String type = "Raw";
    String responseJSON, sendJSon;
    private UserSessionManager session;
    //</editor-fold>
    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> wordListCons = null;
    private ArrayList<HashMap<String, String>> listCons;
    private int conslistSize = 0;

    private ArrayList<HashMap<String, String>> wordListProd = null;
    private ArrayList<HashMap<String, String>> listProd;
    private int prodlistSize = 0;
    private String uniqueId = "";
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private Spinner spSKU, spProdSKU;
    private LinearLayout llProduced;
    private EditText etConsumedQty, etProducedQty;
    private Button btnAddConsumed, btnAddProduced, btnSubmit;
    private TextView tvProdEmpty, tvConsEmpty, tvInventory, tvViewQty;
    private ListView listConsumed, listProduced;
    private TableLayout tableGridHeadConsumed, tableGridHeadProduced;
    //</editor-fold>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centre_conversion);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        //</editor-fold>

        //<editor-fold desc="Code to Delete Data from Temporary Table">
        db.open();
        db.DeleteTempConversion();
        db.close();
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
        llProduced = (LinearLayout) findViewById(R.id.llProduced);
        spSKU = (Spinner) findViewById(R.id.spSKU);
        spProdSKU = (Spinner) findViewById(R.id.spProdSKU);
        etConsumedQty = (EditText) findViewById(R.id.etConsumedQty);
        etProducedQty = (EditText) findViewById(R.id.etProducedQty);
        btnAddConsumed = (Button) findViewById(R.id.btnAddConsumed);
        btnAddProduced = (Button) findViewById(R.id.btnAddProduced);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvProdEmpty = (TextView) findViewById(R.id.tvProdEmpty);
        tvConsEmpty = (TextView) findViewById(R.id.tvConsEmpty);
        tvInventory = (TextView) findViewById(R.id.tvInventory);
        tvViewQty = (TextView) findViewById(R.id.tvViewQty);
        listConsumed = (ListView) findViewById(R.id.listConsumed);
        listProduced = (ListView) findViewById(R.id.listProduced);
        tableGridHeadConsumed = (TableLayout) findViewById(R.id.tableGridHeadConsumed);
        tableGridHeadProduced = (TableLayout) findViewById(R.id.tableGridHeadProduced);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(5);
        etConsumedQty.setFilters(FilterArray);
        etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] FilterProdArray = new InputFilter[1];
        FilterProdArray[0] = new InputFilter.LengthFilter(5);
        etProducedQty.setFilters(FilterProdArray);
        etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        //</editor-fold>
        //<editor-fold desc="Code to be executed on change of text">
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!etConsumedQty.getText().toString().equalsIgnoreCase(".")) {
                    if (etConsumedQty.getText().toString().equalsIgnoreCase("."))
                        etConsumedQty.setText("");

                } else {
                    etConsumedQty.setText("");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etConsumedQty.addTextChangedListener(textWatcher);

        TextWatcher textWatcherprod = new TextWatcher() {
            public void afterTextChanged(Editable s) {

                if (!etProducedQty.getText().toString().equalsIgnoreCase(".")) {
                    if (etProducedQty.getText().toString().equalsIgnoreCase("."))
                        etProducedQty.setText("");

                } else {
                    etProducedQty.setText("");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etProducedQty.addTextChangedListener(textWatcherprod);

        etProducedQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    if(Pattern.matches(fpRegex, etProducedQty.getText()))
                    {

                    }
                    else
                        etProducedQty.setText("");

                }
            }
        });
        etConsumedQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    if(Pattern.matches(fpRegex, etConsumedQty.getText()))
                    {

                    }
                    else
                        etConsumedQty.setText("");

                }
            }
        });
        //</editor-fold>
        //<editor-fold desc="Code to Bind Spinners">
        spSKU.setAdapter(DataAdapter("skuinv", ""));
        spProdSKU.setAdapter(DataAdapter("sku", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change on Consumed SKU Spinner">
        spSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etConsumedQty.setText("");
                db.open();
                tvInventory.setText(db.getSkuInventory(((CustomType) spSKU.getSelectedItem()).getId()));
                tvViewQty.setText(db.getSkuInventory(((CustomType) spSKU.getSelectedItem()).getId()));
                db.close();
                if (((CustomType) spSKU.getSelectedItem()).getId().split("-")[1].equalsIgnoreCase("0")) {
                    etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 5;
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    etConsumedQty.setFilters(FilterArray);
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change on Produced SKU Spinner">
        spProdSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etProducedQty.setText("");
                if (((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[1].equalsIgnoreCase("0")) {
                    etProducedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 5;
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    etProducedQty.setFilters(FilterArray);
                    etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click ob Button Add Consumed Click">
        btnAddConsumed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                /*etConsumedQty.clearFocus();
                 if (type.equalsIgnoreCase("SKU") && spSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पाद अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etConsumedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत मात्रा अनिवार्य है।" : "Consumed quantity is mandatory.");
                else if (Double.valueOf(etConsumedQty.getText().toString()) > Double.valueOf(tvInventory.getText().toString()))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत मात्रा उपलब्ध मात्रा से अधिक नहीं हो सकती।" : "Consumed quantity cannot exceed available quantity.");
                else {
                    db.openR();
                   // Boolean alreadyAdded = db.isConsumedAlreadyAdded(((CustomType) spRawMaterial.getSelectedItem()).getId(), ((CustomType) spSKU.getSelectedItem()).getId().split("-")[0]);
                   *//* if (alreadyAdded)
                        common.showToast(lang.equalsIgnoreCase("hi") ? "खपत आइटम पहले ही जोड़ दिया गया है।" : "Consumed item already added.");*//*
                    else {
                        db.open();
                        db.Insert_OutletConversionConsumedTemp(((CustomType) spRawMaterial.getSelectedItem()).getId(), ((CustomType) spSKU.getSelectedItem()).getId().split("-")[0], Double.valueOf(etConsumedQty.getText().toString()).toString());
                        db.close();
                        spSKU.setSelection(0);
                        etConsumedQty.setText("");
                        common.showToast(lang.equalsIgnoreCase("hi") ? "खपत किया गया आइटम सफलतापूर्वक जोड़ा गया।" : "Consumed item added successfully.");

                        wordListCons = new ArrayList<HashMap<String, String>>();
                        db.openR();
                        wordListCons = db.getTempConsumed();
                        conslistSize = wordListCons.size();
                        if (conslistSize != 0) {
                            listConsumed.setAdapter(new CustomAdapter(mContext, wordListCons));

                            ViewGroup.LayoutParams params = listConsumed.getLayoutParams();
                            listConsumed.setLayoutParams(params);
                            listConsumed.requestLayout();
                            tvConsEmpty.setVisibility(View.GONE);
                            tableGridHeadConsumed.setVisibility(View.VISIBLE);
                            llProduced.setVisibility(View.VISIBLE);
                        } else {
                            listConsumed.setAdapter(null);
                            tvConsEmpty.setVisibility(View.VISIBLE);
                            tableGridHeadConsumed.setVisibility(View.GONE);
                            llProduced.setVisibility(View.GONE);
                        }
                    }
                }*/

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click ob Button Add Produced Click">
        btnAddProduced.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                etProducedQty.clearFocus();
                if (spProdSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "एसकेयू अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etProducedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित मात्रा अनिवार्य है।" : "Produced quantity is mandatory.");
                else {
                    db.openR();
                    Boolean alreadyAdded = db.isProducedAlreadyAdded(((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[0]);
                    if (alreadyAdded)
                        common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित आइटम पहले ही जोड़ दिया गया है।" : "Produced item already added.");
                    else {
                        db.open();
                        db.Insert_OutletConversionProducedTemp(((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[0], Double.valueOf(etProducedQty.getText().toString()).toString());
                        db.close();
                        spProdSKU.setSelection(0);
                        etProducedQty.setText("");
                        common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित किया गया आइटम सफलतापूर्वक जोड़ा गया।" : "Produced item added successfully.");

                        wordListProd = new ArrayList<HashMap<String, String>>();
                        db.openR();
                        wordListProd = db.getTempProduced();
                        prodlistSize = wordListProd.size();
                        if (prodlistSize != 0) {
                           // listProduced.setAdapter(new CustomAdapterProduced(mContext, wordListProd));

                            ViewGroup.LayoutParams params = listProduced.getLayoutParams();
                            listProduced.setLayoutParams(params);
                            listProduced.requestLayout();
                            tvProdEmpty.setVisibility(View.GONE);
                            tableGridHeadProduced.setVisibility(View.VISIBLE);
                            btnSubmit.setVisibility(View.VISIBLE);
                        } else {
                            listProduced.setAdapter(null);
                            tvProdEmpty.setVisibility(View.VISIBLE);
                            tableGridHeadProduced.setVisibility(View.GONE);
                            btnSubmit.setVisibility(View.GONE);
                        }
                    }
                }

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Button Submit Click">
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक कन्वर्सन सुरक्षित करना चाहते हैं?" : "Are you sure, you want to save stock conversion transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (common.isConnected()) {
                                    /*AsyncOutletConversionWSCall task = new AsyncOutletConversionWSCall();
                                    task.execute();*/
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
        });
        //</editor-fold>
    }

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
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Action Bar Menu Item">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_go_to_home:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक रूपांतरण मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock conversion module it will discard stock conversion transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent i = new Intent(ActivityCentreConversion.this, ActivityAdminHomeScreen.class);
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
        builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक रूपांतरण मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock conversion module it will discard stock conversion transaction?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent i = new Intent(ActivityCentreConversion.this, ActivityListCentreConversion.class);
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
}
