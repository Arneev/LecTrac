//OPTIMIZE CODE AND MAKE SURE USING CONSTANTS
//USE PARAMS FOR SQLITE QUERIES

package com.example.lectrac;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorCompletionService;

public class RegisterLoginManager{


    //region Constants
    final static String START_SALT = "!@We#4";
    final static String  END_SALT = "HQWn98";
    final static int STUDENT_NUMBER_LENGTH = 7;
    final static String tblStudent = "STUDENT";
    final static String tblLecturer = "LECTURER";
    final static int tblUserLength = 5;
    final static String tblUser = "USER";
    final static String tblWITS = "WITS";
    final static String errorCodeMoreThanOne = "Error Code 01";
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

    boolean hasWhitespace(String line){
        int size = line.length();

        for (int i = 0; i < size; i++){
            if (line.charAt(i) == ' ') return true;
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
    //endregion

    //region Register
    boolean RegisterAttempt(String userID, String firstName, String lastName, String email, String nick, String password,String confirmPass) throws NoSuchAlgorithmException, InterruptedException, JSONException {
        //userID sorted out
        if (userID.length() != 0){
            userID = userID.trim();

            if (!correctUserID(userID)){
                return false;
            }

            if (alreadyReg(userID)){
                ShowUserError("You already registered before");
                return false;
            }
        }

        //region Getting JSON Object
        JSONObject obj = null;

        boolean found = foundObj(tblWITS, "User_ID = " + '"' + userID + '"');

        if (!found){
            ShowUserError("There is no WITS User ID with that user ID, please contact support");
            return false;
        }

        boolean isLec = isLecturerWITS(userID);

        obj = onlineDB.getJSONObj("SELECT * FROM" +
                    " " + tblWITS + " WHERE User_ID" + '"' + userID + '"');


        //endregion

        //firstName sorted out
        if (firstName.length() != 0){
            firstName = firstName.trim();

            if (!correctFirstName(firstName)){
                return false;
            }
        }
        else{
            firstName = obj.getString("FirstName");

        }

        //lastName sorted out
        if (lastName.length() != 0){
            lastName = lastName.trim();

            if (!correctSurname(lastName)){
                return false;
            }
        }
        else{
            lastName = obj.getString("LastName");

        }

        //email sorted out
        if (email.length() != 0){
            email = email.trim();

            if (!correctEmail(email)){
                return false;
            }
        }
        else{

            if (isLec){
                email = obj.getString("Email");
            }

            Log("Using WITS Email");
        }

        //nick sorted out
        if (nick.length() != 0){
            nick = nick.trim();
        }
        else {
            nick = firstName + " " + lastName;
        }

        //password sorted out (salt and hashed :D )
        if (password.length() != 0){
            password = password.trim();

            if (!correctPassParams(password)) {
                return false;
            }

            if (confirmPass.length() == 0){
                ShowUserError("Please confirm your password");
                return false;
            }

            if (confirmPass != password){
                ShowUserError("Make sure your password and confirm password are matching");
                return false;
            }

            //Confirm pass cass checked

            password = saltAndHash(password);
        }
        else{
            if (confirmPass.length() != 0){
                ShowUserError("Remove all text from Confirm Password");
                return false;
            }

            Log("Might give error about returning BINARY(16) AS A STRING - " +
                    "WHEN PASSWORD.LENGTH = 0 IN REGISTER ATTEMPT");


            password = obj.getString("Password");

        }

        return Register(userID,firstName,lastName,email,nick,password);
    }

    boolean alreadyReg(String userID) throws InterruptedException {

        if (onlineDB.getJSONArr("SELECT * FROM" +
                " STUDENT WHERE Student_ID = " + "'" + userID + "'").length() != 0 ||
                onlineDB.getJSONArr("SELECT * FROM" +
                 " LECTURER WHERE Lecturer_ID = " + "'" + userID + "'").length() != 0){
            return true;
        }

        return false;
    }

    boolean Register(@NotNull String userID, String firstName, String lastName, String email, String nick,@NotNull String password) throws InterruptedException {
        boolean isLec = isLecturer(userID);

        String[] values = new String[6];
        values[0] = '"' + userID + '"';
        values[1] = '"' + firstName + '"';
        values[2] = '"' + lastName + '"';
        values[3] = '"' + email + '"';
        values[4] = '"' + nick + '"';
        values[5] = password;

        try{
            if (isLec){
                onlineDB.Insert(tblLecturer,values);
            }
            else{
                onlineDB.Insert(tblStudent,values);
            }

            return true;
        }catch (Exception e){
            ShowUserError("Oops, please contact support");
            return false;
        }

    }

    boolean correctPassParams(@NotNull String password){

        boolean containSpecial = false;
        boolean isDigit = false;
        boolean isUpper = false;
        boolean isLower = false;
        boolean hasWhiteSpace = false;

        for (char c : password.toCharArray()){
            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^'
                    || c == '&' || c == '*'){
                containSpecial = true;
            }
            if (Character.isDigit(c)){
                isDigit = true;
            }

            if (Character.isUpperCase(c)){
                isUpper = true;
            }

            if (Character.isLowerCase(c)){
                isLower = true;
            }

            if (c == ' '){
                hasWhiteSpace = true;
            }
        }

        if (!containSpecial){
            ShowUserError("Make sure there is at least one special character");
            return false;
        }

        if (!isDigit){
            ShowUserError("Make sure there is at least one number");
            return false;
        }

        if (!isLower){
            ShowUserError("Make sure there is at least one lower case character");
            return false;
        }

        if (!isUpper) {
            ShowUserError("Make sure there is at least one upper case character");
            return false;
        }

        if (hasWhiteSpace){
            ShowUserError("Cannot have whitespace in password");
            return false;
        }

        if (password.length() < 8){
            ShowUserError("Make sure your password is at least 8 characters long");
            return false;
        }

        return true;
    }

    boolean RuntimeCorrectUserID(@NotNull String userID){
        if (userID.length() != 7){
            ShowUserError("Enter a valid user ID");
            return false;
        }
        return true;
    }

    boolean correctUserID(String userID) throws InterruptedException {
        if (userID.length() != 7){
            ShowUserError("Enter a valid user ID");
            return false;
        }

        if (hasWhitespace(userID)){
            ShowUserError("Cannot have a space in User ID");
        }

        try
        {
            int iUserID = Integer.parseInt(userID);

            if (iUserID < 0 || iUserID > 9999999){
                ShowUserError("Enter a valid user ID");
                return false;
            }

        }catch(Exception e){
            ShowUserError("Enter a valid user ID");
        }

        boolean isCorrect = foundObj(tblWITS,"User_ID = " + "'" + userID + "'");

        if (isCorrect){
            return true;
        }

        ShowUserError("Enter a valid user ID");
        return false;

    }

    boolean foundObj(String tableName, String condition) throws InterruptedException {
        String query = "SELECT * FROM " + tableName + " WHERE " + condition;
        JSONArray arr = onlineDB.getJSONArr(query);

        int size = arr.length();

        if (size == 1){
            return true;
        }
        else if (size == 0){
            Log("Condition not found from query, query : " + query);
            return false;
        }
        else if (size > 1){
            Log("There is more than one row");
            return false;
        }

        Log("Apparently arr size is less than 0");
        return false;


    }

    boolean correctEmail(String email){
        int size = email.length();

        if (size > 128){
            ShowUserError("Use another email address that is shorter");
            return false;
        }

        if (hasWhitespace(email)){
            ShowUserError("email address cannot have a space");
            return false;
        }

        boolean isCorrect = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (isCorrect){
            return true;
        }

        ShowUserError("Enter a valid email address");
        return false;
    }

    boolean correctSurname(String surname){

        for(char c : surname.toCharArray()) {
            if(Character.isDigit(c)) {
                ShowUserError("There cannot be a number in your surname...");
                return false;
            }

            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^'
                    || c == '&' || c == '*' || c == '(' || c == ')') {
                ShowUserError("Cannot have special characters");
                return false;
            }
        }

        return true;
    }

