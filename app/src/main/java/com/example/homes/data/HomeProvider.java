package com.example.homes.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.homes.data.HomeContract.HomeEntry;

/**
 * {@link ContentProvider} for Homes app.
 */
public class HomeProvider extends ContentProvider {

    /** URI matcher code for the content URI for the homes table */
    private static final int HOMES = 100;

    /** URI matcher code for the content URI for a single home in the homes table */
    private static final int HOME_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(HomeContract.CONTENT_AUTHORITY, HomeContract.PATH_HOMES, HOMES);
        sUriMatcher.addURI(HomeContract.CONTENT_AUTHORITY, HomeContract.PATH_HOMES + "/#", HOME_ID);
    }



    /** Tag for the log messages */
    public static final String LOG_TAG = HomeProvider.class.getSimpleName();

    /** Database helper object */
    private HomeDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new HomeDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case HOMES:
                // For the HOMES code, query the homes table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the homes table.
                cursor = database.query(HomeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case HOME_ID:
                // For the HOME_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.homes/homes/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = HomeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the homes table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(HomeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOMES:
                return insertHome(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a home into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertHome(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(HomeEntry.COLUMN_HOME_ADDRESS);
        if (name == null) {
            throw new IllegalArgumentException("Home requires an address");
        }

        // Check if type is valid
        Integer type = values.getAsInteger(HomeEntry.COLUMN_HOME_TYPE);
        if (type == null || !HomeEntry.isValidType(type)) {
            throw new IllegalArgumentException("Home requires valid type");
        }

        // If the income is provided, check that it's greater than or equal to 0 /mo
        Integer income = values.getAsInteger(HomeEntry.COLUMN_HOME_INCOME);
        if (income != null && income < 0) {
            throw new IllegalArgumentException("Home requires valid income");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new home with the given values
        long id = database.insert(HomeEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the home content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOMES:
                return updateHome(uri, contentValues, selection, selectionArgs);
            case HOME_ID:
                // For the HOME_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = HomeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateHome(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update homes in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more homes).
     * Return the number of rows that were successfully updated.
     */
    private int updateHome(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check if key exists to update
        if (values.containsKey(HomeEntry.COLUMN_HOME_ADDRESS)) {
            String name = values.getAsString(HomeEntry.COLUMN_HOME_ADDRESS);
            if (name == null) {
                throw new IllegalArgumentException("Home requires a name");
            }
        }

        if (values.containsKey(HomeEntry.COLUMN_HOME_TYPE)) {
            Integer type = values.getAsInteger(HomeEntry.COLUMN_HOME_TYPE);
            if (type == null || !HomeEntry.isValidType(type)) {
                throw new IllegalArgumentException("Home requires valid type");
            }
        }

        if (values.containsKey(HomeEntry.COLUMN_HOME_INCOME)) {
            // Check that the income is greater than or equal to 0 /mo
            Integer income = values.getAsInteger(HomeEntry.COLUMN_HOME_INCOME);
            if (income != null && income < 0) {
                throw new IllegalArgumentException("Home requires valid income");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(HomeEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOMES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(HomeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case HOME_ID:
                // Delete a single row given by the ID in the URI
                selection = HomeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(HomeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOMES:
                return HomeEntry.CONTENT_LIST_TYPE;
            case HOME_ID:
                return HomeEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}