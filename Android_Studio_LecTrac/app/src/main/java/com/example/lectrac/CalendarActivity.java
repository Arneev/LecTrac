package com.example.lectrac;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.lectrac.HelperFunctions.*;

public class CalendarActivity extends AppCompatActivity {

    DrawerLayout drawer;

    private CompactCalendarView cCalendarView;

    String calendarDate;
    ArrayList<String> arrTaskNames = new ArrayList<>();
    ArrayList<String> arrTaskCourses = new ArrayList<>();
    ArrayList<String> arrTaskTimes = new ArrayList<>();
    ArrayList<String> arrCalendarDates = new ArrayList<>();

    String[] arrMonths = new String[] {"January", "February", "March", "April", "May",
                                                "June", "July", "August", "September", "October",
                                                "November", "December"};

    RecyclerView recyclerView;
    CalendarAdapter calendarAdapter;
    static LocalDatabaseManager localDB;

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    Button btnScrollRight, btnScrollLeft;
    TextView tvMonth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);



        localDB = new LocalDatabaseManager(this);

        setDrawer();

        cCalendarView = (CompactCalendarView) findViewById(R.id.cvCalendar);
        recyclerView = (RecyclerView) findViewById(R.id.rvCalendarEvents);

        btnScrollLeft = findViewById(R.id.btnCalendarLeft);
        btnScrollRight = findViewById(R.id.btnCalendarRight);
        tvMonth = findViewById(R.id.tvCalendarMonth);

        setUpDate();
        scrollOnClick();

        try {
            highlightDates();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        setNightMode(this);
        setIconsToAppearMode();
    }


    // drawer

    public void setDrawer(){

        boolean isLec = localDB.isLec();

        // use the tool bar as action bar because the action bar was removed
        Toolbar toolbar = findViewById(R.id.toolbarTop);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();

        if (isLec){
            navigationView.inflateMenu(R.menu.drawer_menu_lecturer);
        }
        else {
            navigationView.inflateMenu(R.menu.drawer_menu);
        }

        View header = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
        navigationView.addHeaderView(header);

        final DrawerHelper drawerHelper = new DrawerHelper(CalendarActivity.this, toolbar,
                                                            drawer, navigationView, header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return drawerHelper.onNavigationItemSelected(menuItem);
            }
        });
    }


    // end of drawer


    void StartAdapter() throws InterruptedException {
        Log("Starting to do the RecyclerView code");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //region Sync
                try {
                    Syncer syncer = new Syncer(CalendarActivity.this);
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
        Log("send data to calendar adapter");
        calendarAdapter = new CalendarAdapter(this, arrTaskNames, arrTaskCourses, arrTaskTimes);
        recyclerView.setAdapter(calendarAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(CalendarActivity.this));
        Log("done");
    }


    // dates that have due dates have a small red circle under the date, indicating a task is due
    public void highlightDates() throws ParseException {

        Log("highlightDates");

        arrCalendarDates.clear();

        getLocalDates();
        getOnlineDates();

        long epoch;
        Date tempDate;

        for (int i = 0; i < arrCalendarDates.size(); i++) {

            tempDate = format.parse(arrCalendarDates.get(i));
            assert tempDate != null;
            epoch = tempDate.getTime() + 'L';

            Event event = new Event(Color.BLUE, epoch, null);
            cCalendarView.addEvent(event);
        }

        Log("set events");
    }


    // helper functions

    void getLocalDates() {

        Log("getLocalDates");

        Cursor cursor = localDB.doQuery("SELECT * FROM USER_TASK");

        if (cursor.getCount() == 0){
            Log("No local tasks");
            return;
        }

        int indexDate = cursor.getColumnIndex("Task_Due_Date");
        cursor.moveToFirst();

        for (int index = 0; index < cursor.getCount(); index++){

            arrCalendarDates.add(cursor.getString(indexDate));
            cursor.moveToNext();
        }
    }


    void getOnlineDates() {

        Log("getOnlineDates");

        Cursor cursor = localDB.doQuery("SELECT * FROM LECTURER_TASK");

        if (cursor.getCount() == 0){
            return;
        }

        int indexDate = cursor.getColumnIndex("Task_Due_Date");
        cursor.moveToFirst();

        for (int index = 0; index < cursor.getCount(); index++){

            arrCalendarDates.add(cursor.getString(indexDate));
            cursor.moveToNext();
        }
    }


    public void setUpDate(){

        final TextView tvDate = findViewById(R.id.tvCalendarDate);
        final DateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        // current date onCreate
        Date currDate = Calendar.getInstance().getTime();
        tvDate.setText(displayFormat.format(currDate));

        cCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                calendarDate = displayFormat.format(dateClicked);
                tvDate.setText(calendarDate);
                Log(calendarDate);

                arrTaskNames.clear();
                arrTaskCourses.clear();
                arrTaskTimes.clear();

                try{
                    StartAdapter();
                }catch (Exception e){
                    Log(e.toString());
                    Log("Failed to do Adapter shit");
                }

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {

                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(firstDayOfNewMonth);
                int month = tempCal.get(Calendar.MONTH);
                int year = tempCal.get(Calendar.YEAR);

                String display = arrMonths[month] + " " + year;
                tvMonth.setText(display);
            }
        });



    }


    public void addFromLocalDB(){

        Log("addFromLocalDB");
        // local tasks

        Cursor cursor = localDB.doQuery("SELECT * FROM USER_TASK WHERE Task_Due_Date = '" +
                calendarDate + "'");

        if (cursor.getCount() == 0){

            Log("No local tasks");
            return;
        }

        cursor.moveToFirst();

        int indexName = cursor.getColumnIndex("Task_Name");
        int indexCourse = cursor.getColumnIndex("Course_Code");
        int indexTime = cursor.getColumnIndex("Task_Due_Time");

        for (int index = 0; index < cursor.getCount(); index++){

            arrTaskNames.add(cursor.getString(indexName));
            arrTaskCourses.add(cursor.getString(indexCourse));
            arrTaskTimes.add(cursor.getString(indexTime));

            cursor.moveToNext();
        }

        Log("successful addFromLocalDB");
    }

    public void addFromOnlineDB(){

        Log("addFromOnlineDB");
        // online tasks

        Cursor cursor1 = localDB.doQuery("SELECT * FROM LECTURER_TASK WHERE Task_Due_Date = '" +
                calendarDate + "'");
        Log("SELECT * FROM LECTURER_TASK WHERE Task_Due_Date = '" +
                calendarDate + "'");

        if (cursor1.getCount() == 0){

            Log("No online tasks");
            return;
        }

        cursor1.moveToFirst();

        int iName = cursor1.getColumnIndex("Task_Name");
        int iCourse = cursor1.getColumnIndex("Course_Code");
        int iTime = cursor1.getColumnIndex("Task_Due_Time");

        for (int i = 0; i < cursor1.getCount(); i++){

            arrTaskNames.add(cursor1.getString(iName));
            arrTaskCourses.add(cursor1.getString(iCourse));
            arrTaskTimes.add(cursor1.getString(iTime));

            cursor1.moveToNext();
        }
        Log("successful addFromOnlineDB");
    }


    void scrollOnClick(){

        // set date onCreate
        Calendar tempCal = Calendar.getInstance();
        Date currDate = Calendar.getInstance().getTime();
        tempCal.setTime(currDate);
        int month = tempCal.get(Calendar.MONTH);
        int year = tempCal.get(Calendar.YEAR);

        String display = arrMonths[month] + " " + year;
        tvMonth.setText(display);

        btnScrollRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cCalendarView.scrollRight();
            }
        });


        btnScrollLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cCalendarView.scrollLeft();
            }
        });
    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    // end region

    public void setIconsToAppearMode(){
        Button btnArrowCalLeft = findViewById(R.id.btnCalendarLeft);
        Button btnArrowCalRight = findViewById(R.id.btnCalendarRight);
        Toolbar toolbar = findViewById(R.id.toolbarTop);

        if (isDarkMode(this)){
            btnArrowCalLeft.setBackgroundResource(R.drawable.ic_arrow_left_white);
            btnArrowCalRight.setBackgroundResource(R.drawable.ic_arrow_right_white);
            toolbar.getContext().setTheme(R.style.ToolbarIconDark);
        }
        else{
            btnArrowCalLeft.setBackgroundResource(R.drawable.ic_arrow_left);
            btnArrowCalRight.setBackgroundResource(R.drawable.ic_arrow_right);
            toolbar.getContext().setTheme(R.style.ToolbarIconLight);
        }
    }

}
