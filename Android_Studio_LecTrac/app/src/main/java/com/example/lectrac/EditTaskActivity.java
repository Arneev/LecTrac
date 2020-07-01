package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class EditTaskActivity extends AppCompatActivity {

    //region Intialization
    int position;

    public static String newTaskName, newDueDate, newDueTime, newCourseCode;
    public static String taskID, oldTaskName, oldCourseCode, oldDate, oldTime;
    public static OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    public static String[] courses;
    public LocalDatabaseManager localDB = new LocalDatabaseManager(this);
    static boolean shouldChangeActs;

    ToDoAdapter toDoAdapter;
    RecyclerView recyclerView;

    ArrayList<String> arrOnlyTaskNames = new ArrayList<>();
    ArrayList<String> arrOnlyTaskCourses = new ArrayList<>();
    ArrayList<String> arrOnlyTaskIDs = new ArrayList<>();

    String tableName = "";
    
    static ErrorClass ec;

    boolean updateDateInOnline;
    boolean updateTimeInOnline;
    boolean updateCourseCode;

    static ProgressBar progressBar;

    Boolean isLec, isLecTask;
    private long mLastClickTime = 0;

    String whichActivity;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        progressBar = findViewById(R.id.progressBarEditTask);
        shouldChangeActs = false;
        // is user a student or lecturer?
        isLec = localDB.isLec();

        ec = new ErrorClass(this);


        updateDateInOnline = false;
        updateTimeInOnline = false;
        updateCourseCode = false;

        // get values passed from Adapter class
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle != null) {

            position = bundle.getInt("position");
            arrOnlyTaskNames = bundle.getStringArrayList("arrTaskNames");
            arrOnlyTaskCourses = bundle.getStringArrayList("arrTaskCourses");
            arrOnlyTaskIDs = bundle.getStringArrayList("arrTaskIDs");
            whichActivity = bundle.getString("Activity");

            Log("Data passed between activities is successful");
        }

        // methods
        setCourseSpinnerItems();
        setComponents();

        DateOnClicker();
        TimeOnClicker();
        SaveTaskClick();
        cancelEditTaskClick();

    }

    public boolean saveTask() throws InterruptedException, JSONException, IOException {

        Log("About to edit task");

        getTaskTitle();
        getCourse();

        // set parameters for doUpdate
        String setting = "";
        String condition = "Task_ID = " + unquote(taskID.substring(1));


        // is user a student or lecturer?
        boolean isLec = localDB.isLec();

        // quotations
        // *check if user updated any values

        boolean blnDoUpdate = false;

        // if isLec and task is posted then edit task in onlineDB

        if (isTaskNameNull()){
            ec.ShowUserError("Enter a task name",this);
            shouldChangeActs = false;
            return false;
        }
        else{

            // if the task name was changed, update array
            if (!newTaskName.equals(oldTaskName)){

                blnDoUpdate = true;

                newTaskName = quote(completeUnquote(newTaskName,10));
                setting += "Task_Name = " + newTaskName + ",";
                arrOnlyTaskNames.set(position, newTaskName);
                Log("About to update:" + setting);
            }
        }

        // date cannot be empty because the user cannot un-select a date, date was set at onCreate
        // due date cannot be removed, user has to delete the task and create a new one with no date
        // If the task due date was changed, update the database
       if (isDateNull(oldDate)){

           if (isDateNull(newDueDate)){
               Log("No update for date");
           }
           else {

               blnDoUpdate = true;

               newDueDate = quote(completeUnquote(newDueDate,10));
               setting = setting + "Task_Due_Date = " + newDueDate + ",";
               Log("About to update:" + setting);
               updateDateInOnline = true;
           }
       }
       else {

           if (isDateNull(newDueDate)){
               Log("No update for date");
           }
           else {

               if (!newDueDate.equals(oldDate)){

                   blnDoUpdate = true;

                   newDueDate = quote(completeUnquote(newDueDate,10));
                   setting = setting + "Task_Due_Date = " + newDueDate + ",";
                   Log("About to update:" + setting);

                   updateDateInOnline = true;
               }
           }

       }


        // time cannot be empty because the user cannot un-select the time, time was set at onCreate
        // due time cannot be removed, user has to delete the task and create a new one with no time
        // if the task due date was changed, update the database
        if (isTimeNull(oldTime)){

            if (isTimeNull(newDueTime)){
                Log("No update for time");
            }
            else {

                blnDoUpdate = true;
                newDueTime = quote(completeUnquote(newDueTime,10));
                setting = setting + "Task_Due_Time = " + newDueTime + ",";
                Log("About to update:" + setting);
                updateTimeInOnline = true;
            }
        }
        else {

            if (isTimeNull(newDueTime)){
                Log("No update for time");
            }
            else {

                if (!newDueTime.equals(oldTime)) {

                    blnDoUpdate = true;

                    newDueTime = quote(completeUnquote(newDueTime,10));
                    setting = setting + "Task_Due_Time = " + newDueTime + ",";
                    Log("About to update:" + setting);
                    updateTimeInOnline = true;
                }
            }

        }

        // Course code is set at onCreate so it will never be none
        // if the task course was changed, update the database and array
        if (!newCourseCode.equals(oldCourseCode)){

            blnDoUpdate = true;

            newCourseCode = quote(completeUnquote(newCourseCode,10));
            setting = setting + "Course_Code = " + newCourseCode;
            arrOnlyTaskCourses.set(position, newCourseCode);
            Log("About to update:" + setting);

            updateCourseCode = true;
        }

        // *end of checking

        setting = removeComa(setting);


        // update in local DB and if isLec update in online DB
        tableName = tblUserTask;

        if (blnDoUpdate){
            if (isLecTask){
                tableName = tblLocalLecTask;

                //Insert into local db and online db
            }
            if (isLec){
                if (isCourseNull()){
                    tableName = tblLocalLecTask;
                    if (!isOnline(this)){
                        ec.ShowUserMessage("You are not connected to the internet",this);
                        shouldChangeActs = false;
                        return false;
                    }

                    if (isLecTask){
                        ec.ShowUserError("You cannot post a message without a course code");
                        shouldChangeActs = false;
                        return false;
                    }


                    // Local DB
                    tableName = tblLocalLecTask;
                    Log("Update in " + tableName);
                    LogCal(setting);
                    LogCal(condition);
                    localDB.doUpdate(tableName, setting, condition);

                    // Online DB
                    Log("Update in online DB");


                    if (isLecTask) {
                        //onlineDB.Update(tableName, setting, condition);
                        Log("GONNA UPDATE ONLINEEEEEEE");

                        if (updateCourseCode) {
                            onlineDB.update_task_coursecode_taskid(unquote(newCourseCode), taskID);
                        }

                        if (!newTaskName.equals(oldTaskName)) {
                            onlineDB.update_task_taskname_taskid(unquote(newTaskName), taskID);
                        }

                        if (updateTimeInOnline) {
                            onlineDB.update_task_duetime_taskid(unquote(newDueTime), taskID);
                        }

                        if (updateDateInOnline) {
                            onlineDB.update_task_duedate_taskid(unquote(newDueDate), taskID);
                        }
                    }
                    //end of OnlineDB
                }
                else {
                    Log("Update in " + tableName);
                    LogCal(setting);
                    LogCal(condition);
                    localDB.doUpdate(tableName, setting, condition);
                }

            }
            else {
                Log("Update in " + tableName);
                LogCal(setting);
                LogCal(condition);
                localDB.doUpdate(tableName, setting, condition);
            }
        }

        return true;
    }

    //region Getters
    public void getTaskTitle(){

        // get the title of the task
        EditText edtTitleTask = findViewById(R.id.etTaskName);
        newTaskName = edtTitleTask.getText().toString();

    }

    public void getDate() {
        final TextView displayDate = findViewById(R.id.tvDate);

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

                newDueDate = dateText;
                displayDate.setText(dateText);
            }
        }, YEAR, MONTH, DATE); // part of datePickerDialog

        datePickerDialog.show();

    }

    public void getTime() {

        final TextView displayTime = findViewById(R.id.tvTime);

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

                newDueTime = dateText;
                displayTime.setText(dateText);
            }
        }, HOUR, MINUTE, is24HourFormat);

        timePickerDialog.show();
    }

    public void getCourse(){
        final Spinner spinCourse = findViewById(R.id.spinCourses);
        newCourseCode = spinCourse.getSelectedItem().toString();
    }

    //endregion

    //region HelperFunctions

    public boolean isTaskNameNull(){

        EditText etTaskName = findViewById(R.id.etTaskName);
        String checkTaskName = etTaskName.getText().toString();

        if (checkTaskName.equals("")){
            Log("checkTaskName is null");
            return true;
        }
        return false;
    }

    public boolean isDateNull(String checkDate){

        if (checkDate == null || checkDate.equals("NULL") || checkDate.equals("null")){
            Log("checkDate is null");
            return true;
        }
        return false;
    }

    public boolean isTimeNull(String checkTime){

        if (checkTime == null || checkTime.equals("NULL") || checkTime.equals("null")){
            Log("checkTime is null");
            return true;
        }
        return false;
    }

    public boolean isCourseNull(){

        String checkCourse = newCourseCode;

        if (checkCourse == null || checkCourse.equals("None") | checkCourse.equals("NULL")){
            Log("checkCourse is null");
            return true;
        }
        return false;

        //sCourse from textView will always have a string,
        // if "None" is selected, consider this null
    }

    public String removeComa(String str) {

        if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    //endregion

    //region Clickers & Setters
    public void setCourseSpinnerItems() {

        Spinner spinCourse = findViewById(R.id.spinCourses);
        courses = localDB.getCourses(localDB);
        int courseSize = courses.length;

        List<String> list = new ArrayList<String>();

        list.add("None");

        // get course to display
        oldCourseCode = arrOnlyTaskCourses.get(position);

        for (int i = 0; i < courseSize; i++) {
            list.add(courses[i]);

        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCourse.setAdapter(dataAdapter);

        // set spinner to display course code
        int spinnerPosition = dataAdapter.getPosition(oldCourseCode);
        spinCourse.setSelection(spinnerPosition);

        Log("Course name set");
    }

    public void setComponents(){

        // set task name
        oldTaskName = arrOnlyTaskNames.get(position);
        EditText etTaskName = findViewById(R.id.etTaskName);
        etTaskName.setText(oldTaskName);

        Log("Task name set");

        // set task date and time

        boolean isLec = localDB.isLec();
        Log("isLec about to return " + Boolean.toString(isLec));

        // set table name and get task id
        taskID = arrOnlyTaskIDs.get(position);

        if (taskID.charAt(0) == 'U'){

            tableName = tblUserTask;
            isLecTask = false;
        }
        else if (taskID.charAt(0) == 'L'){

            tableName = tblLocalLecTask;
            isLecTask = true;
        }
        else{
            Log("No TaskID found");
            ec.ShowUserError("Please contact support - Task ID not found",this);
        }


        Cursor cursor = localDB.doQuery("SELECT * FROM " + tableName +
                " WHERE Task_ID = " + taskID.substring(1));

        Log("SELECT * FROM " + tableName + " WHERE Task_ID = " + taskID.substring(1));

        cursor.moveToFirst();

        int indexDate = cursor.getColumnIndex("Task_Due_Date");
        oldDate = cursor.getString(indexDate);

        int indexTime = cursor.getColumnIndex("Task_Due_Time");
        oldTime = cursor.getString(indexTime);

        TextView tvDate = findViewById(R.id.tvDate);
        tvDate.setText(oldDate);

        TextView tvTime = findViewById(R.id.tvTime);
        tvTime.setText(oldTime);

        Log("Task date and time set");

    }

    public void SaveTaskClick(){
        Button btnSaveTask = findViewById(R.id.btnEditTask);

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
                    if (!saveTask()){
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    else{
                        if (whichActivity.equals("To-Do List")) {
                            ec.ShowUserMessageWait("Edited Task", ToDoListActivity.class);
                        }else {
                            ec.ShowUserMessageWait("Edited Task", CalendarActivity.class);
                        }
                    }


                } catch (InterruptedException | JSONException | IOException e) {
                    ec.ShowUserError(showCheckInternetConnection,EditTaskActivity.this);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void DateOnClicker(){
        Button btnDueDate = findViewById(R.id.btnDate);
        newDueDate = null;

        btnDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });
    }

    public void TimeOnClicker(){
        Button btnDueTime = findViewById(R.id.btnTime);
        newDueTime = null;

        btnDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
            }
        });
    }

    public void cancelEditTaskClick(){
        Button btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (whichActivity.equals("To-Do List")) {
                    startActivity(new Intent(EditTaskActivity.this, ToDoListActivity.class));
                }else {
                    startActivity(new Intent(EditTaskActivity.this, CalendarActivity.class));
                }
            }
        });
    }


    //endregion

    //region Helper Function

    @Override
    public void onBackPressed(){

        if (whichActivity.equals("To-Do List")) {
            startActivity(new Intent(this, ToDoListActivity.class));
        }else {
            startActivity(new Intent(this, CalendarActivity.class));
        }
    }

    //endregion
}
