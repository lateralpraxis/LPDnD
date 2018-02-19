package lateralpraxis.lpdnd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import lateralpraxis.lpdnd.types.CustomType;


@SuppressLint("InflateParams") public class ActivityCreateDemand extends ListActivity {
	private Button btnGo, btnCreate;
	private Common common;
	UserSessionManager session;
	private DatabaseAdapter dba;
	private String newDemandId ="";
	private String[] arrTemp;
	private SimpleDateFormat dateFormatter;
	private LinearLayout llCustomer;
	private TextView tvFromDate, tvNoRecord, tvTotalAmount;
	private Spinner spCustomer;
	private String imei;
	private String responseJSON,sendJSon;
	private String customerId="0", customer, customerWithRoute;
	private String userId, userRole, routeId;
	double totalQty=0;
	final Context context = this;
	private String lang;
	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_demand);

		common = new Common(getApplicationContext());
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


		//To read user role from user session manager
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);
		userRole = user.get(UserSessionManager.KEY_ROLES);
		routeId = user.get(UserSessionManager.KEY_ROUTEID);
		Log.i("LPDND", "routeId="+routeId);
		String[] arr = routeId.split(",");
		if(arr.length>1)
		{
			routeId = arr[0];
		}
		

		//to create object of controls 
		llCustomer = (LinearLayout)findViewById(R.id.llCustomer);
		btnGo= (Button) findViewById(R.id.btnGo);
		btnCreate= (Button) findViewById(R.id.btnCreate);
		tvTotalAmount 	= (TextView)findViewById(R.id.tvTotalAmount);
		tvNoRecord = (TextView)findViewById(R.id.tvNoRecord);	

		//To create instance of database 
		dba=new DatabaseAdapter(this);
		dba.open();

		//To create instance of control used in page 
		spCustomer = (Spinner) findViewById(R.id.spCustomer);
		spCustomer.setAdapter(DataAdapter("customerByRoute", routeId, lang));

		//Event handler to be invoked when an item is select
		spCustomer.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			//On select of customer from drop down list
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				setListAdapter(null);
				btnCreate.setVisibility(View.GONE);
				tvNoRecord.setVisibility(View.GONE);
				tvTotalAmount.setText("");
				customerWithRoute = ((CustomType) spCustomer.getSelectedItem()).getId();
				String[] arr = customerWithRoute.split("~");
				if(arr.length>1)
				{
					customerId = arr[0];
					//customerRouteId = arr[1];
					//customerRoute = arr[2];
				}
				customer =((CustomType)spCustomer.getSelectedItem()).getName();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		//To create instance of date format to display on screen
		dateFormatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
		tvFromDate = (TextView)findViewById(R.id.tvFromDate);

		//To bind default demand date
		final Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE,1);
		tvFromDate.setText(dateFormatter.format(c.getTime()));

		//To bind product
		if(userRole.equalsIgnoreCase("Customer"))
		{
			//To hide customer column
			llCustomer.setVisibility(View.GONE);
			GetProduct();
		}
		else
			llCustomer.setVisibility(View.VISIBLE);

		//Event hander of go button
		btnGo.setOnClickListener(new View.OnClickListener() {
			//When go button click
			@Override
			public void onClick(View arg0) {
				tvTotalAmount.setText("");
				//To validate required field
				if((customerId.equalsIgnoreCase("0") ||customerId ==null) && userRole.equalsIgnoreCase("Route Officer") && lang.equalsIgnoreCase("en"))
					common.showToast("Customer is mandatory.");
				else if((customerId.equalsIgnoreCase("0") ||customerId ==null) && userRole.equalsIgnoreCase("Route Officer") && lang.equalsIgnoreCase("hi"))
					common.showToast("ग्राहक अनिवार्य है।");
				else
				{
					//To call web services to get list of product 
					AsyncItemWSCall task = new AsyncItemWSCall();
					task.execute();										
				}
			}
		});

		//Event hander to create demand button
		btnCreate.setOnClickListener(new View.OnClickListener() {
			//When create button click
			@Override
			public void onClick(View arg0) {

				//To validate required field and please enter at least one quantity!
				int zeroCount=0, totalRow=0;
				int invalidCount = 0;
				for(int i =0;i<getListView().getChildCount();i++)
				{
					totalRow++;
					View v = getListView().getChildAt(i);
					EditText etQty = (EditText)v.findViewById(R.id.etQty);
					if (etQty.getText().toString().equalsIgnoreCase("."))
						invalidCount=invalidCount+1;

					if(!etQty.getText().toString().equalsIgnoreCase("."))
					{
						String qty = etQty.length() == 0? "0" :etQty.getText().toString().trim();
						if(etQty.length() == 0 || Double.parseDouble(qty)==0)
							zeroCount++;
					}
				}
				if(invalidCount>0 && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCreateDemand.this, "Please enter valid quantity!", false);
				else if(invalidCount>0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCreateDemand.this, "कृपया वैध मात्रा दर्ज करें!", false);

				else if(totalRow == zeroCount && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityCreateDemand.this, "Please enter atleast one quantity!", false);
				else if(totalRow == zeroCount && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityCreateDemand.this, "कृपया कम से कम एक मात्रा दर्ज करें!", false);

				else if(String.valueOf(tvFromDate.getText()).equals("") && lang.equalsIgnoreCase("en"))
					common.showToast("Demand Date is mandatory.");
				else if(String.valueOf(tvFromDate.getText()).equals("") && lang.equalsIgnoreCase("hi"))
					common.showToast("डिमांड डेट अनिवार्य है।");

				else if(customerId == null && userRole.equalsIgnoreCase("Route Officer") && lang.equalsIgnoreCase("en"))
					common.showToast("Customer is mandatory.");
				else if(customerId == null && userRole.equalsIgnoreCase("Route Officer") && lang.equalsIgnoreCase("hi"))
					common.showToast("ग्राहक अनिवार्य है।");
				else
				{
					if (common.isConnected()) {						
						//Confirmation message before submit data
						Builder alertDialogBuilder = new Builder(context);
						alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
						alertDialogBuilder.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to submit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes", 
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if(common.isConnected())
								{								
									try {
										//To store data in demand table
										dba.open();
										if(userRole.equalsIgnoreCase("Customer"))
										{
											HashMap<String, String> user = session.getLoginUserDetails();
											customerId = user.get(UserSessionManager.KEY_ID);
											customer = user.get(UserSessionManager.KEY_USERNAME);
										}
										String insertDemand = dba.Insert_Demand(dba.uniqueId(common.getIMEI()),customerId, customer, String.valueOf(tvFromDate.getText()), userRole );
										newDemandId = insertDemand;
										if(insertDemand.contains("success"))
										{
											for(int i =0;i<getListView().getChildCount();i++)
											{
												View v = getListView().getChildAt(i);
												TextView tvId = (TextView)v.findViewById(R.id.tvId);
												TextView tvItem = (TextView)v.findViewById(R.id.tvItemName);
												EditText etQty = (EditText)v.findViewById(R.id.etQty);
												TextView tvRate = (TextView)v.findViewById(R.id.tvRate);
												//To validate if user enter only . 
												if(!etQty.getText().toString().equalsIgnoreCase("."))
												{
													String qty = etQty.getText().toString().trim().length() == 0? "0" :etQty.getText().toString().trim();	
													if(Double.parseDouble(qty)!=0)
														dba.Insert_DemandDetails(insertDemand.split("~")[1], tvId.getText().toString(), tvItem.getText().toString(),qty, tvRate.getText().toString());
												}						
											}
										}

										dba.close();
										//Call method to post demand data
										AsyncDemandWSCall task = new AsyncDemandWSCall();
										task.execute();
									} catch (Exception e) {
										e.printStackTrace();
										common.showAlert(ActivityCreateDemand.this, "Error in Saving Demand.\n"+e.getMessage(), false);
									}
								}
								else{
									dba.insertExceptions("Unable to connect to Internet !", "ActivityCreateDemand.java","onCreate()");
								}
							}	            	
						}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}	            	
						});
						alertDialogBuilder.create().show();
					}	
					else
						common.showToast("Unable to connect to Internet!");
				}
			}
		});
	}

	//To bind product from database
	private void GetProduct()
	{		
		dba.openR();
		ArrayList<HashMap<String, String>> listItem =  dba.getItems();
		if(listItem.size()>0) {	
			//To show product and its detail
			setListAdapter(new CustomAdapter(this, listItem));
			arrTemp = new String[listItem.size()];
			ListViewHelper.getListViewSize(getListView());	
			btnCreate.setVisibility(View.VISIBLE);
			tvNoRecord.setVisibility(View.GONE);
		}
		else	
		{	
			//To show 'no records found'
			setListAdapter(null);
			btnCreate.setVisibility(View.GONE);
			tvNoRecord.setVisibility(View.VISIBLE);
		}
	}

	//To sum of amount and return as total amount
	private String GetTotal()
	{
		totalQty=0.0;
		for(int i =0;i<getListView().getChildCount();i++)
		{
			View v = getListView().getChildAt(i);			
			TextView tvAmount = (TextView)v.findViewById(R.id.tvAmount);
			Double qty = 0.0;
			if(TextUtils.isEmpty(tvAmount.getText().toString().trim()))
				qty=0.0;
			else
				qty=Double.parseDouble(tvAmount.getText().toString().replace(",",""));
			totalQty = totalQty+ qty;
		}
		return String.valueOf(totalQty);
	}

	//To make view holder to display on screen
	public class CustomAdapter extends BaseAdapter {
		private Context context2;
		class ViewHolder {
			TextView tvId, tvItemName, tvRate, tvAmount; 
			EditText etQty;
			int ref;

		}
		ArrayList<HashMap<String, String>> _listItems;

		//constructor of custom adapter class
		public CustomAdapter(Context context,
				ArrayList<HashMap<String, String>> listItem) {
			this.context2 = context;
			mInflater = LayoutInflater.from(context2);
			_listItems = listItem;
		}

		private LayoutInflater mInflater;
		@Override

		//To get item list count
		public int getCount() {
			return _listItems.size();
		}

		//To get item name
		@Override
		public Object getItem(int arg0) {
			return _listItems.get(arg0);
		}

		//To get item id
		@Override
		public long getItemId(int arg0) { 
			return arg0;
		}

		//To get item name
		@Override
		public int getViewTypeCount() {
			return getCount();
		}

		//To get item position
		@Override
		public int getItemViewType(int position) {
			return position;
		}

		//To instantiate layout XML file into its corresponding view objects.
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {			
			final ViewHolder holder;
			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.activity_create_demand_item, null);
				holder = new ViewHolder();

				holder.tvId = (TextView)arg1.findViewById(R.id.tvId);
				holder.tvItemName = (TextView)arg1.findViewById(R.id.tvItemName);
				holder.tvRate = (TextView)arg1.findViewById(R.id.tvRate);
				holder.etQty = (EditText)arg1.findViewById(R.id.etQty);
				holder.tvAmount=(TextView)arg1.findViewById(R.id.tvAmount);
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			holder.ref = arg0;

			//To bind data from list
			holder.tvId.setText(_listItems.get(arg0).get("Id"));
			holder.tvItemName.setText(lang.equalsIgnoreCase("hi")?_listItems.get(arg0).get("ProductLocal"): _listItems.get(arg0).get("Name"));			
			holder.tvRate.setText(common.stringToTwoDecimal(_listItems.get(arg0).get("Rate")));
			if(arrTemp[arg0] != null)
				holder.etQty.setText(String.format("%.1f", Double.parseDouble(arrTemp[arg0])));
			if(_listItems.get(arg0).get("Type").length()<1)
			{
				//To display decimal point in number key board control
				holder.etQty.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,1)});
				holder.etQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
			}
			else
			{
				//To display only number in key board control
				int maxLength = 3;
				InputFilter[] FilterArray = new InputFilter[1];
				FilterArray[0] = new InputFilter.LengthFilter(maxLength);
				holder.etQty.setFilters(FilterArray);
				holder.etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
			}
			//holder.tvType.setText(_listItems.get(arg0).get("Type"));

			// Instantiates a TextWatcher, to observe value changes and trigger the result calculation
			TextWatcher textWatcher = new TextWatcher() {
				public void afterTextChanged(Editable s) {

					if (!holder.etQty.getText().toString().equalsIgnoreCase(".")) {
						if (holder.etQty.getText().toString().equalsIgnoreCase("."))
							holder.etQty.setText("");
						if (holder.etQty.getText().toString().trim().length() > 0) 
						{
							if (Double.parseDouble(holder.etQty.getText().toString())== 0)
							{
								holder.etQty.setText("");
								holder.tvAmount.setText("");
							}
							else
							{
								Double totAmt= Double.parseDouble(holder.etQty.getText().toString())*Double.parseDouble(holder.tvRate.getText().toString().replace(",", ""));
								holder.tvAmount.setText(common.stringToTwoDecimal(String.valueOf(totAmt)));
							}
						}
						else
						{
							holder.tvAmount.setText("");
						}
					}else
					{
						holder.tvAmount.setText("");
					}


					/*if(holder.etQty.getText().toString().equalsIgnoreCase(".") && holder.etQty.getText().length()==1)
					{
						holder.etQty.setText("");
						holder.tvAmount.setText("");
					}
					if(!holder.etQty.getText().toString().equalsIgnoreCase("."))
					{
						if (holder.etQty.getText().length()>0)	
						{
							if(Pattern.matches(fpRegex, holder.etQty.getText()))
							{
								if (Double.parseDouble(holder.etQty.getText().toString())== 0)
								{
									holder.etQty.setText("");
									holder.tvAmount.setText("");
								}
								else
								{
									//common.stringToTwoDecimal(
									Double totAmt= Double.parseDouble(holder.etQty.getText().toString())*Double.parseDouble(holder.tvRate.getText().toString().replace(",", ""));
									holder.tvAmount.setText(common.stringToTwoDecimal(String.valueOf(totAmt)));
								}
							}
							else
							{
								holder.etQty.setText("");
								holder.tvAmount.setText("");	
							}
						}
						else
						{
							holder.tvAmount.setText("");				
						}								
					}*/
					//To display total amount in footer of amount
					if(lang.equalsIgnoreCase("hi"))
				tvTotalAmount.setText(Html.fromHtml("<b>कुल: "+common.stringToTwoDecimal(GetTotal())+"</b>"));
			else
				tvTotalAmount.setText(Html.fromHtml("<b>Total: "+common.stringToTwoDecimal(GetTotal())+"</b>"));

				}
				public void beforeTextChanged(CharSequence s, int start, int count, int after){}
				public void onTextChanged(CharSequence s, int start, int before, int count){}
			};

			// Adds the TextWatcher as TextChangedListener to both EditTexts
			holder.etQty.addTextChangedListener(textWatcher);

			// Fix for text selection handle not disappearing
			holder.etQty.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View view2, boolean hasFocus) {
					view2.dispatchWindowFocusChanged(hasFocus); 
				}
			});		

			/*//Code to calculate total amount
			holder.etQty.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus)
					{
						if(Pattern.matches(fpRegex, holder.etQty.getText()))
						{
							//To display total amount in footer of amount
							tvTotalAmount.setText(Html.fromHtml("<b>Total: "+common.stringToTwoDecimal(GetTotal())+"</b>"));
						}
						else
							holder.etQty.setText("");

					}
				}
			});*/
			return arg1;
		}
	}	

	//Class to handle product web services call as separate thread
	private class AsyncItemWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCreateDemand.this);
		@Override
		protected String doInBackground(String... params) {
			try {	
				String[] name = {"action", "userId", "role"};
				String[] value = {"ReadItem", customerId, userRole};
				//Call method of web service to download product masters from server
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
					//To call product method 
					GetProduct();	
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCreateDemand.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityCreateDemand.this,"Item Downloading failed: " +e.toString(), false);
			}
			Dialog.dismiss(); 
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Product..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//To make web service class to post data of demand 
	private class AsyncDemandWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCreateDemand.this);
		@Override
		protected String doInBackground(String... params) {
			// These two need to be declared outside the try/catch
			// so that they can be closed in the finally block.
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;

			try {
				URL url = new URL("http://wtfismyip.com/text");

				// Create the request to open the connection
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
				InputStream inputStream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				if (inputStream == null) {
					// Nothing to do.
					return null;
				}
				reader = new BufferedReader(new InputStreamReader(inputStream));

				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}


				imei = common.getIMEI();
				HashMap<String, String> user = session.getLoginUserDetails();
				dba.open();
				JSONObject jsonDemand = new JSONObject();

				//to get demand from database
				ArrayList<HashMap<String, String>> insmast = dba.getAllDemand();                         
				if (insmast != null && insmast.size() > 0) {
					JSONArray array = new JSONArray();
					try {

						//To make json string to post demand
						for (HashMap<String, String> insp : insmast) {
							JSONObject jsonins = new JSONObject();
							jsonins.put("CustomerId", insp.get("CustomerId"));						
							jsonins.put("UserId", user.get(UserSessionManager.KEY_ID));
							jsonins.put("ipAddress", (buffer.length() == 0)?"-":buffer.toString());
							jsonins.put("Machine", imei);
							jsonins.put("UniqueId", insp.get("UniqueId"));
							jsonins.put("DemandDate", String.valueOf(tvFromDate.getText()));
							jsonins.put("Role", insp.get("Role"));
							array.put(jsonins);
						}

						jsonDemand.put("Demand",array);					
						dba.close();

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						return "ERROR: "+e.getMessage();
					}
					finally
					{
						dba.close();
					}

					dba.open();
					JSONObject jsonDetails = new JSONObject();
					//To get demand details from database
					ArrayList<HashMap<String, String>> insdet = dba.getDemandDetails();                         
					if (insdet != null && insdet.size() > 0) {

						//To make json string to post demand details
						JSONArray arraydet = new JSONArray();
						try {
							for (HashMap<String, String> insd : insdet) {
								JSONObject jsondet = new JSONObject();
								jsondet.put("ItemId", insd.get("ItemId"));
								jsondet.put("Rate", insd.get("Rate"));
								jsondet.put("Quantity", insd.get("Qty"));
								arraydet.put(jsondet);
							}

							jsonDetails.put("DemandDetails",arraydet);
							dba.close();
						}
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						finally
						{
							dba.close();
						}
					}
					sendJSon = jsonDemand+"~"+jsonDetails;

					//To invoke json web service to create demand 
					responseJSON = common.invokeJSONWS(sendJSon,"json","CreateDemand", common.url );
				}  
				else
				{
					return "No demand pending to be send.";
				}
				return responseJSON;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: "+e.getMessage();
			} finally {dba.close(); }


		}

		//After execution of json web service to create demand
		@Override
		protected void onPostExecute(String result) {

			try {
				Log.i("AsyncDemandWSCall onPostExecute result=", result);

				dba.open();
				dba.DeleteMasterData("Demand");
				dba.DeleteMasterData("DemandDetails");
				dba.close();

				//To display message after response from server
				if(!result.contains("ERROR: "))
				{
					if(result.split("~")[1].toString().equalsIgnoreCase("0"))
						common.showAlert(ActivityCreateDemand.this, "Demand for today is closed!", false);
					else
					{
						common.showToast("Demand Created Successfully.");
						if(common.isConnected())
						{
							//call method of view demand json web service
							AsyncViewDemandWSCall task = new AsyncViewDemandWSCall();
							task.execute();	
						}
					}
				}
				else
				{
					if(result.contains("null"))
						result="Server not responding.";					
					dba.open();
					dba.DeleteDemand(newDemandId);
					dba.close();
					common.showAlert(ActivityCreateDemand.this, result,false);
					common.showToast("Error: "+result);
					Intent i = new Intent(ActivityCreateDemand.this, ActivityHomeScreen.class);							
					finish();
					startActivity(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityCreateDemand.this,"Synchronizing failed: " +e.toString(),false);
				common.showToast("Synchronizing failed: "+e.toString());
				Intent i = new Intent(ActivityCreateDemand.this, ActivityHomeScreen.class);							
				finish();
				startActivity(i);
			}
			Dialog.dismiss();
		}

		//To display message on screen within process
		@Override
		protected void onPreExecute() {
			Log.i("onPreExecute", "onPreExecute");
			Dialog.setMessage("Posting Demand...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
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

	//To create menu on inflater
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		return true;
	}

	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent homeScreenIntent = new Intent(this, ActivityHomeScreen.class);
		homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeScreenIntent);
		finish();
	}

	//Class to handle demand web service call as separate thread
	private class AsyncViewDemandWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityCreateDemand.this);
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
				String[] value = {"ReadDemand", userId, userRole, demandDate};
				responseJSON="";				
				//Call method of web service to download demand from server
				responseJSON = common.CallJsonWS(name, value, "ReadDemandMaster", common.url);	
				return "";
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
						task.execute();	
					}
					else
						common.showToast("Unable to connect to Internet !");
				}
				else
				{
					if(result.contains("null") || result =="")
						result ="Server not responding. Please try again later.";
					common.showAlert(ActivityCreateDemand.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityCreateDemand.this,"Demand Downloading failed: " +"Unable to get response from server.", false);
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
		private ProgressDialog Dialog = new ProgressDialog(ActivityCreateDemand.this);
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
					common.showAlert(ActivityCreateDemand.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityCreateDemand.this,"Demand Downloading failed: " +"Unable to get response from server.", false);
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

	private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter, String lang)
	{
		dba.open();
		List <CustomType> lables = dba.GetMasterDetailsByLang(masterType, filter, lang);
		ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this,android.R.layout.simple_spinner_item, lables);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dba.close();
		return dataAdapter;
	}
}
