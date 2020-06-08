package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

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

import static com.example.lectrac.Syncer.*;

public class MainActivity extends AppCompatActivity {

    public static void Log(String error){
        Log.i("Perso",error);
    }

    static LocalDatabaseManager localDB;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                localDB = new LocalDatabaseManager(MainActivity.this);
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (localDB.isLoggedIn()){
            //region Sync
            //            try {
            //                Sync(this);
            //            } catch (JSONException e) {
            //                e.printStackTrace();
            //            } catch (IOException e) {
            //                e.printStackTrace();
            //            } catch (InterruptedException e) {
            //                e.printStackTrace();
            //            } catch (ParseException e) {
            //                e.printStackTrace();
            //            }
            //            //endregion
            startActivity(new Intent(MainActivity.this, DrawerActivity.class));
            return;
        }

        //configureNextButton();
        openRegistration();

        //region Sync
        try {
            Sync(this);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //endregion
    }


    public void LoginButtonClick(View v) throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        RegisterLoginManager loginManager = new RegisterLoginManager();

        TextView tvUserID = (TextView)findViewById(R.id.edtUserID);
        String userID =  tvUserID.getText().toString();

        TextView tvPassword = (TextView)findViewById(R.id.edtPassword);
        String password =  tvPassword.getText().toString();

        Log("Attempting to LogInAttempt()");
        boolean isSuccessful = loginManager.LogInAttempt(password,userID,this);

        if (isSuccessful){
            Log("LOG IN IS SUCCESSFUL <3 :P");
            startActivity(new Intent(MainActivity.this, DrawerActivity.class));
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

//    private void configureNextButton(){
//
//        Button loginButton = (Button) findViewById(R.id.btnLogin);
//        loginButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, DrawerActivity.class));
//            }
//        });
//    }




}
