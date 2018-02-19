package lateralpraxis.lpdnd;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.Spinner;

import lateralpraxis.lpdnd.ActivityPayment.CustomAdapter;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityComplaintFeedback extends Activity {

	/*Start of code to declare controls*/
	private Spinner spComplaintCategory;
	private Button btnCreate;
	private EditText etRemark;
	private String userId;
	private String categoryId, rBar;
	private RatingBar ratingBar;
	private RadioButton rbComplaint;
	private LinearLayout llFeedback,llComplaint;
	private UserSessionManager session;	
	private RadioGroup RadioMode;
	/*End of code to declare controls*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	/*End of code to declare class*/	

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complaint_feedback);		
		common = new Common(this);
		spComplaintCategory= (Spinner) findViewById(R.id.spComplaintCategory);
		session = new UserSessionManager(this);
		db=new DatabaseAdapter(getApplicationContext());
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//Code to find layouts, radio group, editText, button and rating bar		
		llFeedback= (LinearLayout) findViewById(R.id.llFeedback);
		llComplaint= (LinearLayout) findViewById(R.id.llComplaint);
		RadioMode = (RadioGroup) findViewById(R.id.RadioMode);
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		etRemark= (EditText) findViewById(R.id.etRemark);
		btnCreate= (Button) findViewById(R.id.btnCreate);

		//Code to bind complaint category in spinner
		spComplaintCategory.setAdapter(DataAdapter("complaintcategory",""));		

		//To enabled home menu at top bar



		//Code on radio group selection change event for show / hide complaint/feedback details
		RadioMode.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				View radioButton = RadioMode.findViewById(checkedId);
				int index = RadioMode.indexOfChild(radioButton);
				switch (index) {
				case 0: // first button - complaint
					spComplaintCategory.setSelection(0);
					llComplaint.setVisibility(View.VISIBLE);
					llFeedback.setVisibility(View.GONE);
					break;
				case 1: // second button - feedback
					ratingBar.setRating(1.0f);
					llFeedback.setVisibility(View.VISIBLE);	
					llComplaint.setVisibility(View.GONE);
					break;
				}
			}
		});


		//Code on button create click event to add complaint/feedback data in complaint table
		btnCreate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				// get selected radio button from radioGroup
				int selectedId = RadioMode.getCheckedRadioButtonId();

				// find the radio button by returned id
				rbComplaint = (RadioButton) findViewById(selectedId);

				//Code for validating mandatory fields				
				if(spComplaintCategory.getSelectedItemPosition()==0 && String.valueOf(rbComplaint.getText()).equalsIgnoreCase("Complaint"))
				{
					common.showToast("Please select Category.");
				}					
				else if(etRemark.getText().toString().trim().equals(""))
				{
					common.showToast("Please enter remarks.");
				}
				else
				{	
					//Code to insert data in complaint table
					db.open();
					HashMap<String, String> user = session.getLoginUserDetails();
					userId = user.get(UserSessionManager.KEY_ID);

					if(String.valueOf(rbComplaint.getText()).equalsIgnoreCase("Complaint"))
					{
						categoryId=((CustomType)spComplaintCategory.getSelectedItem()).getId();
						rBar = "0";
					}
					else
					{
						categoryId="0";
						rBar = String.valueOf(ratingBar.getRating());
					}
					Log.i("type", String.valueOf(rbComplaint.getText()));
					Log.i("categoryId", categoryId);
					Log.i("rBar", rBar);
					db.insertComplaint(String.valueOf(userId),String.valueOf(rbComplaint.getText()),categoryId,rBar,String.valueOf(etRemark.getText()), UUID.randomUUID().toString());
					db.close();
					//Code to clear all controls after adding data in complaint table
					spComplaintCategory.setSelection(0);
					etRemark.setText("");
					common.showToast("Query added successfully.");
					Intent myIntent = new Intent(ActivityComplaintFeedback.this, ActivityComplaintFeedbackViewSummary.class);
					startActivity(myIntent);
					finish();
				}

			}
		});
	}
	// Code to fill data in drop down list
	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter)
	{
		db.open();
		List <CustomType> lables = db.GetMasterDetails(masterType, filter);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this,android.R.layout.simple_spinner_item, lables);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		db.close();
		return dataAdapter;
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
