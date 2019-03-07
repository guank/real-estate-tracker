package com.example.homes;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.homes.data.HomeContract.HomeEntry;

/**
 * An adapter for a list or grid view
 * that uses a Cursor of home data as its data source.
 * This adapter knows how to create list items for each
 * row of home data in the Cursor.
 */
public class HomeCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new HomeCursorAdapter.
     */
    public HomeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the home data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current home can be set on the name TextView
     * in the list item layout.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView rentalTextView = (TextView) view.findViewById(R.id.rental);

        // Find the columns of home attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_ADDRESS);
        int countyColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_COUNTY);
        int rentalColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_INCOME);

        // Read the home attributes from the Cursor for the current home
        String homeAddress = cursor.getString(nameColumnIndex);
        String homeCounty = cursor.getString(countyColumnIndex);
        String rentalCounty = cursor.getString(rentalColumnIndex);

        // If the home county is empty string or null, then use some default text
        // that says "Unknown county", so the TextView isn't blank.
        if (TextUtils.isEmpty(homeCounty)) {
            homeCounty = context.getString(R.string.unknown_county);
        }

        String unitString = context.getString(R.string.unit_home_income);

        // Update the TextViews with the attributes for the current home
        nameTextView.setText(homeAddress);
        summaryTextView.setText(homeCounty);
        rentalTextView.setText(rentalCounty + unitString);
    }
}