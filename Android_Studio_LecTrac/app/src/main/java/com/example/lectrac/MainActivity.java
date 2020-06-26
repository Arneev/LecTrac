package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.ShowUserError;
import static com.example.lectrac.HelperFunctions.setNightMode;
import static com.example.lectrac.Syncer.*;

public class MainActivity extends AppCompatActivity {

    public static void Log(String error){
        Log.i("Perso",error);
    }

    static LocalDatabaseManager localDB;
    static Context context;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        SetToDefault();

        context = this;


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                localDB = new LocalDatabaseManager(MainActivity.this);

                if (localDB.isLoggedIn()){
                    //region Try Syncing
                    try {
                        Syncer syncClass = new Syncer(context);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //endregion

                    startActivity(new Intent(MainActivity.this, DrawerActivity.class));
                    return;
                }

            }
        });



        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log(e.toString());
        }

        openRegistration();

    }


    public void LoginButtonClick(View v) throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        RegisterLoginManager loginManager = new RegisterLoginManager();

        TextView tvUserID = (TextView)findViewById(R.id.edtUserID);
        String userID =  tvUserID.getText().toString();

        TextView tvPassword = (TextView)findViewById(R.id.edtPassword);
        String password =  tvPassword.getText().toString();

        Log("Attempting to LogInAttempt()");
        boolean isSuccessful = loginManager.LogInAttempt(password,userID,context);

        if (isSuccessful){
            Log("LOG IN IS SUCCESSFUL <3 :P");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    //region Try Syncing
                    try {
                        Syncer syncClass = new Syncer(MainActivity.this, true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //endregion

                    startActivity(new Intent(MainActivity.this, DrawerActivity.class));
                    return;
                }
            });

            t.start();
            t.join();
        }
        else{
            Log("LOG IN IS NOT SUCCESSFUL :(");
        }
    }

    private void openRegistration(){

        TextView openRegister = (TextView) findViewById(R.id.create_acc);
        openRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    public void SetToDefault(){

        TextView userID = findViewById(R.id.edtUserID);
        TextView pass = findViewById(R.id.edtPassword);

        userID.setText("");
        pass.setText("");
    }


    //region Helper Function

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, RegisterActivity.class));
    }

    //endregion

}
