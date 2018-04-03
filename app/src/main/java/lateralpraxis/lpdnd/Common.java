package lateralpraxis.lpdnd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Common {
	private final static String namespace = "http://tempuri.org/";
	private final static String soap_action = "http://tempuri.org/";
	static Context c;
	static HashMap<String, String> user;
	private static String responseJSON;
    //public final String domain = "http://122.180.148.98:81"; // 81 Port for QA
    public final String domain = "http://122.180.148.98:82"; // 82 Port for Development
    //public final String domain = "http://122.180.148.98:82";
	//For Demo Server
	//public final String domain = "http://54.148.76.44"; // lateralpraxis.co.in
	//public final String url=domain+"/LPDnD-Ganesh/Shared/Services/Android.asmx";
	public final String url=domain+"/LPDnD/Shared/Services/Android.asmx";
	public String log = "lpdnd_app";
	public String deviceIP = "";
	UserSessionManager session;
	private DatabaseAdapter databaseAdapter;

	public Common(Context context)
	{
		c= context;
		session = new UserSessionManager(c); 
		databaseAdapter=new DatabaseAdapter(c);
		user= session.getLoginUserDetails();
	}

	//Check device has internet connection
	public boolean isConnected()	{
		ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else	{
			showToast("Unable to connect to Internet !");
			return false;
		}
	}

	//To show toast message
	public void showToast(String msg) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(c,msg, msg.length()+20000);
		toast.getView().setBackgroundColor(ContextCompat.getColor(c, android.R.color.white));
		TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
		v.setTextColor(ContextCompat.getColor(c, R.color.black));
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	//To show toast message with time duration
	public void showToast(String msg, int duration) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(c,msg, duration);
		toast.getView().setBackgroundColor(ContextCompat.getColor(c, android.R.color.white));
		TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
		v.setTextColor(ContextCompat.getColor(c, R.color.black));
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	//To show logout alert message
	public void showLogoutAlert(final Activity activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(appClose)
					activity.finish();
				else
				{
					TerminateSession();

				}
			}
		});

		alertbox.show();
	}

	//To show alert message with back to home page
	public void showAlertWithHomePage(final Context activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent intent = new Intent(activity, ActivityHomeScreen.class);
				//Clear all activities and start new task
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
				activity.startActivity(intent);

			}
		});

		alertbox.show();
	}

	//To show alert
	public void showAlert(final Activity activity, String message, final Boolean appClose)
	{
		AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
		alertbox.setTitle("Alert");
		alertbox.setCancelable(false);
		alertbox.setMessage(message);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(appClose)
					activity.finish();
			}
		});
		alertbox.show();
	}

	//Method to get current date time
	public String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MMM-yyyy HH:mm:ss", Locale.US);
		Date date = new Date();
		return dateFormat.format(date);
	}

	//Method to get current date
	public String getCurrentDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd-MMM-yyyy", Locale.US);
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	//To show date from "yyyy-MM-dd HH:mm:ss" to "dd-MMM-yyyy" format
    public String convertToDisplayDateFormat(String dateValue)
    {
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String createDateForDB = "";
        Date date = null;
        try {
            date = format.parse(dateValue);

            SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            createDateForDB = dbdateformat.format(date);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
        }
        return createDateForDB;
    }

   
	//alert on back button press.
	public void BackPressed(final Activity act) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act); 
		// set title
		alertDialogBuilder.setTitle("Confirmation"); 
		// set dialog message
		alertDialogBuilder
		.setMessage("Are you sure, you want to close this application?")
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, close
				System.out.println("Yes Pressed");
				dialog.cancel();			
				//act.finish();	
				Intent intent = new Intent(act, ActivityClose.class);
				//Clear all activities and start new task
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); 
				act.startActivity(intent);

			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				dialog.cancel();			
			}
		}); 
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create(); 
		// show it
		alertDialog.show();	
	}

	//To terminate user session 
	public void TerminateSession() {
		session.logoutUser();
	}

	//To append 0 if number is less than 10
	public String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	//To show GPS settings alert
	public void showGPSSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				c.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	//Method to copy db to sd card
	public String copyDBToSDCard(String DBName) throws Exception {
		try {
			InputStream myInput = new FileInputStream(c.getDatabasePath(DBName));

			File sdDir = Environment.getExternalStorageDirectory();

			File file = new File(sdDir+"/"+DBName);
			if (!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					databaseAdapter.insertExceptions(e.getMessage(), "Common.java","copyDBToSDCard");
					Log.i("FO","File creation failed for " + file);
				}
			}

			boolean success = true;
			if (success) {
				OutputStream myOutput = new FileOutputStream(sdDir+"/"+DBName);

				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer))>0){
					myOutput.write(buffer, 0, length);
				}
				//Close the streams
				myOutput.flush();
				myOutput.close();
			}
			else
			{
				showToast("Error in Backup");
			}

			myInput.close();
			Log.i("Database_Operation","copied");
			return sdDir+"/"+DBName;

		} catch (Exception e) {
			Log.i("Database_Operation","exception="+e);
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","copyDBToSDCard");
			return ("Error: In Database backup--> "+ e.getMessage());
		}
	}

	//To show date from "yyyy-MM-dd HH:mm:ss" to "dd-MMM-yyyy" format
	public String convertDateFormat(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 
		String createDateForDB = "";
		Date date = null;
		try {  
			date = format.parse(dateValue);  

			SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US); 
			createDateForDB = dbdateformat.format(date);

		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
		}
		return createDateForDB;
	}

	//To show date from "yyyy-MM-dd'T'HH:mm:ss" to "dd-MM-yy" format
	public String convertTDateFormat(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); 
		String createDateForDB = "";
		Date date = null;
		try {  
			date = format.parse(dateValue);  

			SimpleDateFormat  dbdateformat = new SimpleDateFormat("dd-MM-yy", Locale.US); 
			createDateForDB = dbdateformat.format(date);

		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateFormat");
		}
		return createDateForDB;
	}

	//To convert date time to mill second 
	public long convertDateStringToMilliSeconds(String dateValue)
	{
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); 
		long longMilliSeconds = 0;
		long remainingTime = 0;
		Date date = null;
		try {  
			date = format.parse(dateValue);  
			//longMilliSeconds = date.getTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			longMilliSeconds = calendar.getTimeInMillis();
			remainingTime= longMilliSeconds - System.currentTimeMillis();
		} catch (ParseException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace(); 
			databaseAdapter.insertExceptions(e.getMessage(), "Common.java","convertDateStringToMilliSeconds");
		}
		return remainingTime;
	}

	//To display number in 2 digits with comma formatted
	public String convertToTwoDecimal(String value)
	{		
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00"); 
		String result = formatter.format(Double.valueOf(value));
		return result;
	}

	//To display number in 3 digits with comma formatted
	public String convertToThreeDecimal(String value)
	{
		//This method apply commas and three digits
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.000"); 
		String result = formatter.format(Double.valueOf(value));
		return result;
	}


	//To display string number in 4 digits 
	public double stringToFourDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("0.0000"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}

	//To display number in 2 digits 
	public double stringToTwoDecimal(double value)
	{
		//NumberFormat formatter = new DecimalFormat("0.00"); 
		//DecimalFormat formatter = new DecimalFormat("0.00"); 
		DecimalFormat formatter = new DecimalFormat("#.00"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}

	//To display string number in 3 digits 
	public double stringToThreeDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("0.000"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}

	//To send JSON String
	public  String invokeJSONWS(String sendValue, String sendName, String methName, String newUrl) throws Exception {
		// Create request
		SoapObject request = new SoapObject(namespace, methName);

		// Property which holds input parameters		
		PropertyInfo paramPI = new PropertyInfo();
		// Set Name
		paramPI.setName(sendName);
		// Set Value
		paramPI.setValue(sendValue);
		// Set dataType
		paramPI.setType(String.class);
		// Add the property to request object

		request.addProperty(paramPI);

		// Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		// Set output SOAP object
		envelope.setOutputSoapObject(request);
		// Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(newUrl,10000);

		// Invoke web service
		androidHttpTransport.call(soap_action+methName, envelope);
		// Get the response
		SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
		// Assign it to static variable
		responseJSON = response.toString();

		return responseJSON;
	}

	//To get IMEI number of device
	public String getIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}


	//To send JSON String with two parameter
	public  String invokeTwinJSONWS(String sendValue1, String sendName1, String sendValue2, String sendName2, String methName, String newUrl) throws Exception {
		// Create request
		SoapObject request = new SoapObject(namespace, methName);

		// Property which holds input parameters		
		PropertyInfo paramPI = new PropertyInfo();
		// Set Name
		paramPI.setName(sendName1);
		// Set Value
		paramPI.setValue(sendValue1);


		PropertyInfo paramPI2 = new PropertyInfo();
		// Set Name
		paramPI2.setName(sendName2);
		// Set Value
		paramPI2.setValue(sendValue2);
		// Set dataType
		paramPI2.setType(String.class);
		// Add the property to request object

		request.addProperty(paramPI);
		request.addProperty(paramPI2);

		// Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		// Set output SOAP object
		envelope.setOutputSoapObject(request);
		// Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(newUrl,5000);

		// Invoke web service
		androidHttpTransport.call(soap_action+methName, envelope);
		// Get the response
		SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
		// Assign it to static variable
		responseJSON = response.toString();

		return responseJSON;
	}
	//To Get JSON String
	public  String CallJsonWS(String[] name, String[] value, String methodName, String newUrl) throws Exception {
		// Create request
		SoapObject request = new SoapObject(namespace, methodName);

		for(int i=0;i<name.length;i++){
			// Property which holds input parameters		
			PropertyInfo paramPI = new PropertyInfo();				
			// Set Name
			paramPI.setName(name[i]);
			// Set Value
			paramPI.setValue(value[i]);
			// Set dataType
			paramPI.setType(String.class);
			// Add the property to request object
			request.addProperty(paramPI);
		}


		Log.d("CallJsonWS"+methodName, request.toString());

		// Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		// Set output SOAP object
		envelope.setOutputSoapObject(request);
		// Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(newUrl,10000);

		// Invoke web service
		androidHttpTransport.call(soap_action+methodName, envelope);
		// Get the response
		SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
		// Assign it to static variable
		responseJSON = response.toString();

		return responseJSON;
	}


	/*//To display string number in 2 digits 
	public double stringToTwoDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("0.00"); 
		return Double.parseDouble(formatter.format(Double.valueOf(value)));
	}*/

	//To display string number in 2 digits 
	public String stringToTwoDecimal(String value)
	{
		NumberFormat formatter = new DecimalFormat("##,##,##,##,##,##,##,##,##0.00"); 
		formatter.setRoundingMode(RoundingMode.FLOOR);
		return formatter.format(Double.valueOf(value));
	}

	public String formateDateFromstring(String inputFormat, String outputFormat, String inputDate){

		Date parsed = null;
		String outputDate = "";

		SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, Locale.getDefault());
		SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, Locale.getDefault());

		try {
			parsed = df_input.parse(inputDate);
			outputDate = df_output.format(parsed);

		} catch (ParseException e) { 

		}

		return outputDate;

	}
	
	/*public static String getMobileIP() {
		  try {
		    for (Enumeration<NetworkInterface> en = NetworkInterface
		    .getNetworkInterfaces(); en.hasMoreElements();) {
		       NetworkInterface intf = (NetworkInterface) en.nextElement();
		       for (Enumeration<InetAddress> enumIpAddr = intf
		          .getInetAddresses(); enumIpAddr.hasMoreElements();) {
		          InetAddress inetAddress = enumIpAddr.nextElement();
		          if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
		             String ipaddress = inetAddress .getHostAddress().toString();
		             return ipaddress;
		          }
		       }
		    }
		  } catch (SocketException ex) {
		     Log.e("GetMobileIP", "Exception in Get IP Address: " + ex.toString());
		  }
		  return null;
		}
	
	private String getWIFIIP() {
		 try {
		   WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		   int ipAddress = wifiInfo.getIpAddress();
		   return String.format(Locale.getDefault(), "%d.%d.%d.%d",
		   (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
		   (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
		 } catch (Exception ex) {
		   Log.e("GetWIFIIP", ex.getMessage());
		   return null;
		 }
		}*/
	
	 public String getDeviceIPAddress(boolean useIPv4) {
	        try {
	            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	            for (NetworkInterface networkInterface : networkInterfaces) {
	                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
	                for (InetAddress inetAddress : inetAddresses) {
	                    if (!inetAddress.isLoopbackAddress()) {
	                        String sAddr = inetAddress.getHostAddress().toUpperCase();
	                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
	                        if (useIPv4) {
	                            if (isIPv4)
	                                return sAddr;
	                        } else {
	                            if (!isIPv4) {
	                                // drop ip6 port suffix
	                                int delim = sAddr.indexOf('%');
	                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
	                            }
	                        }
	                    }
	                }
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return "";
	    }

	public String prevent_E_Notation(String value)
	{
		return new BigDecimal(value).toPlainString();
	}

}


