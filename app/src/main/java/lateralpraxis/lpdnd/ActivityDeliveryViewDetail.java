package lateralpraxis.lpdnd;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import lateralpraxis.lpdnd.types.CustomerPayment;

@SuppressLint("InflateParams")
public class ActivityDeliveryViewDetail extends Activity implements Runnable {
    /*Start of code for printer*/
    private static final int REQUEST_CONNECT_DEVICE = 111;
    private static final int REQUEST_ENABLE_BT = 222;
    final Context context = this;
    Button mScan, mPrint, mDisc;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    Common common;
    CustomAdapter Cadapter;
    UserSessionManager session;
    double totalQty = 0, totalAmounts = 0;
    /*End of code for printer*/
    Button buttonLeft, buttonRight;
    File file;
    long different = 0;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private Cursor cursor;
    private DatabaseAdapter dba;
    private String id = "0", header, first = "", userName = "";
    private ArrayList<HashMap<String, String>> HeaderDetails;
    private ListView listViewMain;
    private TextView tvNoRecord, tvTotalAmount, tvHeader;
    private MainAdapter ListAdapter;
    private int cnt = 0;
    private int lsize = 0;
    /*Start of code to declare variables*/
    private ArrayList<HashMap<String, String>> PaymentDetails;
    private TextView tvEmpty, tvTotalAmt, tvHeaderPayment;
    private TableLayout tableDataHeader, tableLayoutTotal;
    private ListView lvPaymentDetails;
    private SimpleDateFormat dateFormat_database;
    private String lang;
    /*End of code to declare variables*/
    /*Start of variable declaration for displaying image*/
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(ActivityDeliveryViewDetail.this, "Connected.", Toast.LENGTH_SHORT).show();
        }
    };

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }

    /*End of variable declaration for displaying image*/
    //Code to be executed on page load
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_delivery_view_detail);

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

// To read user role from user session manager
        final HashMap<String, String> user = session.getLoginUserDetails();
        userName = user.get(UserSessionManager.KEY_USERNAME);

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //To create instance of date format
        dateFormat_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        //To create instance of database and control
        dba = new DatabaseAdapter(this);
        HeaderDetails = new ArrayList<HashMap<String, String>>();
        listViewMain = (ListView) findViewById(R.id.listViewMain);
        tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        tvHeaderPayment = (TextView) findViewById(R.id.tvHeaderPayment);

        //Hash Map for storing data
        PaymentDetails = new ArrayList<HashMap<String, String>>();
        //Code to find layouts
        tableDataHeader = (TableLayout) findViewById(R.id.tableDataHeader);
        tableLayoutTotal = (TableLayout) findViewById(R.id.tableLayoutTotal);
        lvPaymentDetails = (ListView) findViewById(R.id.lvPaymentDetails);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        tvTotalAmt = (TextView) findViewById(R.id.tvTotalAmt);
        buttonLeft = (Button) findViewById(R.id.ButtonLeft);
        buttonRight = (Button) findViewById(R.id.ButtonRight);

        //To extract id from bundle to show details
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("Id");
            first = extras.getString("First");
            header = extras.getString("Header");
        }

