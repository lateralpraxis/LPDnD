package lateralpraxis.lpdnd;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private DatabaseAdapter databaseAdapter;

    public DatabaseHelper(Context context, String name, CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    public DatabaseHelper(Context context) {

        super(context, DatabaseAdapter.DATABASE_NAME, null,
                DatabaseAdapter.DATABASE_VERSION);
        databaseAdapter = new DatabaseAdapter(context);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase _db) {
        _db.execSQL(DatabaseAdapter.Exceptions_CREATE);
        _db.execSQL(DatabaseAdapter.Item_CREATE);
        _db.execSQL(DatabaseAdapter.Centre_CREATE);
        _db.execSQL(DatabaseAdapter.ConversionCentre_CREATE);
        _db.execSQL(DatabaseAdapter.MISCompany_CREATE);
        _db.execSQL(DatabaseAdapter.Msg_CREATE);
        _db.execSQL(DatabaseAdapter.Demand_CREATE);
        _db.execSQL(DatabaseAdapter.DemandDetails_CREATE);
        _db.execSQL(DatabaseAdapter.ViewDemand_CREATE);
        _db.execSQL(DatabaseAdapter.Route_CREATE);
        _db.execSQL(DatabaseAdapter.Vehicle_CREATE);
        _db.execSQL(DatabaseAdapter.Bank_CREATE);
        _db.execSQL(DatabaseAdapter.ViewDemandDetails_CREATE);
        _db.execSQL(DatabaseAdapter.Company_CREATE);
        _db.execSQL(DatabaseAdapter.DeliveryInputDemand_CREATE);
        _db.execSQL(DatabaseAdapter.DeliveryInput_CREATE);
        _db.execSQL(DatabaseAdapter.Delivery_CREATE);
        _db.execSQL(DatabaseAdapter.DeliveryDetail_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerRate_CREATE);
        _db.execSQL(DatabaseAdapter.ProductMaster_CREATE);
        _db.execSQL(DatabaseAdapter.CompanyRate_CREATE);

        _db.execSQL(DatabaseAdapter.CustomerType_CREATE);
        _db.execSQL(DatabaseAdapter.Country_CREATE);
        _db.execSQL(DatabaseAdapter.State_CREATE);
        _db.execSQL(DatabaseAdapter.City_CREATE);
        _db.execSQL(DatabaseAdapter.PinCode_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerRegistration_CREATE);

        _db.execSQL(DatabaseAdapter.Complaint_CREATE);

        _db.execSQL(DatabaseAdapter.StockReturn_CREATE);
        _db.execSQL(DatabaseAdapter.StockReturnDetail_CREATE);

        _db.execSQL(DatabaseAdapter.CashDepositHeader_CREATE);
        _db.execSQL(DatabaseAdapter.CashDepositDetail_CREATE);
        _db.execSQL(DatabaseAdapter.CashDepositTransaction_CREATE);
        _db.execSQL(DatabaseAdapter.ComplaintCategory_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerLedger_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerPaymentTemp_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerPaymentMaster_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerPaymentDetail_CREATE);
        _db.execSQL(DatabaseAdapter.DemandDate_CREATE);
        _db.execSQL(DatabaseAdapter.DemandCutOff_CREATE);
        _db.execSQL(DatabaseAdapter.RouteVehicleMaster_CREATE);
        _db.execSQL(DatabaseAdapter.CustomerMaster_CREATE);
        _db.execSQL(DatabaseAdapter.TempDocTABLE_CREATE);
        _db.execSQL(DatabaseAdapter.CashDepositDeleteDataTABLE_CREATE);
        //Retail Outlet
        _db.execSQL(DatabaseAdapter.RawMaterialMaster_CREATE);
        _db.execSQL(DatabaseAdapter.OutletInventory_CREATE);
        _db.execSQL(DatabaseAdapter.SKUMaster_CREATE);
        _db.execSQL(DatabaseAdapter.SaleRateMaster_CREATE);
        _db.execSQL(DatabaseAdapter.OutletPrimaryReceipt_CREATE);

        _db.execSQL(DatabaseAdapter.OutletSale_CREATE);
        _db.execSQL(DatabaseAdapter.OutletSaleDetail_CREATE);
        _db.execSQL(DatabaseAdapter.OutletConversionConsumedTemp_CREATE);
        _db.execSQL(DatabaseAdapter.OutletConversionProducedTemp_CREATE);
        _db.execSQL(DatabaseAdapter.OutletConversion_CREATE);
        _db.execSQL(DatabaseAdapter.OutletConversionConsumed_CREATE);
        _db.execSQL(DatabaseAdapter.OutletConversionProduced_CREATE);
        _db.execSQL(DatabaseAdapter.DeliveryConfirmStatus_CREATE);
        _db.execSQL(DatabaseAdapter.SKULiveInventory_CREATE);
        _db.execSQL(DatabaseAdapter.RawMaterialLiveInventory_CREATE);
        _db.execSQL(DatabaseAdapter.OutletLedger_CREATE);
        _db.execSQL(DatabaseAdapter.OutletPaymentReceipt_CREATE);
        _db.execSQL(DatabaseAdapter.ExpenseHead_CREATE);
        _db.execSQL(DatabaseAdapter.ExpenseBooking_CREATE);
        _db.execSQL(DatabaseAdapter.ExpenseConfirmation_CREATE);
        _db.execSQL(DatabaseAdapter.CentreExpenseConfirmation_CREATE);
        _db.execSQL(DatabaseAdapter.ExpenseBookingAccountant_CREATE);
        _db.execSQL(DatabaseAdapter.CentreSKULiveInventory_CREATE);
        _db.execSQL(DatabaseAdapter.CentreUserCentres_CREATE);
        _db.execSQL(DatabaseAdapter.CentreSKU_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {

        onCreate(_db);
    }

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            databaseAdapter.insertExceptions(sqlEx.getMessage(), "DatabaseHelper.java", "getData");
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());
            databaseAdapter.insertExceptions(ex.getMessage(), "DatabaseHelper.java", "getData");
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }


    }

}
