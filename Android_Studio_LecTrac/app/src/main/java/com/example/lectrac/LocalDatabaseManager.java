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
        //SORT OUT FOREIGN KEYS
        //SORT OUT AUTOINCREMENT
        //SET DEFAULTS

        final String tblUSER = "CREATE TABLE USER(isLoggedIn BOOLEAN NOT NULL," +
                " isLecturer BOOLEAN NOT NULL," +
                " isDarkMode BOOLEAN NOT NULL, Nickname VARCHAR(30));";


        final String tblLEC = "CREATE TABLE LECTURER(Lecturer_ID CHAR(7) PRIMARY KEY," +
                " Lecturer_FName VARCHAR(50) NOT NULL, Lecturer_LName VARCHAR(50) NOT NULL," +
                "Lecturer_Email VARCHAR(128) NOT NULL, Lecturer_Reference VARCHAR(50));";

        final String tblTASK = "CREATE TABLE TASK(Task_ID INT AUTOINCREMENT NOT NULL, isUser BOOLEAN NOT NULL," +
                "Task_Name VARCHAR(50) NOT NULL, Task_Due_Date DATE,Course_Code CHAR(8) CHAR(8), PRIMARY KEY(Task_ID,isUser));";


        final String tblTEST = "CREATE TABLE TEST(Test_No INT AUTOINCREMENT PRIMARY KEY, Test_Name VARCHAR(50) NOT NULL," +
                " Test_Mark INT NOT NULL, Test_Total INT NOT NULL, Course_Code CHAR(8));";

        final String tblMESSAGE = "CREATE TABLE MESSAGE(Message_ID INT AUTOINCREMENT PRIMARY KEY," +
                " Message_Name VARCHAR(50) NOT NULL, Message_Classification VARCHAR(50)," +
                " MESSAGE_CONTENT VARCHAR(512), Message_Due_Date DATE, Message_isDeleted BOOLEAN NOT NULL," +
                " Course_Code CHAR(8), Lecturer_ID CHAR(7) NOT NULL);";

        final String tblCOURSE = "CREATE TABLE COURSE(Course_Code CHAR(8) PRIMARY KEY, Course_Name VARCHAR(50) NOT NULL);";

        db.execSQL(tblUSER);
        db.execSQL(tblLEC);
        db.execSQL(tblTASK);
        db.execSQL(tblCOURSE);
        db.execSQL(tblTEST);
        db.execSQL(tblMESSAGE);
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
