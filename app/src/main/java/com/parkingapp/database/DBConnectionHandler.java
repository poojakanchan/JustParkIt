package com.parkingapp.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.parkingapp.parser.OperationHoursBean;
import com.parkingapp.parser.SFParkBean;
import com.parkingapp.utility.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.InputStreamReader;

import java.util.zip.ZipInputStream;
import java.util.List;

/**
 * Created by nayanakamath on 4/24/15.
 * Singleton class handles DataBase connection and queries to SQLite.
 * /

 /*
 * File history
 * 1.Pooja Kanchan
 * Added a parsing method parseSQL for sf_street_cleaning.sql file. It parses data from parser and stores in database
 * Added table PARKING_FAVORITES to store favorite parking locations. The table has name as a primary key to avoid duplicate entries.
 * Added methods insertParkingInfo, getFavouriteParkingSpots, removeParkingInfo to perform insert, select and delete operations on the table
 * Changed the class to singleton.
 *
 * 2. Nayana Kamath
 * Solved issue with repeated entries into database
 * Added whole SF Street Cleaning data into StreetCleaning table
 * Solved issue with Streetnames having "'" like O'Farrell street
 */


public class DBConnectionHandler extends SQLiteOpenHelper {

    private Context context;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME ="StreetCleaningDB";

    //private static final String TABLE_NAME ="Sfsu_StreetCleaning";
    private static final String TABLE_NAME ="StreetCleaning";

    //private static final String STREET_CLEANING_TABLE_NAME ="Sfsu_StreetCleaning";
    private static final String PARKING_FAVORITES_TABLE_NAME ="Parking_favorites";

    private static DBConnectionHandler dbConnectionHandler;

    private DBConnectionHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    /**
     * static method to get an instance of DBConnectionHandler class.
     * @param context application context to be used
     * @return static instance of the class.
     */
    public static DBConnectionHandler getDBHandler(Context context){
        if(dbConnectionHandler == null){
            dbConnectionHandler = new DBConnectionHandler(context);
            dbConnectionHandler.onCreate(dbConnectionHandler.getWritableDatabase());
        }
        return dbConnectionHandler;
    }

    /**
     * parses the sql file and executes insert queries to table STREET_CLEANING
     * @param sqLiteDatabase database to be used
     * @param assetManager reference to assetManager to accesss the SQL file
     */

