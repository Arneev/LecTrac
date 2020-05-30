package com.example.lectrac;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DatabaseManager extends SQLiteOpenHelper {

    //CONSTANTS
    private static String tblWITS = "WITS";

    //Initialization
    OkHttpClient client = new OkHttpClient();
    JSONArray arr = null;

    public DatabaseManager(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    void Debug(String error){
        Log.d("DEBUG","The problem is at" + error);
    }



    void Query(String query) throws InterruptedException {


        //Url
        String url = "https://lamp.ms.wits.ac.za/home/s2180393/Query.php";
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        httpBuilder.addQueryParameter("query", query);
        url = httpBuilder.build().toString();

        //Request
        Request request = new Request.Builder()
                .url(url)
                .build();

        //Response

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Debug("Request Failed");
                notify();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        arr = new JSONArray(response.body().string());
                        //notify();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //notify();
                } else {
                    Debug("Welp, response failed :(");
                    //notify();
                }
            }
        });

        //client.wait();

    }

       /* void Display (final Activity a) throws InterruptedException, JSONException {
            Query("SELECT * FROM WITS");
            JSONArray Jarr = arr;

            if (Jarr == null) {
                Debug("JSONArray is null");
                return;
            }
            JSONObject obj = Jarr.getJSONObject(0);
            Debug("Success!");


        }*/



}

