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
import java.text.ParseException;
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

    void StartAdapter() throws InterruptedException {
        Log("Starting to do the RecyclerView code");

        // get the reference of RecyclerView
        recyclerView = (RecyclerView)findViewById(R.id.rvToDoItems);

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

        addFromLocalDB();
        t.join();
        addFromOnlineDB();


        // using adapter class for Recycler View

        toDoAdapter = new ToDoAdapter(this, arrOnlyTaskNames, arrOnlyTaskCourses, arrOnlyTaskIDs);
        recyclerView.setAdapter(toDoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ToDoListActivity.this));
    }



    private void addFromLocalDB (){

        String taskName, taskCourse, taskID;

        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblUserTask);

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

            taskID = Integer.toString(cursor.getInt(indexID));
            taskName = cursor.getString(indexName);
            taskCourse = cursor.getString(indexCourse);

            arrOnlyTaskIDs.add(taskID);
            arrOnlyTaskNames.add(taskName);
            arrOnlyTaskCourses.add(taskCourse);

            cursor.moveToNext();
        }
    }

    private void addFromOnlineDB(){

        String taskName, taskCourse, taskID;

        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblLocalLecTask);

        int cursorCount = cursor.getCount();

        if (cursorCount == 0){
            Log("No lecturer task");
            return;
        }

        cursor.moveToFirst();
        int indexID = cursor.getColumnIndex("Task_ID");
        int indexName = cursor.getColumnIndex("Task_Name");
        int indexCourse = cursor.getColumnIndex("Course_Code");

        for (int localIndex = 0; localIndex < cursorCount; localIndex++){

            taskID = Integer.toString(cursor.getInt(indexID));
            taskName = cursor.getString(indexName);
            taskCourse = cursor.getString(indexCourse);

            arrOnlyTaskIDs.add(taskID);
            arrOnlyTaskNames.add(taskName);
            arrOnlyTaskCourses.add(taskCourse);

            cursor.moveToNext();

        }
    }

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


    //endregion

}
