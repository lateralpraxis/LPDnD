package lateralpraxis.lpdnd.stockconversion;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityCreateStockConversion extends Activity {

    private final Context mContext = this;
    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    String lang = "en";
    String type = "Raw";
    private UserSessionManager session;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private RadioGroup RadioType;
    private RadioButton RadioRaw, RadioSKU;
    private Spinner spRawMaterial, spSKU, spProdSKU;
    private LinearLayout llRawMaterial, llSKU, llProduced;
    private EditText etConsumedQty, etProducedQty;
    private Button btnAddConsumed, btnAddProduced;
    private TextView tvProdEmpty, tvConsEmpty,tvInventory;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stock_conversion);

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
        spProdSKU = (Spinner) findViewById(R.id.spProdSKU);
        etConsumedQty = (EditText) findViewById(R.id.etConsumedQty);
        etProducedQty = (EditText) findViewById(R.id.etProducedQty);
        btnAddConsumed = (Button) findViewById(R.id.btnAddConsumed);
        btnAddProduced = (Button) findViewById(R.id.btnAddProduced);
        tvProdEmpty = (TextView) findViewById(R.id.tvProdEmpty);
        tvConsEmpty = (TextView) findViewById(R.id.tvConsEmpty);
        tvInventory = (TextView) findViewById(R.id.tvInventory);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
        etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etProducedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2)});
        etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to Bind Spinners">
        spRawMaterial.setAdapter(DataAdapter("rawmaterialinv", ""));
        spSKU.setAdapter(DataAdapter("skuinv", ""));
        spProdSKU.setAdapter(DataAdapter("sku", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be exceuted on change of Radio Button">
        RadioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = RadioType.findViewById(checkedId);
                int index = RadioType.indexOfChild(radioButton);
                spRawMaterial.setSelection(0);
                spSKU.setSelection(0);
                etConsumedQty.setText("");
                if (index == 0) {
                    llRawMaterial.setVisibility(View.VISIBLE);
                    llSKU.setVisibility(View.GONE);
                    etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    type="Raw";
                } else {
                    llRawMaterial.setVisibility(View.GONE);
                    llSKU.setVisibility(View.VISIBLE);
                    type="SKU";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change on Consumed SKU Spinner">
        spSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etProducedQty.setText("");
                db.open();
                tvInventory.setText(db.getSkuInventory(((CustomType) spSKU.getSelectedItem()).getId().split("~")[0]));
                db.close();
                if (((CustomType) spSKU.getSelectedItem()).getId().split("~")[1].equalsIgnoreCase("0")) {
                    etProducedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 4;
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

        //<editor-fold desc="Code to be executed on Selected Index change on Consumed Raw Material Spinner">
        spRawMaterial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etProducedQty.setText("");
                db.open();
                tvInventory.setText(db.getRawMaterialInventory(((CustomType) spRawMaterial.getSelectedItem()).getId()));
                db.close();
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
                etConsumedQty.setText("");
                if (((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[1].equalsIgnoreCase("0")) {
                    etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 4;
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
}
