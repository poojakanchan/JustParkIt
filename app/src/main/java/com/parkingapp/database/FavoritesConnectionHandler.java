package com.parkingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Omar on 5/12/2015.
 */

public class FavoritesConnectionHandler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "FavoritesDatabase.db";
    public static final String FAVORITES_TABLE_NAME = "favorites";
    public static final String FAVORITES_COLUMN_ID = "id";
    public static final String FAVORITES_COLUMN_SNIPPET = "snippet";
    public String value;

    private static FavoritesConnectionHandler favoritesConnectionHandler;

    /**
     *
     * @param context
     */
    public FavoritesConnectionHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * This class provides an instance to the FavoritesConnectionHandler class.
     * @param context
     * @return favoritesConnectionHandler
     */

    public static FavoritesConnectionHandler getDBHandler(Context context) {
        if (favoritesConnectionHandler == null) {
            favoritesConnectionHandler = new FavoritesConnectionHandler(context);
            favoritesConnectionHandler.onCreate(favoritesConnectionHandler.getWritableDatabase());
        }
        return favoritesConnectionHandler;
    }

    /**
     * Called when application is first loaded.
     *
     * A table called "favorites' is created with two columns. An id column which is the primary key
     * and a snippet column which holds the information for a specific marker/location.
     * @param db - database that will be used to hold favorite locations.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS favorites " + "(id integer primary key, snippet text)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS favorites");
        onCreate(db);
    }
    public boolean insertFavorite(String snippet){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("snippet", snippet);
        db.insert("favorites", null, contentValues);
        return true;
    }

    public String getData(int id) {
        String str = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select snippet from favorites where id=" + id + "", null);
        if (res.moveToFirst()) {
            str = res.getString(res.getColumnIndex("snippet"));
        }
        return str;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FAVORITES_TABLE_NAME);
        return numRows;
    }

    public Integer deleteFavorite(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public List getAllFavorites() {
        List<String> arrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from favorites", null );
        res.moveToFirst();
        while(!res.isAfterLast()) {
            arrayList.add(res.getString(res.getColumnIndex(FAVORITES_COLUMN_SNIPPET)));
        }
        return arrayList;
    }
}
