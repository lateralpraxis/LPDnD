package lateralpraxis.lpdnd;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.types.CustomType;
import lateralpraxis.lpdnd.types.CustomerPayment;

public class ActivityDeliveryPayment   extends Activity implements Runnable{

	/*Start of code for printer*/
	private static final int REQUEST_CONNECT_DEVICE = 111;
	private static final int REQUEST_ENABLE_BT = 222;
	Button mScan, mPrint, mDisc;
	BluetoothAdapter mBluetoothAdapter;
	private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ProgressDialog mBluetoothConnectProgressDialog;
	private BluetoothSocket mBluetoothSocket;
	BluetoothDevice mBluetoothDevice;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private Cursor cursor;
	String btAddress = "0", today="";
	ArrayList<HashMap<String, String>> listA = null;
	String total = "0";
	List<CustomerPayment> lablePayment = null;
	/*End of code for printer*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	private UserSessionManager session;
	private MainAdapter ListAdapter;
	/*End of code to declare class*/

	/*Start of variables for uploading image*/
	private final Context mContext = this;
	protected static final int CAMERA_REQUEST = 0;
	protected static final int GALLERY_REQUEST = 1;
	Bitmap bitmap;
	Uri uri;
	Intent picIntent = null;
	private String level1Dir, level2Dir, fullPath,
	photoPath,uuidImg;
	private int fileCount = 0,companyCount =0,paymentCount=0,lsize=0,cnt=0;
	double totalDelamt, totalPayable, totalBalance;
	private String customerId, customerName, deliveryUniqueId, from;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private static final int PICK_Camera_IMAGE = 0;
	File destination, file;
	private ImageLoadingUtils utils;
	/*End of variable declaration for uploading image*/
	/*Start of variable declaration for displaying image*/
	File file1;
	private File[] listFile;
	private String[] FilePathStrings;
	private String[] FileNameStrings;
	/*End of variable declaration for displaying image*/
	/*Start of Code to Declare controls*/
	private TextView tvCustName, tvCustId,tvAttach,tvCompanyId,tvCompanyName,tvPayableAmount,tvBalanceData,tvBalanceAmount,tvTotalAmount;
	private Spinner spBank;
	private EditText etCashAmount, etChequeAmount,etChequeNo,etOnlineAmount,etRemarks;
	private Button btnUpload,btnNext,btnSubmit,btnSkip;
	private ListView lvDeliveryInfoList;
	private LinearLayout llBank,llAttachment,llChequeNo,llRemarks;
	String lang="en", userName = "";
	/*End of Code to Declare controls*/
	final String Digits     = "(\\p{Digit}+)";
	final String HexDigits  = "(\\p{XDigit}+)";
	// an exponent is 'e' or 'E' followed by an optionally 
	// signed decimal integer.
	final String Exp        = "[eE][+-]?"+Digits;
	final String fpRegex    =
			("[\\x00-\\x20]*"+ // Optional leading "whitespace"
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
	    "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

	    // . Digits ExponentPart_opt FloatTypeSuffix_opt
	    "(\\.("+Digits+")("+Exp+")?)|"+

	    // Hexadecimal strings
	    "((" +
	    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
	    "(0[xX]" + HexDigits + "(\\.)?)|" +

	    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
	    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

	    ")[pP][+-]?" + Digits + "))" +
	    "[fFdD]?))" +
					"[\\x00-\\x20]*");

