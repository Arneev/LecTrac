package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.Log;
import static com.example.lectrac.HelperFunctions.isDarkMode;
import static com.example.lectrac.HelperFunctions.isOnline;
import static com.example.lectrac.HelperFunctions.*;
import static com.example.lectrac.HelperFunctions.myPrefName;
import static com.example.lectrac.HelperFunctions.setNightMode;
import static com.example.lectrac.Syncer.*;

public class MainActivity extends AppCompatActivity {


    public static void Log(String error){
        Log.i("Perso",error);
    }

    static LocalDatabaseManager localDB;
    static Context context;
    static ErrorClass ec;
    static Button loginBtn;
    static TextView lblForgotPass;
    static boolean onForgotPass;
    static TextView tvPass;
    HelperFunctions hp = new HelperFunctions();
    static ProgressBar progressBar;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBarLogin);
        ec = new ErrorClass(this);
        loginBtn = findViewById(R.id.btnLogin);
        lblForgotPass = findViewById(R.id.lblForgotPassword);
        tvPass = findViewById(R.id.edtPassword);
        setLblForgotPassListener();
        setButtonListener();

        resetPasswordClick();
        SetToDefault();

        context = this;


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                localDB = new LocalDatabaseManager(MainActivity.this);

                if (localDB.isLoggedIn()){
                    setNightMode(context);
                    //region Try Syncing
                    try {
                        Syncer syncClass = new Syncer(context);
                    } catch (Exception e){
                        ec.ShowUserMessage(showCheckInternetConnection,MainActivity.this);
                    }
                    //endregion

                    startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                    return;
                }
                else{
                    SharedPreferences sharedPreferences = getSharedPreferences(myPrefName, Context.MODE_PRIVATE);

                    if (!sharedPreferences.contains("isDarkMode")){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isDarkMode", false);
                        editor.apply();
                    }



                }

            }
        });



        t.start();
        progressBar.setVisibility(View.VISIBLE);
        try {
            t.join();
        } catch (InterruptedException e) {
            Log(e.toString());
        }
        progressBar.setVisibility(View.GONE);
        openRegistration();

    }

    public void setButtonListener(){
        loginBtn = findViewById(R.id.btnLogin);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                try {
                    LoginButtonClick();
                } catch (Exception e) {
                    ec.ShowUserError(showCheckInternetConnection,MainActivity.this);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void LoginButtonClick() throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        if (!isOnline(this)){
            ec.ShowUserMessage(showNotConnected,this);
            return;
        }
        RegisterLoginManager loginManager = new RegisterLoginManager();

        TextView tvUserID = (TextView)findViewById(R.id.edtUserID);
        String userID =  tvUserID.getText().toString();

        TextView tvPassword = (TextView)findViewById(R.id.edtPassword);
        String password =  tvPassword.getText().toString();

        Log("Attempting to LogInAttempt()");
        boolean isSuccessful = loginManager.LogInAttempt(password,userID,context);

        if (isSuccessful){
            Log("LOG IN IS SUCCESSFUL <3 :P");
            SharedPreferences sharedPreferences = getSharedPreferences(myPrefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isDarkMode", false);
            editor.apply();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    //region Try Syncing

                    try{
                        Syncer syncClass = new Syncer(MainActivity.this, false,true);
                    }
                    catch (Exception e){
                        ec.ShowUserMessage(showCheckInternetConnection,MainActivity.this);
                    }
                    //endregion

                    startActivity(new Intent(MainActivity.this, CalendarActivity.class));
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

    public void resetPasswordClick(){

        lblForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
            }
        });
    }


    public void SetToDefault(){

        TextView userID = findViewById(R.id.edtUserID);
        TextView pass = findViewById(R.id.edtPassword);

        userID.setText("");
        pass.setText("");
    }


    public void setLblForgotPassListener(){
        lblForgotPass = findViewById(R.id.lblForgotPassword);
        lblForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onForgotPass = true;
            }
        });
    }




    //region Helper Function

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, RegisterActivity.class));
    }

    //endregion

}
