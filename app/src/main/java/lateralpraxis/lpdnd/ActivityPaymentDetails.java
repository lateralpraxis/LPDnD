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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lateralpraxis.lpdnd.types.CustomerPayment;

@SuppressLint("InflateParams")
public class ActivityPaymentDetails extends Activity implements Runnable{
	/*Start of code for printer*/
	private static final int REQUEST_CONNECT_DEVICE = 111;
	private static final int REQUEST_ENABLE_BT = 222;
	final Context context = this;
	Button mScan, mPrint, mDisc;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice mBluetoothDevice;
	private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ProgressDialog mBluetoothConnectProgressDialog;
	private BluetoothSocket mBluetoothSocket;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	/*End of code for printer*/
	/*Start of code to declare controls*/
	private TextView tvDataHead,tvEmpty,tvTotalAmt;
	private TableLayout tableDataHeader,tableLayoutTotal;
	private ListView lvPaymentDetails;
	/*End of code to declare controls*/

	/*Start of code to declare class*/
	UserSessionManager session;
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	/*End of code to declare class*/

	/*Start of code to declare variables*/
	private ArrayList<HashMap<String, String>> PaymentDetails;
	private int lsize=0;
	private String strCustId="";
	private String strCustName="", userName = "";
	private String strDate="";
	/*End of code to declare variables*/

