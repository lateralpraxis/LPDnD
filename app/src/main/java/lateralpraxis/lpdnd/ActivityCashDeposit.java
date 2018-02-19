package lateralpraxis.lpdnd;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import lateralpraxis.lpdnd.types.CashDeposit;

@SuppressLint("InflateParams")
public class ActivityCashDeposit  extends Activity{
	/*Start of code to declare controls*/
	private TextView tvEmpty,tvTotalAmt,tvCompanyId,tvCompanyName,tvBalanceData,
	tvCollectionData, tvTotalData, tvTotalDepositedData, tvTotalChequeDepositedData,tvOnlineData;
	private Button btnSubmit, btnNext;
	private EditText etAmount, etRemarks;
	private String strId="";
	private CheckBox chkAll;
	private LinearLayout  llAmount, llCashDeposit,llNavBtn,llTotal, llChequeDeposited, llTotalDeposited, llRemarks ;

	private TableLayout tableLayout1,tableLayoutTotal;
	private ListView lvCashDepositList;
	private String companyId, userId, previousBalance,	collectionAmount, totalAmount, depositAmount,onlineAmount;
	private String JSONStr, deviceIP, responseJSON;
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
	/*End of code to declare controls*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	private UserSessionManager session;
	/*End of code to declare class*/

	/*Start of code to declare variables*/
	private ArrayList<HashMap<String, String>> CashDepositDetails;
	private int lsize=0;
	private int routeCount = 0,totalCount = 0 ;
	private double totalChequeAmount = 0, totalChequeCount = 0;
	private final Context mContext = this;
	private int checkedCount = 0;
	private String lang;
	/*End of code to declare variables*/

	//On create method similar to page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Code to set layout
		setContentView(R.layout.activity_cash_deposit);
		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		
		//Code to find layouts
		llCashDeposit= (LinearLayout) findViewById(R.id.llCashDeposit);
		llAmount= (LinearLayout) findViewById(R.id.llAmount);
		llNavBtn= (LinearLayout) findViewById(R.id.llNavBtn);

		llTotal= (LinearLayout) findViewById(R.id.llTotal);
		llChequeDeposited= (LinearLayout) findViewById(R.id.llChequeDeposited);
		llTotalDeposited= (LinearLayout) findViewById(R.id.llTotalDeposited);
		llRemarks= (LinearLayout) findViewById(R.id.llRemarks);




		tableLayout1=(TableLayout) findViewById(R.id.tableLayout1);
		tableLayoutTotal=(TableLayout) findViewById(R.id.tableLayoutTotal);
		chkAll=(CheckBox) findViewById(R.id.chkAll);
		//Code to find controls inside layouts
		etAmount = (EditText) findViewById(R.id.etAmount);
		etRemarks= (EditText) findViewById(R.id.etRemarks);
		tvCompanyName= (TextView) findViewById(R.id.tvCompanyName);
		tvCompanyId= (TextView) findViewById(R.id.tvCompanyId);
		tvBalanceData= (TextView) findViewById(R.id.tvBalanceData);
		tvCollectionData= (TextView) findViewById(R.id.tvCollectionData);
		tvTotalAmt= (TextView) findViewById(R.id.tvTotalAmt);
		tvTotalData= (TextView) findViewById(R.id.tvTotalData);
		tvEmpty= (TextView) findViewById(R.id.tvEmpty);
		tvTotalDepositedData= (TextView) findViewById(R.id.tvTotalDepositedData);
		tvTotalChequeDepositedData= (TextView) findViewById(R.id.tvTotalChequeDepositedData);
		tvOnlineData= (TextView) findViewById(R.id.tvOnlineData);
		btnSubmit= (Button) findViewById(R.id.btnSubmit);
		btnNext= (Button) findViewById(R.id.btnNext);	

		tvTotalChequeDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));
		tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf("0")));
		lvCashDepositList =(ListView)findViewById(R.id.lvCashDepositList);
		//Code to set how many decimal places are allowed
		etAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});
		db.open();
		totalCount = db.GetTotalCashDepositCount();
		db.close();
		//Hash Map for storing data
		CashDepositDetails = new ArrayList<HashMap<String, String>>();
		BindHeaderDetail();
		chkAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean isChecked = chkAll.isChecked();
				totalChequeAmount = 0;
				if (isChecked == true) {					
					for(int i =0; i < lvCashDepositList.getCount(); i++)
					{
						View v2 = lvCashDepositList.getChildAt(i);
						TextView tvHiddenAmount = (TextView)v2.findViewById(R.id.tvHiddenAmount);
						totalChequeAmount = totalChequeAmount + Double.parseDouble(tvHiddenAmount.getText().toString());
					}
					tvTotalChequeDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));
					if(etAmount.getText().toString().trim().length() > 0)
						tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf((Double.valueOf(etAmount.getText().toString().trim()) -
								Double.valueOf(totalChequeAmount)))));
					else							
						tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));

					BindCashDeposit("1");
				} else if(isChecked==false)
				{
					totalChequeAmount = 0;
					tvTotalChequeDepositedData.setText("0.00");
					if(etAmount.getText().toString().trim().length() > 0)
						tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf((Double.valueOf(etAmount.getText().toString().trim()) -
								Double.valueOf(totalChequeAmount)))));
					else							
						tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));
					BindCashDeposit("0");
				}
			}
		});

		//Code to be executed on click of next button
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final HashMap<String, String> user = session.getLoginUserDetails();
				userId = user.get(UserSessionManager.KEY_ID);
				strId = "";
				//To validate required field and please enter at least one quantity!				
				for(int i =0; i < lvCashDepositList.getCount(); i++)
				{
					View v = lvCashDepositList.getChildAt(i);
					TextView tvPCDetailId = (TextView)v.findViewById(R.id.tvPCDetailId);
					CheckBox select = (CheckBox)v.findViewById(R.id.select);
					if(select.isChecked())
					{
						checkedCount=checkedCount+1;
						strId=strId+tvPCDetailId.getText().toString()+",";
					}					
				}
				if(etAmount.getText().toString().trim().equalsIgnoreCase(".") && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCashDeposit.this, "Please enter valid amount!", false);
				else if(etAmount.getText().toString().trim().equalsIgnoreCase(".") && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCashDeposit.this, "कृपया वैध राशि दर्ज करें!", false);
				
				else if(etAmount.getText().toString().trim().equalsIgnoreCase("") && checkedCount==0 && lang.equalsIgnoreCase("en"))
				common.showAlert(ActivityCashDeposit.this, "Please enter amount or select at least one cheque!", false);
				else if(etAmount.getText().toString().trim().equalsIgnoreCase("") && checkedCount==0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCashDeposit.this, "कृपया राशि दर्ज करें या कम से कम एक चेक का चयन करें!", false);
					
				
				else if(!etAmount.getText().toString().trim().equalsIgnoreCase("") && (Double.valueOf(etAmount.getText().toString().trim()) > Double.valueOf(totalAmount.trim())) && lang.equalsIgnoreCase("en"))
									common.showAlert(ActivityCashDeposit.this, "Cash Deposited amount cannot be greater than Total Payable!", false);
				else if(!etAmount.getText().toString().trim().equalsIgnoreCase("") && (Double.valueOf(etAmount.getText().toString().trim()) > Double.valueOf(totalAmount.trim())) && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCashDeposit.this, "नकद जमा राशि कुल भुगतान योग्य से अधिक नहीं हो सकती!", false);

				else
				{		
					Builder alertDialogBuilder = new Builder(mContext);
					alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
					alertDialogBuilder.setMessage(lang.equalsIgnoreCase("hi")?"भुगतान विवरण संपादन योग्य नहीं होगा। \nक्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Payment Details would not be editable.\nAre you sure, you want to save?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
														new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								depositAmount = etAmount.getText().toString().trim().
										equalsIgnoreCase("")?"0": String.valueOf(etAmount.getText());

								db.open();
								if(!etAmount.getText().toString().trim().equalsIgnoreCase("") || checkedCount> 0)
									db.Insert_CashDepositTransaction(companyId, previousBalance, collectionAmount, totalAmount, depositAmount, strId, etRemarks.getText().toString().trim());
								db.close();
								common.showToast(lang.equalsIgnoreCase("hi")?"नकद जमा विवरण सफलतापूर्वक जोड़ा गया।":"Cash Deposit details added successfully.");
								
								Intent i = new Intent(ActivityCashDeposit.this,ActivityCashDeposit.class);
								startActivity(i);
								finish();
							} catch (Exception e) {
								e.printStackTrace();
								common.showAlert(ActivityCashDeposit.this, "Error in Saving Cash Deposit.\n"+e.getMessage(), false);
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

		//On click of Customer Sign up
		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				final HashMap<String, String> user = session.getLoginUserDetails();
				userId = user.get(UserSessionManager.KEY_ID);
				strId = "";
				//To validate required field and please enter at least one quantity!
				for(int i =0; i < lvCashDepositList.getCount(); i++)
				{
					View v = lvCashDepositList.getChildAt(i);
					TextView tvPCDetailId = (TextView)v.findViewById(R.id.tvPCDetailId);
					CheckBox select = (CheckBox)v.findViewById(R.id.select);
					if(select.isChecked())
					{
						checkedCount=checkedCount+1;
						strId=strId+tvPCDetailId.getText().toString()+",";
					}					
				}				
				if(etAmount.getText().toString().trim().equalsIgnoreCase("") && checkedCount==0 && lang.equalsIgnoreCase("en"))
									common.showAlert(ActivityCashDeposit.this, "Please enter amount or select at least one cheque!", false);
				else if(etAmount.getText().toString().trim().equalsIgnoreCase("") && checkedCount==0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCashDeposit.this, "कृपया राशि दर्ज करें या कम से कम एक चेक का चयन करें!", false);
				
				
				else if(!etAmount.getText().toString().trim().equalsIgnoreCase("") && (Double.valueOf(etAmount.getText().toString().trim()) > Double.valueOf(totalAmount.trim())) && lang.equalsIgnoreCase("en"))
									common.showAlert(ActivityCashDeposit.this, "Cash Deposited amount cannot be greater than Total Payable!", false);
				else if(!etAmount.getText().toString().trim().equalsIgnoreCase("") && (Double.valueOf(etAmount.getText().toString().trim()) > Double.valueOf(totalAmount.trim())) && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCashDeposit.this, "नकद जमा राशि कुल भुगतान योग्य से अधिक नहीं हो सकती!", false);

				else
				{	
					Builder alertDialogBuilder = new Builder(mContext);
					alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
					alertDialogBuilder.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to submit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes", 
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {

								depositAmount = etAmount.getText().toString().trim().equalsIgnoreCase("")?"0": String.valueOf(etAmount.getText());
								db.open();
								if(!etAmount.getText().toString().trim().equalsIgnoreCase("") || checkedCount> 0)
									db.Insert_CashDepositTransaction(companyId, previousBalance, collectionAmount, totalAmount, depositAmount, strId, etRemarks.getText().toString().trim());	
								common.showToast(lang.equalsIgnoreCase("hi")?"नकद जमा विवरण सफलतापूर्वक जोड़ा गया।":"Cash Deposit details added successfully.");
								
								totalChequeCount = db.GetTotalChequeCollected();
								if(db.GetTotalCashDeposited()>0.1 || totalChequeCount > 0 )
								{
									//Call Async activity to send json to server for customer Validation
									if(common.isConnected())	
									{	
										FetchExternalIP task = new FetchExternalIP();
										task.execute();
									}
									else{
										db.insertExceptions("Unable to connect to Internet !", "ActivityCashDeposit.java", "onCreate()");
									}
								}
								else
								{
									Builder builder = new Builder(ActivityCashDeposit.this);
									builder.setTitle(lang.equalsIgnoreCase("hi")?"चेतावनी":"Alert");
									builder.setMessage(lang.equalsIgnoreCase("hi")?"शून्य डेटा दर्ज किया गया है, विवरण सहेजे नहीं जाएंगे।":"Nil data has been entered details will not be saved.")
									.setCancelable(false)
									.setPositiveButton("OK", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											//Code to delete all data from temporary table
											db.open();
											db.DeleteCashDeposit();
											db.close();
											Intent homeScreenIntent;
											homeScreenIntent = new Intent(ActivityCashDeposit.this, ActivityHomeScreen.class);
											homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
											startActivity(homeScreenIntent);
											finish();
										}
									});
									AlertDialog alert = builder.create();
									alert.show();
									//common.showAlert(ActivityPaymentOnly.this, "Nil data has been entered details will not be saved.", false);
								}
							}
							catch (Exception e) {
								e.printStackTrace();
								common.showAlert(ActivityCashDeposit.this, "Error in Saving Cash Deposit.\n"+e.getMessage(), false);
							}
						}	            	
					}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}	            	
					});
					db.close();
					alertDialogBuilder.create().show();
				}				
			}
		});
		//Code to set scroll on on touch event of list view
		lvCashDepositList.setOnTouchListener(new ListView.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;
				case MotionEvent.ACTION_UP:
					// Allow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				// Handle ListView touch events.
				v.onTouchEvent(event);
				return true;
			}
		});

		//On change of Return Quantity
		etAmount.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean gainFocus) {
				// onFocus
				if (gainFocus) {

				}
				// onBlur
				else {
					if(etAmount.getText().toString().trim().equalsIgnoreCase("."))
						etAmount.setText("");
					else if(!etAmount.getText().toString().trim().equalsIgnoreCase("") && (Double.valueOf(etAmount.getText().toString().trim()) > Double.valueOf(totalAmount.trim())))
					{
						etAmount.setText("");
						tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf((Double.valueOf("0.00") +
								Double.valueOf(totalChequeAmount)))));
					}
					else
					{
						if(etAmount.getText().toString().trim().length() > 0 && Pattern.matches(fpRegex, etAmount.getText()))
						{
							tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf((Double.valueOf(etAmount.getText().toString().trim()) +
									Double.valueOf(totalChequeAmount)))));
						}
						else
						{
							etAmount.setText("");
						}

					}
				}
			}
		});
	}


	private class FetchExternalIP extends AsyncTask<Void, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCashDeposit.this);
		@Override
		protected String doInBackground(Void... params) {
			// These two need to be declared outside the try/catch
			// so that they can be closed in the finally block.
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			// Will contain the raw JSON response as a string.
			String result = null;
			try {
				URL url = new URL("http://wtfismyip.com/text");
				// Create the request to OpenWeatherMap, and open the connection
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
				InputStream inputStream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				if (inputStream == null) {
					// Nothing to do.
					return null;
				}
				reader = new BufferedReader(new InputStreamReader(inputStream));

				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}

				if (buffer.length() == 0) {
					// Stream was empty.  No point in parsing.
					return null;
				}
				deviceIP = result = buffer.toString();
				return result;
			} catch (IOException e) {
				db.insertExceptions(e.getMessage(), "ActivityCashDeposit.java", "FetchExternalIP");

				return null;
			} finally{
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						db.insertExceptions(e.getMessage(), "ActivityCashDeposit.java", "FetchExternalIP");
					}
				}
			}
		}
		@Override
		protected void onPostExecute(String result) {
			db.open();
			JSONObject jsonCDT = new JSONObject();
			ArrayList<HashMap<String, String>> cdt = db.getCashDepositTransaction();                         
			if (cdt != null && cdt.size() > 0) {
				JSONArray array = new JSONArray();
				try {
					for (HashMap<String, String> cfd : cdt) {
						JSONObject jsonsret = new JSONObject();	
						jsonsret.put("CompanyId", cfd.get("CompanyId"));							
						jsonsret.put("PreviousBalance", cfd.get("PreviousBalance"));
						jsonsret.put("CollectionAmount", cfd.get("CollectionAmount"));
						jsonsret.put("TotalAmount", cfd.get("TotalAmount"));
						jsonsret.put("DepositAmount", cfd.get("DepositAmount"));
						jsonsret.put("Remarks", cfd.get("Remarks"));
						jsonsret.put("PCDetailId", cfd.get("PCDetailId"));
						array.put(jsonsret);
					}
					jsonCDT.put("CashDeposit",array);	
					db.close();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					db.insertExceptions(e.getMessage(), "ActivityCashDeposit.java", "FetchExternalIP");
				}
				finally
				{
					db.close();
				}
				JSONStr = jsonCDT.toString();
				AsyncCashDepositSubmitWSCall task = new AsyncCashDepositSubmitWSCall();
				task.execute();
				Dialog.dismiss();
			}  
			else
			{
				common.showToast("No Cash Deposit are pending to be Send to server!");			
			}
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Please Wait..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//Async Class to send Credentials
	private class AsyncCashDepositSubmitWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCashDeposit.this);
		@Override
		protected String doInBackground(String... params) {
			try {

				String[] name = {"json", "deviceId", "userId", "ipAddr", "machine", "rOfficerId" };
				String[] value = {JSONStr, db.uniqueId(common.getIMEI()), userId, common.getDeviceIPAddress(true), common.getIMEI(), userId};
				responseJSON= common.CallJsonWS(name,value,"InsertCashDepositDetails",common.url);
			}
			catch (SocketTimeoutException e){
				db.insertExceptions("TimeOut Exception. Internet is slow", "ActivityCashDeposit.java", "AsyncCashDepositSubmitWSCall");
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				//e.printStackTrace();
				db.insertExceptions(e.getMessage(), "ActivityCashDeposit.java", "AsyncCashDepositSubmitWSCall");
				return "ERROR: "+ e.getMessage();
			} 
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			Dialog.dismiss();
			try {
				if(!result.contains("ERROR: "))
				{
					db.open();
					db.DeleteCashDeposit();
					db.close();
					common.showToast("Cash Deposit has been successfully submitted.");
					Intent	intent = new Intent(mContext, ActivityHomeScreen.class);
					startActivity(intent);
					finish();
				}
				else
				{
					common.showAlertWithHomePage(ActivityCashDeposit.this, "Unable to login try again",false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				db.insertExceptions(e.getMessage(), "ActivityCashDeposit.java", "AsyncCashDepositSubmitWSCall");
				common.showAlertWithHomePage(ActivityCashDeposit.this,"Error: "+e.getMessage(),false);
			}
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}

	//Bind Header Details
	private void BindHeaderDetail()
	{
		db.open();
		HashMap<String,String> CashHeaderMap = db.GetPendingCashDeposit();
		tvCompanyName.setText(CashHeaderMap.get("CompanyName"));
		tvCompanyId.setText(CashHeaderMap.get("CompanyId"));
		companyId = String.valueOf(CashHeaderMap.get("CompanyId"));
		tvBalanceData.setText(common.stringToTwoDecimal(CashHeaderMap.get("PreviousBalance")));
		tvCollectionData.setText(common.stringToTwoDecimal(CashHeaderMap.get("CollectionAmount")));
		tvTotalData.setText(common.stringToTwoDecimal(CashHeaderMap.get("TotalAmount")));
		tvOnlineData.setText(common.stringToTwoDecimal(CashHeaderMap.get("OnlineAmount")));
		previousBalance = CashHeaderMap.get("PreviousBalance");
		collectionAmount= CashHeaderMap.get("CollectionAmount");
		totalAmount= CashHeaderMap.get("TotalAmount");
		onlineAmount = CashHeaderMap.get("OnlineAmount");
		routeCount =  db.GetCashDepositTransactionCount()+1;
		db.close();
		BindCashDeposit("0");				
		//Code to decide whether to display next button or submit button
		//if(routeCount==totalCount && !routeId.equalsIgnoreCase("0") && !companyId.equalsIgnoreCase("0"))
		if(routeCount==totalCount)
		{
			btnSubmit.setVisibility(View.VISIBLE);
			btnNext.setVisibility(View.GONE);
		}
		else
		{
			btnSubmit.setVisibility(View.GONE);
			btnNext.setVisibility(View.VISIBLE);
		}
	}


	//Method to bind payment detail data from temporary table
	private void BindCashDeposit(String str)
	{
		/*Start of code to bind data from temporary table*/
		CashDepositDetails.clear();
		db.open();		
		List <CashDeposit> lables = db.getRouteWiseCashDeposit(companyId);
		lsize = lables.size();
		//if(lsize>0 && Double.valueOf(totalAmount.trim()) > 0)
		if(lsize > 0)
		{
			//code to show / hide layouts
			llCashDeposit.setVisibility(View.VISIBLE);
			tvEmpty.setVisibility(View.GONE);
			tableLayout1.setVisibility(View.VISIBLE);
			tableLayoutTotal.setVisibility(View.VISIBLE);
			llNavBtn.setVisibility(View.VISIBLE);			
			llAmount.setVisibility(View.VISIBLE);
			llTotal.setVisibility(View.VISIBLE);
			llChequeDeposited.setVisibility(View.VISIBLE);
			llTotalDeposited.setVisibility(View.VISIBLE);
			llRemarks.setVisibility(View.VISIBLE);
			//Looping through hash map and add data to hash map
			for(int i=0;i<lables.size();i++){
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("PCDetailId", String.valueOf(lables.get(i).getId())); 
				hm.put("CustomerName", String.valueOf(lables.get(i).getCustomerName())); 
				hm.put("PaymentDate", String.valueOf(lables.get(i).getPaymentDate()));  
				hm.put("Cheque", String.valueOf(lables.get(i).getCheque()));
				hm.put("Amount",String.valueOf(lables.get(i).getAmount()));
				hm.put("UniqueId", String.valueOf(lables.get(i).getUniqueId()));
				hm.put("Checked", str);
				CashDepositDetails.add(hm);
			}
			db.open();
			String strTotal =String.valueOf(db.getCashDepositTotalPayment(companyId));
			tvTotalAmt.setText(common.stringToTwoDecimal(strTotal));
		}
		//else if(lsize==0 && Double.valueOf(totalAmount.trim()) > 0)
		else if(lsize==0)
		{
			tvEmpty.setVisibility(View.GONE);
			llNavBtn.setVisibility(View.VISIBLE);
			llAmount.setVisibility(View.VISIBLE);
			llTotal.setVisibility(View.VISIBLE);
			llChequeDeposited.setVisibility(View.VISIBLE);
			llTotalDeposited.setVisibility(View.VISIBLE);
			llRemarks.setVisibility(View.VISIBLE);

		}
		else
		{
			//Display no records message
			tvEmpty.setVisibility(View.VISIBLE);
			tableLayout1.setVisibility(View.GONE);
			tableLayoutTotal.setVisibility(View.GONE);
			llNavBtn.setVisibility(View.GONE);
			llAmount.setVisibility(View.GONE);
			llAmount.setVisibility(View.GONE);
			llTotal.setVisibility(View.GONE);
			llChequeDeposited.setVisibility(View.GONE);
			llTotalDeposited.setVisibility(View.GONE);
			llRemarks.setVisibility(View.GONE);
			llCashDeposit.setVisibility(View.GONE);
		}
		db.close();
		//Code to set hash map data in custom adapter
		Cadapter = new CustomAdapter(ActivityCashDeposit.this,CashDepositDetails);
		if(lsize>0)
			lvCashDepositList.setAdapter(Cadapter);	
		lvCashDepositList.requestLayout();
		/*End of code to bind data from temporary table*/
	}

	//Class for Binding Data in ListView
	public static class ViewHolder {
		//Control Declaration
		TextView tvPCDetailId, tvCustomerData,tvChequeData,tvAmountData,tvUniqueId, tvHiddenAmount; 
		CheckBox select, selectall;
	}
	//Declaring Adapter for binding data in List View
	public class CustomAdapter extends BaseAdapter {

		boolean[] itemChecked;
		private Context paymentContext;
		private LayoutInflater mInflater;
		//Method to return count on data in adapter
		@Override
		public int getCount() {
			return CashDepositDetails.size();
		}

		@Override
		public Object getItem(int arg0) {
			return CashDepositDetails.get(arg0);
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
		public CustomAdapter(Context context,ArrayList<HashMap<String, String>> lvCashDepositList) {
			this.paymentContext = context;
			mInflater = LayoutInflater.from(paymentContext);
			CashDepositDetails = lvCashDepositList;
			itemChecked = new boolean[lvCashDepositList.size()];

		}

		//Event is similar to row data bound event
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {			


			final ViewHolder holder;
			if (arg1 == null) 
			{
				//Code to set layout inside list view
				arg1 = mInflater.inflate(R.layout.activity_cash_deposit_item, null); 
				holder = new ViewHolder();
				//Code to find controls inside list view
				holder.tvPCDetailId = (TextView)arg1.findViewById(R.id.tvPCDetailId);
				holder.tvUniqueId = (TextView)arg1.findViewById(R.id.tvUniqueId);
				holder.tvCustomerData = (TextView)arg1.findViewById(R.id.tvCustomerData);
				holder.tvChequeData = (TextView)arg1.findViewById(R.id.tvChequeData);
				holder.tvAmountData = (TextView)arg1.findViewById(R.id.tvAmountData);
				holder.tvHiddenAmount = (TextView)arg1.findViewById(R.id.tvHiddenAmount);
				holder.select = (CheckBox) arg1.findViewById(R.id.select);
				holder.select.setChecked(false);
				if (itemChecked[arg0])
					holder.select.setChecked(true);
				else
					holder.select.setChecked(false);


				holder.select.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (holder.select.isChecked())
						{
							itemChecked[arg0] = true;
							totalChequeAmount = totalChequeAmount + Double.parseDouble(holder.tvHiddenAmount.getText().toString());
							tvTotalChequeDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));

						}
						else
						{
							totalChequeAmount = totalChequeAmount - Double.parseDouble(holder.tvHiddenAmount.getText().toString());
							tvTotalChequeDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));
							itemChecked[arg0] = false;
							chkAll.setChecked(false);
						}
						if(etAmount.getText().toString().trim().length() > 0)
							tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf((Double.valueOf(etAmount.getText().toString().trim()) +
									Double.valueOf(totalChequeAmount)))));
						else							
							tvTotalDepositedData.setText(common.stringToTwoDecimal(String.valueOf(totalChequeAmount)));
					}
				});				
				arg1.setTag(holder);
			}
			else
			{

				holder = (ViewHolder) arg1.getTag();
			}
			//Code to bind data from hash map in controls
			holder.tvPCDetailId.setText(CashDepositDetails.get(arg0).get("PCDetailId"));
			holder.tvCustomerData.setText(CashDepositDetails.get(arg0).get("CustomerName"));
			holder.tvChequeData.setText(CashDepositDetails.get(arg0).get("Cheque"));
			holder.tvUniqueId.setText(CashDepositDetails.get(arg0).get("UniqueId"));
			holder.tvAmountData.setText(common.stringToTwoDecimal(CashDepositDetails.get(arg0).get("Amount")));
			holder.tvHiddenAmount.setText(CashDepositDetails.get(arg0).get("Amount"));
			if(CashDepositDetails.get(arg0).get("Checked").equalsIgnoreCase("1"))
				holder.select.setChecked(true);
			else
				holder.select.setChecked(false);


			return arg1;
		}
	}

	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		case android.R.id.home:
			Builder builder1 = new Builder(mContext);
			builder1.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
			builder1.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप वाकई नकदी जमा मॉड्यूल छोड़ना चाहते हैं, यह नकदी जमा लेनदेन को त्याग देगा?":"Are you sure, you want to leave cash deposit module it will discard cash deposit transaction?");
			builder1.setCancelable(true);
			builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int id) {
					Intent i = new Intent(ActivityCashDeposit.this,ActivityHomeScreen.class);
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

		Builder builder1 = new Builder(mContext);
		builder1.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
		builder1.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप वाकई नकदी जमा मॉड्यूल छोड़ना चाहते हैं, यह नकदी जमा लेनदेन को त्याग देगा?":"Are you sure, you want to leave cash deposit module it will discard cash deposit transaction?");
		builder1.setCancelable(true);
		builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int id) {

				Intent homeScreenIntent = new Intent(ActivityCashDeposit.this, ActivityHomeScreen.class);
				homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(homeScreenIntent);
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

}