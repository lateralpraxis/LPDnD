package lateralpraxis.lpdnd.OutletExcess;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import lateralpraxis.lpdnd.ActivityHomeScreen;
import lateralpraxis.lpdnd.Common;
import lateralpraxis.lpdnd.DatabaseAdapter;
import lateralpraxis.lpdnd.DecimalDigitsInputFilter;
import lateralpraxis.lpdnd.ImageLoadingUtils;
import lateralpraxis.lpdnd.R;
import lateralpraxis.lpdnd.UserSessionManager;
import lateralpraxis.lpdnd.ViewImage;
import lateralpraxis.lpdnd.types.CustomType;

public class ActivityAddExcess extends Activity {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    final String fpRegex =
            ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                    "[+-]?(" +         // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from the Java Language Specification, 2nd
                    // edition, section 3.10.2.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");
    //<editor-fold desc="Variable Declaration for Uploading Attachment">
    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_REQUEST = 1;
    private static final int PICK_Camera_IMAGE = 0;
    private final Context mContext = this;
    //<editor-fold desc="Code for class declaration">
    DatabaseAdapter db;
    Common common;
    //</editor-fold>
    String lang = "en";
    Bitmap bitmap;
    Uri uri;
    Intent picIntent = null;
    //</editor-fold>
    File destination, file;
    private UserSessionManager session;
    private Intent intent;
    //<editor-fold desc="Code to Declare Controls">
    private Spinner spExcessHead;
    private EditText etAmt,etRemarks;
    private TextView tvAttach;
    private Button btnSave, btnUpload;
    private String level1Dir, level2Dir, fullPath,
            photoPath, uuidImg;
    private int fileCount = 0;
    private ImageLoadingUtils utils;
    private File[] listFile;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    //</editor-fold>

