package com.example.lectrac;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.arch.core.util.Function;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.lectrac.HelperFunctions.*;

final public class OnlineDatabaseManager {

    //region Constants
    final String baseUrl = "https://lamp.ms.wits.ac.za/home/s2180393/";

    final String insert_lecturer_lecturerarr = "insert_lecturer_lecturerarr.php?";
    final String insert_student_studentarr = "insert_student_studentarr.php?";
    final String select_course_enrolled_userid_union_select_registered_userid = "select_course_enrolled_userid_union_select_registered_userid.php?";
    final String select_lecturer_registered_coursearr = "select_lecturer_registered_coursearr.php?";
    final String select_message_coursearr = "select_message_coursearr.php?";
    final String select_registered_coursearr = "select_registered_coursearr.php?";
    final String select_student_studentid = "select_student_studentid.php?";
    final String select_wits_userid = "select_wits_userid.php?";
    final String select_task_coursearr = "select_task_coursearr.php?";
    final String select_test_wrote_coursearr_userid = "select_test_wrote_coursearr_userid.php?";
    final String delete_message_messageid = "delete_message_messageid.php?";
    final String delete_task_taskid = "delete_task_taskid.php?";
    final String insert_message = "insert_message.php";
    final String insert_task = "insert_task.php?";
    final String select_lecturer_userid = "select_lecturer_userid.php?";
    final String update_lecturer_lecturernickname_userid = "update_lecturer_lecturernickname_userid.php?";
    final String update_student_studentnickname_userid = "update_student_studentnickname_userid.php?";
    final String update_task_coursecode_taskid = "update_task_coursecode_taskid.php";
    final String update_task_duedate_taskid = "update_task_duedate_taskid.php?";
    final String update_task_duetime_taskid = "update_task_duetime_taskid.php?";
    final String update_task_taskname_taskid = "update_task_taskname_taskid.php?";
    final String update_lecturer_password_userid = "update_lecturer_password_userid.php?";
    final String update_student_password_userid = "update_student_password_userid.php?";
    final String update_password_userid_newpass_oldpass = "update_password_userid_newpass_oldpass.php?";

    //endregion

    //region Initialization
    static OkHttpClient client = new OkHttpClient();
    static JSONArray arr = null;
    //endregion

    //region Queries
    JSONArray Query(String url, final String param, final String paramvalue) throws InterruptedException {
        arr = null;
        url = baseUrl + url;
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter(param, paramvalue);
        url = httpBuilder.build().toString();
        final String errorUrl = url;

        //Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()){
                        Log("Response is successful");
                        String res = response.body().string();
                        Log(res);
                        arr = new JSONArray(res);

                        if (arr == null){
                            Log("arr is empty");
                        }
                        Log("Response is successful with query");
                    }
                    else{
                        Log("Response has failed with query" + errorUrl + paramvalue);
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        return arr;
    }

    JSONArray Query(String url) throws InterruptedException {
        url = baseUrl + url;
        arr = null;

        final String errorUrl = url;

        //Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()){
                        Log("Response is successful");
                        String res = response.body().string();
                        Log(res);
                        arr = new JSONArray(res);

                        if (arr == null){
                            Log("arr is empty");
                        }
                        Log("Response is successful with query");
                    }
                    else{
                        Log("Response has failed with query");
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        return arr;
    }

    JSONArray Query(String url, String[] param, String[] paramvalue) throws InterruptedException {
        url = baseUrl + url;
        arr = null;
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

        int size = param.length;

        for (int i = 0; i < size; i++){
            httpBuilder.addQueryParameter(param[i], paramvalue[i]);
        }

        url = httpBuilder.build().toString();
        final String errorUrl = url;

        //Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()){
                        Log("Response is successful");
                        String res = response.body().string();
                        Log(res);
                        arr = new JSONArray(res);

                        if (arr == null){
                            Log("arr is empty");
                        }
                        Log("Response is successful with query");
                    }
                    else{
                        Log("Response has failed with query" + errorUrl);
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        return arr;
    }

    JSONArray Query(String url, String arrayName, String[] arrayVals) throws InterruptedException {
        url = baseUrl + url;
        arr = null;

        String toAdd = "";

        int size = arrayVals.length;

        for (int i = 0; i < size; i++){
            toAdd += arrayName + "[]" + "="+ arrayVals[i];
            if (i != size - 1){
                toAdd += "&";
            }
        }

        url += toAdd;

        final String errorUrl = url;

        //Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()){
                        Log("Response is successful");
                        String res = response.body().string();
                        Log(res);
                        arr = new JSONArray(res);

                        if (arr == null){
                            Log("arr is empty");
                        }
                        Log("Response is successful with query");
                    }
                    else{
                        Log("Response has failed with query" + errorUrl);
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        return arr;
    }

