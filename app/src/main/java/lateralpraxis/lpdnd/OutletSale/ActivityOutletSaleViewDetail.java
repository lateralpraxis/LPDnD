package lateralpraxis.lpdnd.OutletSale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
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
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityDeliveryViewSummary;
import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.ViewImage;
import lateralpraxis.lpdnd.types.CustomerPayment;

@SuppressLint("InflateParams") public class ActivityOutletSaleViewDetail extends Activity{
	private DatabaseAdapter dba;
	Common common;
	CustomAdapter Cadapter;
	private String id="0", header;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord, tvTotalAmount, tvHeader;
	private MainAdapter ListAdapter;
	UserSessionManager session;
	final Context context = this;
	private int cnt=0;
	double totalQty=0, totalAmounts=0;
	private int lsize=0;
	Button buttonLeft, buttonRight;
	/*Start of code to declare variables*/
	private ArrayList<HashMap<String, String>> PaymentDetails;
	private TextView tvEmpty,tvTotalAmt, tvHeaderPayment;
	private TableLayout tableDataHeader,tableLayoutTotal;
	private ListView lvPaymentDetails;
	private SimpleDateFormat dateFormat_database;
	private String lang;
	/*End of code to declare variables*/
	/*Start of variable declaration for displaying image*/
	private File[] listFile;
	File file;
	long different=0;
	private String[] FilePathStrings;
	private String[] FileNameStrings;
	/*End of variable declaration for displaying image*/
	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_outlet_sale_view_detail);

		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		lang= session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		common = new Common(this);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of date format
		dateFormat_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		//To create instance of database and control
		dba=new DatabaseAdapter(this);
		HeaderDetails = new ArrayList<HashMap<String, String>>();
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
		tvHeader =  (TextView) findViewById(R.id.tvHeader);
		tvHeaderPayment  =  (TextView) findViewById(R.id.tvHeaderPayment);

		//Hash Map for storing data
		PaymentDetails = new ArrayList<HashMap<String, String>>();
		//Code to find layouts
		tableDataHeader=(TableLayout) findViewById(R.id.tableDataHeader);
		tableLayoutTotal=(TableLayout) findViewById(R.id.tableLayoutTotal);
		lvPaymentDetails =(ListView)findViewById(R.id.lvPaymentDetails);
		tvEmpty= (TextView) findViewById(R.id.tvEmpty);
		tvTotalAmt= (TextView) findViewById(R.id.tvTotalAmt);
		buttonLeft = (Button)findViewById(R.id.ButtonLeft);
		buttonRight = (Button)findViewById(R.id.ButtonRight);

		//Call method to enabled icon on top of 'go to home' page



		//To extract id from bundle to show details
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			id = extras.getString("Id");
			//strDate=extras.getString("Date");
			header = extras.getString("Header");			
		}

		//To hide right arrow
		try {
			dba.open();
			Date date1 = dateFormat_database.parse(dba.getDemandDate());
			Date date2 = dateFormat_database.parse(dba.getDate());
			dba.close();
			Calendar c = Calendar.getInstance(); 
			c.setTime(date2); 
			c.add(Calendar.DATE, -1);

			different = c.getTimeInMillis() - date1.getTime();
			Log.i("different", String.valueOf(different));

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(different < 0)
			buttonRight.setVisibility(View.GONE);
		else
			buttonRight.setVisibility(View.VISIBLE);

		DataBind();
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

				Intent myIntent = new Intent(ActivityOutletSaleViewDetail.this, ActivityOutletSaleViewDetail.class);
				myIntent.putExtra("Id", id); 
				myIntent.putExtra("Header",header);
				startActivity(myIntent);
				finish();
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
				Intent myIntent = new Intent(ActivityOutletSaleViewDetail.this, ActivityOutletSaleViewDetail.class);
				myIntent.putExtra("Id", id); 
				myIntent.putExtra("Header",header);
				startActivity(myIntent);
				finish();
			}
		});

	}

	private void DataBind()
	{
		String deliveryDate = "";
		dba.open();
		deliveryDate = dba.getDemandDate();
		tvHeader.setText(Html.fromHtml("<font color=#000000> "+common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", deliveryDate)+ " "+header+"</font>"));
		dba.close();

		//To get delivery details from database
		dba.open();	
		ArrayList<HashMap<String, String>> lables =null;
		lables = dba.getDeliveryDetails(id, deliveryDate);	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Item", String.valueOf(lable.get("Item"))); 
				hm.put("DelQty", String.valueOf(lable.get("DelQty")));
				hm.put("Rate", String.valueOf(lable.get("Rate")));
				hm.put("Amount", String.valueOf(lable.get("Amount")));
				hm.put("DQty", String.valueOf(lable.get("DQty")));
				totalAmounts =totalAmounts+ Double.valueOf(lable.get("DelQty"))*Double.valueOf(lable.get("Rate").replace(",", ""));
				HeaderDetails.add(hm); 
			}
		}
		dba.close();
		if(lsize==0)
		{
			//To display no record message 
			tvNoRecord.setVisibility(View.VISIBLE);
			listViewMain.setVisibility(View.GONE);
		}
		else
		{
			//To bind data and display list view
			tvNoRecord.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			ListAdapter = new MainAdapter(ActivityOutletSaleViewDetail.this);
			listViewMain.setAdapter(ListAdapter);

			if(lang.equalsIgnoreCase("hi"))
				tvTotalAmount.setText(Html.fromHtml("<b>कुल: "+common.stringToTwoDecimal(String.valueOf(totalAmounts))+"</b>"));
			else
				tvTotalAmount.setText(Html.fromHtml("<b>Total: "+common.stringToTwoDecimal(String.valueOf(totalAmounts))+"</b>"));
		}	
		/*Start of code to bind data from temporary table*/
		PaymentDetails.clear();
		dba.open();		
		List <CustomerPayment> lablePayment = null;
		lablePayment = dba.getCustomerPaymentForDelivery(id, deliveryDate);
		lsize = lablePayment.size();
		if(lsize>0)
		{
			tvHeaderPayment.setVisibility(View.VISIBLE);
			tvEmpty.setVisibility(View.GONE);
			tableDataHeader.setVisibility(View.VISIBLE);
			tableLayoutTotal.setVisibility(View.VISIBLE);
			//Looping through hash map and add data to hash map
			for(int i=0;i<lablePayment.size();i++){
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Id", String.valueOf(lablePayment.get(i).getId())); 
				hm.put("CompanyName", String.valueOf(lablePayment.get(i).getCompanyName())); 
				hm.put("Amount", common.stringToTwoDecimal(lablePayment.get(i).getAmount()));  
				hm.put("ChequeNumber", String.valueOf(lablePayment.get(i).getChequeNumber()));
				hm.put("Bank", String.valueOf(lablePayment.get(i).getBankName()));
				hm.put("ImagePath", String.valueOf(lablePayment.get(i).getUniqueId()));
				PaymentDetails.add(hm);
			}
			dba.open();
			String strTotal =String.valueOf(common.stringToTwoDecimal(dba.getCustomerTotalPaymentForDelivery(id, deliveryDate)));
			tvTotalAmt.setText(strTotal);
		}
		else
		{
			//Display no records message
			tvHeaderPayment.setVisibility(View.GONE);
			tvEmpty.setVisibility(View.GONE);
			tableDataHeader.setVisibility(View.GONE);
			tableLayoutTotal.setVisibility(View.GONE);
		}
		dba.close();
		//Code to set hash map data in custom adapter
		Cadapter = new CustomAdapter(ActivityOutletSaleViewDetail.this,PaymentDetails);
		if(lsize>0)
			lvPaymentDetails.setAdapter(Cadapter);	
		/*End of code to bind data from temporary table*/
	}

	//To make class of delivery view holder
	public class MainAdapter extends BaseAdapter {
		class ViewHolder {
			TextView tvItem, tvDelQty, tvRate, tvAmount, tvDQty; 
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
				arg1 = mInflater.inflate(R.layout.activity_delivery_view_detail_item, null);
				holder = new ViewHolder();

				holder.tvItem = (TextView)arg1.findViewById(R.id.tvItem);
				holder.tvDelQty = (TextView)arg1.findViewById(R.id.tvDelQty);
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
			if(Double.parseDouble(HeaderDetails.get(arg0).get("DQty")) == 0)
				holder.tvDQty.setText("-");
			else
				holder.tvDQty.setText(HeaderDetails.get(arg0).get("DQty").replace(".0", ""));
			holder.tvDelQty.setText(HeaderDetails.get(arg0).get("DelQty").replace(".0", ""));
			holder.tvRate.setText(common.stringToTwoDecimal(HeaderDetails.get(arg0).get("Rate")));
			holder.tvAmount.setText(common.stringToTwoDecimal(HeaderDetails.get(arg0).get("Amount")));

			//Code to check if row is even or odd and set set color for alternate rows
			/*if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));  
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/
			return arg1;
		}

	}

	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent  intent;
		intent = new Intent(this, ActivityDeliveryViewSummary.class);
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
			intent = new Intent(this, ActivityDeliveryViewSummary.class);
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



	//Class for Binding Data in ListView
	public static class ViewHolder {
		//Control Declaration
		TextView tvCompany,tvAmount,tvCheque,tvDocumentPath; 
		Button btnAttach;
	}

	//Declaring Adapter for binding data in List View
	public class CustomAdapter extends BaseAdapter {

		private Context paymentContext;
		private LayoutInflater mInflater;
		//Method to return count on data in adapter
		@Override
		public int getCount() {
			return PaymentDetails.size();
		}

		@Override
		public Object getItem(int arg0) {
			return PaymentDetails.get(arg0);
		}

		@Override
		public long getItemId(int arg0) { 
			return arg0;
		}

		@Override
		public int getViewTypeCount() {

			return getCount();
		}

		@Override
		public int getItemViewType(int position) {

			return position;
		}
		//Adapter constructor
		public CustomAdapter(Context context,ArrayList<HashMap<String, String>> lvPaymentDetails) {
			this.paymentContext = context;
			mInflater = LayoutInflater.from(paymentContext);
			PaymentDetails = lvPaymentDetails;
		}

		//Event is similar to row data bound event
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {			


			final ViewHolder holder;
			if (arg1 == null) 
			{
				//Code to set layout inside list view
				arg1 = mInflater.inflate(R.layout.list_payment_detail, null); 
				holder = new ViewHolder();
				//Code to find controls inside listview
				holder.tvCompany = (TextView)arg1.findViewById(R.id.tvCompany);
				holder.tvAmount = (TextView)arg1.findViewById(R.id.tvAmount);
				holder.tvCheque = (TextView)arg1.findViewById(R.id.tvCheque);
				holder.tvDocumentPath= (TextView)arg1.findViewById(R.id.tvDocumentPath);
				holder.btnAttach = (Button)arg1.findViewById(R.id.btnAttach);
				arg1.setTag(holder);

			}
			else
			{

				holder = (ViewHolder) arg1.getTag();
			}
			//Code to bind data from hash map in controls
			holder.tvCompany.setText(PaymentDetails.get(arg0).get("CompanyName"));
			holder.tvAmount.setText(PaymentDetails.get(arg0).get("Amount"));
			holder.tvCheque.setText(Html.fromHtml("<font color=#000000> "+PaymentDetails.get(arg0).get("ChequeNumber")+"</font>") );
			holder.tvDocumentPath.setText(PaymentDetails.get(arg0).get("ImagePath"));
			if(TextUtils.isEmpty(PaymentDetails.get(arg0).get("ImagePath").trim()) )
				holder.btnAttach.setVisibility(View.GONE);
			else
				holder.btnAttach.setVisibility(View.VISIBLE);
			//Button delete event for deleting attachment
			holder.btnAttach.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {

						String actPath = holder.tvDocumentPath.getText().toString();
						int pathLen = actPath.split("/").length;
						//to Get Unique Id
						String newPath1 = actPath.split("/")[pathLen-2];
						String newPath2 = actPath.split("/")[pathLen-3];

						String catType = "Cheque";
						// Check for SD Card
						if (!Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							common.showToast("Error! No SDCARD Found!");
						} else {
							// Locate the image folder in your SD Card
							file = new File(Environment.getExternalStorageDirectory()
									+  File.separator+newPath2+File.separator+newPath1+File.separator);
						}

						if (file.isDirectory()) {

							listFile = file.listFiles(new FilenameFilter() {
								public boolean accept(File directory, String fileName) {
									return fileName.endsWith(".jpeg")|| fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
								}
							});
							// Create a String array for FilePathStrings
							FilePathStrings = new String[listFile.length];
							// Create a String array for FileNameStrings
							FileNameStrings = new String[listFile.length];

							for (int i = 0; i < listFile.length; i++) {
								FilePathStrings[i] = listFile[i].getAbsolutePath();
								// Get the name image file
								FileNameStrings[i] = listFile[i].getName();

								Intent i1 = new Intent(ActivityOutletSaleViewDetail.this, ViewImage.class);
								// Pass String arrays FilePathStrings
								i1.putExtra("filepath", FilePathStrings);
								// Pass String arrays FileNameStrings
								i1.putExtra("filename", FileNameStrings);
								// Pass String category type
								i1.putExtra("categorytype", catType);
								// Pass click position
								i1.putExtra("position", 0);
								startActivity(i1);
							}
						}


					} catch (Exception except) {
						//except.printStackTrace();
						common.showAlert(ActivityOutletSaleViewDetail.this,"Error: "+except.getMessage(),false);

					}

				}
			});

			//Code to check if row is even or odd and set set color for alternate rows
			/*if (arg0 % 2 == 1) {
				arg1.setBackgroundColor(Color.parseColor("#D3D3D3"));  
			} else {
				arg1.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}*/

			return arg1;
		}

	}
}
