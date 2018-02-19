package lateralpraxis.lpdnd;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserSessionManager {

	SharedPreferences pref;

	Editor editor;

	Context _context;

	int PRIVATE_MODE = 0;

	public static final String PREFER_NAME = "MyPrefsFile";
	private static final String IS_USER_LOGIN = "IsUserLoggedIn";

	public static final String KEY_ID = "sp_id";
	public static final String KEY_CODE = "sp_code";
	public static final String KEY_MEMBERSHIPID = "sp_membershipid";
	public static final String KEY_EMAIL = "sp_email";
	public static final String KEY_IMEI = "spimei";
	public static final String KEY_USERNAME = "sp_username";
	public static final String KEY_PWD = "sp_pwd";
	public static final String KEY_CUSTOMERTYPEID = "sp_customertypeid";
	public static final String KEY_CUSTOMERTYPE = "sp_customertype";
	public static final String KEY_ROUTEID = "sp_routeid";
	public static final String KEY_ROUTE = "sp_route";
	public static final String KEY_VEHICLEID = "sp_vehicleid";
	public static final String KEY_CENTREID = "sp_centreid";
	public static final String KEY_ROLES = "sp_roles";
	//current activated lang
	public static final String KEY_PREFLANG = "pref_lang";
	//all languages options from server
	public static final String KEY_OPTLANG = "opt_lang";


	public UserSessionManager(Context context){
		this._context = context;
		pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
		editor = pref.edit();
		editor.commit();
	}

	public void createUserLoginSession(String id, String code, String userName, String membershipId, String customerTypeId, String customerType, 
			String routeId, String route, String roles, String imei, String password, String optionLang, String vehicleId){
		editor.putBoolean(IS_USER_LOGIN, true);
		editor.putString(KEY_ID, id);
		editor.putString(KEY_CODE, code);
		editor.putString(KEY_USERNAME, userName);
		editor.putString(KEY_MEMBERSHIPID, membershipId);
		editor.putString(KEY_CUSTOMERTYPEID, customerTypeId);
		editor.putString(KEY_CUSTOMERTYPE, customerType);
		editor.putString(KEY_ROUTEID, routeId);
		editor.putString(KEY_ROUTE, route);
		editor.putString(KEY_ROLES, roles);
		editor.putString(KEY_IMEI, imei);
		editor.putString(KEY_PWD, password);
		editor.putString(KEY_OPTLANG, optionLang);
		editor.putString(KEY_PREFLANG, "en");
		editor.putString(KEY_VEHICLEID, vehicleId);
		editor.commit();
	}

	public void updatePrefLanguage(String lang){	
		editor.putString(KEY_PREFLANG, lang);
		editor.commit();
		
	}  
	public void updatePassword(String pwd){	
		editor.putString(KEY_PWD, pwd);
		editor.commit();

	}
	
	public void updateRouteOfficerDetails(String routeId, String centreId, String vehicleId ){	
		editor.putString(KEY_ROUTEID, routeId);
		editor.putString(KEY_CENTREID, centreId);
		editor.putString(KEY_VEHICLEID, vehicleId);
		editor.commit();
		
	}


	public boolean checkLogin(){
		if(!this.isUserLoggedIn()){
			Intent i = new Intent(_context,ActivityLogin.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(i);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean checkLoginShowHome(){
		if(this.isUserLoggedIn()){
			Intent i = new Intent(_context,ActivityHomeScreen.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(i);
			return true;
		}
		else
		{
			return false;
		}
	}


	public HashMap<String, String> getLoginUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		user.put(KEY_ID, pref.getString(KEY_ID, null));
		user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
		user.put(KEY_PWD, pref.getString(KEY_PWD, null));
		user.put(KEY_CODE, pref.getString(KEY_CODE, null));
		user.put(KEY_MEMBERSHIPID,  pref.getString(KEY_MEMBERSHIPID, null));
		user.put(KEY_CUSTOMERTYPEID, pref.getString(KEY_CUSTOMERTYPEID, null));
		user.put(KEY_CUSTOMERTYPE, pref.getString(KEY_CUSTOMERTYPE, null));
		user.put(KEY_ROUTEID, pref.getString(KEY_ROUTEID, null));
		user.put(KEY_ROUTE, pref.getString(KEY_ROUTE, null));
		user.put(KEY_ROLES, pref.getString(KEY_ROLES, null));
		user.put(KEY_IMEI, pref.getString(KEY_IMEI, null));
		user.put(KEY_OPTLANG, pref.getString(KEY_OPTLANG, null));
		user.put(KEY_VEHICLEID, pref.getString(KEY_VEHICLEID, null));
		user.put(KEY_CENTREID, pref.getString(KEY_CENTREID, null));
		return user;
	}
	
	public String getDefaultLang(){
		return pref.getString(KEY_PREFLANG, null);
	}   

	public void logoutUser(){

		editor.clear();
		editor.commit();

		Intent i = new Intent(_context, ActivityLogin.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		_context.startActivity(i);
	}

	public boolean isUserLoggedIn(){
		return pref.getBoolean(IS_USER_LOGIN, false);
	}
}