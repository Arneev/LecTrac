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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.example.lectrac.HelperFunctions.*;

public class AddNewTaskActivity extends AppCompatActivity {

    String sTaskName, sDueDate, sDueTime;
    OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();
    HelperFunctions helperFunctions = new HelperFunctions();

    private SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);

        getTaskTitle();
        getTaskDueDateTime();

        Button btnSaveTask = findViewById(R.id.btnSaveTask);
        Button btnCancel = findViewById(R.id.btnCancelTask);

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


        cancelAddTask();
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

    private void getTaskTitle(){

        // get the title of the task
        EditText edtTitleTask = findViewById(R.id.etTitleTask);
        sTaskName = edtTitleTask.getText().toString();

    }

    private void getTaskDueDateTime(){

        Button btnDueDate = findViewById(R.id.btnDueDate);
        Button btnDueTime = findViewById(R.id.btnDueTime);

        btnDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });


        btnDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
            }
        });
    }


    private void getDate() {

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

                displayDate.setText(dateText);
            }
        }, YEAR, MONTH, DATE); // part of datePickerDialog

        datePickerDialog.show();

        sDueDate = displayDate.getText().toString();
    }


    private void getTime() {

        final TextView displayTime = findViewById(R.id.tvDisplayTime);

        // get instance of the calendar to get dynamic time
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                //Log.i(TAG, "onTimeSet: " + hour + minute);
                Calendar calTime = Calendar.getInstance();
                calTime.set(Calendar.HOUR, hour);
                calTime.set(Calendar.MINUTE, minute);
                String dateText = DateFormat.format("HH:mm", calTime).toString();
                displayTime.setText(dateText);
            }
        }, HOUR, MINUTE, is24HourFormat);

        timePickerDialog.show();

        sDueTime = displayTime.getText().toString();
    }


    public void saveTask() throws InterruptedException, JSONException, IOException {

        LocalDatabaseManager localDatabaseManager = new LocalDatabaseManager(this);

        // is user a student or lecturer?
        boolean isLec = localDatabaseManager.isLec();

        // quotations
        sTaskName = quote(sTaskName);
        sDueDate = quote(sDueDate);
        sDueTime = quote(sDueTime);

        // data
        String tableName = "USER_TASK";
        String[] columns = {"Task_Name", "Task_Due_Date", "Task_Due_Time"};
        String[] data = {sTaskName, sDueDate, sDueTime};

        // save new task to local database
        localDatabaseManager.doInsert(tableName, columns, data);

        // if user is a lecturer then the task must also be saved to the online database
        if (isLec){
            onlineDB.Insert(tableName, columns, data);
        }
    }


    private void cancelAddTask(){

        Button btnCancel = findViewById(R.id.btnCancelTask);
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddNewTaskActivity.this, ToDoListActivity.class));
            }
        });
    }
}
