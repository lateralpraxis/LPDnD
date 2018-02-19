package lateralpraxis.lpdnd;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONArray;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityDeleteDeliveryView  extends Activity {

	/*Start of code to declare Controls*/
	private ListView listDeliveryView,listPaymanetView;
	private TextView tvNoRecord,tvPayNoRecord;
	private TextView tvName,tvDate,tvInvoice,tvVehicle,tvCustomer,tvDelTotalAmount,tvPayTotalAmount;
	private LinearLayout llDelTotal,llPayTotal;
	private Button btnDelivery,btnDelete;
	/*End of code to declare Controls*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	private UserSessionManager session;
	private final Context mContext = this;
	/*End of code to declare class*/

	/*Start of code for Variable Declaration*/
	private String lang,userId,responseJSON,deliveryId,action="";
	private int listDelSize = 0;
	private int listPaySize = 0;
	private Double delTotAmt=0.0;
	private Double payTotAmt=0.0;
	private ArrayList<HashMap<String, String>> wordDelList = null;
	private ArrayList<HashMap<String, String>> wordPayList = null;
	HashMap<String, String> delmap = null;
	HashMap<String, String> paymap = null;
	/*End of code for Variable Declaration*/
	//On create method similar to page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Code to set layout
		setContentView(R.layout.activity_delete_delivery_view);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		/*Start of code to find Controls*/
		tvName= (TextView) findViewById(R.id.tvName);
		tvDate= (TextView) findViewById(R.id.tvDate);
		tvInvoice= (TextView) findViewById(R.id.tvInvoice);
		tvVehicle= (TextView) findViewById(R.id.tvVehicle);
		tvCustomer= (TextView) findViewById(R.id.tvCustomer);
		listDeliveryView = (ListView) findViewById(R.id.listDeliveryView);		
		listPaymanetView = (ListView) findViewById(R.id.listPaymanetView);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		tvPayNoRecord = (TextView) findViewById(R.id.tvPayNoRecord);
		tvDelTotalAmount = (TextView) findViewById(R.id.tvDelTotalAmount);	
		tvPayTotalAmount= (TextView) findViewById(R.id.tvPayTotalAmount);	
		llDelTotal = (LinearLayout) findViewById(R.id.llDelTotal);
		llPayTotal= (LinearLayout) findViewById(R.id.llPayTotal);
		btnDelivery= (Button) findViewById(R.id.btnDelivery);
		btnDelete= (Button) findViewById(R.id.btnDelete);
		btnDelete.setVisibility(View.GONE);
		/*End of code to find Controls*/

		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());
		/*		*/
		final HashMap<String, String> user = session.getLoginUserDetails();
		userId = user.get(UserSessionManager.KEY_ID);

		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);

		/*Code to get data from posted page*/
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			deliveryId = extras.getString("DeliveryId");
		}
		if(common.isConnected())
		{
			AsyncDeliveryDetailWSCall task = new AsyncDeliveryDetailWSCall();
			task.execute();
		}
		else
		{
			Intent intent = new Intent(mContext, ActivityAdminHomeScreen.class);
			startActivity(intent);
			finish();
		}

		//Code on Delete Button Click Event to delete delivery along with payment detail
		btnDelete.setOnClickListener(new View.OnClickListener() {
			// When create button click
			@Override
			public void onClick(View arg0) {
				Builder alertDialogBuilder = new Builder(mContext);
				alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
				alertDialogBuilder
				.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं,आप इस वितरण तथा जमा राशि को हटाना चाहते हैं?":"Are you sure, you want to delete this delivery and payment detail?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int id) {
						if(common.isConnected())
						{
							action="DeleteDeliveryPaymentData";
							AsyncDeleteDeliveryWSCall task = new AsyncDeleteDeliveryWSCall();
							task.execute();
						}
					}	            	
				}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}	            	
				});
				alertDialogBuilder.create().show();
			}
		});

		//Code on Delivery Button Click Event to delete delivery without payment detail
		btnDelivery.setOnClickListener(new View.OnClickListener() {
			// When create button click
			@Override
			public void onClick(View arg0) {
				Builder alertDialogBuilder = new Builder(mContext);
				alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
				alertDialogBuilder
				.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं,आप इस वितरण को हटाना चाहते हैं?":"Are you sure, you want to delete this delivery?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(
							DialogInterface dialog,
							int id) {
						if(common.isConnected())
						{
							action="DeleteDeliveryData";
							AsyncDeleteDeliveryWSCall task = new AsyncDeleteDeliveryWSCall();
							task.execute();
						}
					}	            	
				}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}	            	
				});
				alertDialogBuilder.create().show();
			}
		});
	}

	// Web Service to Fetch Delivery Data For Delete
	private class AsyncDeliveryDetailWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityDeleteDeliveryView.this);

		@Override
		protected String doInBackground(String... params) {
			try {

				String[] name = {"id" };
				String[] value = { deliveryId };
				// Call method of web service to Read Delivery Data For Delete
				responseJSON = "";
				responseJSON = common.CallJsonWS(name, value,"GetDeliveryDeleteDatails", common.url);
				return "";
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: " + e.getMessage();
			}

		}

		// After execution of product web service
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					String data="";
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON.split("~")[0]);
					JSONArray jsonDelArray = new JSONArray(responseJSON.split("~")[1]);
					JSONArray jsonPayArray = new JSONArray(responseJSON.split("~")[2]);
					wordDelList = new ArrayList<HashMap<String, String>>();
					wordPayList = new ArrayList<HashMap<String, String>>();
					if (jsonArray.length() > 0) {
						for (int i = 0; i < jsonArray.length(); ++i) {
							tvName.setText(jsonArray.getJSONObject(i)
									.getString("FullName"));
							tvDate.setText(common.convertToDisplayDateFormat(jsonArray.getJSONObject(i)
									.getString("DeliveryDate")));
							tvInvoice.setText(jsonArray.getJSONObject(i)
									.getString("InvoiceNo"));
							tvVehicle.setText(jsonArray.getJSONObject(i)
									.getString("VehicleNo"));
							tvCustomer.setText(jsonArray.getJSONObject(i)
									.getString("CustomerName"));
						}
						if (jsonDelArray.length() > 0) {
							for (int i = 0; i < jsonDelArray.length(); ++i) {
								delmap = new HashMap<String, String>();
								delmap.put("SKUName", jsonDelArray.getJSONObject(i)
										.getString("SKUName"));
								delmap.put("SKU", jsonDelArray.getJSONObject(i)
										.getString("SKU"));
								delmap.put("Quantity", jsonDelArray.getJSONObject(i)
										.getString("Quantity"));
								delmap.put("Amount", jsonDelArray.getJSONObject(i)
										.getString("Amount"));
								delTotAmt =delTotAmt+ Double.valueOf(jsonDelArray.getJSONObject(i)
										.getString("Amount"));
								wordDelList.add(delmap);
							}
							listDelSize = wordDelList.size();
							if (listDelSize != 0) {
								listDeliveryView.setAdapter(new DeliveryListAdapter(mContext, wordDelList));

								ViewGroup.LayoutParams params = listDeliveryView.getLayoutParams();
								listDeliveryView.setLayoutParams(params);
								listDeliveryView.requestLayout();
								tvNoRecord.setVisibility(View.GONE);
								llDelTotal.setVisibility(View.VISIBLE);
								tvDelTotalAmount.setText(Html.fromHtml("<b>Total "+common.convertToTwoDecimal(String.valueOf(delTotAmt))+"</b>"));
							} else {
								listDeliveryView.setAdapter(null);
								tvNoRecord.setVisibility(View.VISIBLE);
								llDelTotal.setVisibility(View.GONE);
							}

						}

						if (jsonPayArray.length() > 0) {
							for (int i = 0; i < jsonPayArray.length(); ++i) {
								paymap = new HashMap<String, String>();
								paymap.put("PaymentMode", jsonPayArray.getJSONObject(i)
										.getString("PaymentMode"));
								paymap.put("Title", jsonPayArray.getJSONObject(i)
										.getString("Title"));
								paymap.put("ChequeNo", jsonPayArray.getJSONObject(i)
										.getString("ChequeNo"));
								paymap.put("Amount", jsonPayArray.getJSONObject(i)
										.getString("Amount"));
								payTotAmt=payTotAmt+ Double.valueOf(jsonPayArray.getJSONObject(i)
										.getString("Amount"));
								wordPayList.add(paymap);
							}
							listPaySize = wordPayList.size();
							if (listPaySize != 0) {
								listPaymanetView.setAdapter(new PayListAdapter(mContext, wordPayList));

								ViewGroup.LayoutParams params = listPaymanetView.getLayoutParams();
								//params.height = 500;
								listPaymanetView.setLayoutParams(params);
								listPaymanetView.requestLayout();
								tvPayNoRecord.setVisibility(View.GONE);
								llPayTotal.setVisibility(View.VISIBLE);
								btnDelete.setVisibility(View.VISIBLE);
								listPaymanetView.setVisibility(View.VISIBLE);
								tvPayTotalAmount.setText(Html.fromHtml("<b>Total "+common.convertToTwoDecimal(String.valueOf(payTotAmt))+"</b>"));
							} else {
								listPaymanetView.setAdapter(null);
								tvPayNoRecord.setVisibility(View.VISIBLE);
								llPayTotal.setVisibility(View.GONE);
								listPaymanetView.setVisibility(View.GONE);
								btnDelete.setVisibility(View.GONE);
							}

						}


					} else {
						common.showToast("There is no data available for deleting delivery!");
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showToast(result);
					Intent intent = new Intent(mContext, ActivityDeleteDeliveryList.class);
					startActivity(intent);
					finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showToast("Delivery Details Downloading failed: " + e.toString());
				Intent intent = new Intent(mContext, ActivityDeleteDeliveryList.class);
				startActivity(intent);
				finish();
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Delivery Details..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//Async Task To Call Delete Delivery with Or Without Payment Details
	private class AsyncDeleteDeliveryWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityDeleteDeliveryView.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = {"id","action", "userId", "ipAddress", "machine" };
				String[] value = { deliveryId,action, userId,common.getDeviceIPAddress(true),common.getIMEI() };
				responseJSON = "";
				// Call method of web service to delete Delivery Detail from server
				responseJSON = common.CallJsonWS(name, value, "DeleteDeliveryDetails",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to Delete Delivery Details
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					common.showToast("Delivery details deleted successfully");					
					Intent intent = new Intent(ActivityDeleteDeliveryView.this, ActivityDeleteDeliveryList.class);
					startActivity(intent);
					finish();
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityDeleteDeliveryView.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityDeleteDeliveryView.this,e.getMessage(), false);
			}
			Dialog.dismiss();
		}

		// To display Delivery Deletion Message
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Deleting Delivery...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	//<editor-fold desc="Code Binding Data In Delivery List">
	public static class viewHolder {
		TextView tvSKU, tvQty, tvAmount;
		int ref;
	}

	private class DeliveryListAdapter extends BaseAdapter {
		LayoutInflater inflater;
		ArrayList<HashMap<String, String>> _listData;
		String _type;
		private Context context2;

		public DeliveryListAdapter(Context context,
				ArrayList<HashMap<String, String>> listData) {
			this.context2 = context;
			inflater = LayoutInflater.from(context2);
			_listData = listData;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return _listData.size();
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

		public View getView(final int position, View convertView, ViewGroup parent) {
			final viewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.activity_delete_delivery_item, null);
				holder = new viewHolder();
				convertView.setTag(holder);

			} else {
				holder = (viewHolder) convertView.getTag();
			}
			holder.ref = position;
			holder.tvSKU = (TextView) convertView
					.findViewById(R.id.tvSKU);
			holder.tvQty = (TextView) convertView
					.findViewById(R.id.tvQty);
			holder.tvAmount = (TextView) convertView
					.findViewById(R.id.tvAmount);

			final HashMap<String,String> itemData = _listData.get(position);
			holder.tvSKU.setText(itemData.get("SKUName"));
			if(itemData.get("SKU").equalsIgnoreCase("0.0") || itemData.get("SKU").equalsIgnoreCase("0"))
				holder.tvQty.setText(String.format("%.1f",Double.parseDouble(itemData.get("Quantity"))).replace(".0", ""));
			else
				holder.tvQty.setText(common.convertToTwoDecimal(itemData.get("Quantity")));
			holder.tvAmount.setText(common.convertToTwoDecimal(itemData.get("Amount")));

			convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
			return convertView;
		}

	}
	//</editor-fold>

	//<editor-fold desc="Code Binding Data In Payment List">
	public static class viewPayHolder {
		TextView tvModeHead, tvAmount;
		int ref;
	}

	private class PayListAdapter extends BaseAdapter {
		LayoutInflater inflater;
		ArrayList<HashMap<String, String>> _listData;
		String _type;
		private Context context2;

		public PayListAdapter(Context context,
				ArrayList<HashMap<String, String>> listData) {
			this.context2 = context;
			inflater = LayoutInflater.from(context2);
			_listData = listData;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return _listData.size();
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

		public View getView(final int position, View convertView, ViewGroup parent) {
			final viewPayHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.cash_deposit_delete_item, null);
				holder = new viewPayHolder();
				convertView.setTag(holder);

			} else {
				holder = (viewPayHolder) convertView.getTag();
			}
			holder.ref = position;
			holder.tvModeHead = (TextView) convertView
					.findViewById(R.id.tvModeHead);
			holder.tvAmount = (TextView) convertView
					.findViewById(R.id.tvAmount);

			final HashMap<String,String> itemData = _listData.get(position);
			if(itemData.get("PaymentMode").equalsIgnoreCase("Cash"))
				holder.tvModeHead.setText(itemData.get("PaymentMode"));
			else if(itemData.get("PaymentMode").equalsIgnoreCase("Cheque"))
				holder.tvModeHead.setText(itemData.get("Title")+" - "+itemData.get("ChequeNo"));
			else
				holder.tvModeHead.setText(itemData.get("PaymentMode")+" - "+itemData.get("ChequeNo"));
			holder.tvAmount.setText(common.convertToTwoDecimal(itemData.get("Amount")));

			convertView.setBackgroundColor(Color.parseColor((position % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
			return convertView;
		}

	}
	//</editor-fold>

	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		case android.R.id.home:

			Intent i = new Intent(ActivityDeleteDeliveryView.this,ActivityDeleteDeliveryList.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(i);
			finish();		
			return true;

		case R.id.action_go_to_home: 
			Intent homeScreenIntent = new Intent(this, ActivityAdminHomeScreen.class);
			homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeScreenIntent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}	
	}

	// To create menu on inflater
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		return true;
	}

	// When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent homeScreenIntent = new Intent(ActivityDeleteDeliveryView.this, ActivityDeleteDeliveryList.class);
		homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeScreenIntent);
		finish();					
	}



}
