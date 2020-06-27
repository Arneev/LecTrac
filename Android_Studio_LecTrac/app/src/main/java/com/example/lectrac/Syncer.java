package com.example.lectrac;

import android.content.Context;
import android.database.Cursor;

import androidx.constraintlayout.solver.widgets.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class Syncer {

    static boolean isLec;
    static Context context;
    static boolean isManual;


    Syncer(Context context, boolean isManual) throws InterruptedException, ParseException, JSONException, IOException {
        if (isManual){
            ManualSync(context);
        }
        else{
            Sync(context);
        }
    }

    Syncer(Context context) throws InterruptedException, ParseException, JSONException, IOException {
        Sync(context);
    }

    public void ManualSync(Context context) throws InterruptedException, ParseException, JSONException, IOException {
        Sync(context,true);
    }

    public void Sync(Context context) throws InterruptedException, ParseException, JSONException, IOException {
        Sync(context,false);
    }

    public void Sync(Context ct, boolean isManual) throws JSONException, IOException, InterruptedException, ParseException {
        context = ct;
        boolean isOnline = isOnline(context);

        this.isManual = isManual;
        if (isManual){
            if (!isOnline){
                Log("The user is offline");
                ShowUserError("You are not connected to the internet, if problem persists then contact support with " + errorProblemSync);
                return;
            }
        }
        else {
            if (!isOnline){
                Log("The user is offline");
                Log("This is auto sync");
                return;
            }
        }



        LocalDatabaseManager localDB = new LocalDatabaseManager(context);
        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();


        if (!syncValidity(localDB)){
            Log("syncValidity Failed");
            ShowUserError("There was a problem syncing, try again, if problem persists contact support");
            return;
        }

        isLec = localDB.isLec();

        if (isManual){
            //Have to Sync these first for primary key and foreign key relationship
            SyncCourses(localDB,onlineDB);
            SyncLecturer(localDB,onlineDB);

            //Doesn't matter which order
            SyncLecReg(localDB,onlineDB);

        }

        SyncTasks(localDB,onlineDB);
        SyncMessages(localDB,onlineDB);
        SyncTests(localDB,onlineDB);



    }


    //region Mini Sync Functions
    public static boolean syncValidity(LocalDatabaseManager localDB){
        Cursor cursor = localDB.doQuery("SELECT * FROM USER");
        int iCount = cursor.getCount();
        cursor.moveToFirst();

        if (iCount == 0){
            ShowUserError("Please login or register");
            Log("Sync validity failed");
            return false;
        }

        Log("Sync validity passed :D");
        return true;
    }

    public static void SyncLecReg(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException {
        Log("About to sync lecRegistered ");
        LocalLog("About to sync lecRegistered");

        String[] courses = localDB.getCourses(localDB);

        String onlineQuery = "SELECT * FROM REGISTERED WHERE (";
        int courseSize = courses.length;

        for (int i = 0; i < courseSize; i++){
            onlineQuery += "Course_Code = " + quote(courses[i]);

            if (i + 1 < courseSize){
                onlineQuery += " OR ";
            }
        }

        onlineQuery += ")";

        Log("SyncReg query is, " + onlineQuery);
        LocalLog("SyncReg query is, " + onlineQuery);


        String localQuery = "SELECT * FROM " + tblRegistered;
        Cursor cursor = localDB.doQuery(localQuery);

        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr(onlineQuery);
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }


        //Empty test table
        EmptyLocalTable(localDB,tblRegistered);

        //If online database is empty, return
        if (onlineArr == null){
            return;
        }

        int onlineSize = onlineArr.length();

        //Move all into test table
        for (int i = 0; i < onlineSize; i ++){
            JSONObject obj = onlineArr.getJSONObject(i);

            LocalInsertLecReg(localDB,obj);
        }
    }

    public static void SyncMessages(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException, IOException, InterruptedException, ParseException {
        Log("About to sync messages");
        LocalLog("About to sync messages");

        String localQuery = "SELECT * FROM " + tblMessage;
        Cursor cursor = localDB.doQuery(localQuery);

        String[] courses = localDB.getCourses(localDB);
        String onlineQuery = "SELECT * FROM MESSAGE WHERE ";
        int courseSize = courses.length;

        for (int i = 0; i < courseSize; i++){
            onlineQuery += "Course_Code = " + quote(courses[i]);

            if (i + 1 < courseSize){
                onlineQuery += " OR ";
            }
        }

        int localSize = cursor.getCount();
        boolean isLocalNotEmpty;

        if (localSize == 0){
            isLocalNotEmpty = false;
        }
        else{
            isLocalNotEmpty = true;
        }

        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr(onlineQuery);
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
        localSize = cursor.getCount();
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

            localDB.doDelete(tblMessage, "Message_ID", messageIDtoDel);
        }


        //Updating localDB after query
        Log("Updating localDB after query");
        cursor = localDB.doQuery(localQuery);
        localSize = cursor.getCount();
        cursor.moveToFirst();
        //endregion

        //region Adding all of those that need to be added from onlineDB
        List<Integer> toAdd = new ArrayList<>();

        Log("Online size is for msgs is " + onlineSize);
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
        int localSize = cursor.getCount();
        boolean isLocalNotEmpty;

        if (localSize == 0){
            isLocalNotEmpty = false;
        }
        else{
            isLocalNotEmpty = true;
        }


        String onlineQuery = "SELECT * FROM TASK WHERE ";
        String[] courses = localDB.getCourses(localDB);
        int courseSize = courses.length;

        for (int i = 0; i < courseSize; i++){
            onlineQuery += "Course_Code = " + quote(courses[i]);

            if (i + 1 < courseSize){
                onlineQuery += " OR ";
            }
        }

        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr(onlineQuery);
        }catch (Exception e){
            ShowUserError("Failed to sync tasks from cloud, please contact support or try again");
            return;
        }

        //If online database is empty, empty local task
        if (onlineArr == null){
            EmptyLocalTable(localDB, tblLocalLecTask);
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
        localSize = cursor.getCount();
        cursor.moveToFirst();
        int localIDX = cursor.getColumnIndex("Task_ID");

        List<Integer> toDel = new ArrayList<>();

        for (int i = 0; i < localSize; i++){
            int localTaskID = cursor.getInt(localIDX);
            Boolean found = false;

            for (int j = 0; j < onlineSize; j++){
                int onlineTaskID = onlineArr.getJSONObject(j).getInt("Task_ID");

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

            localDB.doDelete(tblLocalLecTask, "Task_ID", taskIDtoDel);
        }


        //Updating localDB after query
        Log("Updating localDB after query");
        cursor = localDB.doQuery(localQuery);
        localSize = cursor.getCount();
        cursor.moveToFirst();
        //endregion

        //region Adding all of those that need to be added from onlineDB
        List<Integer> toAdd = new ArrayList<>();
        Log("The online size is " + onlineSize);
        for (int i = 0; i < onlineSize; i++){
            JSONObject obj = onlineArr.getJSONObject(i);
            int onlineTaskID = obj.getInt("Task_ID");

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

    public static void SyncTests(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException {
        Log("About to sync test ");
        LocalLog("About to sync lec tests");

        String userID = localDB.getUserID(localDB);
        String[] courses = localDB.getCourses(localDB);

        String onlineQuery = "SELECT * FROM TEST,WROTE WHERE (";
        int courseSize = courses.length;

        for (int i = 0; i < courseSize; i++){
            onlineQuery += "TEST.Course_Code = " + quote(courses[i]);

            if (i + 1 < courseSize){
                onlineQuery += " OR ";
            }
        }

        onlineQuery += ") AND WROTE.Test_No = TEST.Test_No AND WROTE.Student_ID = " + quote(userID);

        Log("SyncTest query is, " + onlineQuery);
        LocalLog("SyncTest query is, " + onlineQuery);

        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr(onlineQuery);
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }


        //Empty test table
        EmptyLocalTable(localDB,tblTest);

        //If online database is empty, return
        if (onlineArr == null){
            return;
        }

        int onlineSize = onlineArr.length();

        //Move all into test table
        for (int i = 0; i < onlineSize; i ++){
            JSONObject obj = onlineArr.getJSONObject(i);

            LocalInsertTest(localDB,obj);
        }

    }

    public static void SyncLecturer(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException {
        Log("About to sync lecturer ");
        LocalLog("About to sync lecturer");

        String userID = localDB.getUserID(localDB);
        String[] courses = localDB.getCourses(localDB);
        String onlineQuery = "SELECT * FROM LECTURER,REGISTERED WHERE (";
        int courseSize = courses.length;

        for (int i = 0; i < courseSize; i++){
            onlineQuery += "REGISTERED.Course_Code = " + quote(courses[i]);

            if (i + 1 < courseSize){
                onlineQuery += " OR ";
            }
        }

        onlineQuery += ") AND LECTURER.Lecturer_ID = REGISTERED.Lecturer_ID";

        Log("SyncLec query is, " + onlineQuery);
        LocalLog("SyncLec query is, " + onlineQuery);


        String localQuery = "SELECT * FROM " + tblLecturer;
        Cursor cursor = localDB.doQuery(localQuery);

        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr(onlineQuery);
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }


        //Empty test table
        EmptyLocalTable(localDB,tblLecturer);

        //If online database is empty, return
        if (onlineArr == null){
            return;
        }

        int onlineSize = onlineArr.length();

        //Move all into test table
        for (int i = 0; i < onlineSize; i ++){
            JSONObject obj = onlineArr.getJSONObject(i);

            LocalInsertLecturer(localDB,obj);
        }
    }

    public static void SyncCourses(LocalDatabaseManager localDB, OnlineDatabaseManager onlineDB) throws JSONException {
        Log("About to sync course ");
        LocalLog("About to sync course");

        String userID = localDB.getUserID(localDB);

        String onlineQuery = "SELECT * FROM COURSE,ENROLLED WHERE Student_ID = " + quote(userID) + " AND COURSE.Course_Code = ENROLLED.Course_Code UNION " +
                "SELECT * FROM COURSE,REGISTERED WHERE Lecturer_ID = " + quote(userID) + " AND COURSE.Course_Code = REGISTERED.Course_Code";

        Log("SyncLec query is, " + onlineQuery);
        LocalLog("SyncLec query is, " + onlineQuery);


        String localQuery = "SELECT * FROM " + tblCourse;
        Cursor cursor = localDB.doQuery(localQuery);

        JSONArray onlineArr = null;

        try {
            onlineArr = onlineDB.getJSONArr(onlineQuery);
        }catch (Exception e){
            ShowUserError("Failed to sync messages from cloud, please contact support or try again");
            return;
        }


        //Empty test table
        EmptyLocalTable(localDB,tblCourse);

        //If online database is empty, return
        if (onlineArr == null){
            return;
        }

        int onlineSize = onlineArr.length();

        //Move all into test table
        for (int i = 0; i < onlineSize; i ++){
            JSONObject obj = onlineArr.getJSONObject(i);

            LocalInsertCourse(localDB,obj);
        }
    }
    //endregion

    //region HelperFunctions

    public static void LocalInsertLecReg(LocalDatabaseManager localDB, JSONObject obj) throws JSONException {
        String[] values = new String[2];

        String lecID = obj.getString("Lecturer_ID");
        String courseCode = obj.getString("Course_Code");

        values[0] = doubleQuote(lecID);
        values[1] = doubleQuote(courseCode);

        try{
            localDB.doInsert(tblRegistered,values);
        }catch (Exception e){
            Log("Problem sync course");
            ShowUserError("Problem syncing, please contact support");
        }
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
            Log("Problem sync messages");
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
            Log("Problem sync task");
            ShowUserError("Problem syncing, please contact support");
        }

    }

    public static void LocalInsertTest(LocalDatabaseManager localDB, JSONObject obj) throws JSONException {

        String[] values = new String[5];

        int testNo = obj.getInt("Test_No");
        String testName = obj.getString("Test_Name");
        int testMark = obj.getInt("Test_Mark");
        int testTotal = obj.getInt("Test_Total");
        String courseCode = obj.getString("Course_Code");

        values[0] = Integer.toString(testNo);
        values[1] = doubleQuote(testName);
        values[2] = Integer.toString(testMark);
        values[3] = Integer.toString(testTotal);
        values[4] = doubleQuote(courseCode);

        try{
            localDB.doInsert(tblTest,values);
        }catch (Exception e){
            Log("Problem sync test");
            ShowUserError("Problem syncing, please contact support");
        }

    }

    public static void LocalInsertLecturer(LocalDatabaseManager localDB, JSONObject obj) throws JSONException {

        String[] values = new String[5];

        String lecID = obj.getString("Lecturer_ID");
        String lecName = obj.getString("Lecturer_FName");
        String lecSurname = obj.getString("Lecturer_LName");
        String lecEmail = obj.getString("Lecturer_Email");
        String lecRef = obj.getString("Lecturer_Reference");

        values[0] = doubleQuote(lecID);
        values[1] = doubleQuote(lecName);
        values[2] = doubleQuote(lecSurname);
        values[3] = doubleQuote(lecEmail);
        values[4] = doubleQuote(lecRef);

        try{
            localDB.doInsert(tblLecturer,values);
        }catch (Exception e){
            Log("Problem sync lec");
            ShowUserError("Problem syncing, please contact support");
        }

    }

    public static void LocalInsertCourse(LocalDatabaseManager localDB, JSONObject obj) throws JSONException {

        String[] values = new String[2];

        String courseCode = obj.getString("Course_Code");
        String courseName = obj.getString("Course_Name");

        values[0] = doubleQuote(courseCode);
        values[1] = doubleQuote(courseName);

        try{
            localDB.doInsert(tblCourse,values);
        }catch (Exception e){
            Log("Problem sync course");
            ShowUserError("Problem syncing, please contact support");
        }

    }

    public static void EmptyLocalTable(LocalDatabaseManager localDB, String tableName){
        localDB.DoDeleteEntire(tableName);
    }

    public static void ShowUserError(String error){
        if (isManual){
            HelperFunctions.ShowUserError(error,context);
        }
        return;
    }


    //endregion
}
