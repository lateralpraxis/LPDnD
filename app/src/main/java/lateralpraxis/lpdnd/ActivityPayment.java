package lateralpraxis.lpdnd;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import lateralpraxis.lpdnd.types.CustomType;
import lateralpraxis.lpdnd.types.CustomerPayment;

public class ActivityPayment  extends Activity{
	/*Start of code to declare controls*/
	private Spinner spCustomer,spCompany,spBank;
	private TextView tvBalanceData,tvCustName, tvCustId,tvAttach,tvEmpty,tvTotalAmt;
	private Button btnGo,btnCreate,btnUpload,btnSubmit;
	private EditText etAmount,etCheque;
	private LinearLayout llCustSelection,llPayment,llBank,llCheque,llAttachment,llNavBtn,llBottom,llBottomNew;
	private TableLayout tableLayout1,tableLayoutTotal;
	//private RelativeLayout relativeLayoutSubmit;
	private RadioGroup RadioMode;
	private ListView lvPaymentInfoList;
	/*End of code to declare controls*/

	/*Start of code to declare class*/
	DatabaseAdapter db;
	Common common;
	CustomAdapter Cadapter;
	private UserSessionManager session;
	/*End of code to declare class*/

	/*Start of code to declare variables*/
	private String balance;
	private String mode="Cash";
	private ArrayList<HashMap<String, String>> PaymentDetails;
	private int lsize=0;
	private final Context mContext = this;
	private String deliveryUniqueId="0", customerId="0", customer="0", from="";
	protected static final int CAMERA_REQUEST = 0;
	protected static final int GALLERY_REQUEST = 1;
	Bitmap bitmap;
	Uri uri;
	Intent picIntent = null;
	private String level1Dir, level2Dir, fullPath,
	photoPath,uuidImg;
	private int fileCount = 0;
	private static final int PICK_Camera_IMAGE = 0;
	File destination, file;
	private ImageLoadingUtils utils;
	/*End of variable declaration for uploading image*/
	//On create method similar to page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Code to set layout
		setContentView(R.layout.activity_payment);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		//Code to create instance of classes
		db = new DatabaseAdapter(this);
		common = new Common(this);
		session = new UserSessionManager(getApplicationContext());
		utils = new ImageLoadingUtils(this);
		/*		*/
		//Code to delete all data from temporary table
		db.open();
		db.DeleteTempPaymentDetails("0");
		db.close();

		//Code to find layouts		
		llCustSelection= (LinearLayout) findViewById(R.id.llCustSelection);
		llPayment= (LinearLayout) findViewById(R.id.llPayment);
		llBank= (LinearLayout) findViewById(R.id.llBank);
		llCheque= (LinearLayout) findViewById(R.id.llCheque);
		llAttachment= (LinearLayout) findViewById(R.id.llAttachment);
		llNavBtn= (LinearLayout) findViewById(R.id.llNavBtn);
		llBottom= (LinearLayout) findViewById(R.id.llBottom);
		llBottomNew= (LinearLayout) findViewById(R.id.llBottomNew);
		tableLayout1=(TableLayout) findViewById(R.id.tableLayout1);
		tableLayoutTotal=(TableLayout) findViewById(R.id.tableLayoutTotal);
		//relativeLayoutSubmit = (RelativeLayout)findViewById(R.id.relativeLayoutSubmit);

