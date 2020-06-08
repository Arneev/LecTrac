package com.example.lectrac;

import android.content.Context;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
//import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class Syncer {

    public static void Sync(Context context) throws JSONException, IOException, InterruptedException, ParseException {
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

    public static boolean syncValidity(LocalDatabaseManager localDB){
        Cursor cursor = localDB.doQuery("SELECT * FROM USER");
        int iCount = cursor.getCount();
        cursor.moveToFirst();

        if (iCount == 0){
            ShowUserError("Please login or register");

            localDB.doQuery("DELETE FROM USER_TASK");
            return false;
        }

        return true;
    }

    public static void SyncMessages(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException, IOException, InterruptedException, ParseException {
        Log("About to sync messages");
        LocalLog("About to sync messages");

        String localQuery = "SELECT * FROM " + tblMessage;
        Cursor cursor = localDB.doQuery(localQuery);
        boolean isLocalNotEmpty = cursor.moveToFirst();
        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr("SELECT * FROM " + tblMessage);
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }

        //If online database is empty, empty local messages
        if (onlineArr == null){
            EmptyLocalTable(localDB,tblMessage);
            return;
        }

        int onlineSize = onlineArr.length();

        //if local is empty, move all online into local
        if (!isLocalNotEmpty){
            for (int i = 0; i < onlineSize; i ++){
                JSONObject obj = onlineArr.getJSONObject(i);

                LocalInsertMessages(localDB,obj);
            }
        }

        //region Deleting all of those that are not in the online db
        int localSize = cursor.getCount();
        cursor.moveToFirst();
        int localIDX = cursor.getColumnIndex("Message_ID");

        List<Integer> toDel = new ArrayList<>();

        for (int i = 0; i < localSize; i++){
            int localMessageID = cursor.getInt(localIDX);
            Boolean found = false;

            for (int j = 0; j < onlineSize; j++){
                int onlineMessageID = onlineArr.getJSONObject(j).getInt("Message_ID");

                if (localMessageID == onlineMessageID){
                    found = true;
                    break;
                }
            }

            if (!found){
                toDel.add(localMessageID);
            }

        }

        int toDelSize = toDel.size();

        for (int i = 0; i < toDelSize; i++){
            String messageIDtoDel = Integer.toString(toDel.get(i));

            localDB.doDelete(tblMessage, "MESSAGE_ID", messageIDtoDel);
        }


        //Updating localDB after query
        Log("Updating localDB after query");
        cursor = localDB.doQuery(localQuery);
        localSize = cursor.getCount();
        cursor.moveToFirst();
        //endregion

        //region Adding all of those that need to be added from onlineDB
        List<Integer> toAdd = new ArrayList<>();

        for (int i = 0; i < onlineSize; i++){
            JSONObject obj = onlineArr.getJSONObject(i);
            int onlineMessageID = obj.getInt("Message_ID");

            boolean inLocal = false;

            for (int j = 0; j < localSize; j++){
                if (j == 0){
                    cursor.moveToFirst();
                }
                else{
                    cursor.moveToNext();
                }

                int localMessageID = cursor.getInt(localIDX);

                if (onlineMessageID == localMessageID){
                    inLocal = true;
                }
            }

            if (!inLocal){
                toAdd.add(i);
            }
        }

        int toAddSize = toAdd.size();

        for (int i = 0; i < toAddSize; i++){
            JSONObject obj = onlineArr.getJSONObject(toAdd.get(i));

            LocalInsertMessages(localDB,obj);
        }



        //endregion

    }

    public static void SyncTasks(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException {
        Log("About to sync lec tasks");
        LocalLog("About to sync lec tasks");

        String localQuery = "SELECT * FROM " + tblLocalLecTask;
        Cursor cursor = localDB.doQuery(localQuery);
        boolean isLocalNotEmpty = cursor.moveToFirst();
        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr("SELECT * FROM " + tblTask);
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }

        //If online database is empty, empty local messages
        if (onlineArr == null){
            EmptyLocalTable(localDB,tblLocalLecTask);
            return;
        }

        int onlineSize = onlineArr.length();

        //if local is empty, move all online into local
        if (!isLocalNotEmpty){
            for (int i = 0; i < onlineSize; i ++){
                JSONObject obj = onlineArr.getJSONObject(i);

                LocalInsertTask(localDB,obj);
            }
        }

        //region Deleting all of those that are not in the online db
        int localSize = cursor.getCount();
        cursor.moveToFirst();
        int localIDX = cursor.getColumnIndex("Task_ID");

        List<Integer> toDel = new ArrayList<>();

        for (int i = 0; i < localSize; i++){
            int localTaskID = cursor.getInt(localIDX);
            Boolean found = false;

            for (int j = 0; j < onlineSize; j++){
                int onlineTaskID = onlineArr.getJSONObject(j).getInt("Message_ID");

                if (localTaskID == onlineTaskID){
                    found = true;
                    break;
                }
            }

            if (!found){
                toDel.add(localTaskID);
            }

        }

        int toDelSize = toDel.size();

        for (int i = 0; i < toDelSize; i++){
            String taskIDtoDel = Integer.toString(toDel.get(i));

            localDB.doDelete(tblLocalLecTask, "TASK_ID", taskIDtoDel);
        }


        //Updating localDB after query
        Log("Updating localDB after query");
        cursor = localDB.doQuery(localQuery);
        localSize = cursor.getCount();
        cursor.moveToFirst();
        //endregion

        //region Adding all of those that need to be added from onlineDB
        List<Integer> toAdd = new ArrayList<>();

        for (int i = 0; i < onlineSize; i++){
            JSONObject obj = onlineArr.getJSONObject(i);
            int onlineTaskID = obj.getInt("Message_ID");

            boolean inLocal = false;

            for (int j = 0; j < localSize; j++){
                if (j == 0){
                    cursor.moveToFirst();
                }
                else{
                    cursor.moveToNext();
                }

                int localTaskID = cursor.getInt(localIDX);

                if (onlineTaskID == localTaskID){
                    inLocal = true;
                }
            }

            if (!inLocal){
                toAdd.add(i);
            }
        }

        int toAddSize = toAdd.size();

        for (int i = 0; i < toAddSize; i++){
            JSONObject obj = onlineArr.getJSONObject(toAdd.get(i));

            LocalInsertTask(localDB,obj);
        }



        //endregion
    }

    public static void SyncTests(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){
        String userID = localDB.getUserID(localDB);

        Log("About to sync lec tests");
        LocalLog("About to sync lec tasks");

        String localQuery = "SELECT * FROM " + tblLocalLecTask;
        Cursor cursor = localDB.doQuery(localQuery);
        boolean isLocalNotEmpty = cursor.moveToFirst();
        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr("SELECT * FROM " + tblTask + " WHERE ");
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }

        //If online database is empty, empty local messages
        if (onlineArr == null){
            EmptyLocalTable(localDB,tblLocalLecTask);
            return;
        }

        int onlineSize = onlineArr.length();

        //if local is empty, move all online into local
        if (!isLocalNotEmpty){
            for (int i = 0; i < onlineSize; i ++){
                JSONObject obj = onlineArr.getJSONObject(i);

                LocalInsertTask(localDB,obj);
            }
        }

        //region Deleting all of those that are not in the online db
        int localSize = cursor.getCount();
        cursor.moveToFirst();
        int localIDX = cursor.getColumnIndex("Task_ID");

        List<Integer> toDel = new ArrayList<>();

        for (int i = 0; i < localSize; i++){
            int localTaskID = cursor.getInt(localIDX);
            Boolean found = false;

            for (int j = 0; j < onlineSize; j++){
                int onlineTaskID = onlineArr.getJSONObject(j).getInt("Message_ID");

                if (localTaskID == onlineTaskID){
                    found = true;
                    break;
                }
            }

            if (!found){
                toDel.add(localTaskID);
            }

        }

        int toDelSize = toDel.size();

        for (int i = 0; i < toDelSize; i++){
            String taskIDtoDel = Integer.toString(toDel.get(i));

            localDB.doDelete(tblLocalLecTask, "TASK_ID", taskIDtoDel);
        }


        //Updating localDB after query
        Log("Updating localDB after query");
        cursor = localDB.doQuery(localQuery);
        localSize = cursor.getCount();
        cursor.moveToFirst();
        //endregion

        //region Adding all of those that need to be added from onlineDB
        List<Integer> toAdd = new ArrayList<>();

        for (int i = 0; i < onlineSize; i++){
            JSONObject obj = onlineArr.getJSONObject(i);
            int onlineTaskID = obj.getInt("Message_ID");

            boolean inLocal = false;

            for (int j = 0; j < localSize; j++){
                if (j == 0){
                    cursor.moveToFirst();
                }
                else{
                    cursor.moveToNext();
                }

                int localTaskID = cursor.getInt(localIDX);

                if (onlineTaskID == localTaskID){
                    inLocal = true;
                }
            }

            if (!inLocal){
                toAdd.add(i);
            }
        }

        int toAddSize = toAdd.size();

        for (int i = 0; i < toAddSize; i++){
            JSONObject obj = onlineArr.getJSONObject(toAdd.get(i));

            LocalInsertTask(localDB,obj);
        }



        //endregion
    }

    public static void SyncLecturer(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){

    }

    public static void SyncCourses(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB){

    }


    //region HelperFunctions
    public static boolean inIntArr(int n, int[] arr){
        boolean inArr = false;
        int size = arr.length;

        for (int i = 0; i < size; i++){
            if (n == arr[i]){
                inArr = true;
            }
        }

        return inArr;
    }

    public static void LocalInsertMessages(LocalDatabaseManager localDB, JSONObject obj) throws JSONException {

        String isDeleted = "0";
        String[] values = new String[8];

        int messageID = obj.getInt("Message_ID");
        String messageName = obj.getString("Message_Name");
        String messageClass = obj.getString("Message_Classification");
        String messageContent = obj.getString("Message_Contents");
        String messageDate = obj.getString("Message_Date_Posted");
        String courseCode = obj.getString("Course_Code");
        String lecturerID = obj.getString("Lecturer_ID");

        values[0] = Integer.toString(messageID);
        values[1] = doubleQuote(messageName);
        values[2] = doubleQuote(messageClass);
        values[3] = doubleQuote(messageContent);
        values[4] = doubleQuote(messageDate);
        values[5] = isDeleted;
        values[6] = doubleQuote(courseCode);
        values[7] = doubleQuote(lecturerID);

        try{
            localDB.doInsert(tblMessage,values);
        }catch (Exception e){
            ShowUserError("Problem syncing, please contact support");
        }

    }

    public static void LocalInsertTask(LocalDatabaseManager localDB, JSONObject obj) throws JSONException {

        String isDone = "0";
        String[] values = new String[7];

        int taskID = obj.getInt("Task_ID");
        String taskName = obj.getString("Task_Name");
        String taskDate = obj.getString("Task_Due_Date");
        String taskTime = obj.getString("Task_Due_Time");
        String courseCode = obj.getString("Course_Code");
        String lecturerID = obj.getString("Lecturer_ID");

        values[0] = Integer.toString(taskID);
        values[1] = doubleQuote(taskName);
        values[2] = doubleQuote(taskDate);
        values[3] = doubleQuote(taskTime);
        values[4] = isDone;
        values[5] = doubleQuote(courseCode);
        values[6] = doubleQuote(lecturerID);

        try{
            localDB.doInsert(tblLocalLecTask,values);
        }catch (Exception e){
            ShowUserError("Problem syncing, please contact support");
        }

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


    //endregion
}
