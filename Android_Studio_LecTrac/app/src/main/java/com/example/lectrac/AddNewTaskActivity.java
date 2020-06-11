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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

    public static String sTaskName, sDueDate, sDueTime, sCourseCode;
    public static OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    public static String[] courses;
    public static LocalDatabaseManager localDB = null;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);

        localDB = new LocalDatabaseManager(this); //Sorry i just had to rename this lol

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

    public void saveTask() throws InterruptedException, JSONException, IOException {
        Log("About to saveTask");

        getTaskTitle();
        getCourse();

        // is user a student or lecturer?
        boolean isLec = localDB.isLec();
        Log("isLec about to return " + Boolean.toString(isLec));

        courses = localDB.getCourses(localDB);
        int courseSize = courses.length;


        // quotations


        if (isTaskNameNull()){
            ShowUserError("Enter a task name");
        }
        else{
            sTaskName = quote(sTaskName);
        }



        if (isDateNull()){
            sDueDate = "NULL";
        }
        else{
            sDueDate = quote(sDueDate);
        }



        if (isTimeNull()){
            sDueTime = "NULL";
        }
        else{
            sDueTime = quote(sDueTime);
        }

        if (isCourseNull()){

            if (isLec){
                if (courseSize > 0){
                    ShowUserError("You have to enter a course code, cannot be none");
                }
                else if (courseSize == 0){
                    ShowUserError("Contact support with " + errorLecNoCourse);
                }
                return;
            }

            sCourseCode = "NULL"; //Going to perso userTask (Only he/she can access it)

        }
        else{
            sCourseCode = quote(sCourseCode);
        }


        Log(sTaskName);
        Log(sDueDate);
        Log(sDueTime);

        // data
        String tableName = "USER_TASK";
        String[] columns = {"Task_Name", "Task_Due_Date", "Task_Due_Time","isDone","Course_Code"};
        String[] data = {sTaskName, sDueDate, sDueTime, "0", sCourseCode};

        // save new task to local database






        // if user is a lecturer then the task must also be saved to the online database
        if (isLec){
            if (!sCourseCode.equals("NULL")){
                tableName = tblLocalLecTask;
            }

            localDB.doInsert(tableName, columns, data);

            Log("isLec and about to insert into onlineDB");
            String userID = quote(localDB.getUserID(localDB));

            String[] lecCols = {"Task_Name","Task_Due_Date","Course_Code","Lecturer_ID","Task_Due_Time"};
            String[] lecData = {sTaskName,sDueDate,sCourseCode,userID,sDueTime};

            onlineDB.Insert(tblTask,lecCols,lecData);


        }
        else{
            localDB.doInsert(tableName, columns, data);
            Log("isStudent, no insert into onlineDB");
        }
    }



    //region HelperFunctions


    //Please use Log(String); function to debug and test
    public boolean isDateNull(){
        return false; //Complete pls
    }

    public boolean isTaskNameNull(){
        return false; //Complete pls
    }

    public boolean isTimeNull(){
        return false; //Complete pls
    }

    public boolean isCourseNull(){
        return false; //Complete pls

        //sCourse from textView will always have a string,
        // if "None" is selected, consider this null
    }

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
    //endregion


    //region Clickers
    public void SaveTaskClick(){
        Button btnSaveTask = findViewById(R.id.btnSaveTask);

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveTask();
                } catch (InterruptedException | JSONException | IOException e) {
                    e.printStackTrace();
                }
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

}
