package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {

    public static void Log(String error){
        Log.i("Perso",error);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        exitRegistration();
    }

    public void RegisterButtonClick(View v) throws InterruptedException, NoSuchAlgorithmException, JSONException {
        RegisterLoginManager registerManager = new RegisterLoginManager();

        TextView tvUserID = (TextView)findViewById(R.id.edtUserID);
        String userID = tvUserID.getText().toString();

        TextView tvFirstName = (TextView)findViewById(R.id.edtFirstName);
        String firstName = tvFirstName.getText().toString();

        TextView tvSurname = (TextView)findViewById(R.id.edtSurname);
        String surname = tvSurname.getText().toString();

        TextView tvEmail = (TextView)findViewById(R.id.edtEmail);
        String email = tvEmail.getText().toString();

        TextView tvNickname = (TextView)findViewById(R.id.edtNickname);
        String nickname = tvNickname.getText().toString();

        TextView tvPassword = (TextView)findViewById(R.id.edtPassword);
        String password = tvPassword.getText().toString();

        TextView tvConfirmPassword = (TextView)findViewById(R.id.edtConfirmPassword);
        String confirmPassword = tvConfirmPassword.getText().toString();

        boolean isSuccessful = registerManager.RegisterAttempt(userID,firstName,surname,email,nickname,password,confirmPassword);
        if (isSuccessful){
            Log("LOG IN IS SUCCESSFUL <3 :P");
        }
        else{
            Log("LOG IN IS NOT SUCCESSFUL :(");
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
}
