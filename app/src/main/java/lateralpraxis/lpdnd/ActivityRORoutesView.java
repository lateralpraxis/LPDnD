package lateralpraxis.lpdnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import lateralpraxis.lpdnd.types.CustomType;

public class ActivityRORoutesView extends Activity {
	// declare private variables for database access
	private DatabaseAdapter db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// auto generated method
		super.onCreate(savedInstanceState);

		// code set to layout
		setContentView(R.layout.activity_ro_routes_view);

		// create instance of the database
		db = new DatabaseAdapter(this);
		//Call method to enabled icon on top of 'go to home' page

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		
		ListView lvRoutes = (ListView) findViewById(R.id.lvRoutes);
		// create HashMap
		List<HashMap<String, Object>> arrList = new ArrayList<HashMap<String, Object>>();

		// open database with openR - No need to close the database instance
		db.openR();

		// get Route Officers routes into CustomType
		List<CustomType> routes = db.GetRoutesForRouteOfficers();

		// run the loop to get the route values into HashMap
		for (int x = 0; x < routes.size(); x++) {
			// instantiate the HashMap item
			HashMap<String, Object> hm = new HashMap<String, Object>();

			// get the values into HashMap and then add to ArrayList
			hm.put("id", String.valueOf(routes.get(x).getId()));
			hm.put("name","\u2022 "+String.valueOf(routes.get(x).getName()));
			arrList.add(hm);
		}
		
		// keys used in the HashMap
		String[] from = {"id", "name"};
		
		// values in list_roroutes
		int[] to = {R.id.tvRouteId, R.id.tvRouteName};
		
		// instantiate the adapter to store each each item
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), arrList, R.layout.list_roroutes, from, to);
		lvRoutes.setAdapter(adapter);
		
		lvRoutes.setOnItemClickListener(new OnItemClickListener()   {             
			public void onItemClick(AdapterView<?> lv, View item, int position, long id) 
			{
				Intent intent = new Intent(ActivityRORoutesView.this, ActivityRORouteAllocation.class);
				intent.putExtra("routeId", String.valueOf(((TextView) item.findViewById(R.id.tvRouteId)).getText().toString()));
				intent.putExtra("routeName", String.valueOf(((TextView) item.findViewById(R.id.tvRouteName)).getText().toString()));
				startActivity(intent);
			}  
		});
		
	}

	//When press back button go to home screen
	@Override
	public void onBackPressed() {
		Intent  intent;
			intent = new Intent(this, ActivityHomeScreen.class);
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
				intent = new Intent(this, ActivityHomeScreen.class);
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