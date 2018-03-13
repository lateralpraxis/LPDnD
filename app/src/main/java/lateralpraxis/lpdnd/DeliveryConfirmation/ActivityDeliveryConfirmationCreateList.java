package lateralpraxis.lpdnd.DeliveryConfirmation;

//<editor-fold desc="Import">
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityChangePassword;
import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
//</editor-fold>

public class ActivityDeliveryConfirmationCreateList extends Activity {

    //<editor-fold desc="Code for Variable Declaration">
    /*Code for Variable Declaration*/
    private final Context mContext = this;
    HashMap<String, String> map = null;
    DatabaseAdapter db;
    Common common;
    private String lang, userId, responseJSON, sendJSon;
    private int listSize = 0;
    private ArrayList<HashMap<String, String>> wordList = null;
    private UserSessionManager session;
    private ListView listViewConfirm;
    private TextView tvNoRecord;
    private View tvDivider;
    /*End of Code for Variable Declaration*/
    //</editor-fold>

    //<editor-fold desc="On create method to load page ">
    //On create method to load page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Code to set layout
        setContentView(R.layout.activity_delivery_confirmation_create_list);

        //Code to create instance of classes
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);

        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        //Start of code to find Controls
        listViewConfirm = (ListView) findViewById(R.id.listViewConfirm);
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        tvDivider = findViewById(R.id.tvDivider);
        tvDivider.setVisibility(View.GONE);
        if (common.isConnected()) {
            String[] myTaskParams = {"transactions"};
            AsyncCustomerValidatePasswordWSCall task = new AsyncCustomerValidatePasswordWSCall();
            task.execute(myTaskParams);
        } else {
            Intent intent = new Intent(mContext, ActivityHomeScreen.class);
            startActivity(intent);
            finish();
        }

        listViewConfirm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivityDeliveryConfirmationCreateList.this, ActivityDeliveryConfirmationCreateView.class);
                intent.putExtra("DeliveryId", String.valueOf(((TextView) item.findViewById(R.id.tvId)).getText().toString()));
                intent.putExtra("Name", String.valueOf(((TextView) item.findViewById(R.id.tvName)).getText().toString()));
                intent.putExtra("Date", String.valueOf(((TextView) item.findViewById(R.id.tvDate)).getText().toString()));
                intent.putExtra("Invoice", String.valueOf(((TextView) item.findViewById(R.id.tvInvoice)).getText().toString()));
                intent.putExtra("Vehicle", String.valueOf(((TextView) item.findViewById(R.id.tvVehicle)).getText().toString()));
                startActivity(intent);
                finish();
            }
        });
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
        Intent homeScreenIntent = new Intent(ActivityDeliveryConfirmationCreateList.this, ActivityHomeScreen.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
    }
    //</editor-fold>

    //<editor-fold desc="showChangePassWindow">
    private void showChangePassWindow(final String source, final String resp) {
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.dialog_password_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);
        alertDialogBuilder.setView(promptsView);
        // Code to find controls in dialog
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.etPassword);

        final CheckBox ckShowPass = (CheckBox) promptsView
                .findViewById(R.id.ckShowPass);

        final TextView tvMsg = (TextView) promptsView.findViewById(R.id.tvMsg);

        tvMsg.setText(resp);

        ckShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                int start, end;

                if (!isChecked) {
                    start = userInput.getSelectionStart();
                    end = userInput.getSelectionEnd();
                    userInput
                            .setTransformationMethod(new PasswordTransformationMethod());
                    userInput.setSelection(start, end);
                } else {
                    start = userInput.getSelectionStart();
                    end = userInput.getSelectionEnd();
                    userInput.setTransformationMethod(null);
                    userInput.setSelection(start, end);
                }

            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String password = userInput.getText().toString().trim();
                                if (password.length() > 0) {
                                    // Code to update password in session and
                                    // call validate Async Method
                                    session.updatePassword(password);

                                    String[] myTaskParams = {source};
                                    AsyncCustomerValidatePasswordWSCall task = new AsyncCustomerValidatePasswordWSCall();
                                    task.execute(myTaskParams);
                                } else {
                                    // Display message if password is not
                                    // enetered
                                    common.showToast("Password is mandatory");
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
    //</editor-fold>

    //<editor-fold desc="Code Binding Data In List">
    public static class viewHolder {
        TextView tvId, tvDate, tvName, tvVehicle, tvInvoice;
        LinearLayout llDate, llVehicle, llName;
        int ref;
    }

    //<editor-fold desc="Web Service to Fetch Delivery Detail">
    // Web Service to Fetch Delivery Detail
    private class AsyncDeliveryDetailWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "id"};
                String[] value = {"ReadDeliveryList", params[0]};
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
                    String data = "";
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    wordList = new ArrayList<HashMap<String, String>>();
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            map = new HashMap<String, String>();
                            map.put("DeliveryId", jsonArray.getJSONObject(i)
                                    .getString("DeliveryId"));
                            map.put("FullName", jsonArray.getJSONObject(i)
                                    .getString("FullName"));
                            map.put("VehicleNo", jsonArray.getJSONObject(i)
                                    .getString("VehicleNo"));
                            map.put("DeliveryDate", jsonArray.getJSONObject(i)
                                    .getString("DeliveryDate"));
                            map.put("InvoiceNo", jsonArray.getJSONObject(i)
                                    .getString("InvoiceNo"));
                            map.put("CustomerName", jsonArray.getJSONObject(i)
                                    .getString("CustomerName"));

                            if (data.equalsIgnoreCase(jsonArray.getJSONObject(i)
                                    .getString("FullName") + "~" + jsonArray.getJSONObject(i)
                                    .getString("VehicleNo") + "~" + jsonArray.getJSONObject(i)
                                    .getString("DeliveryDate")))
                                map.put("Flag", "1");
                            else
                                map.put("Flag", "0");
                            data = jsonArray.getJSONObject(i)
                                    .getString("FullName") + "~" + jsonArray.getJSONObject(i)
                                    .getString("VehicleNo") + "~" + jsonArray.getJSONObject(i)
                                    .getString("DeliveryDate");
                            wordList.add(map);
                        }
                        listSize = wordList.size();
                        if (listSize != 0) {
                            listViewConfirm.setAdapter(new ReportListAdapter(mContext, wordList));

                            ViewGroup.LayoutParams params = listViewConfirm.getLayoutParams();
                            //params.height = 500;
                            listViewConfirm.setLayoutParams(params);
                            listViewConfirm.requestLayout();
                            tvNoRecord.setVisibility(View.GONE);
                            tvDivider.setVisibility(View.VISIBLE);
                        } else {
                            listViewConfirm.setAdapter(null);
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
                    /*Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
                    startActivity(intent);
					finish();*/
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Delivery Downloading failed: " + e.toString());
                /*Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
                startActivity(intent);
				finish();*/
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Delivery Data..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

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
                convertView = inflater.inflate(R.layout.activity_delivery_confirmation_create_list_item, null);
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
            holder.tvName = (TextView) convertView
                    .findViewById(R.id.tvName);
            holder.tvVehicle = (TextView) convertView
                    .findViewById(R.id.tvVehicle);
            holder.tvInvoice = (TextView) convertView
                    .findViewById(R.id.tvInvoice);

            holder.llDate = (LinearLayout) convertView
                    .findViewById(R.id.llDate);
            holder.llVehicle = (LinearLayout) convertView
                    .findViewById(R.id.llVehicle);
            holder.llName = (LinearLayout) convertView
                    .findViewById(R.id.llName);

            final HashMap<String, String> itemData = _listData.get(position);
            holder.tvId.setText(itemData.get("DeliveryId"));
            holder.tvDate.setText(common.convertToDisplayDateFormat(itemData.get("DeliveryDate")));
            holder.tvName.setText(itemData.get("FullName"));
            holder.tvVehicle.setText(itemData.get("VehicleNo"));
            //holder.tvInvoice.setText(itemData.get("InvoiceNo")+" - "+itemData.get("CustomerName"));
            holder.tvInvoice.setText(itemData.get("InvoiceNo"));
            if (itemData.get("Flag").equalsIgnoreCase("1")) {
                holder.llDate.setVisibility(View.GONE);
                holder.llVehicle.setVisibility(View.GONE);
                holder.llName.setVisibility(View.GONE);
            } else {
                holder.llDate.setVisibility(View.VISIBLE);
                holder.llVehicle.setVisibility(View.VISIBLE);
                holder.llName.setVisibility(View.VISIBLE);
            }
            //convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return convertView;
        }

    }
    //</editor-fold>

    //<editor-fold desc="Code to Validate Retail Outlet Customer">
    private class AsyncCustomerValidatePasswordWSCall extends
            AsyncTask<String, Void, String> {
        String source = "";
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {
            try { // if this button is clicked, close

                source = params[0];
                db.openR();
                HashMap<String, String> user = session.getLoginUserDetails();
                // Creation of JSON string for posting validating data
                JSONObject json = new JSONObject();
                json.put("username", user.get(UserSessionManager.KEY_CODE));
                json.put("password", user.get(UserSessionManager.KEY_PWD));
                json.put("imei", common.getIMEI());
                json.put("version", db.getVersion());
                String JSONStr = json.toString();

                // Store response fetched from server in responseJSON variable
                responseJSON = common.invokeJSONWS(JSONStr, "json",
                        "ValidatePassword", common.url);

            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                // e.printStackTrace();
                return "ERROR: Unable to fetch response from server.";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Check if result contains error
                if (!result.contains("ERROR")) {
                    String passExpired = responseJSON.split("~")[0];
                    String passServer = responseJSON.split("~")[1];
                    String membershipError = responseJSON.split("~")[2];
                    // Check if password is expire and open change password
                    // intent
                    if (passExpired.toLowerCase(Locale.US).equals("yes")) {
                        Intent intent = new Intent(mContext,
                                ActivityChangePassword.class);
                        intent.putExtra("fromwhere", "login");
                        startActivity(intent);
                        finish();
                    }
                    // Code to check other validations
                    else if (passServer.toLowerCase(Locale.US).equals("no")) {
                        String resp = "";

                        if (membershipError.toLowerCase(Locale.US).contains(
                                "NO_USER".toLowerCase(Locale.US))) {
                            resp = "There is no user in the system";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("BARRED".toLowerCase(Locale.US))) {
                            resp = "Your account has been barred by the Administrator.";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("LOCKED".toLowerCase(Locale.US))) {
                            resp = "Your account has been locked out because "
                                    + "you have exceeded the maximum number of incorrect login attempts. "
                                    + "Please contact the System Admin to "
                                    + "unblock your account.";
                        } else if (membershipError.toLowerCase(Locale.US)
                                .contains("LOGINFAILED".toLowerCase(Locale.US))) {
                            resp = "Invalid password. "
                                    + "Password is case-sensitive. "
                                    + "Access to the system will be disabled after "
                                    + responseJSON.split("~")[3] + " "
                                    + "consecutive wrong attempts.\n"
                                    + "Number of Attempts remaining: "
                                    + responseJSON.split("~")[4];
                        } else {
                            resp = "Password mismatched. Enter latest password!";
                        }

                        showChangePassWindow(source, resp);
                    }

                    // If version does not match logout user
                    if (responseJSON.contains("NOVERSION")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                mContext);
                        builder.setMessage(
                                "Application is running an older version. Please install latest version.!")
                                .setCancelable(false)
                                .setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                // Code to call async method
                                                // for posting transactions
                                                if (common.isConnected()) {
                                                    AsyncCustomerPrimaryReceiptWSCall task = new AsyncCustomerPrimaryReceiptWSCall();
                                                    task.execute();
                                                }
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } else {
                        if (common.isConnected()) {
                            AsyncCustomerPrimaryReceiptWSCall task = new AsyncCustomerPrimaryReceiptWSCall();
                            task.execute();
                        }
                    }

                } else {
                    common.showAlert(ActivityDeliveryConfirmationCreateList.this,
                            "Unable to fetch response from server.", false);
                }

            } catch (Exception e) {
                common.showAlert(ActivityDeliveryConfirmationCreateList.this,
                        "Validating credentials failed: " + e.toString(), false);
            }
            Dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Validating your credentials..");
            Dialog.setCancelable(false);
            Dialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }

    }
    //</editor-fold>

    //<editor-fold desc="Async for Posting Primary Receipt for Retail Outlet">
    private class AsyncCustomerPrimaryReceiptWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonPayment = new JSONObject();
                db.open();
                // to get Primary Receipt from database
                ArrayList<HashMap<String, String>> insmast = db
                        .getUnSyncPrimaryReceipt();
                db.close();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To make json string to post payment
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("UniqueId", insp.get("UniqueId"));
                        jsonins.put("CustomerId", insp.get("CustomerId"));
                        jsonins.put("MaterialId", insp.get("MaterialId"));
                        jsonins.put("SkuId", insp.get("SKUId"));
                        jsonins.put("Quantity", insp.get("Quantity"));
                        jsonins.put("Amount", insp.get("Amount"));
                        jsonins.put("TransactionDate", insp.get("CreateDate"));
                        jsonins.put("CreateBy", userId);
                        jsonins.put("ipAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("Machine", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonPayment.put("PrimaryReceipt", array);

                    sendJSon = jsonPayment.toString();

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "InsertPimaryReceipt", common.url);
                } else {
                    return "No primary receipt pending to be send.";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                db.close();
            }
        }

        // After execution of json web service to create Primary Receipt
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (responseJSON.equalsIgnoreCase("success")) {
                        db.open();
                        db.Update_PrimaryReceiptIsSync();
                        db.close();
                    }
                    if (common.isConnected()) {
                        // call method of Sync Pending Outlet Payments
                        AsyncCustomerPaymentsWSCall task = new AsyncCustomerPaymentsWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityDeliveryConfirmationCreateList.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityDeliveryConfirmationCreateList.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Primary Receipt...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async method for Posting Outlet Payment for Retail Outlet">
    private class AsyncCustomerPaymentsWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonPayment = new JSONObject();
                db.open();
                // to get Primary Receipt from database
                ArrayList<HashMap<String, String>> insmast = db.getUnSyncOutletPayment();
                db.close();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To make json string to post payment
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("UniqueId", insp.get("UniqueId"));
                        jsonins.put("CustomerId", insp.get("CustomerId"));
                        jsonins.put("Amount", insp.get("Amount"));
                        jsonins.put("TransactionDate", insp.get("AndroidDate"));
                        jsonins.put("CreateBy", userId);
                        jsonins.put("ipAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("Machine", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonPayment.put("Payments", array);

                    sendJSon = jsonPayment.toString();

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "InsertOutletPayment", common.url);
                } else {
                    return "No outlet payment pending to be send.";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                db.close();
            }
        }

        // After execution of json web service to create payment
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (responseJSON.equalsIgnoreCase("success")) {
                        db.open();
                        db.Update_PaymentReceiptIsSync();
                        db.close();
                    }
                    if (common.isConnected()) {
                        // call method of Post Customer Expense
                        AsyncCustomerExpenseWSCall task = new AsyncCustomerExpenseWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityDeliveryConfirmationCreateList.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityDeliveryConfirmationCreateList.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Outlet Payment...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async method for Posting Outlet Expense for Retail Outlet">
    private class AsyncCustomerExpenseWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {

            // Will contain the raw JSON response as a string.
            try {

                responseJSON = "";

                JSONObject jsonExpense = new JSONObject();
                db.open();
                // to get Primary Receipt from database
                ArrayList<HashMap<String, String>> insmast = db
                        .getUnSyncOutletExpense();
                db.close();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To make json string to post payment
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();
                        jsonins.put("UniqueId", insp.get("UniqueId"));
                        jsonins.put("CustomerId", insp.get("CustomerId"));
                        jsonins.put("ExpenseHeadId", insp.get("ExpenseHeadId"));
                        jsonins.put("Amount", insp.get("Amount"));
                        jsonins.put("Remarks", insp.get("Remarks"));
                        jsonins.put("TransactionDate", insp.get("TransactionDate"));
                        jsonins.put("CreateBy", userId);
                        jsonins.put("ipAddress",
                                common.getDeviceIPAddress(true));
                        jsonins.put("Machine", common.getIMEI());
                        array.put(jsonins);
                    }
                    jsonExpense.put("Master", array);

                    sendJSon = jsonExpense.toString();

                    // To invoke json web service to create payment
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateOutletExpense", common.url);
                } else {
                    return "No outlet expense pending to be send.";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                db.close();
            }
        }

        // After execution of json web service to create payment
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (responseJSON.equalsIgnoreCase("success")) {
                        db.open();
                        db.Update_OutletExpenseIsSync();
                        db.close();
                    }
                    if (common.isConnected()) {

                        AsyncOutletSaleWSCall task = new AsyncOutletSaleWSCall();
                        task.execute();

                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showAlert(ActivityDeliveryConfirmationCreateList.this, result, false);
                    common.showToast("Error: " + result);
                }
            } catch (Exception e) {
                common.showAlert(ActivityDeliveryConfirmationCreateList.this,
                        "Unable to fetch response from server.", false);
            }

            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {

            Dialog.setMessage("Posting Outlet Expense...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="To make web service class to post data of outlet sale">
    private class AsyncOutletSaleWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {
            // Will contain the raw JSON response as a string.
            try {
                responseJSON = "";
                JSONObject jsonOutletSale = new JSONObject();

                // to get outlet sale from database
                db.open();
                ArrayList<HashMap<String, String>> insmast = db.getUnSyncOutletSale();
                db.close();
                if (insmast != null && insmast.size() > 0) {
                    JSONArray array = new JSONArray();
                    // To make json string to post outlet sale
                    for (HashMap<String, String> insp : insmast) {
                        JSONObject jsonins = new JSONObject();

                        jsonins.put("UniqueId", insp.get("UniqueId"));
                        jsonins.put("UserId", insp.get("CreateBy"));
                        jsonins.put("CustomerId", insp.get("CustomerId"));
                        jsonins.put("SaleType", insp.get("SaleType"));
                        jsonins.put("SaleDate", insp.get("SaleDate"));
                        jsonins.put("AndroidDate", insp.get("SaleDate"));
                        jsonins.put("ipAddress", common.getDeviceIPAddress(true));
                        jsonins.put("Machine", insp.get("Imei"));
                        array.put(jsonins);
                    }
                    jsonOutletSale.put("Master", array);

                    JSONObject jsonDetails = new JSONObject();
                    // To get outlet sale details from database
                    db.open();
                    ArrayList<HashMap<String, String>> insdet = db.getUnSyncOutletSaleDetail();
                    db.close();
                    if (insdet != null && insdet.size() > 0) {

                        // To make json string to post outlet sale details
                        JSONArray arraydet = new JSONArray();
                        for (HashMap<String, String> insd : insdet) {
                            JSONObject jsondet = new JSONObject();
                            jsondet.put("UniqueId", insd.get("UniqueId"));
                            jsondet.put("SkuId", insd.get("SkuId"));
                            jsondet.put("Quantity", insd.get("Qty"));
                            jsondet.put("Rate", insd.get("Rate"));
                            jsondet.put("SaleRate", insd.get("SaleRate"));
                            arraydet.put(jsondet);
                        }
                        jsonDetails.put("Detail", arraydet);
                    }
                    sendJSon = jsonOutletSale + "~" + jsonDetails;
                    // To invoke json web service to create outlet sale
                    responseJSON = common.invokeJSONWS(sendJSon, "json",
                            "CreateOutletSale", common.url);
                } else {
                    return "No outlet sale pending to be send.~";
                }
                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                db.close();
            }
        }

        // After execution of json web service to create outlet sale
        @Override
        protected void onPostExecute(String result) {
            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (responseJSON.equalsIgnoreCase("success")) {
                        db.open();
                        db.UpdateOutletSaleIsSync();
                        db.close();
                    }
                    if (common.isConnected()) {

                        AsyncPendingDeliveryStatusWSCall task = new AsyncPendingDeliveryStatusWSCall();
                        task.execute();
                    }
                } else {
                    if (result.contains("null"))
                        result = "Server not responding.";
                    common.showToast("Error: " + result);
                }

            } catch (Exception e) {

            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Posting Sale Details...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Download Pending Delivery Status">
    private class AsyncPendingDeliveryStatusWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityDeliveryConfirmationCreateList.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "userId", "role"};
                String[] value = {"CheckPendingDelivery", userId, "Customer"};
                responseJSON = "";
                // Call method of web service to download Reatil Outlet Inventory from
                // server
                responseJSON = common.CallJsonWS(name, value, "ReadMaster",
                        common.url);
                return responseJSON;
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            }
        }

        // After execution of web service to download Retail Outlet Inventory
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR")) {
                    // To display message after response from server
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    db.open();
                    db.DeleteMasterData("DeliveryConfirmStatus");
                    String status = "";
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        db.Insert_DeliveryConfirmStatus(jsonArray.getJSONObject(i)
                                .getString("A"));
                        status = jsonArray.getJSONObject(i).getString("A");
                    }
                    db.close();
                    if (common.isConnected()) {
                        String[] params = {userId};
                        AsyncDeliveryDetailWSCall task = new AsyncDeliveryDetailWSCall();
                        task.execute(params);
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                    common.showAlert(ActivityDeliveryConfirmationCreateList.this, result, false);
                }
            } catch (Exception e) {
                common.showAlert(ActivityDeliveryConfirmationCreateList.this,
                        "Pending Delivery Status Downloading failed: "
                                + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Pending Delivery Status..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>
}
