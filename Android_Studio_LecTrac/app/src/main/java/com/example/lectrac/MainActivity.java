package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Start();


    }

    //Start Method
    void Start(){
        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();


    }


}