    JSONArray Query(String url, String arrayName, String[] arrayVals, String secondParam, String secondValue) throws InterruptedException {
        arr = null;
        url = baseUrl + url;
        String toAdd = "";


        int size = arrayVals.length;

        for (int i = 0; i < size; i++){
            toAdd += arrayName + "[]="+ arrayVals[i];

            toAdd += "&";

        }

        toAdd += secondParam + "="+ secondValue;

        url += toAdd;

        final String errorUrl = url;

        //Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()){
                        Log("Response is successful");
                        String res = response.body().string();
                        Log(res);
                        arr = new JSONArray(res);

                        if (arr == null){
                            Log("arr is empty");
                        }
                        Log("Response is successful with query");
                    }
                    else{
                        Log("Response has failed with query" + errorUrl);
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        return arr;
    }

    JSONArray Query(String url, String param1, String val1, String param2, String val2) throws InterruptedException {
        arr = null;
        url = baseUrl + url;

        url += param2 + "=" +val2;
        url += "&" + param1 + "=" + val1;

        Log(url);

        //Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()){
                        Log("Response is successful");
                        String res = response.body().string();
                        Log(res);
                        arr = new JSONArray(res);

                        if (arr == null){
                            Log("arr is empty");
                        }
                        Log("Response is successful with query");
                    }
                    else{
                        Log("Response has failed with query");
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        return arr;
    }

    //endregion Queries

    //region Online PHP Functions

    JSONArray insert_lecturer_lecturerarr(String[] vals) throws InterruptedException {
        String[] params = new String[6];
        params[0] = "userID";
        params[1] = "firstName";
        params[2] = "lastName";
        params[3] = "email";
        params[4] = "nick";
        params[5] = "password";

        return Query( insert_lecturer_lecturerarr,params,vals);
    }

    JSONArray insert_student_studentarr(String[] vals) throws InterruptedException {
        String[] params = new String[6];
        params[0] = "userID";
        params[1] = "firstName";
        params[2] = "lastName";
        params[3] = "email";
        params[4] = "nick";
        params[5] = "password";

        return Query( insert_student_studentarr,params,vals);
    }

    JSONArray select_course_enrolled_userid_union_select_registered_userid(String userID) throws InterruptedException {

        return Query( select_course_enrolled_userid_union_select_registered_userid,"userID",userID);
    }


    JSONArray select_lecturer_registered_coursearr(String[] courses) throws InterruptedException {
        return Query( select_lecturer_registered_coursearr,"course",courses);
    }

    JSONArray select_registered_coursearr(String[] courses) throws InterruptedException {
        return Query( select_registered_coursearr,"course",courses);
    }

    JSONArray select_student_studentid(String userID) throws InterruptedException {
        return Query( select_student_studentid,"studentID",userID);
    }
    JSONArray select_task_coursearr(String[] courses) throws InterruptedException {
        return Query( select_task_coursearr,"course",courses);
    }
    JSONArray select_message_coursearr(String[] courses) throws InterruptedException {
        return Query( select_message_coursearr,"course",courses);
    }

    JSONArray select_wits_userid(String userID) throws InterruptedException {
        return Query( select_wits_userid,"userID",userID);
    }

    JSONArray select_test_wrote_coursearr_userid(String[] course, String userID) throws InterruptedException {
        return Query( select_test_wrote_coursearr_userid,"course",course,"userID",userID);
    }

    JSONArray update_lecturer_password_userid(String userID) throws InterruptedException {
        return Query( update_lecturer_password_userid,"userID",userID);
    }

    JSONArray update_student_password_userid(String userID) throws InterruptedException {
        return Query( update_student_password_userid,"userID",userID);
    }


    JSONArray delete_message_messageid(String messageID) throws InterruptedException {
        return Query(delete_message_messageid,"messageID",messageID);
    }

    JSONArray delete_task_taskid(String taskID) throws InterruptedException {
        return Query(delete_task_taskid,"taskID",taskID);
    }

    JSONArray insert_message(String[] vals) throws InterruptedException {
        String[] params = new String[6];
        params[0] = "messageName";
        params[1] = "classification";
        params[2] = "contents";
        params[3] = "courseCode";
        params[4] = "datePosted";
        params[5] = "userID";

        return Query(insert_message,params,vals);
    }

    JSONArray insert_task(String[] vals) throws InterruptedException {
        String[] params = new String[5];
        params[0] = "taskName";
        params[1] = "dueDate";
        params[2] = "courseCode";
        params[3] = "userID";
        params[4] = "dueTime";

        return Query(insert_task,params,vals);
    }

