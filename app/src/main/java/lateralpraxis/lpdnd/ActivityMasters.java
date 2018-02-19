package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;



import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ActivityMasters extends Activity{
	private String userRole, userId; 
	private TextView tvHeader;
	Button go, btn;
	TableLayout tl;
	TableRow tr;
	List<Integer> views = Arrays.asList(
			R.layout.btn_vehicle, R.layout.btn_customer, R.layout.btn_rate, R.layout.btn_product);
	LinearLayout btnLayout;
	final Context context = this;
	static final int ITEM_PER_ROW = 2;
	private Intent intent;
	private static String responseJSON;
	private UserSessionManager session;
	private DatabaseAdapter dba;
	Common common;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_masters);

		//To create object of common class
		common = new Common(getApplicationContext());

		//To create object of user session
		session = new UserSessionManager(getApplicationContext());

		//To create object of database
		dba=new DatabaseAdapter(getApplicationContext());

		//To enabled home menu at top bar


		
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);
		userRole = user.get(UserSessionManager.KEY_ROLES);
		tvHeader = (TextView)findViewById(R.id.tvHeader);
		tvHeader.setText("View Masters");
		go = (Button) findViewById(R.id.btnGo);
		tl = (TableLayout) findViewById(R.id.tlmainMenu);
		tl.setColumnStretchable(0, true);
		tl.setColumnStretchable(1, true);
		go.performClick();

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
			case R.layout.btn_vehicle:
				btn = (Button) btnLayout.findViewById(R.id.btnVehicle);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context,ActivityVehicleMaster.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_customer:
				btn = (Button) btnLayout.findViewById(R.id.btnCustomer);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context,ActivityCustomerMaster.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_rate:
				btn = (Button) btnLayout.findViewById(R.id.btnRate);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context,ActivityRateMaster.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_product:
				btn = (Button) btnLayout.findViewById(R.id.btnCustomerProduct);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						AsyncProductMasterWSCall task = new AsyncProductMasterWSCall();
						task.execute(new String[]{"2"});
					}
				});
				break;
			default:
				break;
		}

		return btnLayout;
	}

	//Class to handle product master web services call as separate thread
	private class AsyncProductMasterWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityMasters.this);
		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadProductMaster", userId, userRole};
				responseJSON="";
				//Call method of web service to download product master from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",common.url);	
				return params[0];
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				Log.i("LPDND", e.getMessage());
				// TODO: handle exception
				return "ERROR: "+ "Unable to get response from server.";
			} 
		}

		//After execution of web service to download product master
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					//To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ProductMaster");
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String companyId= jsonArray.getJSONObject(i).getString("A");
						String companyName= jsonArray.getJSONObject(i).getString("B");
						String skuId= jsonArray.getJSONObject(i).getString("C");
						String skuName= jsonArray.getJSONObject(i).getString("D");
						String packingType= jsonArray.getJSONObject(i).getString("E");
						String rate= jsonArray.getJSONObject(i).getString("F");
						String getNameLocal= jsonArray.getJSONObject(i).getString("G");
						String getProductName= jsonArray.getJSONObject(i).getString("H");
						String getSkuUnit= jsonArray.getJSONObject(i).getString("I");
						String getUom= jsonArray.getJSONObject(i).getString("J");
						dba.Insert_ProductMaster(companyId, companyName, skuId, skuName, packingType, rate, getNameLocal, getProductName, getSkuUnit, getUom);
					}
					dba.close();
					if(result.equalsIgnoreCase("0"))
						common.showAlert(ActivityMasters.this, "Synchronization completed successfully.",false);
					else if(result.equalsIgnoreCase("2"))
					{
						intent = new Intent(context, ActivityProductMaster.class);
						startActivity(intent);
						finish();
					}
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityMasters.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityMasters.this,"Product Downloading failed: " +"Unable to get response from server.", false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Product..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
	
	//When press back button go to home screen
		@Override
		public void onBackPressed() {
			Intent homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
			homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreenIntent);
			finish();
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
				Intent intent = new Intent(this, ActivityHomeScreen.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(intent);
				finish();
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

}
