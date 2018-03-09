package lateralpraxis.lpdnd.ExpenseBooking;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityAddExpense extends Activity {

    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    private UserSessionManager session;
    String lang = "en";
    private final Context mContext = this;
    private Intent intent;
    //</editor-fold>

    //<editor-fold desc="Code to Declare Controls">
    private Spinner spExpenseHead;
    private EditText etAmt,etRemarks;
    private Button btnSave;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

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

        //<editor-fold desc="Code to Find Controls">
        spExpenseHead = (Spinner) findViewById(R.id.spExpenseHead);
        etAmt = (EditText) findViewById(R.id.etAmt);
        etRemarks = (EditText) findViewById(R.id.etRemarks);
        btnSave = (Button) findViewById(R.id.btnSave);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        etAmt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etAmt.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to Bind Spinners">
        spExpenseHead.setAdapter(DataAdapter("exphead", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Save Button">
        btnSave.setOnClickListener(new View.OnClickListener() {
            //When go button click
            @Override
            public void onClick(View arg0) {
                if(((CustomType)spExpenseHead.getSelectedItem()).getId().equalsIgnoreCase("0"))
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया व्यय हेड का चयन करें":"Please select Expense Head.");
                else if(etAmt.getText().toString().trim().length()<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया राशि दर्ज करें":"Please enter amount.");
                else if (Double.valueOf(etAmt.getText().toString())<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"राशि शून्य नहीं हो सकती":"Amount cannot be zero.");
                else if(etRemarks.getText().toString().trim().length()<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया टिप्पणी दर्ज करें":"Please enter remarks.");
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप व्यय विवरण जमा करना चाहते हैं?" : "Are you sure, you want to submit expense details?");
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
                                    db.Insert_ExpenseBooking(customerId, ((CustomType) spExpenseHead.getSelectedItem()).getId(),Double.valueOf(etAmt.getText().toString()).toString(), etRemarks.getText().toString(), UUID.randomUUID().toString());
                                    db.close();
                                    common.showToast(lang.equalsIgnoreCase("hi") ? "व्यय विवरण सफलतापूर्वक सहेजा गया" : "Expense details saved successfully.");
                                    Intent intent = new Intent(ActivityAddExpense.this, ActivityListBooking.class);
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
                onBackPressed();
                return true;
            case R.id.action_go_to_home:
                Intent i = new Intent(ActivityAddExpense.this, ActivityHomeScreen.class);
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

        Intent i = new Intent(ActivityAddExpense.this, ActivityListBooking.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>
}
