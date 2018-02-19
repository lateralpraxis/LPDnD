package lateralpraxis.lpdnd;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("InflateParams") public class ActivityRORouteAllocation  extends Activity{
	private Common common;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private DatabaseAdapter dba;
	private ListView listviewAllocation;
	private TextView tvNoRecord,tvTopHeader,tvHeader;
	final Context context = this;
	UserSessionManager session;
	private int cnt=0, cntComp=0;
	private String imei;
	Button btnCreate;
	private String routeId,routeName, responseJSON,lang,userId;
	private static String  responseJSON1, sendJSon,newId;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ro_route_allocation);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		common = new Common(this);
		// create instance of the database
		dba = new DatabaseAdapter(this);
		listviewAllocation = (ListView) findViewById(R.id.listAllocation);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		tvTopHeader= (TextView) findViewById(R.id.tvTopHeader);
		tvHeader= (TextView) findViewById(R.id.tvHeader);

		//tvTopHeader.setText(tvTopHeader.getText()+" "+common.getCurrentDate());
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		imei = common.getIMEI();

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);

		//To enabled home menu at top bar


		final HashMap<String, String> user = session.getLoginUserDetails();
		routeId = user.get(UserSessionManager.KEY_ROUTEID);
		userId = user.get(UserSessionManager.KEY_ID);
		dba.openR();
		routeName=dba.getRouteNameById(routeId);

		/*Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			routeId = extras.getString("routeId");
			routeName =extras.getString("routeName");
			String strRoute =lang.equalsIgnoreCase("hi") ?"मार्ग ":"Route: ";
			tvHeader.setText(Html.fromHtml("<b>"+strRoute+"</b>"+routeName.replace("\u2022", "")));
		}*/
		if (common.isConnected()) {
			
			String[] myTaskParams = { "transactions" };
			//call method of get customer json web service
			AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
			task.execute(myTaskParams);	
		
		}
	}	

	public static class viewHolder {
		TextView tvCompany, tvSKUName, tvAlQty, tvDeliQty,
		tvBalanaceQty,tvRetQty;//tvCFQty,

	}

	private class CustomAdapter extends BaseAdapter {
		private Context context2;
		LayoutInflater inflater;
		ArrayList<HashMap<String, String>> _listAllocation;

		public CustomAdapter(Context context,
				ArrayList<HashMap<String, String>> listAlloc) {
			this.context2 = context;
			inflater = LayoutInflater.from(context2);
			_listAllocation = listAlloc;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return _listAllocation.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final viewHolder holder;
			cnt= cnt+1;
			cntComp= cntComp+1;
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.list_ro_route_allocations, null);
				holder = new viewHolder();

				convertView.setTag(holder);

			} else {
				holder = (viewHolder) convertView.getTag();
			}
			holder.tvCompany = (TextView) convertView
					.findViewById(R.id.tvCompany);
			holder.tvSKUName = (TextView) convertView
					.findViewById(R.id.tvItem);
			//holder.tvCFQty = (TextView) convertView.findViewById(R.id.tvCFQty);
			holder.tvAlQty = (TextView) convertView.findViewById(R.id.tvALQty);
			holder.tvDeliQty = (TextView) convertView
					.findViewById(R.id.tvDLQty);
			holder.tvRetQty= (TextView) convertView
					.findViewById(R.id.tvRetQty);
			holder.tvBalanaceQty = (TextView) convertView
					.findViewById(R.id.tvBalQty);

			final HashMap<String, String> itemAllocation = _listAllocation
					.get(position);
			if(position ==0)
			{
				cnt=1;
				holder.tvCompany.setVisibility(View.VISIBLE);
				holder.tvCompany.setText(HeaderDetails.get(position).get("Company").toString());
				cntComp=1;

			}
			else
			{	
				//Log.i(TAG,String.valueOf(HeaderDetails.get(arg0-1).get("Date").equals(HeaderDetails.get(arg0).get("Date"))));
				if(HeaderDetails.get(position-1).get("Company").equals(HeaderDetails.get(position).get("Company")))
				{
					holder.tvCompany.setVisibility(View.GONE);	
				}
				else
				{
					cnt=1;
					holder.tvCompany.setVisibility(View.VISIBLE);
					holder.tvCompany.setText(HeaderDetails.get(position).get("Company").toString());
				}

			}

			holder.tvSKUName.setText(HeaderDetails.get(position).get("SKU").toString());
			//holder.tvCFQty.setText(HeaderDetails.get(position).get("CarryForward").toString());
			holder.tvAlQty.setText(Html.fromHtml("<font color=#800000> "+ HeaderDetails.get(position).get("Allocated").toString()+"</font>"));
			holder.tvDeliQty.setText(HeaderDetails.get(position).get("Delivered").toString());
			holder.tvRetQty.setText(HeaderDetails.get(position).get("RetQty").toString());
			holder.tvBalanaceQty.setText(HeaderDetails.get(position).get("Balance").toString());

			return convertView;
		}

	}

	// Class to handle vehicle web services call as separate thread
	private class AsyncAllocationWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityRORouteAllocation.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "userId" };
				String[] value = { userId };
				responseJSON = "";
				// Call method of web service to download allocation from server
				responseJSON = common.CallJsonWS(name, value,"ReadAllocationForRoute", common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download Allocation
		@Override
		protected void onPostExecute(String result) {
			try {
				String allocDate="";
				if (!result.contains("ERROR: ")) {
					// To display vehicle after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					HeaderDetails = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < jsonArray.length(); ++i) {
						HashMap<String, String> details = new HashMap<String, String>();
						details.put("Company", jsonArray.getJSONObject(i)
								.getString("Company"));
						details.put("SKU",lang.equalsIgnoreCase("hi")?jsonArray.getJSONObject(i)
								.getString("SKUNameLocal"): jsonArray.getJSONObject(i)
								.getString("SKUName"));
						/*	details.put("CarryForward", jsonArray.getJSONObject(i)
								.getString("CFQty").replace(".0", ""));*/
						details.put("Allocated", jsonArray.getJSONObject(i)
								.getString("AlQty").replace(".0", ""));
						details.put("Delivered", jsonArray.getJSONObject(i)
								.getString("DeliQty").replace(".0", ""));
						details.put("RetQty", jsonArray.getJSONObject(i)
								.getString("ReturnQty").replace(".0", ""));
						details.put("Balance", jsonArray.getJSONObject(i)
								.getString("BalanceQty").replace(".0", ""));
						details.put("AllocationDate", jsonArray.getJSONObject(i)
								.getString("AllocationDate").replace("T", " "));
						details.put("VehicleNo", jsonArray.getJSONObject(i)
								.getString("VehicleNo"));
						details.put("RouteName", jsonArray.getJSONObject(i)
								.getString("RouteName"));						
						HeaderDetails.add(details);
						allocDate=jsonArray.getJSONObject(i)
								.getString("AllocationDate").replace("T", " ");
						
						String strRoute =lang.equalsIgnoreCase("hi") ?"मार्ग:  ":"Route: ";
						String strVehicle =lang.equalsIgnoreCase("hi") ?" वाहन: ":" Vehicle: ";
						tvHeader.setText(Html.fromHtml("<b>"+strRoute+"</b>"+jsonArray.getJSONObject(i)
								.getString("RouteName")+"<b>"+strVehicle+"</b>"+jsonArray.getJSONObject(i)
								.getString("VehicleNo")));

					}
					tvTopHeader.setText(tvTopHeader.getText()+" "+common.convertDateFormat(allocDate));
					if(jsonArray.length()!=0) {	

						listviewAllocation.setAdapter(new CustomAdapter(ActivityRORouteAllocation.this, HeaderDetails));
						tvNoRecord.setVisibility(View.GONE);
					}
					else
					{
						tvNoRecord.setVisibility(View.VISIBLE);
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityRORouteAllocation.this, result,
							false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityRORouteAllocation.this,
						"Allocation Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display vehicle on screen within process
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Allocation...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
	
	//Async Method to Validate User Current Credentials from Portal
		private class AsyncValidatePasswordWSCall extends AsyncTask<String, Void, String> {
			String source="";
			private ProgressDialog Dialog = new ProgressDialog(ActivityRORouteAllocation.this);
			@Override
			protected String doInBackground(String... params) {
				try {	// if this button is clicked, close

					source=params[0];    
					dba.openR();
					HashMap<String, String> user = session.getLoginUserDetails();
					//Creation of JSON string for posting validating data 
					JSONObject json = new JSONObject();
					json.put("username",user.get(UserSessionManager.KEY_CODE));
					json.put("password",user.get(UserSessionManager.KEY_PWD) );
					json.put("imei",imei);
					json.put("version",dba.getVersion());
					String JSONStr=json.toString();

					//Store response fetched  from server in responseJSON variable
					responseJSON= common.invokeJSONWS(JSONStr,"json","ValidatePassword",common.url );

				}
				catch (SocketTimeoutException e){
					return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
				} catch (final Exception e) {
					// TODO: handle exception
					//e.printStackTrace();
					return "ERROR: Unable to fetch response from server.";
				} 
				return "";
			}

			@Override
			protected void onPostExecute(String result) {
				try {
					//Check if result contains error
					if(!result.contains("ERROR"))
					{
						String passExpired = responseJSON.split("~")[0];
						String passServer = responseJSON.split("~")[1];
						String membershipError = responseJSON.split("~")[2];
						//Check if password is expire and open change password intent 
						if(passExpired.toLowerCase(Locale.US).equals("yes"))
						{
							Intent	intent = new Intent(context, ActivityChangePassword.class);
							intent.putExtra("fromwhere", "login");
							startActivity(intent);
							finish();
						}
						//Code to check other validations
						else if(passServer.toLowerCase(Locale.US).equals("no"))
						{
							String resp="";

							if(membershipError.toLowerCase(Locale.US).contains("NO_USER".toLowerCase(Locale.US)))
							{
								resp="There is no user in the system";
							}
							else if(membershipError.toLowerCase(Locale.US).contains("BARRED".toLowerCase(Locale.US)))
							{
								resp="Your account has been barred by the Administrator.";
							}
							else if(membershipError.toLowerCase(Locale.US).contains("LOCKED".toLowerCase(Locale.US)))
							{
								resp="Your account has been locked out because " +
										"you have exceeded the maximum number of incorrect login attempts. " +
										"Please contact the System Admin to " +
										"unblock your account.";
							}
							else if(membershipError.toLowerCase(Locale.US).contains("LOGINFAILED".toLowerCase(Locale.US)))
							{
								resp="Invalid password. " +
										"Password is case-sensitive. " +
										"Access to the system will be disabled after " + responseJSON.split("~")[3] + " " +
										"consecutive wrong attempts.\n" +
										"Number of Attempts remaining: " + responseJSON.split("~")[4];
							}		
							else
							{
								resp= "Password mismatched. Enter latest password!";
							}


							showChangePassWindow(source, resp);
						}

						//Code to check source of request
						else if(source.equals("transactions"))
						{
							//If version does not match logout user
							if(responseJSON.contains("NOVERSION"))
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(context);
								builder.setMessage("Application is running an older version. Please install latest version.!")
								.setCancelable(false)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										//Code to call async method for posting transactions
										if(common.isConnected())
										{

												//to get delivery from database
												AsyncDeliveryWSCall task = new AsyncDeliveryWSCall();
												task.execute();	
										}
									}
								});
								AlertDialog alert = builder.create();
								alert.show();

							}
							else
							{
								if(common.isConnected())
								{

										//to get delivery from database
										AsyncDeliveryWSCall task = new AsyncDeliveryWSCall();
										task.execute();	
								}
							}

						}
					}
					else
					{
						common.showAlert(ActivityRORouteAllocation.this, "Unable to fetch response from server.",false);
					}

				} catch (Exception e) {
					common.showAlert(ActivityRORouteAllocation.this,"Validating credentials failed: " +e.toString(),false);
				}
				Dialog.dismiss(); 
			}

			@Override
			protected void onPreExecute() {
				Dialog.setMessage("Validating your credentials..");
				Dialog.setCancelable(false);
				Dialog.show();

			}

			@Override
			protected void onProgressUpdate(Void... values) {

			}

		}


		//To make web service class to post data of delivery 
		private class AsyncDeliveryWSCall extends AsyncTask<String, Void, String> {
			private ProgressDialog Dialog = new ProgressDialog(ActivityRORouteAllocation.this);
			@Override
			protected String doInBackground(String... params) {

				HttpURLConnection urlConnection = null;
				BufferedReader reader = null;

				// Will contain the raw JSON response as a string.
				try {
								
					responseJSON="";

					JSONObject jsonDelivery = new JSONObject();

					//to get delivery from database
					dba.open();
					ArrayList<HashMap<String, String>> insmast = dba.getUnSyncDelivery(); 
					dba.close();
					if (insmast != null && insmast.size() > 0) 
					{
						JSONArray array = new JSONArray();
						//To make json string to post delivery
						for (HashMap<String, String> insp : insmast) {
							JSONObject jsonins = new JSONObject();

							jsonins.put("UniqueId", insp.get("UniqueId"));
							jsonins.put("CentreId", insp.get("CentreId"));
							jsonins.put("RouteId", insp.get("RouteId"));
							jsonins.put("VehicleId", insp.get("VehicleId"));
							jsonins.put("CustomerId", insp.get("CustomerId"));
							jsonins.put("UserId", insp.get("CreateBy"));
							jsonins.put("DeliveryDate", insp.get("CreateDate"));
							jsonins.put("ipAddress", common.getDeviceIPAddress(true));
							jsonins.put("Machine", insp.get("Imei"));
							jsonins.put("DeliverTo", insp.get("DeliverTo"));
							array.put(jsonins);
						}
						jsonDelivery.put("Master",array);

						JSONObject jsonDetails = new JSONObject();
						//To get delivery details from database
						dba.open();
						ArrayList<HashMap<String, String>> insdet = dba.getUnSyncDeliveryDetail(); 
						dba.close();
						if (insdet != null && insdet.size() > 0) {

							//To make json string to post delivery details
							JSONArray arraydet = new JSONArray();						
							for (HashMap<String, String> insd : insdet) {
								JSONObject jsondet = new JSONObject();
								jsondet.put("UniqueId", insd.get("UniqueId"));
								jsondet.put("CompanyId", insd.get("CompanyId"));
								jsondet.put("SkuId", insd.get("SkuId"));
								jsondet.put("Rate", insd.get("Rate"));
								jsondet.put("DeliveryQty", insd.get("Qty"));
								arraydet.put(jsondet);
							}
							jsonDetails.put("Detail",arraydet);
						}
						sendJSon = jsonDelivery+"~"+jsonDetails;
						//To invoke json web service to create delivery 
						responseJSON = common.invokeJSONWS(sendJSon,"json","CreateDelivery", common.url );					
					}  
					else
					{
						return "No delivery pending to be send.~";
					}
					return responseJSON;
				} catch (Exception e) {
					// TODO: handle exception
					return "ERROR: "+"Unable to get response from server.";
				} finally {dba.close(); }


			}

			//After execution of json web service to create delivery
			@Override
			protected void onPostExecute(String result) {

				try {
					//To display message after response from server
					if(!result.contains("ERROR"))
					{
						if(responseJSON.equalsIgnoreCase("success"))
						{
							dba.open();
							dba.Update_DeliveryIsSync();
							dba.close();						
						}
						if(common.isConnected())
						{
							//call method of payment json web service
							AsyncStockReturnTransactionWSCall task= new AsyncStockReturnTransactionWSCall();
							task.execute();	
						}
					}
					else
					{
						if(result.contains("null"))
							result="Server not responding.";
						common.showToast("Error: "+result);
					}

				} catch (Exception e) {

				}
				Dialog.dismiss();
			}

			//To display message on screen within process
			@Override
			protected void onPreExecute() {

				Dialog.setMessage("Posting Delivery...");
				Dialog.setCancelable(false);
				Dialog.show();
			}
		}


		private class AsyncStockReturnTransactionWSCall extends AsyncTask<String, Void, String> {
			private ProgressDialog Dialog = new ProgressDialog(ActivityRORouteAllocation.this);
			@Override
			protected String doInBackground(String... params) {
				try {
					// These two need to be declared outside the try/catch
					// so that they can be closed in the finally block.


					JSONObject jsonStockRet = new JSONObject();
					dba.open();
					ArrayList<HashMap<String, String>> srmast = dba.getStockReturn();   
					dba.close();
					if (srmast != null && srmast.size() > 0) {
						JSONArray array = new JSONArray();
						try {
							for (HashMap<String, String> srd : srmast) {
								JSONObject jsonsret = new JSONObject();
								newId = srd.get("Id");
								jsonsret.put("UniqueId", srd.get("UniqueId"));
								jsonsret.put("RouteId", srd.get("RouteId"));
								jsonsret.put("VehicleId", srd.get("VehicleId"));
								jsonsret.put("TransactionDate", srd.get("ReturnDate"));
								jsonsret.put("UserId", srd.get("CreateBy"));
								jsonsret.put("ipAddress",  common.getDeviceIPAddress(true));
								jsonsret.put("Machine", common.getIMEI());
								array.put(jsonsret);
							}
							jsonStockRet.put("SR",array);					


						} catch (JSONException e) {
							// TODO Auto-generated catch block

							return "ERROR: "+e.getMessage();
						}
						finally
						{
							dba.close();
						}


						JSONObject jsonDetails = new JSONObject();
						dba.open();
						ArrayList<HashMap<String, String>> insdet = dba.getStockReturnDetails();
						dba.close();
						if (insdet != null && insdet.size() > 0) {
							JSONArray arraydet = new JSONArray();
							try {
								for (HashMap<String, String> insd : insdet) {
									JSONObject jsondet = new JSONObject();
									//jsondet.put("ReturnId", insd.get("ReturnId"));
									jsondet.put("SKUId", insd.get("SKUId"));
									jsondet.put("ReturnQuantity", insd.get("ReturnQty"));
									jsondet.put("LeakageQuantity", insd.get("LeakageQty"));							
									arraydet.put(jsondet);
								}

								jsonDetails.put("SRD",arraydet);

							}
							catch (JSONException e) {
								// TODO Auto-generated catch block

							}
							finally
							{
								dba.close();
							}
						}

						sendJSon = jsonStockRet+"~"+jsonDetails;

						responseJSON = common.invokeJSONWS(sendJSon,"json","CreateStockReturn", common.url );


					}  
					else
					{
						return "No Stock Return are pending to be Send to server!";
					}



					return "";
				} catch (Exception e) {
					// TODO: handle exception
					return "ERROR: "+e.getMessage();
				} finally {dba.close(); }


			}

			@Override
			protected void onPostExecute(String result) {

				try {

					if(!result.contains("ERROR"))
					{
						if(responseJSON.equalsIgnoreCase("success"))
						{
							dba.open();
							//dba.Update_StockReturnDetails(newId);
							dba.DeleteStockReturn();
							dba.close();						
						}					

						if(common.isConnected())
						{
							//call method of payment json web service
							AsyncPaymentWSCall task = new AsyncPaymentWSCall();
							task.execute();	
						}

					}
					else
					{		
						if(result.contains("null") || result =="")
							result ="Server not responding. Please try again later.";
						common.showAlert(ActivityRORouteAllocation.this, result,false);
					}

				} catch (Exception e) {
					common.showAlert(ActivityRORouteAllocation.this,"Synchronizing failed: " +"Unable to get response from server.", false);
				}
				Dialog.dismiss();
			}

			@Override
			protected void onPreExecute() {
				Dialog.setMessage("Uploading Stock Return..");
				Dialog.setCancelable(false);
				Dialog.show();

			}

			@Override
			protected void onProgressUpdate(Void... values) {

			}

		}

		//To make web service class to post data of payment 
		private class AsyncPaymentWSCall extends AsyncTask<String, Void, String> {
			private ProgressDialog Dialog = new ProgressDialog(
					ActivityRORouteAllocation.this);

			@Override
			protected String doInBackground(String... params) {

				// Will contain the raw JSON response as a string.
				try {

					responseJSON = "";

					JSONObject jsonPayment = new JSONObject();
					dba.open();
					// to get payment from database
					ArrayList<HashMap<String, String>> insmast = dba
							.getUnSyncPayment();
					dba.close();
					if (insmast != null && insmast.size() > 0) {
						JSONArray array = new JSONArray();
						// To make json string to post payment
						for (HashMap<String, String> insp : insmast) {
							JSONObject jsonins = new JSONObject();
							jsonins.put("UniqueId", insp.get("UniqueId"));
							jsonins.put("CustomerId", insp.get("CustomerId"));
							jsonins.put("UserId", insp.get("CreateBy"));
							jsonins.put("CreateDate", insp.get("CreateDate"));
							jsonins.put("DeliveryId", insp.get("DeliveryUniqueId"));
							jsonins.put("ipAddress",
									common.getDeviceIPAddress(true));
							jsonins.put("Machine", common.getIMEI());
							array.put(jsonins);
						}
						jsonPayment.put("Master", array);

						JSONObject jsonDetails = new JSONObject();
						// To get payment details from database
						dba.open();
						ArrayList<HashMap<String, String>> insdet = dba
								.getUnSyncPaymentDetail();
						dba.close();
						if (insdet != null && insdet.size() > 0) {
							// To make json string to post payment details
							JSONArray arraydet = new JSONArray();
							for (HashMap<String, String> insd : insdet) {
								JSONObject jsondet = new JSONObject();
								jsondet.put("UniqueId", insd.get("MasterUniqueId"));
								jsondet.put("CompanyId", insd.get("CompanyId"));
								jsondet.put("BankId", insd.get("BankId"));
								jsondet.put("ChequeNo", insd.get("ChequeNumber"));
								jsondet.put("Amount", insd.get("Amount"));
								jsondet.put("DetailUniqueId", insd.get("UniqueId"));
								jsondet.put("UploadFileName", insd.get("ImageName"));
								jsondet.put("Remarks", insd.get("Remarks"));
								arraydet.put(jsondet);
							}
							jsonDetails.put("Detail", arraydet);
						}

						sendJSon = jsonPayment + "~" + jsonDetails;

						// To invoke json web service to create payment
						responseJSON = common.invokeJSONWS(sendJSon, "json",
								"CreatePayment", common.url);
					} else {
						return "No payment pending to be send.";
					}
					return responseJSON;
				} catch (Exception e) {
					// TODO: handle exception
					return "ERROR: " + "Unable to get response from server.";
				} finally {
					dba.close();
				}
			}

			// After execution of json web service to create payment
			@Override
			protected void onPostExecute(String result) {

				try {
					// To display message after response from server
					if (!result.contains("ERROR")) {
						if (responseJSON.equalsIgnoreCase("success")) {
							dba.open();
							dba.Update_PaymentIsSync();
							dba.close();
						}
						if (common.isConnected()) {
							// call method of get Allocation json web service
							AsyncAllocationWSCall task = new AsyncAllocationWSCall();
							task.execute();
						}
					} else {
						if (result.contains("null"))
							result = "Server not responding.";
						common.showAlert(ActivityRORouteAllocation.this, result, false);
						common.showToast("Error: " + result);
					}
				} catch (Exception e) {
					common.showAlert(ActivityRORouteAllocation.this,
							"Unable to fetch response from server.", false);
				}

				Dialog.dismiss();
			}

			// To display message on screen within process
			@Override
			protected void onPreExecute() {

				Dialog.setMessage("Posting Payment...");
				Dialog.setCancelable(false);
				Dialog.show();
			}
		}
		
	
	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		//Intent homeScreenIntent = new Intent(this, ActivityRORoutesView.class);
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
			//Intent intent = new Intent(this, ActivityRORoutesView.class);
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

	//Method to display change password dialog
		private void showChangePassWindow(final String source, final String resp) {
			LayoutInflater li = LayoutInflater.from(context);
			View promptsView = li.inflate(R.layout.dialog_password_prompt, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			alertDialogBuilder.setView(promptsView);
			//Code to find controls in dialog
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.etPassword);

			final CheckBox ckShowPass = (CheckBox) promptsView
					.findViewById(R.id.ckShowPass);

			final TextView tvMsg = (TextView) promptsView
					.findViewById(R.id.tvMsg);

			tvMsg.setText(resp);

			ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int start,end;

					if(!isChecked){
						start=userInput.getSelectionStart();
						end=userInput.getSelectionEnd();
						userInput.setTransformationMethod(new PasswordTransformationMethod());;
						userInput.setSelection(start,end);
					}else{
						start=userInput.getSelectionStart();
						end=userInput.getSelectionEnd();
						userInput.setTransformationMethod(null);
						userInput.setSelection(start,end);
					}

				}
			});


			// set dialog message
			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Submit",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					String password = userInput.getText().toString().trim();
					if (password.length()> 0) {
						//Code to update password in session and call validate Async Method
						session.updatePassword(password);

						String[] myTaskParams = { source };
						AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
						task.execute(myTaskParams);
					}
					else
					{
						//Display message if password is not enetered
						common.showToast("Password is mandatory");
					}
				}
			})
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();


		}
}
