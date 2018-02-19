package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ActivityStockReturn extends ListActivity {
	private Button  btnCreate;
	private Common common;
	UserSessionManager session;
	private DatabaseAdapter dba;
	private String newReturnId ="";
	private String[] arrTemp;	
	private TextView tvNoRecord;
	private String lang, routeId, routeName;
	private String vehicleId;
	private String vehicleIdWithName;
	private Double returnQ = 0.0;
	private Double leakageQ = 0.0;
	final Context context = this;
	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_return);
		common = new Common(getApplicationContext());
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);



		//To read user role from user session manager
		final HashMap<String, String> user = session.getLoginUserDetails();
		//to create object of controls 	
		btnCreate= (Button) findViewById(R.id.btnCreate);
		tvNoRecord = (TextView)findViewById(R.id.tvNoRecord);	
		//To create instance of database 
		dba=new DatabaseAdapter(this);
		BindRouteStock(user.get(UserSessionManager.KEY_ROUTEID));
		//Event hander to create demand button
		btnCreate.setOnClickListener(new View.OnClickListener() {
			//When create button click
			@Override
			public void onClick(View arg0) {
				//To validate required field and please enter at least one quantity!
				int zeroCount=0, totalRow=0;
				int invalidCount=0,excesscount=0;
				for(int i =0;i<getListView().getChildCount();i++)
				{
					Double availQty,retQty,leakQty;
					totalRow++;
					View v = getListView().getChildAt(i);
					EditText etReturnQty = (EditText)v.findViewById(R.id.etReturnQty);
					EditText etLeakageQty = (EditText)v.findViewById(R.id.etLeakageQty);
					TextView tvAvailQty= (TextView)v.findViewById(R.id.tvAvailQty);
					if(!etReturnQty.getText().toString().equalsIgnoreCase(".") && !etLeakageQty.getText().toString().equalsIgnoreCase(".") )
					{
						if(etReturnQty.length() == 0 && etLeakageQty.length()==0)
							zeroCount++;
					}
					if(etReturnQty.getText().toString().equalsIgnoreCase("."))
						invalidCount=invalidCount+1;
					if(etReturnQty.getText().toString().equalsIgnoreCase(".") || etReturnQty.getText().toString().trim().equalsIgnoreCase("") )
						retQty=0.0;
					else
						retQty=Double.valueOf(etReturnQty.getText().toString());
					if(etLeakageQty.getText().toString().equalsIgnoreCase(".") || etLeakageQty.getText().toString().trim().equalsIgnoreCase("") )
						leakQty=0.0;
					else
						leakQty=Double.valueOf(etLeakageQty.getText().toString());
					availQty=Double.valueOf(tvAvailQty.getText().toString());
					if(retQty+leakQty>availQty)
						excesscount=excesscount+1;
				}
				if(invalidCount>0 && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityStockReturn.this, "Please enter valid quantity!", false);
				else if(invalidCount>0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityStockReturn.this, "कृपया वैध मात्रा दर्ज करें!", false);

				else if(excesscount>0 && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityStockReturn.this, "Sum of leakage and return quantity cannot exceed available quantity!", false);
				else if(excesscount>0 && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityStockReturn.this, "रिसाव और रिटर्न की मात्रा उपलब्ध मात्रा से अधिक नहीं हो सकती!", false);


				else if(totalRow == zeroCount && lang.equalsIgnoreCase("en"))
					common.showAlert(ActivityStockReturn.this, "Please enter atleast one quantity!", false);
				else if(totalRow == zeroCount && lang.equalsIgnoreCase("hi"))
					common.showAlert(ActivityStockReturn.this, "कृपया कम से कम एक मात्रा दर्ज करें!", false);
				else
				{

					//Confirmation message before submit data
					Builder alertDialogBuilder = new Builder(context);
					alertDialogBuilder.setTitle(lang.equalsIgnoreCase("hi")?"पुष्टीकरण":"Confirmation");
					alertDialogBuilder.setMessage(lang.equalsIgnoreCase("hi")?"क्या आप निश्चित हैं, आप विवरण सुरक्षित करना चाहते हैं?":"Are you sure, you want to submit?").setCancelable(false).setPositiveButton(lang.equalsIgnoreCase("hi")?"हाँ":"Yes", 
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								//To store data in demand table
								dba.open();
								String insertReturn = dba.Insert_StockReturn(dba.uniqueId(common.getIMEI()), user.get(UserSessionManager.KEY_ROUTEID), user.get(UserSessionManager.KEY_VEHICLEID), common.getDateTime(), user.get(UserSessionManager.KEY_ID));
								newReturnId = insertReturn;
								if(insertReturn.contains("success"))
								{
									for(int i =0;i<getListView().getChildCount();i++)
									{
										View v = getListView().getChildAt(i);
										TextView tvSKUId = (TextView)v.findViewById(R.id.tvSKUId);
										TextView tvSKUName = (TextView)v.findViewById(R.id.tvSKUName);
										EditText etReturnQty = (EditText)v.findViewById(R.id.etReturnQty);
										EditText etLeakageQty = (EditText)v.findViewById(R.id.etLeakageQty);
										//To validate if user enter only . 
										if(!etReturnQty.getText().toString().equalsIgnoreCase(".") && !etLeakageQty.getText().toString().equalsIgnoreCase("."))
										{
											String retQty = etReturnQty.getText().toString().trim().length() == 0? "0" :etReturnQty.getText().toString().trim();	
											String lkgQty = etLeakageQty.getText().toString().trim().length() == 0? "0" :etLeakageQty.getText().toString().trim();
											if(Double.parseDouble(retQty)!=0 || Double.parseDouble(lkgQty)!=0 ){
												dba.Insert_StockReturnDetails(newReturnId.split("~")[1], tvSKUId.getText().toString(), retQty, lkgQty, tvSKUName.getText().toString().trim() );
												dba.UpdateRouteStock(user.get(UserSessionManager.KEY_ROUTEID), tvSKUId.getText().toString(), String.valueOf((Double.parseDouble(retQty) + Double.parseDouble(lkgQty))));
											}
											Intent intent = new Intent(context, ActivityStockReturnViewDetail.class);
											intent.putExtra("ReturnDate", common.formateDateFromstring("dd-MMM-yyyy", "yyyy-MM-dd",common.getCurrentDate()));
											intent.putExtra("RouteId", user.get(UserSessionManager.KEY_ROUTEID));
											intent.putExtra("Header", common.getCurrentDate()+" "+user.get(UserSessionManager.KEY_ROUTE).toString());
											startActivity(intent);		
											finish();
										}						
									}
								}
								dba.close();
							} catch (Exception e) {
								e.printStackTrace();
								common.showAlert(ActivityStockReturn.this, "Error in Saving Stock Return.\n"+e.getMessage(), false);
							}
						}	            	
					}).setNegativeButton(lang.equalsIgnoreCase("hi")?"नहीं":"No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}	            	
					});
					alertDialogBuilder.create().show();					
				}
			}
		});
	}

	//To bind SKU from database
	private void BindRouteStock(String routeId)
	{	
		dba.openR();	
		ArrayList<HashMap<String, String>> listItem =  dba.getRouteStock(routeId);
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

	//To make view holder to display on screen
	public class CustomAdapter extends BaseAdapter {
		private Context context2;
		class ViewHolder {
			TextView tvSKUId, tvSKUName, tvAvailQty, tvSKU; 
			EditText etReturnQty, etLeakageQty;
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
		@SuppressLint("InflateParams")
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {			
			final ViewHolder holder;
			if (arg1 == null) 
			{
				arg1 = mInflater.inflate(R.layout.activity_stock_return_item, null);
				holder = new ViewHolder();
				holder.tvSKUId = (TextView)arg1.findViewById(R.id.tvSKUId);
				holder.tvSKUName = (TextView)arg1.findViewById(R.id.tvSKUName);
				holder.tvSKU= (TextView)arg1.findViewById(R.id.tvSKU);
				holder.tvAvailQty= (TextView)arg1.findViewById(R.id.tvAvailQty);
				holder.etReturnQty=(EditText)arg1.findViewById(R.id.etReturnQty);
				holder.etLeakageQty=(EditText)arg1.findViewById(R.id.etLeakageQty);
				arg1.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) arg1.getTag();
			}
			holder.ref = arg0;

			//To bind data from list
			holder.tvSKUId.setText(_listItems.get(arg0).get("SKUId"));
			holder.tvSKUName.setText(lang.equalsIgnoreCase("hi")?_listItems.get(arg0).get("SKUNameLocal"): _listItems.get(arg0).get("SKUName"));
			holder.tvSKU.setText(_listItems.get(arg0).get("SKU"));
			
			holder.tvAvailQty.setText(String.format("%.1f",Double.parseDouble(_listItems.get(arg0).get("AvailQty"))).replace(".0", ""));
			if(arrTemp[arg0] != null)
			{
				holder.etReturnQty.setText(String.format("%.1f", Double.parseDouble(arrTemp[arg0])));
				holder.etLeakageQty.setText(String.format("%.1f", Double.parseDouble(arrTemp[arg0])));
			}
			if(_listItems.get(arg0).get("SKU").equalsIgnoreCase(""))
			{
				//To display decimal point in number key board control
				holder.etReturnQty.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,1)});
				holder.etReturnQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);

				holder.etLeakageQty.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,1)});
				holder.etLeakageQty.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
			}
			else
			{
				//To display only number in key board control
				int maxLength = 3;
				InputFilter[] FilterArray = new InputFilter[1];
				FilterArray[0] = new InputFilter.LengthFilter(maxLength);
				holder.etReturnQty.setFilters(FilterArray);
				holder.etReturnQty.setInputType(InputType.TYPE_CLASS_NUMBER);
				holder.etLeakageQty.setFilters(FilterArray);
				holder.etLeakageQty.setInputType(InputType.TYPE_CLASS_NUMBER);
			}
			//On change of Return Quantity
			holder.etReturnQty
			.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean gainFocus) {
					// onFocus
					if (gainFocus) {

					}
					// onBlur
					else {
						if(holder.etReturnQty.getText().toString().trim().equalsIgnoreCase("."))
							holder.etReturnQty.setText("0");
						if(holder.etReturnQty.getText().toString()
								.trim().length() > 0)
						{
							if((Double.parseDouble(holder.etReturnQty.getText().toString()
									.trim()))==0)
								holder.etReturnQty.setText("");
						}
						if(holder.etLeakageQty.getText().toString().trim().equalsIgnoreCase("."))
							leakageQ =0.0;
						else if(holder.etLeakageQty.getText().toString().trim().length() > 0)
							leakageQ = 	Double.valueOf(holder.etLeakageQty.getText().toString().trim());
						else
							leakageQ = Double.valueOf("0");
						if (holder.etReturnQty.getText().toString()
								.trim().length() > 0) {
							if (Double.valueOf(holder.etReturnQty
									.getText().toString().trim()) + leakageQ > Double
									.valueOf(holder.tvAvailQty
											.getText().toString()
											.trim())) {
								common.showToast(lang.equalsIgnoreCase("hi")?"रिसाव और वापसी राशि का योग उपलब्ध मात्रा से अधिक नहीं होना चाहिए।":"Sum of Leakage and Return Quantity should not be exceeded from available quantity.");
								holder.etReturnQty.setText("");
							}
						}
					}
				}
			});
			//On change of Leakage Quantity
			holder.etLeakageQty
			.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean gainFocus) {
					// onFocus
					if (gainFocus) {

					}
					// onBlur
					else {
						if(holder.etLeakageQty.getText().toString()
								.trim().length() > 0)
						{
							if((Double.parseDouble(holder.etLeakageQty.getText().toString()
									.trim()))==0)
								holder.etLeakageQty.setText("");
						}
						if(holder.etReturnQty.getText().toString().trim().length() > 0)
							returnQ = 	Double.valueOf(holder.etReturnQty.getText().toString().trim());
						else
							returnQ = Double.valueOf("0");

						if (holder.etLeakageQty.getText().toString()
								.trim().length() > 0) {
							if (Double.valueOf(holder.etLeakageQty
									.getText().toString().trim()) + returnQ > Double
									.valueOf(holder.tvAvailQty
											.getText().toString()
											.trim())											) {
								common.showToast(lang.equalsIgnoreCase("hi")?"रिसाव और वापसी राशि का योग उपलब्ध मात्रा से अधिक नहीं होना चाहिए।":"Sum of Leakage and Return Quantity should not be exceeded from available quantity.");
								holder.etLeakageQty.setText("");
							}
						}
					}
				}
			});
			return arg1;
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
}
