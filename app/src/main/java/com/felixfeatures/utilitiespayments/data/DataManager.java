package com.felixfeatures.utilitiespayments.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class provides operations with list of srvices and services payments.
 * (getting, adding, removing, saving to database etc.)
 */
public class DataManager {

    public final static String TAG = "DataManager_log";

    private static final int FIXED_RATE_TYPE = 0;
    private static final int SIMPLE_RATE_TYPE = 1;
    private static final int DIFFERENTIATED_RATE_TYPE = 2;

    static final String SERVICES_TABLE = "SERVICES";
    static final String PAYMENTS_TABLE = "PAYMENTS";

    private static DBHelper dbHelper;
    private static List<Service> services;
    private static DataManager manager;

    private SQLiteDatabase db;

    static {
        manager = new DataManager();
    }

    private DataManager() {
    }

    public static DataManager getInstance(Context context) {
        dbHelper = new DBHelper(context, 1);
        return manager;
    }

    private Service getServiceFromDB(Cursor cursor) {
        int serviceIdColIndex = cursor.getColumnIndex("ID");
        int nameColIndex = cursor.getColumnIndex("NAME");
        int rateTypeColIndex = cursor.getColumnIndex("RATE_TYPE");

        int serviceId = cursor.getInt(serviceIdColIndex);
        String serviceName = cursor.getString(nameColIndex);
        int rateTypeInt = cursor.getInt(rateTypeColIndex);

        Service.RateType serviceRateType = null;
        switch (rateTypeInt) {
            case FIXED_RATE_TYPE: {
                serviceRateType = Service.RateType.Fixed;
                break;
            }
            case SIMPLE_RATE_TYPE: {
                serviceRateType = Service.RateType.Simple;
                break;
            }
            case DIFFERENTIATED_RATE_TYPE: {
                serviceRateType = Service.RateType.Differentiated;
                break;
            }
        }
        Log.d(TAG,
                "getServiceFromDB(Cursor cursor)" +
                        ". service ID = " + serviceId +
                        ", name = " + serviceName +
                        ", rateType = " + serviceRateType.name());

        Service service = new Service(serviceName, serviceRateType);

        // Get service payments from DB and put them into service
        String selection = "SERVICE_ID=" + serviceId;
        Cursor paymentsCursor = db.query(PAYMENTS_TABLE, null, selection, null, null, null, null);
        if (paymentsCursor.moveToFirst()) {
            do {
                Payment payment = getPaymentFromDB(service, paymentsCursor);
                Period paymentPeriod = payment.getPeriod();
                service.addPayment(paymentPeriod, payment);
            } while (paymentsCursor.moveToNext());
        } else {
            Log.d(TAG, "getServiceFromDB(Cursor cursor). Table PAYMENTS has no rows for service " + service.getName());
        }
        paymentsCursor.close();
        return service;
    }

