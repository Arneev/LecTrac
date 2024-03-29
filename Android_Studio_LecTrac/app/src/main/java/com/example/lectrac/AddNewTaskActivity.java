package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.example.lectrac.HelperFunctions.*;

public class AddNewTaskActivity extends AppCompatActivity {

    //region Initialization
    public static String sTaskId, sTaskName, sDueDate, sDueTime, sCourseCode;
    public static OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    public static String[] courses;
    public static LocalDatabaseManager localDB = null;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private long mLastClickTime = 0;
    static ProgressBar progressBar;

    ToDoAdapter toDoAdapter;
    RecyclerView recyclerView;
    static ErrorClass ec;

    Boolean isLec, mustPost;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);
        progressBar = findViewById(R.id.progressBarAddTask);
        ec = new ErrorClass(this);
        setNightMode(this);
        localDB = new LocalDatabaseManager(this);

        // is user a student or lecturer?
        isLec = localDB.isLec();
        Log("isLec about to return " + Boolean.toString(isLec));

        mustPost = false;


        sTaskId = "";
        sTaskName = "";
        sDueDate = "";
        sDueTime = "";
        sCourseCode = "";

        SetCourseSpinnerItems();

        DateOnClicker();
        TimeOnClicker();
        SaveTaskClick();
        cancelAddTaskClick();
    }

    public void isPosted(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Upload New Task");

        builder.setMessage("Would you like to post this task to students?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mustPost = true;

                        try {
                            saveTask();
                        } catch (Exception e){
                            ec.ShowUserMessage("Problem saving task");
                        }

                        dialog.dismiss();
                        ec.ShowUserMessageWait("Finished Add Task",ToDoListActivity.class);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mustPost = false;

                        try {
                            saveTask();
                        } catch (Exception e){
                            ec.ShowUserMessage("Problem saving task");
                        }

                        dialog.dismiss();
                        ec.ShowUserMessageWait("Finished Add Task",ToDoListActivity.class);
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //region Getters
    public void getTaskTitle(){

        // get the title of the task
        EditText edtTitleTask = findViewById(R.id.etTitleTask);
        sTaskName = edtTitleTask.getText().toString();

    }

    public void getDate() {
        final TextView displayDate = findViewById(R.id.tvDisplayDate);

        // get instance of the calendar to get dynamic date
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                Calendar calDate = Calendar.getInstance();
                calDate.set(Calendar.YEAR, year);
                calDate.set(Calendar.MONTH, month);
                calDate.set(Calendar.DATE, date);
                String dateText = DateFormat.format("yyyy-MM-dd", calDate).toString();

                sDueDate = dateText;
                displayDate.setText(dateText);
            }
        }, YEAR, MONTH, DATE); // part of datePickerDialog

        datePickerDialog.show();

    }

    public void getTime() {

        final TextView displayTime = findViewById(R.id.tvDisplayTime);

        // get instance of the calendar to get dynamic time
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                //Log.i(TAG, "onTimeSet: " + hour + minute); - USE THE LOG IN THE HELPER FUNCTION -_-
                Calendar calTime = Calendar.getInstance();
                calTime.set(Calendar.HOUR, hour);
                calTime.set(Calendar.MINUTE, minute);
                String dateText = DateFormat.format("HH:mm", calTime).toString();

                sDueTime = dateText;
                displayTime.setText(dateText);
            }
        }, HOUR, MINUTE, is24HourFormat);

        timePickerDialog.show();
    }

    public void getCourse(){
        final Spinner spinCourse = findViewById(R.id.spinCourseCode);
        sCourseCode = spinCourse.getSelectedItem().toString();
    }

    //endregion

    public boolean saveTask() throws InterruptedException, JSONException, IOException {
        Log("About to saveTask");

        getTaskTitle();
        getCourse();


        courses = localDB.getCourses(localDB);
        int courseSize = courses.length;

        // quotations

        if (isTaskNameNull()) {
            ec.ShowUserError("Enter a task name", this);
            return false;
        }
        else{
            sTaskName = quote(completeUnquote(sTaskName,10));
        }

        if (isDateNull()){
            sDueDate = "NULL";


        }
        else {
            sDueDate = quote(completeUnquote(sDueDate,10));
        }

        if (isTimeNull()){
            sDueTime = "NULL";
        }
        else{
            sDueTime = quote(completeUnquote(sDueTime,10));
        }


        if (isCourseNull() && mustPost){

            if (isLec){
                if (courseSize > 0){
                    ec.ShowUserError("You have to enter a course code, cannot be empty",this);
                    return false;
                }
                else if (courseSize == 0){
                    ec.ShowUserError("Contact support with " + errorLecNoCourse,this);
                    return false;
                }

            }

            sCourseCode = "NULL"; //Going to perso userTask (Only he/she can access it)

        }else{
            sCourseCode = quote(completeUnquote(sCourseCode,10));
        }



        Log(sTaskName);
        Log(sDueDate);
        Log(sDueTime);

        // data
        String tableName = tblUserTask;
        String[] columns = {"Task_Name", "Task_Due_Date", "Task_Due_Time","isDone","Course_Code"};
        String[] data = {sTaskName, sDueDate, sDueTime, "0", sCourseCode};

        // save new task to local database


        // if user is a lecturer then the task must also be saved to the online database
        if (isLec){
            if (!sCourseCode.equals("None") && mustPost){
                if (!isOnline(this)){
                    ec.ShowUserMessage("You are not connected to the internet",this);
                    return false;
                }

                //Local insert - inefficient, change later on but keep for now
                String userID = quote(localDB.getUserID(localDB));

                String[] locLecCols = {"Task_Name","Task_Due_Date","Task_Due_Time","isDone","Course_Code","Lecturer_ID"};
                String[] locLecData = {sTaskName,sDueDate,sDueTime,"0",sCourseCode,userID};
                tableName = tblLocalLecTask;

                Log("isLec and sCourseCode is NOT NULL about to insert into localDB");
                localDB.doInsert(tableName,locLecCols,locLecData);
                sTaskId = "L" + localDB.getLastID(tableName);


                //Online insert

                Log("isLec and about to insert into onlineDB");

                String[] lecData = {unquote(sTaskName),unquote(sDueDate),unquote(sCourseCode),unquote(userID),unquote(sDueTime)};

                onlineDB.insert_task(lecData);

            }
            else{
                if (sCourseCode.equals("None") && mustPost){
                    ec.ShowUserError("Please choose a course code");
                    return false;
                }

                Log("isLec and sCourseCode IS NULL about to insert into localDB");
                localDB.doInsert(tableName, columns, data);
                sTaskId = "U" + localDB.getLastID(tableName);

            }

        }
        else{
            localDB.doInsert(tableName, columns, data);
            Log("isStudent, no insert into onlineDB");
            sTaskId = "U" + localDB.getLastID(tableName);
        }

        // adapter

        Log("Update toDoAdapter after adding new task");
        toDoAdapter = new ToDoAdapter(sTaskName, sCourseCode, sTaskId);

        return true;
    }

    //region HelperFunctions

    public boolean isDateNull(){

        TextView tvDate = findViewById(R.id.tvDisplayDate);
        String checkDate = tvDate.getText().toString();

        if (checkDate.equals("") || checkDate.equals("Date")){
            Log("checkDate is null");
            return true;
        }
        return false;
    }

    public boolean isTaskNameNull(){

        EditText etTaskName = findViewById(R.id.etTitleTask);
        String checkTaskName = etTaskName.getText().toString();

        if (checkTaskName == null || checkTaskName.equals("")){
            Log("checkTaskName is null");
            return true;
        }
        return false;
    }

    public boolean isTimeNull(){

        TextView tvTime = findViewById(R.id.tvDisplayTime);
        String checkTime = tvTime.getText().toString();

        if (checkTime == null || checkTime.equals("") || checkTime.equals("Time") || checkTime.equals("null") || checkTime.equals("NULL")){
            Log("checkTime is null");
            return true;
        }
        return false;
    }

    public boolean isCourseNull(){

        Spinner spinner = (Spinner)findViewById(R.id.spinCourseCode);
        String checkCourse = spinner.getSelectedItem().toString();

        if (checkCourse == null || checkCourse.equals("None") || checkCourse.equals("null") || checkCourse.equals("NULL")){
            Log("checkCourse is null");
            return true;
        }
        return false;

        //sCourse from textView will always have a string,
        // if "None" is selected, consider this null
    }

    //endregion

    //region Clickers
    public void SetCourseSpinnerItems(){
        Spinner spinCourse = findViewById(R.id.spinCourseCode);
        courses = localDB.getCourses(localDB);
        int courseSize = courses.length;

        List<String> list = new ArrayList<String>();

        list.add("None");

        for(int i = 0; i < courseSize; i++){
            list.add(courses[i]);
        }


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCourse.setAdapter(dataAdapter);
    }

    public void SaveTaskClick(){
        Button btnSaveTask = findViewById(R.id.btnSaveTask);

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                try {
                    if (isLec) {
                        isPosted();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (saveTask()){
                        progressBar.setVisibility(View.GONE);
                        ec.ShowUserMessageWait("Finished Add Task",ToDoListActivity.class);
                    }

                } catch (InterruptedException | JSONException | IOException e) {
                    Log(e.toString());
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void DateOnClicker(){
        Button btnDueDate = findViewById(R.id.btnDueDate);

        btnDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });
    }

    public void TimeOnClicker(){
        Button btnDueTime = findViewById(R.id.btnDueTime);

        btnDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
            }
        });
    }

    public void cancelAddTaskClick(){
        Button btnCancel = findViewById(R.id.btnCancelTask);

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddNewTaskActivity.this, ToDoListActivity.class));
            }
        });
    }


    public void blah(){

        final TextView displayDate = findViewById(R.id.tvDisplayDate);
        TextView displayTime = findViewById(R.id.tvDisplayTime);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(true);
        builder.setTitle(displayDate.getText().toString());
        builder.setMessage(displayTime.getText().toString());

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    //endregion


    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, ToDoListActivity.class));
    }

}
