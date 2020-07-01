package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
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
import java.util.Calendar;
import java.util.Date;

import static com.example.lectrac.HelperFunctions.*;
import static com.example.lectrac.Syncer.*;

public class MainActivity extends AppCompatActivity {

    //region Intitialization
    static LocalDatabaseManager localDB;
    static Context context;
    static ErrorClass ec;
    static Button loginBtn;
    static TextView lblForgotPass;
    static boolean onForgotPass;
    static TextView tvPass;
    HelperFunctions hp = new HelperFunctions();
    static ProgressBar progressBar;

    //endregion

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
        myAlarm();
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
        setNightMode(this);

    }

    //region Listeners
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

    //endregion

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, RegisterActivity.class));
    }


    //region Notifications
    public void myAlarm() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 30);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
        else{
            Log("Alarm manager is null");
        }

        Log("Finish set up alarm");

    }


    //endregion
}
