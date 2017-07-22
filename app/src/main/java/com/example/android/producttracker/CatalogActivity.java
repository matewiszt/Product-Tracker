package com.example.android.producttracker;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.producttracker.data.ProductContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Create a variable for the ProductCursorAdapter
    ProductCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //Get the floating action button and create a listener for it
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Launch EditorActivity in Add mode to add a new product when tapping on the button
                Intent addIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(addIntent);
            }
        });

        //Get the list, the adapter and the empty view and set them to the list
        ListView listView = (ListView) findViewById(R.id.product_list);
        mAdapter = new ProductCursorAdapter(this, null);
        LinearLayout emptyListView = (LinearLayout) findViewById(R.id.empty_view);

        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyListView);

        //Create a listener for tapping the list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Launch DetailActivity to show the detail page when tapping on the list item
                Intent detailIntent = new Intent(CatalogActivity.this, DetailActivity.class);
                Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                detailIntent.setData(uri);
                startActivity(detailIntent);
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create a menu from menu_catalog.xml menu resource file
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle the options' select
        switch (item.getItemId()) {

            //If the user taps on the add menu item, open the EditorActivity in Add mode
            case R.id.catalog_add_menu_item:

                Intent addIntent = new Intent(this, EditorActivity.class);
                startActivity(addIntent);
                return true;

            //If the user taps on the delete menu item, show a confirmation dialog
            case R.id.catalog_delete_menu_item:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Create a projection object with the column needed for the list items
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE
        };

        //Return a CursorLoader with the above columns
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);

    }

    private void showDeleteConfirmationDialog() {

        //Create an AlertDialog.Builder and set the message, and click listeners
        //for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_all_menu_item_title) + "?");
        builder.setPositiveButton(getString(R.string.delete_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Delete all products
                int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

                if (rowsDeleted == 0) {

                    //Notify the user about the failure
                    Toast.makeText(CatalogActivity.this, getString(R.string.delete_failure_text), Toast.LENGTH_LONG).show();

                } else {

                    //Notify the user about the success
                    Toast.makeText(CatalogActivity.this, getString(R.string.delete_all_success_text), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //Dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
