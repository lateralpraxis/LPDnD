package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import lateralpraxis.lpdnd.ActivityCashDeposit.CustomAdapter;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityCashDepositDelete extends Activity {
	/*Start of code for Variable Declaration*/
	private String lang,userId,cashDepositId,data,total,responseJSON;
	private int listSize = 0;
	/*End of code for Variable Declaration*/
	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	private UserSessionManager session;
	private final Context mContext = this;
	/*End of code to declare class*/

	/*Start of code to declare Controls*/
	private TextView tvName,tvDate,tvCode,tvTotalAmount;
	private ListView listViewMain;
	private Button btnDelete;
	/*End of code to declare Controls*/
	//On create method similar to page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Code to set layout
		setContentView(R.layout.cash_deposit_delete_view);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());
		/*		*/
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);

		/*Code to get data from posted page*/
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			cashDepositId = extras.getString("CashDepositId");
		}

		//Start of code to find Controls
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvName = (TextView) findViewById(R.id.tvName);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvCode = (TextView) findViewById(R.id.tvCode);
		tvTotalAmount= (TextView) findViewById(R.id.tvTotalAmount);
		btnDelete= (Button) findViewById(R.id.btnDelete);
		tvCode.setText("CD"+cashDepositId);
		db.openR();
		data = db.GetCashDepositHeaderData(cashDepositId);
		tvDate.setText(common.convertToDisplayDateFormat(data.split("~")[0].toString()));
		tvName.setText(data.split("~")[1].toString());

		db.openR();
		ArrayList<HashMap<String, String>> listData = db.getCashDepositListForDelete(cashDepositId);

		listSize = listData.size();
		if (listSize != 0) {
			listViewMain.setAdapter(new ReportListAdapter(mContext, listData));

			ViewGroup.LayoutParams params = listViewMain.getLayoutParams();
			//params.height = 500;
			listViewMain.setLayoutParams(params);
			listViewMain.requestLayout();
		} else {
			listViewMain.setAdapter(null);
		}
		db.openR();
		total = db.GetTotalCashDepositHeaderData(cashDepositId);
		tvTotalAmount.setText(Html.fromHtml("<b>Total </b>"+common.convertToTwoDecimal(total)));

		// Event hander to delete Cash Deposit
		btnDelete.setOnClickListener(new View.OnClickListener() {
			// When create button click
			@Override
			public void onClick(View arg0) {

				Builder alertDialogBuilder = new Builder(mContext);
				alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
				alertDialogBuilder
				.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं,आप इस नकद जमा को हटाना चाहते हैं?":"Are you sure, you want to delete this cash deposit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int id) {
						if(common.isConnected())
						{
							AsyncDeleteCashDepositWSCall task = new AsyncDeleteCashDepositWSCall();
							task.execute();
						}
					}	            	
				}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}	            	
				});
				alertDialogBuilder.create().show();
			}

		});
	}

	//<editor-fold desc="Code Binding Data In List">
	public static class viewHolder {
		TextView tvModeHead, tvAmount;
		int ref;
	}

	private class ReportListAdapter extends BaseAdapter {
		LayoutInflater inflater;
		ArrayList<HashMap<String, String>> _listData;
		String _type;
		private Context context2;

		public ReportListAdapter(Context context,
				ArrayList<HashMap<String, String>> listData) {
			this.context2 = context;
			inflater = LayoutInflater.from(context2);
			_listData = listData;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return _listData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			final viewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.cash_deposit_delete_item, null);
				holder = new viewHolder();
				convertView.setTag(holder);

			} else {
				holder = (viewHolder) convertView.getTag();
			}
			holder.ref = position;
			holder.tvModeHead = (TextView) convertView
					.findViewById(R.id.tvModeHead);
			holder.tvAmount = (TextView) convertView
					.findViewById(R.id.tvAmount);

			final HashMap<String,String> itemData = _listData.get(position);
			holder.tvModeHead.setText(itemData.get("Mode"));
			holder.tvAmount.setText(common.convertToTwoDecimal(itemData.get("Amount")));

			convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
			return convertView;
		}

	}
	//</editor-fold>

	//Async Task To Call Delete Cash Deposit Data
	private class AsyncDeleteCashDepositWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityCashDepositDelete.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = {"id", "userId", "ipAddress", "machine" };
				String[] value = { cashDepositId, userId,common.getDeviceIPAddress(true),common.getIMEI() };
				responseJSON = "";
				// Call method of web service to delete Cash deposit from server
				responseJSON = common.CallJsonWS(name, value, "DeleteCashDeposit",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to delete cash deposit
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					common.showToast("Cash deposit deleted successfully");					
					Intent intent = new Intent(ActivityCashDepositDelete.this, ActivityAdminHomeScreen.class);
					startActivity(intent);
					finish();
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityCashDepositDelete.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityCashDepositDelete.this,e.getMessage(), false);
			}
			Dialog.dismiss();
		}

		// To display Cash deposit deletion message
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Deleting Cash Deposit...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Web Service to Fetch Cash Deposit Data For Delete
	private class AsyncCashDepositWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityCashDepositDelete.this);

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
					db.open();
					db.DeleteMasterData("CashDepositDeleteData");
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					if (jsonArray.length() > 0) {

						// inserting data into CashDepositDeleteData
						for (int i = 0; i < jsonArray.length(); ++i) {

							db.Insert_CashDepositDeleteData(jsonArray.getJSONObject(i)
									.getString("CashDepositId"),jsonArray.getJSONObject(i)
									.getString("CashDepositDetailId"),jsonArray.getJSONObject(i)
									.getString("DepositDate"),jsonArray.getJSONObject(i)
									.getString("PCDetailId"),jsonArray.getJSONObject(i)
									.getString("Mode"),jsonArray.getJSONObject(i)
									.getString("Amount"),jsonArray.getJSONObject(i)
									.getString("FullName"));
						}
						db.close();
						Intent intent = new Intent(ActivityCashDepositDelete.this, ActivityCashDepositView.class);
						startActivity(intent);
						finish();
					} else {
						common.showToast("There is no data available for deleting cash deposit!");
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityCashDepositDelete.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityCashDepositDelete.this,
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

	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		case android.R.id.home:
			/*AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
				builder1.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
				builder1.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप वाकई नकदी जमा मॉड्यूल छोड़ना चाहते हैं, यह नकदी जमा लेनदेन को त्याग देगा?":"Are you sure, you want to leave cash deposit module it will discard cash deposit transaction?");
				builder1.setCancelable(true);
				builder1.setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int id) {*/
			Intent i = new Intent(ActivityCashDepositDelete.this,ActivityCashDepositView.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(i);
			finish();		

			/*}
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
				alertnew.show();*/
			return true;

		case R.id.action_go_to_home: 
			Intent homeScreenIntent = new Intent(this, ActivityAdminHomeScreen.class);
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
		Intent homeScreenIntent = new Intent(ActivityCashDepositDelete.this, ActivityCashDepositView.class);
		homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeScreenIntent);
		finish();					
	}

}
