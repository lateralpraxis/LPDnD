package lateralpraxis.lpdnd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import lateralpraxis.lpdnd.OutletSale.ActivityOutletSaleViewSummary;
import lateralpraxis.lpdnd.primaryreceipt.ActivityListPrimaryReceipt;
import lateralpraxis.lpdnd.stockconversion.ActivityCreateStockConversion;

public class ActivityHomeScreen extends Activity {

	private String userRole, password, userId, loginId;
	private TextView tvHeader;
	final Context context = this;
    static final int ITEM_PER_ROW = 2;
	private Intent intent;
	Common common;
	private UserSessionManager session;
	private static String responseJSON, responseJSON1, sendJSon, newId,
	customerType, strSyncWhat;
	HashMap<String, String> map = null;
	private String imei;
	private DatabaseAdapter dba;
    Button go, btn;
    TableLayout tl;
    TableRow tr;


	List<Integer> views = Arrays.asList(
			R.layout.btn_master, R.layout.btn_sync);
	LinearLayout btnLayout;

	// Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// To create object of user session
		session = new UserSessionManager(getApplicationContext());

		// To create object of common class
		common = new Common(getApplicationContext());

		// To create object of database
		dba = new DatabaseAdapter(getApplicationContext());
		 /*--------Start of Code to find controls -----------------------------*/
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        go = (Button) findViewById(R.id.btnGo);
        tl = (TableLayout) findViewById(R.id.tlmainMenu);
        tl.setColumnStretchable(0, true);
        tl.setColumnStretchable(1, true);
    /*--------End of Code to find controls -----------------------------*/
		strSyncWhat = "";
		try {
			common.copyDBToSDCard("ganeshdairy.db");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			dba.open();
			dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
					"DataBackup Issue");
			dba.close();
			// e.printStackTrace();
		}

		// To check user login session is exist or not
		if (session.checkLogin())
			finish();
		else {
			// To store required information of login user
			imei = common.getIMEI();
			final HashMap<String, String> user = session.getLoginUserDetails();
			tvHeader = (TextView) findViewById(R.id.tvHeader);
			userId = user.get(UserSessionManager.KEY_ID);
			loginId = user.get(UserSessionManager.KEY_CODE);
			password = user.get(UserSessionManager.KEY_PWD);
			userRole = user.get(UserSessionManager.KEY_ROLES);
			customerType = user.get(UserSessionManager.KEY_CUSTOMERTYPE);

			if (userRole.equalsIgnoreCase("Customer"))
				tvHeader.setText("Welcome, "
						+ user.get(UserSessionManager.KEY_USERNAME) + " [ "
						+ customerType + " ]");
			else
				tvHeader.setText("Welcome, "
						+ user.get(UserSessionManager.KEY_USERNAME) + " [ "
						+ Html.fromHtml(userRole.replace(",", ", ")) + " ]");
			
			if (userRole.equalsIgnoreCase("Customer")) {
				if(customerType.equalsIgnoreCase("Retail Outlet"))
                views = Arrays.asList( R.layout.btn_product,R.layout.btn_demand, R.layout.btn_primaryreceipt, R.layout.btn_outlet_sale,R.layout.btn_stockconversion,R.layout.btn_customersync);
				else
					views = Arrays.asList( R.layout.btn_product,R.layout.btn_demand);
			}
			else if(userRole.contains("Route Officer"))
			{
                views = Arrays.asList(R.layout.btn_allocation, R.layout.btn_demand, R.layout.btn_delivery, R.layout.btn_payment,R.layout.btn_cashdeposit,R.layout.btn_return,R.layout.btn_master,R.layout.btn_report,R.layout.btn_sync);
			}
			else if(userRole.contains("Collection Officer"))
			{
                views = Arrays.asList( R.layout.btn_payment,R.layout.btn_cashdeposit,R.layout.btn_master,R.layout.btn_report,R.layout.btn_sync);
			}
            else if(userRole.contains("Accountant"))
            {
                views = Arrays.asList( R.layout.btn_payment,R.layout.btn_master,R.layout.btn_report,R.layout.btn_sync);
            }

            go.performClick();

			String[] params = { "1" };
			// To download customer master in 'Route Officer' role
			if (userRole.contains("Route Officer")) {
				dba.openR();
				if (dba.IsSyncRequiredForRouteOfficer()) {
					String[] myTaskParams = { "masters" };
					// call method of get customer json web service
					AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
					task.execute(myTaskParams);
				}
			}
			else if(userRole.contains("Accountant") || userRole.equalsIgnoreCase("Collection Officer")) {
				dba.openR();
				if (dba.IsSyncRequiredForAccountant()) {
					String[] myTaskParams = { "masters" };
					// call method of get customer json web service
					AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
					task.execute(myTaskParams);
				}
			}
			else {
				dba.openR();
				if (dba.IsSyncRequiredForCustomer()) {
					String[] myTaskParams = { "masters" };
					// call method of get customer json web service
					AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
					task.execute(myTaskParams);
				}
			}
		}
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
            case R.layout.btn_allocation:
                btn = (Button) btnLayout.findViewById(R.id.btnAllocation);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        intent = new Intent(context,
                                ActivityRORouteAllocation.class);
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case R.layout.btn_demand:
                btn = (Button) btnLayout.findViewById(R.id.btnDemandView);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (common.isConnected()) {
                            // call method of view demand json web service
                            AsyncViewDemandWSCall task = new AsyncViewDemandWSCall();
                            task.execute(new String[] { "3" });
                        }
                    }
                });
                break;
            case R.layout.btn_delivery:
                btn = (Button) btnLayout.findViewById(R.id.btnDeliveryView);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (common.isConnected()) {
                            intent = new Intent(context,
                                    ActivityDeliveryViewSummary.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                break;
            case R.layout.btn_cashdeposit:
                btn = (Button) btnLayout.findViewById(R.id.btnCashDepositView);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        strSyncWhat = "cashdeposit";
                        if (common.isConnected()) {
                            if (common.isConnected()) {
                                String[] myTaskParams = { "transactions" };
                                AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                task.execute(myTaskParams);
                            }
                        }
                    }
                });
                break;
            case R.layout.btn_payment:
                btn = (Button) btnLayout.findViewById(R.id.btnPayment);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        intent = new Intent(context, ActivityPaymentView.class);
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case R.layout.btn_product:
                btn = (Button) btnLayout.findViewById(R.id.btnCustomerProduct);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (common.isConnected()) {
                            AsyncProductMasterWSCall task = new AsyncProductMasterWSCall();
                            task.execute(new String[] { "2" });
                        }
                    }
                });
                break;
            case R.layout.btn_return:
                btn = (Button) btnLayout.findViewById(R.id.btnReturn);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (common.isConnected()) {
                            intent = new Intent(context,
                                    ActivityStockReturnViewSummary.class);
                            startActivity(intent);
                            finish();
                        } else {
                            intent = new Intent(context, ActivityHomeScreen.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                break;
            case R.layout.btn_master:
                btn = (Button) btnLayout.findViewById(R.id.btnMasters);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(context, ActivityMasters.class);
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case R.layout.btn_report:
                btn = (Button) btnLayout.findViewById(R.id.btnReports);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent = new Intent(context,
                                ActivityReport.class);
                        intent.putExtra("From", "Home");
                        startActivity(intent);
                        finish();
                    }
                });
                break;
            case R.layout.btn_sync:
                btn = (Button) btnLayout.findViewById(R.id.btnSyncTrans);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        strSyncWhat = "masters";
                        if (common.isConnected()) {

                            if (common.isConnected()) {
                                String[] myTaskParams = { "transactions" };
                                // call method of get customer json web service
                                AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
                                task.execute(myTaskParams);
                            }
                        } else {
                            common.showAlert(ActivityHomeScreen.this,
                                    "Unable to connect to Internet !", false);
                        }
                    }
                });
                break;
			case R.layout.btn_stockconversion:
				btn = (Button) btnLayout.findViewById(R.id.btnStockConversion);
				btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						intent = new Intent(context,
								ActivityCreateStockConversion.class);
						startActivity(intent);
						finish();
					}
				});
				break;
			case R.layout.btn_customersync:
				btn = (Button) btnLayout.findViewById(R.id.btnSync);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (common.isConnected()) {

							if (common.isConnected()) {
								String[] myTaskParams = { "transactions" };
								// call method of get customer json web service
								AsyncCustomerValidatePasswordWSCall task = new AsyncCustomerValidatePasswordWSCall();
								task.execute(myTaskParams);
							}
						} else {
							common.showAlert(ActivityHomeScreen.this,
									"Unable to connect to Internet !", false);
						}
					}
				});
				break;

			case R.layout.btn_primaryreceipt:
                btn = (Button) btnLayout.findViewById(R.id.btnPrimaryReceipt);
                btn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
						intent = new Intent(context, ActivityListPrimaryReceipt.class);
						startActivity(intent);
						finish();
                    }
                });
                break;
			case R.layout.btn_outlet_sale:
				btn = (Button) btnLayout.findViewById(R.id.btnOutletSale);
				btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						intent = new Intent(context, ActivityOutletSaleViewSummary.class);
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


	// Class to handle route web services call as separate thread
	private class AsyncUpdateMastersRouteOfficerDetailsWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadRouteOfficerData", userId, userRole };
				responseJSON = "";
				// Call method of web service to download route from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download route
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display route after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);

					for (int i = 0; i < jsonArray.length(); ++i) {
						session.updateRouteOfficerDetails(jsonArray
								.getJSONObject(i).getString("RouteId"),
								jsonArray.getJSONObject(i)
								.getString("CentreId"), jsonArray
								.getJSONObject(i)
								.getString("VehicleId"));
					}
					dba.close();
					String[] params = { "1" };
					if (common.isConnected()) {
						// call method of get customer json web service
						AsyncRouteWSCall task = new AsyncRouteWSCall();
						task.execute(params);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Route Officer Assignments Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display route on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Route Officer Assignments...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	private class AsyncUpdateTransactionsRouteOfficerDetailsWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadRouteOfficerData", userId, userRole };
				responseJSON = "";
				// Call method of web service to download route from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download route
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display route after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);

					for (int i = 0; i < jsonArray.length(); ++i) {
						session.updateRouteOfficerDetails(jsonArray
								.getJSONObject(i).getString("RouteId"),
								jsonArray.getJSONObject(i)
								.getString("CentreId"), jsonArray
								.getJSONObject(i)
								.getString("VehicleId"));
					}

					dba.close();
					if (common.isConnected()) {
						AsyncTransCustomerLedgerWSCall task = new AsyncTransCustomerLedgerWSCall();
						task.execute();
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Route Officer Assignments Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display route on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Route Officer Assignments...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle customer ledger web services call as separate thread
	private class AsyncTransCustomerLedgerWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCustomerLedger", userId, userRole };
				responseJSON = "";
				// Call method of web service to download customer Ledger from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to download Customer Ledger
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display customer ledger after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("CustomerLedger");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getCustomerId = jsonArray.getJSONObject(i)
								.getString("A");
						String getCompanyId = jsonArray.getJSONObject(i)
								.getString("B");
						Double getBalance = Double.valueOf(jsonArray
								.getJSONObject(i).getString("C"));
						dba.Insert_CustomerLedger(getCustomerId, getCompanyId,
								getBalance);
					}
					dba.close();
					if (common.isConnected()) {
						if (strSyncWhat.equalsIgnoreCase("masters")) {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									context);
							// set title
							alertDialogBuilder.setTitle("Sync Successful");
							// set dialog message
							alertDialogBuilder
							.setMessage(
									"Transaction Synchronization completed successfully. It is recommended to synchronize master data. Do you want to continue?")
									.setCancelable(false)
									.setPositiveButton(
											"Yes",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {

													if (common.isConnected()) {
														String[] params = { "0" };
														AsyncRouteWSCall task = new AsyncRouteWSCall();
														task.execute(params);
													}
												}
											})
											.setNegativeButton(
													"No",
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int id) {
															// if this button is
															// clicked, just close
															dialog.cancel();
														}
													});
							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder
									.create();
							// show it
							alertDialog.show();
						} else {
							if (common.isConnected()) {

								// To call web services to get cash deposit
								// details
								AsyncCashDepositWSCall task = new AsyncCashDepositWSCall();
								task.execute();
							} else {
								intent = new Intent(context,
										ActivityHomeScreen.class);
								startActivity(intent);
								finish();
							}
						}
					} else {
						common.showAlert(ActivityHomeScreen.this,
								"Unable to connect to Internet !", false);
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Customer Ledger Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display Customer Ledger on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Customer Ledger...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle route web services call as separate thread
	private class AsyncRouteWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadRoute", userId, userRole };
				responseJSON = "";
				// Call method of web service to download route from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download route
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display route after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("Route");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getId = jsonArray.getJSONObject(i)
								.getString("A");
						String getName = jsonArray.getJSONObject(i).getString(
								"B");
						String getCentreId = jsonArray.getJSONObject(i)
								.getString("C");
						dba.Insert_Route(getId, getName, getCentreId);
					}
					dba.close();

					if (common.isConnected()) {
						// call method of get bank json web service
						AsyncBankWSCall task = new AsyncBankWSCall();
						task.execute(result);
					} else {
						common.showAlert(ActivityHomeScreen.this,
								"Unable to connect to Internet !", false);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Route Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display route on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Route...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle bank web services call as separate thread
	private class AsyncBankWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadBank", userId, userRole };
				responseJSON = "";
				// Call method of web service to download bank from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download bank
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display bank after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("Bank");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getId = jsonArray.getJSONObject(i)
								.getString("A");
						String getName = jsonArray.getJSONObject(i).getString(
								"B");
						dba.Insert_Bank(getId, getName);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get vehicle json web service
						AsyncVehicleWSCall task = new AsyncVehicleWSCall();
						task.execute(result);
					} else {
						common.showAlert(ActivityHomeScreen.this,
								"Unable to connect to Internet !", false);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Bank Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display bank on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Bank...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle vehicle web services call as separate thread
	private class AsyncVehicleWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadVehicle", userId, userRole };
				responseJSON = "";
				// Call method of web service to download vehicle from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download vehicle
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display vehicle after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("Vehicle");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getId = jsonArray.getJSONObject(i)
								.getString("A");
						String getName = jsonArray.getJSONObject(i).getString(
								"B");
						String getRoute = jsonArray.getJSONObject(i).getString(
								"C");
						dba.Insert_Vehicle(getId, getName, getRoute);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get company json web service
						AsyncCompanyWSCall task = new AsyncCompanyWSCall();
						task.execute(result);
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Vehicle Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display vehicle on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Vehicle...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle Company web services call as separate thread
	private class AsyncCompanyWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCompany", userId, userRole };
				responseJSON = "";
				// Call method of web service to download vehicle from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download company
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// create array fro company data from response
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					// Delete data from Company Master Table
					dba.DeleteMasterData("Company");
					// Loop through array and insert in company master table
					for (int i = 0; i < jsonArray.length(); ++i) {
						String id = jsonArray.getJSONObject(i).getString("A");
						String name = jsonArray.getJSONObject(i).getString("B");
						String shortName = jsonArray.getJSONObject(i)
								.getString("C");
						dba.Insert_Company(id, name, shortName);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get route json web service
						AsyncDeliveryInputDemandWSCall task = new AsyncDeliveryInputDemandWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Company Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display company on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Company...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle delivery input demand web services call as separate
	// thread
	private class AsyncDeliveryInputDemandWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadDeliveryInputDemand", userId, userRole };
				responseJSON = "";
				// Call method of web service to download delivery input demand
				// from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download delivery input demand
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display delivery input demand after response from
					// server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("DeliveryInputDemand");
					dba.close();
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getCustomerId = jsonArray.getJSONObject(i)
								.getString("A");
						String getSKUId = jsonArray.getJSONObject(i).getString(
								"B");
						String getDQty = jsonArray.getJSONObject(i).getString(
								"C");
						dba.open();
						dba.Insert_DeliveryInputDemand(getCustomerId, getSKUId,
								getDQty);
						dba.close();
					}

					if (common.isConnected()) {
						// call method of get customer ledger json web service
						AsyncDeliveryInputWSCall task = new AsyncDeliveryInputWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {

				common.showAlert(ActivityHomeScreen.this,
						"DeliveryInputDemand Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display delivery input demand on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading DeliveryInput Demand...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle delivery input web services call as separate thread
	private class AsyncDeliveryInputWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadDeliveryInput", userId, userRole };
				responseJSON = "";
				// Call method of web service to download delivery input from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			}
		}

		// After execution of web service to download delivery input
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display delivery input after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("DeliveryInput");
					dba.close();
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getId = jsonArray.getJSONObject(i)
								.getString("A");
						String getName = jsonArray.getJSONObject(i).getString(
								"B");
						String getType = jsonArray.getJSONObject(i).getString(
								"C");
						String getAQty = jsonArray.getJSONObject(i).getString(
								"D");
						String getCompany = jsonArray.getJSONObject(i)
								.getString("E");
						String getRoute = jsonArray.getJSONObject(i).getString(
								"F");
						String getProduct = jsonArray.getJSONObject(i)
								.getString("G");
						String getSkuUnit = jsonArray.getJSONObject(i)
								.getString("H");
						String getUom = jsonArray.getJSONObject(i).getString(
								"I");
						String getProductLocal = jsonArray.getJSONObject(i)
								.getString("J");
						String getMulFactor = jsonArray.getJSONObject(i)
								.getString("K");
						dba.open();
						dba.Insert_DeliveryInput(getId, getName, getType,
								getAQty, getCompany, getRoute, getProduct,
								getSkuUnit.replace(".0", ""), getUom,
								getProductLocal, getMulFactor);
						dba.close();
					}

					if (common.isConnected()) {
						// call method of get customer ledger json web service
						AsyncCustomerLedgerWSCall task = new AsyncCustomerLedgerWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {

				common.showAlert(ActivityHomeScreen.this,
						"DeliveryInput Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display delivery input on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading DeliveryInput...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	private class AsyncStockReturnTransactionWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {

				JSONObject jsonStockRet = new JSONObject();
				dba.open();
				ArrayList<HashMap<String, String>> srmast = dba
						.getStockReturn();
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
							jsonsret.put("TransactionDate",
									srd.get("ReturnDate"));
							jsonsret.put("UserId", srd.get("CreateBy"));
							jsonsret.put("ipAddress",
									common.getDeviceIPAddress(true));
							jsonsret.put("Machine", common.getIMEI());
							array.put(jsonsret);
						}
						jsonStockRet.put("SR", array);

					} catch (JSONException e) {
						// TODO Auto-generated catch block

						return "ERROR: " + e.getMessage();
					} finally {
						dba.close();
					}

					JSONObject jsonDetails = new JSONObject();
					dba.open();
					ArrayList<HashMap<String, String>> insdet = dba
							.getStockReturnDetails();
					dba.close();
					if (insdet != null && insdet.size() > 0) {
						JSONArray arraydet = new JSONArray();
						try {
							for (HashMap<String, String> insd : insdet) {
								JSONObject jsondet = new JSONObject();
								// jsondet.put("ReturnId",
								// insd.get("ReturnId"));
								jsondet.put("SKUId", insd.get("SKUId"));
								jsondet.put("ReturnQuantity",
										insd.get("ReturnQty"));
								jsondet.put("LeakageQuantity",
										insd.get("LeakageQty"));
								jsondet.put("UniqueId", insd.get("UniqueId"));
								arraydet.put(jsondet);
							}

							jsonDetails.put("SRD", arraydet);

						} catch (JSONException e) {
							// TODO Auto-generated catch block

						} finally {
							dba.close();
						}
					}

					sendJSon = jsonStockRet + "~" + jsonDetails;

					responseJSON = common.invokeJSONWS(sendJSon, "json",
							"CreateStockReturn", common.url);

				} else {
					return "No Stock Return are pending to be Send to server!";
				}

				return "";
			} catch (Exception e) {
				// TODO: handle exception
				return "ERROR: " + e.getMessage();
			} finally {
				dba.close();
			}

		}

		@Override
		protected void onPostExecute(String result) {

			try {

				if (!result.contains("ERROR")) {
					if (responseJSON.equalsIgnoreCase("success")) {
						dba.open();
						// dba.Update_StockReturnDetails(newId);
						dba.DeleteStockReturn();
						dba.close();
					}

					if (common.isConnected()) {
						// call method of payment json web service
						AsyncPaymentWSCall task = new AsyncPaymentWSCall();
						task.execute();
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}

			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Synchronizing failed: "
								+ "Unable to get response from server.", false);
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

	// To make web service class to logout user from login
	private class AsyncLogOutWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				responseJSON = "";
				JSONObject json = new JSONObject();
				json.put("username", loginId);
				json.put("password", password);
				json.put("imei", imei);
				json.put("role", userRole);
				// To invoke json method to logout user
				responseJSON = common.invokeJSONWS(json.toString(), "json",
						"LogoutUserAndroid", common.url);
			} catch (SocketTimeoutException e) {
				dba.open();
				dba.insertExceptions("TimeOut Exception. Internet is slow",
						"ActivityHomeScreen.java", "AsyncLogOutWSCall");
				dba.close();
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				dba.open();
				dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
						"AsyncLogOutWSCall");
				dba.close();
				return "ERROR: " + e.getMessage();
			}
			return responseJSON;
		}

		// After execution of web service to logout user
		@Override
		protected void onPostExecute(String result) {
			try {

				// To display message after response from server
				if (result.contains("success")) {
					session.logoutUser();
					common.showToast("You have been logged out successfully!");
					finish();
				} else {
					common.showAlert(ActivityHomeScreen.this,
							"Unable to get response from server.", false);
				}
			} catch (Exception e) {
				dba.open();
				dba.insertExceptions(e.getMessage(), "ActivityHomeScreen.java",
						"AsyncLogOutWSCall");
				dba.close();
				common.showAlert(ActivityHomeScreen.this, "Log out failed: "
						+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Logging out ..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// To create menu of logout on top of screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home_screen, menu);
		return true;
	}

	// Event hander of logout menu on top of screen
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_logout) {

			// "Log Out",
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			// set title
			alertDialogBuilder.setTitle("Confirmation");
			// set dialog message
			alertDialogBuilder
			.setMessage("Are you sure, you want to Log Out ?")
			.setCancelable(false)
			.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int id) {
					if (common.isConnected()) {
						dba.open();
						if (dba.IslogoutAllowed()) {
							dba.deleteAllData();
							dba.close();
							AsyncLogOutWSCall task = new AsyncLogOutWSCall();
							task.execute();
						} else {
							common.showToast("Can not logout as data is pending to synchronizing.");
						}
					} else {
						common.showToast("Unable to connect to Internet !");
					}
				}
			})
			.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
						int id) {
					// if this button is clicked, just close
					dialog.cancel();
				}
			});
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return true;
		} else if (id == R.id.action_changelanguage) {
			ArrayList<String> myLanguagelist = new ArrayList<String>();
			myLanguagelist.clear();
			final HashMap<String, String> user = session.getLoginUserDetails();
			String[] items = user.get(UserSessionManager.KEY_OPTLANG)
					.split(",");

			for (String st : items) {
				if (st.equals("English")) {
					myLanguagelist.add(getResources().getString(
							R.string.lang_eng));
				} else if (st.equals("Hindi")) {
					myLanguagelist.add(getResources().getString(
							R.string.lang_hindi));
				}
			}

			final AlertDialog.Builder builderSingle = new AlertDialog.Builder(
					ActivityHomeScreen.this);
			builderSingle.setTitle(getResources().getString(
					R.string.change_language));

			final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
					ActivityHomeScreen.this,
					android.R.layout.select_dialog_singlechoice, myLanguagelist);

			// radio button highlight
			int selected = 0;
			String lang = session.getDefaultLang();

			if (lang.equalsIgnoreCase("en")) {
				selected = 0;

			} else if (lang.equalsIgnoreCase("hi")) {
				selected = 1;

			}

			builderSingle.setSingleChoiceItems(arrayAdapter, selected,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					String setLang = arrayAdapter.getItem(item);

					String langCode = "";
					if (setLang.equalsIgnoreCase(getResources()
							.getString(R.string.lang_eng))) {
						langCode = "en";

					} else if (setLang.equalsIgnoreCase(getResources()
							.getString(R.string.lang_hindi))) {
						langCode = "hi";

					}

					session.updatePrefLanguage(langCode);
					dialog.cancel();

					Locale myLocale = new Locale(langCode);
					Resources res = getResources();
					DisplayMetrics dm = res.getDisplayMetrics();
					Configuration conf = res.getConfiguration();
					conf.locale = myLocale;
					res.updateConfiguration(conf, dm);
					common.showToast("You have selected " + setLang);
					Intent refresh = new Intent(context,
							ActivityHomeScreen.class);
					startActivity(refresh);

				}
			});
			builderSingle.show();
		} else if (id == R.id.action_changepassword) {
			Intent i = new Intent(ActivityHomeScreen.this,
					ActivityChangePassword.class);
			i.putExtra("fromwhere", "home");
			startActivity(i);
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	// To display text in text box from right to left in case of number
	@SuppressLint("RtlHardcoded")
	private void populateText(LinearLayout ll, ArrayList<View> views,
			Context mContext) {
		ll.removeAllViews();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int maxWidth = displaymetrics.widthPixels - 20;

		// To get layout and set gravity
		LinearLayout.LayoutParams params;
		LinearLayout newLL = new LinearLayout(mContext);
		newLL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		newLL.setGravity(Gravity.LEFT);
		newLL.setOrientation(LinearLayout.HORIZONTAL);

		int widthSoFar = 0;

		for (int i = 0; i < views.size(); i++) {
			LinearLayout LL = new LinearLayout(mContext);
			LL.setOrientation(LinearLayout.HORIZONTAL);
			LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
			LL.setLayoutParams(new ListView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			// To measure width of text box to set margins
			views.get(i).measure(0, 0);
			params = new LinearLayout.LayoutParams(views.get(i)
					.getMeasuredWidth(), LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 15, 15, 0);

			// To add this view to current layout
			LL.addView(views.get(i), params);
			LL.measure(0, 0);
			widthSoFar += views.get(i).getMeasuredWidth();// YOU MAY NEED TO ADD
			// THE MARGINS
			if (widthSoFar >= maxWidth) {
				ll.addView(newLL);
				newLL = new LinearLayout(mContext);
				newLL.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				newLL.setOrientation(LinearLayout.HORIZONTAL);
				newLL.setGravity(Gravity.LEFT);
				params = new LinearLayout.LayoutParams(LL.getMeasuredWidth(),
						LL.getMeasuredHeight());
				newLL.addView(LL, params);
				widthSoFar = LL.getMeasuredWidth();
			} else {
				newLL.addView(LL);
			}
		}
		ll.addView(newLL);
	}

	// Class to handle customer master web services call as separate thread
	private class AsyncCustomerMasterWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCustomerMaster", userId, userRole };
				responseJSON = "";
				// Call method of web service to download customer master master
				// from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download customer master master
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("CustomerMaster");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String routeId = jsonArray.getJSONObject(i).getString(
								"A");
						String route = jsonArray.getJSONObject(i)
								.getString("B");
						String customerId = jsonArray.getJSONObject(i)
								.getString("C");
						String customer = jsonArray.getJSONObject(i).getString(
								"D");
						String mobile = jsonArray.getJSONObject(i).getString(
								"E");
						String customerType = jsonArray.getJSONObject(i)
								.getString("F");
						String loginId = jsonArray.getJSONObject(i).getString(
								"G");
						String customerLocal = jsonArray.getJSONObject(i)
								.getString("H");
						dba.Insert_CustomerMaster(routeId, route, customerId,
								customer, mobile, customerType, loginId,
								customerLocal);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get route json web service
						AsyncVehicleMasterWSCall task = new AsyncVehicleMasterWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Customer Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Customer ..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle vehicle master web services call as separate thread
	private class AsyncVehicleMasterWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadRouteVehicle", userId, userRole };
				responseJSON = "";
				// Call method of web service to download vehicle master from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download vehicle master
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("RouteVehicleMaster");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String routeId = jsonArray.getJSONObject(i).getString(
								"A");
						String route = jsonArray.getJSONObject(i)
								.getString("B");
						String vehicleId = jsonArray.getJSONObject(i)
								.getString("C");
						String vehicle = jsonArray.getJSONObject(i).getString(
								"D");
						String capacity = jsonArray.getJSONObject(i).getString(
								"E");
						dba.Insert_RouteVehicleMaster(routeId, route,
								vehicleId, vehicle, capacity);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get route json web service
						AsyncViewDemandWSCall task = new AsyncViewDemandWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Vehicle Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Vehicle..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle product master web services call as separate thread
	private class AsyncProductMasterWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadProductMaster", userId, userRole };
				responseJSON = "";
				// Call method of web service to download product master from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download product master
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ProductMaster");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String companyId = jsonArray.getJSONObject(i)
								.getString("A");
						String companyName = jsonArray.getJSONObject(i)
								.getString("B");
						String skuId = jsonArray.getJSONObject(i)
								.getString("C");
						String skuName = jsonArray.getJSONObject(i).getString(
								"D");
						String packingType = jsonArray.getJSONObject(i)
								.getString("E");
						String rate = jsonArray.getJSONObject(i).getString("F");
						String getNameLocal = jsonArray.getJSONObject(i)
								.getString("G");
						String getProductName = jsonArray.getJSONObject(i)
								.getString("H");
						String getSkuUnit = jsonArray.getJSONObject(i)
								.getString("I");
						String getUom = jsonArray.getJSONObject(i).getString(
								"J");
						dba.Insert_ProductMaster(companyId, companyName, skuId,
								skuName, packingType, rate, getNameLocal,
								getProductName, getSkuUnit, getUom);
					}
					dba.close();
					if (result.equalsIgnoreCase("0"))
						common.showAlert(ActivityHomeScreen.this,
								"Synchronization completed successfully.",
								false);
					else if (result.equalsIgnoreCase("2")) {
						intent = new Intent(context,
								ActivityProductMaster.class);
						startActivity(intent);
						finish();
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Product Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Product..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}


	// Class to handle promotional message web services call as separate thread
	private class AsyncMsgWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadMsg", userId, userRole };
				responseJSON = "";
				// Call method of web service to download promotional message
				// from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return "";
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download promotional message
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("Msg");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getId = jsonArray.getJSONObject(i)
								.getString("A");
						String getName = jsonArray.getJSONObject(i).getString(
								"B");
						dba.Insert_Msg(getId, getName);
					}
					dba.close();
					intent = new Intent(context,
							ActivityMessageViewSummary.class);
					startActivity(intent);
					finish();
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Offers Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Offers..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle demand / allocated web service call as separate thread
	private class AsyncViewDemandWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						"yyyy-MM-dd", Locale.US);
				// To bind default demand date
				final Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 1);
				String demandDate = dateFormatter.format(c.getTime());
				dba.open();
				dba.Insert_DemandDate(demandDate);
				dba.close();

				String[] name = { "action", "userId", "role", "demandDate" };
				String[] value = { "ReadDemand", userId, userRole, demandDate };
				responseJSON = "";
				// Call method of web service to download demand / allocation
				// from server
				responseJSON = common.CallJsonWS(name, value,
						"ReadDemandMaster", common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}

		}

		// After execution of web service for demand / allocation
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ViewDemand");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String id = jsonArray.getJSONObject(i).getString("A");
						String name = jsonArray.getJSONObject(i).getString("B");
						dba.Insert_ViewDemand(id, name);
					}
					dba.close();
					if (common.isConnected()) {
						if (result.equalsIgnoreCase("3")) {
							// Call web services of demand details
							AsyncViewDemandDetailsWSCall task = new AsyncViewDemandDetailsWSCall();
							task.execute(result);
						} else {
							// call method of get route json web service
							AsyncProductMasterWSCall task = new AsyncProductMasterWSCall();
							task.execute(result);
						}
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Demand Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Demand..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}


	// Class to handle Raw Material web service call as separate thread
	private class AsyncRawMaterialMasterWSCall extends
			AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadRawMaterial", userId, userRole };
				responseJSON = "";
				// Call method of web service to download raw Material master from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download Raw Material Master
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("RawMaterialMaster");
					for (int i = 0; i < jsonArray.length(); ++i) {
						dba.Insert_RawMaterialMaster(jsonArray.getJSONObject(i)
								.getString("A"), jsonArray.getJSONObject(i)
								.getString("B"), jsonArray.getJSONObject(i)
								.getString("C"), jsonArray.getJSONObject(i)
								.getString("D"));
					}
					dba.close();
					if (common.isConnected()) {
						AsyncSKUMasterWSCall task = new AsyncSKUMasterWSCall();
							task.execute(result);

					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Raw Material Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Raw Material..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}


	// Class to handle SKUMater web service call as separate thread
	private class AsyncSKUMasterWSCall extends
			AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadSKU", userId, userRole };
				responseJSON = "";
				// Call method of web service to download SKU master from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download SKU Master
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("SKUMaster");
					for (int i = 0; i < jsonArray.length(); ++i) {
						dba.Insert_SKUMaster(jsonArray.getJSONObject(i)
								.getString("A"), jsonArray.getJSONObject(i)
								.getString("B"), jsonArray.getJSONObject(i)
								.getString("C"), jsonArray.getJSONObject(i)
								.getString("D"), jsonArray.getJSONObject(i)
								.getString("E"));
					}
					dba.close();
					if (common.isConnected()) {
						AsyncSaleRateMasterWSCall task = new AsyncSaleRateMasterWSCall();
						task.execute(result);

					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"SKU Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading SKU..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle SaleRateMaster web service call as separate thread
	private class AsyncSaleRateMasterWSCall extends
			AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadSKUSaleRate", userId, userRole };
				responseJSON = "";
				// Call method of web service to download product master from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download Raw Material Master
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("SaleRateMaster");
					for (int i = 0; i < jsonArray.length(); ++i) {
						dba.Insert_SaleRateMaster(jsonArray.getJSONObject(i)
								.getString("A"), jsonArray.getJSONObject(i)
								.getString("B"), jsonArray.getJSONObject(i)
								.getString("C"), jsonArray.getJSONObject(i)
								.getString("D"));
					}
					dba.close();
					if (common.isConnected()) {
						AsyncRetailOutletInventoryWSCall task = new AsyncRetailOutletInventoryWSCall();
						task.execute(result);

					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Sale Rate Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Sale Rate..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	private class AsyncRetailOutletInventoryWSCall extends
			AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadOutletInventory", userId, userRole };
				responseJSON = "";
				// Call method of web service to download Reatil Outlet Inventory from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service to download Retail Outlet Inventory
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("OutletInventory");
					for (int i = 0; i < jsonArray.length(); ++i) {
						dba.Insert_OutletInventory(jsonArray.getJSONObject(i)
								.getString("A"), jsonArray.getJSONObject(i)
								.getString("B"), jsonArray.getJSONObject(i)
								.getString("C"));
					}
					dba.close();
						common.showAlert(ActivityHomeScreen.this,
								"Synchronization completed successfully.",
								false);
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Inventory Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Inventory..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
	// Class to handle demand / allocated details web service call as separate
	// thread
	private class AsyncViewDemandDetailsWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
						"yyyy-MM-dd", Locale.US);
				// To bind default demand date
				final Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, 1);
				String demandDate = dateFormatter.format(c.getTime());

				String[] name = { "action", "userId", "role", "demandDate" };
				String[] value = { "ReadDemandDetails", userId, userRole,
						demandDate };
				responseJSON = "";
				// Call method of web service to download demand / allocation
				// details from server
				responseJSON = common.CallJsonWS(name, value,
						"ReadDemandMaster", common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			}
		}

		// After execution of web service for demand / allocation details
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ViewDemandDetails");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String id = jsonArray.getJSONObject(i).getString("A");
						String item = jsonArray.getJSONObject(i).getString("B");
						String qty = jsonArray.getJSONObject(i).getString("C");
						String packingType = jsonArray.getJSONObject(i)
								.getString("D");
						String rate = jsonArray.getJSONObject(i).getString("E");
						String getNameLocal = jsonArray.getJSONObject(i)
								.getString("F");
						String getProductName = jsonArray.getJSONObject(i)
								.getString("G");
						String getSkuUnit = jsonArray.getJSONObject(i)
								.getString("H");
						String getUom = jsonArray.getJSONObject(i).getString(
								"I");
						dba.Insert_ViewDemandDetails(id, item, qty,
								packingType, rate, getNameLocal,
								getProductName, getSkuUnit, getUom);
					}
					dba.close();
					if (common.isConnected()) {
						// To call web services to display demand cut off time
						AsyncDemandCutOffDisplayWSCall task = new AsyncDemandCutOffDisplayWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Demand Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Demand..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle demand cut off web services call as separate thread
	private class AsyncDemandCutOffDisplayWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {

				// Call method of web service to download demand cut off masters
				// from server
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadDemandCutOffDisplay", userId, userRole };

				responseJSON = "";
				responseJSON = common.CallJsonWS(name, value,
						"ReadDemandCutOffDisplay", common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return "ERROR: " + e.getMessage();
			}

		}

		// After execution of demand cut off web service
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To insert data after response from server
					dba.open();
					dba.DeleteMasterData("DemandCutOff");
					dba.Insert_DemandCutOff(responseJSON);
					dba.close();
					if (result.equalsIgnoreCase("3")) {
						if (userRole.equalsIgnoreCase("Customer")) {
							intent = new Intent(context,
									ActivityDemandViewDetail.class);
							intent.putExtra("Id", userId);
							startActivity(intent);
							finish();
						} else {
							intent = new Intent(context,
									ActivityDemandViewSummary.class);
							startActivity(intent);
							finish();
						}
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(
						ActivityHomeScreen.this,
						"DemandCutOffDisplay Downloading failed: "
								+ e.toString(), false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Demand CutOff Time..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// To make web service class to post data of delivery
	private class AsyncDeliveryWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {

			/*
			 * HttpURLConnection urlConnection = null; BufferedReader reader =
			 * null;
			 */

			// Will contain the raw JSON response as a string.
			try {
				// To get ip address of working network
				/*
				 * URL url = new URL("http://wtfismyip.com/text");
				 * 
				 * // Create the request to open the connection urlConnection =
				 * (HttpURLConnection) url.openConnection();
				 * urlConnection.setRequestMethod("GET");
				 * urlConnection.connect();
				 * 
				 * // Read the input stream into a String InputStream
				 * inputStream = urlConnection.getInputStream(); StringBuffer
				 * buffer = new StringBuffer(); if (inputStream == null) { //
				 * Nothing to do. return null; } reader = new BufferedReader(new
				 * InputStreamReader(inputStream));
				 * 
				 * String line; while ((line = reader.readLine()) != null) {
				 * buffer.append(line); }
				 * 
				 * if (buffer.length() == 0) { // Stream was empty. No point in
				 * parsing. return null; }
				 */
				responseJSON = "";

				JSONObject jsonDelivery = new JSONObject();

				// to get delivery from database
				dba.open();
				ArrayList<HashMap<String, String>> insmast = dba
						.getUnSyncDelivery();
				dba.close();
				if (insmast != null && insmast.size() > 0) {
					JSONArray array = new JSONArray();
					// To make json string to post delivery
					for (HashMap<String, String> insp : insmast) {
						JSONObject jsonins = new JSONObject();

						jsonins.put("UniqueId", insp.get("UniqueId"));
						jsonins.put("CentreId", insp.get("CentreId"));
						jsonins.put("RouteId", insp.get("RouteId"));
						jsonins.put("VehicleId", insp.get("VehicleId"));
						jsonins.put("CustomerId", insp.get("CustomerId"));
						jsonins.put("UserId", insp.get("CreateBy"));
						jsonins.put("DeliveryDate", insp.get("CreateDate"));
						jsonins.put("ipAddress",
								common.getDeviceIPAddress(true));
						jsonins.put("Machine", insp.get("Imei"));
						jsonins.put("DeliverTo", insp.get("DeliverTo"));
						array.put(jsonins);
					}
					jsonDelivery.put("Master", array);

					JSONObject jsonDetails = new JSONObject();
					// To get delivery details from database
					dba.open();
					ArrayList<HashMap<String, String>> insdet = dba
							.getUnSyncDeliveryDetail();
					dba.close();
					if (insdet != null && insdet.size() > 0) {

						// To make json string to post delivery details
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
						jsonDetails.put("Detail", arraydet);
					}
					sendJSon = jsonDelivery + "~" + jsonDetails;
					Log.e("sendJSon", sendJSon);
					// To invoke json web service to create delivery
					responseJSON = common.invokeJSONWS(sendJSon, "json",
							"CreateDelivery", common.url);
				} else {
					return "No delivery pending to be send.~";
				}
				return responseJSON;
			} catch (Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to get response from server.";
			} finally {
				dba.close();
			}

		}

		// After execution of json web service to create delivery
		@Override
		protected void onPostExecute(String result) {

			try {
				// To display message after response from server
				if (!result.contains("ERROR")) {
					if (responseJSON.equalsIgnoreCase("success")) {
						dba.open();
						dba.Update_DeliveryIsSync();
						dba.close();
					}
					if (common.isConnected()) {
						// call method of payment json web service
						AsyncStockReturnTransactionWSCall task = new AsyncStockReturnTransactionWSCall();
						task.execute();
					}
				} else {
					if (result.contains("null"))
						result = "Server not responding.";
					common.showToast("Error: " + result);
				}

			} catch (Exception e) {

			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {

			Dialog.setMessage("Posting Delivery...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// To make web service class to post data of payment
	private class AsyncPaymentWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

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
						// call method of get customer json web service
						AsyncComplaintWSCall task = new AsyncComplaintWSCall();
						task.execute();
					}
				} else {
					if (result.contains("null"))
						result = "Server not responding.";
					common.showAlert(ActivityHomeScreen.this, result, false);
					common.showToast("Error: " + result);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
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

	// Async Task to send all attachments on the Portal
	private class Async_AllAttachments_WSCall extends
	AsyncTask<String, String, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				responseJSON = "";

				JSONObject jsonDocs = new JSONObject();

				dba.open();
				// Code to fetch data from database and store in hash map
				ArrayList<HashMap<String, String>> docDet = dba
						.getAttachmentsForSync();

				if (docDet != null && docDet.size() > 0) {
					JSONArray array = new JSONArray();
					try {
						int totalFilesCount = docDet.size();
						int currentCount = 0;
						// Code to loop through hash map and create JSON
						for (HashMap<String, String> mast : docDet) {
							JSONObject jsonDoc = new JSONObject();

							currentCount++;

							jsonDoc.put("UniqueId", mast.get("UniqueId"));
							jsonDoc.put("UploadFileName",
									mast.get("UploadFileName"));
							File fle = new File(mast.get("ImagePath"));
							String flArray = "";
							// Code to check if file exists and create byte
							// array to be passed to json
							if (fle.exists()
									&& (fle.getAbsolutePath().contains(".jpg")
											|| fle.getAbsolutePath().contains(
													".png")
													|| fle.getAbsolutePath().contains(
															".gif")
															|| fle.getAbsolutePath().contains(
																	".jpeg") || fle
																	.getAbsolutePath().contains(".bmp"))) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inPreferredConfig = Bitmap.Config.ALPHA_8;
								Bitmap bitmap = BitmapFactory.decodeFile(
										fle.getAbsolutePath(), options);
								flArray = getByteArrayFromImage(bitmap);

								jsonDoc.put("FileArray", flArray);

								array.put(jsonDoc);
								jsonDocs.put("Attachment", array);
								String sendJSon = jsonDocs.toString();

								// Code to send json to portal and store
								// response stored in responseJSON
								responseJSON = common
										.invokeJSONWS(sendJSon, "json",
												"InsertAttachments", common.url);
								// Check responseJSON and update attachment
								// status
								if (responseJSON.equals("SUCCESS")) {
									dba.open();
									dba.updateAttachmentStatus(mast
											.get("UniqueId"));
									publishProgress("Attachment(s) Uploaded: "
											+ currentCount + "/"
											+ totalFilesCount);
								}
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block

						return "ERROR: Unable to fetch response from server.";
					}

				}

				return responseJSON;
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Internet is slow";
			} catch (Exception e) {
				// TODO: handle exception

				return "ERROR: Unable to fetch response from server.";
			}

		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Dialog.setMessage(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			Dialog.dismiss();
			try {
				if (!result.contains("ERROR")) {
					File dir = new File(
							Environment.getExternalStorageDirectory() + "/"
									+ "LPDND");
					deleteRecursive(dir);
					dba.open();
					dba.deletePaymentCollection();
					dba.close();

					if (common.isConnected()) {
						String[] params = { "0" };
						AsyncUpdateTransactionsRouteOfficerDetailsWSCall task = new AsyncUpdateTransactionsRouteOfficerDetailsWSCall();
						task.execute(params);
					}

				} else {
					if (result == null || result == "null"
							|| result.equals("ERROR: null"))
						common.showAlert(ActivityHomeScreen.this,
								"Unable to get response from server.", false);
					else
						common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {

				common.showAlert(
						ActivityHomeScreen.this,
						"Synchronizing failed - Upload Attachments: "
								+ e.getMessage(), false);
			}

		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Uploading Attachments (Step 2/2)..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle customer ledger web services call as separate thread
	private class AsyncCustomerLedgerWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCustomerLedger", userId, userRole };
				responseJSON = "";
				// Call method of web service to download customer Ledger from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to download Customer Ledger
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display customer ledger after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("CustomerLedger");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getCustomerId = jsonArray.getJSONObject(i)
								.getString("A");
						String getCompanyId = jsonArray.getJSONObject(i)
								.getString("B");
						Double getBalance = Double.valueOf(jsonArray
								.getJSONObject(i).getString("C"));
						dba.Insert_CustomerLedger(getCustomerId, getCompanyId,
								getBalance);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get route json web service
						AsyncCustomerRateWSCall task = new AsyncCustomerRateWSCall();
						task.execute(result);
					} else {
						common.showAlert(ActivityHomeScreen.this,
								"Unable to connect to Internet !", false);
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Customer Ledger Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display Customer Ledger on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Customer Ledger...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle customer rate web services call as separate thread
	private class AsyncCustomerRateWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCustomerRate", userId, userRole };
				responseJSON = "";
				// Call method of web service to download customer rate from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to download CustomerRate
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display customer rate after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("CustomerRate");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getCustomerId = jsonArray.getJSONObject(i)
								.getString("A");
						String getSKUId = jsonArray.getJSONObject(i).getString(
								"B");
						String getRate = jsonArray.getJSONObject(i).getString(
								"C");
						dba.Insert_CustomerRate(getCustomerId, getSKUId,
								getRate);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get complaint category json web
						// service
						AsyncCompanyRateWSCall task = new AsyncCompanyRateWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Customer Rate  Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display CustomerRate on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Customer Rate ...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle Company rate web services call as separate thread
	private class AsyncCompanyRateWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadCompanyRate", userId, userRole };
				responseJSON = "";
				// Call method of web service to download company rate from
				// server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to download CompanyRate
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display company rate after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("CompanyRate");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getSKUId = jsonArray.getJSONObject(i).getString(
								"A");
						String getRate = jsonArray.getJSONObject(i).getString(
								"B");
						dba.Insert_CompanyRate(getSKUId, getRate);
					}
					dba.close();
					if (common.isConnected()) {
						// call method of get complaint category json web
						// service
						AsyncComplaintCategoryWSCall task = new AsyncComplaintCategoryWSCall();
						task.execute(result);
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Company Rate  Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display CompanyRate on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Company Rate ...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle complaint category web services call as separate thread
	private class AsyncComplaintCategoryWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				String[] name = { "action", "userId", "role" };
				String[] value = { "ReadComplaintCategory", userId, userRole };
				responseJSON = "";
				// Call method of web service to download complaint category
				// from server
				responseJSON = common.CallJsonWS(name, value, "ReadMaster",
						common.url);
				return params[0];
			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				return "ERROR: " + "Unable to fetch response from server.";
			}
		}

		// After execution of web service to download customer
		@Override
		protected void onPostExecute(String result) {
			try {
				if (!result.contains("ERROR")) {
					// To display customer after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					dba.open();
					dba.DeleteMasterData("ComplaintCategory");
					for (int i = 0; i < jsonArray.length(); ++i) {
						String getId = jsonArray.getJSONObject(i)
								.getString("A");
						String getName = jsonArray.getJSONObject(i).getString(
								"B");
						dba.Insert_ComplaintCategory(getId, getName);
					}
					dba.close();
					if (common.isConnected()) {
						if (!userRole.equalsIgnoreCase("Customer")) {
							// call method of get route json web service
							AsyncCustomerMasterWSCall task = new AsyncCustomerMasterWSCall();
							task.execute(result);
						}
						else
						{
							if(customerType.equalsIgnoreCase("Retail Outlet")) {
								AsyncRawMaterialMasterWSCall task = new AsyncRawMaterialMasterWSCall();
								task.execute(result);
							}
						}
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Complaint Category Downloading failed: "
								+ "Unable to get response from server.", false);
			}
			Dialog.dismiss();
		}

		// To display customer on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Complaint Category...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Class to handle product web services call as separate thread
	private class AsyncCashDepositWSCall extends
	AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {

				String[] name = { "companyId", "userId" };
				String[] value = { "0", userId };
				// Call method of web service to download product masters from
				// server
				responseJSON = "";
				responseJSON1 = "";
				responseJSON1 = common.CallJsonWS(name, value,
						"GetCashDepositCreateDataForHeader", common.url);
				responseJSON = common.CallJsonWS(name, value,
						"GetCashDepositCreateDataForDetails", common.url);
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
					dba.open();
					dba.DeleteMasterData("CashDepositHeader");
					dba.DeleteMasterData("CashDepositDetail");
					dba.DeleteMasterData("CashDepositTransaction");
					// To display message after response from server
					JSONArray jsonArray = new JSONArray(responseJSON);
					JSONArray jsonArray1 = new JSONArray(responseJSON1);
					if (jsonArray1.length() > 0) {
						// inserting data into cash deposit header data
						for (int i = 0; i < jsonArray1.length(); ++i) {
							String compId = jsonArray1.getJSONObject(i)
									.getString("CompanyId");
							String compName = jsonArray1.getJSONObject(i)
									.getString("CompanyName");
							String prevBal = jsonArray1.getJSONObject(i)
									.getString("PreviousBalance");
							String coln = jsonArray1.getJSONObject(i)
									.getString("CollectionAmount");
							String total = jsonArray1.getJSONObject(i)
									.getString("TotalAmount");
							String online = jsonArray1.getJSONObject(i)
									.getString("OnlineAmount");
							dba.Insert_CashDepositHeader(compId, compName,
									prevBal, coln, total, online);
						}
						// inserting data into cash deposit detail data
						for (int i = 0; i < jsonArray.length(); ++i) {
							String companyId = jsonArray.getJSONObject(i)
									.getString("CompanyId");
							String pcDetailId = jsonArray.getJSONObject(i)
									.getString("Id");
							String custName = jsonArray.getJSONObject(i)
									.getString("CustomerName");
							String pDate = jsonArray.getJSONObject(i)
									.getString("PaymentDate");
							String cheque = jsonArray.getJSONObject(i)
									.getString("Cheque");
							String amount = jsonArray.getJSONObject(i)
									.getString("Amount");
							dba.Insert_CashDepositDetails(companyId,
									pcDetailId, custName, pDate, cheque, amount);
						}
						dba.close();
						intent = new Intent(context, ActivityCashDeposit.class);
						startActivity(intent);
						finish();
					} else {
						common.showToast("There is no data for cash deposit!");
					}

				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				common.showAlert(ActivityHomeScreen.this,
						"Cash Deposit Downloading failed: " + e.toString(),
						false);
			}
			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Downloading Cash Deposit Data..");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}

	// Method to delete files from Directory
	void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteRecursive(child);

		fileOrDirectory.delete();
	}

	// Method to compress, create and return byte array for document
	private String getByteArrayFromImage(Bitmap bitmap)
			throws FileNotFoundException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 80, bos);
		byte[] data = bos.toByteArray();
		String file = Base64.encodeToString(data, Base64.DEFAULT);

		return file;
	}

	// Class to handle complaint web service call as separate thread
	private class AsyncComplaintWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try {
				dba.open();
				JSONObject jsonComp = new JSONObject();
				ArrayList<HashMap<String, String>> cfmast = dba.getComplaint();
				if (cfmast != null && cfmast.size() > 0) {
					JSONArray array = new JSONArray();
					try {
						for (HashMap<String, String> cfd : cfmast) {
							JSONObject jsonsret = new JSONObject();
							newId = cfd.get("Id");
							jsonsret.put("CustomerId", cfd.get("CustomerId"));
							jsonsret.put("ComplaintDate",
									cfd.get("ComplaintDate"));
							jsonsret.put("ComplaintType",
									cfd.get("ComplaintType"));
							jsonsret.put("CategoryId",
									cfd.get("ComplaintCategoryId"));
							jsonsret.put("Rating", cfd.get("FeedbackRating"));
							jsonsret.put("Remarks", cfd.get("CustomerRemark"));
							jsonsret.put("UniqueId", cfd.get("DeviceUniqueId"));
							jsonsret.put("ipAddress",
									common.getDeviceIPAddress(true));
							jsonsret.put("Machine", common.getIMEI());
							array.put(jsonsret);
						}
						jsonComp.put("Comp", array);

						dba.close();

					} catch (JSONException e) {
						// TODO Auto-generated catch block

						return "ERROR: " + e.getMessage();
					} finally {
						dba.close();
					}

					sendJSon = jsonComp.toString();

					responseJSON = common.invokeJSONWS(sendJSon, "json",
							"InsertComplaintDetails", common.url);
				} else {
					return "No Query are pending to be Send to server!";
				}
				return "";
			} catch (Exception e) {
				// TODO: handle exception

				return "ERROR: " + "Unable to fetch response from server.";
			} finally {
				dba.close();
			}

		}

		@Override
		protected void onPostExecute(String result) {

			try {

				if (!result.contains("ERROR")) {
					dba.open();
					dba.DeleteComplaint();
					dba.close();
					if (userRole.equalsIgnoreCase("Customer")) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								context);
						// set title
						alertDialogBuilder.setTitle("Sync Successful");
						// set dialog message
						alertDialogBuilder
						.setMessage(
								"Transaction Synchronization completed successfully. It is recommended to synchronize master data. Do you want to continue?")
								.setCancelable(false)
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int id) {

										if (common.isConnected()) {
											String[] params = { "0" };
											AsyncComplaintCategoryWSCall task = new AsyncComplaintCategoryWSCall();
											task.execute(params);
										}
									}
								})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int id) {
										// if this button is clicked,
										// just close
										dialog.cancel();
									}
								});
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
						// show it
						alertDialog.show();
					} else {
						if (common.isConnected()) {
							// call method of attachment json web service
							Async_AllAttachments_WSCall task = new Async_AllAttachments_WSCall();
							task.execute();
						}
					}
				} else {
					if (result.contains("null") || result == "")
						result = "Server not responding. Please try again later.";
					common.showAlert(ActivityHomeScreen.this, result, false);
				}
			} catch (Exception e) {

				common.showAlert(ActivityHomeScreen.this,
						"Synchronizing failed: "
								+ "Unable to get response from server.", false);
			}

			Dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			Dialog.setMessage("Uploading Query Data..");
			Dialog.setCancelable(false);
			Dialog.show();

		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}

	}


	//<editor-fold desc="Code to Validate User">
	private class AsyncValidatePasswordWSCall extends
	AsyncTask<String, Void, String> {
		String source = "";
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try { // if this button is clicked, close

				source = params[0];
				dba.openR();
				HashMap<String, String> user = session.getLoginUserDetails();
				// Creation of JSON string for posting validating data
				JSONObject json = new JSONObject();
				json.put("username", user.get(UserSessionManager.KEY_CODE));
				json.put("password", user.get(UserSessionManager.KEY_PWD));
				json.put("imei", imei);
				json.put("version", dba.getVersion());
				String JSONStr = json.toString();

				// Store response fetched from server in responseJSON variable
				responseJSON = common.invokeJSONWS(JSONStr, "json",
						"ValidatePassword", common.url);

			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				// e.printStackTrace();
				return "ERROR: Unable to fetch response from server.";
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				// Check if result contains error
				if (!result.contains("ERROR")) {
					String passExpired = responseJSON.split("~")[0];
					String passServer = responseJSON.split("~")[1];
					String membershipError = responseJSON.split("~")[2];
					// Check if password is expire and open change password
					// intent
					if (passExpired.toLowerCase(Locale.US).equals("yes")) {
						Intent intent = new Intent(context,
								ActivityChangePassword.class);
						intent.putExtra("fromwhere", "login");
						startActivity(intent);
						finish();
					}
					// Code to check other validations
					else if (passServer.toLowerCase(Locale.US).equals("no")) {
						String resp = "";

						if (membershipError.toLowerCase(Locale.US).contains(
								"NO_USER".toLowerCase(Locale.US))) {
							resp = "There is no user in the system";
						} else if (membershipError.toLowerCase(Locale.US)
								.contains("BARRED".toLowerCase(Locale.US))) {
							resp = "Your account has been barred by the Administrator.";
						} else if (membershipError.toLowerCase(Locale.US)
								.contains("LOCKED".toLowerCase(Locale.US))) {
							resp = "Your account has been locked out because "
									+ "you have exceeded the maximum number of incorrect login attempts. "
									+ "Please contact the System Admin to "
									+ "unblock your account.";
						} else if (membershipError.toLowerCase(Locale.US)
								.contains("LOGINFAILED".toLowerCase(Locale.US))) {
							resp = "Invalid password. "
									+ "Password is case-sensitive. "
									+ "Access to the system will be disabled after "
									+ responseJSON.split("~")[3] + " "
									+ "consecutive wrong attempts.\n"
									+ "Number of Attempts remaining: "
									+ responseJSON.split("~")[4];
						} else {
							resp = "Password mismatched. Enter latest password!";
						}

						showChangePassWindow(source, resp);
					}

					// Code to check source of request
					else if (source.equals("transactions")) {
						// If version does not match logout user
						if (responseJSON.contains("NOVERSION")) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									context);
							builder.setMessage(
									"Application is running an older version. Please install latest version.!")
									.setCancelable(false)
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													// Code to call async method
													// for posting transactions
													if (common.isConnected()) {
														if (userRole.equalsIgnoreCase("Customer")) {
															// call method of
															// get customer json
															// web service
															AsyncComplaintWSCall task = new AsyncComplaintWSCall();
															task.execute();
														} else {
															// to get delivery
															// from database
															AsyncDeliveryWSCall task = new AsyncDeliveryWSCall();
															task.execute();
														}
													}
												}
											});
							AlertDialog alert = builder.create();
							alert.show();

						} else {
							if (common.isConnected()) {
								if (userRole.equalsIgnoreCase("Customer")) {
									// call method of get customer json web
									// service
									AsyncComplaintWSCall task = new AsyncComplaintWSCall();
									task.execute();
								} else {
									// to get delivery from database
									AsyncDeliveryWSCall task = new AsyncDeliveryWSCall();
									task.execute();
								}
							}
						}

					} else {
						if (responseJSON.contains("NOVERSION")) {
							// Calling async method for master synchronization
							AlertDialog.Builder builder = new AlertDialog.Builder(
									context);
							builder.setMessage(
									"Application is running an older version. Please install latest version.!")
									.setCancelable(false)
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													if (dba.IslogoutAllowed()) {
														AsyncLogOutWSCall task = new AsyncLogOutWSCall();
														task.execute();
													} else
														common.showAlert(
																ActivityHomeScreen.this,
																"There are transactions(s) pending to be sync with the server. Kindly Sync the pending transaction(s).",
																false);
												}
											});
							AlertDialog alert = builder.create();
							alert.show();
						} else {
							String[] params = { "1" };
							// To download customer master in 'Route Officer'
							// role
							if (userRole.contains("Route Officer")) {
								dba.openR();
								if (dba.IsSyncRequiredForRouteOfficer()) {
									AsyncUpdateMastersRouteOfficerDetailsWSCall task = new AsyncUpdateMastersRouteOfficerDetailsWSCall();
									task.execute(params);
									// call method of get customer json web
									// service
									// commented
									/*
									 * AsyncRouteWSCall task = new
									 * AsyncRouteWSCall ();
									 * task.execute(params);
									 */
								}
							} 
							else if(userRole.contains("Accountant") || userRole.contains("Collection Officer")) {
								dba.openR();
								if (dba.IsSyncRequiredForAccountant()) {
									String[] myTaskParams = { "masters" };
									// call method of get validate password json web service
									if(common.isConnected())
									{
										AsyncBankWSCall task = new AsyncBankWSCall();
										task.execute(result);
									}
								}
							}
							else {
								dba.openR();
								if (dba.IsSyncRequiredForCustomer()) {
									AsyncComplaintCategoryWSCall task = new AsyncComplaintCategoryWSCall();
									task.execute(params);
								}
							}
						}
					}
				} else {
					common.showAlert(ActivityHomeScreen.this,
							"Unable to fetch response from server.", false);
				}

			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Validating credentials failed: " + e.toString(), false);
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
	//</editor-fold>


	//<editor-fold desc="Async for Posting Primary Receipt for Retail Outlet">
	private class AsyncCustomerPrimaryReceiptWSCall extends AsyncTask<String, Void, String> {
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {

			// Will contain the raw JSON response as a string.
			try {

				responseJSON = "";

				JSONObject jsonPayment = new JSONObject();
				dba.open();
				// to get Primary Receipt from database
				ArrayList<HashMap<String, String>> insmast = dba
						.getUnSyncPrimaryReceipt();
				dba.close();
				if (insmast != null && insmast.size() > 0) {
					JSONArray array = new JSONArray();
					// To make json string to post payment
					for (HashMap<String, String> insp : insmast) {
						JSONObject jsonins = new JSONObject();
						jsonins.put("UniqueId", insp.get("UniqueId"));
						jsonins.put("CustomerId", insp.get("CustomerId"));
						jsonins.put("MaterialId", insp.get("MaterialId"));
						jsonins.put("SkuId", insp.get("SKUId"));
						jsonins.put("Quantity", insp.get("Quantity"));
						jsonins.put("Amount", insp.get("Amount"));
						jsonins.put("TransactionDate", insp.get("CreateDate"));
						jsonins.put("CreateBy", userId);
						jsonins.put("ipAddress",
								common.getDeviceIPAddress(true));
						jsonins.put("Machine", common.getIMEI());
						array.put(jsonins);
					}
					jsonPayment.put("PrimaryReceipt", array);

					sendJSon = jsonPayment.toString();

					// To invoke json web service to create payment
					responseJSON = common.invokeJSONWS(sendJSon, "json",
							"InsertPimaryReceipt", common.url);
				} else {
					return "No primary receipt pending to be send.";
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
						dba.Update_PrimaryReceiptIsSync();
						dba.close();
					}
					if (common.isConnected()) {
						// call method of get customer json web service
						AsyncComplaintWSCall task = new AsyncComplaintWSCall();
						task.execute();
					}
				} else {
					if (result.contains("null"))
						result = "Server not responding.";
					common.showAlert(ActivityHomeScreen.this, result, false);
					common.showToast("Error: " + result);
				}
			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Unable to fetch response from server.", false);
			}

			Dialog.dismiss();
		}

		// To display message on screen within process
		@Override
		protected void onPreExecute() {

			Dialog.setMessage("Posting Primary Receipt...");
			Dialog.setCancelable(false);
			Dialog.show();
		}
	}
	//</editor-fold>

	//<editor-fold desc="Code to Validate Retail Outlet Customer">
	private class AsyncCustomerValidatePasswordWSCall extends
			AsyncTask<String, Void, String> {
		String source = "";
		private ProgressDialog Dialog = new ProgressDialog(
				ActivityHomeScreen.this);

		@Override
		protected String doInBackground(String... params) {
			try { // if this button is clicked, close

				source = params[0];
				dba.openR();
				HashMap<String, String> user = session.getLoginUserDetails();
				// Creation of JSON string for posting validating data
				JSONObject json = new JSONObject();
				json.put("username", user.get(UserSessionManager.KEY_CODE));
				json.put("password", user.get(UserSessionManager.KEY_PWD));
				json.put("imei", imei);
				json.put("version", dba.getVersion());
				String JSONStr = json.toString();

				// Store response fetched from server in responseJSON variable
				responseJSON = common.invokeJSONWS(JSONStr, "json",
						"ValidatePassword", common.url);

			} catch (SocketTimeoutException e) {
				return "ERROR: TimeOut Exception. Either Server is busy or Internet is slow";
			} catch (final Exception e) {
				// TODO: handle exception
				// e.printStackTrace();
				return "ERROR: Unable to fetch response from server.";
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				// Check if result contains error
				if (!result.contains("ERROR")) {
					String passExpired = responseJSON.split("~")[0];
					String passServer = responseJSON.split("~")[1];
					String membershipError = responseJSON.split("~")[2];
					// Check if password is expire and open change password
					// intent
					if (passExpired.toLowerCase(Locale.US).equals("yes")) {
						Intent intent = new Intent(context,
								ActivityChangePassword.class);
						intent.putExtra("fromwhere", "login");
						startActivity(intent);
						finish();
					}
					// Code to check other validations
					else if (passServer.toLowerCase(Locale.US).equals("no")) {
						String resp = "";

						if (membershipError.toLowerCase(Locale.US).contains(
								"NO_USER".toLowerCase(Locale.US))) {
							resp = "There is no user in the system";
						} else if (membershipError.toLowerCase(Locale.US)
								.contains("BARRED".toLowerCase(Locale.US))) {
							resp = "Your account has been barred by the Administrator.";
						} else if (membershipError.toLowerCase(Locale.US)
								.contains("LOCKED".toLowerCase(Locale.US))) {
							resp = "Your account has been locked out because "
									+ "you have exceeded the maximum number of incorrect login attempts. "
									+ "Please contact the System Admin to "
									+ "unblock your account.";
						} else if (membershipError.toLowerCase(Locale.US)
								.contains("LOGINFAILED".toLowerCase(Locale.US))) {
							resp = "Invalid password. "
									+ "Password is case-sensitive. "
									+ "Access to the system will be disabled after "
									+ responseJSON.split("~")[3] + " "
									+ "consecutive wrong attempts.\n"
									+ "Number of Attempts remaining: "
									+ responseJSON.split("~")[4];
						} else {
							resp = "Password mismatched. Enter latest password!";
						}

						showChangePassWindow(source, resp);
					}

					// Code to check source of request
					else if (source.equals("transactions")) {
						// If version does not match logout user
						if (responseJSON.contains("NOVERSION")) {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									context);
							builder.setMessage(
									"Application is running an older version. Please install latest version.!")
									.setCancelable(false)
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													// Code to call async method
													// for posting transactions
													if (common.isConnected()) {
														AsyncCustomerPrimaryReceiptWSCall task = new AsyncCustomerPrimaryReceiptWSCall();
														task.execute();
													}
												}
											});
							AlertDialog alert = builder.create();
							alert.show();

						} else {
							if (common.isConnected()) {
								AsyncCustomerPrimaryReceiptWSCall task = new AsyncCustomerPrimaryReceiptWSCall();
								task.execute();
							}
						}

					} else {
						if (responseJSON.contains("NOVERSION")) {
							// Calling async method for master synchronization
							AlertDialog.Builder builder = new AlertDialog.Builder(
									context);
							builder.setMessage(
									"Application is running an older version. Please install latest version.!")
									.setCancelable(false)
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													if (dba.IslogoutAllowed()) {
														AsyncLogOutWSCall task = new AsyncLogOutWSCall();
														task.execute();
													} else
														common.showAlert(
																ActivityHomeScreen.this,
																"There are transactions(s) pending to be sync with the server. Kindly Sync the pending transaction(s).",
																false);
												}
											});
							AlertDialog alert = builder.create();
							alert.show();
						} else {
							String[] params = { "1" };

							dba.openR();
							if (dba.IsSyncRequiredForCustomer()) {
								AsyncComplaintCategoryWSCall task = new AsyncComplaintCategoryWSCall();
								task.execute(params);
							}
						}
					}
				} else {
					common.showAlert(ActivityHomeScreen.this,
							"Unable to fetch response from server.", false);
				}

			} catch (Exception e) {
				common.showAlert(ActivityHomeScreen.this,
						"Validating credentials failed: " + e.toString(), false);
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
	//</editor-fold>

	// Method to display change password dialog
	private void showChangePassWindow(final String source, final String resp) {
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.dialog_password_prompt, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setView(promptsView);
		// Code to find controls in dialog
		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.etPassword);

		final CheckBox ckShowPass = (CheckBox) promptsView
				.findViewById(R.id.ckShowPass);

		final TextView tvMsg = (TextView) promptsView.findViewById(R.id.tvMsg);

		tvMsg.setText(resp);

		ckShowPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				int start, end;

				if (!isChecked) {
					start = userInput.getSelectionStart();
					end = userInput.getSelectionEnd();
					userInput
					.setTransformationMethod(new PasswordTransformationMethod());
					;
					userInput.setSelection(start, end);
				} else {
					start = userInput.getSelectionStart();
					end = userInput.getSelectionEnd();
					userInput.setTransformationMethod(null);
					userInput.setSelection(start, end);
				}

			}
		});

		// set dialog message
		alertDialogBuilder
		.setCancelable(false)
		.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String password = userInput.getText()
						.toString().trim();
				if (password.length() > 0) {
					// Code to update password in session and
					// call validate Async Method
					session.updatePassword(password);

					String[] myTaskParams = { source };
					AsyncValidatePasswordWSCall task = new AsyncValidatePasswordWSCall();
					task.execute(myTaskParams);
				} else {
					// Display message if password is not
					// enetered
					common.showToast("Password is mandatory");
				}
			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	// Code on Activity Resume
	@Override
	protected void onResume() {
		super.onResume();
		String lang = session.getDefaultLang();
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
	}

	// When press back button go to home screen
	@Override
	public void onBackPressed() {
		common.BackPressed(this);
	}
}
