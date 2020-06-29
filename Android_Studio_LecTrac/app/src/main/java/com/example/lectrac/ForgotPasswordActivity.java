package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.lectrac.HelperFunctions.CopyOnly;
import static com.example.lectrac.HelperFunctions.STUDENT_NUMBER_LENGTH;
import static com.example.lectrac.HelperFunctions.hasWhitespace;
import static com.example.lectrac.HelperFunctions.passwordLength;
import static com.example.lectrac.HelperFunctions.saltAndHash;
import static com.example.lectrac.HelperFunctions.setNightMode;

public class ForgotPasswordActivity extends AppCompatActivity {


    public static void Log(String error){
        Log.i("Perso",error);
    }


    static ErrorClass ec;

    String sUserID, sPassword;

    OnlineDatabaseManager onlineDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

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
                    sUserID = etUserID.getText().toString();
                    sPassword = etPassword.getText().toString();

                    beginProcess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void beginProcess() throws JSONException {

        if (checkUserID(sUserID)){

            if (checkPassword(sPassword)){

                if (resetPassword(sUserID)){

                    ec.ShowUserMessage("Password has been reset");
                }
            }
        }


    }

    //region moveToForgot Password stuff


    public boolean resetPassword(String userID){

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
        }

        ec.ShowUserError("Please enter a valid WITS ID");
        return  false;
    }



    boolean checkUserID(String UserID){

        Log("checkUserID");

        if (UserID.isEmpty()){
            ec.ShowUserError("Please enter valid WITS ID",this);
            return false;
        }

        if (hasWhitespace(UserID)){
            ec.ShowUserError("Please enter valid WITS ID",this);
            return false;
        }

        for (int i = 0; i < UserID.length(); i++){

            if (!Character.isDigit(UserID.charAt(i))){
                ec.ShowUserError("Please enter valid WITS ID",this);
                return false;
            }
        }

        int intStudentID = Integer.parseInt(UserID);

        if (intStudentID < 0){
            ec.ShowUserError("Please enter valid WITS ID",this);
            return false;
        }

        if (UserID.length() != 7){
            ec.ShowUserError("Please enter valid WITS ID",this);
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


    //endregion
}