    public void parseSQL(SQLiteDatabase sqLiteDatabase, AssetManager assetManager) {

        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("street_cleaning.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipInputStream zipInputStream=new ZipInputStream(inputStream);

        try{
            while ((zipInputStream.getNextEntry())!=null){
                BufferedReader in = new BufferedReader(new InputStreamReader(zipInputStream));
                String line;
                while ((line=in.readLine()) != null) {
                  sqLiteDatabase.execSQL(line);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * method which is called once when the app is loaded for the first time.
     * In this method, tables  PARKING_FAVORITES and STREETCLEANING are created and street cleaning.csv file is parsed and
     * loaded into table STREETCLEANING table
     * @param db database to be used for the operations.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
   //    db.execSQL("DROP TABLE " + PARKING_FAVORITES_TABLE_NAME);
        db.execSQL(" CREATE TABLE IF NOT EXISTS "+ TABLE_NAME+"(WeekDay VARCHAR, RightLeft VARCHAR, Corridor VARCHAR, FromHour VARCHAR, ToHour VARCHAR, Holidays CHAR, Week1OfMonth CHAR, Week2OfMonth CHAR, Week3OfMonth CHAR, Week4OfMonth CHAR, Week5OfMonth CHAR, LF_FADD NUMBER, LF_TOADD NUMBER, RT_TOADD NUMBER, RT_FADD NUMBER, STREETNAME VARCHAR, ZIP_CODE NUMBER, NHOOD VARCHAR);");
        db.execSQL(" CREATE TABLE IF NOT EXISTS "+ PARKING_FAVORITES_TABLE_NAME+"(name  VARCHAR PRIMARY KEY, type VARCHAR, address VARCHAR, Contact VARCHAR, oprhours VARCHAR, latitude NUMBER, longitude NUMBER);");

        db.execSQL("CREATE INDEX IF NOT EXISTS street_cleaning_index ON " + TABLE_NAME + "(STREETNAME, ZIP_CODE);");

        // Checks if Table is Null
        if(check_TableIsNull(db,TABLE_NAME)) {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            AssetManager assetManager = contextWrapper.getAssets();

                parseSQL(db, assetManager);

           }
        }

    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int newVersion) {
        db.execSQL("DROP TABLE " +TABLE_NAME);
        db.execSQL("DROP TABLE " + PARKING_FAVORITES_TABLE_NAME);
        onCreate(db);
    }

    private boolean check_TableIsNull(SQLiteDatabase db,String TABLE_NAME) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + "", null);
        if (cursor != null) {
            cursor.moveToFirst();
            System.out.println("No of records= " + cursor.getInt(0));
            if (cursor.getInt(0) == 0) {
                //cursor.close();
                System.out.println("Table is Null");
                return true;
            }
            cursor.close();
        }
        System.out.println("Table is not Null");
        return false;
    }

    public ArrayList<StreetCleaningDataBean> getRequiredAddress(Number SUBSTREET, String STREETNAME, Number ZIP_CODE) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        ArrayList<StreetCleaningDataBean> getAddress = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+TABLE_NAME+" where STREETNAME='"+ STREETNAME.replaceAll("'","/") +"'and ZIP_CODE="+ZIP_CODE
                +" and ((LF_FADD <="+SUBSTREET + " and LF_TOADD >="+SUBSTREET + ") or (RT_FADD <="+SUBSTREET + " and RT_TOADD >="+SUBSTREET + "))";

         Cursor c = sqLiteDatabase.rawQuery(selectQuery, null);
         if (c.moveToFirst()) {

            do {
                StreetCleaningDataBean streetCleaningDataBean = new StreetCleaningDataBean();
                streetCleaningDataBean.setWeekDay(c.getString(0));
                streetCleaningDataBean.setRightLeft(c.getString(1));
                streetCleaningDataBean.setCorridor(c.getString(2));
                streetCleaningDataBean.setFromHour(c.getString(3));
                streetCleaningDataBean.setToHour(c.getString(4));
                streetCleaningDataBean.setHolidays(c.getString(5));
                streetCleaningDataBean.setWeek1OfMonth(c.getString(6));
                streetCleaningDataBean.setWeek2OfMonth(c.getString(7));
                streetCleaningDataBean.setWeek3OfMonth(c.getString(8));
                streetCleaningDataBean.setWeek4OfMonth(c.getString(9));
                streetCleaningDataBean.setWeek5OfMonth(c.getString(10));
                streetCleaningDataBean.setLF_FADD(Integer.parseInt(c.getString(11)));
                streetCleaningDataBean.setLF_TOADD(Integer.parseInt(c.getString(12)));
                streetCleaningDataBean.setRT_TOADD(Integer.parseInt(c.getString(13)));
                streetCleaningDataBean.setRT_FADD(Integer.parseInt(c.getString(14)));
                streetCleaningDataBean.setSTREETNAME(c.getString(15));
                streetCleaningDataBean.setZIP_CODE(Integer.parseInt(c.getString(16)));
                streetCleaningDataBean.setNHOOD(c.getString(17));
                getAddress.add(streetCleaningDataBean);

                String log = "Weekday: " + streetCleaningDataBean.getWeekDay() + " ,ZipCode: " + streetCleaningDataBean.getZIP_CODE() + " ,STREETNAME: " + streetCleaningDataBean.getSTREETNAME();
                // Writing Retrieved data to log
                Log.d("Data: ", log);
            } while (c.moveToNext());
        }
        return getAddress;
    }

    /**
     * inserts passed entries to the table PARKING_FAVORITES.
     * The method is called when a user clicks on 'add to favorites' button.
     * @param list List of type SFParkBean to be inserted into table PARKING_FAVORITES
     */
    public void insertParkingInfo(List<SFParkBean>list) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int count=0;

