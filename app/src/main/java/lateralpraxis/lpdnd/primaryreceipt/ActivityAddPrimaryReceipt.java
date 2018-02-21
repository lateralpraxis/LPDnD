package lateralpraxis.lpdnd.primaryreceipt;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import lateralpraxis.lpdnd.R;

public class ActivityAddPrimaryReceipt extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_primary_receipt);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>
    }
}
