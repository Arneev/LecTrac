package com.example.lectrac;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import static com.example.lectrac.HelperFunctions.Log;
import static com.example.lectrac.HelperFunctions.ShowUserError;
import static com.example.lectrac.HelperFunctions.errorLecNoCourse;
import static com.example.lectrac.HelperFunctions.quote;
import static com.example.lectrac.HelperFunctions.tblLocalLecTask;
import static com.example.lectrac.HelperFunctions.tblTask;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    Context context;
    Boolean isLec;
    ArrayList<String> arrTaskNames = new ArrayList<String>();
    ArrayList<String> arrTaskCourses = new ArrayList<String>();
    ArrayList<String> arrTaskIDs = new ArrayList<String>();

    public static OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

    RecyclerView recyclerView;


    public ToDoAdapter(Context cont, Boolean blnLec, ArrayList<String> names, ArrayList<String> courses, ArrayList<String> ids){

        context = cont;
        isLec = blnLec;
        arrTaskNames = names;
        arrTaskCourses = courses;
        arrTaskIDs = ids;
    }

    public ToDoAdapter(String name, String course, String id){

        Log("Add new task to Adapter");
        arrTaskNames.add(name);
        arrTaskCourses.add(course);
        arrTaskIDs.add(id);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_todo, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.myText1.setText(arrTaskNames.get(position));
        holder.myText2.setText(arrTaskCourses.get(position));

        // A student cannot edit a task that is posted by a lecturer
        // so, we need two options, to check if the task can be edited by the user or not.


        holder.ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, holder.ivOptions);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) { // check for item and act accordingly
                            case R.id.mtEditTask:

                                Log("about to start");
                                Intent intent = new Intent(context, EditTaskActivity.class);
                                intent.putExtra("position", position);
                                intent.putExtra("arrTaskNames", arrTaskNames);
                                intent.putExtra("arrTaskCourses", arrTaskCourses);
                                intent.putExtra("arrTaskIDs", arrTaskIDs);
                                context.startActivity(intent);
                                break;

                            case R.id.mtDeleteTask:
                                deleteTask(position);
                                break;
                        }
                        return false;
                    }
                });


                popup.inflate(R.menu.task_menu);
                popup.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrTaskNames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView myText1, myText2;
        ImageView ivOptions;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            myText1 = itemView.findViewById(R.id.tvTaskName);
            myText2 = itemView.findViewById(R.id.tvTaskCourseName);
            ivOptions = itemView.findViewById(R.id.ivOptions);
        }
    }

    public void deleteTask(int position){

        Log("About to delete task");

         LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        String tableName = "USER_TASK";
        String Task_ID = arrTaskIDs.get(position);
        String condition = "Task_ID = " + Task_ID.charAt(1);


        // is user a student or lecturer?
        boolean isLec = localDB.isLec();
        Log("isLec about to return " + Boolean.toString(isLec));

        String sCourseCode = arrTaskCourses.get(position);

        // delete task from local database
        // if user is a lecturer then the task must also be deleted from the online database

        if (isLec){
            if (!sCourseCode.equals("None")){

                tableName = tblLocalLecTask;

                Log("isLec and if sCourseCode is NOT NULL about to delete from localDB");
                localDB.doDelete(tableName, condition);
            }
            else{
                Log("isLec and if sCourseCode IS NULL about to delete from localDB");
                localDB.doDelete(tableName, condition);
            }

            Log("isLec and about to delete from onlineDB");
            onlineDB.Delete("TASK", condition);


        }
        else{
            Log("isStudent, delete from localDB");
            localDB.doDelete(tableName, condition);

        }

        // delete from arrays
        arrTaskIDs.remove(position);
        arrTaskNames.remove(position);
        arrTaskCourses.remove(position);

        // adapter
        Log("Update Adapter");
        this.notifyItemRemoved(position);
        this.notifyDataSetChanged();

        Log("Task deleted");
    }

    public void editTask(int pos, String name, String course){

        Log("Edit task in Adapter");

        arrTaskNames.set(pos, name);
        arrTaskCourses.set(pos, course);

        notifyDataSetChanged();
    }

}
