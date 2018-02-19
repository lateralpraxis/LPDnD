package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ActivityAdminHomeScreen  extends Activity{
	private String userRole, password, userId, loginId,customerType,responseJSON;
	private TextView tvHeader;
	final Context context = this;
	static final int ITEM_PER_ROW = 2;
	private Intent intent;
	Common common;
	private UserSessionManager session;
	HashMap<String, String> map = null;
	private String imei;
	private DatabaseAdapter dba;
	Button go, btn;
	TableLayout tl;
	TableRow tr;
	List<Integer> views = Arrays.asList(
			R.layout.btn_report);
	LinearLayout btnLayout;
	// Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_home);

		// To create object of user session
		session = new UserSessionManager(getApplicationContext());

		// To create object of common class
		common = new Common(getApplicationContext());

		// To create object of database
		dba = new DatabaseAdapter(getApplicationContext());
		/*try {
			common.copyDBToSDCard("ganeshdairy.db");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			dba.open();
			dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
					"DataBackup Issue");
			dba.close();
			// e.printStackTrace();
		}*/
		 /*--------Start of Code to find controls -----------------------------*/
		tvHeader = (TextView) findViewById(R.id.tvHeader);
		go = (Button) findViewById(R.id.btnGo);
		tl = (TableLayout) findViewById(R.id.tlmainMenu);
		tl.setColumnStretchable(0, true);
		tl.setColumnStretchable(1, true);
    /*--------End of Code to find controls -----------------------------*/
		if (session.checkLogin())
			finish();
		else {
			// To store required information of login user
			imei = common.getIMEI();
			final HashMap<String, String> user = session.getLoginUserDetails();
			tvHeader = (TextView) findViewById(R.id.tvHeader);
			userId = user.get(UserSessionManager.KEY_ID);
			loginId = user.get(UserSessionManager.KEY_CODE);
			password = user.get(UserSessionManager.KEY_PWD);
			userRole = user.get(UserSessionManager.KEY_ROLES);
			customerType = user.get(UserSessionManager.KEY_CUSTOMERTYPE);

			tvHeader.setText("Welcome, "
					+ user.get(UserSessionManager.KEY_USERNAME) + " [ "
					+ Html.fromHtml(userRole.replace(",", ", ")) + " ]");
		}


		if (userRole.contains("System User"))
			views = Arrays.asList(
					R.layout.btn_delivery,
					R.layout.btn_payment,
					R.layout.btn_cashdeposit,
					R.layout.btn_report);
		else
			views = Arrays.asList(
					R.layout.btn_report);

		go.performClick();

			/*try {
			common.copyDBToSDCard("ganeshdairy.db");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			dba.open();
			dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
					"DataBackup Issue");
			dba.close();
			// e.printStackTrace();
		}*/
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnGo:
				int index = 0;
				tl.removeAllViews();
				while (index < views.size()) {
					tr = new TableRow(this);
					TableLayout.LayoutParams trParams =
							new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
									TableLayout.LayoutParams.WRAP_CONTENT);
					trParams.setMargins(0, 8, 0, 0);
					tr.setLayoutParams(trParams);
					tr.setId(index + 1);
					tr.setWeightSum(1);
					for (int k = 0; k < ITEM_PER_ROW; k++) {
						if (index < views.size()) {
							btnLayout = createButton(views.get(index));
							if (k == 0)
								btnLayout.setPadding(8, 2, 4, 0);
							else
								btnLayout.setPadding(4, 2, 8, 0);
							tr.addView(btnLayout);
							index++;
						}
					}
					tl.addView(tr);
				}
				break;
		}
	}

	private LinearLayout createButton(int resource) {
		btnLayout = (LinearLayout) View.inflate(this, resource, null);
		switch (resource) {
			case R.layout.btn_delivery:
				btn = (Button) btnLayout.findViewById(R.id.btnDeliveryView);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityDeleteDeliveryList.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_cashdeposit:
				btn = (Button) btnLayout.findViewById(R.id.btnCashDepositView);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if(common.isConnected())
						{
							AsyncCashDepositWSCall task = new AsyncCashDepositWSCall();
							task.execute();
						}
					}
				});
				break;
			case R.layout.btn_payment:
				btn = (Button) btnLayout.findViewById(R.id.btnPayment);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityDeletePaymentList.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_report:
				btn = (Button) btnLayout.findViewById(R.id.btnReports);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityReport.class);
						intent.putExtra("From", "Admin");
						startActivity(intent);
						finish();
					}
				});
				break;
			default:
				break;
		}

		return btnLayout;
	}

	// To create menu of logout on top of screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu_home_screen, menu);
		return true;
	}

	// Event hander of logout menu on top of screen
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_logout) {

			// "Log Out",
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			// set title
			alertDialogBuilder.setTitle("Confirmation");
			// set dialog message
			alertDialogBuilder
					.setMessage("Are you sure, you want to Log Out ?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int id) {
									if (common.isConnected()) {
										dba.open();
										if (dba.IslogoutAllowed()) {
											dba.deleteAllData();
											dba.close();
											AsyncLogOutWSCall task = new AsyncLogOutWSCall();
											task.execute();
										} else {
											common.showToast("Can not logout as data is pending to synchronizing.");
										}
									} else {
										common.showToast("Unable to connect to Internet !");
									}
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int id) {
									// if this button is clicked, just close
									dialog.cancel();
								}
							});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return true;
		} else if (id == R.id.action_changelanguage) {
			ArrayList<String> myLanguagelist = new ArrayList<String>();
			myLanguagelist.clear();
			final HashMap<String, String> user = session.getLoginUserDetails();
			String[] items = user.get(UserSessionManager.KEY_OPTLANG)
					.split(",");

			for (String st : items) {
				if (st.equals("English")) {
					myLanguagelist.add(getResources().getString(
							R.string.lang_eng));
				} else if (st.equals("Hindi")) {
					myLanguagelist.add(getResources().getString(
							R.string.lang_hindi));
				}
			}

			final AlertDialog.Builder builderSingle = new AlertDialog.Builder(
					ActivityAdminHomeScreen.this);
			builderSingle.setTitle(getResources().getString(
					R.string.change_language));

			final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
					ActivityAdminHomeScreen.this,
					android.R.layout.select_dialog_singlechoice, myLanguagelist);

			// radio button highlight
			int selected = 0;
			String lang = session.getDefaultLang();

			if (lang.equalsIgnoreCase("en")) {
				selected = 0;

			} else if (lang.equalsIgnoreCase("hi")) {
				selected = 1;

			}

			builderSingle.setSingleChoiceItems(arrayAdapter, selected,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {

							String setLang = arrayAdapter.getItem(item);

							String langCode = "";
							if (setLang.equalsIgnoreCase(getResources()
									.getString(R.string.lang_eng))) {
								langCode = "en";

							} else if (setLang.equalsIgnoreCase(getResources()
									.getString(R.string.lang_hindi))) {
								langCode = "hi";

							}

							session.updatePrefLanguage(langCode);
							dialog.cancel();

							Locale myLocale = new Locale(langCode);
							Resources res = getResources();
							DisplayMetrics dm = res.getDisplayMetrics();
							Configuration conf = res.getConfiguration();
							conf.locale = myLocale;
							res.updateConfiguration(conf, dm);
							common.showToast("You have selected " + setLang);
							Intent refresh = new Intent(context,
									ActivityAdminHomeScreen.class);
							startActivity(refresh);

						}
					});
			builderSingle.show();
		} else if (id == R.id.action_changepassword) {
			Intent i = new Intent(ActivityAdminHomeScreen.this,
					ActivityChangePassword.class);
			i.putExtra("fromwhere", "home");
			startActivity(i);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}


	// Code on Activity Resume
	@Override
	protected void onResume() {
		super.onResume();
		String lang = session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
	}

	// When press back button go to home screen
	@Override
	public void onBackPressed() {
		common.BackPressed(this);
	}

	// To make web service class to logout user from login
	private class AsyncLogOutWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityAdminHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				responseJSON = "";
				JSONObject json = new JSONObject();
				json.put("username", loginId);
				json.put("password", password);
				json.put("imei", imei);
				json.put("role", userRole);
				// To invoke json method to logout user
				responseJSON = common.invokeJSONWS(json.toString(), "json",
						"LogoutUserAndroid", common.url);
			} catch (SocketTimeoutException e) {
				dba.open();
				dba.insertExceptions("TimeOut Exception. Internet is slow",
						"ActivityAdminHomeScreen.java", "AsyncLogOutWSCall");
				dba.close();
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				dba.open();
				dba.insertExceptions(e.getMessage(), "ActivityAdminHomeScreen.java",
						"AsyncLogOutWSCall");
				dba.close();
				return "ERROR: " + e.getMessage();
			}
			return responseJSON;
		}

		// After execution of web service to logout user
		@Override
		protected void onPostExecute(String result) {
			try {

				// To display message after response from server
				if (result.contains("success")) {
					session.logoutUser();
					common.showToast("You have been logged out successfully!");
					finish();
				} else {
					common.showAlert(ActivityAdminHomeScreen.this,
							"Unable to get response from server.", false);
				}
			} catch (Exception e) {
				dba.open();
				dba.insertExceptions(e.getMessage(), "ActivityAdminHomeScreen.java",
						"AsyncLogOutWSCall");
				dba.close();
				common.showAlert(ActivityAdminHomeScreen.this, "Log out failed: "
						+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Logging out ..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Web Service to Fetch Cash Deposit Data For Delete
	private class AsyncCashDepositWSCall extends
			AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityAdminHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {

				String[] name = { };
				String[] value = {  };
				// Call method of web service to Read Cash Deposit Data For Delete
				responseJSON = "";
				responseJSON = common.CallJsonWS(name, value,"GetCashDepositDataForDelete", common.url);
				return "";
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: " + e.getMessage();
			}

		}

		// After execution of product web service
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					dba.open();
					dba.DeleteMasterData("CashDepositDeleteData");
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					if (jsonArray.length() > 0) {

						// inserting data into CashDepositDeleteData
						for (int i = 0; i < jsonArray.length(); ++i) {

							dba.Insert_CashDepositDeleteData(jsonArray.getJSONObject(i)
									.getString("CashDepositId"),jsonArray.getJSONObject(i)
									.getString("CashDepositDetailId"),jsonArray.getJSONObject(i)
									.getString("DepositDate"),jsonArray.getJSONObject(i)
									.getString("PCDetailId"),jsonArray.getJSONObject(i)
									.getString("Mode"),jsonArray.getJSONObject(i)
									.getString("Amount"),jsonArray.getJSONObject(i)
									.getString("FullName"));
						}
						dba.close();
						intent = new Intent(context, ActivityCashDepositView.class);
						startActivity(intent);
						finish();
					} else {
						common.showToast("There is no data available for deleting cash deposit!");
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityAdminHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityAdminHomeScreen.this,
						"Cash Deposit Downloading failed: " + e.toString(),
						false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Cash Deposit Data..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
}
