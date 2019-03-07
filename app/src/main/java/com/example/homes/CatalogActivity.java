package com.example.homes;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.homes.data.HomeContract.HomeEntry;

/**
 * Displays list of homes that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int HOME_LOADER = 0;

    HomeCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the home data
        ListView homeListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        homeListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of home data in the Cursor.
        // There is no home data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new HomeCursorAdapter(this, null);
        homeListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific field that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // HomeEntry CONTENT_URI.
                // For example, the URI would be "content://com.example.android.homes/homes/2"
                // if the home with ID 2 was clicked on.
                Uri currentHomeUri = ContentUris.withAppendedId(HomeEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentHomeUri);

                // Launch the EditorActivity to display the data for the current home.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(HOME_LOADER, null, this);

    }

    private void insertHome(){
        // Create a ContentValues object where column names are the keys,
        // and Toto's home attributes are the values.
        ContentValues values = new ContentValues();
        values.put(HomeEntry.COLUMN_HOME_ADDRESS, "123 Fake Street");
        values.put(HomeEntry.COLUMN_HOME_COUNTY, "Springfield");
        values.put(HomeEntry.COLUMN_HOME_TYPE, HomeEntry.TYPE_SINGLE);
        values.put(HomeEntry.COLUMN_HOME_INCOME, 1337);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the HomeEntry CONTENT_URI to indicate that we want to insert
        // into the homes database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(HomeEntry.CONTENT_URI, values);

        //Log.v("CatalogActivity", "newRodId: " + newRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertHome();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllHomes();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                HomeEntry._ID,
                HomeEntry.COLUMN_HOME_ADDRESS,
                HomeEntry.COLUMN_HOME_COUNTY,
                HomeEntry.COLUMN_HOME_INCOME};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                HomeEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null); // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update HomeCursorAdapter with this new cursor containing updated home data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all homes in the database.
     */
    private void deleteAllHomes() {
        int rowsDeleted = getContentResolver().delete(HomeEntry.CONTENT_URI, null, null);
        //Log.v("CatalogActivity", rowsDeleted + " rows deleted from home database");
    }

}
