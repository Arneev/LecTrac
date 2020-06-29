package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import static com.example.lectrac.HelperFunctions.STUDENT_NUMBER_LENGTH;
import static com.example.lectrac.HelperFunctions.hasWhitespace;
import static com.example.lectrac.HelperFunctions.isDarkMode;
import static com.example.lectrac.HelperFunctions.myPrefName;
import static com.example.lectrac.HelperFunctions.passwordLength;
import static com.example.lectrac.HelperFunctions.quote;
import static com.example.lectrac.HelperFunctions.setNightMode;
import static com.example.lectrac.HelperFunctions.tblLecturer;
import static com.example.lectrac.HelperFunctions.tblStudent;
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
    static Button forgotPassButton;
    static TextView tvPass;
    static TextView tvUsername;
    static boolean onForgotPass;
    static ProgressBar progressBar;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onForgotPass = false;
        ec = new ErrorClass(this);

        loginBtn = findViewById(R.id.btnLogin);
        lblForgotPass = findViewById(R.id.lblForgotPassword);
        forgotPassButton = findViewById(R.id.btnForgotPassSubmit);
        tvPass = findViewById(R.id.edtPassword);
        tvUsername = findViewById(R.id.edtUserID);
        progressBar = findViewById(R.id.progbar_login);


        ec.endProgressBar(progressBar);
        setLoginButtonListener();
        setLblForgotPassListener();


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

                    startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                    return;
                }
                else{
                    SharedPreferences.Editor editor = getSharedPreferences(myPrefName, Context.MODE_PRIVATE).edit();
                    editor.putBoolean("isDarkMode", false);
                    editor.apply();
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

    public void setLoginButtonListener(){
        loginBtn = findViewById(R.id.btnLogin);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LoginButtonClick();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void LoginButtonClick() throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        ec.startProgressBar(progressBar);
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

                    try{
                        Syncer syncClass = new Syncer(MainActivity.this, false,true);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //endregion

                    //endProgressBar();
                    startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                    return;
                }
            });

            t.start();
            t.join();

        }
        else{
            Log("LOG IN IS NOT SUCCESSFUL :(");
            ec.endProgressBar(progressBar);
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


    public void setLblForgotPassListener(){
        lblForgotPass = findViewById(R.id.lblForgotPassword);
        lblForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setForgotButtonListener();

                loginBtn.setVisibility(View.GONE);
                tvPass.setVisibility(View.GONE);
                forgotPassButton.setVisibility(View.VISIBLE);
                onForgotPass = true;
            }
        });
    }

    public void setForgotButtonListener(){
        forgotPassButton = findViewById(R.id.btnForgotPassSubmit);

        forgotPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = tvUsername.getText().toString();

                if (!checkStudentID(userID)){
                    return;
                }

                if (resetPassword(userID)) {
                    GoBackToOrig();
                }
            }
        });
    }

    public boolean resetPassword(String userID){
        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

        try {
            JSONObject obj = onlineDB.getJSONObj(onlineDB.select_wits_userid(userID));



            String pass = obj.getString("Password");

            if (onlineDB.isLec(userID)){
                onlineDB.update_lecturer_password_userid(userID);
                return true;
            }

            if (onlineDB.isInStudent(userID)){
                onlineDB.update_student_password_userid(userID);
                return true;
            }


        }catch (Exception e){
            Log(e.toString());
        }
        ec.ShowUserError("Please enter a valid WITS ID");
        return  false;
    }

    public void GoBackToOrig(){
        loginBtn.setVisibility(View.VISIBLE);
        tvPass.setVisibility(View.VISIBLE);
        forgotPassButton.setVisibility(View.GONE);
        onForgotPass = false;
    }

    boolean checkStudentID(String studentID){
        Log("checkStudentID");
        if (hasWhitespace(studentID)){
            return false;
        }

        try{
            int intStudentID = Integer.parseInt(studentID);

            if (intStudentID < 0){
                ec.ShowUserError("Enter valid student number",context);
                return false;
            }
        }catch (Exception e){
            ec.ShowUserError("Enter valid student number",context);
            return false;
        }

        if (studentID.length() != STUDENT_NUMBER_LENGTH){
            ec.ShowUserError("Enter valid student number",context);
            return false;
        }

        // TEST CASE WHEN USER IS NOT IN DATABASE IS TAKEN CARE OF IN checkPassword();

        return true;

    }





    //region Helper Function

    @Override
    public void onBackPressed(){
        if (onForgotPass){
            GoBackToOrig();
            return;
        }
        startActivity(new Intent(this, RegisterActivity.class));
    }

    //endregion

}
