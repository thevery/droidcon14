package com.thevery.droidcon14.cursor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.thevery.droidcon14.cursor.SQLiteHelper.ApplesTable.CULTIVAR;
import static com.thevery.droidcon14.cursor.SQLiteHelper.ApplesTable.TABLE_NAME;

public class ApplesContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.thevery.droidcon14");
    private static final String TAG = "ApplesContentProvider";

    private SQLiteHelper helper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");
        SQLiteDatabase database = helper.getWritableDatabase();
        int result = database.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = helper.getWritableDatabase();
        long result = database.insert(TABLE_NAME, CULTIVAR, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, String.valueOf(result));
    }

    @Override
    public boolean onCreate() {
        helper = new SQLiteHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}