    boolean correctFirstName(@NotNull String firstName){

        for(char c : firstName.toCharArray()) {
            if(Character.isDigit(c)) {
                ShowUserError("There cannot be a number in your name...");
                return false;
            }

            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^'
                    || c == '&' || c == '*' || c == '(' || c == ')') {
                ShowUserError("Cannot have special characters");
                return false;
            }

            if (c == ' '){
                ShowUserError("Cannot have a space in your first name");
                return false;
            }
        }

        return true;
    }

    boolean isLecturerWITS(String userID) throws InterruptedException {

        JSONArray tempArr = onlineDB.getJSONArr("SELECT * FROM " + tblWITS +
                " WHERE User_ID = " + "'" + userID + "'");

        int size = tempArr.length();

        if (size == 1){
            String isLec;

            try{
                isLec = tempArr.getJSONObject(0).getString("Classification");
                if (isLec == "LECTURER"){
                    return true;
                }
                return false;
            }catch (Exception e){
                Log("Cannot get classification fromm WITS table, classification column");
                ShowUserError("Problem getting data, contact support");
            }
        }

        if (size == 0){
            ShowUserError("Enter a valid user ID");
            Log("isLecturer arr is empty");
        }

        if (size > 1){
            ShowUserError("Contact support and give them " + errorCodeMoreThanOne);
        }

        return false;
    }

    //endregion

    //region LogIn
    boolean LogInAttempt(String password, String userID, Context context) throws InterruptedException, NoSuchAlgorithmException, JSONException {
        password = password.trim();
        userID = userID.trim();

        if (checkPassword(password,userID) && checkStudentID(userID)){
            return LogIn(password,userID,context);
        }else{
            //Error messages from checkStudentID and
            // checkPassword will show no need to worry

            Log("Login Failed");
            return false;
        }
    }

    boolean LogIn(String password, String userID, Context context) throws JSONException, InterruptedException {
        LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        int isLecturer;

        if (isLecturer(userID)){
            isLecturer = 1;
        }
        else{
            isLecturer = 0;
        }

        String[] values = new String[tblUserLength];

        //Values = {User_ID (String), isLoggedIn (Boolean),
        // isLecturer (Boolean), isDarkMode (Boolean), Nickname (String) };


        values[0] = '"' + userID + '"';
        values[1] = "1";
        values[2] = Integer.toString(isLecturer);
        values[3] = "0";
        values[4] = "NULL";


        localDB.doInsert(tblUser, values);

        return true;
    }

    boolean isLecturer(String userID) throws InterruptedException {
        boolean isLecturer;

        JSONArray tempArr = onlineDB.getJSONArr("SELECT * FROM " + tblStudent + " WHERE STUDENT_ID = " + "'" + userID + "'");

        int size = tempArr.length();
        if (size == 1){
            return false;
        }
        else if (size == 0){
            Log("No matching studentID, might be lecturer");

            tempArr = onlineDB.getJSONArr("SELECT * FROM " + tblLecturer +
                    " WHERE Lecturer_ID = " + userID);

            if (size == 1){
                return true;
            }else if (size == 0){
                Log("No lecturer or student match, please register");
                ShowUserError("No matching user with this user ID");
            }
            else if (size > 1){
                ShowUserError("There seems to be 2 accounts with the same user ID," +
                        " please contact the support team");
            }
        }
        else if (size > 1){
            ShowUserError("There seems to be 2 accounts with the same user ID," +
                    " please contact the support team");
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

        // TEST CASE WHEN USER IS NOT IN DATABASE IS TAKEN CARE OF IN checkPassword();

        return true;

    }

    boolean checkPassword(String password, String userID) throws NoSuchAlgorithmException, InterruptedException {
        if (hasWhitespace(password)){
            ShowUserError("No whitespaces are allowed in password");
            return false;
        }

        String hashPass = saltAndHash(password);

        JSONArray arr = onlineDB.getJSONArr("SELECT * FROM STUDENT" +
                " WHERE Student_ID = " + "'" + userID + "'");



        int size = arr.length();

        if (size == 1){
            try {
                JSONObject obj = arr.getJSONObject(0);

                String hashPassFromDB = obj.getString("Student_Password");

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
            Log("No matching student ID, trying lecturer ID");

            //region Might be Lecturer
            JSONArray lecArr = onlineDB.getJSONArr("SELECT * FROM LECTURER" +
                    " WHERE Lecturer_ID = " + "'" + userID + "'");

            int lecArrSize = lecArr.length();
            if (size == 0){
                Log("No Student or Lecturer with ID, please register");
                ShowUserError("There is no matching user with this userID");
            }

            if (size == 1){
                try {
                    JSONObject obj = arr.getJSONObject(0);
                    String hashPassFromDB = obj.getString("Lecturer_Password");

                    if (hashPass == hashPassFromDB){
                        return true;
                    }

                }catch (Exception e ){
                    Log("For some weird reason, cannot get JSONObject");
                    ShowUserError("Please ensure lecturer id is correct and try again," +
                            " if the problem persists contact the support team");
                }
            }

            if (size > 1){
                ShowUserError("There seems to be 2 accounts with the same user IDs," +
                        " please contact the support team");
            }
            //endregion
        }
        else if (size > 1){
            ShowUserError("There seems to be 2 accounts with the same student number," +
                    " please contact the support team");
        }

        return false;

    }
    //endregion

}
