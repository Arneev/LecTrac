package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import static com.example.lectrac.HelperFunctions.*;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        setNightMode(this);

        // use the tool bar as action bar because the action bar was removed
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // create menu button (toggle)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    // when the option is selected, the method returns true
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){ // check for item and act accordingly
            case R.id.nav_calendar:
                Intent i1 = new Intent(this, CalendarActivity.class);
                startActivity(i1);
                break;
            case R.id.nav_todoList:
                Intent i2 = new Intent(this, ToDoListActivity.class);
                startActivity(i2);
                break;
            case R.id.nav_courses:
                Intent i3 = new Intent(this, CourseListActivity.class);
                startActivity(i3);
                break;
            case R.id.nav_messageBoard:
                Intent i4 = new Intent(this, MessageBoardActivity.class);
                startActivity(i4);
                break;
            case R.id.nav_testMarks:
                Intent i5 = new Intent(this, TestMarksActivity.class);
                startActivity(i5);
                break;
            case R.id.nav_settings:
                Intent i7 = new Intent(this, SettingsActivity.class);
                startActivity(i7);
                break;
            case R.id.nav_logout:
                LocalDatabaseManager localDB = new LocalDatabaseManager(this);
                localDB.doDelete(tblUser);
                Intent i8 = new Intent(this, MainActivity.class);
                startActivity(i8);
        }
        return true;
    }


    //when we press the back button while the navigation bar is open, we don't want to leave
    //the activity immediately, we want to close the navigation drawer.
    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
