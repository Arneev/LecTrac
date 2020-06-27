package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.*;

public class SettingsActivity extends AppCompatActivity {

    DrawerLayout drawer;
    static ErrorClass ec;

    public static LocalDatabaseManager localDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ec = new ErrorClass(this);
        setNightMode(this);
        setIconsToAppearMode();

        setDrawer();

        localDB = new LocalDatabaseManager(SettingsActivity.this);

        try {
            SetStartValues();
        }
        catch(Exception e){
            Log(e.toString());
            Log("There was a problem setting the start values");
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

        final DrawerHelper drawerHelper = new DrawerHelper(SettingsActivity.this, toolbar,
                drawer, navigationView, header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return drawerHelper.onNavigationItemSelected(menuItem);
            }
        });
    }


    // end of drawer


    public void SetStartValues(){
        Cursor cursor = localDB.doQuery("SELECT * FROM " + tblUser);
        cursor.moveToFirst();

        String nick = cursor.getString(cursor.getColumnIndex("Nickname"));
        int iDarkMode = cursor.getInt(cursor.getColumnIndex("isDarkMode"));

        TextView edtNick = findViewById(R.id.edtSettingsNickname);
        CheckBox cbxDarkMode = findViewById(R.id.cbxSettingsDarkMode);

        edtNick.setText(nick);

        Boolean isDarkMode;

        if (iDarkMode == 1){
            isDarkMode = true;
        }
        else{
            isDarkMode = false;
        }

        cbxDarkMode.setChecked(isDarkMode);
    }

    public void Save(View v){

        Boolean isOnline = isOnline(SettingsActivity.this);

        if (!isOnline){
            ec.ShowUserError("Connect to the internet in order to save changes",this);
            return;
        }


        TextView edtNick = findViewById(R.id.edtSettingsNickname);
        CheckBox cbxDarkMode = findViewById(R.id.cbxSettingsDarkMode);

        Boolean isDarkMode = cbxDarkMode.isChecked();
        String nickname = edtNick.getText().toString();

        Log("isDarkMode.toString() is " + isDarkMode.toString());
        Log("nickname with quote(nickname) is " + quote(nickname));

        String sDarkMode;

        SharedPreferences.Editor editor = getSharedPreferences(myPrefName, MODE_WORLD_WRITEABLE).edit();
        editor.clear();
        editor.apply(); // commit changes
        editor.putBoolean("isDarkMode",isDarkMode);
        editor.apply();

        if (isDarkMode){
            sDarkMode = "1";
        }
        else{
            sDarkMode = "0";
        }

        try {
            localDB.doUpdate(tblUser, "isDarkMode = " + sDarkMode);
            localDB.doUpdate(tblUser, "Nickname = " + quote(nickname));
        }
        catch(Exception e){
            Log(e.toString());
            Log("localInsert has failed");
        }

        setNightMode(this);
        setIconsToAppearMode();

        //Local Update done

        Boolean isLec = localDB.isLec();
        String userID = localDB.getUserID(localDB);

        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

        Log("IsLec is " + isLec.toString());

        if (isLec){

            onlineDB.Update(tblLecturer,"Lecturer_Reference = " + quote(nickname),
                    "Lecturer_ID = " + quote(userID));
        }
        else {

            onlineDB.Update(tblStudent,"Student_Nickname = " + quote(nickname),
                    "Student_ID = " + quote(userID));
        }



    }

    public void SettingSync(View v) throws InterruptedException, JSONException, ParseException, IOException {
        Syncer syncer = new Syncer(SettingsActivity.this);

        syncer.ManualSync(SettingsActivity.this);
    }

    public void setIconsToAppearMode(){
        Toolbar toolbar = findViewById(R.id.toolbarTop);

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
