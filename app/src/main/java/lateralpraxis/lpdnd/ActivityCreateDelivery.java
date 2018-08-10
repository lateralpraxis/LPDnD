package lateralpraxis.lpdnd;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import lateralpraxis.lpdnd.types.CustomType;

@SuppressLint("InflateParams") public class ActivityCreateDelivery extends ListActivity implements Runnable{
	private Button btnGo, btnCreate,btnPayment;
	private Common common;
	UserSessionManager session;
	private DatabaseAdapter dba;
	private String[] arrTemp;
	private TextView tvNoRecord, tvTotalAmount;
	private Spinner spCustomer;
	private String userId, userRole, userName = "", routeId, centreId, customerRouteId, customerWithRoute, customerId="0", customerRoute, vehicleId="0";
	double totalQty = 0;
	private String lang;
	private RadioGroup radioGroupRoute;
	final Context context = this;
	private int idx;

	/*Start of code for printer*/
	private static final int REQUEST_CONNECT_DEVICE = 111;
	private static final int REQUEST_ENABLE_BT = 222;
	Button mScan, mPrint, mDisc;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice mBluetoothDevice;
	private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ProgressDialog mBluetoothConnectProgressDialog;
	private BluetoothSocket mBluetoothSocket;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	String btAddress = "0", today="";
	ArrayList<HashMap<String, String>> lables = null;
	String total = "0";
	/*End of code for printer*/

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mBluetoothConnectProgressDialog.dismiss();
			Toast.makeText(ActivityCreateDelivery.this, "Connected", Toast.LENGTH_SHORT).show();
		}
	};

	// Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_delivery);

		common = new Common(getApplicationContext());
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		// To create instance of user session
		session = new UserSessionManager(getApplicationContext());

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);

		// To read user role from user session manager
		final HashMap<String, String> user = session.getLoginUserDetails();
		centreId = user.get(UserSessionManager.KEY_CENTREID);
		userId = user.get(UserSessionManager.KEY_ID);
		userName = user.get(UserSessionManager.KEY_USERNAME);
		userRole = user.get(UserSessionManager.KEY_ROLES);
		routeId = user.get(UserSessionManager.KEY_ROUTEID);
		vehicleId = user.get(UserSessionManager.KEY_VEHICLEID);
		String[] arr = routeId.split(",");
		if(arr.length>1)
		{
			routeId = arr[0];
		}
		Log.i("LPDND", "routeId="+routeId);
		// to create object of controls
		btnGo = (Button) findViewById(R.id.btnGo);
		btnCreate = (Button) findViewById(R.id.btnCreate);
		btnPayment = (Button) findViewById(R.id.btnPayment);
		tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		//tvVehicle = (TextView) findViewById(R.id.tvVehicle);

		// To create instance of database
		dba = new DatabaseAdapter(this);
		dba.open();

		// To create instance of control used in page
		spCustomer = (Spinner) findViewById(R.id.spCustomer);
		//spVehicle = (Spinner) findViewById(R.id.spVehicle);
		//spRoute = (Spinner) findViewById(R.id.spRoute);

		radioGroupRoute = (RadioGroup) findViewById(R.id.radioGroupRoute);

		//<editor-fold desc="Bluetooth">
		setResult(Activity.RESULT_CANCELED);
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(ActivityCreateDelivery.this, R.layout.device_name);

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
				mBluetoothConnectProgressDialog = ProgressDialog.show(ActivityCreateDelivery.this,
						"Connecting...", mBluetoothDevice.getName() + " : "
								+ mBluetoothDevice.getAddress(), true, true);
				Thread mBlutoothConnectThread = new Thread(ActivityCreateDelivery.this);
				mBlutoothConnectThread.start();
			}
		}
		//</editor-fold>

		BindCustomer();

		radioGroupRoute.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton rb = (RadioButton) group.findViewById(checkedId);
				if (null != rb && checkedId > -1) {
					setListAdapter(null);
					customerId="0";
					BindCustomer();
				}
			}
		});

		// To clean list adaptor, no records found and total amount
		spCustomer.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				setListAdapter(null);
				btnCreate.setVisibility(View.GONE);
				btnPayment.setVisibility(View.GONE);
				tvNoRecord.setVisibility(View.GONE);
				tvTotalAmount.setText("");
				customerWithRoute = ((CustomType) spCustomer.getSelectedItem()).getId();
				String[] arr = customerWithRoute.split("~");
				if(arr.length>1)
				{
					customerId = arr[0];
					customerRouteId = arr[1];
					customerRoute = arr[2];
				}
				else
				{
					customerId = customerRouteId = "0";
					customerRoute ="";
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		// Event hander of go button
		btnGo.setOnClickListener(new View.OnClickListener() {
			// When go button click
			@Override
			public void onClick(View arg0) {
				tvTotalAmount.setText("");
				// To validate required field
				if ((customerId == "0" && userRole.equalsIgnoreCase("Route Officer")) && lang.equalsIgnoreCase("en"))
					common.showToast("Customer is mandatory.");
				else if ((customerId == "0" && userRole.equalsIgnoreCase("Route Officer")) && lang.equalsIgnoreCase("hi"))
					common.showToast("ग्राहक अनिवार्य है।");
				else {
					GetDeliveryInput(customerId);
				}
			}
		});

		// Event hander to create payment button
		btnPayment.setOnClickListener(new View.OnClickListener() {
			// When create button click
			@Override
			public void onClick(View arg0) {

				// To validate required field and please enter at least one
				// quantity!
				int zeroCount = 0, totalRow = 0;
				int invalidCount = 0;
				for (int i = 0; i < getListView().getChildCount(); i++) {
					totalRow++;
					View v = getListView().getChildAt(i);
					EditText etQty = (EditText) v.findViewById(R.id.etDelivery);
					if (etQty.getText().toString().equalsIgnoreCase("."))
						invalidCount=invalidCount+1;
					if (!etQty.getText().toString().equalsIgnoreCase(".")) {
						String qty = etQty.length() == 0 ? "0" : etQty
								.getText().toString().trim();
						if (etQty.length() == 0 || Double.parseDouble(qty) == 0)
							zeroCount++;
					}
				}
				if(invalidCount>0 && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCreateDelivery.this, "Please enter valid quantity!", false);
				else if(invalidCount>0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCreateDelivery.this, "कृपया वैध मात्रा दर्ज करें!", false);

				else if (totalRow == zeroCount && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCreateDelivery.this, "Please enter atleast one quantity!", false);
				else if (totalRow == zeroCount && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCreateDelivery.this, "कृपया कम से कम एक मात्रा दर्ज करें!", false);

				else if ((customerId.equalsIgnoreCase("0") && userRole.equalsIgnoreCase("Route Officer")) && lang.equalsIgnoreCase("en"))
					common.showToast("Customer is mandatory!");
				else if ((customerId.equalsIgnoreCase("0") && userRole.equalsIgnoreCase("Route Officer")) && lang.equalsIgnoreCase("hi"))
					common.showToast("ग्राहक अनिवार्य है!");
				else {

					// Confirmation message before submit data
					Builder alertDialogBuilder = new Builder(context);
					alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
					alertDialogBuilder
							.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to submit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int id) {
									if (common.isConnected()) {
										try {
											// To store data in delivery table
											dba.open();
											String insertDelId = dba.Insert_Delivery(dba.uniqueId(common.getIMEI()), routeId, centreId, customerRoute, customerId, ((CustomType) spCustomer.getSelectedItem()).getName(), userId, common.getIMEI(), vehicleId, String.valueOf(idx));
											if(insertDelId.contains("success"))
											{
												for(int i =0;i<getListView().getChildCount();i++)
												{
													View v = getListView().getChildAt(i);
													TextView tvId = (TextView)v.findViewById(R.id.tvId);
													TextView tvFactorId = (TextView)v.findViewById(R.id.tvFactorId);
													TextView tvItem = (TextView)v.findViewById(R.id.tvSku);
													TextView tvRate = (TextView)v.findViewById(R.id.tvRate);
													TextView tvAvailable = (TextView)v.findViewById(R.id.tvAvailable);
													TextView tvDemand = (TextView)v.findViewById(R.id.tvDemand);
													EditText etQty = (EditText)v.findViewById(R.id.etDelivery);
													TextView tvCompanyId = (TextView)v.findViewById(R.id.tvCompanyId);
													//To validate if user enter only .
													if(!etQty.getText().toString().equalsIgnoreCase("."))
													{
														String qty = etQty.getText().toString().trim().length() == 0? "0" :String.valueOf(Double.valueOf(etQty.getText().toString().trim())*Double.valueOf(tvFactorId.getText().toString().trim()));
														if(Double.parseDouble(qty)!=0)
														{
															dba.Insert_DeliveryDetail(insertDelId.split("~")[1], tvId.getText().toString(), tvItem.getText().toString(), tvRate.getText().toString().replace(",", ""), tvDemand.getText().toString().replace("-", "0"), tvAvailable.getText().toString(), qty, customerId, tvCompanyId.getText().toString(), routeId);
														}
													}
												}

												setListAdapter(null);
												btnCreate.setVisibility(View.GONE);
												btnPayment.setVisibility(View.GONE);
												tvNoRecord.setVisibility(View.GONE);
												tvTotalAmount.setText("");
												dba.close();

												common.showToast(lang.equalsIgnoreCase("hi")?"डिलिवरी विवरण सफलतापूर्वक जोड़ा गया।":"Delivery saved successfully.");


												SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
												//To bind default delivery date
												final Calendar c = Calendar.getInstance();
												c.add(Calendar.DATE,0);
												String deliveryDate = dateFormatter.format(c.getTime());
												dba.open();
												dba.Insert_DemandDate(deliveryDate);
												dba.close();

												try {
													if (mBluetoothSocket != null)
														mBluetoothSocket.close();
												} catch (Exception e) {
													Log.e("Tag", "Exe ", e);
												}
												//To open payment collection page
												Intent	intent = new Intent(context, ActivityDeliveryPayment.class);
												intent.putExtra("customerId", customerId);
												intent.putExtra("customer", ((CustomType) spCustomer.getSelectedItem()).getName());
												intent.putExtra("deliveryUniqueId", insertDelId.split("~")[2]);
												intent.putExtra("paymentCount", "0");
												intent.putExtra("from", "delivery");
												startActivity(intent);
												finish();
											}


										} catch (Exception e) {
											e.printStackTrace();
											common.showAlert(ActivityCreateDelivery.this, "Error in Saving delivery.\n"+e.getMessage(), false);
										}
									}
									else{
										dba.insertExceptions("Unable to connect to Internet !", "ActivityCreatedelivery.java","onCreate()");
									}
								}
							}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					alertDialogBuilder.create().show();
				}
			}
		});

		// Event hander to create delivery button
		btnCreate.setOnClickListener(new View.OnClickListener() {
			// When create button click
			@Override
			public void onClick(View arg0) {

				// To validate required field and please enter at least one
				// quantity!
				int zeroCount = 0, totalRow = 0;
				int invalidCount = 0;
				for (int i = 0; i < getListView().getChildCount(); i++) {
					totalRow++;
					View v = getListView().getChildAt(i);
					EditText etQty = (EditText) v.findViewById(R.id.etDelivery);
					if (etQty.getText().toString().equalsIgnoreCase("."))
						invalidCount=invalidCount+1;
					if (!etQty.getText().toString().equalsIgnoreCase(".")) {
						String qty = etQty.length() == 0 ? "0" : etQty
								.getText().toString().trim();
						if (etQty.length() == 0 || Double.parseDouble(qty) == 0)
							zeroCount++;
					}
				}
				if(invalidCount>0 && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCreateDelivery.this, "Please enter valid quantity!", false);
				else if(invalidCount>0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCreateDelivery.this, "कृपया वैध मात्रा दर्ज करें!", false);

				else if (totalRow == zeroCount && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCreateDelivery.this, "Please enter atleast one quantity!", false);
				else if (totalRow == zeroCount && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCreateDelivery.this, "कृपया कम से कम एक मात्रा दर्ज करें!", false);

				else if ((customerId.equalsIgnoreCase("0") && userRole.equalsIgnoreCase("Route Officer")) && lang.equalsIgnoreCase("en"))
					common.showToast("Customer is mandatory.");
				else if ((customerId.equalsIgnoreCase("0") && userRole.equalsIgnoreCase("Route Officer")) && lang.equalsIgnoreCase("hi"))
					common.showToast("ग्राहक अनिवार्य है।");
				else {
					// Confirmation message before submit data
					Builder alertDialogBuilder = new Builder(context);
					alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
					alertDialogBuilder
							.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to submit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int id) {
									if (common.isConnected()) {
										try {
											// To store data in delivery table

											final String unicId = UUID.randomUUID().toString();
											dba.open();
											String insertDelId = dba.Insert_Delivery(unicId, routeId, centreId, customerRoute, customerId, ((CustomType) spCustomer.getSelectedItem()).getName(), userId, common.getIMEI(), vehicleId, String.valueOf(idx));
											dba.close();

											if(insertDelId.contains("success"))
											{
												for(int i =0;i<getListView().getChildCount();i++)
												{
													View v = getListView().getChildAt(i);
													TextView tvId = (TextView)v.findViewById(R.id.tvId);
													TextView tvFactorId = (TextView)v.findViewById(R.id.tvFactorId);
													TextView tvItem = (TextView)v.findViewById(R.id.tvSku);
													TextView tvRate = (TextView)v.findViewById(R.id.tvRate);
													TextView tvAvailable = (TextView)v.findViewById(R.id.tvAvailable);
													TextView tvDemand = (TextView)v.findViewById(R.id.tvDemand);
													EditText etQty = (EditText)v.findViewById(R.id.etDelivery);
													TextView tvCompanyId = (TextView)v.findViewById(R.id.tvCompanyId);
													//To validate if user enter only .
													if(!etQty.getText().toString().equalsIgnoreCase("."))
													{
														String qty = etQty.getText().toString().trim().length() == 0? "0" :String.valueOf(Double.valueOf(etQty.getText().toString().trim())*Double.valueOf(tvFactorId.getText().toString().trim()));
														if(Double.parseDouble(qty)!=0)
														{
															dba.open();
															dba.Insert_DeliveryDetail(insertDelId.split("~")[1], tvId.getText().toString(), tvItem.getText().toString(), tvRate.getText().toString().replace(",", ""), tvDemand.getText().toString().replace("-", "0"), tvAvailable.getText().toString(), qty, customerId, tvCompanyId.getText().toString(), routeId);
															dba.close();
														}
													}
												}

												setListAdapter(null);
												btnCreate.setVisibility(View.GONE);
												btnPayment.setVisibility(View.GONE);
												tvNoRecord.setVisibility(View.GONE);
												tvTotalAmount.setText("");
												common.showToast("Delivery created successfully.");

												{

													// Confirmation message before print data
//													Builder alertBeforePrint = new Builder(context);
//													alertBeforePrint.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
//													alertBeforePrint
//															.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to print?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
//															new DialogInterface.OnClickListener() {
//																	public void onClick(
//																			DialogInterface dialog,
//																			int id) {


													dba.open();
													btAddress = dba.getBluetooth();

													final String custNames =dba.printCustomerNameById(customerId);
													lables = dba.printDeliveryById(unicId);
													total = dba.printDeliveryByIdTotal(unicId);
													today = dba.getDate();
													dba.close();
													if (btAddress.equalsIgnoreCase("0"))
														common.showToast("Alert! Set Printer from menu!", 30000);
													else {
														Thread t = new Thread() {
															public void run() {
																try {

																	NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00");
																	OutputStream os = mBluetoothSocket.getOutputStream();

																	//To get delivery details from database
																	if (lables != null && lables.size() > 0) {
																		String BILL = "                                  ";
																		BILL = BILL + "\n   SHRI GANESH MILK PRODUCTS    ";
																		BILL = BILL + "\n   Plot No. 109. Sector 1A,     ";
																		BILL = BILL + "\n Timber Market, Kopar Khairane, ";
																		BILL = BILL + "\n     Maharashtra - 400709       ";
																		BILL = BILL + "\n   GSTIN/UIN: 27ABGFS3890M2ZG   ";
																		BILL = BILL + "\nTel:27548367/27548369/8080166166";
																		BILL = BILL + "\n--------------------------------";
																		BILL = BILL + String.format("\n%-32s", custNames);
																		BILL = BILL + String.format("\n%32s", common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", today));
																		BILL = BILL + String.format("\n%-10s%8s%6s%8s\n", "Product", "Quantity", "Rate", "Amount");
																		for (HashMap<String, String> lable : lables) {
																			//totalAmounts =totalAmounts+ Double.valueOf(lable.get("DelQty"))*Double.valueOf(lable.get("Rate").replace(",", ""));
																			BILL = BILL + String.valueOf(lable.get("Item")) + "\n";
																			BILL = BILL + String.format("%8s%11s%13s", String.valueOf(lable.get("DelQty")), String.valueOf(formatter.format(Float.valueOf(lable.get("Rate")))), String.valueOf(formatter.format(Float.valueOf(lable.get("Amount")))));
																		}
																		if (Float.valueOf(total) > 0) {
																			BILL = BILL + String.format("\n%-15s%17s", "Total(In Rs.): ", String.valueOf(formatter.format(Float.valueOf(total)))) + "\n";					}
																		BILL = BILL + "--------------------------------";
																		BILL = BILL + String.format("\n%32s\n", userName);
																		BILL = BILL + "\n\n\n";
																		os.write(BILL.getBytes());
																		try {
																			if (mBluetoothSocket != null)
																				mBluetoothSocket.close();
																		} catch (Exception e) {
																			Log.e("Tag", "Exe ", e);
																		}
																	}
																} catch (Exception e) {
																	//Log.e("Main", "Exe ", e);
																}
															}
														};
														t.start();
													}
												}
//															}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
//														public void onClick(DialogInterface dialog, int id) {
//															dialog.cancel();
//														}
//													});
//													alertBeforePrint.create().show();
//												}

												//Thread.sleep(2000);
												SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
												//To bind default delivery date
												final Calendar c = Calendar.getInstance();
												c.add(Calendar.DATE,0);
												String deliveryDate = dateFormatter.format(c.getTime());
												dba.open();
												dba.Insert_DemandDate(deliveryDate);
												dba.close();

												Intent homeScreenIntent;
												homeScreenIntent = new Intent(context, ActivityHomeScreen.class);
												//homeScreenIntent = new Intent(context, ActivityDeliveryViewDetail.class);
												homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
												//homeScreenIntent.putExtra("Id", customerId);
												//homeScreenIntent.putExtra("Header",((CustomType) spCustomer.getSelectedItem()).getName());
												startActivity(homeScreenIntent);
												finish();
											}


										} catch (Exception e) {
											e.printStackTrace();
											common.showAlert(ActivityCreateDelivery.this, "Error in Saving delivery.\n"+e.getMessage(), false);
										}
									}
									else{
										dba.open();
										dba.insertExceptions("Unable to connect to Internet !", "ActivityCreatedelivery.java","onCreate()");
										dba.close();
									}
								}
							}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					alertDialogBuilder.create().show();
				}
			}
		});
	}

	private void BindCustomer()
	{
		btnCreate.setVisibility(View.GONE);
		btnPayment.setVisibility(View.GONE);
		tvNoRecord.setVisibility(View.GONE);
		btnCreate.setVisibility(View.GONE);
		btnPayment.setVisibility(View.GONE);
		tvNoRecord.setVisibility(View.GONE);
		tvTotalAmount.setText("");

		// To get customer list from database and bind to customer drop down list
		Log.i("LPDND", "TEXT="+((RadioButton) radioGroupRoute.findViewById(radioGroupRoute.getCheckedRadioButtonId())).getText());
		//if(((RadioButton) radioGroupRoute.findViewById(radioGroupRoute.getCheckedRadioButtonId())).getText().toString().equalsIgnoreCase("Assigned Route") || ((RadioButton) radioGroupRoute.findViewById(radioGroupRoute.getCheckedRadioButtonId())).getText().toString().equalsIgnoreCase("असाइन रूट"))
		int radioButtonID = radioGroupRoute.getCheckedRadioButtonId();
		View radioButton = radioGroupRoute.findViewById(radioButtonID);
		idx = radioGroupRoute.indexOfChild(radioButton);
		if(idx==0)
			spCustomer.setAdapter(DataAdapter("customerByRoute", routeId, lang));
		else
			spCustomer.setAdapter(DataAdapter("otherCustomerByRoute", routeId, lang));
	}

	//Data Adapter of drop down list
	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter, String lang) {
		dba.open();
		List<CustomType> lables = dba.GetMasterDetailsByLang(masterType, filter, lang);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(
				this, android.R.layout.simple_spinner_item, lables);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dba.close();
		return dataAdapter;
	}

	// To bind delivery from database
	private void GetDeliveryInput(String customerId) {
		dba.openR();
		ArrayList<HashMap<String, String>> listItem = dba.getDeliveryInput(customerId);
		if (listItem.size() > 0) {
			// To show product and its detail
			setListAdapter(new CustomAdapter(this, listItem));
			arrTemp = new String[listItem.size()];
			ListViewHelper.getListViewSize(getListView());
			btnCreate.setVisibility(View.VISIBLE);
			btnPayment.setVisibility(View.VISIBLE);
			tvNoRecord.setVisibility(View.GONE);
		} else {
			// To show 'no records found'
			setListAdapter(null);
			btnCreate.setVisibility(View.GONE);
			btnPayment.setVisibility(View.GONE);
			tvNoRecord.setVisibility(View.VISIBLE);
		}
	}

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

	// To make view holder to display on screen
	public class CustomAdapter extends BaseAdapter {
		private Context context2;
		class ViewHolder {
			TextView tvId, tvSku, tvRate, tvAvailable, tvDemand, tvAmount, tvCompanyId,tvFactorId,tvFactorValue;
			EditText etQty;
			int ref;
		}

		ArrayList<HashMap<String, String>> _listItems;

		// constructor of custom adapter class
		public CustomAdapter(Context context,
							 ArrayList<HashMap<String, String>> listItem) {
			this.context2 = context;
			mInflater = LayoutInflater.from(context2);
			_listItems = listItem;
		}

		private LayoutInflater mInflater;

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
						R.layout.activity_create_delivery_item, null);
				holder = new ViewHolder();

				holder.tvId = (TextView) arg1.findViewById(R.id.tvId);
				holder.tvSku = (TextView) arg1.findViewById(R.id.tvSku);
				holder.tvRate = (TextView) arg1.findViewById(R.id.tvRate);
				holder.tvAvailable = (TextView) arg1.findViewById(R.id.tvAvailable);
				holder.tvDemand = (TextView) arg1.findViewById(R.id.tvDemand);
				holder.etQty = (EditText) arg1.findViewById(R.id.etDelivery);
				holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);
				holder.tvCompanyId = (TextView) arg1.findViewById(R.id.tvCompanyId);
				holder.tvFactorId = (TextView) arg1.findViewById(R.id.tvFactorId);
				holder.tvFactorValue = (TextView) arg1.findViewById(R.id.tvFactorValue);
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			holder.ref = arg0;
			// To bind data from list

			holder.tvFactorId.setText(_listItems.get(arg0).get("mulFactor"));
			holder.tvId.setText(_listItems.get(arg0).get("id"));
			holder.tvSku.setText(lang.equalsIgnoreCase("hi")?_listItems.get(arg0).get("productLocal"): _listItems.get(arg0).get("sku"));
			holder.tvAvailable.setText(String.format("%.1f",
					Double.parseDouble(_listItems.get(arg0).get("aqty"))).replace(".0", ""));
			holder.tvDemand.setText(_listItems.get(arg0).get("dqty"));
			holder.tvCompanyId.setText(_listItems.get(arg0).get("companyId"));
			holder.tvRate.setText(common.stringToTwoDecimal(_listItems.get(arg0).get("rate")));
			if (arrTemp[arg0] != null)
				holder.etQty.setText(String.format("%.1f",
						Double.parseDouble(arrTemp[arg0])));
			if (_listItems.get(arg0).get("type").length() < 1) {
				// To display decimal point in number key board control
				holder.etQty
						.setFilters(new InputFilter[] { new DecimalDigitsInputFilter(
								5, 1) });
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

			// Instantiates a TextWatcher, to observe value changes and trigger the result calculation
			TextWatcher textWatcher = new TextWatcher() {
				public void afterTextChanged(Editable s) {

					if (!holder.etQty.getText().toString().equalsIgnoreCase(".")) {
						if (holder.etQty.getText().toString().equalsIgnoreCase("."))
							holder.etQty.setText("");
						if (holder.etQty.getText().toString().trim().length() > 0)
						{
							if (Double.parseDouble(holder.etQty.getText().toString())== 0)
							{
								holder.etQty.setText("");
								holder.tvAmount.setText("");
							}
							else if (Double.parseDouble(holder.etQty.getText().toString().trim())*Double.parseDouble(holder.tvFactorId.getText().toString().trim()) > Double.parseDouble(holder.tvAvailable.getText().toString().trim()))
							{
								common.showToast("Delivery quantity should not be exceeded from available quantity");
								holder.etQty.setText("");
							}
							else
								holder.tvAmount.setText(common.stringToTwoDecimal(String.valueOf(Double.parseDouble(holder.etQty.getText().toString())*Double.parseDouble(holder.tvFactorId.getText().toString().trim())* Double.parseDouble(holder.tvRate.getText().toString().replace(",", "")))));
						}
						else
						{
							holder.tvAmount.setText("");
						}
					}else
					{
						holder.tvAmount.setText("");
					}

					// To display total amount in footer of amount
					if(lang.equalsIgnoreCase("hi"))
						tvTotalAmount.setText(Html.fromHtml("<b>कुल: "+common.stringToTwoDecimal(GetTotal())+"</b>"));
					else
						tvTotalAmount.setText(Html.fromHtml("<b>Total: "+common.stringToTwoDecimal(GetTotal())+"</b>"));

				}
				public void beforeTextChanged(CharSequence s, int start, int count, int after){}
				public void onTextChanged(CharSequence s, int start, int before, int count){}
			};

			// Adds the TextWatcher as TextChangedListener to both EditTexts
			holder.etQty.addTextChangedListener(textWatcher);

			// Fix for text selection handle not disappearing
			holder.etQty.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View view2, boolean hasFocus) {
					view2.dispatchWindowFocusChanged(hasFocus);
				}
			});
			return arg1;
		}
	}



	// When press back button go to home screen
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
		try {
			if (mBluetoothSocket != null)
				mBluetoothSocket.close();
		} catch (Exception e) {
			Log.e("Tag", "Exe ", e);
		}
		Intent homeScreenIntent = new Intent(this, ActivityDeliveryViewSummary.class);
		homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeScreenIntent);
		finish();
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
					Thread mBlutoothConnectThread = new Thread(ActivityCreateDelivery.this);
					mBlutoothConnectThread.start();
				}
				break;

			case REQUEST_ENABLE_BT:
				if (resultCode == Activity.RESULT_OK) {
					ListPairedDevices();
					Intent i = new Intent(ActivityCreateDelivery.this, ActivityCreateDelivery.class);
					//i.putExtra("customerId", strCustId);
					//i.putExtra("customerName", strCustName);
					//i.putExtra("date", strDate);
					startActivityForResult(i, REQUEST_CONNECT_DEVICE);
				} else {
					Toast.makeText(ActivityCreateDelivery.this, "Message", Toast.LENGTH_LONG).show();
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
}
