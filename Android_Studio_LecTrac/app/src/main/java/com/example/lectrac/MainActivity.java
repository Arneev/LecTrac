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

import static com.example.lectrac.HelperFunctions.quote;
import static com.example.lectrac.HelperFunctions.tblWITS;

public class MainActivity extends AppCompatActivity {

    public static void Log(String error){
        Log.i("Perso",error);
    }

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        configureNextButton();

        openRegistration();



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

    private void configureNextButton(){

        Button loginButton = (Button) findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DrawerActivity.class));
            }
        });
    }




}
