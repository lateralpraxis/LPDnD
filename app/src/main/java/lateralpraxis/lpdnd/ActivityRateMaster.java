package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import lateralpraxis.lpdnd.types.CustomType;

@SuppressLint("InflateParams") public class ActivityRateMaster  extends Activity{
	private DatabaseAdapter dba;
	private Common common;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord;
	private MainAdapter ListAdapter;
	final Context context = this;
	UserSessionManager session;
	private Spinner spCompany, spCustomer;
	private RadioGroup radioGroup;
	private int cnt=0, cntCus=0;
	private int lsize=0;
	Button btnCreate;
	private String lang, routeId, customerWithRoute;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rate_master);

		//To create instance of database
		dba=new DatabaseAdapter(this);
		common = new Common(this);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		
		// To read user role from user session manager
				final HashMap<String, String> user = session.getLoginUserDetails();
				routeId = user.get(UserSessionManager.KEY_ROUTEID);
				
		//To enabled home menu at top bar



		//To create instance of control used in page 
		HeaderDetails = new ArrayList<HashMap<String, String>>();		
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);	
		spCustomer = (Spinner) findViewById(R.id.spCustomer);
		spCompany = (Spinner) findViewById(R.id.spCompany);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

		spCustomer.setVisibility(View.GONE);
		BindCustomer();

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton rb = (RadioButton) group.findViewById(checkedId);
				if (null != rb && checkedId > -1) {
					BindCustomer();
				}
			}
		});


		// To clean list adaptor, no records found and total amount
		spCompany.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				BindRate();	
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		// To clean list adaptor, no records found and total amount
		spCustomer.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				BindRate();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

	}	

	private void BindCustomer()
	{			
		spCompany.setVisibility(View.GONE);
		spCustomer.setVisibility(View.GONE);
		// To get company and customer list from database and bind to company and customer drop down list
		Log.i("LPDND", "TEXT="+((RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId())).getText());
		int radioButtonID = radioGroup.getCheckedRadioButtonId();
		View radioButton = radioGroup.findViewById(radioButtonID);
		int idx = radioGroup.indexOfChild(radioButton);
		if(idx==0)
		{
			spCompany.setVisibility(View.VISIBLE);
			spCompany.setAdapter(DataAdapter("company", "", lang));
		}
		else
		{
			spCustomer.setVisibility(View.VISIBLE);
			spCustomer.setAdapter(DataAdapter("customerByRoute",routeId, lang));
		}
	}

	private void BindRate()
	{
		String companyId="0",customerId="0";
		listViewMain.setAdapter(null);
		HeaderDetails.clear();	
		int radioButtonID = radioGroup.getCheckedRadioButtonId();
		View radioButton = radioGroup.findViewById(radioButtonID);
		int idx = radioGroup.indexOfChild(radioButton);
		if(idx==0)
			companyId=((CustomType) spCompany.getSelectedItem()).getId();
		else
		{
			customerWithRoute = ((CustomType) spCustomer.getSelectedItem()).getId();
			String[] arr = customerWithRoute.split("~");
			if(arr.length>1)
			{
				customerId = arr[0];
				//customerRouteId = arr[1];
				//customerRoute = arr[2];
			}
			else
			{
				customerId = "0";
			}
		}

		//To get all product and bind list view
		if(!companyId.equalsIgnoreCase("0") || !customerId.equalsIgnoreCase("0"))
		{

			ArrayList<HashMap<String, String>> lables= null;
			dba.open();		
			lables = dba.getProductMasterRouteOfficer(companyId, customerId, routeId, lang);	
			lsize=lables.size();
			if (lables != null && lables.size() > 0) {
				for (HashMap<String, String> lable : lables) {	
					HashMap<String, String> hm = new HashMap<String,String>();
					hm.put("Route", String.valueOf(lable.get("Route"))); 
					hm.put("Customer", String.valueOf(lable.get("Customer")));
					hm.put("Company", String.valueOf(lable.get("Company"))); 
					hm.put("Sku", String.valueOf(lable.get("Sku")));
					hm.put("Rate", String.valueOf(lable.get("Rate")));
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
				ListAdapter = new MainAdapter(ActivityRateMaster.this);
				listViewMain.setAdapter(ListAdapter);
			}
		}
	}

	//To make view holder to display on screen
	public class MainAdapter extends BaseAdapter {

		class ViewHolder {		
			TextView tvRoute, tvCustomer, tvCompany, tvItem, tvRate; 
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
			cntCus= cntCus+1;
			final ViewHolder holder;

			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.activity_rate_master_item, null);
				holder = new ViewHolder();
				holder.tvRoute = (TextView)arg1.findViewById(R.id.tvRoute);
				holder.tvCustomer = (TextView)arg1.findViewById(R.id.tvCustomer);
				holder.tvCompany = (TextView)arg1.findViewById(R.id.tvCompany);
				holder.tvItem = (TextView)arg1.findViewById(R.id.tvItem);
				holder.tvRate = (TextView)arg1.findViewById(R.id.tvRate);				
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
				cntCus=1;
				holder.tvCustomer.setVisibility(View.VISIBLE);
				holder.tvCustomer.setText(HeaderDetails.get(arg0).get("Customer"));
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
				if(HeaderDetails.get(arg0-1).get("Customer").equals(HeaderDetails.get(arg0).get("Customer")))
				{
					holder.tvCustomer.setVisibility(View.GONE);	
				}
				else
				{
					cntCus=1;
					holder.tvCustomer.setVisibility(View.VISIBLE);
					holder.tvCustomer.setText(HeaderDetails.get(arg0).get("Customer"));
				}
			}
			//To bind view holder data
			//holder.tvRoute.setText(HeaderDetails.get(arg0).get("Route"));
			//holder.tvCustomer.setText(HeaderDetails.get(arg0).get("Customer"));
			holder.tvCompany.setText(HeaderDetails.get(arg0).get("Company"));
			holder.tvItem.setText(HeaderDetails.get(arg0).get("Sku"));
			holder.tvRate.setText(common.convertToTwoDecimal(HeaderDetails.get(arg0).get("Rate")));

			//Code to check if row is even or odd and set set color for alternate rows
			/*if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));  
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/
			return arg1;
		}
	}

	//Data Adapter of drop down list
	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter, String lang) {
		dba.open();
		List<CustomType> lables = dba.GetMasterDetailsByLang(masterType, filter, lang);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(
				this, android.R.layout.simple_spinner_item, lables);
		dataAdapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dba.close();
		return dataAdapter;
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
