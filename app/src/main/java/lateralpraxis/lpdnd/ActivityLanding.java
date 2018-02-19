package lateralpraxis.lpdnd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ActivityLanding extends Activity {
	//private static final String LOG = "LPDnD";
	final Context context = this;
	Common common;
	Button btnNewUsers;
	Button btnExistingUsers;
	private Intent intent;
	//Code to be executed on page load
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing);
		//To create instance of common page 
		common = new Common(getApplicationContext());
		btnNewUsers=(Button)findViewById(R.id.btnNewUsers);
		btnExistingUsers=(Button)findViewById(R.id.btnExistingUsers);

		//On click of New Users
		btnNewUsers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				intent = new Intent(context,ActivityCustomerSignUp.class);
				startActivity(intent);
			}
		});

		//On click of Existing Users
		btnExistingUsers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				intent = new Intent(context,ActivityLogin.class);
				startActivity(intent);
			}
		});
	}
}
