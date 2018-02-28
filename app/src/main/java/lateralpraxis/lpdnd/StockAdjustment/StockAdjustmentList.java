package lateralpraxis.lpdnd.StockAdjustment;

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
import android.text.InputFilter;
import android.text.InputType;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.types.CustomType;

public class StockAdjustmentList extends Activity {

    private final Context mContext = this;

    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    String lang = "en";
    String type = "Raw";
    String responseJSON, sendJSon;
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
    //</editor-fold>

    //<editor-fold desc="Code for Control Declaration">
    private RadioGroup RadioType;
    private RadioButton RadioRaw, RadioSKU;
    private Spinner spRawMaterial, spSKU, spProdSKU;
    private LinearLayout llRawMaterial, llSKU, llProduced;
    private EditText etConsumedQty, etProducedQty;
    private Button btnAddConsumed, btnAddProduced, btnSubmit;
    private TextView tvProdEmpty, tvConsEmpty, tvInventory;
    private ListView listConsumed, listProduced;
    private TableLayout tableGridHeadConsumed, tableGridHeadProduced;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stock_conversion);

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
        llRawMaterial = (LinearLayout) findViewById(R.id.llRawMaterial);
        llSKU = (LinearLayout) findViewById(R.id.llSKU);
        llProduced = (LinearLayout) findViewById(R.id.llProduced);
        RadioType = (RadioGroup) findViewById(R.id.RadioType);
        RadioRaw = (RadioButton) findViewById(R.id.RadioRaw);
        RadioSKU = (RadioButton) findViewById(R.id.RadioSKU);
        spRawMaterial = (Spinner) findViewById(R.id.spRawMaterial);
        spSKU = (Spinner) findViewById(R.id.spSKU);
        spProdSKU = (Spinner) findViewById(R.id.spProdSKU);
        etConsumedQty = (EditText) findViewById(R.id.etConsumedQty);
        etProducedQty = (EditText) findViewById(R.id.etProducedQty);
        btnAddConsumed = (Button) findViewById(R.id.btnAddConsumed);
        btnAddProduced = (Button) findViewById(R.id.btnAddProduced);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        tvProdEmpty = (TextView) findViewById(R.id.tvProdEmpty);
        tvConsEmpty = (TextView) findViewById(R.id.tvConsEmpty);
        tvInventory = (TextView) findViewById(R.id.tvInventory);
        listConsumed = (ListView) findViewById(R.id.listConsumed);
        listProduced = (ListView) findViewById(R.id.listProduced);
        tableGridHeadConsumed = (TableLayout) findViewById(R.id.tableGridHeadConsumed);
        tableGridHeadProduced = (TableLayout) findViewById(R.id.tableGridHeadProduced);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
        etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etProducedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(6, 2)});
        etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        //<editor-fold desc="Code to Bind Spinners">
        spRawMaterial.setAdapter(DataAdapter("rawmaterialinv", ""));
        spSKU.setAdapter(DataAdapter("skuinv", ""));
        spProdSKU.setAdapter(DataAdapter("sku", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be exceuted on change of Radio Button">
        RadioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = RadioType.findViewById(checkedId);
                int index = RadioType.indexOfChild(radioButton);
                spRawMaterial.setSelection(0);
                spSKU.setSelection(0);
                etConsumedQty.setText("");
                if (index == 0) {
                    llRawMaterial.setVisibility(View.VISIBLE);
                    llSKU.setVisibility(View.GONE);
                    etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    type = "Raw";
                } else {
                    llRawMaterial.setVisibility(View.GONE);
                    llSKU.setVisibility(View.VISIBLE);
                    type = "SKU";
                }
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Selected Index change on Consumed SKU Spinner">
        spSKU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etProducedQty.setText("");
                db.open();
                tvInventory.setText(db.getSkuInventory(((CustomType) spSKU.getSelectedItem()).getId().split("~")[0]));
                db.close();
                if (((CustomType) spSKU.getSelectedItem()).getId().split("~")[1].equalsIgnoreCase("0")) {
                    etProducedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etProducedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 4;
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

        //<editor-fold desc="Code to be executed on Selected Index change on Consumed Raw Material Spinner">
        spRawMaterial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                etProducedQty.setText("");
                db.open();
                tvInventory.setText(db.getRawMaterialInventory(((CustomType) spRawMaterial.getSelectedItem()).getId()));
                db.close();
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
                etConsumedQty.setText("");
                if (((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[1].equalsIgnoreCase("0")) {
                    etConsumedQty.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});
                    etConsumedQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    int maxLength = 4;
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

        //<editor-fold desc="Code to be executed on Click ob Button Add Consumed Click">
        btnAddConsumed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (type.equalsIgnoreCase("Raw") && spRawMaterial.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "कच्ची सामग्री अनिवार्य है।" : "Raw Material is mandatory.");
                else if (type.equalsIgnoreCase("SKU") && spSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "एसकेयू अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etConsumedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत मात्रा अनिवार्य है।" : "Consumed quantity is mandatory.");
                else if (Double.valueOf(etConsumedQty.getText().toString()) > Double.valueOf(tvInventory.getText().toString()))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "खपत मात्रा उपलब्ध मात्रा से अधिक नहीं हो सकती।" : "Consumed quantity cannot exceed available quantity.");
                else {
                    db.openR();
                    Boolean alreadyAdded = db.isConsumedAlreadyAdded(((CustomType) spRawMaterial.getSelectedItem()).getId(), ((CustomType) spSKU.getSelectedItem()).getId().split("~")[0]);
                    if (alreadyAdded)
                        common.showToast(lang.equalsIgnoreCase("hi") ? "खपत आइटम पहले ही जोड़ दिया गया है।" : "Consumed item already added.");
                    else {
                        db.open();
                        db.Insert_OutletConversionConsumedTemp(((CustomType) spRawMaterial.getSelectedItem()).getId(), ((CustomType) spSKU.getSelectedItem()).getId().split("~")[0], etConsumedQty.getText().toString());
                        db.close();
                        spRawMaterial.setSelection(0);
                        spSKU.setSelection(0);
                        etConsumedQty.setText("");
                        common.showToast(lang.equalsIgnoreCase("hi") ? "खपत किया गया आइटम सफलतापूर्वक जोड़ा गया।" : "Consumed item added successfully.");

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
                }

            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Click ob Button Add Produced Click">
        btnAddProduced.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (spProdSKU.getSelectedItemPosition() == 0)
                    common.showToast(lang.equalsIgnoreCase("hi") ? "एसकेयू अनिवार्य है।" : "SKU is mandatory.");
                else if (String.valueOf(etProducedQty.getText()).trim().equals(""))
                    common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित मात्रा अनिवार्य है।" : "Produced quantity is mandatory.");
                else {
                    db.openR();
                    Boolean alreadyAdded = db.isProducedAlreadyAdded(((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[0]);
                    if (alreadyAdded)
                        common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित आइटम पहले ही जोड़ दिया गया है।" : "Produced item already added.");
                    else {
                        db.open();
                        db.Insert_OutletConversionProducedTemp(((CustomType) spProdSKU.getSelectedItem()).getId().split("~")[0], etProducedQty.getText().toString());
                        db.close();
                        spProdSKU.setSelection(0);
                        etProducedQty.setText("");
                        common.showToast(lang.equalsIgnoreCase("hi") ? "उत्पादित किया गया आइटम सफलतापूर्वक जोड़ा गया।" : "Produced item added successfully.");

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
                                    AsyncOutletConversionWSCall task = new AsyncOutletConversionWSCall();
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
    }
    //</editor-fold>

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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक रूपांतरण मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock conversion module it will discard stock conversion transaction?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent i = new Intent(StockAdjustmentList.this, ActivityHomeScreen.class);
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
        builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
        builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप स्टॉक रूपांतरण मॉड्यूल छोड़ना चाहते हैं, यह स्टॉक रूपांतरण को छोड़ देगा?" : "Are you sure, you want to leave stock conversion module it will discard stock conversion transaction?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int id) {
                        Intent i = new Intent(StockAdjustmentList.this, ActivityHomeScreen.class);
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

    //<editor-fold desc="Code to be Bind Data in consumed list view">
    public static class ViewHolder {
        TextView tvId, tvName, tvQty;
        Button btnDelete;
    }
    //</editor-fold>

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
            holder.tvQty.setText(listCons.get(arg0).get("Quantity"));

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
    //</editor-fold>

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
            holder.tvQty.setText(listProd.get(arg0).get("Quantity"));

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

    //<editor-fold desc="Async Method to Post Outlet Conversion Details">
    private class AsyncOutletConversionWSCall extends AsyncTask<String, Void, String> {
        private ProgressDialog Dialog = new ProgressDialog(
                StockAdjustmentList.this);

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
                jsonins.put("CustomerId", customerId);
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
                        jsondet.put("RawMaterialId", insd.get("RawMaterialId"));
                        jsondet.put("SkuId", insd.get("SkuId"));
                        jsondet.put("Quantity", insd.get("Quantity"));
                        jsondet.put("SKUType", insd.get("SKUType"));
                        arraydet.put(jsondet);
                    }
                    jsonDetails.put("Detail", arraydet);
                }
                sendJSon = jsonMaster + "~" + jsonDetails;
                // To invoke json web service to create delivery
                responseJSON = common.invokeJSONWS(sendJSon, "json",
                        "InsertOutletStockConversion", common.url);

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
                        db.close();
                        //</editor-fold>
                        common.showToast(lang.equalsIgnoreCase("hi") ? "स्टॉक कनवर्ज़न सफलतापूर्वक सहेजा गया" : "Stock Conversion saved successfully.");
                        Intent i = new Intent(StockAdjustmentList.this, ActivityHomeScreen.class);
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
