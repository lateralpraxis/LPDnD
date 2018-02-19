package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

public class ActivityComplaintFeedbackViewDetail extends Activity {

	/*Start of code to declare controls and local variable*/
	private DatabaseAdapter dba;
	private String id;
	private TextView tvCompDateData,tvCompTypeData,tvCategoryFeedbackData,tvCategoryFeedback,tvCustomerRemarkData;
	RatingBar ratingBar;
	private Common common;
	UserSessionManager session;
	final Context context = this;
	private int lsize=0;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complaint_feedback_view_detail);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To find control id 
		tvCompDateData = (TextView) findViewById(R.id.tvCompDateData);
		tvCompTypeData = (TextView) findViewById(R.id.tvCompTypeData);
		tvCategoryFeedbackData = (TextView) findViewById(R.id.tvCategoryFeedbackData);
		tvCustomerRemarkData = (TextView) findViewById(R.id.tvCustomerRemarkData);
		tvCategoryFeedback=(TextView) findViewById(R.id.tvCategoryFeedback);
		ratingBar=(RatingBar) findViewById(R.id.ratingBar);
		//To create instance of database and control
		dba=new DatabaseAdapter(this);
		common=new Common(this);

		//Call method to enabled icon on top of 'go to home' page



		//To extract id from bundle to show details
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{			
			id= extras.getString("Id");			
		}
		Log.i("Id",id);

		//To get complaint feedback details from database and show in controls
		dba.open();
		ArrayList<HashMap<String, String>> lables = dba.getComplaintFeedbackSummaryDetails(id);	
		lsize=lables.size();
		Log.i("aaa", String.valueOf(lsize));
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("ComplaintDate", String.valueOf(lable.get("ComplaintDate")));
				hm.put("ComplaintType", String.valueOf(lable.get("ComplaintType")));
				hm.put("CategoryFeedback", String.valueOf(lable.get("CategoryFeedback")));
				hm.put("CustomerRemark", String.valueOf(lable.get("CustomerRemark")));
				tvCompDateData.setText(common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", String.valueOf(lable.get("ComplaintDate"))));
				tvCompTypeData.setText(String.valueOf(lable.get("ComplaintType")));
				tvCategoryFeedbackData.setText(String.valueOf(lable.get("CategoryFeedback")));
				tvCustomerRemarkData.setText(String.valueOf(lable.get("CustomerRemark")));	
				if (String.valueOf(lable.get("ComplaintType")).equalsIgnoreCase("Complaint"))				
				{
					tvCategoryFeedback.setText("Category");
					tvCategoryFeedbackData.setVisibility(View.VISIBLE);	
					ratingBar.setVisibility(View.GONE);
				}
				else
				{
					tvCategoryFeedback.setText("Rating");
					ratingBar.setRating(Float.valueOf(lable.get("CategoryFeedback")));
					tvCategoryFeedbackData.setVisibility(View.GONE);	
					ratingBar.setVisibility(View.VISIBLE);
				}
			}
		}
		dba.close();
	}

	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent homeScreenIntent = new Intent(this, ActivityComplaintFeedbackViewSummary.class);
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
			Intent intent = new Intent(this, ActivityComplaintFeedbackViewSummary.class);
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
