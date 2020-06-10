package com.example.lectrac;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static androidx.core.content.ContextCompat.getSystemService;

public class HelperFunctions {

    //region Constants
    public final static String START_SALT = "!@We#4";
    public final static String  END_SALT = "HQWn98";
    final static int STUDENT_NUMBER_LENGTH = 7;
    final static int tblUserLength = 5;
    final static int passwordLength = 16;
    //endregion

    //region Tables
    final static String tblStudent = "STUDENT";
    final static String tblLecturer = "LECTURER";
    final static String tblUser = "USER";
    final static String tblWITS = "WITS";
    final static String tblMessage = "MESSAGE";
    final static String tblUserTask = "USER_TASK";
    final static String tblLocalLecTask = "LECTURER_TASK";
    final static String tblCourse = "COURSE";
    final static String tblTest = "TEST";
    final static String tblTask = "TASK";
    //endregion

    //region Error Codes
    //More than one user with the same User ID
    final static String errorCodeMoreThanOne = "Error Code 01";
    final static String errorProblemSync = "Error Code 02";
    final static String errorLecNoCourse = "Error Code 03";


    //endregion

//================================================================================================//

    public static void Log(String error){
        Log.i("Perso",error);
    }

    public static void LocalLog(String error){
        Log.i("LocalDB",error);
    }

    public static void ShowUserError(String error){
        //Show Error, for now lets just Log
        Log(error);
        //Change this in the future
    }

    public static boolean hasWhitespace(String line){
        int size = line.length();

        for (int i = 0; i < size; i++){
            if (line.charAt(i) == ' ') return true;
        }

        return false;
    }

    public static String StringToMD5(String string) throws NoSuchAlgorithmException {
        String md5 = "MD5";

        try {
            MessageDigest md = MessageDigest.getInstance(md5);
            byte[] messageDigest = md.digest(string.getBytes());
            BigInteger bigInteger = new BigInteger(1,messageDigest);
            String hashString = bigInteger.toString(16);
            return hashString;
        }catch (NoSuchAlgorithmException e){
            Log("There is no algorithm named " + md5);
            throw e;
        }
    }

    public static String salt(String string){
        String temp = "";
        temp = START_SALT + string + END_SALT;
        return temp;
    }

    public static String saltAndHash(String string) throws NoSuchAlgorithmException {
        String temp = StringToMD5(salt(string));
        return temp;
    }

    public static String CopyOnly(String string, int lengthToCopy){
        int stringLength = string.length();

        if (stringLength <= lengthToCopy){
            Log("String longer than length to copy");
            return string;
        }

        String temp = "";

        for (int i = 0; i < lengthToCopy; i++){
            temp += string.charAt(i);
        }

        return temp;
    }

    public static String quote(String string){
        return "'" + string + "'";
    }

    public static String doubleQuote(String string){ return '"' + string + '"'; }

    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}
