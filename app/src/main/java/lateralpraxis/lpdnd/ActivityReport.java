package lateralpraxis.lpdnd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ActivityReport extends Activity {
	//---------------Start of Code for Control Declaration------------//
	private TextView tvHeader;
	Button go, btn;
	TableLayout tl;
	TableRow tr;
	List<Integer> views = Arrays.asList(
			R.layout.btn_demandreport,
			R.layout.btn_allocationreport,
			R.layout.btn_deliveryreport,
			R.layout.btn_cashdepositreport,
			R.layout.btn_stockconvert,
			R.layout.btn_stockadjust,
			R.layout.btn_inventory,
			R.layout.btn_cashoutstanding,
			R.layout.btn_custdelrec,
			R.layout.btn_vehdelrec);
	LinearLayout btnLayout;
	//---------------End of Code for Control Declaration------------//

	//---------------Start of Code for Class Declaration------------//
	private Intent intent;
	final Context context = this;
	static final int ITEM_PER_ROW = 2;
	private UserSessionManager session;
	Common common;
	//---------------End of Code for Class Declaration------------//	
	private String userRole;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reports);

		session = new UserSessionManager(getApplicationContext());
		//To create object of common class
		common = new Common(getApplicationContext());
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		final HashMap<String, String> user = session.getLoginUserDetails();
		tvHeader = (TextView) findViewById(R.id.tvHeader);
		go = (Button) findViewById(R.id.btnGo);
		tl = (TableLayout) findViewById(R.id.tlmainMenu);
		tl.setColumnStretchable(0, true);
		tl.setColumnStretchable(1, true);
		userRole = user.get(UserSessionManager.KEY_ROLES);
		//---------------Code to set Header------------//
		tvHeader.setText("Welcome, "
				+ user.get(UserSessionManager.KEY_USERNAME) + " [ "
				+ Html.fromHtml(userRole.replace(",", ", ")) + " ]");

		go.performClick();

	}


	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnGo:
				int index = 0;
				tl.removeAllViews();
				while (index < views.size()) {
					tr = new TableRow(this);
					TableLayout.LayoutParams trParams =
							new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
									TableLayout.LayoutParams.WRAP_CONTENT);
					trParams.setMargins(0, 8, 0, 0);
					tr.setLayoutParams(trParams);
					tr.setId(index + 1);
					tr.setWeightSum(1);
					for (int k = 0; k < ITEM_PER_ROW; k++) {
						if (index < views.size()) {
							btnLayout = createButton(views.get(index));
							if (k == 0)
								btnLayout.setPadding(8, 2, 4, 0);
							else
								btnLayout.setPadding(4, 2, 8, 0);
							tr.addView(btnLayout);
							index++;
						}
					}
					tl.addView(tr);
				}
				break;
		}
	}

	private LinearLayout createButton(int resource) {
		btnLayout = (LinearLayout) View.inflate(this, resource, null);
		switch (resource) {
			case R.layout.btn_demandreport:
				btn = (Button) btnLayout.findViewById(R.id.btnDemandReport);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityDemandReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_allocationreport:
				btn = (Button) btnLayout.findViewById(R.id.btnAllocationReport);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, AllocationReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_deliveryreport:
				btn = (Button) btnLayout.findViewById(R.id.btnDeliveryReport);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityDeliveryReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_cashdepositreport:
				btn = (Button) btnLayout.findViewById(R.id.btnCashDepositReport);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityCashCollectReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_stockconvert:
				btn = (Button) btnLayout.findViewById(R.id.btnStockConvert);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityConversionReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_stockadjust:
				btn = (Button) btnLayout.findViewById(R.id.btnStockAdjust);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityAdjustmentReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_inventory:
				btn = (Button) btnLayout.findViewById(R.id.btnInventory);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityInventoryReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_cashoutstanding:
				btn = (Button) btnLayout.findViewById(R.id.btnCashOutStanding);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityOutstanding.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_custdelrec:
				btn = (Button) btnLayout.findViewById(R.id.btnCustDelRec);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityCustDelVsRecReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_vehdelrec:
				btn = (Button) btnLayout.findViewById(R.id.btnVehDelRec);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityVehDelVsRecReport.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			default:
				break;
		}

		return btnLayout;
	}
	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.action_go_to_home: 
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//Event Triggered on Clicking Back
	@Override
	public void onBackPressed() {
		Intent i;
		if(userRole.contains("System User") || userRole.contains("Centre User") || userRole.contains("MIS User") || userRole.contains("Management User") && (!userRole.contains("Route Officer") || !userRole.contains("Collection Officer") || !userRole.contains("Accountant")))
			i = new Intent(ActivityReport.this,ActivityAdminHomeScreen.class);
		else
			i = new Intent(ActivityReport.this,ActivityHomeScreen.class);
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
