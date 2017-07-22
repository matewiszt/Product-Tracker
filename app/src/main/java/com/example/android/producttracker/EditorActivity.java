package com.example.android.producttracker;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.producttracker.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.graphics.Bitmap.CompressFormat.JPEG;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Create global variables for the EditTexts
    EditText mNameField;

    EditText mDescField;

    EditText mQuantityField;

    EditText mPriceField;

    ImageView mImageView;

    //Create a global variable for the Uri
    Uri mUri;

    //Create a boolean to indicate if something was edited or not
    boolean mIsEdited = false;

    //Request code for getting an image from the gallery
    private static final int GET_FROM_GALLERY = 200;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mIsEdited = true;
            return false;
        }
    };

    //A variable for the image empty text
    TextView mImageEmpyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mUri = getIntent().getData();

        //Get the EditTexts of the layout
        mNameField = (EditText) findViewById(R.id.editor_name_field);
        mDescField = (EditText) findViewById(R.id.editor_desc_field);
        mQuantityField = (EditText) findViewById(R.id.editor_quantity_field);
        mPriceField = (EditText) findViewById(R.id.editor_price_field);
        Button uploadButton = (Button) findViewById(R.id.upload_button);
        mImageView = (ImageView) findViewById(R.id.editor_image);
        mImageEmpyText = (TextView) findViewById(R.id.editor_empty_image_text);

        //Manage the Add/Edit mode
        if (mUri == null) {

            setTitle(getString(R.string.add_menu_item_title));
            mImageEmpyText.setVisibility(View.VISIBLE);

        } else {
            invalidateOptionsMenu();
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(0, null, this);
        }

        //Set the OnTouchListeners on the fields
        mNameField.setOnTouchListener(mTouchListener);
        mDescField.setOnTouchListener(mTouchListener);
        mQuantityField.setOnTouchListener(mTouchListener);
        mPriceField.setOnTouchListener(mTouchListener);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        GET_FROM_GALLERY);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create a menu from menu_editor.xml menu resource file
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Handle the options' select
        switch (item.getItemId()) {

            //If the user taps on the save menu item, save the new product/the updated product and go to the Catalog
            case R.id.editor_save_menu_item:
                insertProduct();
                startActivity(new Intent(this, CatalogActivity.class));
                return true;

            //If the user taps on the delete menu item, show the confirmation dialog
            case R.id.editor_delete_menu_item:
                showDeleteConfirmationDialog();
                return true;

            case R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mIsEdited) {
                    NavUtils.navigateUpFromSameTask(this);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.editor_delete_menu_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void insertProduct() {

        int price = 0;
        int quantity = 0;
        byte[]  byteImage;

        //Get the content of the EditTexts
        String name = mNameField.getText().toString();
        String desc = mDescField.getText().toString();
        String priceString = mPriceField.getText().toString();
        String quantityString = mQuantityField.getText().toString();
        Bitmap bitmapImage = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();

        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        if (bitmapImage == null){

            Bitmap defaultBitmap = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.kettlebell, null)).getBitmap();
            byteImage = getBitmapAsByteArray(defaultBitmap);

        } else {

            byteImage = getBitmapAsByteArray(bitmapImage);
        }

        //If NOT NULL fields are empty, return early
        if (TextUtils.isEmpty(name)) {
            return;
        }

        //Create a ContentValues object from the EditText content
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_DESCRIPTION, desc);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_IMAGE, byteImage);

        //If we are in Add mode, call the insert method
        if (mUri == null) {
            Uri newProductUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newProductUri == null) {

                //If the insertion failed, notify the user
                Toast.makeText(this, getString(R.string.insert_failure_text), Toast.LENGTH_LONG).show();

            } else {

                //If the insertion succeeded, notify the user
                Toast.makeText(this, getString(R.string.insert_success_text), Toast.LENGTH_LONG).show();
            }
        } else {

            //If we are in Edit mode, call the update method
            int rowsUpdated = getContentResolver().update(mUri, values, null, null);

            if (rowsUpdated == 0) {

                //If the update failed, notify the user
                Toast.makeText(this, getString(R.string.update_failure_text), Toast.LENGTH_LONG).show();

            } else {

                //If the update succeeded, notify the user
                Toast.makeText(this, getString(R.string.update_success_text), Toast.LENGTH_LONG).show();
            }

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

                if (rowsDeleted == 0) {

                    //Notify the user about the failure
                    Toast.makeText(EditorActivity.this, getString(R.string.delete_failure_text), Toast.LENGTH_LONG).show();

                } else {

                    //Notify the user about the success and launch the CatalogActivity
                    Toast.makeText(EditorActivity.this, getString(R.string.delete_success_text), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(EditorActivity.this, CatalogActivity.class));
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

        if (data.moveToFirst()) {

            //Get the column indices from the database
            int nameIndex = data.getColumnIndex(ProductEntry.COLUMN_NAME);
            int descIndex = data.getColumnIndex(ProductEntry.COLUMN_DESCRIPTION);
            int quantityIndex = data.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceIndex = data.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int imageIndex = data.getColumnIndex(ProductEntry.COLUMN_IMAGE);

            //Set the data to the corresponding Views
            mNameField.setText(data.getString(nameIndex));
            mDescField.setText(data.getString(descIndex));
            mQuantityField.setText(String.valueOf(data.getInt(quantityIndex)));
            mPriceField.setText(String.valueOf(data.getInt(priceIndex)));

            //Get the image and if it exists, set to the image Resource, otherwise, set the empty text
            byte[] byteImage = data.getBlob(imageIndex);

            if ( byteImage != null ) {
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                mImageView.setImageBitmap(bitmapImage);
            } else {
                mImageEmpyText.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //Reset the Views
        mNameField.setText("");
        mDescField.setText("");
        mQuantityField.setText("");
        mPriceField.setText("");
        mImageView.setImageBitmap(null);
        mImageEmpyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // If the product was not edited, continue with handling back button press
        if (!mIsEdited) {
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_changes_text));
        builder.setPositiveButton(getString(R.string.discard_changes_yes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.discard_changes_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {

            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                mImageView.setImageBitmap(bitmap);
                mImageEmpyText.setVisibility(View.GONE);

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(JPEG, 0, outputStream);
        return outputStream.toByteArray();
    }
}
