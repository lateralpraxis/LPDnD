package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityOutstanding  extends Activity {
	/*Start of code for Variable Declaration*/
	private String lang,userId,responseJSON,strType;
	private int listSize = 0;
	private int year, month, day;
	private ArrayList<HashMap<String, String>> list;
	private ArrayList<HashMap<String, String>> wordList = null;
	private String userRole;
	HashMap<String, String> map = null;
	/*End of code for Variable Declaration*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	private UserSessionManager session;
	private final Context mContext = this;
	/*End of code to declare class*/

	/*Start of code to declare Controls*/
	private TextView tvEmpty;
	private ListView listAllocation;
	private Button btnGo;
	private Spinner spCompany;
	private RadioButton RadioCustomer, RadioRouteOfficer;
	private RadioGroup RadioType;
	/*End of code to declare Controls*/

	//On create method similar to page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Code to set layout
		setContentView(R.layout.activity_outstanding_report);

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());
		/*		*/
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);
		userRole = user.get(UserSessionManager.KEY_ROLES);

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);

		//Start of code to find Controls
		listAllocation = (ListView) findViewById(R.id.listAllocation);
		tvEmpty = (TextView) findViewById(R.id.tvEmpty);
		btnGo= (Button) findViewById(R.id.btnGo);
		spCompany= (Spinner) findViewById(R.id.spCompany);
		RadioCustomer = (RadioButton) findViewById(R.id.RadioCustomer);
		RadioRouteOfficer = (RadioButton) findViewById(R.id.RadioRouteOfficer);
		RadioType = (RadioGroup) findViewById(R.id.RadioType);
		strType = "ReadCustomerOutStanding";
		if (userRole.contains("Centre User") || userRole.contains("Route Officer") || userRole.contains("Collection Officer")){

			RadioRouteOfficer.setVisibility(View.INVISIBLE);
		}
		if (common.isConnected()) {
			AsyncCompanyWSCall task = new AsyncCompanyWSCall();
			task.execute();
		}

		btnGo.setOnClickListener(new View.OnClickListener() {
			//On click of add button
			@Override
			public void onClick(View arg0) {

				if (common.isConnected()) {
					AsyncOutstandingListWSCall task = new AsyncOutstandingListWSCall();
					task.execute();
				}
			}
		});
		
		RadioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	         @Override
	         public void onCheckedChanged(RadioGroup group, int checkedId) {
	             View radioButton = RadioType.findViewById(checkedId);
	             int index = RadioType.indexOfChild(radioButton);
	             
	             if (index == 0) {
	            	 strType = "ReadCustomerOutStanding";
	                
	             } else {
	            	 strType = "ReadRouteOfficerOutstanding";
	             }
	         }
	     });

	}

	
	
	//<editor-fold desc="Code to be Bind Data in list view">
	public static class ViewHolder {
		TextView tvName, tvAmount;
	}

	public class CustomAdapter extends BaseAdapter {
		private Context docContext;
		private LayoutInflater mInflater;

		public CustomAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
			this.docContext = context;
			mInflater = LayoutInflater.from(docContext);
			list = lvList;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
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

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {


			final ViewHolder holder;
			if (arg1 == null) {
				arg1 = mInflater.inflate(R.layout.list_outstanding_detail, null);
				holder = new ViewHolder();

				holder.tvName = (TextView) arg1.findViewById(R.id.tvName);
				holder.tvAmount = (TextView) arg1.findViewById(R.id.tvAmount);

				arg1.setTag(holder);

			} else {
				holder = (ViewHolder) arg1.getTag();
			}
			holder.tvName.setText(list.get(arg0).get("Name"));
			holder.tvAmount.setText(list.get(arg0).get("Amount"));

			arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
			return arg1;
		}
	}
	//</editor-fold>



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

	private class AsyncCompanyWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityOutstanding.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				db.open();
				db.deleteMISCompany();
				db.close();
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCompany", userId, "" };
				responseJSON = "";
				// Call method of web service to download route from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);

			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: " + "Unable to fetch response from server.";
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR: ")) {
					JSONArray jsonArray = new JSONArray(responseJSON);

					for (int i = 0; i < jsonArray.length(); ++i) {
						db.open();
						db.Insert_MISCompany(jsonArray.getJSONObject(i).getString("A"), jsonArray.getJSONObject(i).getString("B"));
						db.close();
					}

					//Code to bind Company in spinner
					spCompany.setAdapter(DataAdapter("miscompany",""));


				} else {
					common.showAlert(ActivityOutstanding.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityOutstanding.this, "Company Downloading failed: " + e.toString(), false);
			}
			Dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Company..");
			Dialog.setCancelable(false);
			Dialog.show();

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}

	//<editor-fold desc="Async class Class to handle fetch Outstanding web service call as separate thread">
	private class AsyncOutstandingListWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityOutstanding.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = {"action", "lang", "compId", "userId"};
				String[] value = {strType,lang,((CustomType)spCompany.getSelectedItem()).getId(),userId};
				responseJSON = "";
				//Call method of web service to download data from server
				responseJSON = common.CallJsonWS(name, value, "ReadCompanyWiseReports", common.url);
				return "";
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		//After execution of web service
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR: ")) {
					JSONArray jsonArray = new JSONArray(responseJSON);
					wordList = new ArrayList<HashMap<String, String>>();
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); ++i) {
							map = new HashMap<String, String>();
							map.put("Name", jsonArray.getJSONObject(i)
									.getString("A"));
							map.put("Amount", jsonArray.getJSONObject(i)
									.getString("B"));
							wordList.add(map);
						}

					} else {
						if (result.contains("null")) {
							result = "Server not responding. Please try again later.";
							common.showAlert(ActivityOutstanding.this, result, false);
						}
					}
					listSize = wordList.size();
					if (listSize != 0) {
						listAllocation.setAdapter(new CustomAdapter(mContext, wordList));

						ViewGroup.LayoutParams params = listAllocation.getLayoutParams();
						listAllocation.setLayoutParams(params);
						listAllocation.requestLayout();
						tvEmpty.setVisibility(View.GONE);
					} else {
						listAllocation.setAdapter(null);
						tvEmpty.setVisibility(View.VISIBLE);
					}
				}
			} catch (Exception e) {
				common.showAlert(ActivityOutstanding.this, "Outstanding Downloading failed: " + "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Outstanding..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
	//</editor-fold>

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.action_go_to_home: 
			String userRole="";
			final HashMap<String, String> user = session.getLoginUserDetails();
			userRole = user.get(UserSessionManager.KEY_ROLES);
			Intent i;
			if(userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant")))
				i = new Intent(ActivityOutstanding.this,ActivityAdminHomeScreen.class);
			else
				i = new Intent(ActivityOutstanding.this,ActivityHomeScreen.class);
			startActivity(i);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//Event Triggered on Clicking Back
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ActivityOutstanding.this,ActivityReport.class);
		startActivity(i);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}


	//To create menu on inflater
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		return true;
	}



}
