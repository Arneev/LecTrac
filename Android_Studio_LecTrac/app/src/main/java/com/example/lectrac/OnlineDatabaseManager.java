package com.example.lectrac;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
 import androidx.arch.core.util.Function;
 import okhttp3.Call;
 import okhttp3.Callback;
 import okhttp3.HttpUrl;
 import okhttp3.OkHttpClient;
 import okhttp3.Request;
 import okhttp3.Response;

final public class OnlineDatabaseManager {

    //region CONSTANTS
    private static String tblWITS = "WITS";
    //endregion

    //region Initialization
    OkHttpClient client = new OkHttpClient();
    static JSONArray arr = null;
    //endregion

    //region HelperFunctions
    public static void Log(String error){
        Log.i("Perso",error);
    }
    //endregion


    void Query(final String query) throws InterruptedException {
        arr = null;

        //Testing
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        //Url
        String url = "https://lamp.ms.wits.ac.za/home/s2180393/Query.php";
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("query", query);
        url = httpBuilder.build().toString();

        //Request
        Request request = new Request.Builder()
                .url(url)
                .build();

        Log(url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log("onFailure method for callback");
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()){
                    try{
                        arr = new JSONArray(response.body().string());
                        Log("Response is successful :D , " + arr.toString());
                    }catch (Exception e){
                        arr = null;
                        Log(e.toString());
                    }
                    finally {
                        countDownLatch.countDown();
                    }
                }else {
                    Log("Response is NOT successful :(");
                }
            }
        });

       countDownLatch.await();
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

    void Update(String tableName,String column_value, String condition) {
        try{
            Query("UPDATE " + tableName + " SET " + column_value + " WHERE " + condition);

        }catch (Exception e) { Log(e.toString()); }
    }

    void Insert(String tableName,String columns, String values) {
        try{
            Query("INSERT INTO " + tableName + " (" + columns + ") VALUES (" +  values + ")");

        }catch (Exception e) { Log(e.toString()); }
    }

    void Insert(String tableName, String values) {
        try{
            Query("INSERT INTO " + tableName + " VALUES (" + values + ")");

        }catch (Exception e) { Log(e.toString()); }
    }

    public void Insert(String tableName, String[] values) throws InterruptedException {
        String stringVals = "";
        int size = values.length;

        for (int i = 0; i < size; i++){
            stringVals += values[i];

            if (i != size - 1){
                stringVals += ",";
            }
        }

        Query("INSERT INTO " + tableName + " VALUES(" + values + ")");
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

    JSONArray getJSONArr(String query) throws InterruptedException {
        Query(query);
        return arr;
    }

    JSONObject getJSONObj(String query) throws InterruptedException, JSONException {
        Query(query);

        if (arr.length() > 1){
            Log("ARRAY IS GREATER THAN ONE");
        }
        return arr.getJSONObject(0);
    }

    boolean isEmpty(String tableName, String condition) throws InterruptedException {
        String query = "SELECT * FROM " + tableName + " WHERE " + condition;
        Query(query);

        if (arr.length() == 0) {
            Log("The table, " + tableName + ", is empty with the condition " + condition);
            return true;
        }

        return false;
    }

}
