package lateralpraxis.lpdnd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Locale;

public class ActivityChangePassword extends Activity {
    private static String responseJSON;
    TextView tvInstructions,tvPasswordExpired;
	EditText etOldPassword, etNewPassword, etConfirmPassword;
	CheckBox ckShowPass;
	Button btnChangePassword;
    private Common common;
    private String JSONStr;
	private UserSessionManager session;
	private DatabaseAdapter databaseAdapter;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);		
		common = new Common(this);
		session = new UserSessionManager(this);
		databaseAdapter=new DatabaseAdapter(getApplicationContext());

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of object
		tvInstructions = (TextView)findViewById(R.id.tvInstructions);
		ckShowPass = (CheckBox)findViewById(R.id.ckShowPass);
		tvPasswordExpired = (TextView)findViewById(R.id.tvPasswordExpired);
		etOldPassword = (EditText)findViewById(R.id.etOldPassword);
		etNewPassword = (EditText)findViewById(R.id.etNewPassword);
		etConfirmPassword = (EditText)findViewById(R.id.etConfirmPassword);
		btnChangePassword = (Button)findViewById(R.id.btnChangePassword);


		//To enabled back menu



		//To display password expire message
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{			
			if(extras.getString("fromwhere").equals("home"))
				tvPasswordExpired.setVisibility(View.GONE);
			else
				tvPasswordExpired.setVisibility(View.VISIBLE);
		}

		//To display information for making password 
		tvInstructions.setText(Html.fromHtml(
				"&#8226; Password must be at least 8 characters long<br>" +
						/*"&#8226; Password must contain two lower case alphabets<br>" +
						"&#8226; Password must contain two upper case alphabets<br>" +*/
						"&#8226; Password must contain a numeric character<br>" +
						"&#8226; Password must contain a special character<br>" +
						"&#8226; Password must not repeat a character more than half the length of the password<br>" +
				"&#8226; Both passwords must match<br>"));

		//On click of change password button
		btnChangePassword.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// To validate required field
				if(String.valueOf(etOldPassword.getText()).trim().length() == 0)
					common.showToast("Old Password is mandatory");
				else if(String.valueOf(etNewPassword.getText()).trim().length() == 0)
					common.showToast("New Password is mandatory");
				else if(etConfirmPassword.getText().toString().trim().length() == 0)
					common.showToast("Confirm Password is mandatory");
				else if(etNewPassword.getText().toString().trim().length() < 8)
					common.showToast("New Password should be at least 8 characters long");
				else if(etConfirmPassword.getText().toString().trim().length() < 8)
					common.showToast("Confirm Password should be at least 8 characters long");
				if(String.valueOf(etOldPassword.getText()).trim().equals(String.valueOf(etNewPassword.getText()).trim()))
					common.showToast("New and Old Password cannot be same.");
				else if(!(etConfirmPassword.getText().toString().trim().equals(String.valueOf(etNewPassword.getText()).trim())))
					common.showToast("New and Confirm Password should match");
				else
				{
					//To extract user information 
					HashMap<String, String> user = session.getLoginUserDetails();

					try
					{	
						if(common.isConnected())	
						{	
							//To make json object 
							JSONObject json = new JSONObject();
							try {
								json.put("username", user.get(UserSessionManager.KEY_CODE));
								json.put("oldPassword", String.valueOf(etOldPassword.getText()).trim());
								json.put("newPassword", String.valueOf(etNewPassword.getText()).trim());
								json.put("imei", common.getIMEI());
								json.put("ipAddr","-");
								json.put("version",databaseAdapter.getVersion());
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();

							}
							JSONStr=json.toString();

							//Call the change password web service
							AsyncChangePasswordWSCall task = new AsyncChangePasswordWSCall();
							task.execute();

						}
						else{

						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						common.showToast("Unable to get response from server.");
					}

				}
			}
		});

		//To display password so that user can check it is correct or not
		ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			//When click of on check changed event
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int oldStart,oldEnd;
				int newStart,newEnd;
				int confirmStart,confirmEnd;

				if(!isChecked)
				{
					oldStart=etOldPassword.getSelectionStart();
					oldEnd=etOldPassword.getSelectionEnd();
                    etOldPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etOldPassword.setSelection(oldStart, oldEnd);

					newStart=etNewPassword.getSelectionStart();
					newEnd=etNewPassword.getSelectionEnd();
                    etNewPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etNewPassword.setSelection(newStart, newEnd);

					confirmStart=etConfirmPassword.getSelectionStart();
					confirmEnd=etConfirmPassword.getSelectionEnd();
                    etConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    etConfirmPassword.setSelection(confirmStart, confirmEnd);
                }
				else
				{
					oldStart=etOldPassword.getSelectionStart();
					oldEnd=etOldPassword.getSelectionEnd();
					etOldPassword.setTransformationMethod(null);
					etOldPassword.setSelection(oldStart,oldEnd);

					newStart=etNewPassword.getSelectionStart();
					newEnd=etNewPassword.getSelectionEnd();
					etNewPassword.setTransformationMethod(null);
					etNewPassword.setSelection(newStart,newEnd);

					confirmStart=etConfirmPassword.getSelectionStart();
					confirmEnd=etConfirmPassword.getSelectionEnd();
					etConfirmPassword.setTransformationMethod(null);
					etConfirmPassword.setSelection(confirmStart,confirmEnd);
				}
			}
		});

	}

    //When press back button go to home screen
    @Override
    public void onBackPressed() {
        String userRole = "";
        final HashMap<String, String> user = session.getLoginUserDetails();
        userRole = user.get(UserSessionManager.KEY_ROLES);
        Intent homeScreenIntent;
        if (userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant"))) {
            homeScreenIntent = new Intent(this, ActivityAdminHomeScreen.class);
        } else {
            homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
        }
        homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeScreenIntent);
    }

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
                String userRole = "";
                final HashMap<String, String> user = session.getLoginUserDetails();
                userRole = user.get(UserSessionManager.KEY_ROLES);

                if (userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant"))) {
                    //Open Administrator home page screen
                    Intent intent = new Intent(this, ActivityAdminHomeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    //Open home page screen
                    Intent intent = new Intent(this, ActivityHomeScreen.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            case R.id.action_go_to_home:
                String userRolenew = "";
                final HashMap<String, String> usernew = session.getLoginUserDetails();
                userRolenew = usernew.get(UserSessionManager.KEY_ROLES);

                if (userRolenew.contains("System User") || userRolenew.contains("Centre User") || userRolenew.contains("MIS User") || userRolenew.contains("Management User") && (!userRolenew.contains("Route Officer") || !userRolenew.contains("Collection Officer") || !userRolenew.contains("Accountant"))) {
                    //Open Administrator home page screen
                    Intent intent = new Intent(this, ActivityAdminHomeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    //Open home page screen
                    Intent intent = new Intent(this, ActivityHomeScreen.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	//Make method of web service for changing user password
	private class AsyncChangePasswordWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityChangePassword.this);
		@Override
		protected String doInBackground(String... params) {
            try {
                responseJSON= common.invokeJSONWS(JSONStr,"json","ChangeUserPassword",common.url );
			}
			catch (SocketTimeoutException e)
			{
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();

				return "ERROR: "+ e.getMessage();
            }
            return "";
		}
		//After execution of web service for changing user password
		@Override
		protected void onPostExecute(String result) {
			Dialog.dismiss();
			try {
				//To display message after response from server
				if(!result.contains("ERROR: "))
				{
					if(responseJSON.toLowerCase(Locale.US).contains("NOVERSION".toLowerCase(Locale.US)))
					{
						AlertDialog.Builder alertbox = new AlertDialog.Builder(ActivityChangePassword.this);
						alertbox.setTitle("Alert");
						alertbox.setCancelable(false);
						alertbox.setMessage("Application running is older version. Please install latest version!");
						alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(common.isConnected())
								{
									AsyncLogOutWSCall task = new AsyncLogOutWSCall();
                                    task.execute();
                                }
								else{
									databaseAdapter.insertExceptions("Unable to connect to Internet !", "ActivityHomeScreen.java","onCreate()");
								}
							}
						});

                        alertbox.show();
                    }
					else if(responseJSON.toLowerCase(Locale.US).contains("LoginFailed".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityChangePassword.this, "Invalid Old Password", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("REPEAT_PASSWORD".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityChangePassword.this, "You cannot repeat last "+responseJSON.split("~")[1]+" passwords", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("SHOW_RULES".toLowerCase(Locale.US)))
					{
						common.showAlert(ActivityChangePassword.this, "Your password is not as per required rule", false);
					}
					else if(responseJSON.toLowerCase(Locale.US).contains("SUCCESS".toLowerCase(Locale.US)))
					{
						session.updatePassword(etNewPassword.getText().toString().trim());
						etOldPassword.setText("");
						etNewPassword.setText("");
						etConfirmPassword.setText("");
						common.showToast("Password Changed Successfully!");
						String userRole="";
						final HashMap<String, String> user = session.getLoginUserDetails();
						userRole = user.get(UserSessionManager.KEY_ROLES);
						Intent homeScreenIntent;
						if(userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant")))
							homeScreenIntent = new Intent(ActivityChangePassword.this, ActivityAdminHomeScreen.class);
						else
							homeScreenIntent = new Intent(ActivityChangePassword.this, ActivityHomeScreen.class);
						homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(homeScreenIntent);
					}
				}

				else
				{
					common.showAlert(ActivityChangePassword.this, "Unable to login try again",false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				common.showAlert(ActivityChangePassword.this,"Error: "+"Unable to get response from server.",false);
			}
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Changing your password..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//Make method of web service for logout user from login
	private class AsyncLogOutWSCall extends AsyncTask<String, Void, String> {
		String responseJSON="";
        private ProgressDialog Dialog = new ProgressDialog(ActivityChangePassword.this);

		@Override
		protected String doInBackground(String... params) {
			try {	
				HashMap<String, String> user = session.getLoginUserDetails();
				//Creation of JSON string
				JSONObject json = new JSONObject();
				json.put("username", user.get(UserSessionManager.KEY_USERNAME));
				json.put("password", user.get(UserSessionManager.KEY_PWD));
				json.put("imei", common.getIMEI());

				responseJSON = common.invokeJSONWS(json.toString(),"json","LogoutUserAndroid",common.url);
			}
			catch (SocketTimeoutException e){
				databaseAdapter.insertExceptions("TimeOut Exception. Internet is slow", "ActivityHomeScreen.java","AsyncLogOutWSCall");
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				databaseAdapter.insertExceptions(e.getMessage(), "ActivityChangePassword.java","AsyncLogOutWSCall");
				return "ERROR: "+ e.getMessage();
			} 
			return responseJSON;
		}

		//After execution of web service for logout user from login
		@Override
		protected void onPostExecute(String result) {
			try {
				//To display message after response from server
				if(result.contains("success"))
				{
					session.logoutUser();
					String lang= "en";
					Locale myLocale = new Locale(lang);
					Resources res = getResources();
					DisplayMetrics dm = res.getDisplayMetrics();
					Configuration conf = res.getConfiguration();
					conf.locale = myLocale;
					res.updateConfiguration(conf, dm);
					common.showToast("You have been logged out successfully!");
					finish();
				}
				else
				{
					common.showAlert(ActivityChangePassword.this, "Unable to get response from server.",false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				databaseAdapter.insertExceptions(e.getMessage(), "ActivityChangePassword.java","AsyncLogOutWSCall");
				common.showAlert(ActivityChangePassword.this,"Log out failed: " +"Unable to get response from server.",false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Logging out ..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
}


