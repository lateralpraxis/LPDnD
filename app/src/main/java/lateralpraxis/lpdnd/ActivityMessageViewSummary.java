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

public class ActivityMessageViewSummary  extends Activity{
	private DatabaseAdapter dba;
	private String TAG ="LPDnD";
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord;
	private MainAdapter ListAdapter;
	private int cnt=0;
	private int lsize=0;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_view_summary);
		
		//To create instance of database 
		dba=new DatabaseAdapter(this);

		//To enabled back menu



		//To create object of controls
		HeaderDetails = new ArrayList<HashMap<String, String>>();
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);

		//To get offers from database
		dba.open();	
		ArrayList<HashMap<String, String>> lables = dba.getMsg();	
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
		Log.i(TAG,"lsize="+ lsize);
		if(lsize==0)
		{
			//To show no records found messages
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To bind and show offers from database
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityMessageViewSummary.this);
			listViewMain.setAdapter(ListAdapter);
		}
	}
	
	//To make class of main adapter for view holder
	public class MainAdapter extends BaseAdapter {
		class ViewHolder {
			TextView tvId, tvMsg; 
		}	
		private LayoutInflater mInflater;
		
		//To get row count
		@Override
		public int getCount() {
			return HeaderDetails.size();
		}

		//To get offers message
		@Override
		public Object getItem(int arg0) {
			return HeaderDetails.get(arg0);
		}

		//To get offers id
		@Override
		public long getItemId(int arg0) { 
			return arg0;
		}

		//To get offers position
		@Override
		public int getItemViewType(int position) {

			return position;
		}

		//Constructor of class
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
				arg1 = mInflater.inflate(R.layout.activity_message_view_summary_item, null);
				holder = new ViewHolder();

				holder.tvId = (TextView)arg1.findViewById(R.id.tvId);
				holder.tvMsg = (TextView)arg1.findViewById(R.id.tvMsg);	
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			//To bind offers into controls
			holder.tvMsg.setText(HeaderDetails.get(arg0).get("Name"));
			
			//Code to check if row is even or odd and set set color for alternate rows
			if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));  
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}
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
