package com.example.lectrac;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lectrac.HelperFunctions.*;

public class LocalDatabaseManager extends SQLiteOpenHelper {

    final static String DATABASE_NAME = "LecTrac.db";
    final static int DATABASE_VERSION = 1;
    private SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static SQLiteDatabase db;

    //Constructor
    public LocalDatabaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //SORT OUT FOREIGN KEYS
        //SORT OUT AUTOINCREMENT
        //SET DEFAULTS

        db = sqLiteDatabase;

        final String tblUSER = "CREATE TABLE USER(User_ID CHAR(7) PRIMARY KEY, isLoggedIn BOOLEAN NOT NULL," +
                " isLecturer BOOLEAN NOT NULL," +
                " isDarkMode BOOLEAN NOT NULL, Nickname VARCHAR(30))";


        final String tblLEC = "CREATE TABLE LECTURER(Lecturer_ID CHAR(7) PRIMARY KEY," +
                " Lecturer_FName VARCHAR(50) NOT NULL, Lecturer_LName VARCHAR(50) NOT NULL," +
                " Lecturer_Email VARCHAR(128) NOT NULL, Lecturer_Reference VARCHAR(50))";

        final String tblLECTURER_TASK = "CREATE TABLE LECTURER_TASK(Task_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " Task_Name VARCHAR(50) NOT NULL, Task_Due_Date DATE, Task_Due_Time TIME, isDone BOOLEAN NOT NULL, Course_Code CHAR(8) NOT NULL," +
                " Lecturer_ID CHAR(7) NOT NULL, FOREIGN KEY(Course_Code) REFERENCES COURSE(Course_Code)," +
                " FOREIGN KEY(Lecturer_ID) REFERENCES LECTURER(Lecturer_ID))";


        final String tblTEST = "CREATE TABLE TEST(Test_No INTEGER PRIMARY KEY AUTOINCREMENT, Test_Name VARCHAR(50) NOT NULL," +
                " Test_Mark INTEGER NOT NULL, Test_Total INTEGER NOT NULL, Course_Code CHAR(8)," +
                " FOREIGN KEY(Course_Code) REFERENCES COURSE(Course_Code))";

        final String tblMESSAGE = "CREATE TABLE MESSAGE(Message_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " Message_Name VARCHAR(50) NOT NULL, Message_Classification VARCHAR(50)," +
                " Message_Content VARCHAR(512), Message_Date_Posted DATE NOT NULL, Message_isDeleted BOOLEAN NOT NULL," +
                " Course_Code CHAR(8), Lecturer_ID CHAR(7) NOT NULL, FOREIGN KEY(Course_Code) REFERENCES COURSE(Course_Code)," +
                " FOREIGN KEY(Lecturer_ID) REFERENCES LECTURER(Lecturer_ID))";

        final String tblCOURSE = "CREATE TABLE COURSE(Course_Code CHAR(8) PRIMARY KEY, Course_Name VARCHAR(50) NOT NULL)";

        final String tblUSER_TASK = "CREATE TABLE USER_TASK(Task_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " Task_Name VARCHAR(50) NOT NULL, Task_Due_Date DATE, Task_Due_Time TIME, isDone BOOLEAN NOT NULL, Course_Code CHAR(8))";

        final String tblREGISTERED = "CREATE TABLE REGISTERED(Lecturer_ID CHAR(7), Course_Code CHAR(8), PRIMARY KEY(Lecturer_ID,Course_Code)," +
                " FOREIGN KEY(Lecturer_ID) REFERENCES LECTURER(Lecturer_ID), FOREIGN KEY (Course_Code) REFERENCES COURSE(Course_Code))";


        Log("Creating DB");

        sqLiteDatabase.execSQL(tblCOURSE);
        sqLiteDatabase.execSQL(tblLEC);
        sqLiteDatabase.execSQL(tblTEST);
        sqLiteDatabase.execSQL(tblLECTURER_TASK);
        sqLiteDatabase.execSQL(tblMESSAGE);
        sqLiteDatabase.execSQL(tblUSER);
        sqLiteDatabase.execSQL(tblUSER_TASK);
        sqLiteDatabase.execSQL(tblREGISTERED);


    }

    boolean isLoggedIn(){
        Cursor cursor = doQuery("SELECT * FROM USER");
        int iCount = cursor.getCount();
        if (iCount == 1){
            Log("isLoggedIn about to return true");
            return true;
        }
        Log("isLoggedIn about to return false");
        return false;
    }

