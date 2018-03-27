package lateralpraxis.lpdnd.CentreStockConversion;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.ActivityChangePassword;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;


public class ActivityListCentreConversion extends Activity {
    private final Context mContext = this;
    HashMap<String, String> map = null;
    //<editor-fold desc="Code to declare Class">
    DatabaseAdapter db;
    Common common;
    /*Start of code for Variable Declaration*/
    private String lang, userId, responseJSON, sendJSon;
    private String userRole, password, loginId;
    private int listSize = 0;
    /*End of code for Variable Declaration*/
    private int year, month, day;
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>> wordList = null;
    private UserSessionManager session;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter_display, dateFormatter_database;
    //</editor-fold>

    //<editor-fold desc="Code to declare Controls">
    private TextView tvDate, tvEmpty, linkAddStockConversion;
    private ListView listConvert;
    private Button btnGo;
    private TableLayout tableGridHead;
    //</editor-fold>

    //<editor-fold desc="Methods to display the Calendar">
    private DatePickerDialog.OnDateSetListener dateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    calendar.set(arg1, arg2, arg3);
                    showDate(dateFormatter_display.format(calendar.getTime()));
                }
            };

    //</editor-fold>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_centre_conversion);

        //<editor-fold desc="Code to set Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());


        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        loginId = user.get(UserSessionManager.KEY_CODE);
        password = user.get(UserSessionManager.KEY_PWD);
        userRole = user.get(UserSessionManager.KEY_ROLES);

        //<editor-fold desc="Code to find Controls">
        listConvert = (ListView) findViewById(R.id.listConvert);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        linkAddStockConversion = (TextView) findViewById(R.id.linkAddStockConversion);
        btnGo = (Button) findViewById(R.id.btnGo);
        tableGridHead = (TableLayout) findViewById(R.id.tableGridHead);
        dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        dateFormatter_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        //</editor-fold>
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(dateFormatter_display.format(calendar.getTime()));
        //<editor-fold desc="Code for clicking ob Link Add Click">
        linkAddStockConversion.setOnClickListener(new View.OnClickListener() {
            //On click of view delivery button
            @Override
            public void onClick(View arg0) {
                if (common.isConnected()) {
                    /*String[] myTaskParams = {"transactions"};
                    AsyncCustomerValidatePasswordWSCall task = new AsyncCustomerValidatePasswordWSCall();
                    task.execute(myTaskParams);*/
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to set default language">
        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click of Go Button">
        btnGo.setOnClickListener(new View.OnClickListener() {
            //On click of add button
            @Override
            public void onClick(View arg0) {
                if (common.isConnected()) {
                    AsyncStockReturnListListWSCall task = new AsyncStockReturnListListWSCall();
                    task.execute();
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of List View">
        listConvert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                Intent intent = new Intent(ActivityListCentreConversion.this, ActivityConversionView.class);
                intent.putExtra("Id", String.valueOf(((TextView) item.findViewById(R.id.tvId)).getText().toString()));
                startActivity(intent);
                finish();
            }
        });
        //</editor-fold>
    }

    //<editor-fold desc="Methods to Display Selected Date in TextView">
    private void showDate(String date) {
        tvDate.setText(date.replace(" ", "-"));
    }
    //</editor-fold>

    //<editor-fold desc="Methods to open Calendar">
    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            DatePickerDialog dialog = new DatePickerDialog(this, dateListener, year, month, day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }
        return null;
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
                i = new Intent(ActivityListCentreConversion.this, ActivityAdminHomeScreen.class);
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

    //<editor-fold desc="Event Triggered on Clicking Back">
    @Override
    public void onBackPressed() {
        Intent i = new Intent(ActivityListCentreConversion.this, ActivityAdminHomeScreen.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
    //</editor-fold>

    //<editor-fold desc="Code to Set Option Menu">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
    //</editor-fold>


    //<editor-fold desc="Code to Bind Data in List">
    public static class ViewHolder {
        TextView tvCode, tvDate, tvId;
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
                arg1 = mInflater.inflate(R.layout.list_stock_conversion, null);
                holder = new ViewHolder();
                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvCode = (TextView) arg1.findViewById(R.id.tvCode);
                holder.tvDate = (TextView) arg1.findViewById(R.id.tvDate);


                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvId.setText(list.get(arg0).get("Id"));
            holder.tvCode.setText(list.get(arg0).get("Code"));
            holder.tvDate.setText(common.convertToDisplayDateFormat(list.get(arg0).get("Date")));

            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Async class Class to handle fetch Adjustment web service call as separate thread">
    private class AsyncStockReturnListListWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(ActivityListCentreConversion.this);

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] name = {"action", "lang", "userId", "date"};
                String dateString;
                Date date = new Date();
                date = dateFormatter_display.parse(tvDate.getText().toString().trim());
                dateString = dateFormatter_database.format(date);
                String[] value = {"ReadCentreSCMaster", lang, userId, dateString};
                responseJSON = "";
                //Call method of web service to download data from server
                responseJSON = common.CallJsonWS(name, value, "ReadDateAndUserWiseReport", common.url);
                return "";
            } catch (SocketTimeoutException e) {
                return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
            } catch (final Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            }
        }

        //After execution of web service
        @Override
        protected void onPostExecute(String result) {
            try {
                if (!result.contains("ERROR: ")) {
                    JSONArray jsonArray = new JSONArray(responseJSON);
                    wordList = new ArrayList<HashMap<String, String>>();
                    String prevName = "";
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            map = new HashMap<String, String>();
                            map.put("Id", jsonArray.getJSONObject(i)
                                    .getString("A"));
                            map.put("Code", jsonArray.getJSONObject(i)
                                    .getString("B"));
                            map.put("Date", jsonArray.getJSONObject(i)
                                    .getString("C"));
                            wordList.add(map);
                        }

                    } else {
                        if (result.contains("null")) {
                            result = "Server not responding. Please try again later.";
                            common.showAlert(ActivityListCentreConversion.this, result, false);
                        }
                    }
                    listSize = wordList.size();
                    if (listSize != 0) {
                        listConvert.setAdapter(new CustomAdapter(mContext, wordList));

                        ViewGroup.LayoutParams params = listConvert.getLayoutParams();
                        listConvert.setLayoutParams(params);
                        listConvert.requestLayout();
                        tvEmpty.setVisibility(View.GONE);
                        tableGridHead.setVisibility(View.VISIBLE);
                    } else {
                        listConvert.setAdapter(null);
                        tvEmpty.setVisibility(View.VISIBLE);
                        tableGridHead.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                common.showAlert(ActivityListCentreConversion.this, "Stock Conversion Downloading failed: " + "Unable to get response from server.", false);
            }
            Dialog.dismiss();
        }

        //To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Stock Conversion..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Validate User">
    private class AsyncValidatePasswordWSCall extends
            AsyncTask<String, Void, String> {
        String source = "";
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityListCentreConversion.this);

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
                                                    AsyncLiveInventoryDetailWSCall task = new AsyncLiveInventoryDetailWSCall();
                                                    task.execute();
                                                }
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    } else {
                        if (common.isConnected()) {
                            AsyncLiveInventoryDetailWSCall task = new AsyncLiveInventoryDetailWSCall();
                            task.execute();
                        }
                    }

                } else {
                    common.showAlert(ActivityListCentreConversion.this,
                            "Unable to fetch response from server.", false);
                }

            } catch (Exception e) {
                common.showAlert(ActivityListCentreConversion.this,
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

    //<editor-fold desc="Async Method to Fetch LiveInventory Fro Retail Outlet">
    private class AsyncLiveInventoryDetailWSCall extends
            AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityListCentreConversion.this);

        @Override
        protected String doInBackground(String... params) {
            try {

                String[] name = {"action", "lang", "userId"};
                String[] value = {"GetCentreLiveInventory", lang, userId};
                // Call method of web service to Read Live Inventory For Centre
                responseJSON = "";
                responseJSON = common.CallJsonWS(name, value, "ReadCentreLiveInventory", common.url);
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
                    String data = "";
                    // To display message after response from server
                    JSONArray jsonSKU = new JSONArray(responseJSON.split("~")[0]);
                    JSONArray jsonRaw = new JSONArray(responseJSON.split("~")[1]);
                    if (jsonSKU.length() > 0 || jsonRaw.length() > 0) {
                        if (jsonSKU.length() > 0) {
                            db.open();
                            db.DeleteMasterData("CentreSKULiveInventory");
                            db.close();
                            if (jsonSKU.length() > 0) {
                                for (int i = 0; i < jsonSKU.length(); ++i) {
                                    db.open();
                                    db.Insert_CentreSKULiveInventory(jsonSKU.getJSONObject(i)
                                            .getString("A"),jsonSKU.getJSONObject(i)
                                            .getString("B"), jsonSKU.getJSONObject(i)
                                            .getString("C"), jsonSKU.getJSONObject(i)
                                            .getString("D").replace(".00", ""));
                                    db.close();

                                }

                            }
                        }
                        if (jsonRaw.length() > 0) {
                            db.open();
                            db.DeleteMasterData("CentreUserCentres");
                            db.close();
                            for (int i = 0; i < jsonRaw.length(); ++i) {
                                db.open();
                                db.Insert_CentreUserCentres(jsonRaw.getJSONObject(i)
                                        .getString("A"), jsonRaw.getJSONObject(i)
                                        .getString("B"));
                                db.close();
                            }

                        }
                        Intent intent = new Intent(ActivityListCentreConversion.this,
                                ActivityCentreConversion.class);
                        intent.putExtra("UniqueId", UUID.randomUUID().toString());
                        startActivity(intent);
                        finish();
                    } else {
                        common.showToast("There is no data available for Inventory!");
                    }

                } else {
                    if (result.contains("null") || result == "")
                        result = "Server not responding. Please try again later.";
                }
            } catch (Exception e) {
                e.printStackTrace();
                common.showToast("Centre Inventory Downloading failed: " + e.toString());
                Intent intent = new Intent(mContext, ActivityListCentreConversion.class);
                startActivity(intent);
                finish();
            }
            Dialog.dismiss();
        }

        // To display message on screen within process
        @Override
        protected void onPreExecute() {
            Dialog.setMessage("Downloading Outlet Inventory..");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to show Change Password Window">
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
                                    AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
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
}
