package com.example.lab6;

import android.util.Log;
import android.view.Display;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;

import androidx.arch.core.util.Function;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DatabaseHelper {
    //CONSTANTS
    private static String tblWITS = "WITS";

    void Error(String error){
        Log.e("ERROR","The problem is at" + error);
    }

    void printInfo(String info) {
        Log.i("INFO",info);
    }

    JSONArray Query(final String query){
        //Intialization
        OkHttpClient client = new OkHttpClient();
        String url = "https://lamp.ms.wits.ac.za/home/s2180393/Query.php?query=";
        url += query;

        Request request = new Request.Builder()
                          .url(url)
                          .build();

        //Request Enqueue
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Error("enqueue failure for request - OkHttpClient, when querying " + query);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()){
                    Error("Response is not successful - OkHttpClient, when querying " + query);
                    Error("Unexpected code " + response.toString());
                    throw new IOException("Unexpected code" + response);
                }
                else{//response.isSuccessful
                    try {//trying to get response data
                        JSONArray arr = new JSONArray(response.body().string());

                    } catch (JSONException e) {
                        Error("Response data is unsuccessful, when querying" + query);
                        e.printStackTrace();
                    }
                }//End response.isSuccessful
            }
        });//end of OnResponse
        
        return arr;
        //end of Query function
    }

    void Display(){
        JSONArray arr = Query("SELECT * FROM " + tblWITS);
        try {
            JSONObject obj = arr.getJSONObject(0);
            printInfo(obj.toString());
        } catch (JSONException e) {
            Error("Display, object at index might not exist");
            e.printStackTrace();
        }


    }


}
