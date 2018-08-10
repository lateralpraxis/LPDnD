package lateralpraxis.lpdnd.ExcessConfirmation;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityCashDeposit;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ExcessConfirmationList extends Activity {

    /*Start of code for Variable Declaration*/
    private String lang,userId;
    private int listSize = 0;
    /*End of code for Variable Declaration*/
	/*Start of code to declare class*/
    DatabaseAdapter db;
    Common common;
    ActivityCashDeposit.CustomAdapter Cadapter;
    private UserSessionManager session;
    private final Context mContext = this;
	/*End of code to declare class*/

    /*Start of code to declare Controls*/
    private ListView listViewMain;
    private TextView tvEmpty;
	/*End of code to declare Controls*/

    //On create method similar to page load
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Code to set layout
        setContentView(R.layout.activity_expense_confirmation_view);
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //Code to create instance of classes
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
		/*		*/
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        lang= session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        //Start of code to find Controls
        listViewMain = (ListView) findViewById(R.id.listViewMain);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        //Start of Code to Bind Data in List
        db.openR();
        ArrayList<HashMap<String, String>> listData = db.getExcessConfirmationData();

        listSize = listData.size();
        if (listSize != 0) {
            listViewMain.setAdapter(new ReportListAdapter(mContext, listData));
            ViewGroup.LayoutParams params = listViewMain.getLayoutParams();
            //params.height = 500;
            listViewMain.setLayoutParams(params);
            listViewMain.requestLayout();
            tvEmpty.setVisibility(View.GONE);
        } else {
            listViewMain.setAdapter(null);
            tvEmpty.setVisibility(View.VISIBLE);
        }
        //End of Code to Bind Data in List
        listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ExcessConfirmationList.this, ExcessConfirmationCreate.class);
                intent.putExtra("Id", String.valueOf(((TextView) item.findViewById(R.id.tvId)).getText().toString()));
                startActivity(intent);
                finish();
            }
        });
    }

    //<editor-fold desc="Code Binding Data In List">
    public static class viewHolder {
        TextView tvId, tvDate, tvCustomer, tvExpenseHead, tvAmount;
        int ref;
        TableRow tableHeader;
    }

    private class ReportListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> _listData;
        String _type;
        private Context context2;

        public ReportListAdapter(Context context,
                                 ArrayList<HashMap<String, String>> listData) {
            this.context2 = context;
            inflater = LayoutInflater.from(context2);
            _listData = listData;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return _listData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final viewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.activity_expense_confirmation_view_item, null);
                holder = new viewHolder();
                convertView.setTag(holder);

            } else {
                holder = (viewHolder) convertView.getTag();
            }
            holder.ref = position;
            holder.tvId = (TextView) convertView
                    .findViewById(R.id.tvId);
            holder.tvDate = (TextView) convertView
                    .findViewById(R.id.tvDate);
            holder.tvCustomer = (TextView) convertView
                    .findViewById(R.id.tvCustomer);
            holder.tvExpenseHead = (TextView) convertView
                    .findViewById(R.id.tvExpenseHead);
            holder.tvAmount = (TextView) convertView
                    .findViewById(R.id.tvAmount);
            holder.tableHeader = (TableRow) convertView.findViewById(R.id.tableHeader);

            final HashMap<String,String> itemData = _listData.get(position);
            holder.tvId.setText(itemData.get("Id"));
            holder.tvCustomer.setText(itemData.get("CustomerName"));
            holder.tvExpenseHead.setText(itemData.get("ExpenseHead"));
            holder.tvAmount.setText(common.convertToTwoDecimal(String.valueOf(itemData.get("Amount"))));
            holder.tvDate.setText(common.convertToDisplayDateFormat(itemData.get("ExpenseDate")));
            if(itemData.get("Flag").equalsIgnoreCase("1"))
                holder.tableHeader.setVisibility(View.GONE);
            else
                holder.tableHeader.setVisibility(View.VISIBLE);
            if(itemData.get("Flag1").equalsIgnoreCase("1")) {
                holder.tvCustomer.setVisibility(View.GONE);
            }
            else {
                holder.tvCustomer.setVisibility(View.VISIBLE);
            }
            if(itemData.get("Flag2").equalsIgnoreCase("1")) {
                holder.tvExpenseHead.setVisibility(View.GONE);
            }
            else {
                holder.tvExpenseHead.setVisibility(View.VISIBLE);
            }
            convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;        }

    }
    //</editor-fold>


    //Code to go to intent on selection of menu item
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:

                Intent i = new Intent(ExcessConfirmationList.this,ActivityAdminHomeScreen.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
                return true;

            case R.id.action_go_to_home:
                Intent homeScreenIntent = new Intent(this, ActivityAdminHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // To create menu on inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    // When press back button go to home screen
    @Override
    public void onBackPressed() {
        Intent homeScreenIntent = new Intent(ExcessConfirmationList.this, ActivityAdminHomeScreen.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }

}
