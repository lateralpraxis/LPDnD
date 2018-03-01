package lateralpraxis.lpdnd;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.types.CustomType;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityPaymentOnly   extends Activity{
	/*Start of code to declare controls*/
	private Spinner spCustomer,spBank;
	private TextView tvCustName, tvCustId,tvAttach,tvCompanyId,tvCompanyName,tvPayableAmount,tvBalanceData;
	private Button btnGo,btnUpload,btnNext,btnSubmit,btnSkip;
	private EditText etCashAmount, etChequeAmount,etChequeNo,etOnlineAmount,etRemarks;
	private LinearLayout llCustSelection,llPayment,llBank,llAttachment,llChequeNo,llRemarks;
	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	private UserSessionManager session;
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
	private int fileCount = 0,companyCount =0,paymentCount=0;
	private String customerId, customerName;
	private static final int PICK_Camera_IMAGE = 0;
	File destination, file;
	private ImageLoadingUtils utils;
	/*End of variable declaration for uploading image*/
	/*Start of variable declaration for displaying image*/
	File file1;
	String lang="en";
	private File[] listFile;
	private String[] FilePathStrings;
	private String[] FileNameStrings;
	/*End of variable declaration for displaying image*/
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
		//Code to set layout
		setContentView(R.layout.activity_payment_only);

		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());
		utils = new ImageLoadingUtils(this);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
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
		//Code to find layouts		
		llCustSelection= (LinearLayout) findViewById(R.id.llCustSelection);
		llPayment= (LinearLayout) findViewById(R.id.llPayment);
		llBank= (LinearLayout) findViewById(R.id.llBank);
		llAttachment= (LinearLayout) findViewById(R.id.llAttachment);
		llChequeNo= (LinearLayout) findViewById(R.id.llChequeNo);
		llRemarks= (LinearLayout) findViewById(R.id.llRemarks);
		//Code to find controls inside layouts
		etCashAmount = (EditText) findViewById(R.id.etCashAmount);
		etChequeAmount = (EditText) findViewById(R.id.etChequeAmount);
		etChequeNo = (EditText) findViewById(R.id.etChequeNo);
		etOnlineAmount = (EditText) findViewById(R.id.etOnlineAmount);
		etRemarks = (EditText) findViewById(R.id.etRemarks);
		spCustomer= (Spinner) findViewById(R.id.spCustomer);
		spBank= (Spinner) findViewById(R.id.spBank);
		btnGo= (Button) findViewById(R.id.btnGo);
		btnUpload= (Button) findViewById(R.id.btnUpload);
		btnNext= (Button) findViewById(R.id.btnNext);
		btnSkip= (Button) findViewById(R.id.btnSkip);
		btnSubmit= (Button) findViewById(R.id.btnSubmit);
		tvCustName= (TextView) findViewById(R.id.tvCustName);
		tvCustId= (TextView) findViewById(R.id.tvCustId);
		tvAttach= (TextView) findViewById(R.id.tvAttach);
		tvCompanyId= (TextView) findViewById(R.id.tvCompanyId);
		tvCompanyName= (TextView) findViewById(R.id.tvCompanyName);
		tvPayableAmount= (TextView) findViewById(R.id.tvPayableAmount);
		tvBalanceData= (TextView) findViewById(R.id.tvBalanceData);

		etCashAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		etCashAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

		etChequeAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		etChequeAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

		etOnlineAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		etOnlineAmount.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

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

							Intent i1 = new Intent(ActivityPaymentOnly.this, ViewImage.class);
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
					common.showAlert(ActivityPaymentOnly.this,"Error: "+except.getMessage(),false);

				}
			}
		});

		//Code to bind customer in spinner
		spCustomer.setAdapter(DataAdapter("customer","",lang ));

		//Code to bind bank in spinner
		spBank.setAdapter(DataAdapter("bank",""));
		//code to show / hide layouts
		llCustSelection.setVisibility(View.VISIBLE);
		llPayment.setVisibility(View.GONE);
		//Code to get data requested by intent
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			customerId = extras.getString("custId");
			customerName = extras.getString("custName");
			paymentCount = Integer.valueOf(extras.getString("paymentCount"));
		}
		//Code to check customer ID send from intent
		if(customerId.equalsIgnoreCase("0"))
		{
			//Code to show / Hide layouts on basis of customer id
			llCustSelection.setVisibility(View.VISIBLE);
			llPayment.setVisibility(View.GONE);
			btnGo.setVisibility(View.VISIBLE);
			//Code to delete all data from temporary table
			db.open();
			db.DeleteTempPaymentDetails("0");
			db.close();
		}
		else
		{
			//Code to show / Hide layouts on basis of customer id
			llCustSelection.setVisibility(View.GONE);
			llPayment.setVisibility(View.VISIBLE);
			btnGo.setVisibility(View.GONE);
			db.openR();
			tvCustId.setText(customerId);
			tvCustName.setText(customerName);
			HashMap<String,String> PayDetailMap = db.GetPendingPaymentByCustomerId(customerId);
			tvCompanyId.setText(PayDetailMap.get("CompanyId"));
			tvCompanyName.setText(PayDetailMap.get("CompanyName"));

			if(Double.valueOf(PayDetailMap.get("Balance"))<0)
			{
				tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))).replace("-", "")));
				tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))).replace("-", "")));
			}
			else
			{

				tvPayableAmount.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))))+")");
				tvBalanceData.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))))+")");
			}
			/*	tvPayableAmount.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")));
			tvBalanceData.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")));*/
			companyCount=db.GetCompanyCountByCustomer(customerId);
			db.close();
			paymentCount=paymentCount+1;
		}
		//Code to decide whether to display next button or submit button
		if(paymentCount==companyCount && !customerId.equalsIgnoreCase("0"))
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
		//Code on button go click event show / hide layout and freeze customer
		btnGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(spCustomer.getSelectedItemPosition()==0)
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया ग्राहक का चयन करें।":"Please select customer.");
				}
				else
				{
					//-------------Newly Added
					customerId=String.valueOf(((CustomType)spCustomer.getSelectedItem()).getId());
					if(customerId.equalsIgnoreCase("0"))
					{
						//Code to show / Hide layouts on basis of customer id
						llCustSelection.setVisibility(View.VISIBLE);
						llPayment.setVisibility(View.GONE);
						btnGo.setVisibility(View.VISIBLE);
						//Code to delete all data from temporary table
						db.open();
						db.DeleteTempPaymentDetails("0");
						db.close();
					}
					else
					{
						//Code to show / Hide layouts on basis of customer id
						llCustSelection.setVisibility(View.GONE);
						llPayment.setVisibility(View.VISIBLE);
						btnGo.setVisibility(View.GONE);
						db.openR();
						tvCustId.setText(customerId);
						tvCustName.setText(customerName);
						HashMap<String,String> PayDetailMap = db.GetPendingPaymentByCustomerId(customerId);
						tvCompanyId.setText(PayDetailMap.get("CompanyId"));
						tvCompanyName.setText(PayDetailMap.get("CompanyName"));

						if(Double.valueOf(PayDetailMap.get("Balance"))<0)
						{
							tvPayableAmount.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))).replace("-", "")));
							tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))).replace("-", "")));
						}
						else
						{
							tvPayableAmount.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))))+")");
							tvBalanceData.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(PayDetailMap.get("Balance"))))+")");
						}
						/*	tvPayableAmount.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")));
						tvBalanceData.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")));*/
						companyCount=db.GetCompanyCountByCustomer(customerId);
						db.close();
						paymentCount=paymentCount+1;
					}

					//Code to decide whether to display next button or submit button
					if(paymentCount==companyCount && !customerId.equalsIgnoreCase("0"))
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

					//-------------Newly Added
					//Code to bind customer details
					tvCustName.setText(String.valueOf(((CustomType)spCustomer.getSelectedItem()).getName()));
					tvCustId.setText(String.valueOf(((CustomType)spCustomer.getSelectedItem()).getId()));
					//code to show / hide layouts
					llCustSelection.setVisibility(View.GONE);
					llPayment.setVisibility(View.VISIBLE);
					btnGo.setVisibility(View.GONE);
					db.openR();
					//Code to bind company details and total payable
					HashMap<String,String> PayDetailMap = db.GetPendingPaymentByCustomerId(String.valueOf(((CustomType)spCustomer.getSelectedItem()).getId()));
					tvCompanyId.setText(PayDetailMap.get("CompanyId"));
					tvCompanyName.setText(PayDetailMap.get("CompanyName"));
					/*tvPayableAmount.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")));
					tvBalanceData.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")));*/

					if(Double.valueOf(PayDetailMap.get("Balance"))<0)
					{
						tvPayableAmount.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")).replace("-", ""));
						tvBalanceData.setText(common.stringToTwoDecimal(PayDetailMap.get("Balance")).replace("-", ""));
					}
					else
					{
						tvPayableAmount.setText("("+common.stringToTwoDecimal(PayDetailMap.get("Balance"))+")");
						tvBalanceData.setText("("+common.stringToTwoDecimal(PayDetailMap.get("Balance"))+")");
					}
					companyCount=db.GetCompanyCountByCustomer(String.valueOf(((CustomType)spCustomer.getSelectedItem()).getId()));
					db.close();

				}
			}
		});
		//Code to calculate balance amount on filling Cash Amount
		etCashAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)
				{
					double cashAmt=0.0;
					double chequeAmt=0.0;
					double onlineAmt=0.0;
					double totalPayable =0.0;
					if(Pattern.matches(fpRegex, etCashAmount.getText()))
					{
						if(tvPayableAmount.getText().toString().contains("("))
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
						else
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", ""))*-1;
						if(etCashAmount.getText().toString().trim().equals("") || etCashAmount.getText().toString().trim().equals("."))
							cashAmt =0.0;
						else
							cashAmt=Double.valueOf(etCashAmount.getText().toString());
						if(etChequeAmount.getText().toString().trim().equals(""))
							chequeAmt =0.0;
						else
							chequeAmt=Double.valueOf(etChequeAmount.getText().toString());

						if(etOnlineAmount.getText().toString().trim().equals(""))
							onlineAmt =0.0;
						else
							onlineAmt=Double.valueOf(etOnlineAmount.getText().toString());

						if(common.stringToTwoDecimal(String.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))).contains("-"))
							tvBalanceData.setText(common.stringToTwoDecimal(String.format("%.2f", Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt)))).replace("-", ""));
						else
							tvBalanceData.setText("("+common.stringToTwoDecimal(String.format("%.2f",Double.valueOf(totalPayable+(cashAmt+chequeAmt+onlineAmt))))+")");
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
					double totalPayable =0.0;
					if(Pattern.matches(fpRegex, etChequeAmount.getText()))
					{
						if(tvPayableAmount.getText().toString().contains("("))
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", "").replace("(", "").replace(")", ""));
						else
							totalPayable =Double.valueOf(tvPayableAmount.getText().toString().replace(",", ""))*-1;

						if(etCashAmount.getText().toString().trim().equals(""))
							cashAmt =0.0;
						else
							cashAmt=Double.valueOf(etCashAmount.getText().toString());
						if(etChequeAmount.getText().toString().trim().equals("") || etChequeAmount.getText().toString().trim().equals("."))
							chequeAmt =0.0;
						else
							chequeAmt=Double.valueOf(etChequeAmount.getText().toString());

						if(etOnlineAmount.getText().toString().trim().equals(""))
							onlineAmt =0.0;
						else
							onlineAmt=Double.valueOf(etOnlineAmount.getText().toString());
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
				else if(etCashAmount.getText().toString().trim().equals("."))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया वैध नकदी राशि दर्ज करें।":"Please enter valid cash amount.");
				}
				else if(etChequeAmount.getText().toString().trim().equals(""))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया चेक राशि दर्ज करें।":"Please enter cheque amount.");
				}
				else if(etChequeAmount.getText().toString().trim().equals("."))
				{
					common.showToast(lang.equalsIgnoreCase("hi")?"कृपया वैध चेक राशि दर्ज करें।":"Please enter valid cheque amount.");
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
					Intent i = new Intent(ActivityPaymentOnly.this,ActivityPaymentOnly.class);
					i.putExtra("custId", tvCustId.getText().toString());
					i.putExtra("custName", tvCustName.getText().toString());
					i.putExtra("paymentCount", String.valueOf(paymentCount));
					startActivity(i);
					finish();
				}

			}
		});
		//Code to be executed on click of skip button
		btnSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				db.open();

				//Code to insert data in temporary table
				db.open();
				db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "0", "", "","", "","","");
				//db.insertCustomerPaymentTemp(tvCompanyId.getText().toString(), "0", "", "","", "","");
				db.close();

				Intent i = new Intent(ActivityPaymentOnly.this,ActivityPaymentOnly.class);
				i.putExtra("custId", tvCustId.getText().toString());
				i.putExtra("custName", tvCustName.getText().toString());
				i.putExtra("paymentCount", String.valueOf(paymentCount));
				startActivity(i);
				finish();

			}
		});
		//Click of Upload button to attach cheque photo 
		btnUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String strmessage=lang.equalsIgnoreCase("hi")?"क्या आप वाकई मौजूदा चेक चित्र को हटाना चाहते हैं और नया चेक चित्र अपलोड करना चाहते हैं?":"Are you sure, you want to remove existing cheque picture and upload new cheque picture?";
				if (tvAttach.getText().toString().trim().length() > 0) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							mContext);
					builder1.setTitle("Attach Cheque");
					builder1.setMessage(strmessage);
					builder1.setCancelable(true);
					builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							db.open();
							db.deleteTempDoc();
							db.close();							
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
				etCashAmount.clearFocus();
				etChequeAmount.clearFocus();
				etOnlineAmount.clearFocus();
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
								db.insertCustomerPayment(UUID.randomUUID().toString(), tvCustId.getText().toString(), user.get(UserSessionManager.KEY_ID), "0");
								db.close();
								common.showToast(lang.equalsIgnoreCase("hi")?"भुगतान विवरण सफलतापूर्वक जोड़ा गया।":"Payment details saved successfully.");

								Intent homeScreenIntent;
								homeScreenIntent = new Intent(ActivityPaymentOnly.this, ActivityPaymentView.class);
								homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(homeScreenIntent);
								finish();
							}
							else
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPaymentOnly.this);
								builder.setTitle(lang.equalsIgnoreCase("hi")?"चेतावनी संदेश":"Alert Message");
								builder.setMessage(lang.equalsIgnoreCase("hi")?"शून्य डेटा दर्ज किया गया है विवरण सहेजे नहीं जाएंगे।":"Nil data has been entered details will not be saved.")
								.setCancelable(false)
								.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										//Code to delete all data from temporary table
										db.open();
										db.DeleteTempPaymentDetails("0");
										db.close();
										Intent homeScreenIntent;
										homeScreenIntent = new Intent(ActivityPaymentOnly.this, ActivityPaymentView.class);
										homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(homeScreenIntent);
										finish();
									}
								});
								AlertDialog alert = builder.create();
								alert.show();
								//common.showAlert(ActivityPaymentOnly.this, "Nil data has been entered details will not be saved.", false);

							}
							/*Intent homeScreenIntent;
								if(from.equalsIgnoreCase("delivery"))
									homeScreenIntent = new Intent(ActivityPayment.this, ActivityDeliveryViewSummary.class);
								else
									homeScreenIntent = new Intent(ActivityPayment.this, ActivityPaymentView.class);
								homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(homeScreenIntent);
								finish();*/
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

	//Code for opening dialog for selecting image
	private void startDialog() {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityPaymentOnly.this);
		builderSingle.setTitle("Select Image source");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				ActivityPaymentOnly.this,
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

	//Code for array list to bind data in spinner
	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter, String langOption)
	{
		db.open();
		List <CustomType> lables = db.GetMasterDetailsByLang(masterType, filter, langOption);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this,android.R.layout.simple_spinner_item, lables);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		db.close();
		return dataAdapter;
	}


	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
			builder1.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
			builder1.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप भुगतान मॉड्यूल छोड़ना चाहते हैं, यह भुगतान लेनदेन को छोड़ देगा?":"Are you sure, you want to leave payment module it will discard payment transaction?");
			builder1.setCancelable(true);
			builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int id) {
					Intent i = new Intent(ActivityPaymentOnly.this,ActivityPaymentView.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
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
				Intent i = new Intent(ActivityPaymentOnly.this,ActivityPaymentView.class);
				startActivity(i);
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

}
