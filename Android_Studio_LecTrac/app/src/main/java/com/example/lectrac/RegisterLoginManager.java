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

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorCompletionService;

import static com.example.lectrac.HelperFunctions.*;

public class RegisterLoginManager{


    //region Constants

    //endregion

    //region Initialization
    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    HelperFunctions hp = new HelperFunctions();
    static JSONObject userInWITS = null;
    static JSONObject userInLT = null;
    //endregion
    

    //region Register
    boolean RegisterAttempt(String userID, String firstName, String lastName, String email, String nick, String password,String confirmPass, Context context) throws NoSuchAlgorithmException, InterruptedException, JSONException, IOException {
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
        userInWITS = null;

        boolean found = foundObj(tblWITS, "User_ID = " + quote(userID));

        if (!found){
            ShowUserError("There is no WITS User ID with that user ID, please contact support");
            return false;
        }

        boolean isLec = isLecturerWITS(userID);

        userInWITS = onlineDB.getJSONObj("SELECT * FROM" +
                " " + tblWITS + " WHERE User_ID = " + quote(userID));


        //endregion


        //firstName sorted out
        if (firstName.length() != 0){
            firstName = firstName.trim();

            if (!correctFirstName(firstName)){
                return false;
            }
        }
        else{
            firstName = userInWITS.getString("FirstName");

        }

        //lastName sorted out
        if (lastName.length() != 0){
            lastName = lastName.trim();

            if (!correctSurname(lastName)){
                return false;
            }
        }
        else{
            lastName = userInWITS.getString("LastName");

        }

        //email sorted out
        if (email.length() != 0){
            email = email.trim();

            if (!correctEmail(email)){
                return false;
            }
        }
        else{
            email = userInWITS.getString("Email");
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

            if (!confirmPass.equals(password)){
                ShowUserError("Make sure your password and confirm password are matching");
                return false;
            }

            Log("Might give error about returning BINARY(16) AS A STRING - " +
                    "WHEN PASSWORD.LENGTH = 0 IN REGISTER ATTEMPT");


            String tempPassword = userInWITS.getString("Password");

            //Confirm pass cass checked

            password = saltAndHash(password);
            Log(password);

            //Non cut password (BINARY16)
            password = CopyOnly(password,passwordLength);
            Log(password);

            if (!tempPassword.equals(password)){
                ShowUserError("Enter your WITS password");
                return false;
            }
        }
        else{
            ShowUserError("Enter your WITS password");
            return false;
        }

        return Register(userID,firstName,lastName,email,nick,password,context);
    }

    boolean alreadyReg(String userID) {

        try {
            Log("alreadyReg");
            JSONArray studentLength = onlineDB.getJSONArr("SELECT * FROM" +
                    " STUDENT WHERE Student_ID = " + quote(userID));

            Log(studentLength.length() + " is student length");

            JSONArray lecturerLength = onlineDB.getJSONArr("SELECT * FROM" +
                    " LECTURER WHERE Lecturer_ID = " + quote(userID));

            Log(lecturerLength.length() + " is lecturer length");

            if (studentLength.length() != 0 || lecturerLength.length() != 0) {
                Log("Returning true for alreadyReg");
                return true;
            }
        }catch(Exception e){
            Log(e.toString());
            Log("Returning false for alreadyReg");
            return false;
        }
        return false;
    }

    boolean Register(@NotNull String userID, String firstName, String lastName, String email, String nick,@NotNull String password, Context context) throws InterruptedException, IOException, JSONException {
        Log("Register");
        Log("About to initialize local DB");

        LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        boolean isLec = isLecturerWITS(userID);

        String isLecString;

        if (isLec){
            isLecString = "1";
        }
        else{
            isLecString = "0";
        }


        //Into Online (LecTrac) Database
        String[] onlineValues = new String[6];
        onlineValues[0] = '"' + userID + '"';
        onlineValues[1] = '"' + firstName + '"';
        onlineValues[2] = '"' + lastName + '"';
        onlineValues[3] = '"' + email + '"';
        onlineValues[4] = '"' + nick + '"';
        onlineValues[5] = '"' + password + '"';

        //Into Local (On Device) Database
        String[] localValues = new String[5];
        localValues[0] = '"' + userID + '"';
        localValues[1] = "1";   //isLoggedIn
        localValues[2] = isLecString;
        localValues[3] = "0"; //DarkMode set to false
        localValues[4] = '"' + nick + '"';

        try{
            if (isLec){
                Log("Inserting into Lecturer Table");
                onlineDB.Insert(tblLecturer,onlineValues);
            }
            else{

                Log("Inserting into Student Table");
                onlineDB.Insert(tblStudent,onlineValues);
            }

            try{
                localDB.doInsert(tblUser,localValues);
            }catch (Exception e){
                Log(e.toString());
                Log("Inserting into local DB failed");
            }


            return true;
        }catch (Exception e){
            Log(e.toString());
            Log("OnlineDB insert failed");
            ShowUserError("Oops, please contact support");
            return false;
        }

    }

