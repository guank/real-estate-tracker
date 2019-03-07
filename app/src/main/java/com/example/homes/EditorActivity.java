package com.example.homes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.homes.data.HomeContract.HomeEntry;

/**
 * Allows user to create a new home or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mAddressEditText;
    private EditText mCountyEditText;
    private EditText mIncomeEditText;
    private Spinner mTypeSpinner;

    /**
     * Type of the home. The possible values are:
     * 0 for unknown type, 1 for single, 2 for multi.
     */
    private int mType = HomeEntry.TYPE_UNKNOWN;

    /** Identifier for the home data loader */
    private static final int EXISTING_HOME_LOADER = 0;

    /** Content URI for the existing home (null if it's a new home) */
    private Uri mCurrentHomeUri;

    /** Boolean flag that keeps track of whether the home has been edited (true) or not (false) */
    private boolean mHomeHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mHomeHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHomeHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new home or editing an existing one.
        Intent intent = getIntent();
        mCurrentHomeUri = intent.getData();

        // If the intent DOES NOT contain a home content URI, then we know that we are
        // creating a new home.
        if (mCurrentHomeUri == null) {
            // This is a new home, so change the app bar to say "Add a Home"
            setTitle(getString(R.string.editor_activity_title_new_home));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a home that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing home, so change app bar to say "Edit Home"
            setTitle(getString(R.string.editor_activity_title_edit_home));

            // Initialize a loader to read the home data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_HOME_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mAddressEditText = (EditText) findViewById(R.id.edit_home_address);
        mCountyEditText = (EditText) findViewById(R.id.edit_home_county);
        mIncomeEditText = (EditText) findViewById(R.id.edit_home_income);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mAddressEditText.setOnTouchListener(mTouchListener);
        mCountyEditText.setOnTouchListener(mTouchListener);
        mIncomeEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }



    /**
     * Setup the dropdown spinner that allows the user to select the type of the home.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_single))) {
                        mType = HomeEntry.TYPE_SINGLE;
                    } else if (selection.equals(getString(R.string.type_multi))) {
                        mType = HomeEntry.TYPE_MULTI;
                    } else {
                        mType = HomeEntry.TYPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = 0; // Unknown
            }
        });
    }

    // Get user input from editor and save into database.
    private void saveHome(){
        // Read from EditText fields
        String addressString = mAddressEditText.getText().toString().trim();
        String countyString = mCountyEditText.getText().toString().trim();
        String incomeString = mIncomeEditText.getText().toString().trim();

        // Check if this is supposed to be a new home
        // and check if all the fields in the editor are blank
        if (mCurrentHomeUri == null &&
                TextUtils.isEmpty(addressString) && TextUtils.isEmpty(countyString) &&
                TextUtils.isEmpty(incomeString) && mType == HomeEntry.TYPE_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new home.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and Toto's home attributes are the values.
        ContentValues values = new ContentValues();
        values.put(HomeEntry.COLUMN_HOME_ADDRESS, addressString);
        values.put(HomeEntry.COLUMN_HOME_COUNTY, countyString);
        values.put(HomeEntry.COLUMN_HOME_TYPE, mType);
        // If the income is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int income = 0;
        if (!TextUtils.isEmpty(incomeString)) {
            income = Integer.parseInt(incomeString);
        }
        values.put(HomeEntry.COLUMN_HOME_INCOME, income);

        // Determine if this is a new or existing home by checking if mCurrentHomeUri is null or not
        if (mCurrentHomeUri == null) {
            // This is a NEW home, so insert a new home into the provider,
            // returning the content URI for the new home.
            Uri newUri = getContentResolver().insert(HomeEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_home_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_home_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING home, so update the home with content URI: mCurrentHomeUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentHomeUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentHomeUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_home_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_home_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save info to database
                saveHome();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the home hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mHomeHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new home, hide the "Delete" menu item.
        if (mCurrentHomeUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all home attributes, define a projection that contains
        // all columns from the home table
        String[] projection = {
                HomeEntry._ID,
                HomeEntry.COLUMN_HOME_ADDRESS,
                HomeEntry.COLUMN_HOME_COUNTY,
                HomeEntry.COLUMN_HOME_TYPE,
                HomeEntry.COLUMN_HOME_INCOME };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentHomeUri,         // Query the content URI for the current home
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null); // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of home attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_ADDRESS);
            int countyColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_COUNTY);
            int typeColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_TYPE);
            int incomeColumnIndex = cursor.getColumnIndex(HomeEntry.COLUMN_HOME_INCOME);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String county = cursor.getString(countyColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            int income = cursor.getInt(incomeColumnIndex);

            // Update the views on the screen with the values from the database
            mAddressEditText.setText(name);
            mCountyEditText.setText(county);
            mIncomeEditText.setText(Integer.toString(income));

            // Type is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Single, 2 is Multi).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (type) {
                case HomeEntry.TYPE_SINGLE:
                    mTypeSpinner.setSelection(1);
                    break;
                case HomeEntry.TYPE_MULTI:
                    mTypeSpinner.setSelection(2);
                    break;
                default:
                    mTypeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mAddressEditText.setText("");
        mCountyEditText.setText("");
        mIncomeEditText.setText("");
        mTypeSpinner.setSelection(0); // Select "Unknown" type
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the home.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the home hasn't changed, continue with handling back button press
        if (!mHomeHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the home.
                deleteHome();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the home.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the home in the database.
     */
    private void deleteHome() {
        // Only perform the delete if this is an existing home.
        if (mCurrentHomeUri != null) {
            // Call the ContentResolver to delete the home at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentHomeUri
            // content URI already identifies the home that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentHomeUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_home_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_home_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

}