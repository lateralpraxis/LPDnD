package lateralpraxis.lpdnd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lateralpraxis.lpdnd.types.CashDeposit;
import lateralpraxis.lpdnd.types.CustomType;
import lateralpraxis.lpdnd.types.CustomerPayment;
import lateralpraxis.lpdnd.types.Payments;

public class DatabaseAdapter {

    public static final int NAME_COLUMN = 1;
    static final String DATABASE_NAME = "ganeshdairy.db";
    static final int DATABASE_VERSION = 1;
    // Crating tables of database
    static final String Exceptions_CREATE = "CREATE TABLE Exceptions(Id INTEGER PRIMARY KEY AUTOINCREMENT, Message TEXT, ActivityName TEXT, CalledMethod TEXT, PhoneInfo TEXT, CreatedOn DATETIME);",
            Item_CREATE = "CREATE TABLE IF NOT EXISTS Item(Id TEXT, Name TEXT, Type TEXT, Rate TEXT, ProductLocal TEXT, Product TEXT, SkuUnit TEXT, Uom TEXT);",
            Msg_CREATE = "CREATE TABLE IF NOT EXISTS Msg(Id TEXT, Name TEXT);",
            Centre_CREATE = "CREATE TABLE IF NOT EXISTS Centre(Id TEXT, Name TEXT);",
            MISCompany_CREATE = "CREATE TABLE IF NOT EXISTS MISCompany(Id TEXT, Name TEXT);",
            Route_CREATE = "CREATE TABLE IF NOT EXISTS Route(Id TEXT, Name TEXT, CentreId TEXT);",
            Vehicle_CREATE = "CREATE TABLE IF NOT EXISTS Vehicle(Id TEXT, Name TEXT, RouteId TEXT);",
            Company_CREATE = "CREATE TABLE IF NOT EXISTS Company(Id TEXT, Name TEXT, ShortName TEXT);",
            Bank_CREATE = "CREATE TABLE IF NOT EXISTS Bank(Id TEXT, Name TEXT);",
            RouteVehicleMaster_CREATE = "CREATE TABLE IF NOT EXISTS RouteVehicleMaster(RouteId TEXT, Route TEXT, VehicleId TEXT, Vehicle TEXT, Capacity TEXT);",
            CustomerMaster_CREATE = "CREATE TABLE IF NOT EXISTS CustomerMaster(RouteId TEXT, Route TEXT, CustomerId TEXT, Customer TEXT, Mobile TEXT, CustomerType TEXT, LoginId TEXT, CustomerLocal TEXT);",
            DemandDate_CREATE = "CREATE TABLE IF NOT EXISTS DemandDate(Date TEXT);",
            DemandCutOff_CREATE = "CREATE TABLE IF NOT EXISTS DemandCutOff(DateTimeCurrentTime TEXT);",
            ProductMaster_CREATE = "CREATE TABLE IF NOT EXISTS ProductMaster(CompanyId TEXT, Company TEXT, SKUId TEXT, SKU TEXT, PackingType TEXT, Rate TEXT, ProductLocal TEXT, Product TEXT, SkuUnit TEXT, Uom TEXT);",
            CustomerRate_CREATE = "CREATE TABLE IF NOT EXISTS CustomerRate(CustomerId TEXT, SKUId TEXT, Rate TEXT);",
            CompanyRate_CREATE = "CREATE TABLE IF NOT EXISTS CompanyRate(SKUId TEXT, Rate TEXT);",
            Demand_CREATE = "CREATE TABLE IF NOT EXISTS Demand(Id INTEGER PRIMARY KEY AUTOINCREMENT, Code TEXT, UniqueId TEXT, CustomerId TEXT, Customer TEXT, CreateDate TEXT, DemandDate TEXT, Role TEXT, IsSync TEXT);",
            DemandDetails_CREATE = "CREATE TABLE IF NOT EXISTS DemandDetails(Id INTEGER PRIMARY KEY AUTOINCREMENT, DemandId TEXT, ItemId TEXT, Item TEXT, Qty TEXT, Rate TEXT);",
            ViewDemand_CREATE = "CREATE TABLE IF NOT EXISTS ViewDemand(Id TEXT, Name TEXT);",
            ViewDemandDetails_CREATE = "CREATE TABLE IF NOT EXISTS ViewDemandDetails(Id TEXT, Item TEXT, DemandQty TEXT, PackingType TEXT, Rate TEXT, ProductLocal TEXT, Product TEXT, SkuUnit TEXT, Uom TEXT);",
            DeliveryInputDemand_CREATE = "CREATE TABLE IF NOT EXISTS DeliveryInputDemand(CustomerId TEXT, SKUId TEXT, DQty TEXT);",
            DeliveryInput_CREATE = "CREATE TABLE IF NOT EXISTS DeliveryInput(SkuId TEXT, Sku TEXT, PackingType TEXT, AvailQty TEXT, CompanyId TEXT, RouteId TEXT, Product TEXT, SkuUnit TEXT, Uom TEXT, ProductLocal TEXT, MulFactor TEXT);",
            Delivery_CREATE = "CREATE TABLE IF NOT EXISTS Delivery(Id INTEGER PRIMARY KEY AUTOINCREMENT, UniqueId TEXT, CentreId TEXT, RouteId TEXT, Route TEXT, CustomerId TEXT, Customer TEXT, CreateBy TEXT, CreateDate TEXT, Imei TEXT, IsSync TEXT, VehicleId TEXT, DeliverTo TEXT);",
            DeliveryDetail_CREATE = "CREATE TABLE IF NOT EXISTS DeliveryDetail(Id INTEGER PRIMARY KEY AUTOINCREMENT, DeliveryId TEXT, SkuId TEXT, Sku TEXT, Rate TEXT, DQty TEXT, AvailQty TEXT, DelQty TEXT, CompanyId TEXT);",

    /*********************Tables used in new customer registration******************/
    CustomerType_CREATE = "CREATE TABLE IF NOT EXISTS CustomerType(Id TEXT, Name TEXT);",
            Country_CREATE = "CREATE TABLE IF NOT EXISTS Country(Id TEXT, Name TEXT);",
            State_CREATE = "CREATE TABLE IF NOT EXISTS State(Id TEXT, CountryId TEXT, Name TEXT);",
            City_CREATE = "CREATE TABLE IF NOT EXISTS City(Id TEXT, StateId TEXT, Name TEXT);",
            PinCode_CREATE = "CREATE TABLE IF NOT EXISTS PinCode(Id TEXT, CityId TEXT, Name TEXT);",
            CustomerRegistration_CREATE = "CREATE TABLE IF NOT EXISTS CustomerRegistration(Id INTEGER PRIMARY KEY AUTOINCREMENT, CustTypeId TEXT, LoginId TEXT, CustName TEXT, ContactPerson TEXT, Mobile TEXT, Street TEXT, HouseNo TEXT, LandMark TEXT, EmailId TEXT, StateId TEXT, CityId TEXT, PinCodeId TEXT, CreateDate TEXT, IpAddr TEXT, Machine TEXT);",
    /*********************End of Tables used in new customer registration******************/
    /*********************Tables used in stock return******************/
    StockReturn_CREATE = "CREATE TABLE IF NOT EXISTS StockReturn(Id INTEGER PRIMARY KEY AUTOINCREMENT, UniqueId TEXT, RouteId TEXT, VehicleId TEXT, ReturnDate TEXT, CreateBy TEXT, IsSync TEXT);",
            StockReturnDetail_CREATE = "CREATE TABLE IF NOT EXISTS StockReturnDetails(Id INTEGER PRIMARY KEY AUTOINCREMENT, ReturnId TEXT, SKUId TEXT, SKU TEXT, ReturnQty TEXT, LeakageQty TEXT);",
    /*********************End of Tables used in stock return******************/

    /*********************Tables used in Cash Deposit******************/
    CashDepositHeader_CREATE = "CREATE TABLE IF NOT EXISTS CashDepositHeader(CompanyId TEXT, CompanyName TEXT, PreviousBalance TEXT, CollectionAmount TEXT, TotalAmount TEXT, OnlineAmount TEXT);",
            CashDepositDetail_CREATE = "CREATE TABLE IF NOT EXISTS CashDepositDetail(CompanyId TEXT, PCDetailId TEXT, CustomerName TEXT, PaymentDate TEXT, Cheque TEXT, Amount TEXT);",
            CashDepositTransaction_CREATE = "CREATE TABLE IF NOT EXISTS CashDepositTransaction(CompanyId TEXT, PreviousBalance TEXT, CollectionAmount TEXT, TotalAmount TEXT, DepositAmount TEXT, PCDetailId TEXT, Remarks TEXT);",
    /*********************End of Tables used in Cash Deposit******************/
    CustomerLedger_CREATE = "CREATE TABLE IF NOT EXISTS CustomerLedger(CustomerId TEXT, CompanyId TEXT, Balance REAL);",
            CustomerPaymentTemp_CREATE = "CREATE TABLE IF NOT EXISTS CustomerPaymentTemp(Id INTEGER PRIMARY KEY AUTOINCREMENT,CompanyId TEXT, BankId TEXT, ChequeNumber TEXT,  Amount TEXT, ImagePath TEXT,UniqueId TEXT, ImageName TEXT,Remarks TEXT,CreateDate DATETIME);",
            CustomerPaymentMaster_CREATE = "CREATE TABLE IF NOT EXISTS CustomerPaymentMaster(Id INTEGER PRIMARY KEY AUTOINCREMENT,UniqueId TEXT,CustomerId TEXT, CreateBy TEXT,CreateDate DATETIME, DeliveryUniqueId TEXT, IsSync TEXT);",
            CustomerPaymentDetail_CREATE = "CREATE TABLE IF NOT EXISTS CustomerPaymentDetail(Id INTEGER PRIMARY KEY AUTOINCREMENT,MasterId INTEGER, CompanyId TEXT, BankId TEXT, ChequeNumber TEXT, Amount TEXT, ImagePath TEXT,UniqueId TEXT, ImageName TEXT,Remarks TEXT,CreateDate DATETIME, IsSync TEXT);",

    /********************* Tables used in new Complaint/ feedback ******************/
    ComplaintCategory_CREATE = "CREATE TABLE IF NOT EXISTS ComplaintCategory(Id TEXT, Name TEXT);",
            Complaint_CREATE = "CREATE TABLE IF NOT EXISTS Complaint(Id INTEGER PRIMARY KEY AUTOINCREMENT,ComplaintDate DATETIME,CustomerId TEXT,ComplaintType TEXT,DeviceUniqueId TEXT, ComplaintCategoryId TEXT, FeedBackRating TEXT, CustomerRemark TEXT, IsSync);",
            TempDocTABLE_CREATE = "CREATE TABLE IF NOT EXISTS TempDoc (FileName TEXT)",

    /********************* Tables used in Outlet Sale ******************/
    /********************* Tables used in Outlet Sale ******************/
    OutletSale_CREATE = "CREATE TABLE IF NOT EXISTS OutletSale(Id INTEGER PRIMARY KEY AUTOINCREMENT, UniqueId TEXT, CustomerId TEXT, Customer TEXT, SaleType TEXT, CreateBy TEXT, CreateDate TEXT, Imei TEXT, IsSync TEXT);",
            OutletSaleDetail_CREATE = "CREATE TABLE IF NOT EXISTS OutletSaleDetail(Id INTEGER PRIMARY KEY AUTOINCREMENT, OutletSaleId TEXT, SkuId TEXT, Sku TEXT, Rate TEXT, SaleRate TEXT, Qty TEXT, SaleQty TEXT);",
            ExpenseConfirmation_CREATE = "CREATE TABLE IF NOT EXISTS ExpenseConfirmationData(Id TEXT, ExpenseDate TEXT, CustomerName TEXT, ExpenseHead TEXT, Amount TEXT, Remarks TEXT);",
    /********************* Tables used in Delete For System User ******************/
    CashDepositDeleteDataTABLE_CREATE = "CREATE TABLE IF NOT EXISTS CashDepositDeleteData (CashDepositId TEXT, CashDepositDetailId TEXT, DepositDate TEXT, PCDetailId TEXT, Mode TEXT, Amount TEXT, FullName TEXT)",
            RawMaterialMaster_CREATE = "CREATE TABLE IF NOT EXISTS RawMaterialMaster(Id TEXT, Name TEXT, UOM TEXT, NameLocal TEXT);",
            OutletInventory_CREATE = "CREATE TABLE IF NOT EXISTS OutletInventory(Id INTEGER PRIMARY KEY AUTOINCREMENT,RawMaterialId TEXT, SKUId TEXT, Quantity TEXT);",
            SKUMaster_CREATE = "CREATE TABLE IF NOT EXISTS SKUMaster(Id TEXT,Name TEXT, NameLocal TEXT, Units TEXT, SKU TEXT);",
            SaleRateMaster_CREATE = "CREATE TABLE IF NOT EXISTS SaleRateMaster(Id TEXT,Rate TEXT, FromDate TEXT, ToDate TEXT);",
            OutletPrimaryReceipt_CREATE = "CREATE TABLE IF NOT EXISTS OutletPrimaryReceipt(Id INTEGER PRIMARY KEY AUTOINCREMENT,UniqueId TEXT, CustomerId TEXT, MaterialId TEXT, SKUId TEXT, Quantity TEXT, Amount TEXT,CreateDate TEXT, IsSync TEXT);",
            OutletConversionConsumedTemp_CREATE = "CREATE TABLE IF NOT EXISTS OutletConversionConsumedTemp(Id INTEGER PRIMARY KEY AUTOINCREMENT, MaterialId TEXT, SKUId TEXT, Quantity TEXT);",
            OutletConversionProducedTemp_CREATE = "CREATE TABLE IF NOT EXISTS OutletConversionProducedTemp(Id INTEGER PRIMARY KEY AUTOINCREMENT,SKUId TEXT, Quantity TEXT);",
            OutletConversion_CREATE = "CREATE TABLE IF NOT EXISTS OutletConversion(Id INTEGER PRIMARY KEY AUTOINCREMENT, UniqueId TEXT,CustomerId TEXT, AndroidDate TEXT, IsSync TEXT);",
            OutletConversionConsumed_CREATE = "CREATE TABLE IF NOT EXISTS OutletConversionConsumed(Id INTEGER PRIMARY KEY AUTOINCREMENT, UniqueId TEXT,MaterialId TEXT, SKUId TEXT, Quantity TEXT);",
            OutletConversionProduced_CREATE = "CREATE TABLE IF NOT EXISTS OutletConversionProduced(Id INTEGER PRIMARY KEY AUTOINCREMENT, UniqueId TEXT,SKUId TEXT, Quantity TEXT);",
    DeliveryConfirmStatus_CREATE = "CREATE TABLE IF NOT EXISTS DeliveryConfirmStatus(Status TEXT);",
            SKULiveInventory_CREATE = "CREATE TABLE IF NOT EXISTS SKULiveInventory(Id TEXT, Name TEXT, Quantity TEXT);",
            RawMaterialLiveInventory_CREATE = "CREATE TABLE IF NOT EXISTS RawMaterialLiveInventory(Id TEXT, Name TEXT, Quantity TEXT);",
            OutletLedger_CREATE = "CREATE TABLE IF NOT EXISTS OutletLedger(Id TEXT, Quantity TEXT);",
            ExpenseHead_CREATE = "CREATE TABLE IF NOT EXISTS ExpenseHead(Id TEXT, Name TEXT, NameLocal TEXT);",
    OutletPaymentReceipt_CREATE ="CREATE TABLE IF NOT EXISTS OutletPaymentReceipt(Id INTEGER PRIMARY KEY AUTOINCREMENT,CustomerId TEXT, Amount TEXT, AndroidDate TEXT, UniqueId TEXT, IsSync TEXT);",
    ExpenseBooking_CREATE ="CREATE TABLE IF NOT EXISTS ExpenseBooking(Id INTEGER PRIMARY KEY AUTOINCREMENT,CustomerId TEXT, ExpenseHeadId TEXT, Amount TEXT, Remarks TEXT, AndroidDate TEXT, UniqueId TEXT, IsSync TEXT);";

