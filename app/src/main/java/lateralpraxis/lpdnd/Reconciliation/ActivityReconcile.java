package lateralpraxis.lpdnd.Reconciliation;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityReconcile extends Activity {
    //<editor-fold desc="Code for Class Declaration">
    Common common;
    //</editor-fold>
    //<editor-fold desc="Code for Variable Declaration">
    String lang = "en", userId, custId, custName, cashAmt, creditAmt, From;
    //<editor-fold desc="Code to Declare Controls">
    private TextView tvCustomerId, tvCustomer;
    //</editor-fold>
    private UserSessionManager session;

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
        //</editor-fold>

        tvCustomerId.setText(custId);
        tvCustomer.setText(custName);
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

}
