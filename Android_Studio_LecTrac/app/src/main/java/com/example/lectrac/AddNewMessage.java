package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class AddNewMessage extends AppCompatActivity {

    static LocalDatabaseManager localDB;
    static OnlineDatabaseManager onlineDB;

    static EditText edtHeading;
    static Spinner spinClass;
    static EditText edtContent;
    static Spinner spinCourse;
    public static Button btnAddMessage;

    static String[] courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_message);


        edtHeading = findViewById(R.id.edtAddMessageName);
        spinClass = findViewById(R.id.spinAddMessageClass);
        edtContent = findViewById(R.id.edtAddMessageContent);
        spinCourse = findViewById(R.id.spinAddMessageCourse);
        btnAddMessage = findViewById(R.id.btnMessageAdd);

        localDB = new LocalDatabaseManager(this);
        onlineDB = new OnlineDatabaseManager();
        SetCourseSpinnerItems();
        SetClassificSpinnerItems();


    }

    public void AddMessageButtonClick(View v){
        if (!isOnline(this)){
            ShowUserError("Please connect to the internet",this);
            return;
        }

        String heading = edtHeading.getText().toString();
        String classific = spinClass.getSelectedItem().toString();
        String content = edtContent.getText().toString();
        String course = spinCourse.getSelectedItem().toString();
        String date = getCurrDate();
        String lecID = localDB.getUserID(localDB);
        String isDeleted = "0";


        if (!CheckHeading(heading)){
            ShowUserError("Please enter in a heading name",this);
            return;
        }

        String[] cols = new String[7];
        cols[0] = "Message_Name";
        cols[1] = "Message_Classification";
        cols[2] = "Message_Content";
        cols[3] = "Course_Code";
        cols[4] = "Message_Date_Posted";
        cols[5] = "Message_isDeleted";
        cols[6] = "Lecturer_ID";

        String[] values = new String[7];
        values[0] = doubleQuote(heading);
        values[1] = doubleQuote(classific);
        values[2] = doubleQuote(content);
        values[3] = doubleQuote(course);
        values[4] = doubleQuote(date);
        values[5] = isDeleted;
        values[6] = doubleQuote(lecID);


        try {
            String[] onlineCols = new String[6];
            onlineCols[0] = "Message_Name";
            onlineCols[1] = "Message_Classification";
            onlineCols[2] = "Message_Contents";
            onlineCols[3] = "Course_Code";
            onlineCols[4] = "Message_Date_Posted";
            onlineCols[5] = "Lecturer_ID";

            String[] onlineVals = new String[6];
            onlineVals[0] = doubleQuote(heading);
            onlineVals[1] = doubleQuote(classific);
            onlineVals[2] = doubleQuote(content);
            onlineVals[3] = doubleQuote(course);
            onlineVals[4] = doubleQuote(date);
            onlineVals[5] = doubleQuote(lecID);


            onlineDB.Insert(tblMessage,onlineCols,onlineVals);
        }catch (Exception e){
            Log(e.toString());
            ShowUserError("Failed to update message online, check you internet connection",this);
            return;
        }

        localDB.doInsert(tblMessage,cols,values);

        Log("Finished add message to dbs");

        startActivity(new Intent(this, MessageBoardActivity.class));
    }

    public boolean CheckHeading(String heading){
        if (heading.equals(null) || heading.length() == 0){
            return false;
        }

        return true;
    }


    //region Spinner Setter

    public void SetClassificSpinnerItems(){
        List<String> list = new ArrayList<String>();

        list.add("All");
        list.add("Homework");
        list.add("Announcement");
        list.add("Test");
        list.add("Practice");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinClass.setAdapter(dataAdapter);
    }

    public void SetCourseSpinnerItems(){
        courses = localDB.getCourses(localDB);
        int courseSize = courses.length;

        List<String> list = new ArrayList<String>();

        for(int i = 0; i < courseSize; i++){
            list.add(courses[i]);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCourse.setAdapter(dataAdapter);
    }

    //endregion

    //region Helper Function

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, MessageBoardActivity.class));
    }

    //endregion
}
