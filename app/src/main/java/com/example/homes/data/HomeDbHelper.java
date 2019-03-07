package com.example.homes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.homes.data.HomeContract.HomeEntry;

public class HomeDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = HomeDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link HomeDbHelper}.
     *
     * @param context of the app
     */
    public HomeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the homes table
        String SQL_CREATE_HOMES_TABLE =  "CREATE TABLE " + HomeEntry.TABLE_NAME + " ("
                + HomeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + HomeEntry.COLUMN_HOME_ADDRESS + " TEXT NOT NULL, "
                + HomeEntry.COLUMN_HOME_COUNTY + " TEXT, "
                + HomeEntry.COLUMN_HOME_TYPE + " INTEGER NOT NULL, "
                + HomeEntry.COLUMN_HOME_INCOME + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_HOMES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}