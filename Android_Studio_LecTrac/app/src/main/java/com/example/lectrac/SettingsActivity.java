package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.*;

public class SettingsActivity extends AppCompatActivity {

    public static LocalDatabaseManager localDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setNightMode(this);

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

        // use the tool bar as action bar because the action bar was removed
        Toolbar toolbar = findViewById(R.id.toolbarTop);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);

        final DrawerHelper drawerHelper = new DrawerHelper(SettingsActivity.this, toolbar, drawer, navigationView);

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
            ShowUserError("Connect to the internet in order to save changes",this);
            return;
        }


        TextView edtNick = findViewById(R.id.edtSettingsNickname);
        CheckBox cbxDarkMode = findViewById(R.id.cbxSettingsDarkMode);

        Boolean isDarkMode = cbxDarkMode.isChecked();
        String nickname = edtNick.getText().toString();

        Log("isDarkMode.toString() is " + isDarkMode.toString());
        Log("nickname with quote(nickname) is " + quote(nickname));

        String sDarkMode;

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

    public void ShowUserError(final String error, final Context context){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HelperFunctions.ShowUserError(error,context);
                    }
                });
            }
        });

        t.start();
    }

}
