package lateralpraxis.lpdnd.OutletSale;

//<editor-fold desc="Import">

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.ListViewHelper;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
//</editor-fold>

@SuppressLint("InflateParams")
public class ActivityOutletSaleCreate extends ListActivity {

    //<editor-fold desc="Code to declare variables">
    final Context context = this;
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    final String fpRegex =
            ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                    "[+-]?(" +         // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from the Java Language Specification, 2nd
                    // edition, section 3.10.2.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");
    UserSessionManager session;
    double totalQty = 0;
    private Button btnGo, btnCreate;
    private Common common;
    private DatabaseAdapter dba;
    private String[] arrTemp;
    private TextView tvNoRecord, tvTotalAmount;
    private String userId, customer;
    private String lang, saleType;
    private RadioGroup radioGroupType;
    private View tvDivider;
    private int idx;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on page load">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_sale_create);

        common = new Common(getApplicationContext());
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        // To create instance of user session
        session = new UserSessionManager(getApplicationContext());

        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        // To read user role from user session manager
        final HashMap<String, String> user = session.getLoginUserDetails();
        userId = user.get(UserSessionManager.KEY_ID);
        customer = user.get(UserSessionManager.KEY_USERNAME);

        // to create object of controls
        btnGo = (Button) findViewById(R.id.btnGo);
        btnCreate = (Button) findViewById(R.id.btnCreate);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        radioGroupType = (RadioGroup) findViewById(R.id.radioGroupType);
        tvDivider = findViewById(R.id.tvDivider);
        tvDivider.setVisibility(View.GONE);

        // To create instance of database
        dba = new DatabaseAdapter(this);
        dba.open();

        dba.openR();
        ArrayList<HashMap<String, String>> listItem = dba.getOutletSaleInput();
        if (listItem.size() > 0) {
            // To show product and its detail
            setListAdapter(new CustomAdapter(this, listItem));
            arrTemp = new String[listItem.size()];
            ListViewHelper.getListViewSize(getListView());
            btnCreate.setVisibility(View.VISIBLE);
            tvNoRecord.setVisibility(View.GONE);
            tvDivider.setVisibility(View.VISIBLE);
        } else {
            // To show 'no records found'
            setListAdapter(null);
            btnCreate.setVisibility(View.GONE);
            tvNoRecord.setVisibility(View.VISIBLE);
            tvDivider.setVisibility(View.GONE);
        }

