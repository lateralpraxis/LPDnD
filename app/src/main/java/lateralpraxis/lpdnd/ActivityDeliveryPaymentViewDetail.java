package lateralpraxis.lpdnd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
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

@SuppressLint("InflateParams") public class ActivityDeliveryPaymentViewDetail  extends Activity{
	private DatabaseAdapter dba;
	Common common;
	private String id="0", header;
	private ArrayList<HashMap<String, String>> HeaderDetails;
	private ListView listViewMain;
	private TextView tvNoRecord, tvTotalAmount, tvHeader, tvTotalPaymentAmount;
	private MainAdapter ListAdapter;
	UserSessionManager session;
	final Context context = this;
	private int cnt=0;
	double totalQty=0;
	private int lsize=0;
	Button buttonLeft, buttonRight;
	private SimpleDateFormat dateFormat_database;

	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delivery_payment_view_detail);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//To create instance of user session 
		session = new UserSessionManager(getApplicationContext());
		common = new Common(this);

		//To create instance of date format
		dateFormat_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		//To create instance of database and control
		dba=new DatabaseAdapter(this);
		HeaderDetails = new ArrayList<HashMap<String, String>>();
		listViewMain = (ListView) findViewById(R.id.listViewMain);
		tvNoRecord = (TextView) findViewById(R.id.tvNoRecord);
		tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
		tvTotalPaymentAmount = (TextView) findViewById(R.id.tvTotalPaymentAmount);
		tvHeader =  (TextView) findViewById(R.id.tvHeader);
		buttonLeft = (Button)findViewById(R.id.ButtonLeft);
		buttonRight = (Button)findViewById(R.id.ButtonRight);

		//Call method to enabled icon on top of 'go to home' page



		//To extract id from bundle to show details
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			id = extras.getString("Id");
			header = extras.getString("Header");			
		}

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

				Intent myIntent = new Intent(ActivityDeliveryPaymentViewDetail.this, ActivityDeliveryPaymentViewDetail.class);
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
				Intent myIntent = new Intent(ActivityDeliveryPaymentViewDetail.this, ActivityDeliveryPaymentViewDetail.class);
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
		lables = dba.getDeliveryPaymentDetails(id, deliveryDate);	
		lsize=lables.size();
		if (lables != null && lables.size() > 0) {
			for (HashMap<String, String> lable : lables) {	
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Item", String.valueOf(lable.get("Item"))); 
				hm.put("DQty", String.valueOf(lable.get("DQty")));
				hm.put("DelQty", String.valueOf(lable.get("DelQty")));
				hm.put("Amount", String.valueOf(lable.get("Amount")));
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
			ListAdapter = new MainAdapter(ActivityDeliveryPaymentViewDetail.this);
			listViewMain.setAdapter(ListAdapter);
			tvTotalAmount.setText(Html.fromHtml("<b>Total: "+String.format("%.2f", Double.parseDouble(GetTotal()))+"</b>"));
			dba.open();
			tvTotalPaymentAmount.setText(Html.fromHtml("<b>Total Payment: "+String.format("%.2f", Double.parseDouble(dba.getTotalPayment(id, deliveryDate)))+"</b>"));
			dba.close();
		}			
	}

	//To sum of amount and return as total amount
	private String GetTotal()
	{
		totalQty=0.0;		
		for(int i =0;i<listViewMain.getChildCount();i++)
		{
			View v = listViewMain.getChildAt(i);			
			TextView tvAmount = (TextView)v.findViewById(R.id.tvAmount);
			Double qty = 0.0;
			if(TextUtils.isEmpty(tvAmount.getText().toString().trim()))
				qty=0.0;
			else
				qty=Double.parseDouble(tvAmount.getText().toString().replace(",", ""));

			totalQty = totalQty+ qty;
		}
		return String.valueOf(totalQty);
	}

	//To make class of delivery view holder
	public class MainAdapter extends BaseAdapter {
		class ViewHolder {
			TextView tvItem, tvDQty, tvDelQty, tvAmount; 
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
				arg1 = mInflater.inflate(R.layout.activity_delivery_payment_view_detail_item, null);
				holder = new ViewHolder();

				holder.tvItem = (TextView)arg1.findViewById(R.id.tvItem);
				holder.tvDQty = (TextView)arg1.findViewById(R.id.tvDQty);	
				holder.tvDelQty = (TextView)arg1.findViewById(R.id.tvDelQty);	
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
			holder.tvDelQty.setText(HeaderDetails.get(arg0).get("DelQty"));
			holder.tvAmount.setText(String.format("%.2f", Double.parseDouble(HeaderDetails.get(arg0).get("Amount"))));
			tvTotalAmount.setText(Html.fromHtml("<b>Total: "+String.format("%.2f", Double.parseDouble(GetTotal()))+"</b>"));

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
		intent = new Intent(this, ActivityDeliveryPaymentViewSummary.class);
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
			intent = new Intent(this, ActivityDeliveryPaymentViewSummary.class);
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