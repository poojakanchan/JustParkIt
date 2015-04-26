package com.parkingapp.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.parkingapp.utility.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by nayanakamath on 4/24/15.
 * Class handles DataBase connection and queries to SQLite.
/
 /*
 File history
 1. Pooja K
 * Added a parsing method parseSQL for sf_street_cleaning.sql file. It parses data from parser and stores in database
 */

public class DBConnectionHandler {

    public void parseSQL(SQLiteDatabase db,AssetManager assetManager) {

        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(Constants.SQL_STREET_CLEANING_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(inputStream != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;

            try {

                while ((line = br.readLine()) != null) {
                    db.execSQL(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
   }

    public String createDB(ContextWrapper contextWrapper) {

        AssetManager assetManager = contextWrapper.getAssets();

        SQLiteDatabase db = contextWrapper.openOrCreateDatabase("StreetCleaningDB", Context.MODE_PRIVATE, null);
       db.execSQL("DROP TABLE Sfsu_StreetCleaning");
        db.execSQL(" CREATE TABLE IF NOT EXISTS Sfsu_StreetCleaning(WeekDay VARCHAR, RightLeft VARCHAR, Corridor VARCHAR, FromHour VARCHAR, ToHour VARCHAR, Holidays CHAR, Week1OfMonth CHAR, Week2OfMonth CHAR, Week3OfMonth CHAR, Week4OfMonth CHAR, Week5OfMonth CHAR, LF_FADD NUMBER, LF_TOADD NUMBER, RT_TOADD NUMBER, RT_FADD NUMBER, STREETNAME VARCHAR, ZIP_CODE NUMBER, NHOOD VARCHAR);");

        parseSQL(db,assetManager);
       Cursor c = db.rawQuery("SELECT * FROM Sfsu_StreetCleaning where ZIP_CODE=94132 ;", null); // street name & from two three field & zip code

        StringBuilder buffer = new StringBuilder();

        while (c.moveToNext()) {

            buffer.append("name: " + c.getString(0) + "\n");
            buffer.append("WeekDay: " + c.getString(1) + "\n\n");
        }
        c.close();

        db.close();
        return buffer.toString();
    }
}