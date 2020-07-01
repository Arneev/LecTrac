package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lectrac.HelperFunctions.CopyOnly;
import static com.example.lectrac.HelperFunctions.STUDENT_NUMBER_LENGTH;
import static com.example.lectrac.HelperFunctions.hasWhitespace;
import static com.example.lectrac.HelperFunctions.isOnline;
import static com.example.lectrac.HelperFunctions.passwordLength;
import static com.example.lectrac.HelperFunctions.saltAndHash;
import static com.example.lectrac.HelperFunctions.setNightMode;
import static com.example.lectrac.HelperFunctions.showNotConnected;
import static com.example.lectrac.HelperFunctions.Log;

public class ForgotPasswordActivity extends AppCompatActivity {

    //region Intialization


    static ErrorClass ec;
    static ImageView imgBack;

    String sUserID, sPassword;

    OnlineDatabaseManager onlineDB;
    static ProgressBar progressBar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        progressBar = findViewById(R.id.progressBarForgotPass);
        imgBack = findViewById(R.id.back);
        setForgotPassBack();
        setNightMode(this);

        ec = new ErrorClass(this);
        onlineDB = new OnlineDatabaseManager();

        getInput();
    }

    public void getInput(){

        final EditText etUserID = findViewById(R.id.edtForgotUserID);
        final EditText etPassword = findViewById(R.id.edtForgotPassword);
        Button btChangePassword = findViewById(R.id.btnChangePassword);

        btChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    sUserID = etUserID.getText().toString();
                    sPassword = etPassword.getText().toString();

                    beginProcess();
                } catch (JSONException e) {
                    ec.ShowUserError("Fill in the fields again please");
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    public void beginProcess() throws JSONException {
        if (!isOnline(ForgotPasswordActivity.this)){
            ec.ShowUserMessage(showNotConnected);
            return;
        }

        if (checkUserID(sUserID)){

            if (checkPassword(sPassword)){

                if (resetPassword(sUserID)){

                    ec.ShowUserMessage("Password has been reset");
                }
                else{
                    ec.ShowUserError("Failed to reset password, try again or contact support");
                }
            }
        }


    }

    //region moveToForgot Password stuff

    public boolean resetPassword(String userID){
        Log("about to reset pass");
        try {

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
            ec.ShowUserError(showNotConnected);
            return false;
        }

        ec.ShowUserError("Please enter a valid WITS ID");
        return  false;
    }

    boolean checkUserID(String UserID){

        Log("checkUserID");

        try{
            if (UserID.isEmpty()){
                ec.ShowUserError("Please enter valid WITS ID",this);
                Log("userID is empty");
                return false;
            }

            if (hasWhitespace(UserID)){
                ec.ShowUserError("Please enter valid WITS ID",this);
                Log("userID has whitespace");
                return false;
            }

            for (int i = 0; i < UserID.length(); i++){

                if (!Character.isDigit(UserID.charAt(i))){
                    ec.ShowUserError("Please enter valid WITS ID",this);
                    Log("userID is not all digit");
                    return false;
                }
            }


            int intStudentID = Integer.parseInt(UserID);

            if (intStudentID < 0){
                ec.ShowUserError("Please enter valid WITS ID",this);
                Log("userID is less than 0");
                return false;
            }

            if (UserID.length() != 7){
                ec.ShowUserError("Please enter valid WITS ID",this);
                Log("userID it not length 7");
                return false;
            }
        }catch (Exception e){
            ec.ShowUserError("Please enter valid WITS ID",this);
            Log(e.toString());
            return false;
        }


        // TEST CASE WHEN USER IS NOT IN DATABASE IS TAKEN CARE OF IN checkPassword();

        return true;

    }

    boolean checkPassword(String Password) throws JSONException {

        try {
            JSONObject obj = onlineDB.getJSONObj(onlineDB.select_wits_userid(sUserID));

            assert obj != null;
            String pass = obj.getString("Password");

            Password = saltAndHash(Password);
            Password = CopyOnly(Password, passwordLength);


            if (Password.equals(pass)){
                return true;
            }
            else {
                ec.ShowUserError("Please enter correct WITS password", this);
                return false;
            }
        }
        catch (Exception e){

            Log(e.toString());
            ec.ShowUserError("Please enter a valid WITS ID", this);
            return false;
        }


    }

    void setForgotPassBack(){
        imgBack = findViewById(R.id.back);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
            }
        });
    }

    //endregion
}
