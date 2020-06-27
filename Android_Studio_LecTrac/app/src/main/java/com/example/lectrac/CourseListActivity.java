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
import android.widget.Button;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class CourseListActivity extends AppCompatActivity {

    DrawerLayout drawer;

    static RecyclerView rvCourseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Syncer syncer = new Syncer(CourseListActivity.this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        setNightMode(this);
        setIconsToAppearMode();

        setDrawer();

        rvCourseItems = findViewById(R.id.rvCourseItems);

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startAdapter();
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

        final DrawerHelper drawerHelper = new DrawerHelper(CourseListActivity.this, toolbar,
                drawer, navigationView, header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return drawerHelper.onNavigationItemSelected(menuItem);
            }
        });
    }


    // end of drawer


    void startAdapter(){

        LocalDatabaseManager localDB = new LocalDatabaseManager(CourseListActivity.this);

        ArrayList<CourseDetails> courseDetails = new ArrayList<>();
        String[] courses = localDB.getCourses(localDB);

        final String origQuery = "SELECT Course_Name,Lecturer_FName, Lecturer_LName,Lecturer_Email FROM ( (COURSE INNER JOIN REGISTERED ON " +
                "COURSE.Course_Code = REGISTERED.Course_Code) INNER JOIN LECTURER ON LECTURER.Lecturer_ID = " +
                "REGISTERED.Lecturer_ID) WHERE COURSE.Course_Code = ";


        int size = courses.length;
        int courseNameIndex = 0;
        int firstNameIndex = 1;
        int lastNameIndex = 2;
        int lecIndex = 3;

        //Getting all into courseDetails ArrayList
        for (int i = 0; i < size; i++){
            Cursor cursor = localDB.doQuery(origQuery + quote(courses[i]));
            Log("Query is " + origQuery + quote(courses[i]));

            int lecSize = cursor.getCount();

            Log("Lecsize is " + lecSize);

            if (lecSize == 0){
                continue;
            }

            String[] lecName = new String[lecSize];
            String[] lecEmail = new String[lecSize];

            cursor.moveToFirst();

            for (int j = 0; j < lecSize; j++){

                //region Getting index
                if (i == 0 && j == 0){
                    courseNameIndex = cursor.getColumnIndex("Course_Name");
                    firstNameIndex = cursor.getColumnIndex("Lecturer_FName");
                    lastNameIndex = cursor.getColumnIndex("Lecturer_LName");
                    lecIndex = cursor.getColumnIndex("Lecturer_Email");
                }
                //endregion

                lecName[j] = cursor.getString(firstNameIndex) + " " + cursor.getString(lastNameIndex);
                lecEmail[j] = cursor.getString(lecIndex);

                if (j != lecSize - 1){
                    cursor.moveToNext();
                }
            }

            String courseName = cursor.getString(courseNameIndex);

            CourseDetails tempCourseDetails = new CourseDetails(courseName,lecName,lecEmail);
            courseDetails.add(tempCourseDetails);
        }

        Log("Reached here");
        CourseListAdapter courseListAdapter = new CourseListAdapter(CourseListActivity.this,courseDetails);
        rvCourseItems.setAdapter(courseListAdapter);
        rvCourseItems.setLayoutManager(new LinearLayoutManager(this));

    }

    public void setIconsToAppearMode(){
        Toolbar toolbar = findViewById(R.id.toolbarTop1);

        if (isDarkMode(this)){
            toolbar.setNavigationIcon(R.drawable.ic_list_white);
        }
        else{
            toolbar.setNavigationIcon(R.drawable.ic_list);
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

}
