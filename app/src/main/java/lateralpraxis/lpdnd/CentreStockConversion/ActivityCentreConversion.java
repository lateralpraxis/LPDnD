package lateralpraxis.lpdnd.CentreStockConversion;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.ActivityAdminHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityCentreConversion extends Activity {
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
    private final Context mContext = this;

    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    private UserSessionManager session;
    //</editor-fold>

    //<editor-fold desc="Code for Variable Declaration">
    private ArrayList<HashMap<String, String>> wordListCons = null;
    private ArrayList<HashMap<String, String>> listCons;
    private int conslistSize = 0;

    private ArrayList<HashMap<String, String>> wordListProd = null;
    private ArrayList<HashMap<String, String>> listProd;
    private int prodlistSize = 0;
    private String uniqueId = "";
    Common common;
    String lang = "en";
    String type = "Raw";
    String responseJSON, sendJSon;
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private Spinner spSKU, spProdSKU, spCentre;
    private LinearLayout llProduced, llCentre, llMain;
    private EditText etConsumedQty, etProducedQty;
    private Button btnAddConsumed, btnAddProduced, btnSubmit, btnGo;
    private TextView tvProdEmpty, tvConsEmpty, tvInventory, tvViewQty, tvCentreId, tvCentreName;
    private ListView listConsumed, listProduced;
    private TableLayout tableGridHeadConsumed, tableGridHeadProduced;

    //</editor-fold>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centre_conversion);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        //</editor-fold>

        //<editor-fold desc="Code to Delete Data from Temporary Table">
        db.open();
        db.DeleteTempConversion();
        db.close();
        //</editor-fold>

        //<editor-fold desc="Code to set Unique Id">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            uniqueId = extras.getString("UniqueId");
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
        llProduced = (LinearLayout) findViewById(R.id.llProduced);
        llCentre = (LinearLayout) findViewById(R.id.llCentre);
        llMain = (LinearLayout) findViewById(R.id.llMain);
        spSKU = (Spinner) findViewById(R.id.spSKU);
        spProdSKU = (Spinner) findViewById(R.id.spProdSKU);
        spCentre = (Spinner) findViewById(R.id.spCentre);
        etConsumedQty = (EditText) findViewById(R.id.etConsumedQty);
        etProducedQty = (EditText) findViewById(R.id.etProducedQty);
        btnAddConsumed = (Button) findViewById(R.id.btnAddConsumed);
        btnAddProduced = (Button) findViewById(R.id.btnAddProduced);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnGo = (Button) findViewById(R.id.btnGo);
        tvProdEmpty = (TextView) findViewById(R.id.tvProdEmpty);
        tvConsEmpty = (TextView) findViewById(R.id.tvConsEmpty);
        tvInventory = (TextView) findViewById(R.id.tvInventory);
        tvViewQty = (TextView) findViewById(R.id.tvViewQty);
        tvCentreId = (TextView) findViewById(R.id.tvCentreId);
        tvCentreName = (TextView) findViewById(R.id.tvCentreName);
        listConsumed = (ListView) findViewById(R.id.listConsumed);
        listProduced = (ListView) findViewById(R.id.listProduced);
        tableGridHeadConsumed = (TableLayout) findViewById(R.id.tableGridHeadConsumed);
        tableGridHeadProduced = (TableLayout) findViewById(R.id.tableGridHeadProduced);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(5);
        etConsumedQty.setFilters(FilterArray);
        etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] FilterProdArray = new InputFilter[1];
        FilterProdArray[0] = new InputFilter.LengthFilter(5);
        etProducedQty.setFilters(FilterProdArray);
        etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        //</editor-fold>

        //<editor-fold desc="Code to be executed on change of text">
        TextWatcher textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!etConsumedQty.getText().toString().equalsIgnoreCase(".")) {
                    if (etConsumedQty.getText().toString().equalsIgnoreCase("."))
                        etConsumedQty.setText("");

                } else {
                    etConsumedQty.setText("");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etConsumedQty.addTextChangedListener(textWatcher);

        TextWatcher textWatcherprod = new TextWatcher() {
            public void afterTextChanged(Editable s) {

                if (!etProducedQty.getText().toString().equalsIgnoreCase(".")) {
                    if (etProducedQty.getText().toString().equalsIgnoreCase("."))
                        etProducedQty.setText("");

                } else {
                    etProducedQty.setText("");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        etProducedQty.addTextChangedListener(textWatcherprod);

        etProducedQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (Pattern.matches(fpRegex, etProducedQty.getText())) {

                    } else
                        etProducedQty.setText("");

                }
            }
        });
        etConsumedQty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (Pattern.matches(fpRegex, etConsumedQty.getText())) {

                    } else
                        etConsumedQty.setText("");

                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to Bind Spinners">
        spCentre.setAdapter(DataAdapter("centreusercentre", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change on Consumed SKU Spinner">
        spSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etConsumedQty.setText("");
                db.open();
                tvInventory.setText(db.getCentreSkuInventory(((CustomType) spSKU.getSelectedItem()).getId()));
                tvViewQty.setText(db.getCentreSkuInventory(((CustomType) spSKU.getSelectedItem()).getId()));
                db.close();
                if (((CustomType) spSKU.getSelectedItem()).getId().split("-")[1].equalsIgnoreCase("0")) {
                    etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 5;
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    etConsumedQty.setFilters(FilterArray);
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change on Produced SKU Spinner">
        spProdSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etProducedQty.setText("");
                if (((CustomType) spProdSKU.getSelectedItem()).getId().split("-")[1].equalsIgnoreCase("0")) {
                    etProducedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 5;
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(maxLength);
                    etProducedQty.setFilters(FilterArray);
                    etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click ob Button Add Consumed Click">
        btnAddConsumed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                etConsumedQty.clearFocus();
                 if (type.equalsIgnoreCase("SKU") && spSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पाद अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etConsumedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत मात्रा अनिवार्य है।" : "Consumed quantity is mandatory.");
                else if (Double.valueOf(etConsumedQty.getText().toString()) > Double.valueOf(tvInventory.getText().toString()))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत मात्रा उपलब्ध मात्रा से अधिक नहीं हो सकती।" : "Consumed quantity cannot exceed available quantity.");
                else {
                    db.openR();
                    Boolean alreadyAdded = db.isConsumedAlreadyAdded("0", ((CustomType) spSKU.getSelectedItem()).getId().split("-")[0]);
                    if (alreadyAdded)
                        common.showToast(lang.equalsIgnoreCase("hi") ? "खपत आइटम पहले ही जोड़ दिया गया है।" : "Consumed item already added.");
                    else {
                        db.open();
                        db.Insert_OutletConversionConsumedTemp("0", ((CustomType) spSKU.getSelectedItem()).getId().split("-")[0], Double.valueOf(etConsumedQty.getText().toString()).toString());
                        db.close();
                        spSKU.setSelection(0);
                        etConsumedQty.setText("");
                        common.showToast(lang.equalsIgnoreCase("hi") ? "खपत किया गया आइटम सफलतापूर्वक जोड़ा गया।" : "Consumed item added successfully.");

                        wordListCons = new ArrayList<HashMap<String, String>>();
                        db.openR();
                        wordListCons = db.getCentreTempConsumed();
                        conslistSize = wordListCons.size();
                        if (conslistSize != 0) {
                            listConsumed.setAdapter(new CustomAdapter(mContext, wordListCons));

                            ViewGroup.LayoutParams params = listConsumed.getLayoutParams();
                            listConsumed.setLayoutParams(params);
                            listConsumed.requestLayout();
                            tvConsEmpty.setVisibility(View.GONE);
                            tableGridHeadConsumed.setVisibility(View.VISIBLE);
                            llProduced.setVisibility(View.VISIBLE);
                        } else {
                            listConsumed.setAdapter(null);
                            tvConsEmpty.setVisibility(View.VISIBLE);
                            tableGridHeadConsumed.setVisibility(View.GONE);
                            llProduced.setVisibility(View.GONE);
                        }
                    }
                }

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click ob Button Add Produced Click">
        btnAddProduced.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                etProducedQty.clearFocus();
                if (spProdSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "एसकेयू अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etProducedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित मात्रा अनिवार्य है।" : "Produced quantity is mandatory.");
                else {
                    db.openR();
                    Boolean alreadyAdded = db.isProducedAlreadyAdded(((CustomType) spProdSKU.getSelectedItem()).getId().split("-")[0]);
                    if (alreadyAdded)
                        common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित आइटम पहले ही जोड़ दिया गया है।" : "Produced item already added.");
                    else {
                        db.open();
                        db.Insert_OutletConversionProducedTemp(((CustomType) spProdSKU.getSelectedItem()).getId().split("-")[0], Double.valueOf(etProducedQty.getText().toString()).toString());
                        db.close();
                        spProdSKU.setSelection(0);
                        etProducedQty.setText("");
                        common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित किया गया आइटम सफलतापूर्वक जोड़ा गया।" : "Produced item added successfully.");

                        wordListProd = new ArrayList<HashMap<String, String>>();
                        db.openR();
                        wordListProd = db.getCentreTempProduced();
                        prodlistSize = wordListProd.size();
                        if (prodlistSize != 0) {
                             listProduced.setAdapter(new CustomAdapterProduced(mContext, wordListProd));

                            ViewGroup.LayoutParams params = listProduced.getLayoutParams();
                            listProduced.setLayoutParams(params);
                            listProduced.requestLayout();
                            tvProdEmpty.setVisibility(View.GONE);
                            tableGridHeadProduced.setVisibility(View.VISIBLE);
                            btnSubmit.setVisibility(View.VISIBLE);
                        } else {
                            listProduced.setAdapter(null);
                            tvProdEmpty.setVisibility(View.VISIBLE);
                            tableGridHeadProduced.setVisibility(View.GONE);
                            btnSubmit.setVisibility(View.GONE);
                        }
                    }
                }

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Button Submit Click">
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक कन्वर्सन सुरक्षित करना चाहते हैं?" : "Are you sure, you want to save stock conversion transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                if (common.isConnected()) {
                                    AsyncCentreConversionWSCall task = new AsyncCentreConversionWSCall();
                                    task.execute();
                                }
                            }
                        }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // if this button is clicked, just close
                                dialog.cancel();
                            }
                        });
                AlertDialog alertnew = builder1.create();
                alertnew.show();

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Button Go Click">
        btnGo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (spCentre.getSelectedItemPosition() == 0) {
                    common.showToast(lang.equalsIgnoreCase("hi") ? "कृपया केंद्र का चयन करें" : "Please select centre.");
                } else {
                    llCentre.setVisibility(View.GONE);
                    llMain.setVisibility(View.VISIBLE);
                    tvCentreId.setText(((CustomType) spCentre.getSelectedItem()).getId());
                    tvCentreName.setText(((CustomType) spCentre.getSelectedItem()).getName());
                    spSKU.setAdapter(DataAdapter("centreskuinv", ((CustomType) spCentre.getSelectedItem()).getId()));
                    spProdSKU.setAdapter(DataAdapter("centresku", ""));
                }

            }
        });
        //</editor-fold>
    }

    //<editor-fold desc="Code for Binding Data In Spinner">
    private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter) {
        db.open();
        List<CustomType> lables = db.GetCustomerMasterDetails(masterType, filter);
        ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this, android.R.layout.simple_spinner_item, lables);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        db.close();
        return dataAdapter;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be Bind Data in consumed list view">
    public static class ViewHolder {
        TextView tvId, tvName, tvQty;
        Button btnDelete;
    }

    //<editor-fold desc="Code to be Bind Data in produced list view">
    public static class ProducedViewHolder {
        TextView tvId, tvName, tvQty;
        Button btnDelete;
    }
    //</editor-fold>

    public class CustomAdapter extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
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
                arg1 = mInflater.inflate(R.layout.list_consumed_items, null);
                holder = new ViewHolder();

                holder.tvName = (TextView) arg1.findViewById(R.id.tvName);
                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                holder.btnDelete = (Button) arg1.findViewById(R.id.btnDelete);
                arg1.setTag(holder);

            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            holder.tvId.setText(listCons.get(arg0).get("Id"));
            holder.tvName.setText(listCons.get(arg0).get("Name"));
            holder.tvQty.setText(common.stringToOneDecimal(listCons.get(arg0).get("Quantity")).replace(".0",""));

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "खपत वस्तु को हटाएं" : "Delete Consumed Item");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप इस खपत वस्तु को हटाना चाहते हैं" : "Are you sure you want to delete this consumed item?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    db.open();
                                    db.deleteTempConsumption(holder.tvId.getText().toString());
                                    db.close();
                                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत वस्तु सफलतापूर्वक हटा दिया गया" : "Consumed item deleted successfully.");
                                    wordListCons = new ArrayList<HashMap<String, String>>();
                                    db.openR();
                                    wordListCons = db.getTempConsumed();
                                    conslistSize = wordListCons.size();
                                    if (conslistSize != 0) {
                                        listConsumed.setAdapter(new CustomAdapter(mContext, wordListCons));

                                        ViewGroup.LayoutParams params = listConsumed.getLayoutParams();
                                        listConsumed.setLayoutParams(params);
                                        listConsumed.requestLayout();
                                        tvConsEmpty.setVisibility(View.GONE);
                                        tableGridHeadConsumed.setVisibility(View.VISIBLE);
                                        llProduced.setVisibility(View.VISIBLE);
                                    } else {
                                        listConsumed.setAdapter(null);
                                        tvConsEmpty.setVisibility(View.VISIBLE);
                                        tableGridHeadConsumed.setVisibility(View.GONE);
                                        llProduced.setVisibility(View.GONE);
                                    }
                                }
                            })
                            .setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं"
                                    : "No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertnew = builder1.create();
                    alertnew.show();

                }
            });

            arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
            return arg1;
        }
    }

    public class CustomAdapterProduced extends BaseAdapter {
        private Context docContext;
        private LayoutInflater mInflater;

        public CustomAdapterProduced(Context context, ArrayList<HashMap<String, String>> lvList) {
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
                arg1 = mInflater.inflate(R.layout.list_produced_items, null);
                holder = new ProducedViewHolder();

                holder.tvName = (TextView) arg1.findViewById(R.id.tvName);
                holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
                holder.tvQty = (TextView) arg1.findViewById(R.id.tvQty);
                holder.btnDelete = (Button) arg1.findViewById(R.id.btnDelete);
                arg1.setTag(holder);

            } else {
                holder = (ProducedViewHolder) arg1.getTag();
            }
            holder.tvId.setText(listProd.get(arg0).get("Id"));
            holder.tvName.setText(listProd.get(arg0).get("Name"));
            holder.tvQty.setText(common.stringToOneDecimal(listProd.get(arg0).get("Quantity")).replace(".0",""));

            holder.btnDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "उत्पादित वस्तु को हटाएं" : "Delete Produced Item");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप इस उत्पादित वस्तु को हटाना चाहते हैं" : "Are you sure you want to delete this produced item?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    db.open();
                                    db.deleteTempProduction(holder.tvId.getText().toString());
                                    db.close();
                                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित वस्तु सफलतापूर्वक हटा दिया गया" : "Produced item deleted successfully.");
                                    wordListProd = new ArrayList<HashMap<String, String>>();
                                    db.openR();
                                    wordListProd = db.getTempProduced();
                                    prodlistSize = wordListProd.size();
                                    if (prodlistSize != 0) {
                                        listProduced.setAdapter(new CustomAdapterProduced(mContext, wordListProd));

                                        ViewGroup.LayoutParams params = listProduced.getLayoutParams();
                                        listProduced.setLayoutParams(params);
                                        listProduced.requestLayout();
                                        tvProdEmpty.setVisibility(View.GONE);
                                        tableGridHeadProduced.setVisibility(View.VISIBLE);
                                        btnSubmit.setVisibility(View.VISIBLE);
                                    } else {
                                        listProduced.setAdapter(null);
                                        tvProdEmpty.setVisibility(View.VISIBLE);
                                        tableGridHeadProduced.setVisibility(View.GONE);
                                        btnSubmit.setVisibility(View.GONE);
                                    }
                                }
                            })
                            .setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं"
                                    : "No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertnew = builder1.create();
                    alertnew.show();

                }
            });

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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक रूपांतरण मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock conversion module it will discard stock conversion transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent i = new Intent(ActivityCentreConversion.this, ActivityAdminHomeScreen.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            }
                        }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // if this button is clicked, just close
                                dialog.cancel();
                            }
                        });
                AlertDialog alertnew = builder1.create();
                alertnew.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to b executed on Back Press">
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
        builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
        builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक रूपांतरण मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock conversion module it will discard stock conversion transaction?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent i = new Intent(ActivityCentreConversion.this, ActivityListCentreConversion.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        // if this button is clicked, just close
                        dialog.cancel();
                    }
                });
        AlertDialog alertnew = builder1.create();
        alertnew.show();

    }
    //</editor-fold>

    //<editor-fold desc="Async Method to Post Centre Conversion Details">
    private class AsyncCentreConversionWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                ActivityCentreConversion.this);

        @Override
        protected String doInBackground(String... params) {

            try {

                responseJSON = "";

                JSONObject jsonMaster = new JSONObject();

                String customerId = "";
                HashMap<String, String> user = session.getLoginUserDetails();
                customerId = user.get(UserSessionManager.KEY_ID);
                JSONArray array = new JSONArray();
                // To make json string to post delivery

                JSONObject jsonins = new JSONObject();
                jsonins.put("UniqueId", uniqueId);
                jsonins.put("CentreId", tvCentreId.getText().toString());
                jsonins.put("ipAddress", common.getDeviceIPAddress(true));
                jsonins.put("Machine", common.getIMEI());
                jsonins.put("CreateBy", customerId);
                array.put(jsonins);
                jsonMaster.put("Master", array);

                JSONObject jsonDetails = new JSONObject();
                // To get Conversion details from database
                db.open();
                ArrayList<HashMap<String, String>> insdet = db.getConversionForSync();
                db.close();
                if (insdet != null && insdet.size() > 0) {

                    // To make json string to post delivery details
                    JSONArray arraydet = new JSONArray();
                    for (HashMap<String, String> insd : insdet) {
                        JSONObject jsondet = new JSONObject();
                        jsondet.put("MaterialId", insd.get("RawMaterialId"));
                        jsondet.put("SkuId", insd.get("SkuId"));
                        jsondet.put("Quantity", insd.get("Quantity"));
                        jsondet.put("SkuType", insd.get("SKUType"));
                        arraydet.put(jsondet);
                    }
                    jsonDetails.put("Detail", arraydet);
                }
                sendJSon = jsonMaster + "~" + jsonDetails;
                // To invoke json web service to create delivery
                responseJSON = common.invokeJSONWS(sendJSon, "json",
                        "CreateCentreStockConversion", common.url);

                return responseJSON;
            } catch (Exception e) {
                // TODO: handle exception
                return "ERROR: " + "Unable to get response from server.";
            } finally {
                db.close();
            }

        }

        // After execution of json web service to create delivery
        @Override
        protected void onPostExecute(String result) {

            try {
                // To display message after response from server
                if (!result.contains("ERROR")) {
                    if (responseJSON.equalsIgnoreCase("success")) {
                        //<editor-fold desc="Code to Delete Data from Temporary Table">
                        db.open();
                        db.DeleteTempConversion();
                        db.DeleteMasterData("OutletInventory");
                        db.close();
                        //</editor-fold>
                     /*   if (common.isConnected()) {
                            AsyncRetailOutletInventoryWSCall task = new AsyncRetailOutletInventoryWSCall();
                            task.execute();
                        }*/
                        common.showToast(lang.equalsIgnoreCase("hi") ? "स्टॉक कनवर्ज़न सफलतापूर्वक सहेजा गया" : "Stock Conversion saved successfully.");
                        Intent i = new Intent(ActivityCentreConversion.this, ActivityAdminHomeScreen.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
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

            Dialog.setMessage("Posting Stock Conversion...");
            Dialog.setCancelable(false);
            Dialog.show();
        }
    }
    //</editor-fold>
}