    private Payment getPaymentFromDB(Service service, Cursor cursor) {
        int paymentMonthColIndex = cursor.getColumnIndex("PERIOD_MONTH");
        int paymentYearColIndex = cursor.getColumnIndex("PERIOD_YEAR");
        int paymentRate1ColIndex = cursor.getColumnIndex("RATE1");
        int paymentRate2ColIndex = cursor.getColumnIndex("RATE2");
        int paymentRate3ColIndex = cursor.getColumnIndex("RATE3");
        int paymentBorder12ColIndex = cursor.getColumnIndex("BORDER12");
        int paymentBorder23ColIndex = cursor.getColumnIndex("BORDER23");
        int paymentPrevCounterDataColIndex = cursor.getColumnIndex("PREV_COUNTER_DATA");
        int paymentCurrCounterDataColIndex = cursor.getColumnIndex("CURR_COUNTER_DATA");

        int month = cursor.getInt(paymentMonthColIndex);
        int year = cursor.getInt(paymentYearColIndex);
        double rate1 = cursor.getDouble(paymentRate1ColIndex);
        double rate2 = cursor.getDouble(paymentRate2ColIndex);
        double rate3 = cursor.getDouble(paymentRate3ColIndex);
        int border12 = cursor.getInt(paymentBorder12ColIndex);
        int border23 = cursor.getInt(paymentBorder23ColIndex);
        int prevCounterData = cursor.getInt(paymentPrevCounterDataColIndex);
        int currCounterData = cursor.getInt(paymentCurrCounterDataColIndex);

        Period paymentPeriod = new Period(month, year);

        Log.d(TAG,
                "getPaymentFromDB(Service service, Cursor cursor). payment month = " + paymentPeriod +
                        ", rate1 = " + rate1 +
                        ", rate2 = " + rate2 +
                        ", rate3 = " + rate3 +
                        ", border12 = " + border12 +
                        ", border23 = " + border23 +
                        ", previous counter data = " + prevCounterData +
                        ", current counter data = " + currCounterData);
        Payment payment = null;
        switch (service.getRateType()) {
            case Fixed: {
                FixedUtilityRate utilityRate = new FixedUtilityRate(rate1);
                payment = new FixedPayment(paymentPeriod, utilityRate);
                break;
            }
            case Simple: {
                SimpleCounterUtilityRate utilityRate = new SimpleCounterUtilityRate(rate1);
                payment = new CountedPayment(paymentPeriod, prevCounterData, currCounterData, utilityRate);
                break;
            }
            case Differentiated: {
                DiffCounterUtilityRate utilityRate = new DiffCounterUtilityRate(rate1, border12, rate2, border23, rate3);
                payment = new CountedPayment(paymentPeriod, prevCounterData, currCounterData, utilityRate);
                break;
            }
        }
        return payment;
    }

    private void savePaymentsToDB(int serviceID) {
        Service service = getService(serviceID);
        List<Payment> payments = getPayments(serviceID);
        for (int i = 0; i < payments.size(); i++) {
            int paymentMonth = 0;
            int paymentYear = 0;
            double rate1 = 0;
            double rate2 = 0;
            double rate3 = 0;
            int border12 = 0;
            int border23 = 0;
            int prevCounterData = 0;
            int currCounterData = 0;
            Service.RateType rateType = service.getRateType();
            switch (rateType) {
                case Fixed: {
                    FixedPayment payment = (FixedPayment) payments.get(i);
                    Period period = payment.getPeriod();
                    FixedUtilityRate utilityRate = (FixedUtilityRate) payment.getRate();
                    paymentMonth = period.getMonth();
                    paymentYear = period.getYear();
                    rate1 = utilityRate.getRateValue();
                    break;
                }
                case Simple: {
                    CountedPayment payment = (CountedPayment) payments.get(i);
                    Period period = payment.getPeriod();
                    SimpleCounterUtilityRate utilityRate = (SimpleCounterUtilityRate) payment.getRate();
                    paymentMonth = period.getMonth();
                    paymentYear = period.getYear();
                    rate1 = utilityRate.getRateValue();
                    prevCounterData = payment.getPreviousCounterData();
                    currCounterData = payment.getCurrentCounterData();
                    break;
                }
                case Differentiated: {
                    CountedPayment payment = (CountedPayment) payments.get(i);
                    Period period = payment.getPeriod();
                    DiffCounterUtilityRate utilityRate = (DiffCounterUtilityRate) payment.getRate();
                    paymentMonth = period.getMonth();
                    paymentYear = period.getYear();
                    rate1 = utilityRate.getRateValue1();
                    rate2 = utilityRate.getRateValue2();
                    rate3 = utilityRate.getRateValue3();
                    border12 = utilityRate.getBorder12();
                    border23 = utilityRate.getBorder23();
                    prevCounterData = payment.getPreviousCounterData();
                    currCounterData = payment.getCurrentCounterData();
                    break;
                }
            }
            String query = "INSERT INTO " + PAYMENTS_TABLE + " VALUES(" +
                    "(SELECT ID FROM " + SERVICES_TABLE + " WHERE ID=" + (serviceID + 1) + "), " +
                    paymentMonth + ", " +
                    paymentYear + ", " +
                    rate1 + ", " +
                    rate2 + ", " +
                    rate3 + ", " +
                    border12 + ", " +
                    border23 + ", " +
                    prevCounterData + ", " +
                    currCounterData + ");";
            Log.d(TAG, "savePaymentsToDB(int serviceID). " + query);
            db.execSQL(query);
            Log.d(TAG, "savePaymentsToDB(int serviceID). Payment saved to database: " + service.getName() + ", " + rateType.name());
        }
    }

