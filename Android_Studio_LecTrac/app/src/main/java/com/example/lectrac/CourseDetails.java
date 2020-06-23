package com.example.lectrac;

public class CourseDetails {
    String courseName, courseCode;
    String[] lecFull, lecEmail;

    CourseDetails(String courseName, String[] lecFull, String[] lecEmail){
        this.courseName = courseName;
        this.lecFull = lecFull;
        this.lecEmail = lecEmail;
    }

}
