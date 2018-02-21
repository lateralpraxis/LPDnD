package lateralpraxis.lpdnd.primaryreceipt;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityAddPrimaryReceipt extends Activity {

    LinearLayout llRawMaterial, llSKU;
    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    //<editor-fold desc="Code for Control Declaration">
    private RadioGroup RadioType;
    private RadioButton RadioRaw, RadioSKU;
    private Spinner spRawMaterial, spSKU;
    //</editor-fold>
    private EditText etQty, etAmt;
    private Button btnSave;
    private UserSessionManager session;
    //</editor-fold>

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
    }
}
