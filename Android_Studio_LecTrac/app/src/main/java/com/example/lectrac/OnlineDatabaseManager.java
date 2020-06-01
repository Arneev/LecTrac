package com.example.lectrac;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

public class OnlineDatabaseManager {

    //region CONSTANTS
    private static String tblWITS = "WITS";
    //endregion

    //region Initialization
    OkHttpClient client = new OkHttpClient();
    static JSONArray arr = null;
    //endregion

    //region Helper Functions
    void Log(String error){
        Log.i("Perso",error);
    }

    //endregion Helper Functions

    void Query(final String query) throws InterruptedException {

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

    void Display () throws InterruptedException, JSONException {
        Query("SELECT * FROM WITS");
        Log("Display arr is " + arr.toString());

        if (arr == null) {
            Log("JSONArray is null");
            return;
        }

        JSONObject obj = arr.getJSONObject(0);
        //Log("JSON FIRST OBJECT IS" + obj.toString());

    }



}
