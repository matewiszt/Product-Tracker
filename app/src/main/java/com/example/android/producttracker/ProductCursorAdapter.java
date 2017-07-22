package com.example.android.producttracker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.producttracker.data.ProductContract.ProductEntry;

/**
 * Created by MátéZoltán on 2017. 07. 18..
 */

public class ProductCursorAdapter extends CursorAdapter {

    //Public constructor of the ProductCursorAdapter class
    public ProductCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        //Create a new list item view
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Resources res = context.getResources();

        //Get the elements of the list_item.xml
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        ImageView saleButton = (ImageView) view.findViewById(R.id.button_sale);

        //Get the column indices of the database table
        int idIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
        int quantityIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);

        //Get the values and create the strings to display
        int id = cursor.getInt(idIndex);
        String name = cursor.getString(nameIndex);
        final int quantityInt = cursor.getInt(quantityIndex);
        int priceInt = cursor.getInt(priceIndex);
        final String quantity = res.getString(R.string.on_stock_text) + " " + quantityInt;
        String price = res.getString(R.string.euro_sign) + " " + priceInt + res.getString(R.string.price_ending);

        //Set the values as text
        nameTextView.setText(name);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);

        //Store the context and the uri in variables for the onClickListener
        final Context cont = context;

        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Decrement the quantity by one
               saleProduct(quantityInt, cont, uri);
            }
        });
    }

    private void saleProduct(int value, Context cont, Uri uri){

        Resources res = cont.getResources();

        //Decrement the value by 1
        int newValue = value - 1;

        //Don't let the newValue go below 0
        if ( newValue < 0 ){
            newValue = 0;
        }

        //Update the quantity with the new value
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_QUANTITY, newValue);

        int rowsUpdated = cont.getContentResolver().update(uri, values, null, null);

        if (rowsUpdated == 0) {

            //If the update failed, notify the user
            Toast.makeText(cont, res.getString(R.string.sale_failure_text), Toast.LENGTH_LONG).show();

        } else {

            //If the update succeeded, notify the user
            Toast.makeText(cont, res.getString(R.string.sale_success_text), Toast.LENGTH_LONG).show();
        }


    }
}
