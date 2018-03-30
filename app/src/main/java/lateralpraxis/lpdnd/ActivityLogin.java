package lateralpraxis.lpdnd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

public class ActivityLogin extends Activity {
	//private static final String LOG = "LPDnD";
	Button btnLogin,btnRegistration;
	EditText etUsername, etPassword;
	private DatabaseAdapter dba;
	final Context context = this;
	private String username="", password="";
	private String JSONStr;
	private static String responseJSON;
	Common common;
	UserSessionManager session;
	String imei;
	private CheckBox ckShowPass;
	private DatabaseAdapter databaseAdapter;
	//private TextView tvIsUAT;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//To create instance of common page 
		common = new Common(getApplicationContext());

		//To create instance of database 
		dba=new DatabaseAdapter(this);

		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());

		//To create instance of database 
		databaseAdapter=new DatabaseAdapter(getApplicationContext());

		imei = common.getIMEI();
		//To check user login session is exist or not
		if(session.checkLoginShowHome())
		{
			String lang= session.getDefaultLang();
			Locale myLocale = new Locale(lang);
			Resources res = getResources();
			DisplayMetrics dm = res.getDisplayMetrics();
			Configuration conf = res.getConfiguration();
			conf.locale = myLocale;
			res.updateConfiguration(conf, dm);
			String userRole="";
			final HashMap<String, String> user = session.getLoginUserDetails();
			userRole = user.get(UserSessionManager.KEY_ROLES);
			if(userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") || userRole.contains("Reconciliation User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant")))
			{
				//Open Administrator home page screen
				Intent	intent = new Intent(context, ActivityAdminHomeScreen.class);
				startActivity(intent);
			}
			else
			{
			//Open home page screen
			Intent	intent = new Intent(context, ActivityHomeScreen.class);
			startActivity(intent);
			}
			finish();
		}

		//To create object of control 
		etUsername = (EditText)findViewById(R.id.etUsername);
		etPassword =(EditText)findViewById(R.id.etPassword);
		btnLogin=(Button)findViewById(R.id.btnLogin);
		btnRegistration=(Button)findViewById(R.id.linkbtnNewUser);
		ckShowPass= (CheckBox) findViewById(R.id.ckShowPass);
		//tvIsUAT = (TextView)findViewById(R.id.tvIsUAT);
		etPassword.getParent().getParent().requestChildFocus(etPassword,etPassword);

		//Required to show if UAT is ON
		//if(common.domain.equals("http://IPAddress"))
		//tvIsUAT.setVisibility(View.VISIBLE);
		//else
		//tvIsUAT.setVisibility(View.GONE);

		//On click of login button handler 
		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//To clear database
				dba.openR();
				dba.deleteAllData();
				//to validate required fields
				if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
					etUsername.setError("Please Enter Username");
					etUsername.requestFocus();
				}
				else if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
					etPassword.setError("Please Enter Password");
					etPassword.requestFocus();
				}
				else
				{
					username = etUsername.getText().toString().trim();
					password = etPassword.getText().toString().trim();

					try
					{	
						//Call activity to send json to server for login validation
						if(common.isConnected())	
						{	
							/*//Call method to get IP address of service provider
							FetchExternalIP task = new FetchExternalIP();
							task.execute();*/
							//Creation of JSON string
							JSONObject json = new JSONObject();
							try {
								json.put("username", username);
								json.put("password", password);
								json.put("imei", imei);
								json.put("ipAddr",common.getDeviceIPAddress(true));
								json.put("version",databaseAdapter.getVersion());
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "FetchExternalIP");
							}

							JSONStr=json.toString();

							//Call method of web service for user login
							String[] params = { JSONStr };
							AsyncLoginWSCall task = new AsyncLoginWSCall();
							task.execute(params);
						}
						else{
							databaseAdapter.insertExceptions("Unable to connect to Internet !", "ActivityLogin.java", "onCreate()");
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						common.showToast("Unable to get response from server.");
						databaseAdapter.insertExceptions("Unable to get response from server.", "ActivityLogin.java", "onCreate()");
					}
				}
			}
		});

		//On click of Registration button handler 
		btnRegistration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent	intent = new Intent(context, ActivityCustomerSignUp.class);
				startActivity(intent);
				finish();
			}
		});
		//To set mandatory sign of user name field
		etUsername.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(s.length()>0)
				{ 
					etUsername.setError(null);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});

		//To set mandatory sign of user password field
		etPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(s.length()>0)
				{ 
					etPassword.setError(null);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});

		//To show password in readable format so that user can check it is correct or not
		ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int start,end;

				if(!isChecked){
					start=etPassword.getSelectionStart();
					end=etPassword.getSelectionEnd();
					etPassword.setTransformationMethod(new PasswordTransformationMethod());
					etPassword.setSelection(start,end);
				}else{
					start=etPassword.getSelectionStart();
					end=etPassword.getSelectionEnd();
					etPassword.setTransformationMethod(null);
					etPassword.setSelection(start,end);
				}

			}
		});
	}

	//Make method of web service for credentials
	private class AsyncLoginWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityLogin.this);
		@Override
		protected String doInBackground(String... params) {
			try {					
				responseJSON= common.invokeJSONWS(params[0],"json","GetUserDetails",common.url );
			}
			catch (SocketTimeoutException e){
				databaseAdapter.insertExceptions("TimeOut Exception. Internet is slow", "ActivityLogin.java", "AsyncLoginWSCall");
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "AsyncLoginWSCall");
				return "ERROR: "+ e.getMessage();
			} 
			return responseJSON;
		}

		//After execution of web service for credentials
		@Override
		protected void onPostExecute(String result) {
			Dialog.dismiss();
			try {				
				if(!result.contains("ERROR: "))
				{
					//To display message after response from server
					if(responseJSON.toLowerCase(Locale.US).contains("NOVERSION".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Application running is older version. Please install latest version!", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("DEFAULT_LOGIN_FAILED".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Invalid Username or Password", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("NO_USER".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "There is no user in the system as - "+ etUsername.getText().toString(), false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("NO_ROLE".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Assigned role is not allowed to access application.", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("BARRED".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Your account has been barred by the Administrator.", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("LOCKED".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Your account has been locked out because " +
								"you have exceeded the maximum number of incorrect login attempts. " +
								"Please contact administrator to unblock your account.", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("LOGINFAILED".toLowerCase(Locale.US)))
					{
						String[] resp = responseJSON.split("~");
						common.showAlert(ActivityLogin.this, "Invalid password. " +
								"Please remember Password is case-sensitive. " +
								"Access to the system will be disabled after " + resp[1] + " " +
								"consecutive wrong attempts.\n" +
								"Number of Attempts remaining: " + resp[2] , false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("LoginFailed".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Invalid Username or Password", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("norole".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "No role assigned to this user.", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("imeiexists".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "You have already logged into the app from another device.", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("Error".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Error Unable to get response from server.", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("alreadylogin".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityLogin.this, "Kindly log out from the portal to access this app.", false);
					}
					else
					{
						//To extract data after server response 
						JSONObject reader = new JSONObject(responseJSON);
						String id = reader.getString("Id");
						String code = reader.getString("Code");
						String name = reader.getString("Name");
						String membershipId = reader.getString("MembershipId");
						String customerTypeId = reader.getString("CustomerTypeId");
						String customerType = reader.getString("CustomerType");
						String routeId = reader.getString("RouteId");
						String route = reader.getString("Route");
						String vehicleId = reader.getString("VehicleId");
						String role =  reader.getString("Role").contains("Route Officer")?"Route Officer":reader.getString("Role");//.length()<2?"Customer":reader.getString("Role");
						String passExpired = reader.getString("PassExpired");
						session.createUserLoginSession(id,code,name,membershipId,customerTypeId, customerType, routeId, route,role,imei,password,"English,Hindi", vehicleId);
						databaseAdapter.open();
						databaseAdapter.deleteAllData();
						databaseAdapter.close();

						//Check is password expired?
						if(passExpired.toLowerCase(Locale.US).equals("yes"))
						{
							//Open page to change password
							Intent	intent = new Intent(context, ActivityChangePassword.class);
							intent.putExtra("fromwhere", "login");
							startActivity(intent);
							finish();
						}
						else
						{
							String userRole="";
							final HashMap<String, String> user = session.getLoginUserDetails();
							userRole = user.get(UserSessionManager.KEY_ROLES);
						 
							if(userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant")))
							{
								//Open Administrator home page screen
								Intent	intent = new Intent(context, ActivityAdminHomeScreen.class);
								startActivity(intent);
								finish();
							}
							else
							{
							//Open home page screen
							Intent	intent = new Intent(context, ActivityHomeScreen.class);
							startActivity(intent);
							finish();
							}
						}
					}
				}
				else
				{
					common.showAlert(ActivityLogin.this, "Unable to get response from server.",false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "AsyncLoginWSCall");
				common.showAlert(ActivityLogin.this,"Error: "+"Server not responding",false);
			}
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Validating your credentials..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	String deviceIP;
	//Class of web service for fetching IP address of service provider
	private class FetchExternalIP extends AsyncTask<Void, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityLogin.this);
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

				// Create the request to open the connection
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
				databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "FetchExternalIP");

				return null;
			} finally{
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "FetchExternalIP");
					}
				}
			}
		}

		//After execution of web service for fetching IP address
		@Override
		protected void onPostExecute(String result) {
			//Creation of JSON string
			JSONObject json = new JSONObject();
			try {
				json.put("username", username);
				json.put("password", password);
				json.put("imei", imei);
				json.put("ipAddr",deviceIP==null?"-":deviceIP);
				json.put("version",databaseAdapter.getVersion());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				databaseAdapter.insertExceptions(e.getMessage(), "ActivityLogin.java", "FetchExternalIP");
			}

			JSONStr=json.toString();

			//Call method of web service for user login
			String[] params = { JSONStr };
			AsyncLoginWSCall task = new AsyncLoginWSCall();
			task.execute(params);
			Dialog.dismiss();
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Please Wait..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// When press back button go to home screen
	@Override
	public void onBackPressed() {
		common.BackPressed(this);
	}
}