    // Context of the application using the database.
    private final Context context;

    /********************* End of Tables used in new Complaint/ feedback ******************/

    // Variable to hold the database instance
    public SQLiteDatabase db;
    ContentValues newValues = null;
    HashMap<String, String> map = null;
    String userlang;
    private String result = null;
    private Cursor cursor;
    private ArrayList<HashMap<String, String>> wordList = null;
    private String selectQuery = null;
    private UserSessionManager session;
    // Database open/upgrade helper
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context _context) {
        context = _context;
        dbHelper = new DatabaseHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
        session = new UserSessionManager(_context);
        userlang = session.getDefaultLang();
    }

    public DatabaseAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    // Enable Read mode
    public DatabaseAdapter openR() throws SQLException {
        db = dbHelper.getReadableDatabase();
        return this;
    }

    // Close Database
    public void close() {
        db.close();
    }

    // Close Database
    public boolean isOpen() {
        return db.isOpen();
    }

    // Application Version
    public String getVersion() {
        return "V20170707";
    }

    // Open Database
    public SQLiteDatabase getDatabaseInstance() {
        return db;
    }

    // To insert exceptions into Exceptions table
    public String insertExceptions(String message, String activityName,
                                   String methodName) {
        result = "fail";
        newValues = new ContentValues();

        String PhoneModel = android.os.Build.MODEL;
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        String deviceMan = android.os.Build.MANUFACTURER;

        newValues.put("Message", message);
        newValues.put("ActivityName", activityName);
        newValues.put("CalledMethod", methodName);
        newValues.put("CreatedOn", getDateTime());
        newValues.put("PhoneInfo", deviceMan + " " + PhoneModel + " "
                + AndroidVersion);
        db = dbHelper.getWritableDatabase();
        db.insert("Exceptions", null, newValues);
        result = "success";
        // cursor.close();
        return result;
    }

    // To get error list
    public ArrayList<HashMap<String, String>> getErrorList() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Message, ActivityName, CalledMethod, PhoneInfo, CreatedOn FROM Exceptions ORDER BY CAST(Id AS INT) COLLATE NOCASE ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            HashMap<String, String> user = session.getLoginUserDetails();
            map.put("UserId", user.get(UserSessionManager.KEY_ID));
            map.put("ErrorMessage", cursor.getString(1));
            map.put("ActivityName", cursor.getString(2));
            map.put("CalledMethod", cursor.getString(3));
            map.put("PhoneInfo", cursor.getString(4));
            map.put("CreateOn", cursor.getString(5));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To delete errors
    public void deleteErrors() {
        selectQuery = "DELETE FROM Exceptions";
        db.execSQL(selectQuery);
    }

    public void deleteCentres() {
        selectQuery = "DELETE FROM Centre";
        db.execSQL(selectQuery);
    }

    public void deleteMISCompany() {
        selectQuery = "DELETE FROM MISCompany";
        db.execSQL(selectQuery);
    }

    // To get product list
    public ArrayList<HashMap<String, String>> getItems() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Name, Type, Rate, (CASE WHEN ProductLocal='' THEN Name ELSE ProductLocal END) FROM Item ORDER BY LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("Type", cursor.getString(2));
            map.put("Rate", cursor.getString(3));
            map.put("ProductLocal", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    /****** Get All Required Masters for customer registration ************/
    public List<CustomType> GetMasterDetails(String masterType, String filter) {
        List<CustomType> labels = new ArrayList<CustomType>();
        if (masterType == "customertype")
            selectQuery = "SELECT Id, Name FROM CustomerType ORDER BY LOWER(Name)";
        else if (masterType == "centre")
            selectQuery = "SELECT Id, Name FROM Centre ORDER BY LOWER(Name)";
        else if (masterType == "miscompany")
            selectQuery = "SELECT Id, Name FROM MISCompany ORDER BY LOWER(Name)";
        else if (masterType == "country")
            selectQuery = "SELECT Id, Name FROM Country ORDER BY Name";
        else if (masterType == "state")
            selectQuery = "SELECT Id, Name FROM State WHERE CountryId='"
                    + filter + "' ORDER BY LOWER(Name)";
        else if (masterType == "city")
            selectQuery = "SELECT Id, Name FROM City WHERE StateId ='" + filter
                    + "' ORDER BY LOWER(Name)";
        else if (masterType == "pincode")
            selectQuery = "SELECT Id, Name FROM PinCode WHERE CityId ='"
                    + filter + "' ORDER BY LOWER(Name)";
        else if (masterType == "complaintcategory")
            selectQuery = "SELECT Id, Name FROM ComplaintCategory ORDER BY LOWER(Name)";
        else if (masterType == "company")
            selectQuery = "SELECT Id, Name FROM Company ORDER BY LOWER(Name)";
        else if (masterType == "route")
            selectQuery = "SELECT Id||'~'||CentreId, Name FROM Route ORDER BY LOWER(Name)";
        else if (masterType == "routeonly")
            selectQuery = "SELECT Id, Name FROM Route ORDER BY LOWER(Name)";
        else if (masterType == "bank")
            selectQuery = "SELECT Id, Name FROM Bank ORDER BY LOWER(Name)";
        else if (masterType == "vehicle")
            selectQuery = "SELECT Id, Name FROM Vehicle WHERE RouteId ='"
                    + filter + "' ORDER BY LOWER(Name)";
        else if (masterType == "customer")
            selectQuery = "SELECT CustomerId, Customer, CustomerLocal FROM CustomerMaster ORDER BY LOWER(Customer)";
        else if (masterType == "customerByRoute")
            selectQuery = "SELECT CustomerId||'~'||RouteId||'~'||Route, Customer, CustomerLocal FROM CustomerMaster WHERE RouteId ='"
                    + filter + "' ORDER BY LOWER(Customer)";
        else if (masterType == "otherCustomerByRoute")
            selectQuery = "SELECT CustomerId||'~'||RouteId||'~'||Route, Customer, CustomerLocal FROM CustomerMaster WHERE RouteId !='"
                    + filter + "' ORDER BY LOWER(Customer)";
        //Log.i("LPDND", "selectQuery="+selectQuery);
        cursor = db.rawQuery(selectQuery, null);
        if (masterType == "customer" || masterType == "customerByRoute" || masterType == "otherCustomerByRoute")
            labels.add(new CustomType("0", "...Select Customer"));
        else if (masterType == "route")
            labels.add(new CustomType("0", "...Select Route"));
        else if (masterType == "bank")
            labels.add(new CustomType("0", "...Select Bank"));
        else if (masterType == "routeonly")
            labels.add(new CustomType("0", "...Select Route"));
        else if (masterType == "company")
            labels.add(new CustomType("0", "...Select Company"));
        else if (masterType == "centre")
            labels.add(new CustomType("0", "...Select Centre"));
        else if (masterType == "miscompany")
            labels.add(new CustomType("0", "...Select Company"));
        else
            labels.add(new CustomType("0", "...Select"));
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }


    public List<CustomType> GetCustomerMasterDetails(String masterType, String filter) {
        List<CustomType> labels = new ArrayList<CustomType>();
        switch (masterType) {
            case "rawmaterial":
                if (userlang.equalsIgnoreCase("en"))
                    selectQuery = "SELECT Id, Name||' '||Uom FROM RawMaterialMaster ORDER BY Name COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT Id, NameLocal||' '||Uom FROM RawMaterialMaster ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "rawmaterialinv":
                if (userlang.equalsIgnoreCase("en"))
                    selectQuery = "SELECT Id, Name FROM RawMaterialLiveInventory ORDER BY Name COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT Id, Name FROM RawMaterialLiveInventory ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "sku":
                if (userlang.equalsIgnoreCase("en"))
                    selectQuery = "SELECT Id||'~'||SKU, Name FROM SKUMaster ORDER BY Name COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT Id||'~'||SKU, NameLocal FROM SKUMaster ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "skuinv":
                if (userlang.equalsIgnoreCase("en"))
                    selectQuery = "SELECT Id, Name FROM SKULiveInventory ORDER BY Name COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT Id, Name FROM SKULiveInventory ORDER BY Name COLLATE NOCASE ASC";
                break;
            case "exphead":
                if (userlang.equalsIgnoreCase("en"))
                    selectQuery = "SELECT Id, Name FROM ExpenseHead WHERE Id !='1' ORDER BY Name COLLATE NOCASE ASC";
                else
                    selectQuery = "SELECT Id, NameLocal FROM ExpenseHead WHERE Id !='1' ORDER BY Name COLLATE NOCASE ASC";
                break;
        }
        cursor = db.rawQuery(selectQuery, null);
        if (userlang.equalsIgnoreCase("en")) {
            if (masterType.equalsIgnoreCase("rawmaterial"))
                labels.add(new CustomType("0", "...Select Raw Material"));
            else if (masterType.equalsIgnoreCase("rawmaterialinv"))
                labels.add(new CustomType("0", "...Select Raw Material"));
            else if (masterType.equalsIgnoreCase("sku"))
                labels.add(new CustomType("0~0", "...Select SKU"));
            else if (masterType.equalsIgnoreCase("skuinv"))
                labels.add(new CustomType("0-0", "...Select SKU"));
            else if (masterType.equalsIgnoreCase("exphead"))
                labels.add(new CustomType("0", "...Select Expense Head"));
            else
                labels.add(new CustomType("0", "...Select"));
        } else {
            if (masterType.equalsIgnoreCase("rawmaterial"))
                labels.add(new CustomType("0", "...कच्ची सामग्री चयन करें"));
            else if (masterType.equalsIgnoreCase("rawmaterialinv"))
                labels.add(new CustomType("0", "...कच्ची सामग्री चयन करें"));
            else if (masterType.equalsIgnoreCase("sku"))
                labels.add(new CustomType("0~0", "...उत्पाद चयन करें"));
            else if (masterType.equalsIgnoreCase("skuinv"))
                labels.add(new CustomType("0-0", "...उत्पाद चयन करें"));
            else if (masterType.equalsIgnoreCase("exphead"))
                labels.add(new CustomType("0", "...व्यय हेड चयन करें"));
            else
                labels.add(new CustomType("0", "...चयन करें"));
        }

        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }

    public List<CustomType> GetDeliveryDetailsByLang(String masterType, String filter, String langOption) {
        List<CustomType> labels = new ArrayList<CustomType>();
        if (masterType == "customer" && langOption.equalsIgnoreCase("hi"))
            selectQuery = "SELECT DISTINCT mas.CustomerId,(CASE WHEN mas.CustomerLocal='' THEN mas.Customer ELSE mas.CustomerLocal END) FROM CustomerMaster mas, Delivery del WHERE mas.CustomerId = del.CustomerId ORDER BY LOWER(mas.Customer)";
        else if (masterType == "customer" && langOption.equalsIgnoreCase("en"))
            selectQuery = "SELECT DISTINCT mas.CustomerId, mas.Customer FROM CustomerMaster mas, Delivery del WHERE mas.CustomerId = del.CustomerId ORDER BY LOWER(mas.Customer)";
        //Log.i("LPDND", "selectQuery="+masterType+":"+selectQuery);
        cursor = db.rawQuery(selectQuery, null);

        if ((masterType == "customer" || masterType == "customerByRoute" || masterType == "otherCustomerByRoute") && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Customer"));
        else if ((masterType == "customer" || masterType == "customerByRoute" || masterType == "otherCustomerByRoute") && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...ग्राहक चुनें"));
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }

    public List<CustomType> GetMasterDetailsByLang(String masterType, String filter, String langOption) {
        List<CustomType> labels = new ArrayList<CustomType>();
        if (masterType == "customer" && langOption.equalsIgnoreCase("hi"))
            selectQuery = "SELECT CustomerId,(CASE WHEN CustomerLocal='' THEN Customer ELSE CustomerLocal END) FROM CustomerMaster ORDER BY LOWER(Customer)";
        else if (masterType == "customer" && langOption.equalsIgnoreCase("en"))
            selectQuery = "SELECT CustomerId, Customer FROM CustomerMaster ORDER BY LOWER(Customer)";
        else if (masterType == "customerByRoute" && langOption.equalsIgnoreCase("en"))
            selectQuery = "SELECT CustomerId||'~'||RouteId||'~'||Route, Customer FROM CustomerMaster WHERE RouteId ='"
                    + filter + "' ORDER BY LOWER(Customer)";
        else if (masterType == "customerByRoute" && langOption.equalsIgnoreCase("hi"))
            selectQuery = "SELECT CustomerId||'~'||RouteId||'~'||Route, (CASE WHEN CustomerLocal='' THEN Customer ELSE CustomerLocal END) FROM CustomerMaster WHERE RouteId ='"
                    + filter + "' ORDER BY LOWER(Customer)";

        else if (masterType == "otherCustomerByRoute" && langOption.equalsIgnoreCase("en"))
            selectQuery = "SELECT CustomerId||'~'||RouteId||'~'||Route, Customer FROM CustomerMaster WHERE RouteId !='"
                    + filter + "' ORDER BY LOWER(Customer)";
        else if (masterType == "otherCustomerByRoute" && langOption.equalsIgnoreCase("hi"))
            selectQuery = "SELECT CustomerId||'~'||RouteId||'~'||Route, (CASE WHEN CustomerLocal='' THEN Customer ELSE CustomerLocal END) FROM CustomerMaster WHERE RouteId !='"
                    + filter + "' ORDER BY LOWER(Customer)";

        else if (masterType == "customer")
            selectQuery = "SELECT CustomerId, Customer FROM CustomerMaster ORDER BY LOWER(Customer)";
        else if (masterType == "customer" && langOption.equalsIgnoreCase("en"))
            selectQuery = "SELECT CustomerId, Customer FROM CustomerMaster ORDER BY LOWER(Customer)";
        else if (masterType == "customer" && langOption.equalsIgnoreCase("hi"))
            selectQuery = "SELECT CustomerId, Customer, (CASE WHEN CustomerLocal='' THEN Customer ELSE CustomerLocal END) FROM CustomerMaster ORDER BY LOWER(Customer)";


        else if (masterType == "company")
            selectQuery = "SELECT Id, Name FROM Company ORDER BY LOWER(Name)";

        //Log.i("LPDND", "selectQuery="+masterType+":"+selectQuery);
        cursor = db.rawQuery(selectQuery, null);

        if ((masterType == "customer" || masterType == "customerByRoute" || masterType == "otherCustomerByRoute") && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Customer"));
        else if ((masterType == "customer" || masterType == "customerByRoute" || masterType == "otherCustomerByRoute") && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...ग्राहक चुनें"));
        else if (masterType == "route" && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Route"));
        else if (masterType == "route" && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...मार्ग चुनें"));

        else if (masterType == "bank" && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Bank"));
        else if (masterType == "bank" && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...बैंक चुनें"));

        else if (masterType == "routeonly" && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Route"));
        else if (masterType == "routeonly" && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...मार्ग चुनें"));

        else if (masterType == "company" && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Company"));
        else if (masterType == "company" && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...कंपनी चुनें"));
        else
            labels.add(new CustomType("0", "...Select"));
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }

    /****** End of Get All Required Masters for customer registration ************/

    //To insert Customer records
    public String Insert_Customer(String custTypeId, String loginId, String custName, String contactPerson, String mobile, String street, String houseNo, String landMark, String emailId, String stateId, String cityId, String pinCodeId, String ipAddr, String machine) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CustTypeId", custTypeId);
            newValues.put("LoginId", loginId);
            newValues.put("CustName", custName);
            newValues.put("ContactPerson", contactPerson);
            newValues.put("Mobile", mobile);
            newValues.put("Street", street);
            newValues.put("HouseNo", houseNo);
            newValues.put("LandMark", landMark);
            newValues.put("EmailId", emailId);
            newValues.put("StateId", stateId);
            newValues.put("CityId", cityId);
            newValues.put("PinCodeId", pinCodeId);
            newValues.put("CreateDate", getDateTime());
            newValues.put("IpAddr", getDateTime());
            newValues.put("Machine", getDateTime());
            long custId = db.insert("CustomerRegistration", null, newValues);
            result = "success~" + custId;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /******End of Get All Required Masters for customer registration************/

    /******Stock Return************/
    //To insert stock return records
    public String Insert_StockReturn(String uniqueId, String routeId, String vehicleId, String returnDate, String createBy) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("RouteId", routeId);
            newValues.put("VehicleId", vehicleId);
            newValues.put("ReturnDate", getDateTime());
            newValues.put("CreateBy", createBy);
            newValues.put("isSync", "0");

            long returnId = db.insert("StockReturn", null, newValues);
            result = "success~" + returnId;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert Centre details records
    public String Insert_Centre(String Id, String Name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", Id);
            newValues.put("Name", Name);

            db.insert("Centre", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //To insert MIS Company details records
    public String Insert_MISCompany(String Id, String Name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", Id);
            newValues.put("Name", Name);

            db.insert("MISCompany", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert stock return details records
    public String Insert_StockReturnDetails(String returnId, String skuId, String returnQty, String leakageQty, String sku) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("ReturnId", returnId);
            newValues.put("SKUId", skuId);
            newValues.put("ReturnQty", returnQty);
            newValues.put("LeakageQty", leakageQty);
            newValues.put("SKU", sku);
            db.insert("StockReturnDetails", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //To get all stock return for summary page
    public ArrayList<HashMap<String, String>> getStockReturnSummary() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT strftime('%Y-%m-%d', sr.ReturnDate) AS ReturnDate, rt.Name As RouteNo, sr.RouteId FROM StockReturn sr, Route rt WHERE sr.RouteId = rt.Id ORDER BY strftime('%Y-%m-%d', sr.ReturnDate), rt.Name";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("ReturnDate", cursor.getString(0));
            map.put("RouteNo", cursor.getString(1));
            map.put("RouteId", cursor.getString(2));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //To get all stock return details
    public ArrayList<HashMap<String, String>> getStockReturnSummaryDetails(String retDate, String routeId) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT det.SKU, SUM(det.ReturnQty), SUM(det.LeakageQty) FROM StockReturnDetails det, StockReturn sr WHERE det.ReturnId = sr.Id AND strftime('%d-%m-%Y', '" + retDate + "') -strftime('%d-%m-%Y',ReturnDate) =0 AND sr.RouteId = '" + routeId + "' GROUP BY det.SKU";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("SKUName", cursor.getString(0));
            map.put("ReturnQty", cursor.getString(1));
            map.put("LeakageQty", cursor.getString(2));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }


    //To get all stock return before sync
    public ArrayList<HashMap<String, String>> getStockReturn() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, UniqueId, RouteId, VehicleId, ReturnDate, CreateBy FROM StockReturn WHERE isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("UniqueId", cursor.getString(1));
            map.put("RouteId", cursor.getString(2));
            map.put("VehicleId", cursor.getString(3));
            map.put("ReturnDate", cursor.getString(4));
            map.put("CreateBy", cursor.getString(5));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //To get all stock return details
    public ArrayList<HashMap<String, String>> getStockReturnDetails() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT det.ReturnId, det.SKUId, det.ReturnQty, det.LeakageQty, sr.UniqueId FROM StockReturnDetails det, StockReturn sr WHERE sr.Id = det.ReturnId AND sr.isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("ReturnId", cursor.getString(0));
            map.put("SKUId", cursor.getString(1));
            map.put("ReturnQty", cursor.getString(2));
            map.put("LeakageQty", cursor.getString(3));
            map.put("UniqueId", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //Method to get vehicle Id with Name for route id
    public String getVehicleByRouteId(String routeId) {
        String vehicleIdWithName = "";
        selectQuery = "SELECT Id, Name FROM Vehicle WHERE RouteId= '" + routeId + "' ";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0).equals("0"))
                    vehicleIdWithName = "";
                else
                    vehicleIdWithName = String.valueOf(cursor.getString(0)) + "~" + String.valueOf(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        return vehicleIdWithName;
    }

    //To get Route Stock list
    public ArrayList<HashMap<String, String>> getRouteStock(String routeId) {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT SKUId, SKU, AvailQty, PackingType, ProductLocal FROM DeliveryInput WHERE  CAST(AvailQty AS NUMERIC) > 0 ORDER BY LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("SKUId", cursor.getString(0));
            map.put("SKUName", cursor.getString(1));
            map.put("AvailQty", cursor.getString(2));
            map.put("SKU", cursor.getString(3));
            map.put("SKUNameLocal", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    /******End of Stock Return************/


    //To Get default demand date
    public String getDemandDate() {
        String demandDate = "";
        selectQuery = "SELECT Date FROM DemandDate ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                demandDate = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return demandDate;
    }

    //To Get demand cut off time
    public String getDemandCutOff() {
        String demandDate = "";
        selectQuery = "SELECT DateTimeCurrentTime FROM DemandCutOff";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                demandDate = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return demandDate;
    }

    //To Update route stock
    public String UpdateRouteStock(String routeId, String skuId, String qty) {
        try {
            db.execSQL("UPDATE DeliveryInput SET AvailQty = AvailQty - " + Double.parseDouble(qty) + " WHERE RouteId = '" + routeId + "' AND SkuId ='" + skuId + "'");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To Update Stock Return details
    public String Update_StockReturnDetails(String id) {
        try {
            String query = "UPDATE StockReturn SET IsSync = '1' WHERE IsSync = '0' AND Id = '" + id + "'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //deleting stock return data
    public void DeleteStockReturn() {

        db.execSQL("DELETE FROM StockReturnDetails ");
        db.execSQL("DELETE FROM StockReturn ");
    }

    //To insert demand Date records
    public String Insert_DemandDate(String date) {
        db.execSQL("DELETE FROM DemandDate");
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Date", date);
            db.insert("DemandDate", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert demand cutoff time
    public String Insert_DemandCutOff(String dateTimeCurrentTime) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("DateTimeCurrentTime", dateTimeCurrentTime);
            db.insert("DemandCutOff", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //To insert route vehicle master
    public String Insert_RouteVehicleMaster(String routeId, String route, String vehicleId, String vehicle, String capacity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("RouteId", routeId);
            newValues.put("Route", route);
            newValues.put("VehicleId", vehicleId);
            newValues.put("Vehicle", vehicle);
            newValues.put("Capacity", capacity);
            db.insert("RouteVehicleMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert customer master
    public String Insert_CustomerMaster(String routeId, String route, String customerId, String customer, String mobile, String customerType, String loginId, String customerLocal) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("RouteId", routeId);
            newValues.put("Route", route);
            newValues.put("CustomerId", customerId);
            newValues.put("Customer", customer);
            newValues.put("Mobile", mobile);
            newValues.put("CustomerType", customerType);
            newValues.put("LoginId", loginId);
            newValues.put("CustomerLocal", customerLocal);
            db.insert("CustomerMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To update demand Date records
    public String Update_DemandDate(String date) {
        try {
            String query = "UPDATE DemandDate SET Date = '" + date + "'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert delivery records
    public String Insert_Delivery(String uniqueId, String routeId, String centreId,
                                  String route, String customerId,
                                  String customer, String createBy, String imei, String vehicleId, String deliverTo) {
        //Log.i("LPDND Insert_Delivery", "centreId="+centreId+"vehicleId="+vehicleId+"routeId="+routeId+"customerId="+customerId);
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("RouteId", routeId);
            newValues.put("Route", route);
            newValues.put("CentreId", centreId);
            newValues.put("CustomerId", customerId);
            newValues.put("Customer", customer);
            newValues.put("CreateBy", createBy);
            newValues.put("CreateDate", getDateTime());
            newValues.put("Imei", imei);
            newValues.put("IsSync", "0");
            newValues.put("VehicleId", vehicleId);
            newValues.put("DeliverTo", deliverTo);

            long id = db.insert("Delivery", null, newValues);
            result = "success~" + id + "~" + uniqueId;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert delivery detail records
    public String Insert_DeliveryDetail(String deliveryId, String skuId,
                                        String sku, String rate, String dQty, String availQty, String delQty, String customerId, String companyId, String routeId) {
        //Log.i("LPDND Insert_DeliveryDetail", "skuId="+skuId+"delQty="+delQty+"routeId="+routeId+"customerId="+customerId+"companyId="+companyId);
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("DeliveryId", deliveryId);
            newValues.put("SkuId", skuId);
            newValues.put("Sku", sku);
            newValues.put("Rate", rate);
            newValues.put("DQty", dQty);
            newValues.put("AvailQty", availQty);
            newValues.put("DelQty", delQty);
            newValues.put("CompanyId", companyId);

            long id = db.insert("DeliveryDetail", null, newValues);
            result = "success~" + id;
            db.execSQL("UPDATE CustomerLedger SET Balance = Balance - " + Double.parseDouble(rate) * Double.parseDouble(delQty) + " WHERE CustomerId = '" + customerId + "' AND CompanyId ='" + companyId + "'");
            db.execSQL("UPDATE DeliveryInput SET AvailQty = AvailQty - " + delQty + " WHERE SkuId ='" + skuId + "'");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert route records
    public String Insert_Route(String id, String name, String centreId) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("CentreId", centreId);
            db.insert("Route", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert delivery input demand records
    public String Insert_DeliveryInputDemand(String customerId, String skuId, String demandQty) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CustomerId", customerId);
            newValues.put("SKUId", skuId);
            newValues.put("DQty", demandQty);
            db.insert("DeliveryInputDemand", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert delivery input records
    public String Insert_DeliveryInput(String skuId, String sku,
                                       String packingType, String availQty, String companyId, String routeId, String product, String skuUnit, String uom, String productLocal, String mulFactor) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("SkuId", skuId);
            newValues.put("Sku", sku);
            newValues.put("PackingType", packingType);
            newValues.put("AvailQty", availQty);
            newValues.put("CompanyId", companyId);
            newValues.put("RouteId", routeId);
            newValues.put("Product", product);
            newValues.put("SkuUnit", skuUnit);
            newValues.put("Uom", uom);
            newValues.put("ProductLocal", productLocal);
            newValues.put("MulFactor", mulFactor);
            db.insert("DeliveryInput", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To get delivery input list
    public ArrayList<HashMap<String, String>> getDeliveryInput(String customerId) {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT del.SkuId, del.Sku, del.PackingType, IFNULL(rt.Rate, crt.Rate), IFNULL(dem.DQty, 0), del.AvailQty, del.CompanyId, del.ProductLocal, del.MulFactor FROM DeliveryInput del LEFT OUTER JOIN DeliveryInputDemand dem ON del.SkuId = dem.SKUId AND dem.CustomerId = '" + customerId + "' LEFT OUTER JOIN CustomerRate rt ON  del.SkuId = rt.SKUId AND rt.CustomerId = '" + customerId + "'  LEFT OUTER JOIN CompanyRate crt ON  del.SkuId = crt.SKUId WHERE CAST(del.AvailQty AS NUMERIC)>0  ORDER BY LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        //Log.i("LPDND", "selectQuery="+selectQuery);
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("id", cursor.getString(0));
            map.put("sku", cursor.getString(1));
            map.put("type", cursor.getString(2));
            map.put("rate", cursor.getString(3));
            map.put("dqty", Double.parseDouble(cursor.getString(4).replace(".0", "")) == 0 ? "-" : cursor.getString(4).replace(".0", ""));
            map.put("aqty", cursor.getString(5).replace(".0", ""));
            map.put("companyId", cursor.getString(6));
            map.put("productLocal", cursor.getString(7));
            map.put("mulFactor", cursor.getString(8));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To insert demand records
    public String Insert_Demand(String uniqueId, String customerId, String customer,
                                String demandDate, String role) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("CustomerId", customerId);
            newValues.put("Customer", customer);
            newValues.put("CreateDate", getDateTime());
            newValues.put("DemandDate", demandDate);
            newValues.put("Role", role);
            newValues.put("isSync", "0");

            long orderId = db.insert("Demand", null, newValues);
            result = "success~" + orderId;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert bank records
    public String Insert_Bank(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("Bank", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert vehicle records
    public String Insert_Vehicle(String id, String name, String routeId) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("RouteId", routeId);
            db.insert("Vehicle", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert product master records
    public String Insert_ProductMaster(String companyId, String company, String skuId, String sku, String packingType, String rate, String productLocal, String product, String skuUnit, String uom) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CompanyId", companyId);
            newValues.put("Company", company);
            newValues.put("SKUId", skuId);
            newValues.put("SKU", sku);
            newValues.put("PackingType", packingType);
            newValues.put("Rate", rate);
            newValues.put("ProductLocal", productLocal);
            newValues.put("Product", product);
            newValues.put("SkuUnit", skuUnit);
            newValues.put("Uom", uom);
            db.insert("ProductMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert Raw Material records
    public String Insert_RawMaterialMaster(String id, String name, String uom, String nameLocal) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("UOM", uom);
            newValues.put("NameLocal", nameLocal);

            db.insert("RawMaterialMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert Outlet Inventory records
    public String Insert_OutletInventory(String rawMaterialId, String skuId, String quantity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("RawMaterialId", rawMaterialId);
            newValues.put("SKUId", skuId);
            newValues.put("Quantity", quantity);

            db.insert("OutletInventory", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert SKU records in SKUMaster
    public String Insert_SKUMaster(String id, String name, String nameLocal, String units, String sku) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("NameLocal", nameLocal);
            newValues.put("Units", units);
            newValues.put("SKU", sku);

            db.insert("SKUMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // To insert SKU Sale Rate records in SaleRateMaster
    public String Insert_SaleRateMaster(String id, String rate, String fromDate, String toDate) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Rate", rate);
            newValues.put("FromDate", fromDate);
            newValues.put("ToDate", toDate);

            db.insert("SaleRateMaster", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // To insert Primary Receipt Data records in Primary Receipt And Inventory Table
    public String Insert_PrimaryReceipt(String customerId, String materialId, String skuId, String quantity, String amount) {
        try {
            result = "fail";
            newValues = new ContentValues();

            newValues.put("UniqueId", UUID.randomUUID().toString());
            newValues.put("CustomerId", customerId);
            newValues.put("MaterialId", materialId);
            newValues.put("SKUId", skuId);
            newValues.put("Quantity", quantity);
            newValues.put("Amount", amount);
            newValues.put("CreateDate", getDateTime());

            db.insert("OutletPrimaryReceipt", null, newValues);

            Boolean dataExists = false;
            selectQuery = "SELECT Id FROM OutletInventory WHERE RawMaterialId = '" + materialId + "' AND SKUId = '" + skuId + "' ";
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.getCount() > 0) {
                dataExists = true;
            }
            cursor.close();
            if (dataExists.equals(false)) {
                newValues = new ContentValues();
                newValues.put("RawMaterialId", materialId);
                newValues.put("SKUId", skuId);
                newValues.put("Quantity", quantity);

                db.insert("OutletInventory", null, newValues);
            } else {
                selectQuery = "UPDATE OutletInventory SET Quantity=Quantity+'" + quantity + "'   WHERE RawMaterialId = '" + materialId + "' AND SKUId = '" + skuId + "' ";
                db.execSQL(selectQuery);
            }
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert company records
    public String Insert_Company(String id, String name, String shortName) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("ShortName", shortName);
            db.insert("Company", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert demand records
    public String Insert_ViewDemand(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("ViewDemand", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert demand details records
    public String Insert_ViewDemandDetails(String id, String item,
                                           String demandQty, String packingType, String rate, String productLocal, String product, String skuUnit, String uom) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Item", item);
            newValues.put("DemandQty", demandQty);
            newValues.put("PackingType", packingType);
            newValues.put("Rate", rate);
            newValues.put("ProductLocal", productLocal);
            newValues.put("Product", product);
            newValues.put("SkuUnit", skuUnit);
            newValues.put("Uom", uom);
            db.insert("ViewDemandDetails", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To insert demand details records
    public String Insert_DemandDetails(String orderId, String itemId,
                                       String item, String qty, String rate) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("DemandId", orderId);
            newValues.put("ItemId", itemId);
            newValues.put("Item", item);
            newValues.put("Qty", qty);
            newValues.put("Rate", rate);
            db.insert("DemandDetails", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To get all unsync demands
    public ArrayList<HashMap<String, String>> getAllDemand() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, CustomerId, CreateDate, UniqueId, Role FROM Demand WHERE isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("CustomerId", cursor.getString(1));
            map.put("CreateDate", cursor.getString(2));
            map.put("UniqueId", cursor.getString(3));
            map.put("Role", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all unsync delivery
    public ArrayList<HashMap<String, String>> getUnSyncDelivery() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, UniqueId, CentreId, RouteId, CustomerId, CreateBy, CreateDate, Imei, VehicleId, DeliverTo FROM Delivery WHERE isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("UniqueId", cursor.getString(1));
            map.put("CentreId", cursor.getString(2));
            map.put("RouteId", cursor.getString(3));
            map.put("CustomerId", cursor.getString(4));
            map.put("CreateBy", cursor.getString(5));
            map.put("CreateDate", cursor.getString(6));
            map.put("Imei", cursor.getString(7));
            map.put("VehicleId", cursor.getString(8));
            map.put("DeliverTo", cursor.getString(9));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all unsync delivery details
    public ArrayList<HashMap<String, String>> getUnSyncDeliveryDetail() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT main.UniqueId, det.SkuId, det.Rate, det.DelQty, det.CompanyId FROM DeliveryDetail det, Delivery main WHERE main.Id = det.DeliveryId ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SkuId", cursor.getString(1));
            map.put("Rate", cursor.getString(2));
            map.put("Qty", cursor.getString(3));
            map.put("CompanyId", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }


    //To get all unsync payment
    public ArrayList<HashMap<String, String>> getUnSyncPayment() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, UniqueId, CustomerId, CreateBy, CreateDate, DeliveryUniqueId FROM CustomerPaymentMaster WHERE isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("UniqueId", cursor.getString(1));
            map.put("CustomerId", cursor.getString(2));
            map.put("CreateBy", cursor.getString(3));
            map.put("CreateDate", cursor.getString(4));
            map.put("DeliveryUniqueId", cursor.getString(5));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //To get unsync attachment for sync
    public ArrayList<HashMap<String, String>> getAttachmentsForSync() {

        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();

        selectQuery = "SELECT UniqueId, ImageName, ImagePath FROM CustomerPaymentDetail WHERE IsSync IS NULL ";

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("UploadFileName", cursor.getString(1));
            map.put("ImagePath", cursor.getString(2));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //Method to Update Attachment Sync Status
    public void updateAttachmentStatus(String uniqueId) {
        selectQuery = "UPDATE CustomerPaymentDetail SET IsSync = 1 WHERE UniqueId ='" + uniqueId + "'";
        db.execSQL(selectQuery);
    }

    //Method to delete payment collection
    public void deletePaymentCollection() {
        db.execSQL("Delete FROM CustomerPaymentMaster;");
        db.execSQL("Delete FROM CustomerPaymentDetail;");
    }

    //To get all unsync payment details
    public ArrayList<HashMap<String, String>> getUnSyncPaymentDetail() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT det.CompanyId, det.BankId, det.ChequeNumber, det.Amount, det.UniqueId, det.ImageName, mas.UniqueId,det.Remarks FROM CustomerPaymentMaster mas, CustomerPaymentDetail det WHERE mas.Id = det.MasterId AND det.isSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CompanyId", cursor.getString(0));
            map.put("BankId", cursor.getString(1));
            map.put("ChequeNumber", cursor.getString(2));
            map.put("Amount", cursor.getString(3));
            map.put("UniqueId", cursor.getString(4));
            map.put("ImageName", cursor.getString(5));
            map.put("MasterUniqueId", cursor.getString(6));
            map.put("Remarks", cursor.getString(7));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }


    // To get all demands after sync
    public ArrayList<HashMap<String, String>> getAllSyncDemand() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT od.Code, od.CreateDate, det.Item, det.Qty, det.Type FROM DemandDetails det, Demand od WHERE od.Id = det.DemandId AND od.isSync='1' AND det.Qty > 0 ORDER BY CAST(od.Id AS INT) DESC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Code", cursor.getString(0));
            map.put("CreateDate", cursor.getString(1));
            map.put("Item", cursor.getString(2));
            map.put("Qty", cursor.getString(3));
            map.put("Type", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

	/*// To get all delivery
    public ArrayList<HashMap<String, String>> getDelivery() {
		ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
		selectQuery = "SELECT DISTINCT CustomerId, Customer, SUBSTR(CreateDate,0,11) FROM Delivery ORDER BY SUBSTR(CreateDate,0,11) DESC, LOWER(Customer)";
		cursor = db.rawQuery(selectQuery, null);
		while (cursor.moveToNext()) {
			map = new HashMap<String, String>();
			map.put("Id", cursor.getString(0));
			map.put("Name", cursor.getString(1));
			map.put("Date", cursor.getString(2));
			wordList.add(map);
		}
		cursor.close();
		return wordList;
	}*/

    // To get all delivery & payment details demand
    public ArrayList<HashMap<String, String>> getDeliveryPaymentDetails(String id, String date) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT dd.Sku,  SUM(dd.DQty), SUM(dd.DelQty), SUM(dd.DelQty)*dd.Rate FROM DeliveryDetail dd, (SELECT DISTINCT Id FROM Delivery WHERE Customerid ='" + id + "' AND SUBSTR(CreateDate,0,11)='" + date + "') de WHERE de.Id = dd.DeliveryId GROUP BY SKUID ORDER BY LOWER(dd.Sku)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Item", cursor.getString(0));
            map.put("DQty", (cursor.getString(1).replace(".0", "")).replace("0", "-"));
            map.put("DelQty", cursor.getString(2));
            map.put("Amount", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all delivery details
    public ArrayList<HashMap<String, String>> getDeliveryDetails(String id, String date) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT dd.Sku, SUM(dd.DelQty), dd.Rate, SUM(dd.DelQty)*dd.Rate, SUM(dd.DQty) FROM DeliveryDetail dd, (SELECT DISTINCT Id FROM Delivery WHERE Customerid ='" + id + "' AND SUBSTR(CreateDate,0,11)='" + date + "') de WHERE de.Id = dd.DeliveryId GROUP BY SKUID ORDER BY LOWER(dd.Sku)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Item", cursor.getString(0));
            map.put("DelQty", cursor.getString(1));
            map.put("Rate", cursor.getString(2));
            map.put("Amount", cursor.getString(3));
            map.put("DQty", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all demand
    public ArrayList<HashMap<String, String>> getViewDemand() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT Id, Name FROM ViewDemand ORDER BY LOWER(Name)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all product for customer
    public ArrayList<HashMap<String, String>> getProductMaster(String lang) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        if (lang.equalsIgnoreCase("hi"))
            selectQuery = "SELECT DISTINCT Company, (CASE WHEN ProductLocal='' THEN SKU ELSE ProductLocal END), Rate FROM ProductMaster ORDER BY LOWER(Company), LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        else
            selectQuery = "SELECT DISTINCT Company, SKU, Rate FROM ProductMaster ORDER BY LOWER(Company), LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Company", cursor.getString(0));
            map.put("Sku", cursor.getString(1));
            map.put("Rate", cursor.getString(2));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }


    // To get all product for route officer
    public ArrayList<HashMap<String, String>> getProductMasterRouteOfficer(String companyId, String customerId, String routeId, String lang) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        if (!routeId.equalsIgnoreCase("0")) {
            if ((!companyId.equalsIgnoreCase("0") && customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("en"))
                selectQuery = "SELECT DISTINCT c.Route, c.Customer, p.Company, p.SKU, r.Rate FROM CustomerMaster c, ProductMaster p, CompanyRate r WHERE p.SKUId = r.SKUId AND p.CompanyId = '" + companyId + "' AND c.RouteId = '" + routeId + "' ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
            else if ((companyId.equalsIgnoreCase("0") && !customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("en"))
                selectQuery = "SELECT DISTINCT c.Route, c.Customer, p.Company, p.SKU, r.Rate FROM CustomerMaster c, ProductMaster p, CustomerRate r WHERE c.CustomerId = r.CustomerId AND p.SKUId = r.SKUId AND c.CustomerId = '" + customerId + "' AND c.RouteId = '" + routeId + "' ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
            else if ((!companyId.equalsIgnoreCase("0") && customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("hi"))
                selectQuery = "SELECT DISTINCT c.Route, c.CustomerLocal, p.Company, (CASE WHEN p.ProductLocal='' THEN p.SKU ELSE p.ProductLocal END), r.Rate FROM CustomerMaster c, ProductMaster p, CompanyRate r WHERE p.SKUId = r.SKUId AND p.CompanyId = '" + companyId + "' AND c.RouteId = '" + routeId + "' ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
            else if ((companyId.equalsIgnoreCase("0") && !customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("hi"))
                selectQuery = "SELECT DISTINCT c.Route, c.CustomerLocal, p.Company, (CASE WHEN p.ProductLocal='' THEN p.SKU ELSE p.ProductLocal END), r.Rate FROM CustomerMaster c, ProductMaster p, CustomerRate r WHERE c.CustomerId = r.CustomerId AND p.SKUId = r.SKUId AND c.CustomerId = '" + customerId + "' AND c.RouteId = '" + routeId + "' ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
        } else {
            if ((!companyId.equalsIgnoreCase("0") && customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("en"))
                selectQuery = "SELECT DISTINCT c.Route, c.Customer, p.Company, p.SKU, r.Rate FROM CustomerMaster c, ProductMaster p, CompanyRate r WHERE p.SKUId = r.SKUId AND p.CompanyId = '" + companyId + "'  ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
            else if ((companyId.equalsIgnoreCase("0") && !customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("en"))
                selectQuery = "SELECT DISTINCT c.Route, c.Customer, p.Company, p.SKU, r.Rate FROM CustomerMaster c, ProductMaster p, CustomerRate r WHERE c.CustomerId = r.CustomerId AND p.SKUId = r.SKUId AND c.CustomerId = '" + customerId + "'  ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
            else if ((!companyId.equalsIgnoreCase("0") && customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("hi"))
                selectQuery = "SELECT DISTINCT c.Route, c.CustomerLocal, p.Company, (CASE WHEN p.ProductLocal='' THEN p.SKU ELSE p.ProductLocal END), r.Rate FROM CustomerMaster c, ProductMaster p, CompanyRate r WHERE p.SKUId = r.SKUId AND p.CompanyId = '" + companyId + "'  ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
            else if ((companyId.equalsIgnoreCase("0") && !customerId.equalsIgnoreCase("0")) && lang.equalsIgnoreCase("hi"))
                selectQuery = "SELECT DISTINCT c.Route, c.CustomerLocal, p.Company, (CASE WHEN p.ProductLocal='' THEN p.SKU ELSE p.ProductLocal END), r.Rate FROM CustomerMaster c, ProductMaster p, CustomerRate r WHERE c.CustomerId = r.CustomerId AND p.SKUId = r.SKUId AND c.CustomerId = '" + customerId + "'  ORDER BY LOWER(c.Route), LOWER(c.Customer), LOWER(p.Company), LOWER(p.Product), LOWER(p.SkuUnit), LOWER(p.Uom)";
        }

        //Log.i("LPDND", "selectQuery="+selectQuery);
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Route", cursor.getString(0));
            map.put("Customer", cursor.getString(1));
            map.put("Company", cursor.getString(2));
            map.put("Sku", cursor.getString(3));
            map.put("Rate", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all vehicle
    public ArrayList<HashMap<String, String>> getVehicleMaster() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT Route, Vehicle, Capacity FROM RouteVehicleMaster ORDER BY LOWER(Route), LOWER(Vehicle)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Route", cursor.getString(0));
            map.put("Vehicle", cursor.getString(1));
            map.put("Capacity", cursor.getString(2));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all customer
    public ArrayList<HashMap<String, String>> getCustomerMaster(String routeId, String lang) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();

        if (!routeId.equalsIgnoreCase("0")) {
            if (lang.equalsIgnoreCase("hi"))
                selectQuery = "SELECT DISTINCT Route, (CASE WHEN CustomerLocal='' THEN Customer ELSE CustomerLocal END), Mobile, CustomerType, LoginId FROM CustomerMaster WHERE RouteId ='" + routeId + "' ORDER BY LOWER(Route), LOWER(Customer)";
            else
                selectQuery = "SELECT DISTINCT Route, Customer, Mobile, CustomerType, LoginId FROM CustomerMaster WHERE RouteId ='" + routeId + "' ORDER BY LOWER(Route), LOWER(Customer)";
        } else {
            if (lang.equalsIgnoreCase("hi"))
                selectQuery = "SELECT DISTINCT Route, (CASE WHEN CustomerLocal='' THEN Customer ELSE CustomerLocal END), Mobile, CustomerType, LoginId FROM CustomerMaster ORDER BY LOWER(Route), LOWER(Customer)";
            else
                selectQuery = "SELECT DISTINCT Route, Customer, Mobile, CustomerType, LoginId FROM CustomerMaster ORDER BY LOWER(Route), LOWER(Customer)";
        }
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Route", cursor.getString(0));
            map.put("Customer", cursor.getString(1));
            map.put("Mobile", cursor.getString(2));
            map.put("CustomerType", cursor.getString(3));
            map.put("LoginId", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all allocated details demand
    public ArrayList<HashMap<String, String>> getViewDemandDetails(String id, String lang) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        if (lang.equalsIgnoreCase("hi"))
            selectQuery = "SELECT (CASE WHEN ProductLocal='' THEN Item ELSE ProductLocal END), DemandQty, PackingType, Rate FROM ViewDemandDetails WHERE Id ='"
                    + id + "'  ORDER BY LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        else
            selectQuery = "SELECT Item, DemandQty, PackingType, Rate FROM ViewDemandDetails WHERE Id ='"
                    + id + "'  ORDER BY LOWER(Product), LOWER(SkuUnit), LOWER(Uom)";
        //Log.i("LPDND", "selectQuery="+selectQuery);
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Item", cursor.getString(0));
            map.put("DemandQty", cursor.getString(1));
            map.put("PackingType", cursor.getString(2));
            map.put("Rate", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    // To get all demand details
    public ArrayList<HashMap<String, String>> getDemandDetails() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT ItemId, Item, Qty, Rate FROM DemandDetails det, Demand od WHERE od.Id = det.DemandId AND od.isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("ItemId", cursor.getString(0));
            map.put("Item", cursor.getString(1));
            map.put("Qty", cursor.getString(2));
            map.put("Rate", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }


    // To Update delivery IsSync flag
    public String Update_DeliveryIsSync() {
        try {
            String query = "UPDATE Delivery SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // To Update payment IsSync flag
    public String Update_PaymentIsSync() {
        try {
            String query = "UPDATE CustomerPaymentMaster SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //<editor-fold desc="Code to Updated Primary Receipt IsSYnc Flag">
    public String Update_PrimaryReceiptIsSync() {
        try {
            String query = "UPDATE OutletPrimaryReceipt SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Updated Outlet Payment Receipt IsSYnc Flag">
    public String Update_PaymentReceiptIsSync() {
        try {
            String query = "UPDATE OutletPaymentReceipt SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to Updated Outlet Expense IsSYnc Flag">
    public String Update_OutletExpenseIsSync() {
        try {
            String query = "UPDATE ExpenseBooking SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>


    public void DeleteDemand(String orderId) {
        db.execSQL("DELETE FROM Demand WHERE Id = '" + orderId + "'");
        db.execSQL("DELETE FROM DemandDetails WHERE DemandId = '" + orderId
                + "'");
    }

    // Deleting All Data
    public String deleteAllData() {
        result = "fail";
        try {
            newValues = new ContentValues();
            newValues.put("IsSync", 1);
            db.execSQL("DELETE FROM Exceptions;");
            db.execSQL("DELETE FROM Item;");
            db.execSQL("DELETE FROM Msg;");
            db.execSQL("DELETE FROM Route;");
            db.execSQL("DELETE FROM Vehicle;");
            db.execSQL("DELETE FROM Company;");
            db.execSQL("DELETE FROM Bank;");
            db.execSQL("DELETE FROM CustomerRate;");
            db.execSQL("DELETE FROM Demand;");
            db.execSQL("DELETE FROM DemandDetails;");
            db.execSQL("DELETE FROM ViewDemand;");
            db.execSQL("DELETE FROM ViewDemandDetails;");
            db.execSQL("DELETE FROM DeliveryInput;");
            db.execSQL("DELETE FROM Delivery;");
            db.execSQL("DELETE FROM DeliveryDetail;");
            db.execSQL("DELETE FROM CustomerType;");
            db.execSQL("DELETE FROM Country;");
            db.execSQL("DELETE FROM State;");
            db.execSQL("DELETE FROM City;");
            db.execSQL("DELETE FROM PinCode;");
            db.execSQL("DELETE FROM CustomerRegistration;");
            db.execSQL("DELETE FROM StockReturn;");
            db.execSQL("DELETE FROM StockReturnDetails;");
            db.execSQL("DELETE FROM CashDepositHeader;");
            db.execSQL("DELETE FROM CashDepositDetail;");
            db.execSQL("DELETE FROM CashDepositTransaction;");
            db.execSQL("DELETE FROM CustomerLedger;");
            db.execSQL("DELETE FROM CustomerPaymentTemp;");
            db.execSQL("DELETE FROM CustomerPaymentMaster;");
            db.execSQL("DELETE FROM CustomerPaymentDetail;");
            db.execSQL("DELETE FROM ComplaintCategory;");
            db.execSQL("DELETE FROM Complaint;");

            db.execSQL("DELETE FROM OutletPrimaryReceipt;");
            db.execSQL("DELETE FROM OutletPaymentReceipt;");
            db.execSQL("DELETE FROM ExpenseBooking;");
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    // Deleting master data from masters tables
    public String DeleteMasterData(String table) {
        result = "fail";
        try {
            newValues = new ContentValues();
            db.execSQL("DELETE FROM " + table);
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    // Inserting Data into Item Master
    public String Insert_Item(String id, String name, String type, String rate, String productLocal, String product, String skuUnit, String uom) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("Type", type);
            newValues.put("Rate", rate);
            newValues.put("ProductLocal", productLocal);
            newValues.put("Product", product);
            newValues.put("SkuUnit", skuUnit);
            newValues.put("Uom", uom);
            db.insert("Item", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // To get vehicle by route id
    public List<CustomType> getVehicleByRoute(String routeId) {
        List<CustomType> list = new ArrayList<CustomType>();
        selectQuery = "SELECT Id, Name FROM Vehicle WHERE RouteId = '"
                + routeId + "' ORDER BY LOWER(Name)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            list.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return list;
    }

    /********************* Insert Statement of masters used in new customer registration ******************/
    // Inserting Data into Country Master
    public String Insert_CustomerType(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("CustomerType", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inserting Data into Country Master
    public String Insert_Country(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("Country", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inserting Data into State Master
    public String Insert_State(String id, String countryid, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("CountryId", countryid);
            newValues.put("Name", name);
            db.insert("State", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inserting Data into City Master
    public String Insert_City(String id, String stateid, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("StateId", stateid);
            newValues.put("Name", name);
            db.insert("City", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inserting Data into pin code Master
    public String Insert_PinCode(String id, String cityid, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("CityId", cityid);
            newValues.put("Name", name);
            db.insert("PinCode", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /********************* End of Insert Statement of masters used in new customer registration ******************/

    // Inserting Data into Complaint Category Master
    public String Insert_ComplaintCategory(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("ComplaintCategory", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Inserting Data into Msg Master
    public String Insert_Msg(String id, String name) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            db.insert("Msg", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //<editor-fold desc="Code to insert Consumed Data in Temporary Table">
    public String Insert_OutletConversionConsumedTemp(String materialId, String skuId, String quantity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("MaterialId", materialId);
            newValues.put("SKUId", skuId);
            newValues.put("Quantity", quantity);
            db.insert("OutletConversionConsumedTemp", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Produced Data in Temporary Table">
    public String Insert_OutletConversionProducedTemp(String skuId, String quantity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("SKUId", skuId);
            newValues.put("Quantity", quantity);
            db.insert("OutletConversionProducedTemp", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    // To get all offers
    public ArrayList<HashMap<String, String>> getMsg() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, Name FROM Msg ORDER BY CAST(Id AS INT) DESC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    public boolean IsSyncRequiredForRouteOfficer() {
        boolean isRequired = true;

        db.execSQL("DELETE FROM DeliveryDetail WHERE DeliveryId IN (SELECT Id FROM Delivery WHERE CreateDate < DATE('now', '-30 day'));");
        db.execSQL("DELETE FROM Delivery WHERE CreateDate < DATE('now', '-30 day');");
        int RouteCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM Route";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        RouteCount = cursor.getCount();
        cursor.close();

		/*int VehicleCount;
        // In demand --item online created
		selectQuery = "SELECT * FROM Vehicle";// only in Route Officer
		cursor = db.rawQuery(selectQuery, null);
		VehicleCount = cursor.getCount();
		cursor.close();*/

        int CompanyCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM Company";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        CompanyCount = cursor.getCount();
        cursor.close();


        int BankCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM Bank";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        BankCount = cursor.getCount();
        cursor.close();

        int CustomerRateCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM CustomerRate";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        CustomerRateCount = cursor.getCount();
        cursor.close();

        int customerCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM CustomerMaster";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        customerCount = cursor.getCount();
        cursor.close();

		/*int DeliveryInputCount;
        // In demand --item online created
		selectQuery = "SELECT * FROM DeliveryInput";// only in Route Officer
		cursor = db.rawQuery(selectQuery, null);
		DeliveryInputCount = cursor.getCount();
		cursor.close();*/

        int CustomerLedgerCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM CustomerLedger";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        CustomerLedgerCount = cursor.getCount();
        cursor.close();

        if (customerCount > 0 && RouteCount > 0 && CompanyCount > 0 && BankCount > 0 && CustomerRateCount > 0 && CustomerLedgerCount > 0)
            isRequired = false;

        return isRequired;
    }

    public boolean IsSyncRequiredForAccountant() {
        boolean isRequired = true;

        int CompanyCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM Company";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        CompanyCount = cursor.getCount();
        cursor.close();


        int BankCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM Bank";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        BankCount = cursor.getCount();
        cursor.close();

		/*		int CustomerRateCount;
		// In demand --item online created
		selectQuery = "SELECT * FROM CustomerRate";// only in Route Officer
		cursor = db.rawQuery(selectQuery, null);
		CustomerRateCount = cursor.getCount();
		cursor.close();*/

        int customerCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM CustomerMaster";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        customerCount = cursor.getCount();
        cursor.close();

		/*int DeliveryInputCount;
		// In demand --item online created
		selectQuery = "SELECT * FROM DeliveryInput";// only in Route Officer
		cursor = db.rawQuery(selectQuery, null);
		DeliveryInputCount = cursor.getCount();
		cursor.close();*/

        int CustomerLedgerCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM CustomerLedger";// only in Route Officer
        cursor = db.rawQuery(selectQuery, null);
        CustomerLedgerCount = cursor.getCount();
        cursor.close();

        if (customerCount > 0 && CompanyCount > 0 && BankCount > 0 && CustomerLedgerCount > 0)
            isRequired = false;

        return isRequired;
    }

    public boolean IsSyncRequiredForCustomer() {
        boolean isRequired = true;

        int catCount;
        // In demand --item online created
        selectQuery = "SELECT * FROM ComplaintCategory";// only in Customer
        cursor = db.rawQuery(selectQuery, null);
        catCount = cursor.getCount();
        cursor.close();

        if (catCount > 0)
            isRequired = false;

        return isRequired;
    }

    public boolean IslogoutAllowed() {
        boolean isRequired = true;

        int countDelivery, countStockReturn, countPaymentMaster, countPaymentDetail, countComplaint, countPrimaryReceipt, countoutletPayment, countExpense;

        selectQuery = "SELECT Id FROM Delivery WHERE IsSync = '0'";
        cursor = db.rawQuery(selectQuery, null);
        countDelivery = cursor.getCount();
        cursor.close();

        selectQuery = "SELECT Id FROM StockReturn";
        cursor = db.rawQuery(selectQuery, null);
        countStockReturn = cursor.getCount();
        cursor.close();

        selectQuery = "SELECT Id FROM CustomerPaymentMaster";
        cursor = db.rawQuery(selectQuery, null);
        countPaymentMaster = cursor.getCount();
        cursor.close();

        selectQuery = "SELECT Id FROM CustomerPaymentDetail";
        cursor = db.rawQuery(selectQuery, null);
        countPaymentDetail = cursor.getCount();
        cursor.close();

        selectQuery = "SELECT Id FROM Complaint";
        cursor = db.rawQuery(selectQuery, null);
        countComplaint = cursor.getCount();

        selectQuery = "SELECT Id FROM OutletPrimaryReceipt WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        countPrimaryReceipt = cursor.getCount();

        selectQuery = "SELECT Id FROM OutletPaymentReceipt WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        countoutletPayment = cursor.getCount();

        selectQuery = "SELECT Id FROM ExpenseBooking WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        countExpense = cursor.getCount();


        cursor.close();
        if (countDelivery > 0 || countStockReturn > 0 || countPaymentMaster > 0 || countPaymentDetail > 0 || countComplaint > 0 || countPrimaryReceipt > 0 || countoutletPayment>0 || countExpense>0)
            isRequired = false;

        return isRequired;
    }

    // Method to get current date time
    public String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Method to get uniqueId
    public String uniqueId(String imei) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.US);
        Date date = new Date();
        return dateFormat.format(date) + imei;
    }

    //To insert data in Customer Payment and Detail table
    public String insertCustomerPayment(String uniqueId, String customerId, String createBy, String deliveryUniqueId) {
        result = "fail";
        //Start of code to insert data in Customer Payment Master Table
        newValues = new ContentValues();

        newValues.put("UniqueId", uniqueId);
        newValues.put("CustomerId", customerId);
        newValues.put("CreateBy", createBy);
        newValues.put("CreateDate", getDateTime());
        newValues.put("deliveryUniqueId", deliveryUniqueId);
        newValues.put("IsSync", "0");
        db = dbHelper.getWritableDatabase();
        db.insert("CustomerPaymentMaster", null, newValues);
        //End of code to insert data in Customer Payment Master Table

        //Start of code to get maximum id from CustomerPaymentMaster Table
        int masterId = 0;
        selectQuery = "SELECT MAX(Id) FROM CustomerPaymentMaster ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                masterId = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        //End of code to get maximum id from CustomerPaymentMaster Table

        //Start of code to insert data from Temporary Detail Table to Main Detail Table
        selectQuery = "INSERT INTO CustomerPaymentDetail(MasterId,CompanyId, BankId, ChequeNumber,  Amount, ImagePath,UniqueId, ImageName,Remarks,CreateDate) " +
                "SELECT " + masterId + ", CompanyId, BankId, ChequeNumber,  Amount, ImagePath,UniqueId, ImageName,Remarks,CreateDate FROM CustomerPaymentTemp WHERE CAST(Amount AS NUMERIC)!=0 ";
        db.execSQL(selectQuery);
        //End of code to insert data from Temporary Detail Table to Main Detail Table

		/*Start of code to update customer ledger*/
        selectQuery = "SELECT SUM(Amount), CompanyId FROM CustomerPaymentTemp GROUP BY CompanyId";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            db.execSQL("UPDATE CustomerLedger SET Balance = Balance + " + cursor.getDouble(0) + " WHERE CustomerId = '" + customerId + "' AND CompanyId ='" + cursor.getString(1) + "' ");
        }
        cursor.close();
		/*End of code to update customer ledger*/

        //Start of code to delete data from Temporary Detail Table
        db.execSQL("DELETE FROM CustomerPaymentTemp");
        //End of code to delete data from Temporary Detail Table

        result = "success";

        return result;
    }

    //To insert data in from Customer Payment Temporary table to main
    public String insertCustomerPaymentTemp(String companyId, String bankId, String chequeNumber, String amount, String imagePath, String imageName, String uniqueId, String remarks) {
        result = "fail";
        newValues = new ContentValues();

        newValues.put("CompanyId", companyId);
        newValues.put("BankId", bankId);
        newValues.put("ChequeNumber", chequeNumber);
        newValues.put("Amount", amount);
        newValues.put("ImagePath", imagePath);
        newValues.put("ImageName", imageName);
        newValues.put("UniqueId", uniqueId);
        newValues.put("Remarks", remarks);
        newValues.put("CreateDate", getDateTime());
        db = dbHelper.getWritableDatabase();
        db.insert("CustomerPaymentTemp", null, newValues);
        result = "success";

        return result;
    }

    //Code to delete data from temporary payment details table
    public void DeleteTempPaymentDetails(String id) {
        if (id.equalsIgnoreCase("0"))
            db.execSQL("DELETE FROM CustomerPaymentTemp");
        else
            db.execSQL("DELETE FROM CustomerPaymentTemp WHERE Id = '" + id + "'");
    }


    //Method to get customer payment data from temporary table
    public List<CustomerPayment> getCustomerTempPayment() {
        List<CustomerPayment> labels = new ArrayList<CustomerPayment>();
        selectQuery = "SELECT t.Id, c.Name, t.Amount, (CASE WHEN ifnull(b.Name,0) = 0 THEN t.ChequeNumber ELSE b.Name||' - '||t.ChequeNumber END), b.Name, t.UniqueId FROM CustomerPaymentTemp t LEFT OUTER JOIN Bank b ON t.BankId = b.Id, Company c WHERE t.CompanyId = c.Id ORDER BY c.Name";

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomerPayment(Integer.valueOf(cursor.getString(0)), cursor.getString(1), String.valueOf(cursor.getFloat(2)), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        }
        cursor.close();
        return labels;
    }

    public String getCustomerTempTotalPayment() {
        String total = "";
        selectQuery = "SELECT SUM(Amount) FROM CustomerPaymentTemp ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }

    //Method to check if cheque number already exists
    public int getChequeNumberExistCount(String customerId, String chequeNumber) {
        int id = 0;

        int idTemp = 0;
        //CompanyId TEXT, BankId TEXT, ChequeNumber
        int idMain = 0;
        selectQuery = "SELECT COUNT(Id) FROM CustomerPaymentTemp WHERE ChequeNumber= '" + chequeNumber + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                idTemp = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        selectQuery = "SELECT COUNT(d.Id) FROM CustomerPaymentMaster m, CustomerPaymentDetail d WHERE m.Id = d.MasterId AND m.CustomerId = '" + customerId + "' AND d.ChequeNumber = '" + chequeNumber + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                idMain = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        id = idTemp + idMain;

        return id;
    }

    //Get List of Customer Name and Date for Payment View
    public List<Payments> GetPaymentReport() {
        List<Payments> ir = new ArrayList<Payments>();
        selectQuery = "SELECT DISTINCT p.CustomerId,  c.Customer, SUBSTR(p.CreateDate,0,11)  FROM CustomerPaymentMaster p, CustomerMaster c WHERE p.CustomerId = c.CustomerId AND DeliveryUniqueId='0' ORDER BY SUBSTR(p.CreateDate,0,11) DESC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            ir.add(new Payments(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();
        return ir;
    }

    //To insert data in customer rate
    public String Insert_CustomerRate(String customerId, String skuId, String rate) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CustomerId", customerId);
            newValues.put("SKUId", skuId);
            newValues.put("Rate", rate);
            db.insert("CustomerRate", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert data in customer rate
    public String Insert_CompanyRate(String skuId, String rate) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("SKUId", skuId);
            newValues.put("Rate", rate);
            db.insert("CompanyRate", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert data in customer ledger
    public String Insert_CustomerLedger(String customerId, String companyId, Double balance) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CustomerId", customerId);
            newValues.put("CompanyId", companyId);
            newValues.put("Balance", balance);
            db.insert("CustomerLedger", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Method to balance for customer by company
    public String getBalanceByCustomerandCompany(String customerId, String companyId) {


        String bal = "0";

        selectQuery = "SELECT cl.Balance + ifnull(ct.Amt,0) FROM CustomerLedger cl LEFT OUTER JOIN (SELECT CompanyId, SUM(Amount) AS Amt FROM CustomerPaymentTemp GROUP BY CompanyId) ct ON cl.CompanyId = ct.CompanyId WHERE cl.CustomerId= '" + customerId + "' AND cl.CompanyId= '" + companyId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0).equals("0"))
                    bal = "0";
                else
                    bal = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return bal;
    }

    //Method to get customer payment data by Date
    public List<CustomerPayment> getCustomerPayment(String customerId, String date) {
        List<CustomerPayment> labels = new ArrayList<CustomerPayment>();
        selectQuery = "SELECT cmp.Name, cd.Amount,(CASE WHEN (cd.Remarks!='' AND cd.Remarks IS NOT NULL) THEN cd.Remarks WHEN ifnull(b.Name,0) = 0 THEN cd.ChequeNumber ELSE b.Name||' - '||cd.ChequeNumber END), b.Name, cd.ImagePath FROM CustomerPaymentDetail cd LEFT OUTER JOIN Bank b ON cd.BankId = b.Id, Company cmp WHERE cd.CompanyId = cmp.Id AND cd.MasterId IN (SELECT Id FROM CustomerPaymentMaster WHERE SUBSTR(CreateDate,0,11)='" + date + "' AND CustomerId = '" + customerId + "' AND DeliveryUniqueId='0') ORDER BY cmp.Name";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomerPayment(0, cursor.getString(0), String.valueOf(cursor.getFloat(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
        }
        cursor.close();
        return labels;
    }

    //Method to get customer total payment in delivery and payment
    public String getTotalPayment(String customerId, String date) {
        String total = "";
        selectQuery = "SELECT SUM(Amount) FROM CustomerPaymentDetail WHERE MasterId IN (SELECT Id FROM CustomerPaymentMaster WHERE SUBSTR(CreateDate,0,11)='" + date + "' AND CustomerId = '" + customerId + "' AND DeliveryUniqueId!='0')";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }

    //Method to get customer payment data by Date for delivery
    public List<CustomerPayment> getCustomerPaymentForDelivery(String customerId, String date) {
        List<CustomerPayment> labels = new ArrayList<CustomerPayment>();
        selectQuery = "SELECT cmp.Name, SUM(cd.Amount), (CASE WHEN (cd.Remarks!='' AND cd.Remarks IS NOT NULL) THEN cd.Remarks WHEN ifnull(b.Name,0) = 0 THEN cd.ChequeNumber ELSE b.Name||' - '||cd.ChequeNumber END), b.Name, cd.ImagePath FROM CustomerPaymentDetail cd LEFT OUTER JOIN Bank b ON cd.BankId = b.Id, Company cmp WHERE cd.CompanyId = cmp.Id AND cd.MasterId IN (SELECT Id FROM CustomerPaymentMaster WHERE SUBSTR(CreateDate,0,11)='" + date + "' AND CustomerId = '" + customerId + "' AND DeliveryUniqueId!='0') GROUP BY cmp.Name, cd.ChequeNumber, b.Name, cd.ImagePath,cd.Remarks ORDER BY cmp.Name";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CustomerPayment(0, cursor.getString(0), String.valueOf(cursor.getFloat(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
        }
        cursor.close();
        return labels;
    }

    //Method to get customer payment total by  Date
    public String getCustomerTotalPayment(String customerId, String date) {
        String total = "";
        selectQuery = "SELECT SUM(cd.Amount) FROM CustomerPaymentDetail cd  WHERE cd.MasterId IN (SELECT Id FROM CustomerPaymentMaster WHERE SUBSTR(CreateDate,0,11)='" + date + "' AND CustomerId = '" + customerId + "' AND DeliveryUniqueId='0') ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }

    //Method to get customer payment total by  Date For Delivery
    public String getCustomerTotalPaymentForDelivery(String customerId, String date) {
        String total = "";
        selectQuery = "SELECT SUM(cd.Amount) FROM CustomerPaymentDetail cd  WHERE cd.MasterId IN (SELECT Id FROM CustomerPaymentMaster WHERE SUBSTR(CreateDate,0,11)='" + date + "' AND CustomerId = '" + customerId + "' AND DeliveryUniqueId!='0') ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }

    //To insert data in complaint table
    public String insertComplaint(String CustomerId, String ComplaintType, String ComplaintCategoryId, String FeedbackRating, String CustomerRemark, String DeviceUniqueId) {
        result = "fail";
        newValues = new ContentValues();

        newValues.put("ComplaintDate", getDateTime());
        newValues.put("CustomerId", CustomerId);
        newValues.put("ComplaintType", ComplaintType);
        newValues.put("ComplaintCategoryId", ComplaintCategoryId);
        newValues.put("FeedbackRating", FeedbackRating);
        newValues.put("CustomerRemark", CustomerRemark);
        newValues.put("DeviceUniqueId", DeviceUniqueId);
        newValues.put("isSync", "0");
        db = dbHelper.getWritableDatabase();
        db.insert("Complaint", null, newValues);
        result = "success";

        return result;
    }


    //To Update Complaint details
    public String UpdateComplaint() {
        try {
            String query = "UPDATE Complaint SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Code to delete data from complaint details table
    public void DeleteComplaint() {

        db.execSQL("DELETE FROM Complaint");

    }

    //Method to get complaint feedback data from complaint table
    public ArrayList<HashMap<String, String>> getComplaintFeedback() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT Id, ComplaintDate, ComplaintType, FeedBackRating FROM Complaint ORDER BY LOWER(ComplaintDate) DESC, LOWER(ComplaintType)  ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("ComplaintDate", cursor.getString(1));
            map.put("ComplaintType", cursor.getString(2));
            map.put("FeedBackRating", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //To get all stock return details
    public ArrayList<HashMap<String, String>> getComplaintFeedbackSummaryDetails(String id) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT cf.Id, strftime('%Y-%m-%d', cf.ComplaintDate) AS ComplaintDate, cf.ComplaintType, CASE WHEN cf.ComplaintCategoryId = 0 THEN cf.FeedbackRating ELSE cc.Name END AS CategoryFeedback, cf.CustomerRemark FROM Complaint cf LEFT OUTER JOIN ComplaintCategory cc ON cf.ComplaintCategoryId = cc.Id WHERE cf.Id= '" + id + "'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("ComplaintDate", cursor.getString(1));
            map.put("ComplaintType", cursor.getString(2));
            map.put("CategoryFeedback", cursor.getString(3));
            map.put("CustomerRemark", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //Method to get complaint feedback data from complaint table
    public ArrayList<HashMap<String, String>> getComplaint() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, CustomerId, ComplaintDate, ComplaintType, ComplaintCategoryId, FeedbackRating, CustomerRemark, DeviceUniqueId FROM Complaint ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("CustomerId", cursor.getString(1));
            map.put("ComplaintDate", cursor.getString(2));
            map.put("ComplaintType", cursor.getString(3));
            map.put("ComplaintCategoryId", cursor.getString(4));
            map.put("FeedbackRating", cursor.getString(5));
            map.put("CustomerRemark", cursor.getString(6));
            map.put("DeviceUniqueId", cursor.getString(7));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    /******Cash Deposit************/
    //To insert Cash Deposit Header records
    public String Insert_CashDepositHeader(String companyId, String companyName, String previousBalance, String collectionAmount, String totalAmount, String onlineAmount) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CompanyId", companyId);
            newValues.put("CompanyName", companyName);
            newValues.put("PreviousBalance", previousBalance);
            newValues.put("CollectionAmount", collectionAmount);
            newValues.put("TotalAmount", totalAmount);
            newValues.put("OnlineAmount", onlineAmount);

            db.insert("CashDepositHeader", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert Cash Deposit Details records
    public String Insert_CashDepositDetails(String companyId, String pcDetailId, String customerName, String paymentDate, String cheque, String amount) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CompanyId", companyId);
            newValues.put("PCDetailId", pcDetailId);
            newValues.put("CustomerName", customerName);
            newValues.put("PaymentDate", paymentDate);
            newValues.put("Cheque", cheque);
            newValues.put("Amount", amount);
            db.insert("CashDepositDetail", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert Cash Deposit records
    public String Insert_CashDeposit(String pcDetailId, String customerName, String paymentDate, String cheque, String amount) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("PCDetailId", pcDetailId);
            newValues.put("CustomerName", customerName);
            newValues.put("PaymentDate", paymentDate);
            newValues.put("Cheque", cheque);
            newValues.put("Amount", amount);
            db.insert("CashDeposit", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //To insert Cash Deposit Transaction records
    public String Insert_CashDepositTransaction(String companyId, String previousBalance, String collectionAmount, String totalAmount, String depositAmount, String pcDetailId, String remarks) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CompanyId", companyId);
            newValues.put("PreviousBalance", previousBalance);
            newValues.put("CollectionAmount", collectionAmount);
            newValues.put("TotalAmount", totalAmount);
            newValues.put("DepositAmount", depositAmount);
            newValues.put("pcDetailId", pcDetailId);
            newValues.put("Remarks", remarks);
            db.insert("CashDepositTransaction", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //To insert records in CashDepositDeleteData Table
    public String Insert_CashDepositDeleteData(String cashDepositId, String cashDepositDetailId, String depositDate, String pcdetailId, String mode, String amount, String fullName) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CashDepositId", cashDepositId);
            newValues.put("CashDepositDetailId", cashDepositDetailId);
            newValues.put("DepositDate", depositDate);
            newValues.put("PCDetailId", pcdetailId);
            newValues.put("Mode", mode);
            newValues.put("Amount", amount);
            newValues.put("FullName", fullName);

            db.insert("CashDepositDeleteData", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //<editor-fold desc="To get Cash Deposit Data For Delete">
    public ArrayList<HashMap<String, String>> getCashDepositDeleteData() {
        wordList = new ArrayList<HashMap<String, String>>();
        String date = "";
        selectQuery = "SELECT DISTINCT CashDepositId, DepositDate, FullName FROM CashDepositDeleteData";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CashDepositId", cursor.getString(0));
            map.put("DepositDate", cursor.getString(1));
            map.put("FullName", cursor.getString(2));
            if (date.equalsIgnoreCase(cursor.getString(1)))
                map.put("Flag", "1");
            else
                map.put("Flag", "0");
            date = cursor.getString(1);
            wordList.add(map);
        }

        cursor.close();

        return wordList;
    }
    //</editor-fold>

    //Method to get Cash Deposit Header Data
    public String GetCashDepositHeaderData(String id) {
        String data = "";
        selectQuery = "SELECT DISTINCT DepositDate||'~'||FullName FROM CashDepositDeleteData WHERE CashDepositId='" + id + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return data;
    }

    //Method to get Total Amount for Deleting Cash Deposit Header Data
    public String GetTotalCashDepositHeaderData(String id) {
        String data = "";
        selectQuery = "SELECT SUM(Amount) FROM CashDepositDeleteData WHERE CashDepositId='" + id + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return data;
    }


    //<editor-fold desc="To get Cash Deposit List For Delete">
    public ArrayList<HashMap<String, String>> getCashDepositListForDelete(String id) {
        wordList = new ArrayList<HashMap<String, String>>();

        selectQuery = "SELECT Mode,Amount FROM CashDepositDeleteData WHERE CashDepositId= '" + id + "' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Mode", cursor.getString(0));
            map.put("Amount", cursor.getString(1));
            wordList.add(map);
        }

        cursor.close();

        return wordList;
    }
    //</editor-fold>

    //Method to get cash deposit transactional data from temporary table
    public ArrayList<HashMap<String, String>> getCashDepositTransaction() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT CompanyId, PreviousBalance, CollectionAmount, TotalAmount, DepositAmount, Remarks, PCDetailId FROM CashDepositTransaction WHERE DepositAmount != 0 OR PCDetailId != '' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("CompanyId", cursor.getString(0));
            map.put("PreviousBalance", cursor.getString(1));
            map.put("CollectionAmount", cursor.getString(2));
            map.put("TotalAmount", cursor.getString(3));
            map.put("DepositAmount", cursor.getString(4));
            map.put("Remarks", cursor.getString(5));
            map.put("PCDetailId", cursor.getString(6));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //Method to get total Cash Deposited collected
    public int GetTotalChequeCollected() {
        selectQuery = "SELECT * FROM CashDepositTransaction WHERE PCDetailId <> '' ";
        cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount();
    }

    //Method to get total Cash Deposited collected
    public double GetTotalCashDeposited() {
        double total = 0.0;
        selectQuery = "SELECT SUM(DepositAmount) FROM CashDepositTransaction ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = cursor.getFloat(0);
            } while (cursor.moveToNext());
        }
        return total;
    }

    //Method to get cash deposit total payment
    public String getCashDepositTotalPayment(String companyId) {
        String total = "";
        selectQuery = "SELECT SUM(Amount) FROM CashDepositDetail WHERE CompanyId = '" + companyId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }

    //Get Pending Cash Deposit
    public HashMap<String, String> GetPendingCashDeposit() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        selectQuery = "SELECT CompanyId, CompanyName, PreviousBalance, CollectionAmount, TotalAmount, OnlineAmount FROM CashDepositHeader WHERE CompanyId NOT IN (SELECT CompanyId FROM CashDepositTransaction) ORDER BY CompanyName DESC LIMIT 1 ";

        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToNext()) {
            hashMap.put("CompanyId", cursor.getString(0));
            hashMap.put("CompanyName", cursor.getString(1));
            hashMap.put("PreviousBalance", cursor.getString(2));
            hashMap.put("CollectionAmount", cursor.getString(3));
            hashMap.put("TotalAmount", cursor.getString(4));
            hashMap.put("OnlineAmount", cursor.getString(5));
        }
        return hashMap;
    }

    //Method to get Route wise cash deposit
    public List<CashDeposit> getRouteWiseCashDeposit(String companyId) {
        List<CashDeposit> labels = new ArrayList<CashDeposit>();
        selectQuery = "SELECT PCDetailId, CustomerName, PaymentDate, Cheque, Amount, '' FROM CashDepositDetail WHERE CompanyId = '" + companyId + "' ORDER BY CustomerName ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            labels.add(new CashDeposit(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        }
        cursor.close();
        return labels;
    }

    //Method to get count of cash deposit to be made
    public int GetTotalCashDepositCount() {
        selectQuery = "SELECT * FROM CashDepositHeader";
        cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount();
    }

    //Method to get count of cash deposit transaction entry made
    public int GetCashDepositTransactionCount() {
        selectQuery = "SELECT * FROM CashDepositTransaction";
        cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount();
    }

    //deleting stock return data
    public void DeleteCashDeposit() {
        db.execSQL("DELETE FROM CashDepositHeader");
        db.execSQL("DELETE FROM CashDepositDetail");
        db.execSQL("DELETE FROM CashDepositTransaction");
    }

    //<editor-fold desc="Code to delete data from Temporary Conversion Table">
    public void DeleteTempConversion() {
        db.execSQL("DELETE FROM OutletConversionConsumedTemp");
        db.execSQL("DELETE FROM OutletConversionProducedTemp");
    }
    //</editor-fold>

    /******End of Cash Deposit************/
    //Method to get pending Payment by customer id
    public HashMap<String, String> GetPendingPaymentByCustomerId(String custId) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        selectQuery = "SELECT cl.CompanyId, cp.Name, cl.Balance FROM CustomerLedger cl, Company cp WHERE cl.CustomerId='" + custId + "' AND cl.CompanyId = cp.Id AND cl.CompanyId NOT IN (SELECT CompanyId FROM CustomerPaymentTemp) ORDER BY cl.Balance DESC  LIMIT 1 ";

        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToNext()) {
            hashMap.put("CompanyId", cursor.getString(0));
            hashMap.put("CompanyName", cursor.getString(1));
            hashMap.put("Balance", String.valueOf(cursor.getFloat(2)));
        }
        return hashMap;
    }

    //Method to get pending Payment for delivery
    public HashMap<String, String> GetPendingPaymentForDelivery(String custId, String deliveryId) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        selectQuery = "SELECT cl.CompanyId, cp.Name, cl.Balance FROM CustomerLedger cl, (SELECT DISTINCT det.DeliveryId, det.CompanyId FROM DeliveryDetail det, Delivery mast  WHERE det.DeliveryId = mast.Id AND mast.UniqueId = '" + deliveryId + "') del, Company cp WHERE cl.CustomerId='" + custId + "' AND cl.CompanyId = cp.Id AND cl.CompanyId = del.CompanyId AND cl.CompanyId NOT IN (SELECT CompanyId FROM CustomerPaymentTemp) ORDER BY cl.Balance DESC LIMIT 1 ";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToNext()) {
            hashMap.put("CompanyId", cursor.getString(0));
            hashMap.put("CompanyName", cursor.getString(1));
            hashMap.put("Balance", String.valueOf(String.format("%.2f", cursor.getFloat(2))));
        }
        return hashMap;
    }

    //Get Company count by delivery id and customer id
    public int GetCompanyCountForDelivery(String custId, String deliveryId) {

        selectQuery = "SELECT DISTINCT cl.CompanyId FROM CustomerLedger cl, (SELECT DISTINCT det.DeliveryId, det.CompanyId FROM DeliveryDetail det, Delivery mast  WHERE det.DeliveryId = mast.Id AND mast.UniqueId = '" + deliveryId + "') del, Company cp WHERE cl.CustomerId='" + custId + "' AND cl.CompanyId = cp.Id AND cl.CompanyId = del.CompanyId ";
        cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount();
    }

    //Get Company count by customer
    public int GetCompanyCountByCustomer(String custId) {

        selectQuery = "SELECT * FROM CustomerLedger WHERE CustomerId ='" + custId + "' ";
        cursor = db.rawQuery(selectQuery, null);
        return cursor.getCount();
    }

    //Method to get total payment collected
    public double GetTotaPaymentCollected() {
        double total = 0.0;
        selectQuery = "SELECT SUM(Amount) FROM CustomerPaymentTemp ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = cursor.getFloat(0);
            } while (cursor.moveToNext());
        }
        return total;
    }

    // To delivery details for Payment By Delivery Id and Company Id
    public ArrayList<HashMap<String, String>> getDeliveryDetailsForPayments(String compId, String deliveryId) {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT det.SKU, det.DelQty, det.DelQty*det.Rate FROM DeliveryDetail det, Delivery del WHERE det.CompanyId = '" + compId + "' AND det.DeliveryId = del.Id AND del.UniqueId ='" + deliveryId + "' ORDER BY det.SKU";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("SKU", cursor.getString(0));
            map.put("Qty", cursor.getString(1));
            map.put("Amount", String.valueOf(String.format("%.2f", cursor.getFloat(2))));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }

    //Method to get total payment collected
    public double GetTotaPaymentForDeliveryDetailsForPayments(String compId, String deliveryId) {
        double total = 0.0;
        selectQuery = "SELECT SUM(det.DelQty*det.Rate) FROM DeliveryDetail det, Delivery del WHERE det.CompanyId = '" + compId + "' AND det.DeliveryId = del.Id AND del.UniqueId ='" + deliveryId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = cursor.getFloat(0);
            } while (cursor.moveToNext());
        }
        return total;
    }

    public List<CustomType> GetRoutesForRouteOfficers() {
        // define List of CustomType
        List<CustomType> routes = new ArrayList<CustomType>();

        // define SELECT query for theh list of Routes
        selectQuery = "SELECT Id, Name FROM Route ORDER BY LOWER(Name)";

        // get records of routes
        cursor = db.rawQuery(selectQuery, null);

        // loop through cursor to get the records in the cursor
        while (cursor.moveToNext()) {
            routes.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }

        // close the cursor to release the memory
        cursor.close();
        return routes;
    }


    public String getRouteNameById(String routeId) {
        String routeName = "";
        selectQuery = "SELECT Name FROM Route WHERE Id ='" + routeId + "'";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                routeName = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return routeName;
    }


    public String getTempDocument() {
        String tempFileName = "";
        selectQuery = "SELECT FileName FROM TempDoc";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                tempFileName = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return tempFileName;
    }

    public String deleteTempDoc() {
        selectQuery = "DELETE FROM TempDoc";
        db.execSQL(selectQuery);
        return "success";
    }

    public String Insert_TempDoc(String fileName) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("FileName", fileName);

            db.insert("TempDoc", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    // Method to get current date time
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    //Method to get Expense Confirmation Data
    public String getExpenseConfirmationHeaderData(String id) {
        String data = "";
        selectQuery = "SELECT DISTINCT ExpenseDate||'~'||CustomerName||'~'||ExpenseHead||'~'||Amount||'~'||Remarks FROM ExpenseConfirmationData WHERE Id='" + id + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return data;
    }

    //To insert records in ExpenseConfirmationData Table
    public String Insert_ExpenseConfirmationData(String id, String expenseDate, String customerName, String expenseHead, String amount, String remarks) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("ExpenseDate", expenseDate);
            newValues.put("CustomerName", customerName);
            newValues.put("ExpenseHead", expenseHead);
            newValues.put("Amount", amount);
            newValues.put("Remarks", remarks);

            db.insert("ExpenseConfirmationData", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //<editor-fold desc="To get Expense Booking data For confirmation">
    public ArrayList<HashMap<String, String>> getExpenseConfirmationData() {
        wordList = new ArrayList<HashMap<String, String>>();
        String date = "";
        selectQuery = "SELECT DISTINCT Id, ExpenseDate, CustomerName, ExpenseHead, Amount, Remarks FROM ExpenseConfirmationData";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("ExpenseDate", cursor.getString(1));
            map.put("CustomerName", cursor.getString(2));
            map.put("ExpenseHead", cursor.getString(3));
            map.put("Amount", cursor.getString(4));
            map.put("Remarks", cursor.getString(5));
            if (date.equalsIgnoreCase(cursor.getString(1)))
                map.put("Flag", "1");
            else
                map.put("Flag", "0");
            date = cursor.getString(1);
            wordList.add(map);
        }

        cursor.close();

        return wordList;
    }
    //</editor-fold>

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

        }
        return createDateForDB;
    }

    //<editor-fold desc="Method to Fetch Expense Details">
    public ArrayList<HashMap<String, String>> getExpenseDetails() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        String prevDate="";
        if (userlang.equalsIgnoreCase("en"))
            selectQuery = "SELECT  eb.AndroidDate, eh.Name, eb.Amount, eb.Remarks FROM ExpenseBooking eb, ExpenseHead eh WHERE eb.ExpenseHeadId = eh.Id ORDER BY eb.AndroidDate DESC, LOWER(Name) ASC";
        else
            selectQuery = "SELECT  eb.AndroidDate, eh.NameLocal, eb.Amount, eb.Remarks FROM ExpenseBooking eb, ExpenseHead eh WHERE eb.ExpenseHeadId = eh.Id ORDER BY eb.AndroidDate DESC, LOWER(Name) ASC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Date", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("Amount", cursor.getString(2));
            map.put("Remarks", cursor.getString(3));
            if(prevDate.equalsIgnoreCase(convertToDisplayDateFormat(cursor.getString(0))))
                map.put("Flag", "0");
            else
                map.put("Flag", "1");
            prevDate =convertToDisplayDateFormat(cursor.getString(0));

            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Fetch Primary Receipts">
    public ArrayList<HashMap<String, String>> getPrimaryReceipts() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        if (userlang.equalsIgnoreCase("en"))
            selectQuery = "SELECT 'PR'||pr.Id , ifnull(rm.Name||' '||rm.UOM, sm.Name) AS Name, pr.Quantity, pr.Amount, pr.CreateDate FROM OutletPrimaryReceipt pr LEFT OUTER JOIN RawMaterialMaster rm ON pr.MaterialId = rm.Id LEFT OUTER JOIN SKUMaster sm ON pr.SKUId = sm.Id ORDER BY SUBSTR(pr.CreateDate,0,11) DESC";
        else
            selectQuery = "SELECT 'PR'||pr.Id ,ifnull(rm.NameLocal||' '||rm.UOM, sm.NameLocal) AS Name, pr.Quantity, pr.Amount, pr.CreateDate FROM OutletPrimaryReceipt pr LEFT OUTER JOIN RawMaterialMaster rm ON pr.MaterialId = rm.Id LEFT OUTER JOIN SKUMaster sm ON pr.SKUId = sm.Id ORDER BY SUBSTR(pr.CreateDate,0,11) DESC";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Code", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("Quantity", cursor.getString(2));
            map.put("Amount", cursor.getString(3));
            map.put("Date", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to fetch UnSync Primary Receipts">
    public ArrayList<HashMap<String, String>> getUnSyncPrimaryReceipt() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT UniqueId, CustomerId, MaterialId, SKUId, Quantity, Amount, CreateDate FROM OutletPrimaryReceipt WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("CustomerId", cursor.getString(1));
            map.put("MaterialId", cursor.getString(2));
            map.put("SKUId", cursor.getString(3));
            map.put("Quantity", cursor.getString(4));
            map.put("Amount", cursor.getString(5));
            map.put("CreateDate", cursor.getString(6));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="GetOutletSaleDetailsByLang">
    //Method to Get Outlet Sale Details By Lang
    public List<CustomType> GetOutletSaleDetailsByLang(String masterType, String filter, String langOption) {
        List<CustomType> labels = new ArrayList<CustomType>();
        if (masterType == "customer" && langOption.equalsIgnoreCase("hi"))
            selectQuery = "SELECT DISTINCT mas.CustomerId,(CASE WHEN mas.CustomerLocal='' THEN mas.Customer ELSE mas.CustomerLocal END) FROM CustomerMaster mas, OutletSale os WHERE mas.CustomerId = os.CustomerId ORDER BY LOWER(mas.Customer)";
        else if (masterType == "customer" && langOption.equalsIgnoreCase("en"))
            selectQuery = "SELECT DISTINCT mas.CustomerId, mas.Customer FROM CustomerMaster mas, OutletSale os WHERE mas.CustomerId = os.CustomerId ORDER BY LOWER(mas.Customer)";
        //Log.i("LPDND", "selectQuery="+masterType+":"+selectQuery);
        cursor = db.rawQuery(selectQuery, null);

        if (masterType == "customer" && langOption.equalsIgnoreCase("en"))
            labels.add(new CustomType("0", "...Select Customer"));
        else if (masterType == "customer" && langOption.equalsIgnoreCase("hi"))
            labels.add(new CustomType("0", "...ग्राहक चुनें"));
        while (cursor.moveToNext()) {
            labels.add(new CustomType(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();
        return labels;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get SKU Inventory by SKU Id">
    public String getSkuInventory(String skuId) {
        String total = "";
        selectQuery = "SELECT Quantity from SKULiveInventory WHERE Id='" + skuId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }
    //</editor-fold>

    //<editor-fold desc="Code to get Raw Material Inventory by Raw Material Id">
    public String getRawMaterialInventory(String rawId) {
        String total = "";
        selectQuery = "SELECT Quantity from RawMaterialLiveInventory WHERE Id=" + rawId + "  ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                total = String.valueOf(cursor.getFloat(0));
            } while (cursor.moveToNext());
        }
        return total;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if Consumed Item is already added">
    public Boolean isConsumedAlreadyAdded(String materialId, String skuId) {
        Boolean dataExists = false;
        selectQuery = "SELECT Id FROM OutletConversionConsumedTemp WHERE MaterialId = '" + materialId + "' AND SKUId ='" + skuId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            dataExists = true;
        }
        cursor.close();
        return dataExists;
    }
    //</editor-fold>

    //<editor-fold desc="Code to check if Produced Item is already added">
    public Boolean isProducedAlreadyAdded(String skuId) {
        Boolean dataExists = false;
        selectQuery = "SELECT Id FROM OutletConversionProducedTemp WHERE SKUId ='" + skuId + "' ";
        cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            dataExists = true;
        }
        cursor.close();
        return dataExists;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Fetch Temporary Consumed Items">
    public ArrayList<HashMap<String, String>> getTempConsumed() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        if (userlang.equalsIgnoreCase("en"))
            selectQuery = "SELECT tmp.Id, ifnull(sm.Name,rm.Name||' '||rm.Uom) AS Name, tmp.Quantity FROM OutletConversionConsumedTemp tmp LEFT OUTER JOIN SKUMaster sm ON tmp.SKUId = sm.Id LEFT OUTER JOIN RawMaterialMaster rm ON tmp.MaterialId = rm.Id ORDER BY Name";
        else
            selectQuery = "SELECT tmp.Id, ifnull(sm.NameLocal,rm.NameLocal||' '||rm.Uom) AS Name, tmp.Quantity FROM OutletConversionConsumedTemp tmp LEFT OUTER JOIN SKUMaster sm ON tmp.SKUId = sm.Id LEFT OUTER JOIN RawMaterialMaster rm ON tmp.MaterialId = rm.Id ORDER BY Name";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("Quantity", cursor.getString(2));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Fetch Temporary Produced Items">
    public ArrayList<HashMap<String, String>> getTempProduced() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        if (userlang.equalsIgnoreCase("en"))
            selectQuery = "SELECT tmp.Id, sm.Name , tmp.Quantity FROM OutletConversionProducedTemp tmp LEFT OUTER JOIN SKUMaster sm ON tmp.SKUId = sm.Id ORDER BY sm.Name";
        else
            selectQuery = "SELECT tmp.Id, sm.NameLocal , tmp.Quantity FROM OutletConversionProducedTemp tmp LEFT OUTER JOIN SKUMaster sm ON tmp.SKUId = sm.Id ORDER BY sm.Name";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Name", cursor.getString(1));
            map.put("Quantity", cursor.getString(2));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Code to fetch current Credit Amount">
    public String getCreditAmount(String userId) {
        String creditAmount="0.00";
        selectQuery = "SELECT Quantity FROM OutletLedger WHERE Id = '" + userId + "' ";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                creditAmount = String.valueOf(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return creditAmount;
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete consumption from temporary table by Id">
    public void deleteTempConsumption(String id) {
        db.execSQL("Delete FROM OutletConversionConsumedTemp WHERE Id ='"+id+"';");
    }
    //</editor-fold>

    //<editor-fold desc="Code to delete production from temporary table by Id">
    public void deleteTempProduction(String id) {
        db.execSQL("Delete FROM OutletConversionProducedTemp WHERE Id ='"+id+"';");
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert pending delivery status">
    public String Insert_DeliveryConfirmStatus(String status) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Status", status);

            db.insert("DeliveryConfirmStatus", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Inventory Data in RawMaterialLiveInventory Table">
    public String Insert_RawMaterialLiveInventory(String id, String name, String quantity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("Quantity", quantity);
            db.insert("RawMaterialLiveInventory", null, newValues);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Inventory Data in SKULiveInventory Table">
    public String Insert_SKULiveInventory(String id, String name, String quantity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("Quantity", quantity);
            db.insert("SKULiveInventory", null, newValues);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Credit Ledger Data in OutletLedger Table">
    public String Insert_OutletLedger(String id, String quantity) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Quantity", quantity);
            db.insert("OutletLedger", null, newValues);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Expense Head Data in ExpenseHead Table">
    public String Insert_ExpenseHead(String id, String name, String nameLocal) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("Id", id);
            newValues.put("Name", name);
            newValues.put("NameLocal", nameLocal);
            db.insert("ExpenseHead", null, newValues);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Outlet Payment Receipt Data in OutletPaymentReceipt Table">
    public String Insert_OutletPaymentReceipt(String customerId, String amount,String uniqueId) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CustomerId", customerId);
            newValues.put("Amount", amount);
            newValues.put("AndroidDate", getDateTime());
            newValues.put("UniqueId", uniqueId);
            db.insert("OutletPaymentReceipt", null, newValues);

            db.execSQL("UPDATE OutletLedger SET Quantity = Quantity + " + Double.parseDouble(amount) + " WHERE Id = '" + customerId + "' ");
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Code to insert Expense Data in Expense Booking Table">
    public String Insert_ExpenseBooking(String customerId, String expenseHeadId,String amount,String remarks,String uniqueId) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("CustomerId", customerId);
            newValues.put("ExpenseHeadId", expenseHeadId);
            newValues.put("Amount", amount);
            newValues.put("Remarks", remarks);
            newValues.put("AndroidDate", getDateTime());
            newValues.put("UniqueId", uniqueId);
            db.insert("ExpenseBooking", null, newValues);

            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Method to Fetch Outlet Payment Receipts">
    public ArrayList<HashMap<String, String>> getOutletPayments() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
            selectQuery = "SELECT AndroidDate, Amount FROM OutletPaymentReceipt ORDER BY AndroidDate DESC";

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Date", cursor.getString(0));
            map.put("Amount", cursor.getString(1));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to fetch UnSync Outlet Payments">
    public ArrayList<HashMap<String, String>> getUnSyncOutletPayment() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();

        selectQuery = "SELECT UniqueId, CustomerId,  Amount, AndroidDate FROM OutletPaymentReceipt WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("CustomerId", cursor.getString(1));
            map.put("Amount", cursor.getString(2));
            map.put("AndroidDate", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to fetch UnSync Outlet Expense">
    public ArrayList<HashMap<String, String>> getUnSyncOutletExpense() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();

        selectQuery = "SELECT UniqueId, CustomerId,ExpenseHeadId,  Amount, AndroidDate, Remarks FROM ExpenseBooking WHERE IsSync IS NULL";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("CustomerId", cursor.getString(1));
            map.put("ExpenseHeadId", cursor.getString(2));
            map.put("Amount", cursor.getString(3));
            map.put("TransactionDate", cursor.getString(4));
            map.put("Remarks", cursor.getString(5));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Method to Fetch Data For Synchronizing Stock Conversion">
    public ArrayList<HashMap<String, String>> getConversionForSync() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
            selectQuery = "SELECT MaterialId, SKUId, Quantity,'C' FROM OutletConversionConsumedTemp UNION ALL SELECT 0, SKUId, Quantity,'P'  FROM OutletConversionProducedTemp";

        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("RawMaterialId", cursor.getString(0));
            map.put("SkuId", cursor.getString(1));
            map.put("Quantity", cursor.getString(2));
            map.put("SKUType", cursor.getString(3));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="To Update outlet Sale IsSync flag">
    // To Update outlet Sale IsSync flag
    public String UpdateOutletSaleIsSync() {
        try {
            String query = "UPDATE OutletSale SET IsSync = '1'";
            db.execSQL(query);
            result = "success";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="getUnSyncOutletSale">
    // To get all unsync outlet sale
    public ArrayList<HashMap<String, String>> getUnSyncOutletSale() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT Id, UniqueId, CreateBy, CustomerId, SaleType, CreateDate, Imei FROM OutletSale WHERE isSync='0'";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("UniqueId", cursor.getString(1));
            map.put("CreateBy", cursor.getString(2));
            map.put("CustomerId", cursor.getString(3));
            map.put("SaleType", cursor.getString(4));
            map.put("SaleDate", cursor.getString(5));
            map.put("Imei", cursor.getString(6));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="To get all unsync outlet sale details">
    public ArrayList<HashMap<String, String>> getUnSyncOutletSaleDetail() {
        ArrayList<HashMap<String, String>> wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT main.UniqueId, det.SkuId, det.Rate, det.SaleRate, det.SaleQty FROM OutletSaleDetail det, OutletSale main WHERE main.Id = det.OutletSaleId AND main.IsSync = '0' ";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("UniqueId", cursor.getString(0));
            map.put("SkuId", cursor.getString(1));
            map.put("Rate", cursor.getString(2));
            map.put("SaleRate", cursor.getString(3));
            map.put("Qty", cursor.getString(4));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="Insert_OutletSale">
    // To insert outlet sale records
    public String Insert_OutletSale(String uniqueId, String customerId, String customer, String saleType, String createBy, String imei) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("UniqueId", uniqueId);
            newValues.put("CustomerId", customerId);
            newValues.put("Customer", customer);
            newValues.put("SaleType", saleType);
            newValues.put("CreateBy", createBy);
            newValues.put("CreateDate", getDateTime());
            newValues.put("Imei", imei);
            newValues.put("IsSync", "0");

            long id = db.insert("OutletSale", null, newValues);
            result = "success~" + id + "~" + uniqueId;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Insert_OutletSaleDetail">
    // To insert outlet sale detail records
    public String Insert_OutletSaleDetail(String outletSaleId, String skuId, String sku, String rate, String saleRate, String Qty, String saleQty) {
        try {
            result = "fail";
            newValues = new ContentValues();
            newValues.put("OutletSaleId", outletSaleId);
            newValues.put("SkuId", skuId);
            newValues.put("Sku", sku);
            newValues.put("Rate", rate);
            newValues.put("SaleRate", saleRate);
            newValues.put("Qty", Qty);
            newValues.put("SaleQty", saleQty);

            long id = db.insert("OutletSaleDetail", null, newValues);
            result = "success~" + id;
            //db.execSQL("UPDATE CustomerLedger SET Balance = Balance - " + Double.parseDouble(saleRate) * Double.parseDouble(saleQty) + " WHERE CustomerId = '" + customerId + "'");
            db.execSQL("UPDATE OutletInventory SET Quantity = Quantity - " + saleQty + " WHERE SKUId ='" + skuId + "'");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="getOutletSaleInput">
    // To get outlet sale input list
    public ArrayList<HashMap<String, String>> getOutletSaleInput() {
        wordList = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT inv.SkuId, sku.Name, sku.SKU, rt.Rate, inv.Quantity, sku.NameLocal FROM OutletInventory inv, SaleRateMaster rt, SKUMaster sku WHERE inv.SkuId = rt.Id AND inv.SKUId = sku.Id AND CAST(inv.Quantity AS NUMERIC)>0 AND CAST(rt.Rate AS NUMERIC)>0 AND CAST(inv.SKUId AS NUMERIC)>0 ORDER BY LOWER(sku.Name), LOWER(rt.Rate)";
        //Log.i("LPDND", "selectQuery="+selectQuery);
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("id", cursor.getString(0));
            map.put("sku", cursor.getString(1));
            map.put("type", cursor.getString(2));
            map.put("rate", cursor.getString(3));
            map.put("aqty", cursor.getString(4).replace(".0", ""));
            map.put("skuLocal", cursor.getString(5));
            wordList.add(map);
        }
        cursor.close();
        return wordList;
    }
    //</editor-fold>

    //<editor-fold desc="GetOutletSaleSummery">
    //Method to get outlet sale summery
    public ArrayList<HashMap<String, String>> GetOutletSaleSummery(String filter) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT DISTINCT Id, 'OS'||Id, SaleType, CreateDate FROM OutletSale WHERE CustomerId = '"+filter+"' ORDER BY CAST(Id AS NUMERIC) DESC";
        //Log.i("LPDND", "selectQuery="+masterType+":"+selectQuery);
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Id", cursor.getString(0));
            map.put("Code", cursor.getString(1));
            map.put("Name", cursor.getString(2));
            map.put("Date", cursor.getString(3));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="getOutletSaleDetail">
    // To get outlet sale details by id
    public ArrayList<HashMap<String, String>> getOutletSaleDetail(String id) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        selectQuery = "SELECT mm.Name, dd.SaleQty, dd.SaleRate, dd.SaleQty*dd.SaleRate, mm.NameLocal FROM OutletSaleDetail dd, SKUMaster mm WHERE mm.Id = dd.skuId AND dd.OutletSaleId ='" + id + "' ORDER BY LOWER(mm.Name)";
        cursor = db.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, String>();
            map.put("Item", cursor.getString(0));
            map.put("Qty", cursor.getString(1).replace(".0",""));
            map.put("Rate", cursor.getString(2));
            map.put("Amount", cursor.getString(3));
            map.put("ItemLocal", cursor.getString(4));
            list.add(map);
        }
        cursor.close();
        return list;
    }
    //</editor-fold>

    //<editor-fold desc="To Get Delivery Confirm Status ">
    public String GetDeliveryConfirmStatus() {
        String status = "";
        selectQuery = "SELECT Status FROM DeliveryConfirmStatus";
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                status = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return status;
    }
    //</editor-fold>
}
