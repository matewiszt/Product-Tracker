package com.example.android.producttracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by MátéZoltán on 2017. 07. 17..
 */

public class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.producttracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    private ProductContract(){}

    public static final class ProductEntry implements BaseColumns {

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_IMAGE = "image";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

    }
}
