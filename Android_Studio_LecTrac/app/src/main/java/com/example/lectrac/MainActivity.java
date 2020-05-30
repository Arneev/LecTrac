package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseManager db = new DatabaseManager(this,"dbManager",null,1);

        try {
            db.Display(MainActivity.this);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }


    }
}