        // Event hander to create sale button
        btnCreate.setOnClickListener(new View.OnClickListener() {
            // When create button click
            @Override
            public void onClick(View arg0) {
                // To validate required field and please enter at least one
                // quantity!
                int zeroCount = 0, zeroCountRate = 0, totalRow = 0;
                int invalidCount = 0, invalidCountRate = 0;
                for (int i = 0; i < getListView().getChildCount(); i++) {
                    totalRow++;
                    View v = getListView().getChildAt(i);
                    EditText etSaleQty = (EditText) v.findViewById(R.id.etSaleQty);
                    if (etSaleQty.getText().toString().equalsIgnoreCase("."))
                        invalidCount = invalidCount + 1;
                    if (!etSaleQty.getText().toString().equalsIgnoreCase(".")) {
                        String qty = etSaleQty.length() == 0 ? "0" : etSaleQty
                                .getText().toString().trim();
                        if (etSaleQty.length() == 0 || Double.parseDouble(qty) == 0)
                            zeroCount++;
                    }
//rate
                    EditText etSaleRate = (EditText) v.findViewById(R.id.etSaleRate);
                    if (etSaleRate.getText().toString().equalsIgnoreCase("."))
                        invalidCountRate = invalidCountRate + 1;
                    if (!etSaleRate.getText().toString().equalsIgnoreCase(".")) {
                        String rate = etSaleRate.length() == 0 ? "0" : etSaleRate
                                .getText().toString().trim();
                        if (etSaleRate.length() == 0 || Double.parseDouble(rate) == 0)
                            zeroCountRate++;
                    }
                }
                if (invalidCountRate > 0 && lang.equalsIgnoreCase("en"))
                    common.showAlert(ActivityOutletSaleCreate.this, "Please enter valid rate!", false);
                else if (invalidCountRate > 0 && lang.equalsIgnoreCase("hi"))
                    common.showAlert(ActivityOutletSaleCreate.this, "कृपया मूल्यांकन दर्ज करें!", false);

                else if (totalRow == zeroCountRate && lang.equalsIgnoreCase("en"))
                    common.showAlert(ActivityOutletSaleCreate.this, "Please enter atleast one rate!", false);
                else if (totalRow == zeroCountRate && lang.equalsIgnoreCase("hi"))
                    common.showAlert(ActivityOutletSaleCreate.this, "कृपया कम से कम एक मूल्यांकन दर्ज करें!", false);

                if (invalidCount > 0 && lang.equalsIgnoreCase("en"))
                    common.showAlert(ActivityOutletSaleCreate.this, "Please enter valid quantity!", false);
                else if (invalidCount > 0 && lang.equalsIgnoreCase("hi"))
                    common.showAlert(ActivityOutletSaleCreate.this, "कृपया वैध मात्रा दर्ज करें!", false);

                else if (totalRow == zeroCount && lang.equalsIgnoreCase("en"))
                    common.showAlert(ActivityOutletSaleCreate.this, "Please enter atleast one quantity!", false);
                else if (totalRow == zeroCount && lang.equalsIgnoreCase("hi"))
                    common.showAlert(ActivityOutletSaleCreate.this, "कृपया कम से कम एक मात्रा दर्ज करें!", false);

                else {
                    // Confirmation message before submit data
                    Builder alertDialogBuilder = new Builder(context);
                    alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    alertDialogBuilder
                            .setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?" : "Are you sure, you want to submit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int id) {
                                    if (common.isConnected()) {
                                        try {
                                            int radioButtonID = radioGroupType.getCheckedRadioButtonId();
                                            View radioButton = radioGroupType.findViewById(radioButtonID);
                                            idx = radioGroupType.indexOfChild(radioButton);
                                            if (idx == 0)
                                                saleType = "Cash";
                                            else
                                                saleType = "Credit";
                                            // To store data in sale table
                                            dba.open();
                                            String insertDelId = dba.Insert_OutletSale(UUID.randomUUID().toString(), userId, customer, saleType, userId, common.getIMEI());
                                            if (insertDelId.contains("success")) {
                                                for (int i = 0; i < getListView().getChildCount(); i++) {
                                                    View v = getListView().getChildAt(i);
                                                    TextView tvId = (TextView) v.findViewById(R.id.tvId);
                                                    TextView tvItem = (TextView) v.findViewById(R.id.tvSku);
                                                    TextView tvRate = (TextView) v.findViewById(R.id.tvRate);
                                                    TextView tvQty = (TextView) v.findViewById(R.id.tvQty);
                                                    EditText etSaleRate = (EditText) v.findViewById(R.id.etSaleRate);
                                                    EditText etSaleQty = (EditText) v.findViewById(R.id.etSaleQty);
                                                    //To validate if user enter only .
                                                    if (!etSaleQty.getText().toString().equalsIgnoreCase(".")) {
                                                        String qty = etSaleQty.getText().toString().trim().length() == 0 ? "0" : String.valueOf(Double.valueOf(etSaleQty.getText().toString().trim()));
                                                        if (Double.parseDouble(qty) != 0) {
                                                            dba.Insert_OutletSaleDetail(insertDelId.split("~")[1], tvId.getText().toString(), tvItem.getText().toString(), tvRate.getText().toString().replace(",", ""), etSaleRate.getText().toString().replace("-", "0"), tvQty.getText().toString(), qty);
                                                        }
                                                    }
                                                }

                                                setListAdapter(null);
                                                btnCreate.setVisibility(View.GONE);
                                                tvNoRecord.setVisibility(View.GONE);
                                                tvTotalAmount.setText("");
                                                dba.close();
                                                if (lang.equalsIgnoreCase("hi"))
                                                common.showToast("बिक्री सफलतापूर्वक बनाई गई|");
                                                else
                                                common.showToast("Sale created successfully.");

                                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                                //To bind default sale date
                                                final Calendar c = Calendar.getInstance();
                                                c.add(Calendar.DATE, 0);
                                                String saleDate = dateFormatter.format(c.getTime());

                                                Intent intent;
                                                intent = new Intent(context, ActivityOutletSaleViewSummary.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();

//                                                Intent intent;
//                                                intent = new Intent(context, ActivityOutletSaleViewDetail.class);
//                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                intent.putExtra("Id", insertDelId.split("~")[1]);
//                                                intent.putExtra("Date",saleDate);
//                                                intent.putExtra("Name",saleType);
//                                                startActivity(intent);
//                                                finish();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            common.showAlert(ActivityOutletSaleCreate.this, "Error in Saving sale.\n" + e.getMessage(), false);
                                        }
                                    } else {
                                        dba.insertExceptions("Unable to connect to Internet !", "ActivityCreatedelivery.java", "onCreate()");
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
        for (int i = 0; i < getListView().getChildCount(); i++) {
            View v = getListView().getChildAt(i);
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

    //<editor-fold desc="onOptionsItemSelected">
    // When press back button go to home screen
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
        Intent homeScreenIntent = new Intent(this, ActivityOutletSaleViewSummary.class);
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
        finish();
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
                        R.layout.activity_outlet_sale_create_item, null);
                holder = new ViewHolder();

                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvSku = (TextView) arg1.findViewById(R.id.tvSku);
                holder.tvRate = (TextView) arg1.findViewById(R.id.tvRate);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                holder.etSaleRate = (EditText) arg1.findViewById(R.id.etSaleRate);
                holder.etSaleQty = (EditText) arg1.findViewById(R.id.etSaleQty);
                holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.ref = arg0;

            //Allowed only 2 decimal value
            holder.etSaleRate.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 2)});
            holder.etSaleRate.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

            // To bind data from list
            holder.tvId.setText(_listItems.get(arg0).get("id"));
            holder.tvSku.setText(lang.equalsIgnoreCase("hi") ? _listItems.get(arg0).get("skuLocal") : _listItems.get(arg0).get("sku"));
            holder.tvQty.setText(String.format("%.1f",
                    Double.parseDouble(_listItems.get(arg0).get("aqty"))).replace(".0", ""));
            holder.etSaleRate.setText(_listItems.get(arg0).get("rate"));
            holder.tvRate.setText(common.stringToTwoDecimal(_listItems.get(arg0).get("rate")));
            if (arrTemp[arg0] != null)
                holder.etSaleQty.setText(String.format("%.1f",
                        Double.parseDouble(arrTemp[arg0])));
            if (_listItems.get(arg0).get("type").equals("0")) {
                // To display decimal point in number key board control
                holder.etSaleQty
                        .setFilters(new InputFilter[]{new DecimalDigitsInputFilter(
                                5, 1)});
                holder.etSaleQty.setInputType(InputType.TYPE_CLASS_NUMBER
                        + InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                // To display only number in key board control
                int maxLength = 3;
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                holder.etSaleQty.setFilters(FilterArray);
                holder.etSaleQty.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            // Instantiates a TextWatcher, to observe value changes and trigger the result calculation
            TextWatcher textWatcher = new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (!holder.etSaleQty.getText().toString().equalsIgnoreCase(".")) {
                        if (holder.etSaleQty.getText().toString().equalsIgnoreCase("."))
                            holder.etSaleQty.setText("");
                        if (holder.etSaleQty.getText().toString().trim().length() > 0) {
                            if (Double.parseDouble(holder.etSaleQty.getText().toString()) == 0) {
                                holder.etSaleQty.setText("");
                                holder.tvAmount.setText("");
                            } else if (Double.parseDouble(holder.etSaleQty.getText().toString().trim()) > Double.parseDouble(holder.tvQty.getText().toString().trim())) {
                                if (lang.equalsIgnoreCase("hi"))
                                common.showToast("\n" +"बिक्री मात्रा उपलब्ध मात्रा से अधिक नहीं होनी चाहिए।");
                                else
                                common.showToast("Sale quantity should not be exceeded from available quantity!");
                                holder.etSaleQty.setText("");
                            } else
                                holder.tvAmount.setText(common.stringToTwoDecimal(String.valueOf(Double.parseDouble(holder.etSaleQty.getText().toString()) * Double.parseDouble(holder.etSaleRate.getText().toString().replace(",", "")))));
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
            holder.etSaleQty.addTextChangedListener(textWatcher);

            // Fix for text selection handle not disappearing
            holder.etSaleQty.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view2, boolean hasFocus) {
                    view2.dispatchWindowFocusChanged(hasFocus);
                }
            });
            // Adds the TextWatcher as TextChangedListener to both EditTexts
            holder.etSaleRate.addTextChangedListener(textWatcher);

            // Fix for text selection handle not disappearing
            holder.etSaleRate.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view2, boolean hasFocus) {
                    view2.dispatchWindowFocusChanged(hasFocus);
                }
            });
            return arg1;
        }

        class ViewHolder {
            TextView tvId, tvSku, tvRate, tvQty, tvAmount;
            EditText etSaleRate, etSaleQty;
            int ref;
        }
    }
    //</editor-fold>
}
