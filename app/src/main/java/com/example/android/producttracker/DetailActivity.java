package com.example.android.producttracker;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.producttracker.data.ProductContract.ProductEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //Global variable for the Uri
    Uri mUri;

    //Create global variables for the layout Views
    TextView mNameTextView;

    TextView mPriceTextView;

    TextView mDescTextView;

    TextView mQuantityTextView;

    ImageView mImageView;

    //Variable for the quantity for the OnClickListeners
    int mQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Get the Uri of the current product
        mUri = getIntent().getData();

        //Get the Views of the layout
        mNameTextView = (TextView) findViewById(R.id.detail_product_name);
        mPriceTextView = (TextView) findViewById(R.id.detail_product_price);
        mDescTextView = (TextView) findViewById(R.id.detail_product_desc);
        mQuantityTextView = (TextView) findViewById(R.id.detail_product_quantity);
        mImageView = (ImageView) findViewById(R.id.detail_product_image);
        ImageView plusButton = (ImageView) findViewById(R.id.detail_plus_button);
        ImageView minusButton = (ImageView) findViewById(R.id.detail_minus_button);
        Button orderButton = (Button) findViewById(R.id.detail_order_button);

        //Initiate the CursorLoader
        getLoaderManager().initLoader(0, null, this);

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Increment the quantity by 1
                changeQuantity(mQuantity + 1);
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Decrement the quantity by 1
                changeQuantity(mQuantity - 1);
            }
        });

        //When clicking the order button, call the createOrder method
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createOrderIntent();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create a menu from menu_detail.xml menu resource file
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Handle the options' select
        switch (item.getItemId()){

            //When tapping on the update menu item, launch the EditorActivity in Edit mode
            case R.id.detail_update_menu_item:

                Intent updateIntent = new Intent(this, EditorActivity.class);
                updateIntent.setData(mUri);
                startActivity(updateIntent);
                return true;

            //When tapping on the order menu item, call the createOrderIntent method
            case R.id.detail_order_menu_item:

                createOrderIntent();
                return true;

            //When tapping on the delete menu item, show the confirmation dialog
            case R.id.detail_delete_menu_item:

                showDeleteConfirmationDialog();
                return true;

            //When tapping on the home menu item, navigate back to CatalogActivity
            case R.id.home:

                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Create a projection object with all the columns
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_DESCRIPTION,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_IMAGE
        };

        //Return a CursorLoader object
        return new CursorLoader(this, mUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.moveToFirst()){

            //Get the column indices from the database
            int nameIndex = data.getColumnIndex(ProductEntry.COLUMN_NAME);
            int descIndex = data.getColumnIndex(ProductEntry.COLUMN_DESCRIPTION);
            int quantityIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceIndex = data.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int imageIndex = data.getColumnIndex(ProductEntry.COLUMN_IMAGE);

            //Get the values when needed
            String name = data.getString(nameIndex);
            String desc = data.getString(descIndex);
            mQuantity = data.getInt(quantityIndex);
            String quantity = String.valueOf(mQuantity) + " " + getString(R.string.pieces);
            String price = getString(R.string.euro_sign) + " " + String.valueOf(data.getInt(priceIndex)) + getString(R.string.price_ending);
            byte[] byteImage = data.getBlob(imageIndex);
            Bitmap image = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);

            //Set the data to the corresponding Views and the title
            mNameTextView.setText(name);
            mDescTextView.setText(desc);
            mQuantityTextView.setText(quantity);
            mPriceTextView.setText(price);
            mImageView.setImageBitmap(image);
            setTitle(name);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //Reset the Views
        mNameTextView.setText("");
        mDescTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
        mImageView.setImageResource(R.drawable.kettlebell);
    }

    private void changeQuantity(int value){

        if ( value < 0 ){
            value = 0;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_QUANTITY, value);

        int rowsUpdated = getContentResolver().update(mUri, values, null, null);

        if (rowsUpdated == 0) {

            //If the update failed, notify the user
            Toast.makeText(this, getString(R.string.update_failure_text), Toast.LENGTH_LONG).show();

        } else {

            //If the update succeeded, notify the user
            Toast.makeText(this, getString(R.string.update_success_text), Toast.LENGTH_LONG).show();
        }


    }

    private void showDeleteConfirmationDialog() {

        //Create an AlertDialog.Builder and set the message, and click listeners
        //for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_menu_item_title) + "?");
        builder.setPositiveButton(getString(R.string.delete_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Delete the current product
                int rowsDeleted = getContentResolver().delete(mUri, null, null);

                if (rowsDeleted == 0){

                    //Notify the user about the failure
                    Toast.makeText(DetailActivity.this, getString(R.string.delete_failure_text), Toast.LENGTH_LONG).show();

                } else {

                    //Notify the user about the success and launch the CatalogActivity
                    Toast.makeText(DetailActivity.this, getString(R.string.delete_success_text), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(DetailActivity.this, CatalogActivity.class));
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

    private void createOrderIntent(){

        //Get the name and the description of the current product and create an order text from it
        String productName = mNameTextView.getText().toString();
        String productDesc = mDescTextView.getText().toString();

        String orderString = getString(R.string.order_greeting) + "\n\n" +
                getString(R.string.order_text) + "\n" +
                getString(R.string.label_product_name) + ": " + productName + "\n" +
                getString(R.string.label_product_description) + ": " + productDesc + "\n" +
                getString(R.string.label_product_quantity) + ": " + getString(R.string.order_quantity_text) + "\n\n" +
                getString(R.string.order_thank_you);

        //Create the orderIntent with the order subject and text and launch it
        Intent orderIntent = new Intent(Intent.ACTION_SENDTO);
        orderIntent.setData(Uri.parse("mailto:"));
        orderIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_title));
        orderIntent.putExtra(Intent.EXTRA_TEXT, orderString);

        if (orderIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(orderIntent);
        }
    }
}
