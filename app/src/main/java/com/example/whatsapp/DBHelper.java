package com.example.whatsapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by farha on 14-Nov-17.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mywhatsapp.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Friend (Number TEXT PRIMARY KEY, Status TEXT)");

        db.execSQL("CREATE TABLE Message (Friend TEXT PRIMARY KEY," +
                "Sender TEXT, " +
                "Date TEXT, " +
                "Type TEXT, " +
                "Message TEXT)");

        db.execSQL("CREATE TABLE MyMessage (Friend TEXT," +
                "Sender TEXT, " +
                "Date TEXT, " +
                "Type TEXT, " +
                "Message TEXT, " +
                "PRIMARY KEY(Sender, Date))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("drop TABLE IF EXISTS Message");
        db.execSQL("drop TABLE IF EXISTS MyMessage");
        //db.execSQL("DROP TABLE IF EXISTS Image");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
