package lateralpraxis.lpdnd.stockconversion;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
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

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;

public class ActivityStockConversionDetail extends Activity {

    //<editor-fold desc="Code to declare controls">
    private TextView tvCode,tvDate;
    private ListView listConsumed,listProduced;
    //</editor-fold>

    //<editor-fold desc="Code for Class Declaraion">
    DatabaseAdapter db;
    Common common;
    private UserSessionManager session;
    private final Context mContext = this;
    //</editor-fold>

    //<editor-fold desc="Code for Declaring Variables">
    private String lang="",id="",responseJSON="";
    private ArrayList<HashMap<String, String>> wordConsList = null;
    private ArrayList<HashMap<String, String>> listCons;
    HashMap<String, String> consmap = null;
    private ArrayList<HashMap<String, String>> wordProdList = null;
    private ArrayList<HashMap<String, String>> listProd;
    HashMap<String, String> prodmap = null;
    private int listConsSize = 0,listProdSize=0;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on OnCreate method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_conversion_detail);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        //</editor-fold>

        //<editor-fold desc="Code to set Id from Previous Intent">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null)
        {
            id = extras.getString("Id");
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

        //<editor-fold desc="Code to find controls">
        tvCode = (TextView) findViewById(R.id.tvCode);
        tvDate = (TextView) findViewById(R.id.tvDate);
        listConsumed = (ListView) findViewById(R.id.listConsumed);
        listProduced = (ListView) findViewById(R.id.listProduced);
        //</editor-fold>

        if(common.isConnected())
        {
            AsyncConversionDetailWSCall task = new AsyncConversionDetailWSCall();
            task.execute();
        }
        else
        {
            Intent intent = new Intent(mContext, ActivityListStockConversion.class);
            startActivity(intent);
            finish();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Fetch Conversion Details By Id">
    private class AsyncConversionDetailWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityStockConversionDetail.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"lang","id" };
                String[] value = { lang,id };
                // Call method of web service to Read Conversion Details
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value,"ReadStockConversionDetailReport", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    String data="";
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
                    JSONArray jsonConsume = new JSONArray(responseJSON.split("~")[1]);
                    JSONArray jsonProduce = new JSONArray(responseJSON.split("~")[2]);
                    wordConsList = new ArrayList<HashMap<String, String>>();
                    wordProdList = new ArrayList<HashMap<String, String>>();
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            tvCode.setText(jsonArray.getJSONObject(i)
                                    .getString("B"));
                            tvDate.setText(common.convertToDisplayDateFormat(jsonArray.getJSONObject(i)
                                    .getString("C")));
                        }

                        if (jsonConsume.length() > 0) {
                            for (int i = 0; i < jsonConsume.length(); ++i) {
                                consmap = new HashMap<String, String>();
                                consmap.put("Name", jsonConsume.getJSONObject(i)
                                        .getString("A"));
                                consmap.put("Quantity", jsonConsume.getJSONObject(i)
                                        .getString("B").replace(".0",""));
                                wordConsList.add(consmap);
                            }
                            listConsSize = wordConsList.size();
                            if (listConsSize != 0) {
                                listConsumed.setAdapter(new ConsListAdapter(mContext, wordConsList));
                                ViewGroup.LayoutParams params = listConsumed.getLayoutParams();
                                listConsumed.setLayoutParams(params);
                                listConsumed.requestLayout();

                            } else {
                                listConsumed.setAdapter(null);
                            }

                        }
                        if (jsonProduce.length() > 0) {
                            for (int i = 0; i < jsonProduce.length(); ++i) {
                                prodmap = new HashMap<String, String>();
                                prodmap.put("Name", jsonProduce.getJSONObject(i)
                                        .getString("A"));
                                prodmap.put("Quantity", jsonProduce.getJSONObject(i)
                                        .getString("B").replace(".0",""));
                                wordProdList.add(prodmap);
                            }
                            listProdSize = wordProdList.size();
                            if (listProdSize != 0) {
                                listProduced.setAdapter(new ProdListAdapter(mContext, wordProdList));
                                ViewGroup.LayoutParams params = listProduced.getLayoutParams();
                                listProduced.setLayoutParams(params);
                                listProduced.requestLayout();
                            } else {
                                listProduced.setAdapter(null);
                            }

                        }


                    } else {
                        common.showToast("There is no data available for Conversion!");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showToast(result);
                    Intent intent = new Intent(mContext, ActivityListStockConversion.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Conversion Details Downloading failed: " + e.toString());
                Intent intent = new Intent(mContext, ActivityListStockConversion.class);
                startActivity(intent);
                finish();
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Conversion Details..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to be Bind Data in consumed list view">
    public static class ViewHolder {
        TextView tvName, tvQty;
    }

    public class ConsListAdapter extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public ConsListAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
            this.docContext = context;
            mInflater = LayoutInflater.from(docContext);
            listCons = lvList;
        }

        @Override
        public int getCount() {
            return listCons.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listCons.get(arg0);
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
                arg1 = mInflater.inflate(R.layout.list_consume_view, null);
                holder = new ViewHolder();

                holder.tvName = (TextView) arg1.findViewById(R.id.tvName);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvName.setText(listCons.get(arg0).get("Name"));
            holder.tvQty.setText(common.stringToTwoDecimal(listCons.get(arg0).get("Quantity")).replace(".00",""));



            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }


    public static class ProducedViewHolder {
        TextView  tvName, tvQty;
    }

    public class ProdListAdapter extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public ProdListAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
            this.docContext = context;
            mInflater = LayoutInflater.from(docContext);
            listProd = lvList;
        }

        @Override
        public int getCount() {
            return listProd.size();
        }

        @Override
        public Object getItem(int arg0) {
            return listProd.get(arg0);
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


            final ProducedViewHolder holder;
            if (arg1 == null) {
                arg1 = mInflater.inflate(R.layout.list_produce_view, null);
                holder = new ProducedViewHolder();

                holder.tvName = (TextView) arg1.findViewById(R.id.tvName);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                arg1.setTag(holder);

            } else {
                holder = (ProducedViewHolder) arg1.getTag();
            }
            holder.tvName.setText(listProd.get(arg0).get("Name"));
            holder.tvQty.setText(common.stringToTwoDecimal(listProd.get(arg0).get("Quantity")).replace(".00",""));

            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Back and Home Button">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_go_to_home:
                String userRole = "";
                final HashMap<String, String> user = session.getLoginUserDetails();
                userRole = user.get(UserSessionManager.KEY_ROLES);
                Intent i;
                i = new Intent(ActivityStockConversionDetail.this, ActivityHomeScreen.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Event Triggered on Clicking Back
    @Override
    public void onBackPressed() {
        Intent i = new Intent(ActivityStockConversionDetail.this, ActivityListStockConversion.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
    //</editor-fold>
}
