package lateralpraxis.lpdnd.primaryreceipt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityListPrimaryReceipt extends Activity {

    //<editor-fold desc="Code for Control Declaration">
    private TextView linkAddPrimaryReceipt,tvEmpty;
    private ListView listReceipt;
    //</editor-fold>

    //<editor-fold desc="Code for class declaration">
    private Intent intent;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> wordList = null;
    private ArrayList<HashMap<String, String>> list;
    private int listSize = 0;
    //</editor-fold>


    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    private UserSessionManager session;
    String lang = "en";
    private final Context mContext = this;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_primary_receipt);

        //<editor-fold desc="Code for finding controls">
        linkAddPrimaryReceipt = (TextView) findViewById(R.id.linkAddPrimaryReceipt);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listReceipt= (ListView) findViewById(R.id.listReceipt);
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

        //<editor-fold desc="Code for clicking ob Link Add Click">
        linkAddPrimaryReceipt.setOnClickListener(new View.OnClickListener() {
            //On click of view delivery button
            @Override
            public void onClick(View arg0) {
                intent = new Intent(ActivityListPrimaryReceipt.this, ActivityAddPrimaryReceipt.class);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        wordList = new ArrayList<HashMap<String, String>>();
        db.open();
        wordList = db.getPrimaryReceipts();
        db.close();
        listSize = wordList.size();
        if (listSize != 0) {
            listReceipt.setAdapter(new CustomAdapter(mContext, wordList));

            ViewGroup.LayoutParams params = listReceipt.getLayoutParams();
            listReceipt.setLayoutParams(params);
            listReceipt.requestLayout();
            tvEmpty.setVisibility(View.GONE);
        } else {
            listReceipt.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to be Bind Data in list view">
    public static class ViewHolder {
        TextView tvCode, tvDate,tvName,tvQty,tvAmt;
    }

    public class CustomAdapter extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
            this.docContext = context;
            mInflater = LayoutInflater.from(docContext);
            list = lvList;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public int getViewTypeCount() {

            return getCount();
        }

        @Override
        public int getItemViewType(int position) {

            return position;
        }

        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {


            final ViewHolder holder;
            if (arg1 == null) {
                arg1 = mInflater.inflate(R.layout.list_primary_receipt, null);
                holder = new ViewHolder();

                holder.tvName = (TextView) arg1.findViewById(R.id.tvName);
                holder.tvCode = (TextView) arg1.findViewById(R.id.tvCode);
                holder.tvDate = (TextView) arg1.findViewById(R.id.tvDate);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                holder.tvAmt = (TextView) arg1.findViewById(R.id.tvAmt);

                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvCode.setText(list.get(arg0).get("Code"));
            holder.tvDate.setText(common.convertToDisplayDateFormat(list.get(arg0).get("Date")));
            holder.tvName.setText(list.get(arg0).get("Name"));
            holder.tvQty.setText(list.get(arg0).get("Quantity"));
            holder.tvAmt.setText(common.convertToTwoDecimal(list.get(arg0).get("Amount")));


            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
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

        Intent i = new Intent(ActivityListPrimaryReceipt.this, ActivityHomeScreen.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>
}
