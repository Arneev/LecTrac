package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.*;

public class RegisterActivity extends AppCompatActivity {


    static ErrorClass ec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ec = new ErrorClass(this);

        exitRegistration();
    }

    public void RegisterButtonClick(View v) throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
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

        TextView tvConfirmPassword = (TextView)findViewById(R.id.edtConfirmPassword);
        String confirmPassword = tvConfirmPassword.getText().toString();

        boolean isSuccessful = registerManager.RegisterAttempt(userID,firstName,surname,email,nickname,password,confirmPassword,this);
        if (isSuccessful){
            Log("REGISTER IN IS SUCCESSFUL <3 :P");
            boolean logInSuccess = registerManager.LogInAttempt(password,userID,this);

            if (logInSuccess){
                Log("Log in success");

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //region Try Syncing
                        try {
                            Syncer syncClass = new Syncer(RegisterActivity.this);
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
                    }
                });

                t.start();
                t.join();

                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
            else{
                Log("Failed to login, log in was NOT successful");
            }
        }
        else{
            Log("REGISTER IN IS NOT SUCCESSFUL :(");
        }
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

    //region Helper Function

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, MainActivity.class));
    }

    //endregion
}
