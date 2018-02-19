package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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

@SuppressLint("InflateParams") public class ActivityCustomerMaster  extends Activity{
	private DatabaseAdapter dba;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord;
	private MainAdapter ListAdapter;
	final Context context = this;
	UserSessionManager session;
	private int cnt=0;
	private int lsize=0;
	Button btnCreate;
	private String lang, routeId;
	
	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer_master);
		
		//To create instance of database
		dba=new DatabaseAdapter(this);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		// To read user role from user session manager
				final HashMap<String, String> user = session.getLoginUserDetails();
		routeId = user.get(UserSessionManager.KEY_ROUTEID);
		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		
		//To enabled home menu at top bar



		//To create instance of control used in page 
		HeaderDetails = new ArrayList<HashMap<String, String>>();		
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		
		//To get all product and bind list view
		dba.open();
		ArrayList<HashMap<String, String>> lables = dba.getCustomerMaster(routeId, lang);	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Route", String.valueOf(lable.get("Route"))); 
				hm.put("Customer", String.valueOf(lable.get("Customer")));
				hm.put("Mobile", String.valueOf(lable.get("Mobile")));
				hm.put("CustomerType", String.valueOf(lable.get("CustomerType")));
				hm.put("LoginId", String.valueOf(lable.get("LoginId")));
				HeaderDetails.add(hm); 
			}
		}
		dba.close();

		if(lsize==0)
		{
			//To display list view of product
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To display no record found
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityCustomerMaster.this);
			listViewMain.setAdapter(ListAdapter);
		}
			}	


	//To make view holder to display on screen
	public class MainAdapter extends BaseAdapter {

		class ViewHolder {		
			TextView tvRoute, tvCustomer, tvMobile, tvCustomerType, tvLoginId; 
		}	

		private LayoutInflater mInflater;


		//To count total row of view holder
		@Override
		public int getCount() {
			return HeaderDetails.size();
		}


		//To get item name
		@Override
		public Object getItem(int arg0) {
			return HeaderDetails.get(arg0);
		}


		//To get item id
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


		//To make view holder
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {	
			cnt= cnt+1;
			final ViewHolder holder;

			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.activity_customer_master_item, null);
				holder = new ViewHolder();
				holder.tvRoute = (TextView)arg1.findViewById(R.id.tvRoute);
				holder.tvCustomer = (TextView)arg1.findViewById(R.id.tvCustomer);
				holder.tvMobile = (TextView)arg1.findViewById(R.id.tvMobile);	
				holder.tvCustomerType = (TextView)arg1.findViewById(R.id.tvCustomerType);
				holder.tvLoginId = (TextView)arg1.findViewById(R.id.tvLoginId);				
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			if(arg0 ==0)
			{
				cnt=1;
				holder.tvRoute.setVisibility(View.VISIBLE);
				holder.tvRoute.setText(HeaderDetails.get(arg0).get("Route"));
			}
			else
			{	
				//Log.i(TAG,String.valueOf(HeaderDetails.get(arg0-1).get("Date").equals(HeaderDetails.get(arg0).get("Date"))));
				if(HeaderDetails.get(arg0-1).get("Route").equals(HeaderDetails.get(arg0).get("Route")))
				{
					holder.tvRoute.setVisibility(View.GONE);	
				}
				else
				{
					cnt=1;
					holder.tvRoute.setVisibility(View.VISIBLE);
					holder.tvRoute.setText(HeaderDetails.get(arg0).get("Route"));
				}
			}
			//To bind view holder data
			holder.tvCustomer.setText(HeaderDetails.get(arg0).get("Customer"));
			holder.tvMobile.setText(HeaderDetails.get(arg0).get("Mobile"));
			holder.tvCustomerType.setText(HeaderDetails.get(arg0).get("CustomerType"));
			holder.tvLoginId.setText(HeaderDetails.get(arg0).get("LoginId"));
			
			//Code to check if row is even or odd and set set color for alternate rows
			/*if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));  
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/
			return arg1;
		}
	}

	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent homeScreenIntent = new Intent(this, ActivityMasters.class);
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
			Intent intent = new Intent(this, ActivityMasters.class);
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
