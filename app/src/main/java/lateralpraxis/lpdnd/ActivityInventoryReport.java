package lateralpraxis.lpdnd;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lateralpraxis.lpdnd.types.CustomType;

public class ActivityInventoryReport  extends Activity {
	/*Start of code for Variable Declaration*/
	private String lang,userId,responseJSON;
	private int listSize = 0;
	private int year, month, day;
	private ArrayList<HashMap<String, String>> list;
	private ArrayList<HashMap<String, String>> wordList = null;
	HashMap<String, String> map = null;
	/*End of code for Variable Declaration*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	private UserSessionManager session;
	private final Context mContext = this;
	private Calendar calendar;
	private SimpleDateFormat dateFormatter_display, dateFormatter_database;
	/*End of code to declare class*/

	/*Start of code to declare Controls*/
	private TextView tvDate,tvEmpty;
	private ListView listInventory;
	private Button btnGo;
	private Spinner spCentre, spCompany;
	private TableLayout tableGridHead;
	/*End of code to declare Controls*/
	
	//On create method similar to page load
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			//Code to set layout
			setContentView(R.layout.activity_inventory_report);
			ActionBar ab = getActionBar();
			ab.setDisplayHomeAsUpEnabled(true);

			//Code to create instance of classes
			db = new DatabaseAdapter(this);
			common = new Common(this);
			session = new UserSessionManager(getApplicationContext());


			final HashMap<String, String> user = session.getLoginUserDetails();
			userId = user.get(UserSessionManager.KEY_ID);

			lang= session.getDefaultLang();
			Locale myLocale = new Locale(lang);
			Resources res = getResources();
			DisplayMetrics dm = res.getDisplayMetrics();
			Configuration conf = res.getConfiguration();
			conf.locale = myLocale;
			res.updateConfiguration(conf, dm);

			//Start of code to find Controls
			listInventory = (ListView) findViewById(R.id.listInventory);
			tvDate = (TextView) findViewById(R.id.tvDate);
			tvEmpty = (TextView) findViewById(R.id.tvEmpty);
			btnGo= (Button) findViewById(R.id.btnGo);
			spCentre= (Spinner) findViewById(R.id.spCentre);
			spCompany= (Spinner) findViewById(R.id.spCompany);
			tableGridHead = (TableLayout)findViewById(R.id.tableGridHead);
			tableGridHead.setVisibility(View.GONE);
			tvEmpty.setVisibility(View.GONE);
			dateFormatter_display = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
			dateFormatter_database = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