	//On create method similar to page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		//Code to set layout
		setContentView(R.layout.activity_delivery_payment);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());
		// To read user role from user session manager
		final HashMap<String, String> user = session.getLoginUserDetails();
		userName = user.get(UserSessionManager.KEY_USERNAME);

		utils = new ImageLoadingUtils(this);
		HeaderDetails = new ArrayList<HashMap<String, String>>();
		/*		*/
		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		db.open();
		db.deleteTempDoc();
		db.close();
		//Code to find controls inside layouts
		etCashAmount = (EditText) findViewById(R.id.etCashAmount);
		etChequeAmount = (EditText) findViewById(R.id.etChequeAmount);
		etChequeNo = (EditText) findViewById(R.id.etChequeNo);
		etOnlineAmount = (EditText) findViewById(R.id.etOnlineAmount);
		etRemarks = (EditText) findViewById(R.id.etRemarks);
		spBank= (Spinner) findViewById(R.id.spBank);
		tvCustName= (TextView) findViewById(R.id.tvCustName);
		tvCustId= (TextView) findViewById(R.id.tvCustId);
		tvAttach= (TextView) findViewById(R.id.tvAttach);
		tvCompanyId= (TextView) findViewById(R.id.tvCompanyId);
		tvCompanyName= (TextView) findViewById(R.id.tvCompanyName);
		tvPayableAmount= (TextView) findViewById(R.id.tvPayableAmount);
		tvBalanceData= (TextView) findViewById(R.id.tvBalanceData);
		tvBalanceAmount= (TextView) findViewById(R.id.tvBalanceAmount);
		tvTotalAmount= (TextView) findViewById(R.id.tvTotalAmount);
		lvDeliveryInfoList = (ListView) findViewById(R.id.lvDeliveryInfoList);
		btnSubmit= (Button) findViewById(R.id.btnSubmit);
		btnUpload= (Button) findViewById(R.id.btnUpload);
		btnNext= (Button) findViewById(R.id.btnNext);
		btnSkip= (Button) findViewById(R.id.btnSkip);
		llAttachment= (LinearLayout) findViewById(R.id.llAttachment);
		llBank= (LinearLayout) findViewById(R.id.llBank);
		llChequeNo= (LinearLayout) findViewById(R.id.llChequeNo);
		llRemarks= (LinearLayout) findViewById(R.id.llRemarks);
		//Code to bind bank in spinner
		spBank.setAdapter(DataAdapter("bank",""));

		etCashAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		etCashAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

		etChequeAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		etChequeAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

		etOnlineAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		etOnlineAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

		//Code to get data requested by intent
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			customerId = extras.getString("customerId");
			customerName = extras.getString("customer");
			deliveryUniqueId= extras.getString("deliveryUniqueId");
			paymentCount = Integer.valueOf(extras.getString("paymentCount"));
			from = extras.getString("from");
		}
		if(paymentCount==0)
		{
			db.open();
			db.DeleteTempPaymentDetails("0");
			db.close();
		}
		tvCustId.setText(customerId);
		tvCustName.setText(customerName);

        //<editor-fold desc="Bluetooth">
        setResult(Activity.RESULT_CANCELED);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(ActivityDeliveryPayment.this, R.layout.device_name);

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
                mBluetoothConnectProgressDialog = ProgressDialog.show(ActivityDeliveryPayment.this,
                        "Connecting...", mBluetoothDevice.getName() + " : "
                                + mBluetoothDevice.getAddress(), true, true);
                Thread mBlutoothConnectThread = new Thread(ActivityDeliveryPayment.this);
                mBlutoothConnectThread.start();
            }
        }
        //</editor-fold>

		tvAttach.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				db.openR();
				String struploadedFilePath = db.getTempDocument();

				try {

					String actPath =struploadedFilePath;
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

							Intent i1 = new Intent(ActivityDeliveryPayment.this, ViewImage.class);
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
					common.showAlert(ActivityDeliveryPayment.this,"Error: "+except.getMessage(),false);

				}
			}
		});
		db.openR();
		HashMap<String,String> PayDetailMap = db.GetPendingPaymentForDelivery(customerId, deliveryUniqueId);
		tvCompanyId.setText(PayDetailMap.get("CompanyId"));
		tvCompanyName.setText(PayDetailMap.get("CompanyName"));
		totalPayable =Double.valueOf(PayDetailMap.get("Balance"));
		if(totalPayable<0)
		{
			tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))).replace("-", "")));
			tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))).replace("-", "")));
		}
		else
		{
			tvPayableAmount.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))))+")");
			tvBalanceData.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))))+")");
		}
		companyCount=db.GetCompanyCountForDelivery(customerId, deliveryUniqueId);
		db.close();
		paymentCount=paymentCount+1;

		//To get delivery details from database
		db.open();	
		ArrayList<HashMap<String, String>> lables = db.getDeliveryDetailsForPayments(tvCompanyId.getText().toString(), deliveryUniqueId);	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("SKU", String.valueOf(lable.get("SKU"))); 
				hm.put("Qty", String.valueOf(lable.get("Qty")));
				hm.put("Amount", String.valueOf(lable.get("Amount")));
				totalDelamt =totalDelamt+Double.parseDouble(lable.get("Amount"));
				HeaderDetails.add(hm); 
			}
		}
		//totalDelamt =db.GetTotaPaymentForDeliveryDetailsForPayments(tvCompanyId.getText().toString(), deliveryUniqueId);
		tvTotalAmount.setText(common.stringToTwoDecimal(String.format("%.2f",totalDelamt)));
		if(totalPayable<0)
			totalBalance = Double.valueOf(String.format("%.2f",totalPayable))+Double.valueOf(String.format("%.2f",totalDelamt));
		else
			totalBalance = Double.valueOf(String.format("%.2f",totalPayable))+Double.valueOf(String.format("%.2f",totalDelamt));
		if(totalBalance<0)
			tvBalanceAmount.setText(common.stringToTwoDecimal(String.valueOf(totalBalance)).replace("-", ""));
		else
			tvBalanceAmount.setText("("+common.stringToTwoDecimal(String.valueOf(totalBalance))+")");

		db.close();
		if(lsize>0)
		{
			ListAdapter = new MainAdapter(ActivityDeliveryPayment.this);
			lvDeliveryInfoList.setAdapter(ListAdapter);
		}

		//Code to decide whether to display next button or submit button
		if(paymentCount==companyCount)
		{
			btnSubmit.setVisibility(View.VISIBLE);
			btnNext.setVisibility(View.GONE);
			btnSkip.setVisibility(View.GONE);
		}
		else
		{
			btnSubmit.setVisibility(View.GONE);
			btnNext.setVisibility(View.VISIBLE);
			btnSkip.setVisibility(View.VISIBLE);
		}

		//Code to calculate balance amount on filling Cash Amount
		etCashAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)
				{
					double cashAmt=0.0;
					double chequeAmt=0.0;
					double onlineAmt=0.0;
					double totalPayable=0.0;
					if(Pattern.matches(fpRegex, etCashAmount.getText()))
					{
						if(tvPayableAmount.getText().toString().contains("("))
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
						else
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", ""))*-1;
						if(etCashAmount.getText().toString().trim().equals(""))
							cashAmt =0.0;
						else
							cashAmt=Double.valueOf(etCashAmount.getText().toString());
						
						if(etOnlineAmount.getText().toString().trim().equals(""))
							onlineAmt =0.0;
						else
							onlineAmt=Double.valueOf(etOnlineAmount.getText().toString());
						
						if(etChequeAmount.getText().toString().trim().equals(""))
							chequeAmt =0.0;
						else
							chequeAmt=Double.valueOf(etChequeAmount.getText().toString());

						if(common.stringToTwoDecimal(String.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))).contains("-"))
							tvBalanceData.setText(common.stringToTwoDecimal(String.valueOf(Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt)))).replace("-", ""));
						else
							tvBalanceData.setText("("+common.stringToTwoDecimal(String.valueOf(Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))))+")");
					}
					else
						etCashAmount.setText("");

				}
			}
		});
		//Code to calculate balance amount on filling Cheque Amount
		etChequeAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)
				{
					double cashAmt=0.0;
					double chequeAmt=0.0;
					double onlineAmt=0.0;
					double totalPayable=0.0;
					if(Pattern.matches(fpRegex, etChequeAmount.getText()))
					{
						if(tvPayableAmount.getText().toString().contains("("))
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
						else
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", ""))*-1;
						
						if(etOnlineAmount.getText().toString().trim().equals(""))
							onlineAmt =0.0;
						else
							onlineAmt=Double.valueOf(etOnlineAmount.getText().toString());
						
						if(etCashAmount.getText().toString().trim().equals(""))
							cashAmt =0.0;
						else
							cashAmt=Double.valueOf(etCashAmount.getText().toString());
						if(etChequeAmount.getText().toString().trim().equals(""))
							chequeAmt =0.0;
						else
							chequeAmt=Double.valueOf(etChequeAmount.getText().toString());
						if(common.stringToTwoDecimal(String.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))).contains("-"))
							tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt)))).replace("-", ""));
						else
							tvBalanceData.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))))+")");
					}
					else
						etChequeAmount.setText("");

				}
			}
		});

		//Code to calculate balance amount on filling Online Amount
		etOnlineAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)
				{
					double cashAmt=0.0;
					double chequeAmt=0.0;
					double onlineAmt=0.0;
					double totalPayable =0.0;
					if(Pattern.matches(fpRegex, etOnlineAmount.getText()))
					{
						if(tvPayableAmount.getText().toString().contains("("))
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
						else
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", ""))*-1;

						if(etOnlineAmount.getText().toString().trim().equals(""))
							onlineAmt =0.0;
						else
							onlineAmt=Double.valueOf(etOnlineAmount.getText().toString());
						if(etChequeAmount.getText().toString().trim().equals("") || etChequeAmount.getText().toString().trim().equals("."))
							chequeAmt =0.0;
						else
							chequeAmt=Double.valueOf(etChequeAmount.getText().toString());

						if(etCashAmount.getText().toString().trim().equals("") || etCashAmount.getText().toString().trim().equals("."))
							cashAmt =0.0;
						else
							cashAmt=Double.valueOf(etCashAmount.getText().toString());

						if(common.stringToTwoDecimal(String.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))).contains("-"))
							tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt)))).replace("-", ""));
						else
							tvBalanceData.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))))+")");
					}
					else
						etOnlineAmount.setText("");

				}
			}
		});


		//Text Watcher for Show / Hide Attachment and Bank Details
		TextWatcher textWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(!etChequeAmount.getText().toString().trim().equalsIgnoreCase("."))
				{
					if(etChequeAmount.getText().toString().trim().length()>0 && Double.valueOf(etChequeAmount.getText().toString().trim())>0 )
					{
						llBank.setVisibility(View.VISIBLE);
						llAttachment.setVisibility(View.VISIBLE);
						llChequeNo.setVisibility(View.VISIBLE);
					}
					else
					{
						spBank.setSelection(0);
						tvAttach.setText("");
						etChequeNo.setText("");
						photoPath="";
						llBank.setVisibility(View.GONE);
						llAttachment.setVisibility(View.GONE);
						llChequeNo.setVisibility(View.GONE);
						db.open();
						db.deleteTempDoc();
						db.close();
						level1Dir = "LPDND" + "/" + uuidImg;
						File dir = new File(level1Dir);
						DeleteRecursive(dir);
						tvAttach.setText("");
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){
				if(!etChequeAmount.getText().toString().trim().equalsIgnoreCase("."))
				{
					if(etChequeAmount.getText().toString().trim().length()>0 && Double.valueOf(etChequeAmount.getText().toString().trim())>0)
					{
						llBank.setVisibility(View.VISIBLE);
						llAttachment.setVisibility(View.VISIBLE);
						llChequeNo.setVisibility(View.VISIBLE);
					}
					else
					{
						spBank.setSelection(0);
						tvAttach.setText("");
						etChequeNo.setText("");
						photoPath="";
						llBank.setVisibility(View.GONE);
						llAttachment.setVisibility(View.GONE);
						llChequeNo.setVisibility(View.GONE);
						db.open();
						db.deleteTempDoc();
						db.close();
						level1Dir = "LPDND" + "/" + uuidImg;
						File dir = new File(level1Dir);
						DeleteRecursive(dir);
						tvAttach.setText("");
					}
				}
			}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				if(!etChequeAmount.getText().toString().trim().equalsIgnoreCase("."))
				{
					if(etChequeAmount.getText().toString().trim().length()>0 && Double.valueOf(etChequeAmount.getText().toString().trim())>0)
					{
						llBank.setVisibility(View.VISIBLE);
						llAttachment.setVisibility(View.VISIBLE);
						llChequeNo.setVisibility(View.VISIBLE);
					}
					else
					{
						spBank.setSelection(0);
						tvAttach.setText("");
						etChequeNo.setText("");
						photoPath="";
						llBank.setVisibility(View.GONE);
						llAttachment.setVisibility(View.GONE);
						llChequeNo.setVisibility(View.GONE);
						db.open();
						db.deleteTempDoc();
						db.close();
						level1Dir = "LPDND" + "/" + uuidImg;
						File dir = new File(level1Dir);
						DeleteRecursive(dir);
						tvAttach.setText("");
					}
				}
			}
		};
		etChequeAmount.addTextChangedListener(textWatcher);

		//Text Watcher for Show / Hide Remarks For Online Payment
		TextWatcher textOnlineWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(!etOnlineAmount.getText().toString().trim().equalsIgnoreCase("."))
				{
					if(etOnlineAmount.getText().toString().trim().length()>0 && Double.valueOf(etOnlineAmount.getText().toString().trim())>0)
					{
						llRemarks.setVisibility(View.VISIBLE);
					}
					else
					{
						llRemarks.setVisibility(View.GONE);
						etRemarks.setText("");
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){
				if(!etOnlineAmount.getText().toString().trim().equalsIgnoreCase("."))
				{
					if(etOnlineAmount.getText().toString().trim().length()>0 && Double.valueOf(etOnlineAmount.getText().toString().trim())>0)
					{
						llRemarks.setVisibility(View.VISIBLE);
					}
					else
					{
						llRemarks.setVisibility(View.GONE);
						etRemarks.setText("");
					}
				}
			}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				if(!etOnlineAmount.getText().toString().trim().equalsIgnoreCase("."))
				{
					if(etOnlineAmount.getText().toString().trim().length()>0 && Double.valueOf(etOnlineAmount.getText().toString().trim())>0)
					{
						llRemarks.setVisibility(View.VISIBLE);
					}
					else
					{
						llRemarks.setVisibility(View.GONE);
						etRemarks.setText("");
					}
				}
			}
		};

		etOnlineAmount.addTextChangedListener(textOnlineWatcher);
		//Code to be executed on click of next button
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				db.open();
				//Variable declared for processing image 
				String outdir="";
				String imagePath="";
				String selectedPhotoPath="";
				//Code for validating mandatory fields
				if(etCashAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया नकद राशि दर्ज करें।":"Please enter cash amount.");
				}
				else if(etChequeAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया चेक राशि दर्ज करें।":"Please enter cheque amount.");
				}
				else if(Double.valueOf(String.valueOf(etChequeAmount.getText()).trim())>0 && etChequeNo.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया चेक संख्या दर्ज करें।":"Please enter cheque number.");
				}
				else if(Double.valueOf(String.valueOf(etChequeAmount.getText()).trim())>0 && etChequeNo.getText().length()<6)
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"चेक नंबर में 6 अंक होने चाहिए।":"Cheque number must have 6 digits.");
				}
				else if(Double.valueOf(String.valueOf(etChequeAmount.getText()).trim())>0 && spBank.getSelectedItemPosition()==0)
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया बैंक चुनें।":"Please select bank.");
				}
				else if(etOnlineAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया ऑनलाइन राशि दर्ज करें।":"Please enter online amount.");
				}
				else if(etOnlineAmount.getText().toString().trim().equals("."))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया वैध ऑनलाइन राशि दर्ज करें।":"Please enter valid online amount.");
				}
				else if(Double.valueOf(String.valueOf(etOnlineAmount.getText()).trim())>0 && etRemarks.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया टिप्पणी दर्ज करें।":"Please enter remarks.");
				}
				else
				{
					etCashAmount.clearFocus();
					etChequeAmount.clearFocus();
					etOnlineAmount.clearFocus();
					String newuuidImg="";
					if(tvAttach.getText().toString().trim().length()>0)
					{
						//Code to save image in path and insert data in temporary table
						if(photoPath!=null || photoPath!="" )
						{
							newuuidImg = UUID.randomUUID().toString();
							//Setting directory structure
							level1Dir = "LPDND";
							level2Dir = level1Dir+"/"+newuuidImg;
							//Code to set full path
							fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
							//Code to set absolute path
							outdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+level2Dir;

							//Code to set photo path
							selectedPhotoPath = photoPath.substring(photoPath.lastIndexOf("/")+1);
						}
						//Code to check if directory exists else create directory
						if(createDirectory(level1Dir) && createDirectory(level2Dir) )
						{
							imagePath = copyFile(photoPath, outdir);
						}
					}
					//Code to insert data in temporary table
					db.open();
					db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), ((CustomType)spBank.getSelectedItem()).getId(), etChequeNo.getText().toString().trim(), etChequeAmount.getText().toString().trim(),imagePath, selectedPhotoPath,newuuidImg,"");
					db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "0", "", etCashAmount.getText().toString().trim(),"", "","","");
					db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "0", "", etOnlineAmount.getText().toString().trim(),"", "","",etRemarks.getText().toString().trim());
					db.close();
					//Building directory structure for deleting
					level1Dir = "LPDND";
					level2Dir = level1Dir+"/"+uuidImg;
					File dir = new File(level2Dir);
					//Method for deleting file and directory
					DeleteRecursive(dir);
					//Code to clear all controls after adding data in temporary table
					spBank.setSelection(0);
					etChequeNo.setText("");
					etChequeAmount.setText("");
					etCashAmount.setText("");
					tvAttach.setText("");
					common.showToast(lang.equalsIgnoreCase("hi")?"भुगतान विवरण सफलतापूर्वक जोड़ा गया।":"Payment details added successfully.");

					try {
						if (mBluetoothSocket != null)
							mBluetoothSocket.close();
					} catch (Exception e) {
						Log.e("Tag", "Exe ", e);
					}

					Intent i = new Intent(ActivityDeliveryPayment.this,ActivityDeliveryPayment.class);
					i.putExtra("customerId", tvCustId.getText().toString());
					i.putExtra("customer", tvCustName.getText().toString());
					i.putExtra("paymentCount", String.valueOf(paymentCount));
					i.putExtra("deliveryUniqueId", deliveryUniqueId);
					i.putExtra("from", from);
					startActivity(i);
					finish();
				}
			}
		});

		//Code to be executed on click of skip button
		btnSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					if (mBluetoothSocket != null)
						mBluetoothSocket.close();
				} catch (Exception e) {
					Log.e("Tag", "Exe ", e);
				}

				//Code to insert data in temporary table
				db.open();
				db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "", "", "0".toString().trim(),"", "","","");
				db.close();

				Intent i = new Intent(ActivityDeliveryPayment.this,ActivityDeliveryPayment.class);
				i.putExtra("customerId", tvCustId.getText().toString());
				i.putExtra("customer", tvCustName.getText().toString());
				i.putExtra("paymentCount", String.valueOf(paymentCount));
				i.putExtra("deliveryUniqueId", deliveryUniqueId);
				i.putExtra("from", from);
				startActivity(i);
				finish();

			}
		});

		//Click of Upload button to attach cheque photo 
		btnUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (tvAttach.getText().toString().trim().length() > 0) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							mContext);
					builder1.setTitle("Attach Cheque");
					builder1.setMessage("Are you sure you want to remove existing cheque picture and upload new cheque picture?");
					builder1.setCancelable(true);
					builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							level1Dir = "LPDND" + "/" + uuidImg;
							File dir = new File(level1Dir);
							DeleteRecursive(dir);
							tvAttach.setText("");
							startDialog();

						}
					}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No",
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
				else
					startDialog();
			}
		});

		//Click of Submit button to attach cheque photo 
		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//Code for validating mandatory fields
				if(etCashAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया नकद राशि दर्ज करें।":"Please enter cash amount.");
				}
				else if(etChequeAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया चेक राशि दर्ज करें।":"Please enter cheque amount.");
				}
				else if(Double.valueOf(String.valueOf(etChequeAmount.getText()).trim())>0 && etChequeNo.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया चेक नंबर दर्ज करें।":"Please enter cheque number.");
				}
				else if(Double.valueOf(String.valueOf(etChequeAmount.getText()).trim())>0 && etChequeNo.getText().length()<6)
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"चेक नंबर में 6 अंक होने चाहिए।":"Cheque number must have 6 digits.");
				}
				else if(Double.valueOf(String.valueOf(etChequeAmount.getText()).trim())>0 && spBank.getSelectedItemPosition()==0)
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया बैंक चुनें।":"Please select bank.");
				}
				else if(etOnlineAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया ऑनलाइन राशि दर्ज करें।":"Please enter online amount.");
				}
				else if(etOnlineAmount.getText().toString().trim().equals("."))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया वैध ऑनलाइन राशि दर्ज करें।":"Please enter valid online amount.");
				}
				else if(Double.valueOf(String.valueOf(etOnlineAmount.getText()).trim())>0 && etRemarks.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया टिप्पणी दर्ज करें।":"Please enter remarks.");
				}
				else
				{
					etCashAmount.clearFocus();
					etChequeAmount.clearFocus();
					etOnlineAmount.clearFocus();
					String stralertmsg=lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप भुगतान विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to save payment details?";
					final HashMap<String, String> user = session.getLoginUserDetails();
					AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
					builder1.setTitle(lang.equalsIgnoreCase("hi")?"भुगतान सबमिट करें":"Submit Payment");
					builder1.setMessage(stralertmsg);
					builder1.setCancelable(true);
					builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							//Variable declared for processing image 
							String outdir="";
							String imagePath="";
							String selectedPhotoPath="";
							//Code for inserting data in temporary table
							String newuuidImg="";
							if(tvAttach.getText().toString().trim().length()>0)
							{
								//Code to save image in path and insert data in temporary table
								if(photoPath!=null || photoPath!="" )
								{
									newuuidImg = UUID.randomUUID().toString();
									//Setting directory structure
									level1Dir = "LPDND";
									level2Dir = level1Dir+"/"+newuuidImg;
									//Code to set full path
									fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
									//Code to set absolute path
									outdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+level2Dir;

									//Code to set photo path
									selectedPhotoPath = photoPath.substring(photoPath.lastIndexOf("/")+1);
								}
								//Code to check if directory exists else create directory
								if(createDirectory(level1Dir) && createDirectory(level2Dir) )
								{
									imagePath = copyFile(photoPath, outdir);
								}
							}
							//Code to insert data in temporary table
							db.open();
							db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), ((CustomType)spBank.getSelectedItem()).getId(), etChequeNo.getText().toString().trim(), etChequeAmount.getText().toString().trim(),imagePath, selectedPhotoPath,newuuidImg,"");
							db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "0", "", etCashAmount.getText().toString().trim(),"", "","","");
							db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "0", "", etOnlineAmount.getText().toString().trim(),"", "","",etRemarks.getText().toString().trim());
							db.close();
							//Building directory structure for deleting
							level1Dir = "LPDND";
							level2Dir = level1Dir+"/"+uuidImg;
							File dir = new File(level2Dir);
							//Method for deleting file and directory
							DeleteRecursive(dir);
							db.openR();
							if(db.GetTotaPaymentCollected()>0.1)
							{
								//end of code to insert data in temporary table
								db.open();
								db.insertCustomerPayment(UUID.randomUUID().toString(), tvCustId.getText().toString(), user.get(UserSessionManager.KEY_ID), deliveryUniqueId);
								db.close();
								common.showToast(lang.equalsIgnoreCase("hi")?"भुगतान विवरण सफलतापूर्वक जोड़ा गया।":"Payment details saved successfully.");

								/////////////////////
								{
									db.open();
									btAddress = db.getBluetooth();
									today = db.getDate();
									listA = db.printDeliveryById(deliveryUniqueId);
									total = db.printDeliveryByIdTotal(deliveryUniqueId);
									lablePayment = db.printCustomerPaymentDelivery(deliveryUniqueId);
									db.close();
									if (btAddress.equalsIgnoreCase("0"))
										common.showToast("Alert! Set Printer from menu!", 30000);
									else {
										Thread t = new Thread() {
											public void run() {
												try {
													NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00");
													OutputStream os = mBluetoothSocket.getOutputStream();
													String BILL="";
													//To get delivery details
													if (listA != null && listA.size() > 0) {
														BILL = "                                  ";
														BILL = BILL + "\n   SHRI GANESH MILK PRODUCTS    ";
														BILL = BILL + "\n   Plot No. 109. Sector 1A,     ";
														BILL = BILL + "\n Timber Market, Kopar Khairane, ";
														BILL = BILL + "\n     Maharashtra - 400709       ";
														BILL = BILL + "\n   GSTIN/UIN: 27ABGFS3890M2ZG   ";
														BILL = BILL + "\nTel:27548367/27548369/8080166166";
														BILL = BILL + "\n--------------------------------";
														BILL = BILL + String.format("\n%-32s", customerName);
														BILL = BILL + String.format("\n%32s", common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", today));
														BILL = BILL + String.format("\n%-10s%8s%6s%8s\n", "Product", "Quantity", "Rate", "Amount");
														for (HashMap<String, String> lable : listA) {
															BILL = BILL + String.valueOf(lable.get("Item")) + "\n";
															BILL = BILL + String.format("%8s%11s%13s", String.valueOf(lable.get("DelQty")), String.valueOf(formatter.format(Float.valueOf(lable.get("Rate")))), String.valueOf(formatter.format(Float.valueOf(lable.get("Amount")))));
														}
														if (Float.valueOf(total) > 0) {
															BILL = BILL + String.format("\n%-15s%17s", "Total(In Rs.): ", String.valueOf(formatter.format(Float.valueOf(total)))) + "\n";
														}
													}
													////////// Payment /////////
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
													////// End of Payment //////
												} catch (Exception e) {
													//Log.e("Main", "Exe ", e);
												}
											}
										};
										t.start();
									}
								}
								/////////////////////

								SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
								//To bind default date
								final Calendar c = Calendar.getInstance();
								String demandDate = dateFormatter.format(c.getTime());
								db.open();
								db.Update_DemandDate(demandDate);
								db.close();

								Intent homeScreenIntent;
								homeScreenIntent = new Intent(ActivityDeliveryPayment.this, ActivityHomeScreen.class);
								//homeScreenIntent = new Intent(ActivityDeliveryPayment.this, ActivityDeliveryViewDetail.class);
								homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								//homeScreenIntent.putExtra("Id", tvCustId.getText().toString());
								//homeScreenIntent.putExtra("Header",tvCustName.getText().toString());
								//homeScreenIntent.putExtra("First","first");
								startActivity(homeScreenIntent);
								finish();
							}
							else
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDeliveryPayment.this);
								builder.setTitle(lang.equalsIgnoreCase("hi")?"चेतावनी संदेश":"Alert Message");
								builder.setMessage(lang.equalsIgnoreCase("hi")?"शून्य डेटा दर्ज किया गया है विवरण सहेजे नहीं जाएंगे।":"Nil data has been entered details will not be saved.")
								.setCancelable(false)
								.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										//Code to delete all data from temporary table
										db.open();
										db.DeleteTempPaymentDetails("0");
										db.close();

										SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
										//To bind default date
										final Calendar c = Calendar.getInstance();
										String demandDate = dateFormatter.format(c.getTime());
										db.open();
										db.Update_DemandDate(demandDate);
										db.close();
										try {
											if (mBluetoothSocket != null)
												mBluetoothSocket.close();
										} catch (Exception e) {
											Log.e("Tag", "Exe ", e);
										}

										Intent homeScreenIntent;
										homeScreenIntent = new Intent(ActivityDeliveryPayment.this, ActivityDeliveryViewDetail.class);
										homeScreenIntent.putExtra("Id", tvCustId.getText().toString()); 
										homeScreenIntent.putExtra("Header",tvCustName.getText().toString());
										homeScreenIntent.putExtra("First","first");
										homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(homeScreenIntent);
										finish();
									}
								});
								AlertDialog alert = builder.create();
								alert.show();										
							}

						}
					}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No",
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
			}
		});
	}

	//To make class of delivery view holder
	@SuppressLint("InflateParams")
	public class MainAdapter extends BaseAdapter {
		class ViewHolder {
			TextView tvSKUName, tvDelQty, tvDelAmt; 
		}	

		private LayoutInflater mInflater;
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

		//Main adapter of view holder
		public MainAdapter(Context context) {
			super();			
			mInflater = LayoutInflater.from(context);
		}

		//To instantiate layout XML file into its corresponding view objects.
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {	
			cnt= cnt+1;
			final ViewHolder holder;

			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.list_delivery_payment, null);
				holder = new ViewHolder();
				holder.tvSKUName = (TextView)arg1.findViewById(R.id.tvSKUName);
				holder.tvDelQty = (TextView)arg1.findViewById(R.id.tvDelQty);	
				holder.tvDelAmt = (TextView)arg1.findViewById(R.id.tvDelAmt);		
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			//To bind data into view holder
			holder.tvSKUName.setText(HeaderDetails.get(arg0).get("SKU"));
			holder.tvDelQty.setText(HeaderDetails.get(arg0).get("Qty").replace(".0", ""));
			holder.tvDelAmt.setText(common.stringToTwoDecimal(HeaderDetails.get(arg0).get("Amount")));
			return arg1;
		}
	}

	//Code for opening dialog for selecting image
	private void startDialog() {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityDeliveryPayment.this);
		builderSingle.setTitle("Select Image source");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				ActivityDeliveryPayment.this,
				android.R.layout.select_dialog_singlechoice);
		arrayAdapter.add("Capture Image");
		arrayAdapter.add("Select from Gallery");


		builderSingle.setNegativeButton(
				"Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builderSingle.setAdapter(
				arrayAdapter,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = arrayAdapter.getItem(which);
						//Check if camera option is selected
						if(strName.equals("Capture Image"))
						{
							//Setting directory structure
							uuidImg = UUID.randomUUID().toString();
							level1Dir = "LPDND";
							level2Dir = level1Dir+"/"+uuidImg;
							String imageName =random() + ".jpg";
							fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
							destination = new File(fullPath, imageName);
							//Check if directory exists else create directory
							if(createDirectory(level1Dir) && createDirectory(level2Dir))
							{
								//Code to open camera intent
								Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								intent.putExtra(MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(destination));
								startActivityForResult(intent, PICK_Camera_IMAGE);
								db.open();
								db.Insert_TempDoc(fullPath + "/" + imageName);
								db.close();
							}
							//Code to set image name
							photoPath =fullPath+imageName;
							tvAttach.setText(imageName);
						}
						else if (strName.equals("Select from Gallery"))
						{
							//Code to open gallery intent
							picIntent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							picIntent.putExtra("return_data",true);
							startActivityForResult(picIntent,GALLERY_REQUEST);
						}
						else 
						{
							common.showToast("No File available for review.");
						}
					}
				});
		builderSingle.show();
	}

	//Code to be executed after action done for attaching
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				if (resultCode == Activity.RESULT_OK) {
					Bundle mExtra = data.getExtras();
					String mDeviceAddress = mExtra.getString("DeviceAddress");
					//Log.v(TAG, "Coming incoming address " + mDeviceAddress);
					mBluetoothDevice = mBluetoothAdapter
							.getRemoteDevice(mDeviceAddress);
					mBluetoothConnectProgressDialog = ProgressDialog.show(this,
							"Connecting...", mBluetoothDevice.getName() + " : "
									+ mBluetoothDevice.getAddress(), true, false);
					Thread mBlutoothConnectThread = new Thread(this);
					mBlutoothConnectThread.start();
					// pairToDevice(mBluetoothDevice); This method is replaced by
					// progress dialog with thread
				}
				break;

			case REQUEST_ENABLE_BT:
				if (resultCode == Activity.RESULT_OK) {
					ListPairedDevices();
					//Intent connectIntent = new Intent(ActivityDeliveryPayment.this,ActivityDeliveryPayment.class);

					Intent i = new Intent(ActivityDeliveryPayment.this,ActivityDeliveryPayment.class);
					i.putExtra("customerId", tvCustId.getText().toString());
					i.putExtra("customer", tvCustName.getText().toString());
					i.putExtra("paymentCount", String.valueOf(paymentCount));
					i.putExtra("deliveryUniqueId", deliveryUniqueId);
					i.putExtra("from", from);
					startActivityForResult(i, REQUEST_CONNECT_DEVICE);
				} else {
					Toast.makeText(ActivityDeliveryPayment.this, "Message", Toast.LENGTH_LONG).show();
				}
				break;
		}

		if(requestCode==0 && resultCode==0 && data ==null)
		{
			//Reset image name and hide reset button
			tvAttach.setText("");
		}
		else if (requestCode==GALLERY_REQUEST){		
			//Gallery request and result code is ok
			if (resultCode==RESULT_OK){
				if (data!=null) {
					uri = data.getData();
					if(uri!=null)
					{
						photoPath=getRealPathFromUri(uri);
						tvAttach.setText(photoPath.substring(photoPath.lastIndexOf("/")+1));
						uuidImg = UUID.randomUUID().toString();
						//Set directory path
						level1Dir = "LPDND";
						level2Dir = level1Dir+"/"+uuidImg;
						fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
						//Code to create file inside directory
						if (createDirectory(level1Dir)
								&& createDirectory(level2Dir)) {
							copyFile(photoPath, fullPath);
							destination = new File(photoPath);
						}
						db.open();
						db.Insert_TempDoc(fullPath + "/" + destination.getName());
						db.close();
					}
					else
					{

						Toast.makeText(getApplicationContext(), "Cancelled",
								Toast.LENGTH_SHORT).show();

					}
					if(photoPath!="" && photoPath!=null)
					{
						common.showToast("Gallery Image selected at path: "+ photoPath);
					}

				}else {
					Toast.makeText(getApplicationContext(), "Cancelled",
							Toast.LENGTH_SHORT).show();
				}
			}else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Cancelled",
						Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {
				//Camera request and result code is ok
				try {
					FileInputStream in = new FileInputStream(destination);
					photoPath = compressImage(destination.getAbsolutePath());
					//code to fetch selected image path
					in.close();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				catch (IOException e) {

					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
		else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Cancelled",
					Toast.LENGTH_SHORT).show();			
		}
	}

	//Method to get Actual path of image
	private String getRealPathFromUri(Uri tempUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = this.getContentResolver().query(tempUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	//Method to calculate sample size for image
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	//Method to get file count in directory
	public int CountFiles(File[] files) {
		if(files==null || files.length==0)
		{
			return 0;
		}
		else
		{
			for (File file : files) {
				if (file.isDirectory()) {
					CountFiles(file.listFiles());
				} else {
					if(!file.getAbsolutePath().contains(".nomedia"))
						fileCount++;
				}
			}
			return fileCount;
		}
	}

	//Method to compress the image
	public String compressImage(String path) {

		String filePath = path;
		Bitmap scaledBitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;						
		Bitmap bmp = BitmapFactory.decodeFile(filePath,options);

		int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;
		float maxHeight = 816.0f;
		float maxWidth = 612.0f;
		float imgRatio = actualWidth / actualHeight;
		float maxRatio = maxWidth / maxHeight;

		if (actualHeight > maxHeight || actualWidth > maxWidth) {
			if (imgRatio < maxRatio) {
				imgRatio = maxHeight / actualHeight;
				actualWidth = (int) (imgRatio * actualWidth);
				actualHeight = (int) maxHeight;
			} else if (imgRatio > maxRatio) {
				imgRatio = maxWidth / actualWidth;
				actualHeight = (int) (imgRatio * actualHeight);
				actualWidth = (int) maxWidth;
			} else {
				actualHeight = (int) maxHeight;
				actualWidth = (int) maxWidth;     

			}
		}

		options.inSampleSize = utils.calculateInSampleSize(options, actualWidth, actualHeight);
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inTempStorage = new byte[16*1024];

		try{	
			bmp = BitmapFactory.decodeFile(filePath,options);
		}
		catch(OutOfMemoryError exception){
			//exception.printStackTrace();

		}
		try{
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
		}
		catch(OutOfMemoryError exception){
			//exception.printStackTrace();
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float)options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(destination);
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}

		return destination.getAbsolutePath();

	}

	//Method to generate random number and return the same
	public static String random() {
		Random r = new Random();

		char[] choices = ("abcdefghijklmnopqrstuvwxyz" +
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
				"01234567890").toCharArray();

		StringBuilder salt = new StringBuilder(10);
		for (int i = 0; i<10; ++i)
			salt.append(choices[r.nextInt(choices.length)]);
		return "img_"+salt.toString();
	}

	//Method to delete File Recursively
	public void DeleteRecursive(File fileOrDirectory) {

		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();

	}


	//Method to create new directory
	private boolean createDirectory(String dirName)
	{
		//Code to Create Directory for Inspection (Parent)
		File folder = new File(Environment.getExternalStorageDirectory() + "/"+dirName);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
		}
		if (success) {
			copyNoMediaFile(dirName);
			return true;
		} else {
			return false;
		}
	}

	//Method to create No Media File in directory
	private void copyNoMediaFile(String dirName)
	{
		try {
			// Open your local db as the input stream
			//boolean D= true;
			String storageState = Environment.getExternalStorageState();

			if ( Environment.MEDIA_MOUNTED.equals( storageState ) )
			{
				try
				{
					File noMedia = new File ( Environment
							.getExternalStorageDirectory()
							+ "/"
							+ level2Dir, ".nomedia" );
					if ( noMedia.exists() )
					{


					}

					FileOutputStream noMediaOutStream = new FileOutputStream ( noMedia );
					noMediaOutStream.write ( 0 );
					noMediaOutStream.close ( );
				}
				catch ( Exception e )
				{

				}
			}
			else
			{

			}

		} catch (Exception e) {

		}
	}

	//Copy file from one place to another
	private String copyFile(String inputPath, String outputPath) {

		File f = new File(inputPath);
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(inputPath);        
			out = new FileOutputStream(outputPath+"/"+f.getName());

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			compressImage(outputPath+"/"+f.getName());

			// write the output file (You have now copied the file)
			out.flush();
			out.close();
			out = null;  



		}  catch (FileNotFoundException fnfe1) {
			//Log.e("tag", fnfe1.getMessage());
		}
		catch (Exception e) {
			//Log.e("tag", e.getMessage());
		}
		return outputPath+"/"+f.getName();
	}
	//Code for array list to bind data in spinner
	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter)
	{
		db.open();
		List <CustomType> lables = db.GetMasterDetails(masterType, filter);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this,android.R.layout.simple_spinner_item, lables);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		db.close();
		return dataAdapter;
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

	//Event Triggered on Clicking Back
	@Override
	public void onBackPressed() {

		AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
		builder1.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
		builder1.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप भुगतान मॉड्यूल छोड़ना चाहते हैं, यह भुगतान लेनदेन को छोड़ देगा?":"Are you sure, you want to leave payment module it will discard payment transaction?");
		builder1.setCancelable(true);
		builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int id) {
				try {
					if (mBluetoothSocket != null)
						mBluetoothSocket.close();
				} catch (Exception e) {
					Log.e("Tag", "Exe ", e);
				}
				Intent i;
				if(from.equalsIgnoreCase("delivery"))
				{
					i = new Intent(ActivityDeliveryPayment.this, ActivityDeliveryViewDetail.class);
					i.putExtra("Id", tvCustId.getText().toString());
					i.putExtra("Header", tvCustName.getText().toString());
					i.putExtra("First","first");
				}
				else
					i = new Intent(ActivityDeliveryPayment.this,ActivityPaymentView.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
			}
		}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No",
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


	//To create menu on inflater
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		return true;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mBluetoothAdapter != null)
		{
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> mAdapterView, View mView, int mPosition, long mLong)
		{
			mBluetoothAdapter.cancelDiscovery();
			String mDeviceInfo = ((TextView) mView).getText().toString();
			String mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length() - 17);
			//Log.v(TAG, "Device_Address " + mDeviceAddress);

			Bundle mBundle = new Bundle();
			mBundle.putString("DeviceAddress", mDeviceAddress);
			Intent mBackIntent = new Intent();
			mBackIntent.putExtras(mBundle);
			setResult(Activity.RESULT_OK, mBackIntent);
			finish();
		}
	};

	private void ListPairedDevices() {
		Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (mPairedDevices.size() > 0) {
			for (BluetoothDevice mDevice : mPairedDevices) {
				//Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  "+ mDevice.getAddress());
			}
		}
	}

	public void run() {
		try {
			mBluetoothSocket = mBluetoothDevice
					.createRfcommSocketToServiceRecord(applicationUUID);
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

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mBluetoothConnectProgressDialog.dismiss();
			Toast.makeText(ActivityDeliveryPayment.this, "Connected", Toast.LENGTH_LONG).show();
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

	public byte[] sel(int val) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putInt(val);
		buffer.flip();
		return buffer.array();
	}
}
