package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class TestMarksActivity extends AppCompatActivity {

    DrawerLayout drawer;

    static String[] courses;
    static LocalDatabaseManager localDB;
    static ErrorClass ec;

    Spinner spinCourse;
    RecyclerView rvTest;

    static ArrayList<String> arrName = new ArrayList<>();
    static ArrayList<Integer> arrMark = new ArrayList<>();
    static ArrayList<Integer> arrTot = new ArrayList<>();
    static ArrayList<String> arrCourse = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_marks);

        ec = new ErrorClass(this);
        setNightMode(this);
        setIconsToAppearMode();

        setDrawer();

        localDB = new LocalDatabaseManager(this);
        courses = localDB.getCourses(localDB);

        spinCourse = findViewById(R.id.spinTestCourse);
        rvTest = findViewById(R.id.rvTest);

        SetCourseSpinnerItems();
        SetCourseChangeListener();

        try{
            startAdapter();
        }catch (Exception e){
            Log(e.toString());
            ec.ShowUserError("Failed to update tests",this);
        }
    }

    // drawer

    public void setDrawer(){

        LocalDatabaseManager localDB = new LocalDatabaseManager(this);

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

        final DrawerHelper drawerHelper = new DrawerHelper(TestMarksActivity.this, toolbar,
                drawer, navigationView, header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return drawerHelper.onNavigationItemSelected(menuItem);
            }
        });
    }


    // end of drawer


    void startAdapter() {
        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblTest);
        setAdapter(cursor);
    }

    void startAdapter(String condition){
        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblTest + " WHERE " + condition);
        setAdapter(cursor);
    }

    void setAdapter(Cursor cursor){
        clearArr();

        if (!cursor.moveToFirst()){
            ec.ShowUserError("There are no tests available",this);
            rvTest.setAdapter(null);
            rvTest.setLayoutManager(new LinearLayoutManager(this));
            return;
        }

        int size = cursor.getCount();


        int nameIdx = cursor.getColumnIndex("Test_Name");
        int markIdx = cursor.getColumnIndex("Test_Mark");
        int totIdx = cursor.getColumnIndex("Test_Total");
        int courseIdx = cursor.getColumnIndex("Course_Code");

        for (int i = 0; i < size; i++){

            String name = cursor.getString(nameIdx);
            int mark = cursor.getInt(markIdx);
            int tot = cursor.getInt(totIdx);
            String courseCode = cursor.getString(courseIdx);


            arrName.add(name);
            arrCourse.add(courseCode);
            arrMark.add(mark);
            arrTot.add(tot);

            cursor.moveToNext();
        }

        TestMarksAdapter testMarksAdapter = new TestMarksAdapter(this,arrName,arrMark,arrTot,arrCourse);
        rvTest.setAdapter(testMarksAdapter);
        rvTest.setLayoutManager(new LinearLayoutManager(this));
    }

    public void FilterOnCourseChange(String course){
        if (course.equals("All")){
            startAdapter();
            return;
        }
        String condition = "Course_Code = " + quote(course);
        startAdapter(condition);
    }

    void clearArr(){
        arrName.clear();
        arrMark.clear();
        arrTot.clear();
        arrCourse.clear();
    }

    //region Spinner Setter
    public void SetCourseSpinnerItems(){
        int courseSize = courses.length;

        List<String> list = new ArrayList<String>();

        list.add("All");

        for(int i = 0; i < courseSize; i++){
            list.add(courses[i]);
        }


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
                FilterOnCourseChange(course);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }


    public void setIconsToAppearMode() {
        Toolbar toolbar = findViewById(R.id.toolbarTop);

        if (isDarkMode(this)) {
            toolbar.getContext().setTheme(R.style.ToolbarIconDark);
        } else {
            toolbar.getContext().setTheme(R.style.ToolbarIconLight);

        }
    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);

        }
    }

    //endregion
}