			calendar = Calendar.getInstance();
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH);
			day = calendar.get(Calendar.DAY_OF_MONTH);
			showDate(dateFormatter_display.format(calendar.getTime()));
			if (common.isConnected()) {
			AsyncCentreWSCall task = new AsyncCentreWSCall();
			task.execute();
			}

			btnGo.setOnClickListener(new View.OnClickListener() {
				//On click of add button
				@Override
				public void onClick(View arg0) {
//					if(spCentre.getSelectedItemPosition()==0)
//						common.showToast(lang.equalsIgnoreCase("hi")?"कृपया केंद्र का चयन करें।":"Please select centre.");
//					else if(spCompany.getSelectedItemPosition()==0)
//						common.showToast(lang.equalsIgnoreCase("hi")?"कृपया कंपनी का चयन करें।":"Please select company.");
//					else
//					{
						if (common.isConnected()) {
							AsyncInventoryListWSCall task = new AsyncInventoryListWSCall();
							task.execute();
						}
//					}
				}
			});

		}
		
		//<editor-fold desc="Methods to display the Calendar">
		private DatePickerDialog.OnDateSetListener dateListener = new
				DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
				calendar.set(arg1, arg2, arg3);
				showDate(dateFormatter_display.format(calendar.getTime()));
			}
		};
		//</editor-fold>

		//<editor-fold desc="Methods to Display Selected Date in TextView">
		private void showDate(String date) {
			tvDate.setText(date.replace(" ", "-"));
		}
		//</editor-fold>

		//<editor-fold desc="Methods to open Calendar">
		@SuppressWarnings("deprecation")
		public void setDate(View view) {
			showDialog(999);

		}

		@Override
		protected Dialog onCreateDialog(int id) {
			if (id == 999) {
				DatePickerDialog dialog = new DatePickerDialog(this, dateListener, year, month, day);
				dialog.getDatePicker().setMaxDate(new Date().getTime());
				return dialog;
			}
			return null;
		}
		//</editor-fold>




		private class AsyncCentreWSCall extends AsyncTask<String, Void, String> {
			private ProgressDialog Dialog = new ProgressDialog(ActivityInventoryReport.this);

			@Override
			protected String doInBackground(String... params) {
				try {
					db.open();
					db.deleteCentres();
					db.close();
					String[] name = { "action", "userId", "role" };
					String[] value = { "ReadCentre", userId, "" };
					responseJSON = "";
					// Call method of web service to download route from server
					responseJSON = common.CallJsonWS(name, value, "ReadMaster",
							common.url);

				} catch (SocketTimeoutException e) {
					return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
				} catch (final Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return "ERROR: " + "Unable to fetch response from server.";
				}
				return "";
			}

			@Override
			protected void onPostExecute(String result) {
				try {
					if (!result.contains("ERROR: ")) {
						JSONArray jsonArray = new JSONArray(responseJSON);

						for (int i = 0; i < jsonArray.length(); ++i) {
							db.open();
							db.Insert_Centre(jsonArray.getJSONObject(i).getString("A"), jsonArray.getJSONObject(i).getString("B"));
							db.close();
						}

						//Code to bind Centres in spinner
						spCentre.setAdapter(DataAdapter("centre",""));
						if(common.isConnected())
						{
							AsyncCompanyWSCall taskComp = new AsyncCompanyWSCall();
							taskComp.execute();
						}
					} else {
						common.showAlert(ActivityInventoryReport.this, result, false);
					}
				} catch (Exception e) {
					common.showAlert(ActivityInventoryReport.this, "Centre Downloading failed: " + e.toString(), false);
				}
				Dialog.dismiss();
			}

			@Override
			protected void onPreExecute() {
				Dialog.setMessage("Downloading Centre..");
				Dialog.setCancelable(false);
				Dialog.show();

			}

			@Override
			protected void onProgressUpdate(Void... values) {
			}

		}


		//<editor-fold desc="Code to be Bind Data in list view">
		public static class ViewHolder {
			TextView tvCentreVeh, tvProduct, tvQuantity;
			TableRow tableHeader;
		}

		public class CustomAdapter extends BaseAdapter {
			private Context docContext;
			private LayoutInflater mInflater;

			public CustomAdapter(Context context, ArrayList<HashMap<String, String>> lvList) {
				this.docContext = context;
				mInflater = LayoutInflater.from(docContext);
				list = lvList;
			}

			@Override
			public int getCount() {
				return list.size();
			}

			@Override
			public Object getItem(int arg0) {
				return list.get(arg0);
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

			@Override
			public View getView(final int arg0, View arg1, ViewGroup arg2) {


				final ViewHolder holder;
				if (arg1 == null) {
					arg1 = mInflater.inflate(R.layout.list_inventory_detail, null);
					holder = new ViewHolder();
					
					holder.tvCentreVeh = (TextView) arg1.findViewById(R.id.tvCentreVeh);
					holder.tvProduct = (TextView) arg1.findViewById(R.id.tvProduct);
					holder.tvQuantity = (TextView) arg1.findViewById(R.id.tvQuantity);
					
					holder.tableHeader = (TableRow) arg1.findViewById(R.id.tableHeader);

					arg1.setTag(holder);

				} else {
					holder = (ViewHolder) arg1.getTag();
				}
				
				holder.tvProduct.setText(list.get(arg0).get("Product"));
				holder.tvQuantity.setText(list.get(arg0).get("Quantity").replace(".0", ""));
				
				if(list.get(arg0).get("Flag").equalsIgnoreCase("1"))
					holder.tvCentreVeh.setText("");
				else
					holder.tvCentreVeh.setText(list.get(arg0).get("Location"));

				arg1.setBackgroundColor(Color.parseColor((arg0 % 2 == 1) ? "#EEEEEE" : "#FFFFFF"));
				return arg1;
			}
		}
		//</editor-fold>



		//Code for array list to bind data in spinner
		private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter)
		{
			db.open();
			List <CustomType> lables = db.GetMasterDetails(masterType, filter);
			ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this,android.R.layout.simple_spinner_item, lables);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			db.close();
			return dataAdapter;
		}

		//<editor-fold desc="Async class Class to handle fetch Inventory web service call as separate thread">
		private class AsyncInventoryListWSCall extends AsyncTask<String, Void, String> {
			private ProgressDialog Dialog = new ProgressDialog(ActivityInventoryReport.this);

			@Override
			protected String doInBackground(String... params) {
				try {
					String[] name = {"action", "lang", "centreId", "userId", "date", "compId"};
					String dateString;
					Date date = new Date();
					date = dateFormatter_display.parse(tvDate.getText().toString().trim());
					dateString = dateFormatter_database.format(date);
					String[] value = {"ReadInventory",lang,((CustomType)spCentre.getSelectedItem()).getId(),userId,  dateString, ((CustomType)spCompany.getSelectedItem()).getId()};
					responseJSON = "";
					//Call method of web service to download data from server
					responseJSON = common.CallJsonWS(name, value, "ReadAllReport", common.url);
					return "";
				} catch (SocketTimeoutException e) {
					return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
				} catch (final Exception e) {
					// TODO: handle exception
					return "ERROR: " + "Unable to get response from server.";
				}
			}

			//After execution of web service
			@Override
			protected void onPostExecute(String result) {
				try {
					if (!result.contains("ERROR: ")) {
						JSONArray jsonArray = new JSONArray(responseJSON);
						wordList = new ArrayList<HashMap<String, String>>();
						String prevName = "";
						if (jsonArray.length() > 0) {
							for (int i = 0; i < jsonArray.length(); ++i) {
								map = new HashMap<String, String>();
								map.put("Location", jsonArray.getJSONObject(i)
										.getString("B"));
								map.put("Product", jsonArray.getJSONObject(i)
										.getString("C"));
								map.put("Quantity", jsonArray.getJSONObject(i)
										.getString("D"));
								
								if(prevName.equalsIgnoreCase(jsonArray.getJSONObject(i)
										.getString("B")))
									map.put("Flag", "1");
								else
									map.put("Flag", "0");
								prevName=jsonArray.getJSONObject(i)
										.getString("B");
								wordList.add(map);
							}

						} else {
							if (result.contains("null")) {
								result = "Server not responding. Please try again later.";
								common.showAlert(ActivityInventoryReport.this, result, false);
							}
						}
						listSize = wordList.size();
						if (listSize != 0) {
							listInventory.setAdapter(new CustomAdapter(mContext, wordList));

							ViewGroup.LayoutParams params = listInventory.getLayoutParams();
							listInventory.setLayoutParams(params);
							listInventory.requestLayout();
							tvEmpty.setVisibility(View.GONE);
							tableGridHead.setVisibility(View.VISIBLE);
						} else {
							listInventory.setAdapter(null);
							tvEmpty.setVisibility(View.VISIBLE);
						}
					}
				} catch (Exception e) {
					common.showAlert(ActivityInventoryReport.this, "Inventory Downloading failed: " + "Unable to get response from server.", false);
				}
				Dialog.dismiss();
			}

			//To display message on screen within process
			@Override
			protected void onPreExecute() {
				Dialog.setMessage("Downloading Inventory..");
				Dialog.setCancelable(false);
				Dialog.show();
			}
		}
		//</editor-fold>
		
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;

			case R.id.action_go_to_home: 
				String userRole="";
				final HashMap<String, String> user = session.getLoginUserDetails();
				userRole = user.get(UserSessionManager.KEY_ROLES);
				Intent i;
				if(userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant")))
					i = new Intent(ActivityInventoryReport.this,ActivityAdminHomeScreen.class);
				else
					i = new Intent(ActivityInventoryReport.this,ActivityHomeScreen.class);
				startActivity(i);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

		//Event Triggered on Clicking Back
		@Override
		public void onBackPressed() {
			Intent i = new Intent(ActivityInventoryReport.this,ActivityReport.class);
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

	//To bind Company to spCompany
	private class AsyncCompanyWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(ActivityInventoryReport.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				db.open();
				db.deleteMISCompany();
				db.close();
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCompany", userId, "" };
				responseJSON = "";
				// Call method of web service to download route from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);

			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: " + "Unable to fetch response from server.";
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR: ")) {
					JSONArray jsonArray = new JSONArray(responseJSON);

					for (int i = 0; i < jsonArray.length(); ++i) {
						db.open();
						db.Insert_MISCompany(jsonArray.getJSONObject(i).getString("A"), jsonArray.getJSONObject(i).getString("B"));
						db.close();
					}

					//Code to bind Centres in spinner
					spCompany.setAdapter(DataAdapter("miscompany",""));
				} else {
					common.showAlert(ActivityInventoryReport.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityInventoryReport.this, "Company Downloading failed: " + e.toString(), false);
			}
			Dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Company..");
			Dialog.setCancelable(false);
			Dialog.show();

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}

	}


}