    //<editor-fold desc="Method to generate random number and return the same">
    public static String random() {
        Random r = new Random();

        char[] choices = ("abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "01234567890").toCharArray();

        StringBuilder salt = new StringBuilder(10);
        for (int i = 0; i < 10; ++i)
            salt.append(choices[r.nextInt(choices.length)]);
        return "img_" + salt.toString();
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed on On Create Method">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_excess);

        //<editor-fold desc="Code for setting Action Bar">
        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //</editor-fold>

        //<editor-fold desc="Code for creating Instance of Class">
        db = new DatabaseAdapter(this);
        common = new Common(this);
        session = new UserSessionManager(getApplicationContext());
        utils = new ImageLoadingUtils(this);
        //</editor-fold>

        //<editor-fold desc="Code to delete Temporary Document">
        db.open();
        db.deleteTempDoc();
        db.close();
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

        //<editor-fold desc="Code to Find Controls">
        spExcessHead = (Spinner) findViewById(R.id.spExcessHead);
        etAmt = (EditText) findViewById(R.id.etAmt);
        etRemarks = (EditText) findViewById(R.id.etRemarks);
        tvAttach = (TextView) findViewById(R.id.tvAttach);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        //</editor-fold>

        //<editor-fold desc="Code to set Input Filter">
        etAmt.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etAmt.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //</editor-fold>

        etAmt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (Pattern.matches(fpRegex, etAmt.getText())) {

                    }
                    else
                        etAmt.setText("");
                }
            }
        });
        //<editor-fold desc="Code to Bind Spinners">
        spExcessHead.setAdapter(DataAdapter("excesshead", ""));
        //</editor-fold>

        //<editor-fold desc="Code to be executed on click of Save Button">
        btnSave.setOnClickListener(new View.OnClickListener() {
            //When go button click
            @Override
            public void onClick(View arg0) {
                etAmt.clearFocus();
                if(((CustomType)spExcessHead.getSelectedItem()).getId().equalsIgnoreCase("0"))
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया अतिरिक्त फंड हेड का चयन करें":"Please select Excess Fund Head.");
                else if(etAmt.getText().toString().trim().length()<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया राशि दर्ज करें":"Please enter amount.");
                else if (Double.valueOf(etAmt.getText().toString())<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"राशि शून्य नहीं हो सकती":"Amount cannot be zero.");
                else if(etRemarks.getText().toString().trim().length()<=0)
                    common.showToast(lang.equalsIgnoreCase("hi") ?"कृपया टिप्पणी दर्ज करें":"Please enter remarks.");
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "पुष्टीकरण" : "Confirmation");
                    builder1.setMessage(lang.equalsIgnoreCase("hi") ? "क्या आप निश्चित हैं, आप अतिरिक्त फंड विवरण जमा करना चाहते हैं?" : "Are you sure, you want to submit excess fund details?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    String outdir = "";
                                    String imagePath = "";
                                    String selectedPhotoPath = "";
                                    String newuuidImg = "";
                                    newuuidImg = UUID.randomUUID().toString();
                                    if (tvAttach.getText().toString().trim().length() > 0) {
                                        //Code to save image in path and insert data in temporary table
                                        if (photoPath != null || photoPath != "") {

                                            //Setting directory structure
                                            level1Dir = "LPDND";
                                            level2Dir = level1Dir + "/" + newuuidImg;
                                            //Code to set full path
                                            fullPath = Environment.getExternalStorageDirectory() + "/" + level2Dir;
                                            //Code to set absolute path
                                            outdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + level2Dir;

                                            //Code to set photo path
                                            selectedPhotoPath = photoPath.substring(photoPath.lastIndexOf("/") + 1);
                                        }
                                        //Code to check if directory exists else create directory
                                        if (createDirectory(level1Dir) && createDirectory(level2Dir)) {
                                            imagePath = copyFile(photoPath, outdir);
                                        }
                                    }
                                    String customerId = "";
                                    HashMap<String, String> user = session.getLoginUserDetails();
                                    customerId = user.get(UserSessionManager.KEY_ID);
                                    db.open();
                                    db.Insert_ExcessBooking(customerId, ((CustomType) spExcessHead.getSelectedItem()).getId(), Double.valueOf(etAmt.getText().toString()).toString(), etRemarks.getText().toString(), newuuidImg, imagePath, selectedPhotoPath);
                                    db.close();
                                    common.showToast(lang.equalsIgnoreCase("hi") ? "अतिरिक्त फंड विवरण सफलतापूर्वक सहेजा गया" : "Excess Fund details saved successfully.");
                                    Intent intent = new Intent(ActivityAddExcess.this, ActivityListExcess.class);
                                    startActivity(intent);
                                    finish();

                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
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
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to be executed on Button Upload Click">
        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String strmessage = lang.equalsIgnoreCase("hi") ? "क्या आप वाकई मौजूदा चित्र को हटाना चाहते हैं और नया चित्र अपलोड करना चाहते हैं?" : "Are you sure, you want to remove existing picture and upload new picture?";
                if (tvAttach.getText().toString().trim().length() > 0) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                            mContext);
                    builder1.setTitle(lang.equalsIgnoreCase("hi") ? "चित्र संलग्न करें" : "Attach Image");
                    builder1.setMessage(strmessage);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(lang.equalsIgnoreCase("hi") ? "हाँ" : "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    db.open();
                                    db.deleteTempDoc();
                                    db.close();
                                    level1Dir = "LPDND" + "/" + uuidImg;
                                    File dir = new File(level1Dir);
                                    DeleteRecursive(dir);
                                    tvAttach.setText("");
                                    startDialog();

                                }
                            }).setNegativeButton(lang.equalsIgnoreCase("hi") ? "नहीं" : "No",
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
                } else
                    startDialog();
            }
        });
        //</editor-fold>

        //<editor-fold desc="Code to open attachment">
        tvAttach.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.openR();
                String struploadedFilePath = db.getTempDocument();

                try {

                    String actPath = struploadedFilePath;
                    int pathLen = actPath.split("/").length;
                    //to Get Unique Id
                    String newPath1 = actPath.split("/")[pathLen - 2];
                    String newPath2 = actPath.split("/")[pathLen - 3];

                    String catType = "Excess Fund";
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

                            Intent i1 = new Intent(ActivityAddExcess.this, ViewImage.class);
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
                    common.showAlert(ActivityAddExcess.this, "Error: " + except.getMessage(), false);

                }
            }
        });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Code for Binding Data In Spinner">
    private ArrayAdapter<CustomType> DataAdapter(String masterType, String filter) {
        db.open();
        List<CustomType> lables = db.GetCustomerMasterDetails(masterType, filter);
        ArrayAdapter<CustomType> dataAdapter = new ArrayAdapter<CustomType>(this, android.R.layout.simple_spinner_item, lables);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        db.close();
        return dataAdapter;
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
                Intent i = new Intent(ActivityAddExcess.this, ActivityHomeScreen.class);
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

        Intent i = new Intent(ActivityAddExcess.this, ActivityListExcess.class);
        startActivity(i);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();

    }
    //</editor-fold>

    //<editor-fold desc="Method for Opening Dialog">
    private void startDialog() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityAddExcess.this);
        builderSingle.setTitle("Select Image source");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                ActivityAddExcess.this,
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
                        if (strName.equals("Capture Image")) {
                            //Setting directory structure
                            uuidImg = UUID.randomUUID().toString();
                            level1Dir = "LPDND";
                            level2Dir = level1Dir + "/" + uuidImg;
                            String imageName = random() + ".jpg";
                            fullPath = Environment.getExternalStorageDirectory() + "/" + level2Dir;
                            destination = new File(fullPath, imageName);
                            //Check if directory exists else create directory
                            if (createDirectory(level1Dir) && createDirectory(level2Dir)) {
                                //Code to open camera intent
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        Uri.fromFile(destination));
                                startActivityForResult(intent, PICK_Camera_IMAGE);
                                db.open();
                                db.Insert_TempDoc(fullPath + "/" + imageName);
                                db.close();
                            }
                            //Code to set image name
                            photoPath = fullPath + "/" + imageName;
                            tvAttach.setText(imageName);
                        } else if (strName.equals("Select from Gallery")) {
                            //Code to open gallery intent
                            picIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            picIntent.putExtra("return_data", true);
                            startActivityForResult(picIntent, GALLERY_REQUEST);
                        } else {
                            common.showToast("No File available for review.");
                        }
                    }
                });
        builderSingle.show();
    }
    //</editor-fold>

    //<editor-fold desc="Code to be executed after action done for attaching">
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == 0 && data == null) {
            //Reset image name and hide reset button
            tvAttach.setText("");
        } else if (requestCode == GALLERY_REQUEST) {
            //Gallery request and result code is ok
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    uri = data.getData();
                    if (uri != null) {
                        photoPath = getRealPathFromUri(uri);
                        tvAttach.setText(photoPath.substring(photoPath.lastIndexOf("/") + 1));
                        uuidImg = UUID.randomUUID().toString();
                        //Set directory path
                        level1Dir = "LPDND";
                        level2Dir = level1Dir + "/" + uuidImg;
                        fullPath = Environment.getExternalStorageDirectory() + "/" + level2Dir;
                        //Code to create file inside directory
                        if (createDirectory(level1Dir)
                                && createDirectory(level2Dir)) {
                            copyFile(photoPath, fullPath);
                            destination = new File(photoPath);
                        }
                        db.open();
                        db.Insert_TempDoc(fullPath + "/" + destination.getName());
                        db.close();
                    } else {

                        Toast.makeText(getApplicationContext(), "Cancelled",
                                Toast.LENGTH_SHORT).show();

                    }
                    if (photoPath != "" && photoPath != null) {
                        common.showToast("Gallery Image selected at path: " + photoPath);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST) {
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
                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to get Actual path of image">
    private String getRealPathFromUri(Uri tempUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = this.getContentResolver().query(tempUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to get file count in directory">
    public int CountFiles(File[] files) {
        if (files == null || files.length == 0) {
            return 0;
        } else {
            for (File file : files) {
                if (file.isDirectory()) {
                    CountFiles(file.listFiles());
                } else {
                    if (!file.getAbsolutePath().contains(".nomedia"))
                        fileCount++;
                }
            }
            return fileCount;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to compress the image">
    public String compressImage(String path) {

        String filePath = path;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

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
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            //exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            //exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


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
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
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
    //</editor-fold>

    //<editor-fold desc="Method to delete File Recursively">
    public void DeleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();

    }
    //</editor-fold>

    //<editor-fold desc="Method to create new directory">
    private boolean createDirectory(String dirName) {
        //Code to Create Directory for Inspection (Parent)
        File folder = new File(Environment.getExternalStorageDirectory() + "/" + dirName);
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
    //</editor-fold>

    //<editor-fold desc="Method to create No Media File in directory">
    private void copyNoMediaFile(String dirName) {
        try {
            // Open your local db as the input stream
            //boolean D= true;
            String storageState = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                try {
                    File noMedia = new File(Environment
                            .getExternalStorageDirectory()
                            + "/"
                            + level2Dir, ".nomedia");
                    if (noMedia.exists()) {


                    }

                    FileOutputStream noMediaOutStream = new FileOutputStream(noMedia);
                    noMediaOutStream.write(0);
                    noMediaOutStream.close();
                } catch (Exception e) {

                }
            } else {

            }

        } catch (Exception e) {

        }
    }
    //</editor-fold>

    //<editor-fold desc="Copy file from one place to another">
    private String copyFile(String inputPath, String outputPath) {

        File f = new File(inputPath);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + "/" + f.getName());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            compressImage(outputPath + "/" + f.getName());

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;


        } catch (FileNotFoundException fnfe1) {
            //Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            //Log.e("tag", e.getMessage());
        }
        return outputPath + "/" + f.getName();
    }
    //</editor-fold>
}
