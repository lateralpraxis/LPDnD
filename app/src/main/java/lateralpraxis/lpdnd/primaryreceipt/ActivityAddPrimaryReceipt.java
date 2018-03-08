package lateralpraxis.lpdnd.primaryreceipt;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityAddPrimaryReceipt extends Activity {

    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    private UserSessionManager session;
    private final Context mContext = this;
    String lang = "en";
    String type="Raw";
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private RadioGroup RadioType;
    private RadioButton RadioRaw, RadioSKU;
    private Spinner spRawMaterial, spSKU;
    private LinearLayout llRawMaterial, llSKU;
    private EditText etQty, etAmt;
    private Button btnSave;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_primary_receipt);

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

        //<editor-fold desc="Code to find controls">
        llRawMaterial = (LinearLayout) findViewById(R.id.llRawMaterial);
        llSKU = (LinearLayout) findViewById(R.id.llSKU);
        RadioType = (RadioGroup) findViewById(R.id.RadioType);
        RadioRaw = (RadioButton) findViewById(R.id.RadioRaw);
        RadioSKU = (RadioButton) findViewById(R.id.RadioSKU);
        spRawMaterial = (Spinner) findViewById(R.id.spRawMaterial);
        spSKU = (Spinner) findViewById(R.id.spSKU);
        etQty = (EditText) findViewById(R.id.etQty);
        etAmt = (EditText) findViewById(R.id.etAmt);
        btnSave = (Button) findViewById(R.id.btnSave);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        etQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etAmt.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to Bind Spinners">
        spRawMaterial.setAdapter(DataAdapter("rawmaterial", ""));
        spSKU.setAdapter(DataAdapter("sku", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be exceuted on change of Radio Button">
        RadioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = RadioType.findViewById(checkedId);
                int index = RadioType.indexOfChild(radioButton);
                spRawMaterial.setSelection(0);
                spSKU.setSelection(0);
                etAmt.setText("");
                etQty.setText("");
                if (index == 0) {
                    llRawMaterial.setVisibility(View.VISIBLE);
                    llSKU.setVisibility(View.GONE);
                    etQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    type="Raw";
                } else {
                    llRawMaterial.setVisibility(View.GONE);
                    llSKU.setVisibility(View.VISIBLE);
                    type="SKU";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change od SKU Spinner">
        spSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etQty.setText("");
                if (((CustomType) spSKU.getSelectedItem()).getId().split("~")[1].equalsIgnoreCase("0")) {
                    etQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 4;
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    etQty.setFilters(FilterArray);
                    etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Save Button">
        btnSave.setOnClickListener(new View.OnClickListener() {
            //When go button click
            @Override
            public void onClick(View arg0) {
                if(type.equalsIgnoreCase("Raw") && ((CustomType)spRawMaterial.getSelectedItem()).getId().equalsIgnoreCase("0"))
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया कच्चे माल का चयन करें":"Please select Raw Material.");
                else if(type.equalsIgnoreCase("SKU") && ((CustomType)spSKU.getSelectedItem()).getId().split("~")[0].equalsIgnoreCase("0"))
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया एसकेयू चुनें":"Please select SKU.");
                else if(etQty.getText().toString().trim().length()<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया मात्रा दर्ज करें":"Please enter quantity.");
                else if (Double.valueOf(etQty.getText().toString())<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"मात्रा शून्य नहीं हो सकती।":"Quantity cannot be zero.");
                else if(etAmt.getText().toString().trim().length()<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया राशि दर्ज करें":"Please enter amount.");
                else if (Double.valueOf(etAmt.getText().toString())<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"राशि शून्य नहीं हो सकती":"Amount cannot be zero.");
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप प्राथमिक रसीद लेनदेन जमा करना चाहते हैं?" : "Are you sure, you want to submit primary receipt transaction?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {

                                    String customerId = "";
                                    HashMap<String, String> user = session.getLoginUserDetails();
                                    customerId = user.get(UserSessionManager.KEY_ID);
                                    db.open();
                                    db.Insert_PrimaryReceipt(customerId, ((CustomType) spRawMaterial.getSelectedItem()).getId(), ((CustomType) spSKU.getSelectedItem()).getId().split("~")[0], etQty.getText().toString(), etAmt.getText().toString());
                                    db.close();
                                    common.showToast(lang.equalsIgnoreCase("hi") ? "प्राथमिक रसीद को सफलतापूर्वक सहेजा गया" : "Primary Receipt saved successfully.");
                                    Intent intent = new Intent(ActivityAddPrimaryReceipt.this, ActivityListPrimaryReceipt.class);
                                    startActivity(intent);
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप प्राथमिक रसीद मॉड्यूल छोड़ना चाहते हैं, यह प्राथमिक रसीद को छोड़ देगा?" : "Are you sure, you want to leave primary receipt module it will discard primary receipt transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent i = new Intent(ActivityAddPrimaryReceipt.this, ActivityHomeScreen.class);
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
        builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप प्राथमिक रसीद मॉड्यूल छोड़ना चाहते हैं, यह प्राथमिक रसीद को छोड़ देगा?" : "Are you sure, you want to leave primary receipt module it will discard primary receipt transaction?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent i = new Intent(ActivityAddPrimaryReceipt.this, ActivityListPrimaryReceipt.class);
                        startActivity(i);
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