//    public void DeleteEverything(){
//
//    }

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
        //onCreate(db);
    }



    //End of Helper and Starting Functions

    public void doQueryNonSelect(String query){
        getReadableDatabase().execSQL(query);
    }


    public Cursor doQuery(String query) {
        try {
            LocalLog("Querying, " + query);
            Cursor mCur = getReadableDatabase().rawQuery(query,null);
            return mCur;
        } catch (SQLException mSQLException) {
            LocalLog("doQuery no params : " + query);
            return null;
        }
    }

    public void doInsert(String tableName, String[] values){
        String stringVals = "";
        int size = values.length;

        for (int i = 0; i < size; i++){
            stringVals += values[i];

            if (i != size - 1){
                stringVals += ",";
            }
        }

        Log("Local insert with the query, ");
        Log("INSERT INTO " + tableName + " VALUES(" + stringVals + ")");
        doQueryNonSelect("INSERT INTO " + tableName + " VALUES(" + stringVals + ")");
    }

    public void doUpdate(String tableName, String setting, String condition){
        String query = "UPDATE " + tableName + " SET " + setting + " WHERE " + condition;
        doQueryNonSelect(query);
    }

    public void doUpdate(String tableName, String setting){
        String query = "UPDATE " + tableName + " SET " + setting + " WHERE 1 = 1";
        Log(query);
        doQueryNonSelect(query);
    }


    public void doInsert(String tableName,String[] columns, String[] values){
        String stringVals = "";
        String stringCols = "";
        int size = values.length;

        for (int i = 0; i < size; i++){
            stringVals += values[i];
            stringCols += columns[i];

            if (i != size - 1){
                stringVals += ",";
                stringCols += ",";
            }
        }

        Log("query is, " + "INSERT INTO " + tableName + "(" + stringCols + ") VALUES(" + stringVals + ")");

        doQueryNonSelect("INSERT INTO " + tableName + "(" + stringCols + ") VALUES(" + stringVals + ")");
    }

    public void doDelete(String tableName, String condition){
        doQueryNonSelect("DELETE FROM " + tableName  + " WHERE " + condition);
    }

    public void doDelete(String tableName){
        doQueryNonSelect("DELETE FROM " + tableName);
    }

    public void doDelete(String tableName, String colName, String value){

        doQueryNonSelect("DELETE FROM " + tableName  + " WHERE " + colName + " = " + value);
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

    public String getUserID(LocalDatabaseManager localDB){
        Cursor cursor = doQuery("SELECT * FROM " + tblUser);

        if (!cursor.moveToFirst()){
            Log("getUserID but not logged in");
            return "ERROR";
        }

        return cursor.getString(cursor.getColumnIndex("User_ID"));
    }

    public String[] getCourses(LocalDatabaseManager localDB){
        Cursor cursor = doQuery("SELECT * FROM " + tblCourse);
        int size = cursor.getCount();
        String[] values = new String[size];
        int localIDX = cursor.getColumnIndex("Course_Code");

        for (int i = 0; i < size; i++){
            if (i == 0){
                cursor.moveToFirst();
            }
            else{
                cursor.moveToNext();
            }

            values[i] = cursor.getString(localIDX);
        }

        return values;
    }

    public boolean isLec(){
        Cursor cursor = doQuery("SELECT * FROM USER");
        cursor.moveToFirst();
        int localIDX = cursor.getColumnIndex("isLecturer");
        Log("LocalIDX for isLec is " + Integer.toString(localIDX));

        int iLec = cursor.getInt(localIDX);

        if (iLec == 1){
            LocalLog("About to return true for isLec");
            return true;
        }

        LocalLog("About to return false for isLec");
        return false;
    }

    public void DoDeleteEntire(String tableName){
        getWritableDatabase().execSQL("DELETE FROM " + tableName);
    }


    public String getLastID(String tableName) throws InterruptedException, IOException, JSONException {

        String query = "SELECT * FROM " + tableName;

        Cursor cursor =  doQuery(query);
        Log(query);

        cursor.moveToLast();

        int index = cursor.getColumnIndex("Task_ID");
        return cursor.getString(index);
    }

}
