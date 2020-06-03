package com.example.lectrac;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;

import androidx.annotation.Nullable;

final public class LocalDatabaseManager extends SQLiteOpenHelper {

    final static String DATABASE_NAME = "LecTrac.db";
    final static int DATABASE_VERSION = 1;
    private SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static SQLiteDatabase db;

    //Constructor
    public LocalDatabaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String tblUSER = "";
        final String tblLEC = "";
        final String tblTASK = "";
        final String tblCOURSE = "";

        db.execSQL(tblUSER);
        db.execSQL(tblLEC);
        db.execSQL(tblTASK);
        db.execSQL(tblCOURSE);
    }

    public String getCurrDate(){
        return sdf.format(new java.util.Date());
    }

    public SQLiteDatabase getWriteDB(){
        return getWritableDatabase();
    }

    public SQLiteDatabase getReadDB(){
        return getReadableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(db);
    }

    public static void Log(String error){
        Log.i("Perso",error);
    }


    //End of Helper and Starting Functions


    public Cursor doQuery(String query, String[] params) {
        try {
            Cursor mCur = getReadableDatabase().rawQuery(query, params);
            return mCur;
        } catch (SQLException mSQLException) {
            Log("doQuery problem : " + query);
            mSQLException.printStackTrace(System.err);
            return null;
        }
    }

    public void doUpdate(String query, String[] params) {
        try {
            getWritableDatabase().execSQL(query, params);
        } catch (SQLException mSQLException) {
            Log("doUpdate problem : " + query);
            mSQLException.printStackTrace(System.err);
        }
    }

    public Cursor doQuery(String query) {
        try {
            Cursor mCur = getReadableDatabase().rawQuery(query,null);
            return mCur;
        } catch (SQLException mSQLException) {
            Log("doQuery no params : " + query);
            mSQLException.printStackTrace();
            return null;
        }
    }

    public void doUpdate(String query) {
        try {
            this.getWritableDatabase().execSQL(query);
        } catch (SQLException mSQLException) {
            Log("doUpdate no parasm : " + query);
            mSQLException.printStackTrace(System.err);
        }
    }

    public long getDBFileLength()
    {
        //Open the database object in "read" mode.
        final SQLiteDatabase db = getReadableDatabase();

        // Get length of database file
        final String dbPath       = db.getPath();
        final File dbFile       = new File(dbPath);
        final long   dbFileLength = dbFile.length();

        return (dbFileLength);
    }


}
