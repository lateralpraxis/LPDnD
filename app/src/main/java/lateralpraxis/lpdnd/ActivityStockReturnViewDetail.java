package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityStockReturnViewDetail  extends Activity{
	private DatabaseAdapter dba;
	private String returnDate, routeId, header;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord, tvHeader;
	private MainAdapter ListAdapter;
	UserSessionManager session;
	final Context context = this;
	private int cnt=0;
	private int lsize=0;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stockreturn_view_detail);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());

		//To create instance of database and control
		dba=new DatabaseAdapter(this);
		HeaderDetails = new ArrayList<HashMap<String, String>>();
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		tvHeader =  (TextView) findViewById(R.id.tvHeader);
		dba.open();	

		//Call method to enabled icon on top of 'go to home' page



		//To extract id from bundle to show details
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{			
			returnDate= extras.getString("ReturnDate");
			routeId= extras.getString("RouteId");
			header= extras.getString("Header");				
		}
		Log.i("LP", header);
		tvHeader.setText(Html.fromHtml("<font color=#000000> "+header+"</font>"));
		//To get stock return details from database
		ArrayList<HashMap<String, String>> lables = dba.getStockReturnSummaryDetails(returnDate, routeId);	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("SKUName", String.valueOf(lable.get("SKUName"))); 
				hm.put("ReturnQty", String.valueOf(lable.get("ReturnQty")));
				hm.put("LeakageQty", String.valueOf(lable.get("LeakageQty")));
				Log.i("SKUName",String.valueOf(lable.get("SKUName")));
				Log.i("ReturnQty",String.valueOf(lable.get("ReturnQty")));
				Log.i("LeakageQty",String.valueOf(lable.get("LeakageQty")));
				
				
				HeaderDetails.add(hm); 
			}
		}
		dba.close();
		if(lsize==0)
		{
			//To display no record message 
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To bind data and display list view
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityStockReturnViewDetail.this);
			listViewMain.setAdapter(ListAdapter);
		}		
	}

	//To make class of stock return view holder
	public class MainAdapter extends BaseAdapter {
		class ViewHolder {
			TextView tvSKUName, tvReturnQty, tvLeakageQty; 
		}	

		private LayoutInflater mInflater;
		//To get item count
		@Override
		public int getCount() {
			return HeaderDetails.size();
		}

		////To get item name
		@Override
		public Object getItem(int arg0) {
			return HeaderDetails.get(arg0);
		}

		////To get item id
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

		//To instantiate layout XML file into its corresponding view objects.
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {	
			cnt= cnt+1;
			final ViewHolder holder;

			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.activity_stockreturn_view_detail_item, null);
				holder = new ViewHolder();
				holder.tvSKUName = (TextView)arg1.findViewById(R.id.tvSKUName);
				holder.tvReturnQty = (TextView)arg1.findViewById(R.id.tvReturnQty);	
				holder.tvLeakageQty = (TextView)arg1.findViewById(R.id.tvLeakageQty);	

				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			//To bind data into view holder
			holder.tvSKUName.setText(HeaderDetails.get(arg0).get("SKUName"));
			holder.tvReturnQty.setText(HeaderDetails.get(arg0).get("ReturnQty").replace(".0", ""));
			holder.tvLeakageQty.setText(HeaderDetails.get(arg0).get("LeakageQty").replace(".0", ""));
			
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
		Intent  intent;
		intent = new Intent(this, ActivityStockReturnViewSummary.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
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
			Intent  intent;
			intent = new Intent(this, ActivityStockReturnViewSummary.class);
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