    private void recreateTables() {
        Log.d(TAG, "recreateTables()");
        db = dbHelper.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SERVICES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PAYMENTS_TABLE);
        dbHelper.createTables(db);
        dbHelper.close();
    }

    private int getMaxYear() {
        int maxYear = 0;
        for (Service service : services) {
            List<Payment> payments = service.getPayments();
            for (Payment payment : payments) {
                int paymentYear = payment.getPeriod().getYear();
                if (paymentYear > maxYear) {
                    maxYear = paymentYear;
                }
            }
        }
        return maxYear;
    }

    private int getMinYear() {
        int minYear = Integer.MAX_VALUE;
        for (Service service : services) {
            List<Payment> payments = service.getPayments();
            for (Payment payment : payments) {
                int paymentYear = payment.getPeriod().getYear();
                if (paymentYear < minYear) {
                    minYear = paymentYear;
                }
            }
        }
        return minYear;
    }

    private int getMaxMonth(int year) {
        int maxMonth = 0;
        for (Service service : services) {
            List<Payment> payments = service.getPayments();
            for (Payment payment : payments) {
                int paymentYear = payment.getPeriod().getYear();
                if (paymentYear == year) {
                    int paymentMonth = payment.getPeriod().getMonth();
                    if (paymentMonth > maxMonth) {
                        maxMonth = paymentMonth;
                    }
                }
            }
        }
        return maxMonth;
    }

    private int getMinMonth(int year) {
        int minMonth = 0;
        for (Service service : services) {
            List<Payment> payments = service.getPayments();
            for (Payment payment : payments) {
                int paymentYear = payment.getPeriod().getYear();
                if (paymentYear == year) {
                    int paymentMonth = payment.getPeriod().getMonth();
                    if (paymentMonth < minMonth) {
                        minMonth = paymentMonth;
                    }
                }
            }
        }
        return minMonth;
    }

    /***************
     * DataManager interface
     ***************/

    public List<Service> getServices() {
        Log.d(TAG, "**************************** getServices() ****************************");
        if (services == null) {
            // Add services to services list from database
            Log.d(TAG, "getServices(). Starting to create the list of services");
            readServicesFromDB();
        }
        return services;
    }

    public void readServicesFromDB() {
        services = new ArrayList<>();
        db = dbHelper.getWritableDatabase();
        Cursor servicesCursor = db.query(SERVICES_TABLE, null, null, null, null, null, null);
        if (servicesCursor.moveToFirst()) {
            do {
                Service service = getServiceFromDB(servicesCursor);
                services.add(service);
                Log.d(TAG, "getServices(). Service " + service + " was added to the list of services");
            } while (servicesCursor.moveToNext());
        } else {
            Log.d(TAG, "getServices(). Table SERVICES has no rows");
        }
        servicesCursor.close();
        dbHelper.close();
    }

    public void saveServicesToDB() {
        Log.d(TAG, "**************************** saveServicesToDB() ****************************");
        Log.d(TAG, "Saving services: " + Arrays.toString(services.toArray()));
        recreateTables();
        db = dbHelper.getWritableDatabase();
        for (int serviceID = 0; serviceID < services.size(); serviceID++) {
            Service service = getService(serviceID);
            String serviceName = service.getName();
            Service.RateType rateType = service.getRateType();
            int serviceRateType = -1;
            switch (rateType) {
                case Fixed: {
                    serviceRateType = FIXED_RATE_TYPE;
                    break;
                }
                case Simple: {
                    serviceRateType = SIMPLE_RATE_TYPE;
                    break;
                }
                case Differentiated: {
                    serviceRateType = DIFFERENTIATED_RATE_TYPE;
                    break;
                }
            }
            String query = "INSERT INTO " + SERVICES_TABLE + " VALUES(" +
                    "null, " +
                    "\"" + serviceName + "\", " +
                    serviceRateType + ");";
            Log.d(TAG, "saveServicesToDB(). " + query);
            db.execSQL(query);
            savePaymentsToDB(serviceID);
            Log.d(TAG, "saveServicesToDB(). Service saved to database: " + service);
        }
        dbHelper.close();
    }

    public Service getService(int serviceID) {
        return services.get(serviceID);
    }

    public void addService(String name, Service.RateType rateType) {
        Service service = new Service(name, rateType);
        services.add(service);
        Log.d(TAG, "addService(). " + service + " added");
    }

    public void replaceService(int serviceID, String serviceName) {
        Service service = getService(serviceID);
        service.setName(serviceName);
        Log.d(TAG, "replaceService(). " + service + " renamed");
    }

    public void deleteService(int serviceID) {
        String service = getService(serviceID).toString();
        services.remove(serviceID);
        Log.d(TAG, "deleteService(). " + service + " deleted");
    }

    public void addPayment(Period period, int serviceID, Payment payment) {
        Service service = getService(serviceID);
        service.addPayment(period, payment);
        Log.d(TAG, "addPayment(). Payment added to " + service + ". Period: " + period);
    }

    public List<Payment> getPayments(int serviceID) {
        return getService(serviceID).getPayments();
    }

    public int getPaymentsSize(int serviceID) {
        return getPayments(serviceID).size();
    }

    public Payment getPayment(Period period, int serviceID) {
        Payment result = null;
        Service service = getService(serviceID);
        if (service != null) {
            result = service.getPayment(period);
        }
        return result;
    }

    public Payment getPayment(int serviceID, int paymentID) {
        Payment result = null;
        Service service = getService(serviceID);
        if (service != null) {
            result = service.getPayments().get(paymentID);
        }
        return result;
    }

    public void deletePayment(int serviceID, int paymentID) {
        Service service = services.get(serviceID);
        List<Payment> payments = service.getPayments();
        payments.remove(paymentID);
        service.setPayments(payments);
        Log.d(TAG, "deletePayment(). Payment deleted from " + service);
    }

    public Period getMaxPeriod() {
        int maxYear = getMaxYear();
        int maxMonth = getMaxMonth(maxYear);
        Period maxPeriod = new Period(maxMonth, maxYear);
        Log.d(TAG, "getMaxPeriod(). " + maxPeriod);
        return maxPeriod;
    }

    public Period getMinPeriod() {
        int minYear = getMinYear();
        int minMonth = getMinMonth(minYear);
        Period minPeriod = new Period(minMonth, minYear);
        Log.d(TAG, "getMinPeriod(). " + minPeriod);
        return minPeriod;
    }

    /***************
     * End of DataManager interface
     ***************/
}

