package com.thevery.droidcon14.cursor;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.thevery.droidcon14.Apples;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String TEXT_NOT_NULL = " text not null";
    private static final String DATABASE_NAME = "demo.DB";
    private static final int DATABASE_VERSION = 1;
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " integer primary key autoincrement, ";
    private static final String DATABASE_CREATE_ACCOUNTS = "create table " + ApplesTable.TABLE_NAME + "(" +
            ApplesTable._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT +
            ApplesTable.CULTIVAR + TEXT_NOT_NULL +
            ");";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_ACCOUNTS);
        for (String appleName : Apples.APPLES_NAMES) {
            ContentValues cv = new ContentValues();
            cv.put(ApplesTable.CULTIVAR, appleName);
            db.insert(ApplesTable.TABLE_NAME, null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ApplesTable.TABLE_NAME);
    }

    public static class ApplesTable implements BaseColumns {
        public static final String TABLE_NAME = "apples";
        public static final String CULTIVAR = "cultivar";
    }
}
