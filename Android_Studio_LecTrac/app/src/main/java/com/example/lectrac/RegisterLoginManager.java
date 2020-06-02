package com.example.lectrac;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterLoginManager{

    //region Constants
    final static String START_SALT = "!@We#4";
    final static String  END_SALT = "HQWn98";
    final static int STUDENT_NUMBER_LENGTH = 7;
    //endregion

    //region Initialization
    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    //endregion

    //region HelperFunction
    public static void Log(String error){
        Log.i("Perso",error);
    }

    public static void ShowUserError(String error){
        //Show Error, for now lets just Log
        Log(error);
        //Change this in the future
    }
    //endregion

    void LoginAttempt(String password, String studentID) throws InterruptedException, NoSuchAlgorithmException {
        password = password.trim();
        studentID = studentID.trim();

        if (checkPassword(password,studentID) && checkStudentID(studentID)){
            LogIn();
        }else{
            //Error messages from checkStudentID and
            // checkPassword will show no need to worry

            Log("Login Failed");
        }
    }

    void LogIn(){

    }

    boolean hasWhitespace(String line){
        int size = line.length();

        for (int i = 0; i < size; i++){
            if (line.charAt(i) == ' ') return true;
        }

        return false;
    }

    boolean checkStudentID(String studentID){

        if (hasWhitespace(studentID)){
            return false;
        }

        try{
            int intStudentID = Integer.parseInt(studentID);

            if (intStudentID < 0){
                ShowUserError("Enter valid student number");
                return false;
            }
        }catch (Exception e){
            ShowUserError("Enter valid student number");
            return false;
        }

        if (studentID.length() != STUDENT_NUMBER_LENGTH){
            ShowUserError("Enter valid student number");
            return false;
        }

        return true;

    }

    boolean correctPassParams(String password){
        //Do some function
        return true; //DO NOT FORGET TO DELETE THIS LINE
    }

    boolean checkPassword(String password, String studentID) throws NoSuchAlgorithmException, InterruptedException {
        if (correctPassParams(password)){
            ShowUserError("Ensure you follow the password requirements");
            return false;
        }

        if (hasWhitespace(password)){
            ShowUserError("No whitespaces are allowed in password");
            return false;
        }

        String hashPass = saltAndHash(password);

        JSONArray arr = onlineDB.getJSONArr("SELECT * FROM STUDENT" +
                " WHERE Student_ID = " + studentID);

        int size = arr.length();

        if (size == 1){
            try {
                JSONObject obj = arr.getJSONObject(0);
                String hashPassFromDB = obj.getString("Password");

                if (hashPass == hashPassFromDB){
                    return true;
                }

            }catch (Exception e ){
                Log("For some weird reason, cannot get JSONObject");
                ShowUserError("Please ensure student number is correct and try again," +
                        " if the problem persists contact the support team");
            }
        }
        else if (size == 0){
            ShowUserError("Please enter a valid student number or register if you have not");
        }
        else if (size > 1){
            ShowUserError("There seems to be 2 accounts with the same student number," +
                    " please contact the support team");
        }

        return false;

    }

    String StringToMD5(String string) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(string.getBytes());
        BigInteger bigInteger = new BigInteger(1,messageDigest);
        String hashString = bigInteger.toString(16);
        return hashString;
    }

    String salt(String string){
        String temp = "";
        temp = START_SALT + string + END_SALT;
        return temp;
    }

    String saltAndHash(String string) throws NoSuchAlgorithmException {
        String temp = StringToMD5(salt(string));
        return temp;
    }

}
