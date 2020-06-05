package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;


public class ToDoListActivity extends AppCompatActivity {

    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

    ToDoAdapter toDoAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        // get the reference of RecyclerView
        recyclerView = findViewById(R.id.rvToDoItems);

        // create JSON array and get size
        JSONArray arrTaskName = new JSONArray();
        ArrayList<String> arrOnlyTaskNames = new ArrayList<>();
        ArrayList<String> arrOnlyTaskCourses = new ArrayList<>();

        try {
            arrTaskName = onlineDB.getJSONArr("SELECT * FROM TASK");

        } catch (InterruptedException | IOException | JSONException e) {
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
                e.printStackTrace();
            }

        }

        // using adapter class for Recycler View

        toDoAdapter = new ToDoAdapter(this, arrOnlyTaskNames, arrOnlyTaskCourses);
        recyclerView.setAdapter(toDoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

}