    boolean correctPassParams(@NotNull String password){
        Log("Correct Pass Params");
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
        Log("Correct UserID");
        if (userID.length() != 7){
            ShowUserError("Enter a valid user ID");
            return false;
        }

        if (hasWhitespace(userID)){
            ShowUserError("Cannot have a space in User ID");
            return false;
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
            return false;
        }

        return true;

    }

    boolean foundObj(String tableName, String condition) throws InterruptedException, IOException, JSONException {
        Log("foundObj");
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
        Log("correctEmail");
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
        Log("correctSurname");
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
        Log("correctFirstName");
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

    boolean isLecturerWITS(String userID) throws InterruptedException, IOException, JSONException {
        Log("isLecturerWITS");
        JSONArray tempArr = onlineDB.getJSONArr("SELECT * FROM " + tblWITS +
                " WHERE User_ID = " + quote(userID));

        int size = tempArr.length();

        if (size == 1){
            String isLec = "";

            try{
                isLec = tempArr.getJSONObject(0).getString("Classification");
                Log(isLec + " this is IS LEC");
                if ("LECTURER".equals(isLec)){
                    Log("returning true for isLec from WITS");
                    return true;
                }

                Log("returning false for isLec from WITS");
                return false;
            }catch (Exception e){
                Log("Cannot get classification fromm WITS table, classification column");
                ShowUserError("Problem getting data, contact support");
            }
        }

        if (size == 0){
            ShowUserError("Enter a valid user ID");
            Log("isLecturer arr is empty");
            Log("isLecturerWITS BIG ERROR");
        }

        if (size > 1){
            ShowUserError("Contact support and give them " + errorCodeMoreThanOne);
        }

        return false;
    }

    //endregion

    //region LogIn
    boolean LogInAttempt(String password, String userID, Context context) throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        userInLT = null;

        Log("LogInAttempt");
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

    boolean LogIn(String password, String userID, Context context) throws JSONException, InterruptedException, IOException {
        Log("LogIn");
        LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        int isLecturer;
        String nick = "";

        if (isLecturer(userID)){
            isLecturer = 1;
            nick = userInLT.getString("Lecturer_Reference");
        }
        else{
            isLecturer = 0;
            nick = userInLT.getString("Student_Nickname");
        }






        String[] values = new String[tblUserLength];

        //Values = {User_ID (String), isLoggedIn (Boolean),
        // isLecturer (Boolean), isDarkMode (Boolean), Nickname (String) };


        values[0] = '"' + userID + '"';
        values[1] = "1";
        values[2] = Integer.toString(isLecturer);
        values[3] = "0";
        values[4] = '"' + nick + '"';


        localDB.doInsert(tblUser, values);

        return true;
    }

    boolean isLecturer(String userID) throws InterruptedException, IOException, JSONException {
        Log("isLecturer");
        boolean isLecturer;

        JSONArray tempArr = onlineDB.getJSONArr("SELECT * FROM " + tblStudent + " WHERE STUDENT_ID = " + quote(userID));

        int size = tempArr.length();
        if (size == 1){
            return false;
        }
        else if (size == 0){
            Log("No matching studentID, might be lecturer");

            tempArr = onlineDB.getJSONArr("SELECT * FROM " + tblLecturer +
                    " WHERE Lecturer_ID = " + quote(userID));

            int tempSize = tempArr.length();

            if (tempSize == 1){
                return true;
            }else if (tempSize == 0){
                Log("No lecturer or student match, please register");
                ShowUserError("No matching user with this user ID");
            }
            else if (tempSize > 1){
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
        Log("checkStudentID");
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

    boolean checkPassword(String password, String userID) throws NoSuchAlgorithmException, InterruptedException, IOException, JSONException {
        Log("checkPassword");
        if (hasWhitespace(password)){
            ShowUserError("No whitespaces are allowed in password");
            return false;
        }

        String hashPass = saltAndHash(password);

        JSONArray arr = onlineDB.getJSONArr("SELECT * FROM STUDENT" +
                " WHERE Student_ID = " + quote(userID));



        int size = arr.length();

        if (size == 1){
            try {
                JSONObject obj = arr.getJSONObject(0);
                userInLT = obj;

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
                    " WHERE Lecturer_ID = " + quote(userID));

            int lecArrSize = lecArr.length();
            if (lecArrSize == 0){
                Log("No Student or Lecturer with ID, please register");
                ShowUserError("There is no matching user with this userID");
            }

            if (lecArrSize == 1){
                try {
                    JSONObject obj = arr.getJSONObject(0);
                    userInLT = obj;

                    String hashPassFromDB = obj.getString("Lecturer_Password");

                    if (hashPass.equals(hashPassFromDB)){
                        return true;
                    }

                }catch (Exception e ){
                    Log("For some weird reason, cannot get JSONObject");
                    ShowUserError("Please ensure lecturer id is correct and try again," +
                            " if the problem persists contact the support team");
                }
            }

            if (lecArrSize > 1){
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