		//Code to find controls inside layouts
		etAmount = (EditText) findViewById(R.id.etAmount);
		etCheque = (EditText) findViewById(R.id.etCheque);
		tvBalanceData= (TextView) findViewById(R.id.tvBalanceData);
		tvCustName= (TextView) findViewById(R.id.tvCustName);
		tvCustId= (TextView) findViewById(R.id.tvCustId);
		tvAttach= (TextView) findViewById(R.id.tvAttach);
		tvEmpty= (TextView) findViewById(R.id.tvEmpty);
		tvTotalAmt= (TextView) findViewById(R.id.tvTotalAmt);
		spCustomer= (Spinner) findViewById(R.id.spCustomer);
		spCompany= (Spinner) findViewById(R.id.spCompany);
		spBank= (Spinner) findViewById(R.id.spBank);
		btnGo= (Button) findViewById(R.id.btnGo);
		btnCreate= (Button) findViewById(R.id.btnCreate);
		btnUpload= (Button) findViewById(R.id.btnUpload);
		btnSubmit= (Button) findViewById(R.id.btnSubmit);
		RadioMode = (RadioGroup) findViewById(R.id.RadioMode);
		lvPaymentInfoList =(ListView)findViewById(R.id.lvPaymentInfoList);
		//Code to set how many decimal places are allowed
		etAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(8,2)});

		//Hash Map for storing data
		PaymentDetails = new ArrayList<HashMap<String, String>>();

		//code to show / hide layouts
		llCustSelection.setVisibility(View.VISIBLE);
		llPayment.setVisibility(View.GONE);
		//Code to bind company in spinner
		spCompany.setAdapter(DataAdapter("company",""));
		//Code to bind customer in spinner
		spCustomer.setAdapter(DataAdapter("customer",""));
		//Code to bind bank in spinner
		spBank.setAdapter(DataAdapter("bank",""));

		//To extract id from bundle to show details
		Bundle extras = this.getIntent().getExtras();
		if (extras != null)
		{
			customerId = extras.getString("customerId");
			customer = extras.getString("customer");
			deliveryUniqueId = extras.getString("deliveryUniqueId");
			from = extras.getString("from");
		}
		if(!deliveryUniqueId.equalsIgnoreCase("0"))
		{
			tvCustName.setText(customer);
			tvCustId.setText(customerId);
			llCustSelection.setVisibility(View.GONE);
			llPayment.setVisibility(View.VISIBLE);
		}
		//Code on button go click event show / hide layout and freeze customer
		btnGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(spCustomer.getSelectedItemPosition()==0)
				{
					common.showToast("Please select customer.");
				}
				else
				{
					tvCustName.setText(String.valueOf(((CustomType)spCustomer.getSelectedItem()).getName()));
					tvCustId.setText(String.valueOf(((CustomType)spCustomer.getSelectedItem()).getId()));
					//code to show / hide layouts
					llCustSelection.setVisibility(View.GONE);
					llPayment.setVisibility(View.VISIBLE);
				}
			}
		});
		//Code on button create click event to add payment data in temporary table
		btnCreate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Code for validating mandatory fields
				db.open();
				//Variable declared for processing image 
				String outdir="";
				String imagePath="";
				String selectedPhotoPath="";
				if(spCompany.getSelectedItemPosition()==0)
				{
					common.showToast("Please select company.");
				}
				else if(mode.equals("Cheque") && spBank.getSelectedItemPosition()==0)
				{
					common.showToast("Please select bank.");
				}
				else if(mode.equals("Cheque") && etCheque.getText().toString().trim().equals(""))
				{
					common.showToast("Please enter cheque number.");
				}
				/*else if(mode.equals("Cheque") && tvAttach.getText().toString().trim().equals(""))
				{
					common.showToast("Please select attachment.");
				}*/
				else if(mode.equals("Cheque") && db.getChequeNumberExistCount(tvCustId.getText().toString(),etCheque.getText().toString().trim())>0)
				{
					common.showToast("Cheque number already added.");
				}
				else if(etAmount.getText().toString().trim().equals(""))
				{
					common.showToast("Please enter amount.");
				}
				else if (Double.valueOf(String.valueOf(etAmount.getText()).trim())<.01)
				{
					common.showToast("Amount cannot be less than .01");
				}
				else
				{
					String newuuidImg="";
					if(tvAttach.getText().toString().trim().length()>0)
					{
						//Code to save image in path and insert data in temporary table
						if(photoPath!=null || photoPath!="" )
						{
							newuuidImg = UUID.randomUUID().toString();
							//Setting directory structure
							level1Dir = "LPDND";
							level2Dir = level1Dir+"/"+newuuidImg;
							//Code to set full path
							fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
							//Code to set absolute path
							outdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+level2Dir;

							//Code to set photo path
							selectedPhotoPath = photoPath.substring(photoPath.lastIndexOf("/")+1);
						}
						//Code to check if directory exists else create directory
						if(createDirectory(level1Dir) && createDirectory(level2Dir) )
						{
							imagePath = copyFile(photoPath, outdir);
						}
					}
					//Code to insert data in temporary table
					db.open();
					db.insertCustomerPaymentTemp(((CustomType)spCompany.getSelectedItem()).getId(), ((CustomType)spBank.getSelectedItem()).getId(), etCheque.getText().toString().trim(), etAmount.getText().toString().trim(),imagePath, selectedPhotoPath,newuuidImg,"");
					db.close();
					//Building directory structure for deleting
					level1Dir = "LPDND";
					level2Dir = level1Dir+"/"+uuidImg;
					File dir = new File(level2Dir);
					//Method for deleting file and directory
					DeleteRecursive(dir);
					//Code to clear all controls after adding data in temporary table
					spBank.setSelection(0);
					spCompany.setSelection(0);
					etCheque.setText("");
					etAmount.setText("");
					tvAttach.setText("");
					BindPayment();
					common.showToast("Payment details added successfully.");
				}

			}
		});
		//Click of Upload button to attach cheque photo 
		btnUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (tvAttach.getText().toString().trim().length() > 0) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							mContext);
					builder1.setTitle("Attach Cheque");
					builder1.setMessage("Are you sure want to remove existing cheque picture and upload new cheque picture?");
					builder1.setCancelable(true);
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							level1Dir = "LPDND" + "/" + uuidImg;
							File dir = new File(level1Dir);
							DeleteRecursive(dir);
							tvAttach.setText("");
							startDialog();

						}
					}).setNegativeButton("No",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int id) {
							// if this button is clicked, just close
							dialog.cancel();
						}
					});
					AlertDialog alertnew = builder1.create();
					alertnew.show();
				}
				else
					startDialog();
			}
		});

		//Click of Submit button to attach cheque photo 
		btnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final HashMap<String, String> user = session.getLoginUserDetails();
				AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
				builder1.setTitle("Submit Payment");
				builder1.setMessage("Are you sure want to save payment details?");
				builder1.setCancelable(true);
				builder1.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int id) {
						db.open();
						db.insertCustomerPayment(UUID.randomUUID().toString(), tvCustId.getText().toString(), user.get(UserSessionManager.KEY_ID), deliveryUniqueId);
						db.close();
						common.showToast("Payment details saved successfully.");
						Intent homeScreenIntent;
						if(from.equalsIgnoreCase("delivery"))
						{
							SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
							//To bind default delivery date
							final Calendar c = Calendar.getInstance();
							c.add(Calendar.DATE,0);
							String deliveryDate = dateFormatter.format(c.getTime());
							db.open();
							db.Insert_DemandDate(deliveryDate);
							db.close();
							
							homeScreenIntent = new Intent(ActivityPayment.this, ActivityDeliveryViewDetail.class);
							homeScreenIntent.putExtra("Id", tvCustId.getText().toString()); 
							homeScreenIntent.putExtra("Header",tvCustName.getText().toString());
						}
						else
							homeScreenIntent = new Intent(ActivityPayment.this, ActivityPaymentView.class);
						homeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(homeScreenIntent);
						finish();
					}
				}).setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int id) {
						// if this button is clicked, just close
						dialog.cancel();
					}
				});
				AlertDialog alertnew = builder1.create();
				alertnew.show();

			}
		});
		//Code on spinner item change to display balance
		spCompany.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				tvBalanceData.setText("");
				db.open();
				balance =db.getBalanceByCustomerandCompany(tvCustId.getText().toString(), String.valueOf(((CustomType)spCompany.getSelectedItem()).getId()));
				tvBalanceData.setText(common.stringToTwoDecimal(balance));
				db.close();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				tvBalanceData.setText("");
			}

		});
		//Code on radio group selection change event for show / hide cheque details
		RadioMode.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				View radioButton = RadioMode.findViewById(checkedId);
				int index = RadioMode.indexOfChild(radioButton);

				// Add logic here
				spBank.setSelection(0);
				etCheque.setText("");
				switch (index) {
				case 0: // first button - Cash
					llBank.setVisibility(View.GONE);
					llCheque.setVisibility(View.GONE);
					llAttachment.setVisibility(View.GONE);
					llBottom.setVisibility(View.VISIBLE);
					llBottomNew.setVisibility(View.VISIBLE);
					mode="Cash";					
					break;
				case 1: // second button - cheque
					spBank.setSelection(0);
					etCheque.setText("");
					tvAttach.setText("");
					llBank.setVisibility(View.VISIBLE);
					llCheque.setVisibility(View.VISIBLE);
					llAttachment.setVisibility(View.VISIBLE);
					llBottom.setVisibility(View.VISIBLE);
					llBottomNew.setVisibility(View.VISIBLE);
					mode="Cheque";
					break;
				}
			}
		});
		//Code to set scroll on on touch event of list view
		lvPaymentInfoList.setOnTouchListener(new ListView.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;
				case MotionEvent.ACTION_UP:
					// Allow ScrollView to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}
				// Handle ListView touch events.
				v.onTouchEvent(event);
				return true;
			}
		});
	}

	//Class for Binding Data in ListView
	public static class ViewHolder {
		//Control Declaration
		TextView tvPaymentId, tvCompany,tvAmount,tvCheque,tvUniqueId; 

		Button btnDelete;
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
		public CustomAdapter(Context context,ArrayList<HashMap<String, String>> lvPaymentInfoList) {
			this.paymentContext = context;
			mInflater = LayoutInflater.from(paymentContext);
			PaymentDetails = lvPaymentInfoList;
		}

		//Event is similar to row data bound event
		@SuppressLint("InflateParams")
		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {			


			final ViewHolder holder;
			if (arg1 == null) 
			{
				//Code to set layout inside list view
				arg1 = mInflater.inflate(R.layout.list_payment_info, null); 
				holder = new ViewHolder();
				//Code to find controls inside listview
				holder.tvPaymentId = (TextView)arg1.findViewById(R.id.tvPaymentId);
				holder.tvUniqueId = (TextView)arg1.findViewById(R.id.tvUniqueId);
				holder.tvCompany = (TextView)arg1.findViewById(R.id.tvCompany);
				holder.tvAmount = (TextView)arg1.findViewById(R.id.tvAmount);
				holder.tvCheque = (TextView)arg1.findViewById(R.id.tvCheque);
				holder.btnDelete = (Button)arg1.findViewById(R.id.btnDelete);
				arg1.setTag(holder);

			}
			else
			{

				holder = (ViewHolder) arg1.getTag();
			}
			//Code to bind data from hash map in controls
			holder.tvPaymentId.setText(PaymentDetails.get(arg0).get("Id"));
			holder.tvCompany.setText(PaymentDetails.get(arg0).get("CompanyName"));
			holder.tvAmount.setText(PaymentDetails.get(arg0).get("Amount"));
			holder.tvUniqueId.setText(PaymentDetails.get(arg0).get("UniqueId"));
			holder.tvCheque.setText(Html.fromHtml("<font color=#000000> "+PaymentDetails.get(arg0).get("ChequeNumber")+"</font>") );

			//Button delete event for deleting attachment
			holder.btnDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(ActivityPayment.this);
					builder1.setTitle("Delete Payment Details");
					builder1.setMessage("Are you sure you want to delete this payment detail?");
					builder1.setCancelable(true);
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							level1Dir = "LPDND";
							level2Dir =level1Dir+"/"+ holder.tvUniqueId.getText();
							fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
							db.open();
							db.DeleteTempPaymentDetails(holder.tvPaymentId.getText().toString());
							db.close();
							File dirdel = new File(Environment.getExternalStorageDirectory()+level2Dir); 
							if (dirdel.isDirectory()) 
							{
								String[] children = dirdel.list();
								for (int i = 0; i < children.length; i++)
								{
									new File(dirdel, children[i]).delete();
								}
							}
							//code to set directory from which files are to be deleted
							File dir = new File(level2Dir);
							//Method to delete files and directory
							DeleteRecursive(dir);
							common.showToast("Payment details deleted succeessfully.");
							BindPayment();
						}
					})
					.setNegativeButton("No",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							dialog.cancel();
						}
					});
					AlertDialog alertnew = builder1.create();
					alertnew.show();

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
	//Method to bind payment detail data from temporary table
	private void BindPayment()
	{
		/*Start of code to bind data from temporary table*/
		PaymentDetails.clear();
		db.open();		
		List <CustomerPayment> lables = db.getCustomerTempPayment();
		lsize = lables.size();
		if(lsize>0)
		{
			tvEmpty.setVisibility(View.GONE);
			tableLayout1.setVisibility(View.VISIBLE);
			tableLayoutTotal.setVisibility(View.VISIBLE);
			llNavBtn.setVisibility(View.VISIBLE);
			//Looping through hash map and add data to hash map
			for(int i=0;i<lables.size();i++){
				HashMap<String, String> hm = new HashMap<String,String>();
				hm.put("Id", String.valueOf(lables.get(i).getId())); 
				hm.put("CompanyName", String.valueOf(lables.get(i).getCompanyName())); 
				hm.put("Amount", common.stringToTwoDecimal(lables.get(i).getAmount()));  
				hm.put("ChequeNumber", String.valueOf(lables.get(i).getChequeNumber()));
				hm.put("Bank", String.valueOf(lables.get(i).getBankName()));
				hm.put("UniqueId", String.valueOf(lables.get(i).getUniqueId()));
				PaymentDetails.add(hm);
			}
			db.open();
			String strTotal =String.valueOf(common.stringToTwoDecimal(db.getCustomerTempTotalPayment()));
			tvTotalAmt.setText(strTotal);
		}
		else
		{
			//Display no records message
			tvEmpty.setVisibility(View.VISIBLE);
			tableLayout1.setVisibility(View.GONE);
			tableLayoutTotal.setVisibility(View.GONE);
			llNavBtn.setVisibility(View.GONE);
		}
		db.close();
		//Code to set hash map data in custom adapter
		Cadapter = new CustomAdapter(ActivityPayment.this,PaymentDetails);
		if(lsize>0)
			lvPaymentInfoList.setAdapter(Cadapter);	
		lvPaymentInfoList.requestLayout();
		/*End of code to bind data from temporary table*/
	}

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



	//Code for opening dialog for selecting image
	private void startDialog() {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityPayment.this);
		builderSingle.setTitle("Select Image source");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				ActivityPayment.this,
				android.R.layout.select_dialog_singlechoice);
		arrayAdapter.add("Capture Image");
		arrayAdapter.add("Select from Gallery");


		builderSingle.setNegativeButton(
				"Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builderSingle.setAdapter(
				arrayAdapter,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = arrayAdapter.getItem(which);
						//Check if camera option is selected
						if(strName.equals("Capture Image"))
						{
							//Setting directory structure
							uuidImg = UUID.randomUUID().toString();
							level1Dir = "LPDND";
							level2Dir = level1Dir+"/"+uuidImg;
							String imageName =random() + ".jpg";
							fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
							destination = new File(fullPath, imageName);
							//Check if directory exists else create directory
							if(createDirectory(level1Dir) && createDirectory(level2Dir))
							{
								//Code to open camera intent
								Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
								intent.putExtra(MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(destination));
								startActivityForResult(intent, PICK_Camera_IMAGE);

							}
							//Code to set image name
							photoPath =fullPath+imageName;
							tvAttach.setText(imageName);
						}
						else if (strName.equals("Select from Gallery"))
						{
							//Code to open gallery intent
							picIntent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							picIntent.putExtra("return_data",true);
							startActivityForResult(picIntent,GALLERY_REQUEST);
						}
						else 
						{
							common.showToast("No File available for review.");
						}
					}
				});
		builderSingle.show();
	}

	//Code to be executed after action done for attaching
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==0 && resultCode==0 && data ==null)
		{
			//Reset image name and hide reset button
			tvAttach.setText("");
		}
		else if (requestCode==GALLERY_REQUEST){		
			//Gallery request and result code is ok
			if (resultCode==RESULT_OK){
				if (data!=null) {
					uri = data.getData();
					if(uri!=null)
					{
						photoPath=getRealPathFromUri(uri);
						tvAttach.setText(photoPath.substring(photoPath.lastIndexOf("/")+1));
						uuidImg = UUID.randomUUID().toString();
						//Set directory path
						level1Dir = "LPDND";
						level2Dir = level1Dir+"/"+uuidImg;
						fullPath = Environment.getExternalStorageDirectory() + "/"+level2Dir;
						//Code to create file inside directory
						if (createDirectory(level1Dir)
								&& createDirectory(level2Dir)) {
							copyFile(photoPath, fullPath);
							destination = new File(photoPath);
						}
					}
					else
					{

						Toast.makeText(getApplicationContext(), "Cancelled",
								Toast.LENGTH_SHORT).show();

					}
					if(photoPath!="" && photoPath!=null)
					{
						common.showToast("Gallery Image selected at path: "+ photoPath);
					}

				}else {
					Toast.makeText(getApplicationContext(), "Cancelled",
							Toast.LENGTH_SHORT).show();
				}
			}else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Cancelled",
						Toast.LENGTH_SHORT).show();
			}
		}
		else if (requestCode == CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {
				//Camera request and result code is ok
				try {
					FileInputStream in = new FileInputStream(destination);
					photoPath = compressImage(destination.getAbsolutePath());
					//code to fetch selected image path
					in.close();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				catch (IOException e) {

					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
		else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Cancelled",
					Toast.LENGTH_SHORT).show();			
		}
	}

	//Method to get Actual path of image
	private String getRealPathFromUri(Uri tempUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = this.getContentResolver().query(tempUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	//Method to calculate sample size for image
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	//Method to get file count in directory
	public int CountFiles(File[] files) {
		if(files==null || files.length==0)
		{
			return 0;
		}
		else
		{
			for (File file : files) {
				if (file.isDirectory()) {
					CountFiles(file.listFiles());
				} else {
					if(!file.getAbsolutePath().contains(".nomedia"))
						fileCount++;
				}
			}
			return fileCount;
		}
	}

	//Method to compress the image
	public String compressImage(String path) {

		String filePath = path;
		Bitmap scaledBitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;						
		Bitmap bmp = BitmapFactory.decodeFile(filePath,options);

		int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;
		float maxHeight = 816.0f;
		float maxWidth = 612.0f;
		float imgRatio = actualWidth / actualHeight;
		float maxRatio = maxWidth / maxHeight;

		if (actualHeight > maxHeight || actualWidth > maxWidth) {
			if (imgRatio < maxRatio) {
				imgRatio = maxHeight / actualHeight;
				actualWidth = (int) (imgRatio * actualWidth);
				actualHeight = (int) maxHeight;
			} else if (imgRatio > maxRatio) {
				imgRatio = maxWidth / actualWidth;
				actualHeight = (int) (imgRatio * actualHeight);
				actualWidth = (int) maxWidth;
			} else {
				actualHeight = (int) maxHeight;
				actualWidth = (int) maxWidth;     

			}
		}

		options.inSampleSize = utils.calculateInSampleSize(options, actualWidth, actualHeight);
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inTempStorage = new byte[16*1024];

		try{	
			bmp = BitmapFactory.decodeFile(filePath,options);
		}
		catch(OutOfMemoryError exception){
			//exception.printStackTrace();

		}
		try{
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
		}
		catch(OutOfMemoryError exception){
			//exception.printStackTrace();
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float)options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(destination);
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		}

		return destination.getAbsolutePath();

	}

	//Method to generate random number and return the same
	public static String random() {
		Random r = new Random();

		char[] choices = ("abcdefghijklmnopqrstuvwxyz" +
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
				"01234567890").toCharArray();

		StringBuilder salt = new StringBuilder(10);
		for (int i = 0; i<10; ++i)
			salt.append(choices[r.nextInt(choices.length)]);
		return "img_"+salt.toString();
	}

	//Method to delete File Recursively
	public void DeleteRecursive(File fileOrDirectory) {

		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();

	}


	//Method to create new directory
	private boolean createDirectory(String dirName)
	{
		//Code to Create Directory for Inspection (Parent)
		File folder = new File(Environment.getExternalStorageDirectory() + "/"+dirName);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
		}
		if (success) {
			copyNoMediaFile(dirName);
			return true;
		} else {
			return false;
		}
	}

	//Method to create No Media File in directory
	private void copyNoMediaFile(String dirName)
	{
		try {
			// Open your local db as the input stream
			//boolean D= true;
			String storageState = Environment.getExternalStorageState();

			if ( Environment.MEDIA_MOUNTED.equals( storageState ) )
			{
				try
				{
					File noMedia = new File ( Environment
							.getExternalStorageDirectory()
							+ "/"
							+ level2Dir, ".nomedia" );
					if ( noMedia.exists() )
					{


					}

					FileOutputStream noMediaOutStream = new FileOutputStream ( noMedia );
					noMediaOutStream.write ( 0 );
					noMediaOutStream.close ( );
				}
				catch ( Exception e )
				{

				}
			}
			else
			{

			}

		} catch (Exception e) {

		}
	}


	//Copy file from one place to another
	private String copyFile(String inputPath, String outputPath) {

		File f = new File(inputPath);
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(inputPath);        
			out = new FileOutputStream(outputPath+"/"+f.getName());

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			compressImage(outputPath+"/"+f.getName());

			// write the output file (You have now copied the file)
			out.flush();
			out.close();
			out = null;  



		}  catch (FileNotFoundException fnfe1) {
			//Log.e("tag", fnfe1.getMessage());
		}
		catch (Exception e) {
			//Log.e("tag", e.getMessage());
		}
		return outputPath+"/"+f.getName();
	}


	//Code to go to intent on selection of menu item
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			Intent i = new Intent(ActivityPayment.this,ActivityPaymentView.class);
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
		Intent i = new Intent(ActivityPayment.this,ActivityPaymentView.class);
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