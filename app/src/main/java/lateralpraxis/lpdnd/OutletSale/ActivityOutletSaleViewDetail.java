package lateralpraxis.lpdnd.OutletSale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
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

@SuppressLint("InflateParams")
public class ActivityOutletSaleViewDetail extends Activity {

    //<editor-fold desc="Code to declare variables">
    final Context context = this;
    Common common;
    UserSessionManager session;
    double totalAmounts = 0;
    private DatabaseAdapter dba;
    private String id = "0", date, name;
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private ListView listViewMain;
    private TextView tvNoRecord, tvTotalAmount, tvHeader;
    private MainAdapter ListAdapter;
    private int cnt = 0;
    private int lsize = 0;
    private String lang;
    private View tvDivider;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on page load">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_sale_view_detail);

        //To create instance of user session
        session = new UserSessionManager(getApplicationContext());
        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        common = new Common(this);
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //To create instance of database and control
        dba = new DatabaseAdapter(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        listViewMain = (ListView) findViewById(R.id.listViewMain);
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        tvDivider = findViewById(R.id.tvDivider);
        tvDivider.setVisibility(View.GONE);

        //To extract id from bundle to show details
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("Id");
            date = extras.getString("Date");
            name = extras.getString("Name");
            if (lang.equalsIgnoreCase("hi"))
                tvHeader.setText("दिनांक: " + date + "  कोड: OS" + id + "  बिक्री का प्रकार: " + name);
            else
                tvHeader.setText("Date: " + date + "  Code: OS" + id + "  Sale Type: " + name);
        }

//To get outlet sale details from database
        dba.open();
        ArrayList<HashMap<String, String>> lables = null;
        lables = dba.getOutletSaleDetail(id);
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("Item", String.valueOf(lable.get("Item")));
                hm.put("ItemLocal", String.valueOf(lable.get("ItemLocal")));
                hm.put("Qty", String.valueOf(lable.get("Qty")));
                hm.put("Rate", String.valueOf(lable.get("Rate")));
                hm.put("Amount", String.valueOf(lable.get("Amount")));
                totalAmounts = totalAmounts + Double.valueOf(lable.get("Qty")) * Double.valueOf(lable.get("Rate").replace(",", ""));
                HeaderDetails.add(hm);
            }
        }
        dba.close();
        if (lsize == 0) {
            //To display no record message
            tvNoRecord.setVisibility(View.VISIBLE);
            listViewMain.setVisibility(View.GONE);
            tvDivider.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            tvNoRecord.setVisibility(View.GONE);
            listViewMain.setVisibility(View.VISIBLE);
            ListAdapter = new MainAdapter(ActivityOutletSaleViewDetail.this);
            listViewMain.setAdapter(ListAdapter);
            tvDivider.setVisibility(View.VISIBLE);

            if (lang.equalsIgnoreCase("hi"))
                tvTotalAmount.setText(Html.fromHtml("<b>कुल: " + common.stringToTwoDecimal(String.valueOf(totalAmounts)) + "</b>"));
            else
                tvTotalAmount.setText(Html.fromHtml("<b>Total: " + common.stringToTwoDecimal(String.valueOf(totalAmounts)) + "</b>"));
        }
    }
    //</editor-fold>

    //<editor-fold desc="To bind activity on menu item click">
    //When press back button go to home screen
    @Override
    public void onBackPressed() {
        Intent intent;
        intent = new Intent(this, ActivityOutletSaleViewSummary.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //To create menu on inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    //To bind activity on menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_to_home:
                Intent homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="To make class of outlet sale view holder">
    public class MainAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        //Main adapter of view holder
        public MainAdapter(Context context) {
            super();
            mInflater = LayoutInflater.from(context);
        }

        //To get item count
        @Override
        public int getCount() {
            return HeaderDetails.size();
        }

        ////To get item name
        @Override
        public Object getItem(int arg0) {
            return HeaderDetails.get(arg0);
        }

        ////To get item id
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        //To get item position
        @Override
        public int getItemViewType(int position) {

            return position;
        }

        //To instantiate layout XML file into its corresponding view objects.
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {
            cnt = cnt + 1;
            final ViewHolder holder;

            if (arg1 == null) {
                arg1 = mInflater.inflate(R.layout.activity_outlet_sale_view_detail_item, null);
                holder = new ViewHolder();

                holder.tvItem = (TextView) arg1.findViewById(R.id.tvItem);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                holder.tvRate = (TextView) arg1.findViewById(R.id.tvRate);
                holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            //To bind data into view holder
            holder.tvItem.setText((lang.equalsIgnoreCase("hi")) ? HeaderDetails.get(arg0).get("ItemLocal") : HeaderDetails.get(arg0).get("Item"));
            holder.tvQty.setText(HeaderDetails.get(arg0).get("Qty"));
            holder.tvRate.setText(common.stringToTwoDecimal(HeaderDetails.get(arg0).get("Rate")));
            holder.tvAmount.setText(common.stringToTwoDecimal(HeaderDetails.get(arg0).get("Amount")));

            //Code to check if row is even or odd and set set color for alternate rows
            /*if (arg0 % 2 == 1) {
                arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/
            return arg1;
        }

        class ViewHolder {
            TextView tvItem, tvQty, tvRate, tvAmount;
        }

    }
    //</editor-fold>
}
