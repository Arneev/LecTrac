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

    static ErrorClass ec;
    //region Constants

    //endregion

    //region Initialization
    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    HelperFunctions hp = new HelperFunctions();
    static JSONObject userInWITS = null;
    static JSONObject userInLT = null;
    static LocalDatabaseManager localDB = null;
    static Context context;
    //endregion
    

    //region Register
    boolean RegisterAttempt(String userID, String firstName, String lastName, String email, String nick, String password,String confirmPass, Context ct) throws NoSuchAlgorithmException, InterruptedException, JSONException, IOException {
        //userID sorted out
        context = ct;
        ec = new ErrorClass(ct);
        if (userID.length() != 0){
            userID = userID.trim();

            if (!correctUserID(userID)){
                return false;
            }

            if (alreadyReg(userID)){
                ec.ShowUserError("You already registered before",context);
                return false;
            }
        }

        //region Getting JSON Object
        userInWITS = null;

        boolean found = foundObj(quote(userID));

        if (!found){
            ec.ShowUserError("There is no WITS User ID with that user ID, please contact support",context);
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
                ec.ShowUserError("Please confirm your password",context);
                return false;
            }

            if (!confirmPass.equals(password)){
                ec.ShowUserError("Make sure your password and confirm password are matching",context);
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
                ec.ShowUserError("Enter your WITS password",context);
                return false;
            }
        }
        else{
            ec.ShowUserError("Enter your WITS password",context);
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

    boolean Register(@NotNull String userID, String firstName, String lastName, String email, String nick, @NotNull String password, final Context context) throws InterruptedException, IOException, JSONException {
        Log("Register");
        Log("About to initialize local DB");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                localDB = new LocalDatabaseManager(context);
            }
        });

        t.start();
        t.join();

        Log("Finished Initialize");

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
            ec.ShowUserError("Oops, please contact support",context);
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
            ec.ShowUserError("Make sure there is at least one special character",context);
            return false;
        }

        if (!isDigit){
            ec.ShowUserError("Make sure there is at least one number",context);
            return false;
        }

        if (!isLower){
            ec.ShowUserError("Make sure there is at least one lower case character",context);
            return false;
        }

        if (!isUpper) {
            ec.ShowUserError("Make sure there is at least one upper case character",context);
            return false;
        }

        if (hasWhiteSpace){
            ec.ShowUserError("Cannot have whitespace in password",context);
            return false;
        }

        if (password.length() < 8){
            ec.ShowUserError("Make sure your password is at least 8 characters long",context);
            return false;
        }

        return true;
    }

    boolean RuntimeCorrectUserID(@NotNull String userID){
        if (userID.length() != 7){
            ec.ShowUserError("Enter a valid user ID",context);
            return false;
        }
        return true;
    }

    boolean correctUserID(String userID) throws InterruptedException {
        Log("Correct UserID");
        if (userID.length() != 7){
            ec.ShowUserError("Enter a valid user ID",context);
            return false;
        }

        if (hasWhitespace(userID)){
            ec.ShowUserError("Cannot have a space in User ID",context);
            return false;
        }

        try
        {
            int iUserID = Integer.parseInt(userID);

            if (iUserID < 0 || iUserID > 9999999){
                ec.ShowUserError("Enter a valid user ID",context);
                return false;
            }

        }catch(Exception e){
            ec.ShowUserError("Enter a valid user ID",context);
            return false;
        }

        return true;

    }

    boolean foundObj(String userID) throws InterruptedException, IOException, JSONException {
        Log("foundObj");
        String query = "SELECT * FROM WITS WHERE User_ID = " + quote(userID);
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
            ec.ShowUserError("Use another email address that is shorter",context);
            return false;
        }

        if (hasWhitespace(email)){
            ec.ShowUserError("email address cannot have a space",context);
            return false;
        }

        boolean isCorrect = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (isCorrect){
            return true;
        }

        ec.ShowUserError("Enter a valid email address",context);
        return false;
    }

    boolean correctSurname(String surname){
        Log("correctSurname");
        for(char c : surname.toCharArray()) {
            if(Character.isDigit(c)) {
                ec.ShowUserError("There cannot be a number in your surname...",context);
                return false;
            }

            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^'
                    || c == '&' || c == '*' || c == '(' || c == ')') {
                ec.ShowUserError("Cannot have special characters",context);
                return false;
            }
        }

        return true;
    }

    boolean correctFirstName(@NotNull String firstName){
        Log("correctFirstName");
        for(char c : firstName.toCharArray()) {
            if(Character.isDigit(c)) {
                ec.ShowUserError("There cannot be a number in your name...",context);
                return false;
            }

            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^'
                    || c == '&' || c == '*' || c == '(' || c == ')') {
                ec.ShowUserError("Cannot have special characters",context);
                return false;
            }

            if (c == ' '){
                ec.ShowUserError("Cannot have a space in your first name",context);
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
                ec.ShowUserError("Problem getting data, contact support",context);
            }
        }

        if (size == 0){
            ec.ShowUserError("Enter a valid user ID",context);
            Log("isLecturer arr is empty");
            Log("isLecturerWITS BIG ERROR");
        }

        if (size > 1){
            ec.ShowUserError("Contact support and give them " + errorCodeMoreThanOne,context);
        }

        return false;
    }

    //endregion

    //region LogIn
    boolean LogInAttempt(String password, String userID, Context ct) throws InterruptedException, NoSuchAlgorithmException, JSONException, IOException {
        context = ct;
        ec = new ErrorClass(ct);
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
        localDB = new LocalDatabaseManager(context);

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


        for(int i = 0; i < values.length; i++){
            Log(values[i]);
        }

        localDB.doInsert(tblUser, values);
        Log("We supposed to insert into localDB here");

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
                ec.ShowUserError("No matching user with this user ID",context);
            }
            else if (tempSize > 1){
                ec.ShowUserError("There seems to be 2 accounts with the same user ID," +
                        " please contact the support team",context);
            }
        }
        else if (size > 1){
            ec.ShowUserError("There seems to be 2 accounts with the same user ID," +
                    " please contact the support team",context);
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
                ec.ShowUserError("Enter valid student number",context);
                return false;
            }
        }catch (Exception e){
            ec.ShowUserError("Enter valid student number",context);
            return false;
        }

        if (studentID.length() != STUDENT_NUMBER_LENGTH){
            ec.ShowUserError("Enter valid student number",context);
            return false;
        }

        // TEST CASE WHEN USER IS NOT IN DATABASE IS TAKEN CARE OF IN checkPassword();

        return true;

    }

    boolean checkPassword(String password, String userID) throws NoSuchAlgorithmException, InterruptedException, IOException, JSONException {
        Log("checkPassword");
        if (hasWhitespace(password)){
            ec.ShowUserError("No whitespaces are allowed in password",context);
            return false;
        }

        String hashPass = CopyOnly(saltAndHash(password),16);

        JSONArray arr = onlineDB.getJSONArr("SELECT * FROM STUDENT" +
                " WHERE Student_ID = " + quote(userID));



        int size = arr.length();

        if (size == 1){
            try {
                JSONObject obj = arr.getJSONObject(0);
                userInLT = obj;

                String hashPassFromDB = obj.getString("Student_Password");

                if (hashPass.equals(hashPassFromDB)){
                    return true;
                }
                else{
                    ec.ShowUserError("Incorrect password, try again or click on forgot password",context);
                }


            }catch (Exception e ){
                Log("For some weird reason, cannot get JSONObject");
                ec.ShowUserError("Please ensure student number is correct and try again," +
                        " if the problem persists contact the support team",context);
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
                ec.ShowUserError("There is no matching user with this userID",context);
            }

            if (lecArrSize == 1){
                try {
                    JSONObject obj = lecArr.getJSONObject(0);
                    userInLT = obj;

                    String hashPassFromDB = obj.getString("Lecturer_Password");
                    Log(hashPassFromDB);

                    if (hashPass.equals(hashPassFromDB)){
                        return true;
                    }
                    else{
                        ec.ShowUserError("Incorrect password, try again or click on forgot password",context);
                    }


                }catch (Exception e ){
                    Log("For some weird reason, cannot get JSONObject");
                    ec.ShowUserError("Please ensure lecturer id is correct and try again," +
                            " if the problem persists contact the support team",context);
                }
            }

            if (lecArrSize > 1){
                ec.ShowUserError("There seems to be 2 accounts with the same user IDs," +
                        " please contact the support team",context);
            }
            //endregion
        }
        else if (size > 1){
            ec.ShowUserError("There seems to be 2 accounts with the same student number," +
                    " please contact the support team",context);
        }

        return false;

    }
    //endregion


}
