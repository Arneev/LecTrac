package com.example.lectrac;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.drawable.DrawableCompat;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static androidx.core.content.ContextCompat.getSystemService;

public class HelperFunctions {

    //region Constants
    public final static String START_SALT = "!@We#4";
    public final static String  END_SALT = "HQWn98";
    final static int STUDENT_NUMBER_LENGTH = 7;
    final static int tblUserLength = 5;
    final static int passwordLength = 16;
    final static SimpleDateFormat ddMMMyyyy = new SimpleDateFormat("dd-MMM-yyyy");
    final static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
    final static String myPrefName = "myPrefs";
    final static String persoTag = "Perso";
    final static String calTag = "Calendar";

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
    final static String tblRegistered = "REGISTERED";
    //endregion

    //region Error Codes
    //More than one user with the same User ID
    final static String errorCodeMoreThanOne = "Error Code 01";
    final static String errorProblemSync = "Error Code 02";
    final static String errorLecNoCourse = "Error Code 03";
    final static String showCheckInternetConnection = "Please check your internet connection";
    final static String showNotConnected = "You are not connected to the internet";


    //endregion

//================================================================================================//

    public static int getIconCol(Context context){
        if (isDarkMode(context)){
            return Color.WHITE;
        }
        return Color.BLACK;
    }

    public static void Log(String error){
        Log.i("Perso",error);
    }

    public static void LogCal(String error){
        Log.i("Calendar",error);
    }

    public static void LocalLog(String error){
        Log.i("LocalDB",error);
    }

    public static boolean hasWhitespace(String line){
        int size = line.length();

        for (int i = 0; i < size; i++){
            if (line.charAt(i) == ' ') return true;
        }

        return false;
    }

    public static String unquote(String string){
        String temp = "";
        int size = string.length();

        String stringSingleQuote = "'";
        char singleQuote = stringSingleQuote.charAt(0);

        for (int i = 0; i < size; i++){

            if ((string.charAt(i) == '"') || (string.charAt(i) == singleQuote) && (i == 0 || i == size -1)){

            }else {
                temp += string.charAt(i);
            }

        }

        return temp;
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

    public static String orderByDateAndTime(String query){
        query = "SELECT * FROM ( " + query + " ) ORDER BY Task_Due_Date DESC, Task_Due_Time ASC";
        return query;
    }

    public static String getCurrDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    public static String getCurrDate(SimpleDateFormat sdf){
        Date c = Calendar.getInstance().getTime();
        String formattedDate = sdf.format(c);
        return formattedDate;
    }

    public static String addDay(String oldDate, int numberOfDays, SimpleDateFormat dateFormat) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormat.parse(oldDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DAY_OF_YEAR,numberOfDays);
        dateFormat=new SimpleDateFormat("MM-dd-YYYY");
        Date newDate=new Date(c.getTimeInMillis());
        String resultDate=dateFormat.format(newDate);
        return resultDate;
    }

    public static boolean isDarkMode(Context context){
        SharedPreferences pref = context.getSharedPreferences(myPrefName,Context.MODE_PRIVATE);
        boolean isDark = pref.getBoolean("isDarkMode",false);
        return isDark;
    }

    public static void setNightMode(Context context){
        boolean isDark = isDarkMode(context);

        if (isDark){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void printArrString(ArrayList<String> arr, String tag){
        if (arr == null){
            LogOption(tag,"array is null");
            Log("WHOOOPSIE DOOOOO");
            return;
        }
        int size = arr.size();

        for (int i = 0; i < size; i++){
            if (arr.get(i) != null){
                LogOption(tag,arr.get(i));
            }
            else{
                LogOption(tag,"one bit is null, at index " + Integer.toString(i));
            }
        }

    }

    public static void printArrInt(ArrayList<Integer> arr, String tag){
        if (arr == null){
            LogOption(tag,"array is null");
            return;
        }
        int size = arr.size();

        for (int i = 0; i < size; i++){
            if (Integer.toString(arr.get(i)) != null){
                LogOption(tag,Integer.toString(arr.get(i)));
            }
            else{
                LogOption(tag,"one bit is null, at index " + Integer.toString(i));
            }
        }

    }

    public static void LogOption(String tag, String message){
        if (tag.equals(persoTag)){
            Log(message);
        }
        else if (tag.equals(calTag)){
            LogCal(message);
        }
    }

    public static String completeUnquote(String string, int iterations){

        for (int i = 0; i < iterations; i++){
            string = unquote(string);
        }

        return string;
    }


}
