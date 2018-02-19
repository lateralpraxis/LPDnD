package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lateralpraxis.lpdnd.types.CustomType;

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
import android.util.DisplayMetrics;
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
import android.widget.TextView;

@SuppressLint("InflateParams") public class ActivityDemandViewSummary  extends Activity{
	private DatabaseAdapter dba;
	private Common common;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord, tvNote;
	private MainAdapter ListAdapter;
	private Intent intent;
	final Context context = this;
	UserSessionManager session;
	private static String responseJSON;
	private String userId, userRole, customerId, customer, routeId;
	private SimpleDateFormat format1, format2;
	private LinearLayout llName;
	private int cnt=0;
	private int lsize=0;
	Button btnCreate;
	CountDownTimer newtimer;
	private String lang;
	String datetime, date, time, serverTime;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demand_view_summary);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of database
		dba=new DatabaseAdapter(this);
		common = new Common(this);

		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);
		userRole = user.get(UserSessionManager.KEY_ROLES);
		routeId = user.get(UserSessionManager.KEY_ROUTEID);

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);

		//To enabled home menu at top bar



		//To create instance of date format
		format1 = new SimpleDateFormat("hh:mm:ss", Locale.US);
		format2 = new SimpleDateFormat("hh:mm aa", Locale.US);


		//To create instance of control used in page 
		HeaderDetails = new ArrayList<HashMap<String, String>>();		
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		llName = (LinearLayout) findViewById(R.id.llName);
		btnCreate = (Button)findViewById(R.id.btnCreate);
		tvNote = (TextView) findViewById(R.id.tvNote);

		//To hide customer name column
		if(userRole.equalsIgnoreCase("Customer"))
			llName.setVisibility(View.GONE);	

		dba.open();
		datetime = dba.getDemandCutOff();
		date = datetime.split("~")[0];
		time = datetime.split("~")[1];
		serverTime= datetime.split("~")[2];
		dba.close();
		/*//To create instance of date format
		formatter = new SimpleDateFormat("hh:mm", Locale.US);
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(formatter.parse(time));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long startTime = c.getTimeInMillis();	
		//Timer to display changing time
		newtimer = new CountDownTimer(startTime, 1000) { 
			public void onTick(long millisUntilFinished) {
				tvNote.setText("Note: You can place demand for "+date+" upto "+time+". Remaining Time: "+String.format("%02d:%02d:%02d", ((millisUntilFinished/1000) / (60 * 60)) % 24, ((millisUntilFinished/1000) / 60) % 60, (millisUntilFinished/1000)%60));
			}
			public void onFinish() {
			}
		};
		newtimer.start();*/

		//Timer to display changing time work with -ve not work on 00:00
		CountDownTimer newtimer = new CountDownTimer(1000000000, 1000) { 
			public void onTick(long millisUntilFinished) {
				try 
				{			
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

					String diff = String.format("%02d:%02d:%02d", Hours, Mins, Secs);// updated value every1 second
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
//		if(lang.equalsIgnoreCase("hi"))
//			tvNote.setText("नोट: आप "+ date +" "+ time +" तक मांग कर सकते हैं।");
//		else
//			tvNote.setText("Note: You can place demand for "+date+" upto "+time+".");
		
		//tvNote.setText("Note: You can place demand for "+date+" upto "+time);

		//To handle a callback to be invoked when an item in this AdapterView has been clicked
		listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			//On click of list view item
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
				customerId = ((TextView)view.findViewById(R.id.tvId)).getText().toString();
				customer = ((TextView)view.findViewById(R.id.tvName)).getText().toString(); 
				//Call web services of View Demand Details
				AsyncViewDemandDetailsWSCall task = new AsyncViewDemandDetailsWSCall();
				task.execute();	
			}
		});


		//To get all demand and bind list view
		dba.open();
		List<CustomType> lables = dba.GetMasterDetailsByLang("customerByRoute", routeId, lang);	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (CustomType lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				if(!lable.toString().contains("..."))
				{
				String[] arr = lable.getId().split("~");
				if(arr.length>1)
				{
					Log.i("LPDND", "CustomerId="+String.valueOf(arr[0]));
					hm.put("Id", String.valueOf(arr[0])); 
					hm.put("Name", String.valueOf(lable.getName()));
					HeaderDetails.add(hm); 
					//customerRouteId = arr[1];
					//customerRoute = arr[2];
				}
				}
			}
		}
		dba.close();

		if(lsize==0)
		{
			//To display list view of demand
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To display no record found
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityDemandViewSummary.this);
			listViewMain.setAdapter(ListAdapter);
		}

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
	}	


	//To make view holder to display on screen
	public class MainAdapter extends BaseAdapter {

		class ViewHolder {		
			LinearLayout llName;
			TextView tvId, tvName; 
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
				arg1 = mInflater.inflate(R.layout.activity_demand_view_summary_item, null);
				holder = new ViewHolder();
				holder.llName = (LinearLayout)arg1.findViewById(R.id.llName);
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

			//To hide customer column in customer role
			if(userRole.equalsIgnoreCase("Customer"))
				holder.llName.setVisibility(View.GONE);


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
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewSummary.this);
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
					common.showAlert(ActivityDemandViewSummary.this, "Unable to get response from server.",false);
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
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewSummary.this);
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
						common.showAlert(ActivityDemandViewSummary.this, "Demand for today is closed!", false);
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
					common.showAlert(ActivityDemandViewSummary.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityDemandViewSummary.this,"DemandCutOff Downloading failed: " +e.toString(), false);
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
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewSummary.this);
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
					intent = new Intent(context,ActivityCreateDemand.class);
					startActivity(intent);
					finish();
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityDemandViewSummary.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityDemandViewSummary.this,"Item Downloading failed: " +e.toString(), false);
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


	//Class to handle demand details web service call as separate thread
	private class AsyncViewDemandDetailsWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityDemandViewSummary.this);
		@Override
		protected String doInBackground(String... params) {
			try {	

				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
				//To bind default demand date
				final Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE,1);
				String demandDate = dateFormatter.format(c.getTime());
				dba.open();
				dba.Update_DemandDate(demandDate);
				dba.close();

				String[] name = {"action", "userId", "role", "demandDate"};
				String[] value = {"ReadDemandDetails", userId, userRole, demandDate};
				responseJSON="";				
				//Call method of web service to download demand details from server
				responseJSON = common.CallJsonWS(name, value, "ReadDemandMaster", common.url);	
				return "";
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
					if (userRole.equalsIgnoreCase("Customer"))
						intent.putExtra("Id", userId);
					else
					{
						intent.putExtra("Id", customerId);
						intent.putExtra("Header", customer);
					}	
					finish();
					startActivity(intent);	
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityDemandViewSummary.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityDemandViewSummary.this,"Demand Downloading failed: " +"Unable to get response from server.", false);
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
