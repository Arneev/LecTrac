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
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.lectrac.HelperFunctions.*;


public class ToDoListActivity extends AppCompatActivity {

    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    LocalDatabaseManager localDB = new LocalDatabaseManager(this);

    ToDoAdapter toDoAdapter;
    RecyclerView recyclerView;

    ArrayList<String> arrOnlyTaskNames = new ArrayList<>();
    ArrayList<String> arrOnlyTaskCourses = new ArrayList<>();
    ArrayList<String> arrOnlyTaskIDs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        try{
            StartAdapter();
        }catch (Exception e){
            Log(e.toString());
            Log("Failed to do Adapter shit");
        }

        moveToAddTask();

    }

    void StartAdapter(){
        Log("Starting to do the RecyclerView code");

        // get the reference of RecyclerView
        recyclerView = (RecyclerView)findViewById(R.id.rvToDoItems);

        addFromLocalDB();
        addFromOnlineDB();


        // using adapter class for Recycler View

        toDoAdapter = new ToDoAdapter(this, arrOnlyTaskNames, arrOnlyTaskCourses, arrOnlyTaskIDs);
        recyclerView.setAdapter(toDoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void moveToAddTask(){

        Button btnAddNew = (Button) findViewById(R.id.btnAddNew);
        btnAddNew.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToDoListActivity.this, AddNewTaskActivity.class));
            }
        });

    }

    private void addFromLocalDB (){

        int indexID, indexName, indexCourse;
        String taskName, taskCourse, taskID;

        Cursor cursor = localDB.doQuery("SELECT * FROM tblUSER_TASK");

        if (cursor == null){
            return;
        }

        cursor.moveToFirst();

        for (int localIndex = 0; localIndex < cursor.getCount(); localIndex++){

            indexID = cursor.getColumnIndex("Task_ID");
            taskID = cursor.getString(indexID);

            indexName = cursor.getColumnIndex("Task_ID");
            taskName = cursor.getString(indexName);

            indexCourse = cursor.getColumnIndex("Task_ID");
            taskCourse = cursor.getString(indexCourse);

            arrOnlyTaskIDs.add(taskID);
            arrOnlyTaskNames.add(taskName);
            arrOnlyTaskCourses.add(taskCourse);

            cursor.moveToNext();
        }
    }

    private void addFromOnlineDB(){

        // create JSON array, cursor and ArrayList
        JSONArray arrOnlineTasks = null;

        try {
            Log("About to query, SELECT * FROM TASK");
            arrOnlineTasks = onlineDB.getJSONArr("SELECT * FROM TASK");

        } catch (InterruptedException | IOException | JSONException e) {
            Log(e.toString());
            e.printStackTrace();
        }


        // create object to get each item from JSON array to get Task_Name and Course_Code
        JSONObject objTask;
        String taskName, taskCourse, taskID;

        for (int itemIndex = 0; itemIndex < arrOnlineTasks.length(); itemIndex++) {

            try {
                objTask = arrOnlineTasks.getJSONObject(itemIndex);
                taskID = objTask.getString("Task_ID");
                taskName = objTask.getString("Task_Name");
                taskCourse = objTask.getString("Course_Code");

                arrOnlyTaskIDs.add(taskID);
                arrOnlyTaskNames.add(taskName);
                arrOnlyTaskCourses.add(taskCourse);

            } catch (JSONException e) {
                Log(e.toString());
                e.printStackTrace();
            }

        }
    }

}