/**
 * Nested class provides database management
 */
class DBHelper extends SQLiteOpenHelper {

    final String CREATE_SERVICES_TABLE = "CREATE TABLE "
            + DataManager.SERVICES_TABLE
            + "("
            + "ID integer PRIMARY KEY AUTOINCREMENT, "
            + "NAME text, "
            + "RATE_TYPE integer"
            + ");";
    final String CREATE_PAYMENTS_TABLE = "CREATE TABLE "
            + DataManager.PAYMENTS_TABLE
            + "("
            + "SERVICE_ID integer REFERENCES " + DataManager.SERVICES_TABLE + "(ID), "
            + "PERIOD_MONTH integer, "
            + "PERIOD_YEAR integer, "
            + "RATE1 real, "
            + "RATE2 real, "
            + "RATE3 real, "
            + "BORDER12 integer, "
            + "BORDER23 integer, "
            + "PREV_COUNTER_DATA integer, "
            + "CURR_COUNTER_DATA integer"
            + ");";

    public DBHelper(Context context, int dbVer) {
        super(context, "utility_services_DB.db", null, dbVer);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(DataManager.TAG, "DBHelper.onCreate(SQLiteDatabase db)");
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        db.execSQL("DROP TABLE IF EXISTS tableName");
	    onCreate(db);
        */
    }

    public void createTables(SQLiteDatabase db) {
        Log.d(DataManager.TAG, "DBHelper.createTables(SQLiteDatabase db). " + CREATE_SERVICES_TABLE);
        db.execSQL(CREATE_SERVICES_TABLE);
        Log.d(DataManager.TAG, "DBHelper.createTables(SQLiteDatabase db). " + CREATE_PAYMENTS_TABLE);
        db.execSQL(CREATE_PAYMENTS_TABLE);
        Log.d(DataManager.TAG, "DBHelper.createTables(SQLiteDatabase db). Tables created");
    }
}