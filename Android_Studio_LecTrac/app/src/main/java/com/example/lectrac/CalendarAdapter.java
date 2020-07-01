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

    //region Intialization
    Context context;
    ArrayList<String> arrTaskNames = new ArrayList<>();
    ArrayList<String> arrTaskCourses = new ArrayList<>();
    ArrayList<String> arrTaskTimes = new ArrayList<>();
    ArrayList<String> arrTaskIDs = new ArrayList<>();

    ErrorClass ec;
    //endregion

    public static OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

    public CalendarAdapter (Context cont, ArrayList<String> names, ArrayList<String> courses,
                            ArrayList<String> times, ArrayList<String> ids){

        context = cont;
        arrTaskNames = names;
        arrTaskCourses = courses;
        arrTaskTimes = times;
        arrTaskIDs = ids;

        ec = new ErrorClass(context);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_calendar_event, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        LocalDatabaseManager localDB = new LocalDatabaseManager(context);
        final boolean isLec = localDB.isLec();

        final String taskID = arrTaskIDs.get(position);

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

        holder.ivCalendarOptions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!isLec && taskID.charAt(0) == 'L') {


                    PopupMenu popup = new PopupMenu(context, holder.ivCalendarOptions);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            switch (menuItem.getItemId()) { // check for item and act accordingly
                                case R.id.mtDelete:

                                    try {
                                        deleteTask(position);
                                    } catch (InterruptedException e) {
                                        Log(e.toString());
                                    }

                                    break;
                            }
                            return false;
                        }
                    });


                    popup.inflate(R.menu.task_menu_no_edit);
                    popup.show();
                }
                else {

                    PopupMenu popup = new PopupMenu(context, holder.ivCalendarOptions);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            switch (menuItem.getItemId()) { // check for item and act accordingly
                                case R.id.mtEdit:

                                    Intent intent = new Intent(context, EditTaskActivity.class);
                                    intent.putExtra("position", position);
                                    intent.putExtra("arrTaskNames", arrTaskNames);
                                    intent.putExtra("arrTaskCourses", arrTaskCourses);
                                    intent.putExtra("arrTaskIDs", arrTaskIDs);
                                    intent.putExtra("Activity", "Calendar");
                                    context.startActivity(intent);

                                    break;

                                case R.id.mtDelete:

                                    try {
                                        deleteTask(position);
                                    } catch (InterruptedException e) {
                                        Log(e.toString());
                                    }

                                    break;
                            }
                            return false;
                        }
                    });


                    popup.inflate(R.menu.calendar_menu);
                    popup.show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return arrTaskNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView myName, myCourse, myTime;
        ImageView ivCalendarOptions;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            myName = itemView.findViewById(R.id.tvCalendarName);
            myCourse = itemView.findViewById(R.id.tvCalendarCourse);
            myTime = itemView.findViewById(R.id.tvCalendarTime);
            ivCalendarOptions = itemView.findViewById(R.id.ivCalendarOptions);
        }
    }


    public void deleteTask(int position) throws InterruptedException {

        Log("About to delete task");

        LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        String tableName = tblUserTask;
        String Task_ID = arrTaskIDs.get(position);
        String condition = "Task_ID = " + Task_ID.substring(1);


        // is user a student or lecturer?
        boolean isLec = localDB.isLec();


        // delete task from local database
        // if user is a lecturer then the task must also be deleted from the online database

        if (isLec){
            if (Task_ID.charAt(0) == 'L'){

                if (!isOnline(context)){
                    ec.ShowUserError("Connect to the internet in order to save changes",context);
                    return;
                }

                tableName = tblLocalLecTask;

                Log("isLec and if sCourseCode is NOT NULL about to delete from localDB");
                localDB.doDelete(tableName, condition);

                Log("isLec and about to delete from onlineDB");
                onlineDB.delete_task_taskid(Task_ID.substring(1));
            }
            else{
                Log("isLec and if sCourseCode IS NULL about to delete from localDB");
                localDB.doDelete(tableName, condition);
            }

        }
        else{

            if (Task_ID.charAt(0) == 'L'){

                tableName = tblLocalLecTask;
                String setting = "isDone = 1";

                Log("isStudent, set isDone from Lecturer localDB");
                localDB.doUpdate(tableName, setting, condition);
            }
            else {

                Log("isStudent, delete from localDB");
                localDB.doDelete(tableName, condition);
            }

        }

        // delete from arrays
        arrTaskIDs.remove(position);
        arrTaskNames.remove(position);
        arrTaskCourses.remove(position);
        arrTaskTimes.remove(position);

        // adapter
        Log("Update Adapter");
        this.notifyItemRemoved(position);
        this.notifyDataSetChanged();

        ec.ShowUserMessage("Deleted Task");
    }

}
