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

    //region Initialization
    static OkHttpClient client = new OkHttpClient();
    static JSONArray arr = null;
    HelperFunctions hp = new HelperFunctions();
    //endregion

    void Query(final String query) throws InterruptedException, IOException, JSONException {
        arr = null;

        //Url
        String url = "https://lamp.ms.wits.ac.za/home/s2180393/Query.php";
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("query", query);
        url = httpBuilder.build().toString();

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
                        Log("Response is successful with query " + query);
                    }
                    else{
                        Log("Response has failed with query " + query);
                    }
                }catch (Exception e){
                    Log(e.toString());
                    Log("Sync never work");
                }
            }
        });

        t.start();
        t.join();
        Log("Thread join complete");
    }

    void Display (String tableName) throws InterruptedException, JSONException {
        try{
            Query("SELECT * FROM " + tableName);
//            DoSomething()
        }catch (Exception e)
        {
            Log(e.toString());
        }


    }

    boolean QueryBool(String query) throws Exception {

        //If found a row that matches query, will return true
        try{
            Query(query);
            if (arr != null) return true;
            return false;

        }catch (Exception e) {
            Log(e.toString());
            throw new Exception(e);
        }

    }

    void Update(String tableName,String setting, String condition) {
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

    void Insert(String tableName, String values) {
        try{
            Query("INSERT INTO " + tableName + " VALUES (" + values + ")");

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

    void Delete(String tableName, boolean youSure) throws InterruptedException {
        if (!youSure){
            Log("Can NOT delete table, you are not sure");
            return;
        }

        try{
            Query("DELETE FROM " + tableName);

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

    boolean isEmpty(String tableName, String condition) throws InterruptedException, IOException, JSONException {
        String query = "SELECT * FROM " + tableName + " WHERE " + condition;
        Query(query);

        if (arr.length() == 0) {
            Log("The table, " + tableName + ", is empty with the condition " + condition);
            return true;
        }

        return false;
    }

    public JSONObject getUserID(String userID) throws InterruptedException, IOException, JSONException {
       return getJSONObj("SELECT * FROM STUDENT WHERE Student_ID = " + quote(userID) + " UNION " +
                "SELECT * FROM LECTURER WHERE Lecturer_ID = " + quote(userID));
    }



}
