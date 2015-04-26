package com.parkingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by nayanakamath on 4/24/15.
 * Class handles DataBase connection and queries to SQLite.
 * Also parses 'street.csv' file and inserts the data to DataBase
 */

public class DBConnectionHandler {

    public void createDB(ContextWrapper contextWrapper) {

        AssetManager assetManager = contextWrapper.getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open("street.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = contextWrapper.openOrCreateDatabase("StreetCleaningDB", Context.MODE_PRIVATE, null);
        db.execSQL("DROP TABLE Sfsu_StreetCleaning");
        db.execSQL("CREATE TABLE IF NOT EXISTS Sfsu_StreetCleaning(WeekDay VARCHAR, RightLeft VARCHAR, Corridor VARCHAR, FromHour NUMBER, ToHour NUMBER, Holidays CHAR, Week1OfMonth CHAR, Week2OfMonth CHAR, Week3OfMonth CHAR, Week4OfMonth CHAR, Week5OfMonth CHAR, LF_FADD NUMBER, LF_TOADD NUMBER, RT_TOADD NUMBER, RT_FADD NUMBER, STREETNAME VARCHAR, ZIP_CODE NUMBER, NHOOD VARCHAR);");
 //       db.execSQL("CREATE TABLE IF NOT EXISTS Sfsu_StreetCleaning(WeekDay VARCHAR, RightLeft VARCHAR, Corridor VARCHAR, FromHour VARCHAR, ToHour VARCHAR, Holidays VARCHAR, Week1OfMonth VARCHAR, Week2OfMonth VARCHAR, Week3OfMonth VARCHAR, Week4OfMonth VARCHAR, Week5OfMonth VARCHAR, LF_FADD VARCHAR, LF_TOADD VARCHAR, RT_TOADD VARCHAR, RT_FADD VARCHAR, STREETNAME VARCHAR, ZIP_CODE VARCHAR, NHOOD VARCHAR);");

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";

        db.beginTransaction();

        try {

            while((line = br.readLine())!= null) {

               String[] columns = line.split(",");

               ContentValues cv = new ContentValues(columns.length);

               cv.put("WeekDay", columns[0].trim());
               cv.put("RightLeft", columns[1].trim());
               cv.put("Corridor", columns[2].trim());
               cv.put("FromHour", columns[3].trim());
               cv.put("ToHour", columns[4].trim());
               cv.put("Holidays", columns[5].trim());
               cv.put("Week1OfMonth", columns[6].trim());
               cv.put("Week2OfMonth", columns[7].trim());
               cv.put("Week3OfMonth", columns[8].trim());
               cv.put("Week4OfMonth", columns[9].trim());
               cv.put("Week5OfMonth", columns[10].trim());
               cv.put("LF_FADD", columns[11].trim());
               cv.put("LF_TOADD", columns[12].trim());
               cv.put("RT_TOADD", columns[13].trim());
               cv.put("RT_FADD", columns[14].trim());
               cv.put("STREETNAME", columns[15].trim());
               cv.put("ZIP_CODE", columns[16].trim());
               cv.put("NHOOD", columns[17].trim());

               long insertState = db.insert("Sfsu_StreetCleaning", null, cv);
               Log.d("", "Insert state: " + insertState);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d("Transaction Successful", "!");
    }
}