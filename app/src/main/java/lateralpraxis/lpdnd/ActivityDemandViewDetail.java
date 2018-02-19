package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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

@SuppressLint("InflateParams") public class ActivityDemandViewDetail  extends Activity{
	private DatabaseAdapter dba;
	private String id="0", header;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private View listViewMainFooter;
	private TextView tvNoRecord, tvTotalAmount, tvHeader, tvNote;
	private Common common;
	private static String responseJSON;
	private MainAdapter ListAdapter;
	private SimpleDateFormat format1, format2, dateFormat_database;
	UserSessionManager session;
	final Context context = this;
	private String userId, userRole;
	private int cnt=0;
	double totalQty=0, totalAmounts=0;
	private int lsize=0;
	long different=0;
	Button btnCreate, buttonLeft, buttonRight;
	private String lang;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demand_view_detail);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
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
		
		common = new Common(this);


		//To create instance of database and control
		dba=new DatabaseAdapter(this);
		HeaderDetails = new ArrayList<HashMap<String, String>>();
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		listViewMainFooter = (View) findViewById(R.id.listViewMainFooter);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
		btnCreate = (Button)findViewById(R.id.btnCreate);
		tvHeader = (TextView)findViewById(R.id.tvHeader);
		buttonLeft = (Button)findViewById(R.id.ButtonLeft);
		buttonRight = (Button)findViewById(R.id.ButtonRight);
		tvNote = (TextView) findViewById(R.id.tvNote);

		//Call method to enabled icon on top of 'go to home' page



		//To create instance of date format
		dateFormat_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		format1 = new SimpleDateFormat("hh:mm:ss", Locale.US);
		format2 = new SimpleDateFormat("hh:mm aa", Locale.US);


		//To extract id from bundle to show details
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			id = extras.getString("Id");
			header = extras.getString("Header");
		}

		if(userRole.equalsIgnoreCase("Customer"))
		{
			CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) { 
	            public void onTick(long millisUntilFinished) {
	            	try 
					{
	        			dba.open();
	        			String date =dba.getDemandCutOff().split("~")[0];
	        			String time=dba.getDemandCutOff().split("~")[1];
	        			//tvNote.setText("Note: You can place demand for "+date+" upto "+time+".");
	        			tvHeader.setText(common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", dba.getDemandDate()));
	        			dba.close();			
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append(new Date().getHours());
						stringBuilder.append(":");
						stringBuilder.append(new Date().getMinutes());
						stringBuilder.append(":");
						stringBuilder.append(new Date().getSeconds());
						Date Date1 = format1.parse(String.valueOf(stringBuilder.toString()));
						Date Date2 = format2.parse(time);
						long mills = Date2.getTime() - Date1.getTime();
						int Hours = (int) (mills/(1000 * 60 * 60));
						int Mins = (int) (mills/(1000*60)) % 60;
						int Secs = (int) (mills / 1000) % 60;

						String diff = String.format("%02d:%02d:%02d", Hours, Mins, Secs); // updated value every1 second
						Log.i("LPDND", diff);
						if(Mins>=0 && Secs>=0)
						{
						if(lang.equalsIgnoreCase("hi"))
							tvNote.setText("नोट: आप "+ date +" के लिए "+ time +" तक मांग कर सकते हैं।  शेष समय: "+diff);
						else
							tvNote.setText("Note: You can place demand for "+date+" upto "+time+". Remaining Time: "+diff);
						}
						else
						{
							if(lang.equalsIgnoreCase("hi"))
								tvNote.setText("नोट: आप "+ date +" के लिए "+ time +" तक मांग कर सकते हैं।  शेष समय: 00:00:00");
							else
								tvNote.setText("Note: You can place demand for "+date+" upto "+time+". Remaining Time: 00:00:00");
							
						}
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
	            }
	            public void onFinish() {

	            }
	        };
	        newtimer.start();	        
			btnCreate.setVisibility(View.VISIBLE);
			
		}
		else
		{
			btnCreate.setVisibility(View.GONE);
			dba.open();
			tvHeader.setText(Html.fromHtml("<font color=#000000> "+common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", dba.getDemandDate())+ " "+header+"</font>"));
			dba.close();
		}

		//To hide right arrow
		try {
			dba.open();
			Date date1 = dateFormat_database.parse(dba.getDemandDate());
			Date date2 = dateFormat_database.parse(dba.getDate());
			dba.close();
			different = date2.getTime() - date1.getTime();
			Log.i("different", String.valueOf(different));

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(different < 0)
			buttonRight.setVisibility(View.GONE);
		else
			buttonRight.setVisibility(View.VISIBLE);


		//Event hander of left arrow demand button
		buttonLeft.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				dba.open();
				String demand = dba.getDemandDate();
				dba.close();

				//To bind default demand date
				final Calendar c = Calendar.getInstance();
				try {
					c.setTime(dateFormat_database.parse(demand));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				c.add(Calendar.DATE,-1);
				//tvHeader.setText(dateFormat_display.format(c.getTime()));
				String demandDate  = dateFormat_database.format(c.getTime());
				dba.open();
				dba.Update_DemandDate(demandDate);
				dba.close();

				if(common.isConnected())
				{
					String[] params = {demandDate};
					//call method of view demand json web service
					AsyncViewDemandWSCall task = new AsyncViewDemandWSCall();
					task.execute(params);	
				}
			}
		});

		//Event hander of left arrow demand button
		buttonRight.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				dba.open();
				String demand = dba.getDemandDate();
				dba.close();

				//To bind default demand date
				final Calendar c = Calendar.getInstance();
				try {
					c.setTime(dateFormat_database.parse(demand));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				c.add(Calendar.DATE, 1);
				String demandDate  = dateFormat_database.format(c.getTime());
				dba.open();
				dba.Update_DemandDate(demandDate);
				dba.close();

				if(common.isConnected())
				{
					String[] params = {demandDate};
					//call method of view demand json web service
					AsyncViewDemandWSCall task = new AsyncViewDemandWSCall();
					task.execute(params);	
				}
			}
		});

		//Event hander of create demand button
		btnCreate.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if(common.isConnected())
				{
					//To clean demand table
					dba.open();
					dba.DeleteMasterData("Demand");
					dba.DeleteMasterData("DemandDetails");
					dba.close();
					//call method of create demand json web service
					AsyncUploadErrorsWSCall task = new AsyncUploadErrorsWSCall();
					task.execute();	
				}
				else{
					common.showToast("Unable to connect to Internet !");						
				}
			}
		});
		dba.open();
		//To get demand details from database
		ArrayList<HashMap<String, String>> lables = dba.getViewDemandDetails(id, lang);	
		dba.close();
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Item", String.valueOf(lable.get("Item"))); 
				hm.put("DQty", String.valueOf(lable.get("DemandQty")));
				hm.put("Type", String.valueOf(lable.get("PackingType")));
				hm.put("Rate", String.valueOf(lable.get("Rate")));

				totalAmounts =totalAmounts+ Double.valueOf(lable.get("DemandQty"))*Double.valueOf(lable.get("Rate"));
				HeaderDetails.add(hm); 
			}
		}
		if(lsize==0)
		{
			//To display no record message 
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
			listViewMainFooter.setVisibility(View.GONE);
		}
		else
		{
			if(userRole.equalsIgnoreCase("Customer"))
			{
				btnCreate.setVisibility(View.VISIBLE);
			}
			//To bind data and display list view
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			listViewMainFooter.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityDemandViewDetail.this);
			listViewMain.setAdapter(ListAdapter);
			if(lang.equalsIgnoreCase("hi"))
				tvTotalAmount.setText(Html.fromHtml("<b>कुल: "+common.stringToTwoDecimal(String.valueOf(totalAmounts))+"</b>"));
			else
				tvTotalAmount.setText(Html.fromHtml("<b>Total: "+common.stringToTwoDecimal(String.valueOf(totalAmounts))+"</b>"));
		}		
	}

	//To make class of demand view holder
	public class MainAdapter extends BaseAdapter {
		class ViewHolder {
			TextView tvItem, tvDQty, tvRate, tvAmount; 
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
				arg1 = mInflater.inflate(R.layout.activity_demand_view_detail_item, null);
				holder = new ViewHolder();

				holder.tvItem = (TextView)arg1.findViewById(R.id.tvItem);
				holder.tvDQty = (TextView)arg1.findViewById(R.id.tvDQty);	
				holder.tvRate = (TextView)arg1.findViewById(R.id.tvRate);	
				holder.tvAmount = (TextView)arg1.findViewById(R.id.tvAmount);	
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			//To bind data into view holder
			holder.tvItem.setText(HeaderDetails.get(arg0).get("Item"));
			holder.tvDQty.setText(HeaderDetails.get(arg0).get("DQty").replace(".0", ""));
			holder.tvRate.setText(common.stringToTwoDecimal(HeaderDetails.get(arg0).get("Rate")));
			holder.tvAmount.setText(common.stringToTwoDecimal(String.valueOf(Double.parseDouble(holder.tvDQty.getText().toString())* Double.parseDouble(holder.tvRate.getText().toString().replace(",", "")))));


			//Code to check if row is even or odd and set set color for alternate rows
			/*if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));  
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/
			return arg1;
		}

	}

	//To make web service class to upload error
	private class AsyncUploadErrorsWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewDetail.this);
		@Override
		protected String doInBackground(String... params) {
			try {
				responseJSON="";
				dba.open();
				ArrayList<HashMap<String, String>> errorList = dba.getErrorList();  

				if (errorList != null && errorList.size() > 0) {
					JSONArray array = new JSONArray();
					try {

						//To make json error list 
						for (HashMap<String, String> error : errorList) {
							JSONObject jsonError = new JSONObject();
							jsonError.put("Id", error.get("Id"));
							jsonError.put("UserId", error.get("UserId"));
							jsonError.put("ErrorMessage", error.get("ErrorMessage"));
							jsonError.put("ActivityName", error.get("ActivityName"));
							jsonError.put("CalledMethod", error.get("CalledMethod"));
							jsonError.put("PhoneInfo", error.get("PhoneInfo"));
							jsonError.put("CreateOn", error.get("CreateOn"));
							array.put(jsonError);
						}

						String sendJSon = array.toString();
						//To invoke method of error to post on server
						responseJSON = common.invokeJSONWS(sendJSon,"json","LogAndroidError", common.url );
					} catch (SocketTimeoutException e){
						dba.insertExceptions("TimeOut Exception. Internet is slow", "ActivityDemandViewSummary.java","AsyncUploadErrorsWSCall");
						return "ERROR: TimeOut Exception. Internet is slow";
					}catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						dba.insertExceptions(e.getMessage(), "ActivityDemandViewSummary.java","AsyncUploadErrorsWSCall");
						return "ERROR: "+e.getMessage();
					}
				}  
				else
				{
					return "No Errors!";
				}
				return responseJSON;				
			} 
			catch (Exception e) 
			{
				// TODO: handle exception
				e.printStackTrace();
				dba.insertExceptions(e.getMessage(), "ActivityDemandViewSummary.java","AsyncUploadErrorsWSCall");
				return "ERROR: "+e.getMessage();
			} 
		}

		//After execution of web service for error list
		@Override
		protected void onPostExecute(String result) {
			Dialog.dismiss();
			try {
				//To display message after response from server
				if(result.contains("ERROR") || result.contains("Server Error"))
				{					
					common.showAlert(ActivityDemandViewDetail.this, "Unable to get response from server.",false);
				}
				else
				{
					dba.open();
					dba.deleteErrors();
					dba.close();
					if(common.isConnected())
					{
						//To call web services to validate user is allowed to make demand or not 
						AsyncDemandCutOffWSCall task = new AsyncDemandCutOffWSCall();
						task.execute();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Please wait ..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//Class to handle demand cut off web services call as separate thread
	//to validate user is allowed to make demand or not
	private class AsyncDemandCutOffWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewDetail.this);
		@Override
		protected String doInBackground(String... params) {
			try {	

				//Call method of web service to download demand cut off masters from server
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadDemandCutOff", userId, userRole};

				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadDemandCutOff", common.url);	
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			} 

		}

		//After execution of demand cut off web service
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					//To display message after response from server
					if(responseJSON.equalsIgnoreCase("0"))
						common.showAlert(ActivityDemandViewDetail.this, "Demand for today is closed!", false);
					else
					{
						if(common.isConnected())
						{
							//To call web services to get list of product 
							AsyncItemWSCall task = new AsyncItemWSCall();
							task.execute();
						}
					}
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityDemandViewDetail.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityDemandViewDetail.this,"DemandCutOff Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading DemandCutOff..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}


	//Class to handle product web services call as separate thread
	private class AsyncItemWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewDetail.this);
		@Override
		protected String doInBackground(String... params) {
			try {	

				//Call method of web service to download product masters from server
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadItem", userId, userRole};

				responseJSON="";
				responseJSON = common.CallJsonWS(name, value, "ReadMaster", common.url);	
				return "";
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+ e.getMessage();
			} 

		}

		//After execution of product web service
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					//To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("Item");
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String getId= jsonArray.getJSONObject(i).getString("A");
						String getName= jsonArray.getJSONObject(i).getString("B");
						String getType= jsonArray.getJSONObject(i).getString("C");
						String getRate= jsonArray.getJSONObject(i).getString("D");
						String getNameLocal= jsonArray.getJSONObject(i).getString("E");
						String getProductName= jsonArray.getJSONObject(i).getString("F");
						String getSkuUnit= jsonArray.getJSONObject(i).getString("G");
						String getUom= jsonArray.getJSONObject(i).getString("H");
						dba.Insert_Item(getId, getName, getType, getRate, getNameLocal, getProductName, getSkuUnit, getUom);
					}
					dba.close();
					Intent intent = new Intent(context,ActivityCreateDemand.class);
					startActivity(intent);
					finish();
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityDemandViewDetail.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityDemandViewDetail.this,"Item Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Item..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}


	//Class to handle demand web service call as separate thread
	private class AsyncViewDemandWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewDetail.this);
		@Override
		protected String doInBackground(String... params) {
			try {					
				String[] name = {"action", "userId", "role", "demandDate"};
				String[] value = {"ReadDemand", userId, userRole, params[0]};
				responseJSON="";				
				//Call method of web service to download demand from server
				responseJSON = common.CallJsonWS(name, value, "ReadDemandMaster", common.url);	
				return params[0];
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: "+ "Unable to get response from server.";
			} 

		}

		//After execution of web service for demand 
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					//To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ViewDemand");
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String id= jsonArray.getJSONObject(i).getString("A");
						String name= jsonArray.getJSONObject(i).getString("B");
						dba.Insert_ViewDemand(id, name);
					}
					dba.close();
					if(common.isConnected())
					{
						//Call web services of demand / allocation details
						AsyncViewDemandDetailsWSCall task = new AsyncViewDemandDetailsWSCall();
						task.execute(result);	
					}
					else
						common.showToast("Unable to connect to Internet !");
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityDemandViewDetail.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityDemandViewDetail.this,"Demand Downloading failed: " +"Unable to get response from server.", false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Demand..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//Class to handle demand details web service call as separate thread
	private class AsyncViewDemandDetailsWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewDetail.this);
		@Override
		protected String doInBackground(String... params) {
			try {					
				String[] name = {"action", "userId", "role", "demandDate"};
				String[] value = {"ReadDemandDetails", userId, userRole, params[0]};
				responseJSON="";				
				//Call method of web service to download demand details from server
				responseJSON = common.CallJsonWS(name, value, "ReadDemandMaster", common.url);	
				return params[0];
			}
			catch (SocketTimeoutException e){
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: "+"Unable to get response from server.";
			} 
		}

		//After execution of web service for demand details
		@Override
		protected void onPostExecute(String result) {
			try {
				if(!result.contains("ERROR: "))
				{
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ViewDemandDetails");
					for (int i = 0; i < jsonArray.length(); ++i)
					{
						String id= jsonArray.getJSONObject(i).getString("A");
						String item= jsonArray.getJSONObject(i).getString("B");
						String qty= jsonArray.getJSONObject(i).getString("C");
						String packingType= jsonArray.getJSONObject(i).getString("D");
						String rate= jsonArray.getJSONObject(i).getString("E");
						String getNameLocal= jsonArray.getJSONObject(i).getString("F");
						String getProductName= jsonArray.getJSONObject(i).getString("G");
						String getSkuUnit= jsonArray.getJSONObject(i).getString("H");
						String getUom= jsonArray.getJSONObject(i).getString("I");
						dba.Insert_ViewDemandDetails(id, item, qty, packingType, rate, getNameLocal, getProductName, getSkuUnit, getUom);
					}
					dba.close();
					Intent intent;					
					intent = new Intent(context, ActivityDemandViewDetail.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					if (userRole.equalsIgnoreCase("Customer"))
						intent.putExtra("Id", userId);	
					else
					{
						intent.putExtra("Id", id);
						intent.putExtra("Header", header);						
					}
					finish();
					startActivity(intent);	
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityDemandViewDetail.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityDemandViewDetail.this,"Demand Downloading failed: " +"Unable to get response from server.", false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Demand..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}


	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent  intent;
		if (userRole.equalsIgnoreCase("Customer"))		
			intent = new Intent(this, ActivityHomeScreen.class);
		else
			intent = new Intent(this, ActivityDemandViewSummary.class);
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
			if (userRole.equalsIgnoreCase("Customer"))		
				intent = new Intent(this, ActivityHomeScreen.class);
			else
				intent = new Intent(this, ActivityDemandViewSummary.class);
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
