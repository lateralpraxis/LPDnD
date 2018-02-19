package lateralpraxis.lpdnd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("InflateParams") public class ActivityDeliveryPaymentViewSummary  extends Activity{
	private DatabaseAdapter dba;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord;
	private MainAdapter ListAdapter;
	final Context context = this;
	UserSessionManager session;
	Common common;
	private int cnt=0;
	private int lsize=0;


	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery_payment_view_summary);
		
		//To create instance of database
		dba=new DatabaseAdapter(this);
		common=new Common(this);
		
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());

		//To enabled home menu at top bar
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);


		//To create instance of control used in page 
		HeaderDetails = new ArrayList<HashMap<String, String>>();		
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		
		
		//To handle a callback to be invoked when an item in this AdapterView has been clicked
		listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			//On click of list view item
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   
				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
				//To bind default delivery date
				final Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE,0);
				String deliveryDate = dateFormatter.format(c.getTime());
				dba.open();
				dba.Insert_DemandDate(deliveryDate);
				dba.close();
				
				Intent myIntent = new Intent(ActivityDeliveryPaymentViewSummary.this, ActivityDeliveryPaymentViewDetail.class);
				myIntent.putExtra("Id", ((TextView)view.findViewById(R.id.tvId)).getText()); 
				myIntent.putExtra("Header",((TextView)view.findViewById(R.id.tvName)).getText());
				startActivity(myIntent);
				finish();
			}
		});


		//To get all delivery and bind list view
		dba.open();
		ArrayList<HashMap<String, String>> lables = dba.getViewDemand();	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Id", String.valueOf(lable.get("Id"))); 
				hm.put("Name", String.valueOf(lable.get("Name")));
				HeaderDetails.add(hm); 
			}
		}
		dba.close();

		if(lsize==0)
		{
			//To display no record found
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To display list view of delivery
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityDeliveryPaymentViewSummary.this);
			listViewMain.setAdapter(ListAdapter);
		}		
	}	


	//To make view holder to display on screen
	public class MainAdapter extends BaseAdapter {

		class ViewHolder {		
			TextView tvId, tvName, tvDate; 
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
				arg1 = mInflater.inflate(R.layout.activity_delivery_payment_view_summary_item, null);
				holder = new ViewHolder();
				holder.tvId = (TextView)arg1.findViewById(R.id.tvId);
				holder.tvName = (TextView)arg1.findViewById(R.id.tvName);	
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}

			//To bind view holder data
			holder.tvId.setText(HeaderDetails.get(arg0).get("Id"));
			holder.tvName.setText(HeaderDetails.get(arg0).get("Name"));
			//holder.tvDate.setText(common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", HeaderDetails.get(arg0).get("Date")));
			
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
