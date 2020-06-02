package com.example.lectrac;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

final public class LocalDatabaseManager extends SQLiteOpenHelper {

    final static String DATABASE_NAME = "LecTrac.db";
    final static int DATABASE_VERSION = 1;

    static SQLiteDatabase db;

    //Constructor
    public LocalDatabaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
