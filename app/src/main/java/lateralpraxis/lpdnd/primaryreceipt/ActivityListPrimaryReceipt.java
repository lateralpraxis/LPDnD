package lateralpraxis.lpdnd.primaryreceipt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import lateralpraxis.lpdnd.R;

public class ActivityListPrimaryReceipt extends Activity {

    //<editor-fold desc="Code for Control Declaration">
    private TextView linkAddPrimaryReceipt;
    //</editor-fold>

    //<editor-fold desc="Code for class declaration">
    private Intent intent;
    Context context;
    //</editor-fold>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_primary_receipt);

        //<editor-fold desc="Code for finding controls">
        linkAddPrimaryReceipt = (TextView) findViewById(R.id.linkAddPrimaryReceipt);
        //</editor-fold>

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for clicking ob Link Add Click">
        linkAddPrimaryReceipt.setOnClickListener(new View.OnClickListener() {
            //On click of view delivery button
            @Override
            public void onClick(View arg0) {
                intent = new Intent(context, ActivityAddPrimaryReceipt.class);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>
    }
}
