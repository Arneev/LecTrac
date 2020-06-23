package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;


public class ToDoListActivity extends AppCompatActivity {

    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    LocalDatabaseManager localDB = new LocalDatabaseManager(this);

    ToDoAdapter toDoAdapter;
    RecyclerView recyclerView;
    Spinner spinCourse;
    Spinner spinLecFilt;

    ArrayList<String> arrOnlyTaskNames = new ArrayList<>();
    ArrayList<String> arrOnlyTaskCourses = new ArrayList<>();
    ArrayList<String> arrOnlyTaskIDs = new ArrayList<>();

    public static String[] courses;
    public static int lecFilOption;
    public static String latestCourseFil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        lecFilOption = 0;
        latestCourseFil = "All";

        spinLecFilt = findViewById(R.id.spinFilterLec);
        spinCourse = findViewById(R.id.spinFilterCourses);
        recyclerView = findViewById(R.id.rvToDoItems);

        SetLecSpinnerItems();
        SetCourseSpinnerItems();
        SetCourseChangeListener();
        SetLecSpinnerChangeListener();

        try{
            StartAdapter();
        }catch (Exception e){
            Log(e.toString());
            Log("Failed to do Adapter shit");
        }

        moveToAddTask();

    }

    void StartAdapter() throws InterruptedException {
        arrOnlyTaskNames.clear();
        arrOnlyTaskCourses.clear();
        arrOnlyTaskIDs.clear();
        Log("Starting to do the RecyclerView code");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //region Sync
                try {
                    Syncer syncer = new Syncer(ToDoListActivity.this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //endregion
            }
        });

        t.start();

        if (lecFilOption == 0 || lecFilOption == 2){
            addFromLocalDB();
        }

        t.join();

        if (lecFilOption == 0 || lecFilOption == 1){
            addFromOnlineDB();
        }


        // is user a student or lecturer?
        boolean isLec = localDB.isLec();

        // using adapter class for Recycler View

        toDoAdapter = new ToDoAdapter(this, isLec, arrOnlyTaskNames, arrOnlyTaskCourses, arrOnlyTaskIDs);
        recyclerView.setAdapter(toDoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ToDoListActivity.this));
    }

    private void addFromLocalDB (){

        String taskName, taskCourse, taskID, taskTable;

        taskTable = tblUserTask;

        Cursor cursor = localDB.doQuery(orderByDateAndTime("SELECT * FROM " + taskTable));

        int cursorCount = cursor.getCount();

        if (cursorCount == 0){
            Log("No user task");
            return;
        }

        cursor.moveToFirst();
        int indexID = cursor.getColumnIndex("Task_ID");
        int indexName = cursor.getColumnIndex("Task_Name");
        int indexCourse = cursor.getColumnIndex("Course_Code");

        for (int localIndex = 0; localIndex < cursorCount; localIndex++){

            // add a "U" before the task ID to show that the task is from the User_Task table in the localDB
            taskID = "U" + Integer.toString(cursor.getInt(indexID));
            taskName = cursor.getString(indexName);
            taskCourse = cursor.getString(indexCourse);

            arrOnlyTaskIDs.add(taskID);
            arrOnlyTaskNames.add(taskName);
            arrOnlyTaskCourses.add(taskCourse);

            cursor.moveToNext();
        }
    }

    private void addFromOnlineDB(){

        String taskName, taskCourse, taskID, taskTable;
        int isDone;

        taskTable = tblLocalLecTask;

        Cursor cursor = localDB.doQuery(orderByDateAndTime("SELECT * FROM " + taskTable));

        int cursorCount = cursor.getCount();

        if (cursorCount == 0){
            Log("No lecturer task");
            return;
        }

        cursor.moveToFirst();
        int indexID = cursor.getColumnIndex("Task_ID");
        int indexName = cursor.getColumnIndex("Task_Name");
        int indexCourse = cursor.getColumnIndex("Course_Code");
        int indexIsDone = cursor.getColumnIndex("isDone");

        for (int localIndex = 0; localIndex < cursorCount; localIndex++){

            // add a "L" before the task ID to show that the task is from the Lecturer_Task table in the localDB
            taskID = "L" + Integer.toString(cursor.getInt(indexID));
            taskName = cursor.getString(indexName);
            taskCourse = cursor.getString(indexCourse);
            isDone = cursor.getInt(indexIsDone);

            // if task is not done then add to the arrays
            if (isDone == 0) {
                arrOnlyTaskIDs.add(taskID);
                arrOnlyTaskNames.add(taskName);
                arrOnlyTaskCourses.add(taskCourse);
            }

            cursor.moveToNext();

        }
    }

    //region overLoads w/ Condition

    void StartAdapter(String condition) throws InterruptedException {
        arrOnlyTaskNames.clear();
        arrOnlyTaskCourses.clear();
        arrOnlyTaskIDs.clear();
        Log("Starting to do the RecyclerView code WITH CONDITION");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //region Sync
                try {
                    Syncer syncer = new Syncer(ToDoListActivity.this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //endregion
            }
        });

        t.start();

        if (lecFilOption == 0 || lecFilOption == 2){
            addFromLocalDB(condition);
        }

        t.join();

        if (lecFilOption == 0 || lecFilOption == 1){
            addFromOnlineDB(condition);
        }

        // is user a student or lecturer?
        boolean isLec = localDB.isLec();

        // using adapter class for Recycler View

        toDoAdapter = new ToDoAdapter(this, isLec, arrOnlyTaskNames, arrOnlyTaskCourses, arrOnlyTaskIDs);
        recyclerView.setAdapter(toDoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ToDoListActivity.this));
    }

    private void addFromLocalDB (String condition){

        String taskName, taskCourse, taskID, taskTable;

        taskTable = tblUserTask;

        Cursor cursor = localDB.doQuery(orderByDateAndTime("SELECT * FROM " + taskTable + " WHERE " + condition));

        int cursorCount = cursor.getCount();

        if (cursorCount == 0){
            Log("No user task");
            return;
        }

        cursor.moveToFirst();
        int indexID = cursor.getColumnIndex("Task_ID");
        int indexName = cursor.getColumnIndex("Task_Name");
        int indexCourse = cursor.getColumnIndex("Course_Code");

        for (int localIndex = 0; localIndex < cursorCount; localIndex++){

            // add a "U" before the task ID to show that the task is from the User_Task table in the localDB
            taskID = "U" + Integer.toString(cursor.getInt(indexID));
            taskName = cursor.getString(indexName);
            taskCourse = cursor.getString(indexCourse);

            arrOnlyTaskIDs.add(taskID);
            arrOnlyTaskNames.add(taskName);
            arrOnlyTaskCourses.add(taskCourse);

            cursor.moveToNext();
        }
    }

    private void addFromOnlineDB(String condition){

        String taskName, taskCourse, taskID, taskTable;
        int isDone;

        taskTable = tblLocalLecTask;

        Cursor cursor = localDB.doQuery(orderByDateAndTime("SELECT * FROM " + taskTable + " WHERE " + condition));

        int cursorCount = cursor.getCount();

        if (cursorCount == 0){
            Log("No lecturer task");
            return;
        }

        cursor.moveToFirst();
        int indexID = cursor.getColumnIndex("Task_ID");
        int indexName = cursor.getColumnIndex("Task_Name");
        int indexCourse = cursor.getColumnIndex("Course_Code");
        int indexIsDone = cursor.getColumnIndex("isDone");

        for (int localIndex = 0; localIndex < cursorCount; localIndex++){

            // add a "L" before the task ID to show that the task is from the Lecturer_Task table in the localDB
            taskID = "L" + Integer.toString(cursor.getInt(indexID));
            taskName = cursor.getString(indexName);
            taskCourse = cursor.getString(indexCourse);
            isDone = cursor.getInt(indexIsDone);

            // if task is not done then add to the arrays
            if (isDone == 0) {
                arrOnlyTaskIDs.add(taskID);
                arrOnlyTaskNames.add(taskName);
                arrOnlyTaskCourses.add(taskCourse);
            }

            cursor.moveToNext();

        }
    }

    //endregion

    //region Filter

    public void FilterOnChange(String course) throws InterruptedException {
        latestCourseFil = course;

        if (course.equals("All")){
            StartAdapter();
            return;
        }

        String queryCondition;

        queryCondition = "Course_Code = " + quote(course);

        StartAdapter(queryCondition);
    }

    public void FilterTask(String option) throws InterruptedException{
        if (option.equals("Lecturer")){
            lecFilOption = 1;
        }
        else if (option.equals("Personal")){
            lecFilOption = 2;
        }
        else{
            lecFilOption = 0;
        }

        FilterOnChange(latestCourseFil);
    }

    //endregion

    //region Helper Functions
    private void moveToAddTask(){

        Button btnAddNew = (Button) findViewById(R.id.btnAddNew);
        btnAddNew.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToDoListActivity.this, AddNewTaskActivity.class));
            }
        });

    }

    public void SetCourseSpinnerItems(){
        courses = localDB.getCourses(localDB);
        int courseSize = courses.length;

        List<String> list = new ArrayList<String>();

        list.add("All");

        for(int i = 0; i < courseSize; i++){
            list.add(courses[i]);
        }

        list.add("None");

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
                try {
                    FilterOnChange(course);
                } catch (InterruptedException e) {
                    Log(e.toString());
                    ShowUserError("Failed to filter tasks, please contact support");
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public void SetLecSpinnerItems(){
        List<String> list = new ArrayList<String>();

        list.add("All");
        list.add("Lecturer");
        list.add("Personal");


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinLecFilt.setAdapter(dataAdapter);
    }

    public void SetLecSpinnerChangeListener(){

        spinLecFilt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String option = parentView.getItemAtPosition(position).toString();
                try {
                    FilterTask(option);
                } catch (InterruptedException e) {
                    Log(e.toString());
                    Log("LecFilt error");
                    ShowUserError("Failed to filter tasks, please contact support");
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
