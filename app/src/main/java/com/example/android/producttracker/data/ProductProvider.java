package com.example.android.producttracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.producttracker.data.ProductContract.ProductEntry;

/**
 * Created by MátéZoltán on 2017. 07. 17..
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    SQLiteOpenHelper mDbHelper;

    //Create Uri matcher and URI-s for accessing the database elements
    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);

    }

    @Override
    public boolean onCreate() {

        //Create an instance of ProductDbHelper
        mDbHelper = new ProductDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match){

            //If it matches the whole table Uri, request for the whole table
            case PRODUCTS:
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            //If it matches one specific Uri, request the data record where the ID is identical with the Uri
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Query is not supported for " + uri);
        }

        //Set notification about the change
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match){

            //If it matches the whole table Uri, return the type of the whole table
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;

            //If it matches one specific Uri, return the type of the record where the ID is identical with the Uri
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        int match = uriMatcher.match(uri);

        switch (match){

            //It can match only the whole table Uri, call the insertProduct method
            case PRODUCTS:
                return insertProduct(uri, values);

            default:
                throw new IllegalArgumentException("Insert is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = uriMatcher.match(uri);

        switch (match){

            //If it matches the whole table Uri, request for the whole table
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);

            //If it matches one specific Uri, request the data record where the ID is identical with the Uri
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        int match = uriMatcher.match(uri);

        switch (match){

            //If it matches the whole table Uri, delete the whole table
            case PRODUCTS:

                rowsDeleted = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0){

                    //Notify the ContentResolver about the change
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            //If it matches one specific Uri, delete the data record where the ID is identical with the Uri
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0){

                    //Notify the ContentResolver about the change
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;

            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values){

        //Get the values one by one and sanitize them
        String name = values.getAsString(ProductEntry.COLUMN_NAME);
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);

        if (name == null){
            throw new IllegalArgumentException("Every product must have a name");
        }

        if (quantity != null && quantity < 0){
            throw new IllegalArgumentException("The product needs a valid quantity");
        }

        if (price != null && price < 0){
            throw new IllegalArgumentException("The product needs a valid price");
        }

        //Get a database instance and insert the values into it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);

        //If it failed, log an error
        if (newRowId == -1){
            Log.e(LOG_TAG, "Product insert failed for " + uri);
        }
        //Notify the ContentResolver about the change
        getContext().getContentResolver().notifyChange(uri, null);
        //If it is successful, return the Uri of the new database element
        return ContentUris.withAppendedId(uri, newRowId);
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        //If there is no update, return early
        if (values.size() == 0){
            return 0;
        }

        //Get the values one by one and sanitize them
        String name = values.getAsString(ProductEntry.COLUMN_NAME);
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);

        if (values.containsKey(ProductEntry.COLUMN_NAME)) {
            if (name == null) {
                throw new IllegalArgumentException("Every product must have a name");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)) {
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("The product needs a valid quantity");
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            if (price != null && price < 0) {
                throw new IllegalArgumentException("The product needs a valid price");
            }
        }

        //Get a database instance and insert the values into it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        //If it is successful, notify the ContentResolver about the change
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;

    }

}