//<editor-fold desc="Bluetooth">
        setResult(Activity.RESULT_CANCELED);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            if (mPairedDevices.size() > 0) {
                for (BluetoothDevice mDevice : mPairedDevices) {
                    mPairedDevicesArrayAdapter.add(mDevice.getName() + "\n" + mDevice.getAddress());
                    Log.i("LPDND", mDevice.getName() + "\n" + mDevice.getAddress());
                }
            } else {
                String mNoDevices = "None Paired";//getResources().getText(R.string.none_paired).toString();
                mPairedDevicesArrayAdapter.add(mNoDevices);
            }

            String btAddress = "0";
            dba.openR();
            btAddress = dba.getBluetooth();
            if (!btAddress.equalsIgnoreCase("0")) {
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(btAddress);
                mBluetoothConnectProgressDialog = ProgressDialog.show(ActivityDeliveryViewDetail.this,
                        "Connecting...", mBluetoothDevice.getName() + " : "
                                + mBluetoothDevice.getAddress(), true, true);
                Thread mBlutoothConnectThread = new Thread(ActivityDeliveryViewDetail.this);
                mBlutoothConnectThread.start();
            }
        }
        //</editor-fold>

        //<editor-fold desc="mPrint">
        mPrint = (Button) findViewById(R.id.mPrint);
        mPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                String btAddress = "0";
                dba.openR();
                btAddress = dba.getBluetooth();
                if (btAddress.equalsIgnoreCase("0"))
                    common.showToast("Alert! Set Printer from menu!", 30000);
                else {
                    Thread t = new Thread() {
                        public void run() {
                            try {
                                NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00");
                                OutputStream os = mBluetoothSocket.getOutputStream();

                                String BILL = "                                  ";
                                BILL = BILL + "\n   SHRI GANESH MILK PRODUCTS    ";
                                BILL = BILL + "\n   Plot No. 109. Sector 1A,     ";
                                BILL = BILL + "\n Timber Market, Kopar Khairane, ";
                                BILL = BILL + "\n     Maharashtra - 400709       ";
                                BILL = BILL + "\n   GSTIN/UIN: 27ABGFS3890M2ZG   ";
                                BILL = BILL + "\nTel:27548367/27548369/8080166166";
                                BILL = BILL + "\n--------------------------------";


                                String deliveryDate = "";
                                dba.open();
                                deliveryDate = dba.getDemandDate();
                                final String custNames =dba.printCustomerNameById(id);
                                dba.close();

                                BILL = BILL + String.format("\n%-32s", custNames);
                                //BILL = BILL + String.format("\n%32s\n", common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", deliveryDate));
                                BILL = BILL + String.format("\n%32s\n",  deliveryDate);


                                //To get delivery details from database
                                dba.open();
                                ArrayList<HashMap<String, String>> lables = null;
                                String total = "0";

                                lables = dba.getDeliveryDetails(id, deliveryDate);
                                total = dba.getDeliveryDetailPrinter(id, deliveryDate);
                                if (lables != null && lables.size() > 0) {
                                    BILL = BILL + String.format("%-10s%8s%6s%8s", "Product", "Quantity", "Rate", "Amount");
                                    for (HashMap<String, String> lable : lables) {
                                        //totalAmounts =totalAmounts+ Double.valueOf(lable.get("DelQty"))*Double.valueOf(lable.get("Rate").replace(",", ""));
                                        BILL = BILL + String.valueOf(lable.get("Item")) + "\n";
                                        BILL = BILL + String.format("%8s%11s%13s", String.valueOf(lable.get("DelQty")), String.valueOf(formatter.format(Float.valueOf(lable.get("Rate")))), String.valueOf(formatter.format(Float.valueOf(lable.get("Amount")))));
                                    }
                                    if (Float.valueOf(total) > 0) {
                                        BILL = BILL + String.format("\n%-15s%17s", "Total(In Rs.): ", String.valueOf(formatter.format(Float.valueOf(total)))) + "\n";
                                    }
                                }
                                ////////// Payment /////////
                                List<CustomerPayment> lablePayment = null;
                                lablePayment = dba.getCustomerPaymentForDeliveryPrinter(id, deliveryDate);
                                if (lablePayment.size() > 0) {
                                    BILL = BILL + String.format("\n%-32s", "Payment Details");
                                    BILL = BILL + String.format("\n%-7s%11s  %-12s", "Company", "Amount", "Description");
                                    String comp="", cheque="";
//Looping through hash map and add data to printer
                                    for (int i = 0; i < lablePayment.size(); i++) {
                                        if(!lablePayment.get(i).getCompanyName().equalsIgnoreCase(comp)) {
                                            comp = lablePayment.get(i).getCompanyName();
                                            BILL = BILL + String.format("\n%-7s%11s  %-6s%-6s", comp, String.valueOf(formatter.format(Float.valueOf(lablePayment.get(i).getAmount()))), lablePayment.get(i).getBankName() != null ? lablePayment.get(i).getBankName():lablePayment.get(i).getChequeNumber(), lablePayment.get(i).getBankName() != null ? lablePayment.get(i).getChequeNumber():"");
                                        }
                                        else
                                            BILL = BILL + String.format("\n%-7s%11s  %-6s%-6s", "", String.valueOf(formatter.format(Float.valueOf(lablePayment.get(i).getAmount()))), lablePayment.get(i).getBankName() != null ? lablePayment.get(i).getBankName():lablePayment.get(i).getChequeNumber(), lablePayment.get(i).getBankName() != null ? lablePayment.get(i).getChequeNumber():"");
                                    }
                                }
                                ////// End of Payment //////
                                dba.close();
                                BILL = BILL + "--------------------------------";
                                BILL = BILL + String.format("\n%32s\n", userName);
                                BILL = BILL + "\n\n\n";
                                os.write(BILL.getBytes());
                                os.write(BILL.getBytes());
                            } catch (Exception e) {
                                //Log.e("Main", "Exe ", e);
                            }
                        }
                    };
                    t.start();
                }
            }
        });
        //</editor-fold>


        //To hide right arrow
        try {
            dba.open();
            Date date1 = dateFormat_database.parse(dba.getDemandDate());
            Date date2 = dateFormat_database.parse(dba.getDate());
            dba.close();
            Calendar c = Calendar.getInstance();
            c.setTime(date2);
            c.add(Calendar.DATE, -1);

            different = c.getTimeInMillis() - date1.getTime();
            Log.i("different", String.valueOf(different));

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (different < 0)
            buttonRight.setVisibility(View.GONE);
        else
            buttonRight.setVisibility(View.VISIBLE);

        DataBind();

        //<editor-fold desc="Event hander of left arrow demand button">
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    if (mBluetoothSocket != null)
                        mBluetoothSocket.close();
                } catch (Exception e) {
                    Log.e("Tag", "Exe ", e);
                }
                dba.open();
                String demand = dba.getDemandDate();
                dba.close();

                //To bind default demand date
                final Calendar c = Calendar.getInstance();
                try {
                    c.setTime(dateFormat_database.parse(demand));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                c.add(Calendar.DATE, -1);
                //tvHeader.setText(dateFormat_display.format(c.getTime()));
                String demandDate = dateFormat_database.format(c.getTime());
                dba.open();
                dba.Update_DemandDate(demandDate);
                dba.close();

                Intent myIntent = new Intent(ActivityDeliveryViewDetail.this, ActivityDeliveryViewDetail.class);
                myIntent.putExtra("Id", id);
                myIntent.putExtra("Header", header);
                startActivity(myIntent);
                finish();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Event hander of right arrow demand button">
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    if (mBluetoothSocket != null)
                        mBluetoothSocket.close();
                } catch (Exception e) {
                    Log.e("Tag", "Exe ", e);
                }
                dba.open();
                String demand = dba.getDemandDate();
                dba.close();

                //To bind default demand date
                final Calendar c = Calendar.getInstance();
                try {
                    c.setTime(dateFormat_database.parse(demand));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                c.add(Calendar.DATE, 1);
                String demandDate = dateFormat_database.format(c.getTime());
                dba.open();
                dba.Update_DemandDate(demandDate);
                dba.close();
                Intent myIntent = new Intent(ActivityDeliveryViewDetail.this, ActivityDeliveryViewDetail.class);
                myIntent.putExtra("Id", id);
                myIntent.putExtra("Header", header);
                startActivity(myIntent);
                finish();
            }
        });
        //</editor-fold>

    }

    //<editor-fold desc="Data Bind">
    private void DataBind() {
        String deliveryDate = "";
        dba.open();
        deliveryDate = dba.getDemandDate();
        tvHeader.setText(Html.fromHtml("<font color=#000000> " + common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", deliveryDate) + " " + header + "</font>"));
        dba.close();

        //To get delivery details from database
        dba.open();
        ArrayList<HashMap<String, String>> lables = null;
        lables = dba.getDeliveryDetails(id, deliveryDate);
        lsize = lables.size();
        if (lables != null && lables.size() > 0) {
            for (HashMap<String, String> lable : lables) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("Item", String.valueOf(lable.get("Item")));
                hm.put("DelQty", String.valueOf(lable.get("DelQty")));
                hm.put("Rate", String.valueOf(lable.get("Rate")));
                hm.put("Amount", String.valueOf(lable.get("Amount")));
                hm.put("DQty", String.valueOf(lable.get("DQty")));
                totalAmounts = totalAmounts + Double.valueOf(lable.get("DelQty")) * Double.valueOf(lable.get("Rate").replace(",", ""));
                HeaderDetails.add(hm);
            }
        }
        dba.close();
        if (lsize == 0) {
            //To display no record message
            mPrint.setVisibility(View.GONE);
            tvNoRecord.setVisibility(View.VISIBLE);
            listViewMain.setVisibility(View.GONE);
        } else {
            //To bind data and display list view
            mPrint.setVisibility(View.VISIBLE);
            tvNoRecord.setVisibility(View.GONE);
            listViewMain.setVisibility(View.VISIBLE);
            ListAdapter = new MainAdapter(ActivityDeliveryViewDetail.this);
            listViewMain.setAdapter(ListAdapter);

            if (lang.equalsIgnoreCase("hi"))
                tvTotalAmount.setText(Html.fromHtml("<b>कुल: " + common.stringToTwoDecimal(String.valueOf(totalAmounts)) + "</b>"));
            else
                tvTotalAmount.setText(Html.fromHtml("<b>Total: " + common.stringToTwoDecimal(String.valueOf(totalAmounts)) + "</b>"));
        }
        /*Start of code to bind data from temporary table*/
        PaymentDetails.clear();
        dba.open();
        List<CustomerPayment> lablePayment = null;
        lablePayment = dba.getCustomerPaymentForDelivery(id, deliveryDate);
        lsize = lablePayment.size();
        if (lsize > 0) {
            tvHeaderPayment.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            tableDataHeader.setVisibility(View.VISIBLE);
            tableLayoutTotal.setVisibility(View.VISIBLE);
            //Looping through hash map and add data to hash map
            for (int i = 0; i < lablePayment.size(); i++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("Id", String.valueOf(lablePayment.get(i).getId()));
                hm.put("CompanyName", String.valueOf(lablePayment.get(i).getCompanyName()));
                hm.put("Amount", common.stringToTwoDecimal(lablePayment.get(i).getAmount()));
                hm.put("ChequeNumber", String.valueOf(lablePayment.get(i).getChequeNumber()));
                hm.put("Bank", String.valueOf(lablePayment.get(i).getBankName()));
                hm.put("ImagePath", String.valueOf(lablePayment.get(i).getUniqueId()));
                PaymentDetails.add(hm);
            }
            dba.open();
            String strTotal = String.valueOf(common.stringToTwoDecimal(dba.getCustomerTotalPaymentForDelivery(id, deliveryDate)));
            tvTotalAmt.setText(strTotal);
        } else {
            //Display no records message
            tvHeaderPayment.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            tableDataHeader.setVisibility(View.GONE);
            tableLayoutTotal.setVisibility(View.GONE);
        }
        dba.close();
        //Code to set hash map data in custom adapter
        Cadapter = new CustomAdapter(ActivityDeliveryViewDetail.this, PaymentDetails);
        if (lsize > 0)
            lvPaymentDetails.setAdapter(Cadapter);
        /*End of code to bind data from temporary table*/
    }
    //</editor-fold>

    //When press back button go to home screen
    @Override
    public void onBackPressed() {
        try {
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
        Intent intent;
        intent = new Intent(this, ActivityDeliveryViewSummary.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    //</editor-fold>

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
                try {
                    if (mBluetoothSocket != null)
                        mBluetoothSocket.close();
                } catch (Exception e) {
                    Log.e("Tag", "Exe ", e);
                }
                Intent homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
                homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreenIntent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //<editor-fold desc="on Activity Result">
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle mExtra = data.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    //Log.v(TAG, "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(this,
                            "Connecting...", mBluetoothDevice.getName() + " : "
                                    + mBluetoothDevice.getAddress(), true, false);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();
                }
                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent i = new Intent(ActivityDeliveryViewDetail.this, ActivityDeliveryViewDetail.class);
                    i.putExtra("Id", id);
                    i.putExtra("Header", header);
                    startActivityForResult(i, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(ActivityDeliveryViewDetail.this, "Message", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    //</editor-fold>

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                //Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  "+ mDevice.getAddress());
            }
        }
    }

    public void run() {
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            //Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            //Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            //Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    public byte[] sel(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putInt(val);
        buffer.flip();
        return buffer.array();
    }

    //<editor-fold desc="Custom Adapter for Payment">
    //Class for Binding Data in ListView
    public static class ViewHolder {
        //Control Declaration
        TextView tvCompany, tvAmount, tvCheque, tvDocumentPath;
        Button btnAttach;
    }

    //<editor-fold desc="Main Adapter">
    //To make class of delivery view holder
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
                arg1 = mInflater.inflate(R.layout.activity_delivery_view_detail_item, null);
                holder = new ViewHolder();

                holder.tvItem = (TextView) arg1.findViewById(R.id.tvItem);
                holder.tvDelQty = (TextView) arg1.findViewById(R.id.tvDelQty);
                holder.tvDQty = (TextView) arg1.findViewById(R.id.tvDQty);
                holder.tvRate = (TextView) arg1.findViewById(R.id.tvRate);
                holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
                arg1.setTag(holder);
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            //To bind data into view holder
            holder.tvItem.setText(HeaderDetails.get(arg0).get("Item"));
            if (Double.parseDouble(HeaderDetails.get(arg0).get("DQty")) == 0)
                holder.tvDQty.setText("-");
            else
                holder.tvDQty.setText(HeaderDetails.get(arg0).get("DQty").replace(".0", ""));
            holder.tvDelQty.setText(HeaderDetails.get(arg0).get("DelQty").replace(".0", ""));
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
            TextView tvItem, tvDelQty, tvRate, tvAmount, tvDQty;
        }
    }

    //Declaring Adapter for binding data in List View
    public class CustomAdapter extends BaseAdapter {

        private Context paymentContext;
        private LayoutInflater mInflater;

        //Adapter constructor
        public CustomAdapter(Context context, ArrayList<HashMap<String, String>> lvPaymentDetails) {
            this.paymentContext = context;
            mInflater = LayoutInflater.from(paymentContext);
            PaymentDetails = lvPaymentDetails;
        }

        //Method to return count on data in adapter
        @Override
        public int getCount() {
            return PaymentDetails.size();
        }

        @Override
        public Object getItem(int arg0) {
            return PaymentDetails.get(arg0);
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

        //Event is similar to row data bound event
        @Override
        public View getView(final int arg0, View arg1, ViewGroup arg2) {


            final ViewHolder holder;
            if (arg1 == null) {
                //Code to set layout inside list view
                arg1 = mInflater.inflate(R.layout.list_payment_detail, null);
                holder = new ViewHolder();
                //Code to find controls inside listview
                holder.tvCompany = (TextView) arg1.findViewById(R.id.tvCompany);
                holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
                holder.tvCheque = (TextView) arg1.findViewById(R.id.tvCheque);
                holder.tvDocumentPath = (TextView) arg1.findViewById(R.id.tvDocumentPath);
                holder.btnAttach = (Button) arg1.findViewById(R.id.btnAttach);
                arg1.setTag(holder);

            } else {

                holder = (ViewHolder) arg1.getTag();
            }
            //Code to bind data from hash map in controls
            holder.tvCompany.setText(PaymentDetails.get(arg0).get("CompanyName"));
            holder.tvAmount.setText(PaymentDetails.get(arg0).get("Amount"));
            holder.tvCheque.setText(Html.fromHtml("<font color=#000000> " + PaymentDetails.get(arg0).get("ChequeNumber") + "</font>"));
            holder.tvDocumentPath.setText(PaymentDetails.get(arg0).get("ImagePath"));
            if (TextUtils.isEmpty(PaymentDetails.get(arg0).get("ImagePath").trim()))
                holder.btnAttach.setVisibility(View.GONE);
            else
                holder.btnAttach.setVisibility(View.VISIBLE);
            //Button delete event for deleting attachment
            holder.btnAttach.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    try {

                        String actPath = holder.tvDocumentPath.getText().toString();
                        int pathLen = actPath.split("/").length;
                        //to Get Unique Id
                        String newPath1 = actPath.split("/")[pathLen - 2];
                        String newPath2 = actPath.split("/")[pathLen - 3];

                        String catType = "Cheque";
                        // Check for SD Card
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            common.showToast("Error! No SDCARD Found!");
                        } else {
                            // Locate the image folder in your SD Card
                            file = new File(Environment.getExternalStorageDirectory()
                                    + File.separator + newPath2 + File.separator + newPath1 + File.separator);
                        }

                        if (file.isDirectory()) {

                            listFile = file.listFiles(new FilenameFilter() {
                                public boolean accept(File directory, String fileName) {
                                    return fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
                                }
                            });
                            // Create a String array for FilePathStrings
                            FilePathStrings = new String[listFile.length];
                            // Create a String array for FileNameStrings
                            FileNameStrings = new String[listFile.length];

                            for (int i = 0; i < listFile.length; i++) {
                                FilePathStrings[i] = listFile[i].getAbsolutePath();
                                // Get the name image file
                                FileNameStrings[i] = listFile[i].getName();

                                Intent i1 = new Intent(ActivityDeliveryViewDetail.this, ViewImage.class);
                                // Pass String arrays FilePathStrings
                                i1.putExtra("filepath", FilePathStrings);
                                // Pass String arrays FileNameStrings
                                i1.putExtra("filename", FileNameStrings);
                                // Pass String category type
                                i1.putExtra("categorytype", catType);
                                // Pass click position
                                i1.putExtra("position", 0);
                                startActivity(i1);
                            }
                        }


                    } catch (Exception except) {
                        //except.printStackTrace();
                        common.showAlert(ActivityDeliveryViewDetail.this, "Error: " + except.getMessage(), false);

                    }

                }
            });

            //Code to check if row is even or odd and set set color for alternate rows
			/*if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/

            return arg1;
        }

    }

}
