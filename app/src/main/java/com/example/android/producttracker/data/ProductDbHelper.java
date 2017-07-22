package com.example.android.producttracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.producttracker.data.ProductContract.ProductEntry;

/**
 * Created by MátéZoltán on 2017. 07. 17..
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static String DB_NAME = "productTracker.db";

    public static int DB_VERSION = 1;

    private static String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public ProductDbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
            ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_DESCRIPTION + " TEXT, " +
                ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
                ProductEntry.COLUMN_IMAGE + " BLOB);";

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
