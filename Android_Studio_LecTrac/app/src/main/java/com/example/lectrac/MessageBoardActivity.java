package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import static com.example.lectrac.HelperFunctions.Log;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.example.lectrac.AddNewMessage.btnAddMessage;
import static com.example.lectrac.HelperFunctions.*;

public class MessageBoardActivity extends AppCompatActivity {

    static LocalDatabaseManager localDB;

    static RecyclerView rvMessages;
    static Spinner spinCourse;
    static Spinner spinClass;
    static Button btnAddMessage;
    static ErrorClass ec;

    static String latestCourse;
    static String latestClass;

    static ArrayList<String> arrHeading = new ArrayList<>();
    static ArrayList<String> arrCourseCode = new ArrayList<>();
    static ArrayList<String> arrContents = new ArrayList<>();
    static ArrayList<String> arrClassification = new ArrayList<>();
    static ArrayList<String> arrDate = new ArrayList<>();
    static ArrayList<Integer> arrMessageID = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_board);

        setNightMode(this);
        ec = new ErrorClass(this);
        setDrawer();

        localDB = new LocalDatabaseManager(this);


        rvMessages = findViewById(R.id.rvMessageItems);
        spinClass = findViewById(R.id.spinMessageClass);
        spinCourse = findViewById(R.id.spinMessageCourse);
        btnAddMessage = findViewById(R.id.btnMessageAdd);

        if (localDB.isLec()){
            btnAddMessage.setVisibility(View.VISIBLE);
        }
        else{
            btnAddMessage.setVisibility(View.GONE);
        }

        clearArr();

        SetClassItems();
        SetCourseCodeItems();
        SetCourseChangeListener();
        SetClassChangeListener();

        latestCourse = "All";
        latestClass = "All";

        try {
            startAdapter();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    // drawer

    public void setDrawer(){

        // use the tool bar as action bar because the action bar was removed
        Toolbar toolbar = findViewById(R.id.toolbarTop);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);

        final DrawerHelper drawerHelper = new DrawerHelper(MessageBoardActivity.this, toolbar, drawer, navigationView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return drawerHelper.onNavigationItemSelected(menuItem);
            }
        });
    }


    // end of drawer


    void startAdapter() throws ParseException {
        clearArr();

        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblMessage + " ORDER BY Message_Date_Posted DESC");

        if (!cursor.moveToFirst()){
            ec.ShowUserError("There are no messages available",this);
            rvMessages.setAdapter(null);
            rvMessages.setLayoutManager(new LinearLayoutManager(this));
            return;
        }
        int size = cursor.getCount();


        int headingIdx = cursor.getColumnIndex("Message_Name");
        int courseIdx = cursor.getColumnIndex("Course_Code");
        int classIdx = cursor.getColumnIndex("Message_Classification");
        int dateIdx = cursor.getColumnIndex("Message_Date_Posted");
        int contentIdx = cursor.getColumnIndex("Message_Content");
        int msgIdIdx = cursor.getColumnIndex("Message_ID");
        int isDeletedIdx = cursor.getColumnIndex("Message_isDeleted");


        for (int i = 0; i < size; i++){

            int isDeleted = cursor.getInt(isDeletedIdx);
            if (isDeleted == 1){
                cursor.moveToNext();
                continue;
            }

            String heading = cursor.getString(headingIdx);
            String courseCode = cursor.getString(courseIdx);
            String classification = cursor.getString(classIdx);
            String date = cursor.getString(dateIdx);
            String content = cursor.getString(contentIdx);
            int msgId = cursor.getInt(msgIdIdx);


            arrHeading.add(heading);
            arrCourseCode.add(courseCode);
            arrClassification.add(classification);
            arrDate.add(date);
            arrContents.add(content);
            arrMessageID.add(msgId);

            cursor.moveToNext();
        }

        MessageAdapater messageAdapater = new MessageAdapater(this, arrHeading,
                arrCourseCode,arrContents,arrClassification,arrDate, arrMessageID);

        rvMessages.setAdapter(messageAdapater);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
    }

    void startAdapter(String condition1, String condition2) throws ParseException {
        clearArr();

        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblMessage + " WHERE " + condition1 + " AND " + condition2 + " ORDER BY Message_Date_Posted DESC");

        if (!cursor.moveToFirst()){
            ec.ShowUserError("There are no messages available",this);
            rvMessages.setAdapter(null);
            rvMessages.setLayoutManager(new LinearLayoutManager(this));
            return;
        }
        int size = cursor.getCount();


        int headingIdx = cursor.getColumnIndex("Message_Name");
        int courseIdx = cursor.getColumnIndex("Course_Code");
        int classIdx = cursor.getColumnIndex("Message_Classification");
        int dateIdx = cursor.getColumnIndex("Message_Date_Posted");
        int contentIdx = cursor.getColumnIndex("Message_Content");
        int msgIdIdx = cursor.getColumnIndex("Message_ID");
        int isDeletedIdx = cursor.getColumnIndex("Message_isDeleted");

        for (int i = 0; i < size; i++){
            int isDeleted = cursor.getInt(isDeletedIdx);
            if (isDeleted == 1){
                cursor.moveToNext();
                continue;
            }

            String heading = cursor.getString(headingIdx);
            String courseCode = cursor.getString(courseIdx);
            String classification = cursor.getString(classIdx);
            String date = cursor.getString(dateIdx);
            String content = cursor.getString(contentIdx);
            int msgId = cursor.getInt(msgIdIdx);

            arrHeading.add(heading);
            arrCourseCode.add(courseCode);
            arrClassification.add(classification);
            arrDate.add(date);
            arrContents.add(content);
            arrMessageID.add(msgId);

            cursor.moveToNext();
        }


        MessageAdapater messageAdapater = new MessageAdapater(this, arrHeading,
                arrCourseCode,arrContents,arrClassification,arrDate, arrMessageID);

        rvMessages.setAdapter(messageAdapater);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
    }

    public void adapterConditionMaker() throws ParseException {
        String conditionCourse;
        String conditionClass;
        if (latestCourse.equals("All")){
            conditionCourse = "1 = 1";
        }
        else{
            conditionCourse = "Course_Code = " + quote(latestCourse);
        }

        if (latestClass.equals("All")){
            conditionClass = "1 = 1";
        }
        else{
            conditionClass = "Message_Classification = " + quote(latestClass);
        }


        startAdapter(conditionCourse,conditionClass);
    }

    public void clearArr(){
        arrHeading.clear();
        arrCourseCode.clear();
        arrContents.clear();
        arrClassification.clear();
        arrDate.clear();
        arrMessageID.clear();
    }

    public void AddMessageButton(View v){
        startActivity(new Intent(this,AddNewMessage.class));
    }

    //region Spinner sort

    public void SetClassItems(){
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

    public void SetClassChangeListener(){

        spinClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String classification = parentView.getItemAtPosition(position).toString();
                latestClass = classification;
                try {
                    adapterConditionMaker();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public void SetCourseCodeItems(){
        String[] courses = localDB.getCourses(localDB);
        int size = courses.length;

        List<String> list = new ArrayList<String>();

        list.add("All");

        for (int i = 0; i < size; i ++){
            list.add(courses[i]);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCourse.setAdapter(dataAdapter);
    }

    public void SetCourseChangeListener(){

        spinCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String course = parentView.getItemAtPosition(position).toString();
                latestCourse = course;
                try {
                    adapterConditionMaker();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }
    //endregion

}
