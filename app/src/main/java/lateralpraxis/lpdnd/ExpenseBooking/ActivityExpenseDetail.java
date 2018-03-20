package lateralpraxis.lpdnd.ExpenseBooking;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Locale;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.ViewImage;

public class ActivityExpenseDetail extends Activity {

    private final Context mContext = this;
    //<editor-fold desc="Code for Class Declaraion">
    DatabaseAdapter db;
    Common common;
    //</editor-fold>
    File file;
    //<editor-fold desc="Code to declare controls">
    private TextView tvExpenseDate, tvExpenseHead, tvExpenseAmt, tvExpenseRemarks, tvAttach;
    private LinearLayout llAttachment;
    //</editor-fold>
    private UserSessionManager session;
    //<editor-fold desc="Code for Declaring Variables">
    private ArrayList<String> expensedetails;
    private String id, lang, filePath;
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    //</editor-fold>

    //<editor-fold desc="Code to be executed on OnCreate">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        //</editor-fold>

        //<editor-fold desc="Code to set Id from Previous Intent">
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("Id");
        }
        //</editor-fold>

        //<editor-fold desc="Code to Set Language">
        lang = session.getDefaultLang();
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //</editor-fold>

        //<editor-fold desc="Code to find controls">
        tvExpenseDate = (TextView) findViewById(R.id.tvExpenseDate);
        tvExpenseHead = (TextView) findViewById(R.id.tvExpenseHead);
        tvExpenseAmt = (TextView) findViewById(R.id.tvExpenseAmt);
        tvExpenseRemarks = (TextView) findViewById(R.id.tvExpenseRemarks);
        tvAttach = (TextView) findViewById(R.id.tvAttach);
        llAttachment = (LinearLayout) findViewById(R.id.llAttachment);
        //</editor-fold>

        //<editor-fold desc="Code to bind and display data">
        db.openR();
        expensedetails = db.getExpenseDetailById(id, lang);
        tvExpenseDate.setText(common.convertToDisplayDateFormat(expensedetails.get(0)));
        tvExpenseHead.setText(expensedetails.get(1));
        tvExpenseAmt.setText(common.convertToTwoDecimal(expensedetails.get(2)));
        tvExpenseRemarks.setText(expensedetails.get(3));
        tvAttach.setText(expensedetails.get(5));
        filePath = expensedetails.get(4);
        if (expensedetails.get(5).trim().equalsIgnoreCase(""))
            llAttachment.setVisibility(View.GONE);
        else
            llAttachment.setVisibility(View.VISIBLE);
        //</editor-fold>

        //<editor-fold desc="Code to open attachment">
        tvAttach.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {

                    String actPath = filePath;
                    int pathLen = actPath.split("/").length;
                    //to Get Unique Id
                    String newPath1 = actPath.split("/")[pathLen - 2];
                    String newPath2 = actPath.split("/")[pathLen - 3];
                    String catType = "Expense";
                    // Check for SD Card
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        common.showToast("Error! No SDCARD Found!");
                    } else {
                        // Locate the image folder in your SD Card
                        file = new File(Environment.getExternalStorageDirectory()
                                + File.separator + newPath2 + File.separator + newPath1 + File.separator);
                    }

                    if (file.isDirectory()) {

                        listFile = file.listFiles(new FilenameFilter() {
                            public boolean accept(File directory, String fileName) {
                                return fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
                            }
                        });
                        // Create a String array for FilePathStrings
                        FilePathStrings = new String[listFile.length];
                        // Create a String array for FileNameStrings
                        FileNameStrings = new String[listFile.length];

                        for (int i = 0; i < listFile.length; i++) {
                            FilePathStrings[i] = listFile[i].getAbsolutePath();
                            // Get the name image file
                            FileNameStrings[i] = listFile[i].getName();

                            Intent i1 = new Intent(ActivityExpenseDetail.this, ViewImage.class);
                            // Pass String arrays FilePathStrings
                            i1.putExtra("filepath", FilePathStrings);
                            // Pass String arrays FileNameStrings
                            i1.putExtra("filename", FileNameStrings);
                            // Pass String category type
                            i1.putExtra("categorytype", catType);
                            // Pass click position
                            i1.putExtra("position", 0);
                            startActivity(i1);
                        }
                    }


                } catch (Exception except) {
                    //except.printStackTrace();
                    common.showAlert(ActivityExpenseDetail.this, "Image not available.", false);

                }
            }
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Code to Set Home Button in Action Bar">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on Action Bar Menu Item">
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_to_home:
                Intent i = new Intent(ActivityExpenseDetail.this, ActivityHomeScreen.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to b executed on Back Press">
    @Override
    public void onBackPressed() {

        Intent i = new Intent(ActivityExpenseDetail.this, ActivityListBooking.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>
}
