package lateralpraxis.lpdnd;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import lateralpraxis.lpdnd.types.CustomType;

public class ActivityCustomerSignUp extends Activity {
	//Declaring variables
	Button btnCreateCustomer;
	String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
	private Spinner spnCustomerType, spnCountry, spnState, spnCity, spnPinCode;
	private EditText etLogInId, etCustomerName, etContactPerson, etContactPersonMobile, etEmail, etStreet, etHouseNo, etLandMark;
	private Common common;
	//Start Variable Declaration
	private String custTypeId = "", loginId = "", custName = "", contactPerson = "", mobile = "", emailId = "", street = "", houseNo = "", landMark = "", stateId = "", cityId = "", pinCodeId = "";
	private Boolean isLogInIdExist = false;
	private DatabaseAdapter dba;
	private static String responseJSON;
	private String JSONStr;
	private String deviceIP;
	private String imei;
	final Context context = this;
	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer_signup);

		common = new Common(context);
		imei = common.getIMEI();
		dba=new DatabaseAdapter(this);
		//For Syncing latest master data
		SyncMastersData();
		//creating object of controls
		btnCreateCustomer=(Button)findViewById(R.id.btnCreateCustomer);
		spnCustomerType=(Spinner)findViewById(R.id.spnCustomerType);
		spnCountry = (Spinner)findViewById(R.id.spnCountry);
		spnState = (Spinner)findViewById(R.id.spnState);		
		spnCity = (Spinner)findViewById(R.id.spnCity);
		spnPinCode = (Spinner)findViewById(R.id.spnPinCode);
		etLogInId = (EditText)findViewById(R.id.etLogInId);
		etCustomerName = (EditText)findViewById(R.id.etCustomerName);
		etContactPerson = (EditText)findViewById(R.id.etContactPerson);
		etContactPersonMobile = (EditText)findViewById(R.id.etContactPersonMobile);
		etEmail = (EditText)findViewById(R.id.etEmail);
		etStreet = (EditText)findViewById(R.id.etStreet);
		etHouseNo = (EditText)findViewById(R.id.etHouseNo);
		etLandMark = (EditText)findViewById(R.id.etLandMark);
		//On change of Country
		spnCountry.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				spnState.setAdapter(DataAdapter("state",String.valueOf(((CustomType)spnCountry.getSelectedItem()).getId())));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}			
		});
		//On change of State
		spnState.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				spnCity.setAdapter(DataAdapter("city",String.valueOf(((CustomType)spnState.getSelectedItem()).getId())));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}			
		});
		//On Change of City
		spnCity.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				spnPinCode.setAdapter(DataAdapter("pincode",String.valueOf(((CustomType)spnCity.getSelectedItem()).getId())));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}			
		});

		//On Change of Email Id
		etLogInId.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean gainFocus) {
				// onFocus
				if (gainFocus) {

				}
				// onBlur
				else {
					if (etLogInId.getEditableText().toString().trim().length()!= 0)				
					{
						loginId = String.valueOf(etLogInId.getText());
						AsyncCheckLoginIdCall task = new AsyncCheckLoginIdCall();
						task.execute();
					}
				}
			}
		});

		//On Change of Email Id
		etEmail.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean gainFocus) {
				// onFocus
				if (gainFocus) {

				}
				// onBlur
				else {
					if (!etEmail.getEditableText().toString().trim().matches(emailPattern) && etEmail.getEditableText().toString().trim().length()!= 0)				
						common.showToast("Invalid email address", 5);				
				}
			}
		});


		//On Change of Contact Person Mobile
		etContactPersonMobile.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean gainFocus) {
				// onFocus
				if (gainFocus) {

				}
				// onBlur
				else {
					if(!isValidPhoneNumber(String.valueOf(etContactPersonMobile.getEditableText().toString()).trim()))
						common.showToast("Invalid Mobile");		
				}
			}
		});

		//On click of Customer Sign up
		btnCreateCustomer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(spnCustomerType.getSelectedItemPosition()==0)
					common.showToast("Customer Type is mandatory");
				else if(String.valueOf(etLogInId.getText()).trim().equals(""))
					common.showToast("Login Id is mandatory");
				else if(isLogInIdExist.equals("false"))
					common.showToast("User has already been registered");
				else if(String.valueOf(etCustomerName.getText()).trim().equals(""))
					common.showToast("Customer Name is mandatory");
				else if(String.valueOf(etContactPerson.getText()).trim().equals(""))
					common.showToast("Contatct Person is mandatory");
				else if(String.valueOf(etContactPersonMobile.getText()).trim().equals(""))
					common.showToast("Mobile# is mandatory");
				else if(!isValidPhoneNumber(String.valueOf(etContactPersonMobile.getText()).trim()))
					common.showToast("Invalid Mobile");				
				else if(String.valueOf(etEmail.getText()).trim().equals(""))
					common.showToast("Email is mandatory");
				else if (!etEmail.getEditableText().toString().trim().matches(emailPattern) && String.valueOf(etEmail.getText()).trim().length()!= 0)				
					common.showToast("Invalid email address");	
				else if(String.valueOf(etStreet.getText()).trim().equals(""))
					common.showToast("Street is mandatory");
				else if(spnCountry.getSelectedItemPosition()==0)
					common.showToast("Country is mandatory");
				else if(spnState.getSelectedItemPosition()==0)
					common.showToast("State is mandatory");
				else if(spnCity.getSelectedItemPosition()==0)
					common.showToast("City is mandatory");
				else if(spnPinCode.getSelectedItemPosition()==0)
					common.showToast("Pin Code is mandatory");
				else
				{
					custTypeId = String.valueOf(((CustomType)spnCustomerType.getSelectedItem()).getId());
					loginId = String.valueOf(etLogInId.getText());
					custName = String.valueOf(etCustomerName.getText());
					contactPerson = String.valueOf(etContactPerson.getText());
					mobile = String.valueOf(etContactPersonMobile.getText());
					street = String.valueOf(etStreet.getText());
					houseNo = String.valueOf(etHouseNo.getText());
					landMark = String.valueOf(etLandMark.getText());
					emailId = String.valueOf(etEmail.getText());
					stateId = String.valueOf(((CustomType)spnState.getSelectedItem()).getId());
					cityId = String.valueOf(((CustomType)spnCity.getSelectedItem()).getId());
					pinCodeId = String.valueOf(((CustomType)spnPinCode.getSelectedItem()).getId());
					//Call Async activity to send json to server for customer Validation
					if(common.isConnected())	
					{	
						FetchExternalIP task = new FetchExternalIP();
						task.execute();
					}
					else{
						dba.insertExceptions("Unable to connect to Internet !", "ActivityCustomerSignUp.java", "onCreate()");
					}
				}
			}
		});
	}

	//region Sync Masters Data
	private void SyncMastersData(){
		if(common.isConnected())
		{
			AsyncMasterWSCall task = new AsyncMasterWSCall();
			task.execute();		
		}
	}

	//For sending JSON data to create the customer
	private class FetchExternalIP extends AsyncTask<Void, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
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
				dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "FetchExternalIP");

				return null;
			} finally{
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "FetchExternalIP");
					}
				}
			}
		}
		@Override
		protected void onPostExecute(String result) {
			//Creation of JSON string
			JSONObject json = new JSONObject();
			try {
				json.put("custTypeId", custTypeId);
				json.put("loginId", loginId);
				json.put("custName", custName);
				json.put("contactPerson", contactPerson);
				json.put("mobile", mobile);
				json.put("emailId", emailId);
				json.put("street", street);
				json.put("houseNo", houseNo);
				json.put("landMark", landMark);
				json.put("stateId", stateId);
				json.put("cityId", cityId);
				json.put("pinCodeId", pinCodeId);
				json.put("createDate", getDateTime());
				json.put("ipAddr",deviceIP==null?"-":deviceIP);
				json.put("machine", imei);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "FetchExternalIP");
			}

			JSONStr=json.toString();
			AsyncCustomerSubmitWSCall task = new AsyncCustomerSubmitWSCall();
			task.execute();
			Dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Please Wait..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//Async Class to send Customer Data on server
	private class AsyncCustomerSubmitWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {
				responseJSON= common.invokeJSONWS(JSONStr,"json","InsertCustomerDetails",common.url );
			}
			catch (SocketTimeoutException e){
				dba.insertExceptions("TimeOut Exception. Internet is slow", "ActivityCustomerSignUp.java", "AsyncCustomerSubmitWSCall");
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				//e.printStackTrace();
				dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "AsyncCustomerSubmitWSCall");
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
					if(responseJSON.toLowerCase(Locale.US).contains("loginexist".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityCustomerSignUp.this, "User has already been registered!", false);
					}
					else if(responseJSON.contains("mobileexist"))
					{
						common.showAlert(ActivityCustomerSignUp.this, "Mobile# has already been registered!", false);
					}
					else if(responseJSON.contains("emailexist"))
					{
						common.showAlert(ActivityCustomerSignUp.this, "Email Id has already been registered!", false);
					}
					else
					{				
						common.showToast("Your request has been successfully submitted.\nYou will notify once your login id has been activated.");
						Intent	intent = new Intent(context, ActivityHomeScreen.class);
						startActivity(intent);
						finish();
					}
				}
				else
				{
					common.showAlertWithHomePage(ActivityCustomerSignUp.this, "Unable to login try again",false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "AsyncCustomerSubmitWSCall");
				common.showAlertWithHomePage(ActivityCustomerSignUp.this,"Error: "+e.getMessage(),false);
			}
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}

	//Async Class to check log in id
	private class AsyncCheckLoginIdCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {

				responseJSON= common.invokeJSONWS(loginId,"loginId","CheckLoginId",common.url );
			}
			catch (SocketTimeoutException e){
				dba.insertExceptions("TimeOut Exception. Internet is slow", "ActivityCustomerSignUp.java", "AsyncCheckLoginIdCall");
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "AsyncCheckLoginIdCall");
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
					if(responseJSON.toLowerCase(Locale.US).contains("loginexist".toLowerCase(Locale.US)))
					{
						common.showToast("User has already been registered!");
						isLogInIdExist =  false;
					}
					else 
						isLogInIdExist =  true;
				}

				else
				{
					common.showAlertWithHomePage(ActivityCustomerSignUp.this, "Unable to login try again",false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				dba.insertExceptions(e.getMessage(), "ActivityCustomerSignUp.java", "AsyncCheckLoginIdCall");
				common.showAlertWithHomePage(ActivityCustomerSignUp.this,"Error: "+e.getMessage(),false);
			}
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}

	//AysnTask class to handle Masters WS call as separate UI Thread
	private class AsyncMasterWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {	

				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadCustomerType", "0", "0"};
				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);	
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					//clearing all data
					dba.deleteAllData();
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String getId= jsonArray.getJSONObject(i).getString("A");
						String getName= jsonArray.getJSONObject(i).getString("B");
						dba.Insert_CustomerType(getId, getName);
					}		
					spnCustomerType.setAdapter(DataAdapter("customertype",""));
					dba.close();					
					if(common.isConnected())
					{
						AsyncCountryWSCall task = new AsyncCountryWSCall();
						task.execute();
					}
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCustomerSignUp.this, result, false);
				}
			} catch (Exception e) {
				dba.open();
				dba.DeleteMasterData("CustomerType");
				dba.close();
				e.printStackTrace();
				common.showAlert(ActivityCustomerSignUp.this,"Customer Type Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Customer Type..");
			Dialog.setCancelable(false);
			Dialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}

	//AysnTask class to handle Country WS call as separate UI Thread
	private class AsyncCountryWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {	

				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadCountry", "0", "0"};
				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String getId= jsonArray.getJSONObject(i).getString("A");
						String getName= jsonArray.getJSONObject(i).getString("B");
						dba.Insert_Country(getId, getName);
					}
					spnCountry.setAdapter(DataAdapter("country",""));
					dba.close();
					if(common.isConnected())
					{ 
						AsyncStateWSCall task = new AsyncStateWSCall();
						task.execute();
					}
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCustomerSignUp.this, result, false);
				}
			} catch (Exception e) {
				dba.open();
				dba.DeleteMasterData("Country");
				dba.close();
				e.printStackTrace();
				common.showAlert(ActivityCustomerSignUp.this,"Country Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Country..");
			Dialog.setCancelable(false);
			Dialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}

	//AysnTask class to handle State WS call as separate UI Thread
	private class AsyncStateWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadState", "0", "0"};
				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String getCountryId= jsonArray.getJSONObject(i).getString("A");
						String getId= jsonArray.getJSONObject(i).getString("B");
						String getName= jsonArray.getJSONObject(i).getString("C");
						dba.Insert_State(getId, getCountryId, getName);
					}
					dba.close();
					if(common.isConnected())
					{
						AsyncCityWSCall task = new AsyncCityWSCall();
						task.execute();
					}
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCustomerSignUp.this, result, false);
				}
			} catch (Exception e) {
				dba.open();
				dba.DeleteMasterData("State");
				dba.close();
				e.printStackTrace();
				common.showAlert(ActivityCustomerSignUp.this,"State Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading State..");
			Dialog.setCancelable(false);
			Dialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}

	//AysnTask class to handle SBU WS call as separate UI Thread
	private class AsyncCityWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadCity", "0", "0"};
				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String getStateId= jsonArray.getJSONObject(i).getString("A");
						String getId= jsonArray.getJSONObject(i).getString("B");
						String getName= jsonArray.getJSONObject(i).getString("C");
						dba.Insert_City(getId, getStateId, getName);
					}
					dba.close();
					if(common.isConnected())
					{
						AsyncPinCodeWSCall task = new AsyncPinCodeWSCall();
						task.execute();
					}
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCustomerSignUp.this, result, false);
				}
			} catch (Exception e) {
				dba.open();
				dba.DeleteMasterData("City");
				dba.close();
				e.printStackTrace();
				common.showAlert(ActivityCustomerSignUp.this,"City Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading City..");
			Dialog.setCancelable(false);
			Dialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}

	//AysnTask class to handle Pin Code WS call as separate UI Thread
	private class AsyncPinCodeWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCustomerSignUp.this);
		@Override
		protected String doInBackground(String... params) {
			try {	
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadPinCode", "0", "0"};
				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			}
		}
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String getCityId= jsonArray.getJSONObject(i).getString("A");
						String getId= jsonArray.getJSONObject(i).getString("B");
						String getName= jsonArray.getJSONObject(i).getString("C");
						dba.Insert_PinCode(getId,getCityId, getName);
					}
					dba.close();				
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCustomerSignUp.this, result, false);
				}
			} catch (Exception e) {
				dba.open();
				dba.DeleteMasterData("PinCode");
				dba.close();
				e.printStackTrace();
				common.showAlert(ActivityCustomerSignUp.this,"Pin Code Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Pin Code..");
			Dialog.setCancelable(false);
			Dialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}
	//For binding masters data
	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter)
	{
		dba.open();
		List <CustomType> lables = dba.GetMasterDetails(masterType, filter);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this,android.R.layout.simple_spinner_item, lables);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dba.close();
		return dataAdapter;
	}
	//For Checking Valid Phone Number
	public static final boolean isValidPhoneNumber(CharSequence target) {
		if (target.length()!=10) {
			return false;
		} else {
			return android.util.Patterns.PHONE.matcher(target).matches();
		}
	}
	//Method to get current date time
	public String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.US);
		Date date = new  Date();
		return dateFormat.format(date);
	}

	// When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent homeScreenIntent = new Intent(this, ActivityLogin.class);
		homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeScreenIntent);
		finish();
	}


	//When press back button go to home screen
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, ActivityLogin.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(intent);
			finish();
			return true;

		case R.id.action_go_to_home: 
			Intent homeScreenIntent = new Intent(this, ActivityLogin.class);
			homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreenIntent);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
