package com.example.homes.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;


/**
 * API Contract for the Homes app.
 */

public final class HomeContract{

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private HomeContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.homes";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.homes/homes/ is a valid path for
     * looking at home data. content://com.example.android.homes/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_HOMES = "homes";


    /**
     * Inner class that defines constant values for database table.
     */
    public static final class HomeEntry implements BaseColumns {
        /** The content URI to access the home data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HOMES);

        /** Name of database table for homes */
        public final static String TABLE_NAME = "homes";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_HOME_ADDRESS ="address";
        public final static String COLUMN_HOME_COUNTY = "county";
        public final static String COLUMN_HOME_TYPE = "type";
        public final static String COLUMN_HOME_INCOME = "income";

        /**
         * Possible values for the type of the home.
         */
        public static final int TYPE_UNKNOWN = 0;
        public static final int TYPE_SINGLE = 1;
        public static final int TYPE_MULTI = 2;

        /**
         * Returns whether or not the given type is valid
         */
        public static boolean isValidType(int type) {
            if (type == TYPE_UNKNOWN || type == TYPE_SINGLE || type == TYPE_MULTI) {
                return true;
            }
            return false;
        }

        /**
         * The MIME type of the link for a list of homes.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOMES;

        /**
         * The MIME type of the link for a single home.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOMES;


    }
}
