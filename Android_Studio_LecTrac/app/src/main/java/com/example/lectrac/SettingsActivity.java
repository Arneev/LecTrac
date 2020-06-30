package com.example.lectrac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import static com.example.lectrac.HelperFunctions.*;

public class SettingsActivity extends AppCompatActivity {

    DrawerLayout drawer;
    static ErrorClass ec;
    static ProgressBar progressBar;
    static Button saveButton;
    static Context context;
    static CheckBox cbxDarkMode;
    static EditText edtNickname;
    static EditText edtPassword;


    public static LocalDatabaseManager localDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;
        ec = new ErrorClass(context);

        saveButton = findViewById(R.id.btnSettingsSave);
        cbxDarkMode = findViewById(R.id.cbxSettingsDarkMode);
        edtNickname = findViewById(R.id.edtSettingsNickname);
        //edtPassword = findViewById(R.id.edtChangePass);

        setSaveButtonListener();
        onCheckBoxTick();

        setDrawer();
        setNightMode(this);
        setIconsToAppearMode();

        localDB = new LocalDatabaseManager(SettingsActivity.this);

        try {
            SetStartValues();
        }
        catch(Exception e){
            Log(e.toString());
            Log("There was a problem setting the start values");
        }

        progressBar = findViewById(R.id.progBar_Settings);

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

    public void Save() throws InterruptedException {

        Boolean isOnline = isOnline(context);

        if (!isOnline){
            ec.ShowUserError("Connect to the internet in order to save changes",context);
            return;
        }

        edtNickname = findViewById(R.id.edtSettingsNickname);
//        edtPassword = findViewById(R.id.edtChangePass);

        String nickname = edtNickname.getText().toString();
//        String password = edtPassword.getText().toString();
//
//        if (!correctPassParams(password)){
//            return;
//        }

        Log("nickname with quote(nickname) is " + quote(nickname));


        try {
            localDB.doUpdate(tblUser, "Nickname = " + quote(nickname));
        }
        catch(Exception e){
            Log(e.toString());
            Log("localInsert has failed");
        }

        //Local Update done

        Boolean isLec = localDB.isLec();
        String userID = localDB.getUserID(localDB);

//        try{
//            password = CopyOnly(saltAndHash(password),passwordLength);
//        }catch (Exception e){
//            ec.ShowUserError("Please try again");
//            return;
//        }

        OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

        Log("IsLec is " + isLec.toString());

        if (isLec){
            onlineDB.update_lecturer_lecturernickname_userid(nickname,userID);
        }
        else {
            onlineDB.update_student_studentnickname_userid(nickname,userID);
        }



        ec.ShowUserMessage("Finished Save");
    }

    public void SettingSync(View v) throws InterruptedException, JSONException, ParseException, IOException {
        if (!isOnline(this)){
            ec.ShowUserMessage("You are not connected to the internet",this);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        Syncer syncer = new Syncer(SettingsActivity.this);

        syncer.ManualSync(SettingsActivity.this);
        progressBar.setVisibility(View.GONE);
    }

    public void setSaveButtonListener(){
        saveButton = findViewById(R.id.btnSettingsSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    Save();
                } catch (InterruptedException e) {
                    Log(e.toString());
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }



    public void setIconsToAppearMode(){
        Toolbar toolbar = findViewById(R.id.toolbarTop);

        if (isDarkMode(this)){
            toolbar.getContext().setTheme(R.style.ToolbarIconDark);
        }
        else{
            toolbar.getContext().setTheme(R.style.ToolbarIconLight);
        }
    }

    public void onCheckBoxTick(){
        cbxDarkMode = findViewById(R.id.cbxSettingsDarkMode);

        cbxDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences(myPrefName, MODE_WORLD_WRITEABLE).edit();
                editor.clear();
                editor.apply(); // commit changes
                editor.putBoolean("isDarkMode",isChecked);
                editor.apply();

                String sDarkMode = "";

                if (isChecked){
                    sDarkMode = "1";
                }
                else{
                    sDarkMode = "0";
                }

                localDB.doUpdate(tblUser, "isDarkMode = " + sDarkMode);

                setNightMode(context);
                setIconsToAppearMode();
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


    boolean correctPassParams(@NotNull String password){
        Log("Correct Pass Params");
        boolean containSpecial = false;
        boolean isDigit = false;
        boolean isUpper = false;
        boolean isLower = false;
        boolean hasWhiteSpace = false;

        for (char c : password.toCharArray()){
            if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^'
                    || c == '&' || c == '*'){
                containSpecial = true;
            }
            if (Character.isDigit(c)){
                isDigit = true;
            }

            if (Character.isUpperCase(c)){
                isUpper = true;
            }

            if (Character.isLowerCase(c)){
                isLower = true;
            }

            if (c == ' '){
                hasWhiteSpace = true;
            }
        }

        if (!containSpecial){
            ec.ShowUserError("Make sure there is at least one special character",context);
            return false;
        }

        if (!isDigit){
            ec.ShowUserError("Make sure there is at least one number",context);
            return false;
        }

        if (!isLower){
            ec.ShowUserError("Make sure there is at least one lower case character",context);
            return false;
        }

        if (!isUpper) {
            ec.ShowUserError("Make sure there is at least one upper case character",context);
            return false;
        }

        if (hasWhiteSpace){
            ec.ShowUserError("Cannot have whitespace in password",context);
            return false;
        }

        if (password.length() < 8){
            ec.ShowUserError("Make sure your password is at least 8 characters long",context);
            return false;
        }

        return true;
    }

}
