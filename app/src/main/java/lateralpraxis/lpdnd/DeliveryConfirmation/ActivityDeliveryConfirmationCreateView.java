package lateralpraxis.lpdnd.DeliveryConfirmation;

//<editor-fold desc="Import">

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
//</editor-fold>

public class ActivityDeliveryConfirmationCreateView extends Activity {

    //<editor-fold desc="Code for Variable Declaration">
    private final Context mContext = this;
    DatabaseAdapter db;
    Common common;
    double totalQty = 0, totalOldQty = 0;
    HashMap<String, String> delmap = null;
    private ListView listDeliveryView;
    private TextView tvNoRecord;
    private TextView tvName, tvDate, tvInvoice, tvVehicle, tvTotalAmount;
    private Button btnDelivery;
    private UserSessionManager session;
    private String lang, userId, responseJSON, deliveryId, name, date, invoice, vehicle;
    private int listDelSize = 0;
    private ArrayList<HashMap<String, String>> wordDelList = null;
    private View tvDivider;
    //</editor-fold>

    //<editor-fold desc="On create method to load page">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Code to set layout
        setContentView(R.layout.activity_delivery_confirmation_create_view);
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        /*Start of code to find Controls*/
        tvName = (TextView) findViewById(R.id.tvName);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvInvoice = (TextView) findViewById(R.id.tvInvoice);
        tvVehicle = (TextView) findViewById(R.id.tvVehicle);
        listDeliveryView = (ListView) findViewById(R.id.listDeliveryView);
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        btnDelivery = (Button) findViewById(R.id.btnDelivery);
        tvDivider = findViewById(R.id.tvDivider);
        tvDivider.setVisibility(View.GONE);
        /*End of code to find Controls*/

        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());

        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

		/*Code to get data from posted page*/
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            deliveryId = extras.getString("DeliveryId");
            name = extras.getString("Name");
            date = extras.getString("Date");
            invoice = extras.getString("Invoice");
            vehicle = extras.getString("Vehicle");
            tvName.setText(name);
            tvDate.setText(date);
            tvInvoice.setText(invoice);
            tvVehicle.setText(vehicle);
        }
        if (common.isConnected()) {
            String[] params = {deliveryId};
            AsyncDeliveryDetailWSCall task = new AsyncDeliveryDetailWSCall();
            task.execute(params);
        } else {
            Intent intent = new Intent(mContext, ActivityDeliveryConfirmationCreateList.class);
            startActivity(intent);
            finish();
        }

        //Code on Delivery Button Click Event to delete delivery without payment detail
        btnDelivery.setOnClickListener(new View.OnClickListener() {
            // When create button click
            @Override
            public void onClick(View arg0) {

                // To validate required field and please enter at least one
                // quantity!
                int zeroCount = 0, totalRow = 0;
                int invalidCount = 0;
                for (int i = 0; i < listDeliveryView.getChildCount(); i++) {
                    totalRow++;
                    View v = listDeliveryView.getChildAt(i);
                    EditText etQty = (EditText) v.findViewById(R.id.etConfirmation);
                    if (etQty.getText().toString().equalsIgnoreCase("."))
                        invalidCount = invalidCount + 1;
                    if (!etQty.getText().toString().equalsIgnoreCase(".")) {
                        String qty = etQty.length() == 0 ? "0" : etQty
                                .getText().toString().trim();
                        if (etQty.length() == 0 || Double.parseDouble(qty) == 0)
                            zeroCount++;
                    }
                }
                if (invalidCount > 0 && lang.equalsIgnoreCase("en"))
                    common.showAlert(ActivityDeliveryConfirmationCreateView.this, "Please enter valid quantity!", false);
                else if (invalidCount > 0 && lang.equalsIgnoreCase("hi"))
                    common.showAlert(ActivityDeliveryConfirmationCreateView.this, "कृपया वैध मात्रा दर्ज करें!", false);
                else if (totalRow == zeroCount && lang.equalsIgnoreCase("en"))
                    common.showAlert(ActivityDeliveryConfirmationCreateView.this, "Please enter atleast one quantity!", false);
                else if (totalRow == zeroCount && lang.equalsIgnoreCase("hi"))
                    common.showAlert(ActivityDeliveryConfirmationCreateView.this, "कृपया कम से कम एक मात्रा दर्ज करें!", false);
                else {
                    Builder alertDialogBuilder = new Builder(mContext);
                    alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    alertDialogBuilder
                            .setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित इस डिलीवरी की पुष्टि करना चाहते हैं?" : "Are you sure, you want to confirm this delivery?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int id) {
                                    if (common.isConnected()) {
                                        try {
                                            JSONObject jsonDetails = new JSONObject();
                                            JSONArray arraydet = new JSONArray();
                                            for (int i = 0; i < listDeliveryView.getChildCount(); i++) {
                                                View v = listDeliveryView.getChildAt(i);
                                                TextView tvId = (TextView) v.findViewById(R.id.tvId);
                                                EditText etQty = (EditText) v.findViewById(R.id.etConfirmation);
                                                //To validate if user enter only .
                                                if (!etQty.getText().toString().equalsIgnoreCase(".")) {
                                                    String qty = etQty.getText().toString().trim().length() == 0 ? "0" : String.valueOf(Double.valueOf(etQty.getText().toString().trim()));
                                                    if (Double.parseDouble(qty) != 0) {
                                                        JSONObject jsondet = new JSONObject();
                                                        jsondet.put("Id", tvId.getText().toString());
                                                        jsondet.put("Quantity", qty);
                                                        arraydet.put(jsondet);
                                                    }
                                                }
                                            }
                                            jsonDetails.put("Detail", arraydet);
                                            String[] params = {jsonDetails.toString(), deliveryId};
                                            AsyncDeliveryConfirmationWSCall task = new AsyncDeliveryConfirmationWSCall();
                                            task.execute(params);
                                        } catch (Exception e) {
                                            // TODO: handle exception
                                        }
                                    }
                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    alertDialogBuilder.create().show();
                }
            }
        });
    }
    //</editor-fold>

    //<editor-fold desc="Get Total">
    // To sum of amount and return as total amount
    private String GetTotal() {
        totalQty = 0.0;
        for (int i = 0; i < listDeliveryView.getChildCount(); i++) {
            View v = listDeliveryView.getChildAt(i);
            TextView tvAmount = (TextView) v.findViewById(R.id.tvAmount);
            Double qty = 0.0;
            if (TextUtils.isEmpty(tvAmount.getText().toString().trim()))
                qty = 0.0;
            else
                qty = Double.parseDouble(tvAmount.getText().toString().replace(",", ""));
            totalQty = totalQty + qty;
        }
        return String.valueOf(totalQty);
    }
    //</editor-fold>

    //<editor-fold desc="Code to go to intent on selection of menu item">
    //Code to go to intent on selection of menu item
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
        Intent homeScreenIntent = new Intent(ActivityDeliveryConfirmationCreateView.this, ActivityDeliveryConfirmationCreateList.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }
    //</editor-fold>

    //<editor-fold desc="Web Service to Fetch Delivery Details By Id">
    private class AsyncDeliveryDetailWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateView.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "id"};
                String[] value = {"ReadDeliveryDetailById", params[0].replace(".0", "")};
                // Call method of web service to Read Data
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadDeliveryData", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return "ERROR: " + e.getMessage();
            }

        }

        // After execution of product web service
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    wordDelList = new ArrayList<HashMap<String, String>>();

                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            delmap = new HashMap<String, String>();
                            delmap.put("Id", jsonArray.getJSONObject(i)
                                    .getString("A").replace(".0", ""));
                            delmap.put("SkuId", jsonArray.getJSONObject(i)
                                    .getString("B").replace(".0", ""));
                            if (lang.equalsIgnoreCase("hi"))
                                delmap.put("Item", jsonArray.getJSONObject(i)
                                        .getString("G"));
                            else
                                delmap.put("Item", jsonArray.getJSONObject(i)
                                        .getString("C"));
                            delmap.put("Sku", jsonArray.getJSONObject(i)
                                    .getString("D").replace(".0", ""));
                            delmap.put("Rate", jsonArray.getJSONObject(i)
                                    .getString("E"));
                            delmap.put("Qty", jsonArray.getJSONObject(i)
                                    .getString("F").replace(".0", ""));
                            totalOldQty = totalOldQty + (Double.valueOf(jsonArray.getJSONObject(i)
                                    .getString("E")) * Double.valueOf(jsonArray.getJSONObject(i)
                                    .getString("F")));
                            wordDelList.add(delmap);
                        }
                        listDelSize = wordDelList.size();
                        if (listDelSize != 0) {
                            listDeliveryView.setAdapter(new CustomAdapter(mContext, wordDelList));
                            ViewGroup.LayoutParams params = listDeliveryView.getLayoutParams();
                            listDeliveryView.setLayoutParams(params);
                            listDeliveryView.requestLayout();
                            tvNoRecord.setVisibility(View.GONE);
                            tvDivider.setVisibility(View.VISIBLE);
                        } else {
                            listDeliveryView.setAdapter(null);
                            tvNoRecord.setVisibility(View.VISIBLE);
                            tvDivider.setVisibility(View.GONE);
                        }
                    } else {
                        if (lang.equalsIgnoreCase("hi"))
                            common.showToast("वितरण की पुष्टि के लिए कोई डेटा उपलब्ध नहीं है!");
                        else
                            common.showToast("There is no data available for delivery confirmation!");
                    }
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showToast(result);
                    Intent intent = new Intent(mContext, ActivityDeliveryConfirmationCreateList.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Delivery Confirmation Details Downloading failed: " + e.toString());
                Intent intent = new Intent(mContext, ActivityDeliveryConfirmationCreateList.class);
                startActivity(intent);
                finish();
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Delivery Confirmation Details..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async Task To Call delivery confirmation web service">
    private class AsyncDeliveryConfirmationWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateView.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"deliveryId", "json", "userId", "ip", "machine"};
                String[] value = {params[1].replace(".0", ""), params[0], userId, common.getDeviceIPAddress(true), common.getIMEI()};
                responseJSON = "";
                // Call method of web service to sync delivery confirmation to server
                responseJSON = common.CallJsonWS(name, value, "CreateDeliveryConfirmation",
                        common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to fetch response from server.";
            }
        }

        // After execution of web service to delivery confirmation
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    if (lang.equalsIgnoreCase("hi"))
                        common.showToast("डिलिवरी पुष्टिकरण सफलतापूर्वक सहेजा गया|");
                    else
                        common.showToast("Delivery confirmation saved successfully.");

                    Intent intent = new Intent(ActivityDeliveryConfirmationCreateView.this, ActivityDeliveryConfirmationCreateList.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityDeliveryConfirmationCreateView.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityDeliveryConfirmationCreateView.this, e.getMessage(), false);
            }
            Dialog.dismiss();
        }

        // To display Delivery Confirmation Message
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Sending Delivery Confirmation Details...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="To make view holder to display on screen">
    public class CustomAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> _listItems;
        private Context context2;
        private LayoutInflater mInflater;

        // constructor of custom adapter class
        public CustomAdapter(Context context,
                             ArrayList<HashMap<String, String>> listItem) {
            this.context2 = context;
            mInflater = LayoutInflater.from(context2);
            _listItems = listItem;
        }

        @Override
        // To get item list count
        public int getCount() {
            return _listItems.size();
        }

        // To get item name
        @Override
        public Object getItem(int arg0) {
            return _listItems.get(arg0);
        }

        // To get item id
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        // To get item name
        @Override
        public int getViewTypeCount() {
            return getCount();
        }

        // To get item position
        @Override
        public int getItemViewType(int position) {
            return position;
        }

        // To instantiate layout XML file into its corresponding view objects.
        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            final ViewHolder holder;
            if (arg1 == null) {
                arg1 = mInflater.inflate(
                        R.layout.activity_delivery_confirmation_create_view_item, null);
                holder = new ViewHolder();

                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvSku = (TextView) arg1.findViewById(R.id.tvSku);
                holder.tvRate = (TextView) arg1.findViewById(R.id.tvRate);
                holder.tvDelivery = (TextView) arg1.findViewById(R.id.tvDelivery);
                holder.etQty = (EditText) arg1.findViewById(R.id.etConfirmation);
                holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.ref = arg0;
            // To bind data from list

            holder.tvId.setText(_listItems.get(arg0).get("Id"));
            holder.tvSku.setText(lang.equalsIgnoreCase("hi") ? _listItems.get(arg0).get("Item") : _listItems.get(arg0).get("Item"));

            holder.tvDelivery.setText(_listItems.get(arg0).get("Qty"));
            holder.tvRate.setText(common.stringToTwoDecimal(_listItems.get(arg0).get("Rate")));
            //if (_listItems.get(arg0).get("Qty") != null)
            //holder.etQty.setText(String.format("%.1f", Double.parseDouble(_listItems.get(arg0).get("Qty"))));
            holder.etQty.setText(_listItems.get(arg0).get("Qty"));
            if (_listItems.get(arg0).get("Sku").equalsIgnoreCase("0")) {
                // To display decimal point in number key board control
                holder.etQty
                        .setFilters(new InputFilter[]{new DecimalDigitsInputFilter(
                                5, 1)});
                holder.etQty.setInputType(InputType.TYPE_CLASS_NUMBER
                        + InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                // To display only number in key board control
                int maxLength = 3;
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                holder.etQty.setFilters(FilterArray);
                holder.etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            // To display total amount in footer of amount
            if (lang.equalsIgnoreCase("hi"))
                tvTotalAmount.setText(Html.fromHtml("<b>कुल: " + common.stringToTwoDecimal(totalOldQty) + "</b>"));
            else
                tvTotalAmount.setText(Html.fromHtml("<b>Total: " + common.stringToTwoDecimal(totalOldQty) + "</b>"));
            // Instantiates a TextWatcher, to observe value changes and trigger the result calculation
            TextWatcher textWatcher = new TextWatcher() {
                public void afterTextChanged(Editable s) {

                    if (!holder.etQty.getText().toString().equalsIgnoreCase(".")) {
                        if (holder.etQty.getText().toString().equalsIgnoreCase("."))
                            holder.etQty.setText("");
                        if (holder.etQty.getText().toString().trim().length() > 0) {
                            if (Double.parseDouble(holder.etQty.getText().toString()) == 0) {
                                holder.etQty.setText("");
                                holder.tvAmount.setText("");
                            } else if (Double.parseDouble(holder.etQty.getText().toString().trim()) > Double.parseDouble(holder.tvDelivery.getText().toString().trim())) {
                                if (lang.equalsIgnoreCase("hi"))
                                    common.showToast("पुष्टिकरण मात्रा वितरण मात्रा से अधिक नहीं होनी चाहिए!");
                                else
                                    common.showToast("Confirmation quantity should not be exceeded from delivery quantity!");
                                holder.etQty.setText("");
                            } else
                                holder.tvAmount.setText(common.stringToTwoDecimal(String.valueOf(Double.parseDouble(holder.etQty.getText().toString()) * Double.parseDouble(holder.tvRate.getText().toString().replace(",", "")))));
                        } else {
                            holder.tvAmount.setText("");
                        }
                    } else {
                        holder.tvAmount.setText("");
                    }
                    // To display total amount in footer of amount
                    if (lang.equalsIgnoreCase("hi"))
                        tvTotalAmount.setText(Html.fromHtml("<b>कुल: " + common.stringToTwoDecimal(GetTotal()) + "</b>"));
                    else
                        tvTotalAmount.setText(Html.fromHtml("<b>Total: " + common.stringToTwoDecimal(GetTotal()) + "</b>"));

                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            };

            // Adds the TextWatcher as TextChangedListener to both EditTexts
            holder.etQty.addTextChangedListener(textWatcher);

            // Fix for text selection handle not disappearing
            holder.etQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view2, boolean hasFocus) {
                    view2.dispatchWindowFocusChanged(hasFocus);
                }
            });
            return arg1;
        }

        class ViewHolder {
            TextView tvId, tvSku, tvRate, tvDelivery, tvAmount;
            EditText etQty;
            int ref;
        }
    }
    //</editor-fold>
}
