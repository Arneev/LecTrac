package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.lectrac.HelperFunctions.*;


public class ToDoListActivity extends AppCompatActivity {

    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

    ToDoAdapter toDoAdapter;
    RecyclerView recyclerView;


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

        // create JSON array and ArrayList
        JSONArray arrTaskName = null;
        ArrayList<String> arrOnlyTaskNames = new ArrayList<>();
        ArrayList<String> arrOnlyTaskCourses = new ArrayList<>();

        try {
            Log("About to query, SELECT * FROM TASK");
            arrTaskName = onlineDB.getJSONArr("SELECT * FROM TASK");

        } catch (InterruptedException | IOException | JSONException e) {
            Log(e.toString());
            e.printStackTrace();
        }


        // create object to get each item from JSON array to get Task_Name and Course_Code
        JSONObject objTask;
        String taskName, taskCourse;


        for (int itemIndex = 0; itemIndex < arrTaskName.length(); itemIndex++) {

            try {
                objTask = arrTaskName.getJSONObject(itemIndex);
                taskName = objTask.getString("Task_Name");
                taskCourse = objTask.getString("Course_Code");

                arrOnlyTaskNames.add(taskName);
                arrOnlyTaskCourses.add(taskCourse);

            } catch (JSONException e) {
                Log(e.toString());
                e.printStackTrace();
            }

        }

        // using adapter class for Recycler View

        toDoAdapter = new ToDoAdapter(this, arrOnlyTaskNames, arrOnlyTaskCourses);
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

}