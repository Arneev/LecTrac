package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import static com.example.lectrac.HelperFunctions.*;

public class DrawerHelper extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    Context context;

    public DrawerHelper(Context cont, Toolbar toolbar, DrawerLayout drawerLayout,
                        NavigationView navigationView, View header ){

        context = cont;
        drawer = drawerLayout;

        // set name
        setName(header);


        // create menu button (toggle)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle((Activity) context, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }


    // when the option is selected, the method returns true
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){ // check for item and act accordingly
            case R.id.nav_calendar:
                if (context instanceof CalendarActivity){
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                }
                else {
                    Intent i1 = new Intent(context, CalendarActivity.class);
                    context.startActivity(i1);
                    break;
                }

            case R.id.nav_todoList:
                if (context instanceof ToDoListActivity){
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                }
                else {
                    Intent i2 = new Intent(context, ToDoListActivity.class);
                    context.startActivity(i2);
                    break;
                }

            case R.id.nav_courses:
                if (context instanceof CourseListActivity){
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                }
                else {
                    Intent i3 = new Intent(context, CourseListActivity.class);
                    context.startActivity(i3);
                    break;
                }

            case R.id.nav_messageBoard:
                if (context instanceof MessageBoardActivity){
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                }
                else {
                    Intent i4 = new Intent(context, MessageBoardActivity.class);
                    context.startActivity(i4);
                    break;
                }

            case R.id.nav_testMarks:
                if (context instanceof TestMarksActivity){
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                }
                else {
                    Intent i5 = new Intent(context, TestMarksActivity.class);
                    context.startActivity(i5);
                    break;
                }

            case R.id.nav_settings:
                if (context instanceof SettingsActivity){
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                }
                else {
                    Intent i6 = new Intent(context, SettingsActivity.class);
                    context.startActivity(i6);
                    break;
                }

            case R.id.nav_logout:
                LocalDatabaseManager localDB = new LocalDatabaseManager(context);
                localDB.doDelete(tblUser);
                Intent i7 = new Intent(context, MainActivity.class);
                context.startActivity(i7);
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

    public void setName(View header){

        LocalDatabaseManager localDB = new LocalDatabaseManager(context);
        TextView userName = header.findViewById(R.id.tvNavUsername);

        String userID = localDB.getUserID(localDB);
        Cursor cursor = localDB.doQuery("SELECT * FROM USER WHERE User_ID = " + userID);

        if (cursor.getCount() == 0){
            userName.setText("LecTrac");
            return;
        }

        cursor.moveToFirst();
        int NickIndex = cursor.getColumnIndex("Nickname");
        String nickname = cursor.getString(NickIndex);

        userName.setText(nickname);
    }

}