	/*Start of variable declaration for displaying image*/
	private File[] listFile;
	File file;
	private String[] FilePathStrings;
	private String[] FileNameStrings;
	/*End of variable declaration for displaying image*/

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mBluetoothConnectProgressDialog.dismiss();
			Toast.makeText(ActivityPaymentDetails.this, "Connected.", Toast.LENGTH_SHORT).show();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		//Code to set layout
		setContentView(R.layout.activity_payment_detail);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of user session
		session = new UserSessionManager(getApplicationContext());

		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);

		// To read user role from user session manager
		final HashMap<String, String> user = session.getLoginUserDetails();
		userName = user.get(UserSessionManager.KEY_USERNAME);

		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			strCustId=extras.getString("customerId");
			strCustName=extras.getString("customerName");
			strDate=extras.getString("date");
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
			db.openR();
			btAddress = db.getBluetooth();
			if (!btAddress.equalsIgnoreCase("0")) {
				mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(btAddress);
				mBluetoothConnectProgressDialog = ProgressDialog.show(ActivityPaymentDetails.this,
						"Connecting...", mBluetoothDevice.getName() + " : "
								+ mBluetoothDevice.getAddress(), true, true);
				Thread mBlutoothConnectThread = new Thread(ActivityPaymentDetails.this);
				mBlutoothConnectThread.start();
			}
		}
		//</editor-fold>

		//<editor-fold desc="mPrint">
		mPrint = (Button) findViewById(R.id.mPrint);
		mPrint.setOnClickListener(new View.OnClickListener() {
			public void onClick(View mView) {
				String btAddress = "0";
				db.openR();
				btAddress = db.getBluetooth();
				if (btAddress.equalsIgnoreCase("0"))
					common.showToast("Alert! Set Printer from menu!", 30000);
				else {
					Thread t = new Thread() {
						public void run() {
							try {
								NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00");
								OutputStream os = mBluetoothSocket.getOutputStream();
								db.openR();
								final String custNames =db.printCustomerNameById(strCustId);
								String BILL = "                                  ";
								BILL = BILL + "\n   SHRI GANESH MILK PRODUCTS    ";
								BILL = BILL + "\n   Plot No. 109. Sector 1A,     ";
								BILL = BILL + "\n Timber Market, Kopar Khairane, ";
								BILL = BILL + "\n     Maharashtra - 400709       ";
								BILL = BILL + "\n   GSTIN/UIN: 27ABGFS3890M2ZG   ";
								BILL = BILL + "\nTel:27548367/27548369/8080166166";
								BILL = BILL + "\n--------------------------------";

								BILL = BILL + String.format("\n%-32s", custNames);
								BILL = BILL + String.format("\n%32s", common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", strDate));

								////////// Payment /////////
								List<CustomerPayment> lablePayment = null;
								lablePayment = db.getCustomerPaymentPrinter(strCustId,strDate);
								if (lablePayment.size() > 0) {
									//BILL = BILL + String.format("\n%-32s", "Payment Details");
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
								db.close();
								BILL = BILL + "--------------------------------";
								BILL = BILL + String.format("\n%32s\n", userName);
								BILL = BILL + "\n\n\n";
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

		//Hash Map for storing data
		PaymentDetails = new ArrayList<HashMap<String, String>>();
		//Code to find layouts
		tableDataHeader=(TableLayout) findViewById(R.id.tableDataHeader);
		tableLayoutTotal=(TableLayout) findViewById(R.id.tableLayoutTotal);
		lvPaymentDetails =(ListView)findViewById(R.id.lvPaymentDetails);
		tvEmpty= (TextView) findViewById(R.id.tvEmpty);
		tvDataHead= (TextView) findViewById(R.id.tvDataHead);
		tvTotalAmt= (TextView) findViewById(R.id.tvTotalAmt);

		tvDataHead.setText(Html.fromHtml("<font color=#000000> "+strCustName+" , "+common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", strDate)+"</font>"));
		/*Start of code to bind data from temporary table*/
		
		PaymentDetails.clear();
		db.open();		
		List <CustomerPayment> lables = db.getCustomerPayment(strCustId,strDate);
		lsize = lables.size();
		if(lsize>0)
		{
			mPrint.setVisibility(View.VISIBLE);
			tvEmpty.setVisibility(View.GONE);
			tableDataHeader.setVisibility(View.VISIBLE);
			tableLayoutTotal.setVisibility(View.VISIBLE);
			//Looping through hash map and add data to hash map
			for(int i=0;i<lables.size();i++){
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Id", String.valueOf(lables.get(i).getId())); 
				hm.put("CompanyName", String.valueOf(lables.get(i).getCompanyName())); 
				hm.put("Amount", common.stringToTwoDecimal(lables.get(i).getAmount()));  
				hm.put("ChequeNumber", String.valueOf(lables.get(i).getChequeNumber()));
				hm.put("Bank", String.valueOf(lables.get(i).getBankName()));
				hm.put("ImagePath", String.valueOf(lables.get(i).getUniqueId()));
				PaymentDetails.add(hm);
			}
			db.open();
			String strTotal =String.valueOf(common.stringToTwoDecimal(db.getCustomerTotalPayment(strCustId,strDate)));
			tvTotalAmt.setText(strTotal);
		}
		else
		{
			//Display no records message
			mPrint.setVisibility(View.GONE);
			tvEmpty.setVisibility(View.VISIBLE);
			tableDataHeader.setVisibility(View.GONE);
			tableLayoutTotal.setVisibility(View.GONE);
		}
		db.close();
		//Code to set hash map data in custom adapter
		Cadapter = new CustomAdapter(ActivityPaymentDetails.this,PaymentDetails);
		if(lsize>0)
			lvPaymentDetails.setAdapter(Cadapter);	
		/*End of code to bind data from temporary table*/
	}

	//Class for Binding Data in ListView
	public static class ViewHolder {
		//Control Declaration
		TextView tvCompany,tvAmount,tvCheque,tvDocumentPath; 

		Button btnAttach;
	}
	//Declaring Adapter for binding data in List View
	public class CustomAdapter extends BaseAdapter {
		private Context paymentContext;
		private LayoutInflater mInflater;
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
		//Adapter constructor
		public CustomAdapter(Context context,ArrayList<HashMap<String, String>> lvPaymentDetails) {
			this.paymentContext = context;
			mInflater = LayoutInflater.from(paymentContext);
			PaymentDetails = lvPaymentDetails;
		}

		//Event is similar to row data bound event
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {			


			final ViewHolder holder;
			if (arg1 == null) 
			{
				//Code to set layout inside list view
				arg1 = mInflater.inflate(R.layout.list_payment_detail, null); 
				holder = new ViewHolder();
				//Code to find controls inside listview
				holder.tvCompany = (TextView)arg1.findViewById(R.id.tvCompany);
				holder.tvAmount = (TextView)arg1.findViewById(R.id.tvAmount);
				holder.tvCheque = (TextView)arg1.findViewById(R.id.tvCheque);
				holder.tvDocumentPath= (TextView)arg1.findViewById(R.id.tvDocumentPath);
				holder.btnAttach = (Button)arg1.findViewById(R.id.btnAttach);
				arg1.setTag(holder);

			}
			else
			{

				holder = (ViewHolder) arg1.getTag();
			}
			//Code to bind data from hash map in controls
			holder.tvCompany.setText(PaymentDetails.get(arg0).get("CompanyName"));
			holder.tvAmount.setText(PaymentDetails.get(arg0).get("Amount"));
			holder.tvCheque.setText(Html.fromHtml("<font color=#000000> "+PaymentDetails.get(arg0).get("ChequeNumber")+"</font>") );
			holder.tvDocumentPath.setText(PaymentDetails.get(arg0).get("ImagePath"));
			if(TextUtils.isEmpty(PaymentDetails.get(arg0).get("ImagePath").trim()) )
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
						String newPath1 = actPath.split("/")[pathLen-2];
						String newPath2 = actPath.split("/")[pathLen-3];

						String catType = "Cheque";
						// Check for SD Card
						if (!Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							common.showToast("Error! No SDCARD Found!");
						} else {
							// Locate the image folder in your SD Card
							file = new File(Environment.getExternalStorageDirectory()
									+  File.separator+newPath2+File.separator+newPath1+File.separator);
						}

						if (file.isDirectory()) {

							listFile = file.listFiles(new FilenameFilter() {
								public boolean accept(File directory, String fileName) {
									return fileName.endsWith(".jpeg")|| fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
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

								Intent i1 = new Intent(ActivityPaymentDetails.this, ViewImage.class);
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
						common.showAlert(ActivityPaymentDetails.this,"Error: "+except.getMessage(),false);

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

	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			try {
				if (mBluetoothSocket != null)
					mBluetoothSocket.close();
			} catch (Exception e) {
				Log.e("Tag", "Exe ", e);
			}
			Intent i = new Intent(ActivityPaymentDetails.this,ActivityPaymentView.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(i);
			finish();
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

	//Event Triggered on Clicking Back
	@Override
	public void onBackPressed() {
		try {
			if (mBluetoothSocket != null)
				mBluetoothSocket.close();
		} catch (Exception e) {
			Log.e("Tag", "Exe ", e);
		}
		Intent i = new Intent(ActivityPaymentDetails.this,ActivityPaymentView.class);
		startActivity(i);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}

	//To create menu on inflater
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		return true;
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
					Intent i = new Intent(ActivityPaymentDetails.this, ActivityPaymentDetails.class);
					i.putExtra("customerId", strCustId);
					i.putExtra("customerName", strCustName);
					i.putExtra("date", strDate);
					startActivityForResult(i, REQUEST_CONNECT_DEVICE);
				} else {
					Toast.makeText(ActivityPaymentDetails.this, "Message", Toast.LENGTH_LONG).show();
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

}
