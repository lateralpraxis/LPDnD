package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;

import lateralpraxis.lpdnd.types.Payments;

public class ActivityPaymentView extends ListActivity{
	//Code to declare classes
	private DatabaseAdapter db;
	private Common common;
	//Code to declare controls
	private Button btnCreate;
	private TableLayout tableGridHead;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Code to set layout
		setContentView(R.layout.activity_payment_view);
		//Code to create instance of classes
		db=new DatabaseAdapter(this);
		common = new Common(this);
		//Code to set Action Bar
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		/*Code to find controls*/
		btnCreate= (Button) findViewById(R.id.btnCreate);
		tableGridHead= (TableLayout) findViewById(R.id.tableGridHead);

		//Code on button create click event to open create payment intent
		btnCreate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Code to open Payment Creation Intent
				Intent intent = new Intent(ActivityPaymentView.this,ActivityPaymentOnly.class);
				intent.putExtra("custId", "0");
				intent.putExtra("custName", "");
				intent.putExtra("paymentCount", "0");
				startActivity(intent);
				finish();
			}
		});

		BindPaymentReport();

	}
	//Code to click and move to view details intent
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		final String custId = String.valueOf(((TextView)v.findViewById(R.id.tvId)).getText().toString());
		final String custNameString = String.valueOf(((TextView)v.findViewById(R.id.tvName)).getText().toString());
		final String payDate = String.valueOf(((TextView)v.findViewById(R.id.tvMainDate)).getText().toString());

		Intent intent = new Intent(ActivityPaymentView.this, ActivityPaymentDetails.class);
		intent.putExtra("customerId", custId);
		intent.putExtra("customerName",custNameString);
		intent.putExtra("date", payDate);
		startActivity(intent);
	}
	//Code to bind Payment data
	public void BindPaymentReport()
	{
		List<HashMap<String, Object>> aList = new ArrayList<HashMap<String,Object>>();
		db.open();
		List <Payments> lables = db.GetPaymentReport();
		if(lables.size()>0)
			tableGridHead.setVisibility(View.VISIBLE);

		for(int i=0;i<lables.size();i++){
			HashMap<String, Object> hm = new HashMap<String,Object>();
			hm.put("id", String.valueOf(lables.get(i).getId()));
			hm.put("name",String.valueOf(lables.get(i).getName()));
			hm.put("date",common.formateDateFromstring("yyyy-MM-dd", "dd-MMM-yyyy", lables.get(i).getDate()));
			hm.put("exdate",lables.get(i).getDate());


			aList.add(hm); 
		}
		db.close();
		// Keys used in Hashmap
		String[] from = {"id",  "name", "date", "exdate"};
		// Ids of views in listview_layout
		int[] to = {R.id.tvId, R.id.tvName, R.id.tvDate,R.id.tvMainDate};
		// Instantiating an adapter to store each items
		// R.layout.listview_layout defines the layout of each item
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.list_payment_view, from, to);
		//lvAddedCoords.setAdapter(adapter);  
		setListAdapter(adapter);


		ListViewHelper.getListViewSize(getListView());
		//To set height of list view
		setListViewHeightBasedOnItems(getListView());		

	}
	//Code to set list view height
	@SuppressWarnings("unused")
	public boolean setListViewHeightBasedOnItems(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter != null) {

			int numberOfItems = listAdapter.getCount();

			// Get total height of all items.
			int totalItemsHeight = 0;
			for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
				View item = listAdapter.getView(itemPos, null, listView);
				item.measure(0, 0);
				totalItemsHeight += item.getMeasuredHeight();
			}

			// Get total height of all item dividers.
			int totalDividersHeight = listView.getDividerHeight() * 
					(numberOfItems);

			// Set list height.
			ViewGroup.LayoutParams params = listView.getLayoutParams();
			params.height = 1000;//totalItemsHeight * numberOfItems;
			listView.setLayoutParams(params);
			listView.requestLayout();

			return true;

		} else {
			return false;
		}
	}

	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			Intent i = new Intent(ActivityPaymentView.this,ActivityHomeScreen.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(i);
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

	//Event Triggered on Clicking Back
	@Override
	public void onBackPressed() {
		Intent i = new Intent(ActivityPaymentView.this,ActivityHomeScreen.class);
		startActivity(i);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}

	//To create menu on inflater
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_home, menu);
		return true;
	}

}
