package lateralpraxis.lpdnd.ExpenseBooking;

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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;


public class ActivityListBooking extends Activity {

    private final Context mContext = this;
    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    //</editor-fold>
    String lang = "en";
    //<editor-fold desc="Code for Control Declaration">
    private TextView linkAddExpense, tvEmpty;
    private ListView listExpense;
    private TableLayout tableGridHead;
    private UserSessionManager session;
    private Intent intent;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> wordList = null;
    private ArrayList<HashMap<String, String>> list;
    private int listSize = 0;

    //</editor-fold>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_booking);

        //<editor-fold desc="Code for finding controls">
        linkAddExpense = (TextView) findViewById(R.id.linkAddExpense);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        listExpense = (ListView) findViewById(R.id.listExpense);
        tableGridHead = (TableLayout) findViewById(R.id.tableGridHead);
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
        linkAddExpense.setOnClickListener(new View.OnClickListener() {
            //On click of view delivery button
            @Override
            public void onClick(View arg0) {
                intent = new Intent(ActivityListBooking.this, ActivityAddExpense.class);
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>

        wordList = new ArrayList<HashMap<String, String>>();
        db.open();
        wordList = db.getExpenseDetails();
        db.close();
        listSize = wordList.size();
        if (listSize != 0) {
            listExpense.setAdapter(new CustomAdapter(mContext, wordList));

            ViewGroup.LayoutParams params = listExpense.getLayoutParams();
            listExpense.setLayoutParams(params);
            listExpense.requestLayout();
            tvEmpty.setVisibility(View.GONE);
            tableGridHead.setVisibility(View.VISIBLE);
        } else {
            listExpense.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
            tableGridHead.setVisibility(View.GONE);
        }
    }

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

        Intent i = new Intent(ActivityListBooking.this, ActivityHomeScreen.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>

    //<editor-fold desc="Code to be Bind Data in list view">
    public static class ViewHolder {
        TextView tvDate, tvExpenseHead, tvAmount;
        TableRow tableRowDate;
    }
    //</editor-fold>

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
                arg1 = mInflater.inflate(R.layout.list_expense_details, null);
                holder = new ViewHolder();

                holder.tvExpenseHead = (TextView) arg1.findViewById(R.id.tvExpenseHead);
                holder.tvDate = (TextView) arg1.findViewById(R.id.tvDate);
                holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
                holder.tableRowDate = (TableRow) arg1.findViewById(R.id.tableRowDate);
                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvExpenseHead.setText(list.get(arg0).get("Name"));
            holder.tvDate.setText(common.convertToDisplayDateFormat(list.get(arg0).get("Date")));
            holder.tvAmount.setText(common.convertToTwoDecimal(list.get(arg0).get("Amount")));
            if (list.get(arg0).get("Flag").equalsIgnoreCase("0"))
                holder.tableRowDate.setVisibility(View.GONE);
            else
                holder.tableRowDate.setVisibility(View.VISIBLE);

            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }
    //</editor-fold>
}
