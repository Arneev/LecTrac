package com.example.lectrac;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.lectrac.HelperFunctions.*;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder>{

    Context context;
    ArrayList<String> arrTaskNames = new ArrayList<>();
    ArrayList<String> arrTaskCourses = new ArrayList<>();
    ArrayList<String> arrTaskTimes = new ArrayList<>();


    public CalendarAdapter (Context cont, ArrayList<String> names, ArrayList<String> courses, ArrayList<String> times){

        context = cont;
        arrTaskNames = names;
        arrTaskCourses = courses;
        arrTaskTimes = times;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_calendar_event, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        if (arrTaskNames.isEmpty()){

            Log("No Tasks");
            holder.myName.setText("No events");
            return;
        }

        holder.myName.setText(arrTaskNames.get(position));

        if (arrTaskCourses.get(position) == null || arrTaskCourses.get(position) == "NULL"){
            holder.myCourse.setText("None");
        }
        else{
            holder.myCourse.setText(arrTaskCourses.get(position));
        }


        if (arrTaskTimes.get(position) == null || arrTaskTimes.get(position).equals("NULL")){
            holder.myTime.setVisibility(View.INVISIBLE);
        }
        else{
            String display = "Due at: " + arrTaskTimes.get(position).substring(0,5);
            holder.myTime.setText(display);
        }

        holder.ivOptions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, holder.ivOptions);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) { // check for item and act accordingly
                            case R.id.mtEditTask:


                            case R.id.mtDeleteTask:


                            case R.id.mtReminder:
                        }
                        return false;
                    }
                });


                popup.inflate(R.menu.calendar_menu);
                popup.show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return arrTaskNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView myName, myCourse, myTime;
        ImageView ivOptions;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            myName = itemView.findViewById(R.id.tvCalendarName);
            myCourse = itemView.findViewById(R.id.tvCalendarCourse);
            myTime = itemView.findViewById(R.id.tvCalendarTime);
            ivOptions = itemView.findViewById(R.id.ivCalendarOptions);
        }
    }



}