    JSONArray select_lecturer_userid(String lecID) throws InterruptedException {
        return Query(select_lecturer_userid,"userID",lecID);
    }

    JSONArray update_lecturer_lecturernickname_userid(String nick,String userID) throws InterruptedException {
        return Query(update_lecturer_lecturernickname_userid,"userID",userID,"nickname",nick);
    }

    JSONArray update_student_studentnickname_userid(String nick,String userID) throws InterruptedException {
        return Query(update_student_studentnickname_userid,"userID",userID,"nickname",nick);
    }

    JSONArray update_task_coursecode_taskid(String courseCode,String taskID) throws InterruptedException {
        return Query(update_task_coursecode_taskid,"courseCode",courseCode,"taskID",taskID);
    }

    JSONArray update_task_duedate_taskid(String dueDate,String taskID) throws InterruptedException {
        return Query(update_task_duedate_taskid,"dueDate",dueDate,"taskID",taskID);
    }

    JSONArray update_task_duetime_taskid(String dueTime,String taskID) throws InterruptedException {
        return Query(update_task_duetime_taskid,"dueTime",dueTime,"taskID",taskID);
    }

    JSONArray update_task_taskname_taskid(String taskName,String taskID) throws InterruptedException {
        return Query(update_task_taskname_taskid,"taskName",taskName,"taskID",taskID);
    }

    Boolean update_password_userid_newpass_oldpass(String[] vals) throws InterruptedException{
        String[] params = new String[4];
        params[0] = "userID";
        params[1] = "newPass";
        params[2] = "oldPass";
        params[3] = "isLec";

        try{
            if (Query(update_password_userid_newpass_oldpass, params,vals) == null){
                return false;
            }
        }catch (Exception e){
            Log(e.toString());
        }

        return true;

    }

    //endregion

    //region PHP Helper Functions
    JSONObject getJSONObj(JSONArray arr) throws JSONException {
        if (arr != null){
            return arr.getJSONObject(0);
        }
        Log("JSONArray is null");
        return null;
    }

    public boolean isLec(String userID) throws InterruptedException, JSONException, IOException {
        boolean isLec = isEmpty(select_lecturer_userid(userID));
        return  isLec;
    }

    boolean isEmpty(JSONArray arr) throws InterruptedException, IOException, JSONException {
        if (arr != null){
            return arr.isNull(0);
        }
        return true;
    }


    public boolean isInStudent(String userID) throws InterruptedException, JSONException, IOException {
        boolean studentEmpty = isEmpty(select_student_studentid(userID));
        return studentEmpty;
    }

    //endregion

    //region Old Funcs OnlineDB System , Don't Delete
    /*void Update(String tableName,String setting, String condition) {
        try{
            Log("UPDATE " + tableName + " SET " + setting + " WHERE " + condition);
            Query("UPDATE " + tableName + " SET " + setting + " WHERE " + condition);

        }catch (Exception e) { Log(e.toString()); }
    }


    void Insert(String tableName,String[] columns, String[] values) {

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

        try{
            Query("INSERT INTO " + tableName + " (" + stringCols + ") VALUES (" +  stringVals + ")");

        }catch (Exception e) { Log(e.toString()); }
    }


    public void Insert(String tableName, String[] values) throws InterruptedException, IOException, JSONException {
        String stringVals = "";
        int size = values.length;

        for (int i = 0; i < size; i++){
            stringVals += values[i];

            if (i != size - 1){
                stringVals += ",";
            }
        }

        Log("INSERT INTO " + tableName + " VALUES(" + stringVals + ")");
        Query("INSERT INTO " + tableName + " VALUES(" + stringVals + ")");
    }

    void Delete (String tableName, String condition) {
        try{
            Query("DELETE FROM " + tableName + " WHERE " + condition);

        }catch (Exception e) { Log(e.toString()); }
    }



    JSONArray getJSONArr(String query) throws InterruptedException, IOException, JSONException {
        Log(query);
        Query(query);

        if (arr != null){
            return arr;
        }
        Log("About to return null JSONArry with Query, " + query);
        return arr;
    }

    JSONObject getJSONObj(String query) throws InterruptedException, JSONException, IOException {
        Query(query);

        if (arr.length() > 1){
            Log("ARRAY IS GREATER THAN ONE");
            Log(query);
        }

        if (arr.length() == 0){
            Log("Object is empty");
            Log(query);
        }

        return arr.getJSONObject(0);
    }


    */
    //endregion

}
