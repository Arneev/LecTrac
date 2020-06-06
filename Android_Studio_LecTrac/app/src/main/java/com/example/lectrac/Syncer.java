package com.example.lectrac;

import android.content.Context;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lectrac.HelperFunctions.*;

public class Syncer {

    public static void FullSync(Context context) throws JSONException {
        LocalDatabaseManager localDB = new LocalDatabaseManager(context);
        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

        if (!syncValidity(localDB)){
            Log("syncValidity Failed");
            ShowUserError("There was a problem syncing, try again, if problem persists contact support");
            return;
        }

        //Have to Sync these first for primary key and foreign key relationship
        SyncCourses(localDB,onlineDB);
        SyncLecturer(localDB,onlineDB);

        //Doesn't matter which order
        SyncTasks(localDB,onlineDB);
        SyncMessages(localDB,onlineDB);
        SyncTests(localDB,onlineDB);
    }

    public static void Sync(Context context) throws JSONException {
        LocalDatabaseManager localDB = new LocalDatabaseManager(context);
        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

        if (!syncValidity(localDB)){
            Log("syncValidity Failed");
            ShowUserError("There was a problem syncing, try again, if problem persists contact support");
            return;
        }

        //Doesn't matter which order
        SyncTasks(localDB,onlineDB);
        SyncMessages(localDB,onlineDB);
        SyncTests(localDB,onlineDB);
    }

    public static boolean syncValidity(LocalDatabaseManager localDB){
        Cursor cursor = localDB.doQuery("SELECT * FROM USER");

        if (!cursor.moveToFirst()){
            ShowUserError("Please login or register");

            localDB.doQuery("DELETE FROM USER_TASK");
            return false;
        }

        return true;
    }

    public static void SyncMessages(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException {
        JSONArray localArr = LocalToJSON(localDB,tblMessage);
        int localSize = localArr.length();

        JSONArray arr = null;

        //Try to get online Arr
        try {
            arr = onlineDB.getJSONArr("SELECT * FROM MESSAGE");
        }catch (Exception e){
            Log(e.toString());
            Log("SyncMessages problem with query, onlineDB SELECT * FROM MESSAGE");
            ShowUserError("Please try again or contact support");
            EmptyLocalTable(localDB,tblMessage);
            return;
        }

        //Delete all messages from localDB coz onlineDB is empty
        if (arr == null){
            EmptyLocalTable(localDB,tblMessage);
            return;
        }

        int onlineSize = arr.length();
        JSONObject onlineObj = null;

        //Making sure online DB sync with local DB
        for (int i = 0; i < onlineSize; i++){
            onlineObj = arr.getJSONObject(i);

            if (!isInLocalString(onlineObj,localArr,"Message_ID")){
                AddToLocalString(onlineDB,localDB);
            }
        }

        //Delete all unknowns from local DB
        localArr = LocalToJSON(localDB,tblMessage);     //Updating database JSON
        localSize = localArr.length();      //Updating length

        for (int i = 0; i < localSize; i++){
            onlineObj = arr.getJSONObject(i);

            if (!inOnlineString(onlineObj)){
                DeleteFromLocalString(onlineObj);
            }
        }

        Log("Sync Messaging finished");
    }








    public static void SyncTasks(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){


    }

    public static void SyncTests(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){

    }

    public static void SyncLecturer(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){

    }

    public static void SyncCourses(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){

    }


    public static void EmptyLocalTable(LocalDatabaseManager localDB, String tableName){
        localDB.doQuery("DELETE FROM " + tableName);
    }

    public static JSONArray LocalToJSON(LocalDatabaseManager localDB, String tableName) {
        //Function Mostly From StackOverFlow
        String searchQuery = ("SELECT  * FROM " + tableName);
        Cursor cursor = localDB.doQuery(searchQuery);

        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for( int i = 0; i < totalColumn; i++)
            {
                if(cursor.getColumnName(i) != null)
                {
                    try
                    {
                        if(cursor.getString(i) != null)
                        {
                            Log(cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i));
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                        Log(e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        Log(resultSet.toString() );
        return resultSet;
    }

    public static boolean isInLocalString(JSONObject onlineObj, JSONArray localArr,String attribute){

    }

}