            for (SFParkBean bean : list) {
                try {
                    StringBuilder query = buildInsertQuery(bean);
                    sqLiteDatabase.execSQL(query.toString());
                    count++;
                    if (count == Constants.LIMIT_FOR_PARKING_DISPLAY) {
                        break;
                    }
                }  catch(SQLiteConstraintException e) {
                        Log.d("Skipping duplicate row : ", e.getMessage());
                }
            }
    }

    /**
     * Builds insert query for PARKING_FAVORITES table. The values are extracted from passed argument
     * @param bean the SFParkBean object to be inserted.
     * @return Insert query
     */
    private StringBuilder buildInsertQuery(SFParkBean bean) {

        StringBuilder insertQuery = new StringBuilder();
        insertQuery.append("INSERT INTO ");
        insertQuery.append(PARKING_FAVORITES_TABLE_NAME);
        insertQuery.append(" VALUES(");
        appendString(insertQuery, bean.getName());
        appendString(insertQuery,bean.getType());
        appendString(insertQuery,bean.getAddress());
        appendString(insertQuery,bean.getContactNumber());

        List<OperationHoursBean> operationHoursBeanList = bean.getOperationHours();
        if(operationHoursBeanList != null && operationHoursBeanList.size() > 0) {
            insertQuery.append("\"");
            for (OperationHoursBean oprBean : operationHoursBeanList) {
               if(oprBean.getFromDay()!= null) {
                   insertQuery.append(oprBean.getFromDay());
               } else {
                   insertQuery.append("null");
               }
                insertQuery.append(",");

                if(oprBean.getToDay()!= null) {
                    insertQuery.append(oprBean.getToDay());
                }else {
                    insertQuery.append("null");
                }
                insertQuery.append(",");

                if(oprBean.getStartTime()!= null) {
                    insertQuery.append(oprBean.getStartTime());
                } else {
                    insertQuery.append("null");
                }
                insertQuery.append(",");

                if(oprBean.getEndTime()!= null) {
                    insertQuery.append(oprBean.getEndTime());
                }else {
                    insertQuery.append("null");
                }
                insertQuery.append(";");

            }
            insertQuery.deleteCharAt(insertQuery.length()-1);
            insertQuery.append("\"");
        } else {
            insertQuery.append("null");
        }
        insertQuery.append(",");
        insertQuery.append(bean.getLatitude());
        insertQuery.append(",");
        insertQuery.append(bean.getLongitude());
        insertQuery.append(");");
        Log.d("insert query: " , insertQuery.toString());
        return insertQuery;
    }
    private void appendString(StringBuilder query,String value) {
        if(value == null)
        {
            query.append("null,");
        } else {
            query.append("\"");
            query.append(value.trim());
            query.append("\"");
            query.append(",");
        }
    }

    /**
     *  Performs select query on PARKING_FAVORITES table. Returns all the rows from the table to display
     * @return list of parking locations retrieved from the table PARKING_FAVORITES
     */
    public List<SFParkBean> getFavouriteParkingSpots() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        ArrayList<SFParkBean> SFParkBeanList = new ArrayList<>();
        String selectQuery = "SELECT * FROM "+PARKING_FAVORITES_TABLE_NAME;
        Cursor c = sqLiteDatabase.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                SFParkBean bean = new SFParkBean();
                bean.setName(c.getString(0));
                bean.setType(c.getString(1));
                bean.setAddress(c.getString(2));
                bean.setContactNumber(c.getString(3));
                bean.setLatitude(c.getDouble(5));
                bean.setLongitude(c.getDouble(6));

                String oprHours = c.getString(4);
               if(oprHours !=  null && !oprHours.isEmpty()) {
                   String[] oprArray = oprHours.split(";");
                    List<OperationHoursBean> oprList = new ArrayList<>();
                   for (String opr : oprArray) {
                       OperationHoursBean oprBean = new OperationHoursBean();
                       String[] hours = opr.split(",");
                       if (hours[0] != null && !hours[0].isEmpty() && !hours[0].equalsIgnoreCase("null")) {
                               oprBean.setFromDay(hours[0]);
                       }
                       if (hours[1] != null && !hours[1].isEmpty() && !hours[1].equalsIgnoreCase("null")) {
                           oprBean.setToDay(hours[1]);
                       }
                       if (hours[2] != null && !hours[2].isEmpty() && !hours[2].equalsIgnoreCase("null")) {
                           oprBean.setStartTime(hours[2]);
                       }
                       if (hours[3] != null && !hours[3].isEmpty() && !hours[3].equalsIgnoreCase("null")) {
                           oprBean.setEndTime(hours[3]);
                       }
                       oprList.add(oprBean);
                   }
                   bean.setOperationHours(oprList);
               }
                SFParkBeanList.add(bean);
                String log = "Name :" + bean.getName() + " Type : " + bean.getType() + " Address: " + bean.getAddress()
                        + " Contact " + bean.getContactNumber() + " latitude " + bean.getLatitude() + " longitude: " + bean.getLongitude();
                        Log.d("Data: ", log);
            } while (c.moveToNext());
        }
        return SFParkBeanList;
    }

    /**
     * Removes entry with passed name from PARKING_FAVORITES table
     * @param name name of the entry to be removed.
     */
    public void removeParkingInfo(String name) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "DELETE FROM " + PARKING_FAVORITES_TABLE_NAME + " WHERE name = '" + name + "';";
        sqLiteDatabase.execSQL(query);
    }
}
