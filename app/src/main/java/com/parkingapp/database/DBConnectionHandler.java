package com.parkingapp.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.parkingapp.utility.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.InputStreamReader;

/**
 * Created by nayanakamath on 4/24/15.
 * Class handles DataBase connection and queries to SQLite.
 * /

 /*
 * File history
 * 1. Pooja K
 * Added a parsing method parseSQL for sf_street_cleaning.sql file. It parses data from parser and stores in database
 * 2. Nayana Kamath
 * Solved issue with repeated entries into database
 */


public class DBConnectionHandler extends SQLiteOpenHelper {

    private Context context;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME ="StreetCleaningDB";
    private static final String TABLE_NAME ="Sfsu_StreetCleaning";

    public DBConnectionHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    public void parseSQL(SQLiteDatabase sqLiteDatabase, AssetManager assetManager) {
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(Constants.SQL_STREET_CLEANING_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    sqLiteDatabase.execSQL(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE IF NOT EXISTS "+ TABLE_NAME+"(WeekDay VARCHAR, RightLeft VARCHAR, Corridor VARCHAR, FromHour VARCHAR, ToHour VARCHAR, Holidays CHAR, Week1OfMonth CHAR, Week2OfMonth CHAR, Week3OfMonth CHAR, Week4OfMonth CHAR, Week5OfMonth CHAR, LF_FADD NUMBER, LF_TOADD NUMBER, RT_TOADD NUMBER, RT_FADD NUMBER, STREETNAME VARCHAR, ZIP_CODE NUMBER, NHOOD VARCHAR);");

        // Checks if Table is Null
        if(check_TableIsNull(db,TABLE_NAME)==true) {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            AssetManager assetManager = contextWrapper.getAssets();
            parseSQL(db, assetManager);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int newVersion) {
        db.execSQL("DROP TABLE " +TABLE_NAME);
        onCreate(db);
    }

    public boolean check_TableIsNull(SQLiteDatabase db,String TABLE_NAME) {
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

    public ArrayList<StreetCleaningDataBean> getRequiredAddress(String STREETNAME, Number ZIP_CODE) {
         SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
         ArrayList<StreetCleaningDataBean> getAddress = new ArrayList<>();
         String selectQuery = "SELECT * FROM "+TABLE_NAME+" where STREETNAME='"+ STREETNAME +"'and ZIP_CODE="+ZIP_CODE;
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
}
