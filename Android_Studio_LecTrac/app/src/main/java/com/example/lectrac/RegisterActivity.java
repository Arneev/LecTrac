package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.*;

public class RegisterActivity extends AppCompatActivity {

    //region Intialization
    static ErrorClass ec;
    static Button createAccButton;
    static ProgressBar progressBar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressBar = findViewById(R.id.progressBarRegister);
        ec = new ErrorClass(this);
        createAccButton = findViewById(R.id.btnCreateAcc);
        setRegisterButtonClick();
        exitRegistration();
    }

    //region Clickers
    public void RegisterButtonClick() throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        if (!isOnline(this)){
            ec.ShowUserMessage("You are not connected to the internet",this);
            return;
        }

        RegisterLoginManager registerManager = new RegisterLoginManager();

        TextView tvUserID = (TextView)findViewById(R.id.edtUserID);
        String userID = tvUserID.getText().toString();

        TextView tvFirstName = (TextView)findViewById(R.id.edtFirstName);
        String firstName = tvFirstName.getText().toString();

        TextView tvSurname = (TextView)findViewById(R.id.edtSurname);
        String surname = tvSurname.getText().toString();

        TextView tvEmail = (TextView)findViewById(R.id.edtEmail);
        String email = tvEmail.getText().toString();

        TextView tvNickname = (TextView)findViewById(R.id.edtSettingsNickname);
        String nickname = tvNickname.getText().toString();

        TextView tvPassword = (TextView)findViewById(R.id.edtPassword);
        String password = tvPassword.getText().toString();


        boolean isSuccessful = registerManager.RegisterAttempt(userID,firstName,surname,email,nickname,password,this);
        if (isSuccessful){
            Log("REGISTER IN IS SUCCESSFUL <3 :P");
//            boolean logInSuccess = registerManager.LogInAttempt(password,userID,this);
//
//            if (logInSuccess){
//                Log("Log in success");
//
//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //region Try Syncing
//                        try {
//                            Syncer syncClass = new Syncer(RegisterActivity.this);
//                        } catch (Exception e){
//                            ec.ShowUserMessage(showCheckInternetConnection,RegisterActivity.this);
//                        }
//                        //endregion
//                    }
//                });
//
//                t.start();
//                t.join();

                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
//            }
//            else{
//                Log("Failed to login, log in was NOT successful");
//            }
        }
        else{
            Log("REGISTER IN IS NOT SUCCESSFUL :(");
        }
    }

    public void setRegisterButtonClick(){
        createAccButton = findViewById(R.id.btnCreateAcc);

        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                try{
                    RegisterButtonClick();
                }catch (Exception e){
                    ec.ShowUserError(showCheckInternetConnection, RegisterActivity.this);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void exitRegistration(){

        ImageView exitRegister = (ImageView) findViewById(R.id.back);
        exitRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
    }
    //endregion

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, MainActivity.class));
    }

}
