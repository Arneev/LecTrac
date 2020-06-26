package com.example.lectrac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.example.lectrac.HelperFunctions.*;

public class CourseListActivity extends AppCompatActivity {

    static RecyclerView rvCourseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        rvCourseItems = findViewById(R.id.rvCourseItems);

        startAdapter();
    }

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


}
