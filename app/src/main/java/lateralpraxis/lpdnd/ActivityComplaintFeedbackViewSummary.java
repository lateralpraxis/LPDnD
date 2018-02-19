package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ActivityComplaintFeedbackViewSummary extends Activity {

	/*Start of code to declare controls and variable*/
	private DatabaseAdapter dba;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord;
	private Intent intent;
	final Context context = this;
	private Common common;
	UserSessionManager session;
	private MainAdapter ListAdapter;
	private int cnt=0;
	private int lsize=0;
	Button btnCreate;
	RatingBar ratingBar;


	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complaint_feedback_view_summary);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of database
		dba=new DatabaseAdapter(this);
		common = new Common(this);
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());

		//To enabled home menu at top bar



		//To create instance of control used in page 
		HeaderDetails = new ArrayList<HashMap<String, String>>();		
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		btnCreate = (Button)findViewById(R.id.btnCreate);
		ratingBar = (RatingBar)findViewById(R.id.ratingBar);

		//To handle a callback to be invoked when an item in this AdapterView has been clicked
		listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			//On click of list view item
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {   
				Intent myIntent = new Intent(ActivityComplaintFeedbackViewSummary.this, ActivityComplaintFeedbackViewDetail.class);
				myIntent.putExtra("Id", ((TextView)view.findViewById(R.id.tvId)).getText());
				startActivity(myIntent);
				finish();
			}			
		});


		//To get all complaint and bind list view
		dba.open();
		ArrayList<HashMap<String, String>> lables = dba.getComplaintFeedback();	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Id", String.valueOf(lable.get("Id")));
				hm.put("ComplaintDate", String.valueOf(lable.get("ComplaintDate"))); 
				hm.put("ComplaintType", String.valueOf(lable.get("ComplaintType")));
				hm.put("FeedBackRating", String.valueOf(lable.get("FeedBackRating")));
				HeaderDetails.add(hm); 
			}
		}
		dba.close();

		if(lsize==0)
		{
			//To display list view of complaint
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To display no record found
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityComplaintFeedbackViewSummary.this);
			listViewMain.setAdapter(ListAdapter);
		}

		//Event hander of create complaint button
		btnCreate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				intent = new Intent(context,ActivityComplaintFeedback.class);
				startActivity(intent);
				finish();
			}
		});		
	}

	//To make view holder to display on screen
	public class MainAdapter extends BaseAdapter {

		class ViewHolder {		
			LinearLayout llComplaintDate;
			TextView tvComplaintType, tvComplaintDate, tvId;//,tvBlank; 
			RatingBar ratingBar;
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
		@SuppressLint("InflateParams") @Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {	
			cnt= cnt+1;
			final ViewHolder holder;

			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.activity_complaint_feedback_view_summary_item, null);
				holder = new ViewHolder();
				holder.tvComplaintType = (TextView)arg1.findViewById(R.id.tvComplaintType);
				holder.tvComplaintDate = (TextView)arg1.findViewById(R.id.tvComplaintDate);	
				holder.ratingBar = (RatingBar)arg1.findViewById(R.id.ratingBar);
				holder.tvId = (TextView)arg1.findViewById(R.id.tvId);	
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}

			//To bind view holder data
			Log.i("ComplaintDate =",HeaderDetails.get(arg0).get("ComplaintDate"));
			holder.tvComplaintType.setText(HeaderDetails.get(arg0).get("ComplaintType"));
			holder.tvComplaintDate.setText(common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", HeaderDetails.get(arg0).get("ComplaintDate")));
			if(HeaderDetails.get(arg0).get("ComplaintType").equalsIgnoreCase("Feedback"))
			{
				holder.ratingBar.setVisibility(View.VISIBLE);
				holder.ratingBar.setRating(Float.parseFloat(HeaderDetails.get(arg0).get("FeedBackRating")));
			}
			else
			{
				holder.ratingBar.setVisibility(View.GONE);
			}

			holder.tvId.setText(HeaderDetails.get(arg0).get("Id"));		
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


	//When press back button go to home screen
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
