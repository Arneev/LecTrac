package com.example.lectrac;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.lectrac.HelperFunctions.Log;
import static com.example.lectrac.HelperFunctions.errorLecNoCourse;
import static com.example.lectrac.HelperFunctions.isOnline;
import static com.example.lectrac.HelperFunctions.quote;
import static com.example.lectrac.HelperFunctions.tblLocalLecTask;
import static com.example.lectrac.HelperFunctions.tblTask;
import static com.example.lectrac.HelperFunctions.tblUserTask;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> { //implements Filterable

    //region Intialization

    static ErrorClass ec;
    Context context;
    Boolean isLec;
    ArrayList<String> arrTaskNames = new ArrayList<String>();
    ArrayList<String> arrTaskCourses = new ArrayList<String>();
    ArrayList<String> arrTaskIDs = new ArrayList<String>();
    ArrayList<String> arrFilteredCourses = new ArrayList<String>();

    public static OnlineDatabaseManager onlineDB = new OnlineDatabaseManager();

    //endregion

    public ToDoAdapter(Context cont, Boolean blnLec, ArrayList<String> names, ArrayList<String> courses, ArrayList<String> ids){
        context = cont;
        ec = new ErrorClass(context);
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

        final String taskID = arrTaskIDs.get(position);
        LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        holder.myText1.setText(arrTaskNames.get(position));
        holder.myText2.setText(arrTaskCourses.get(position));

        if (isLec){

            // this text view is set to italic, and the width is wrap_contents. So:
            // android:layout_width="wrap_content" , gives you a rectangle for wrapped content.
            // All will work well for normal text (non-italic).
            // Solution as suggested is to have a space at the end of the text

            if (taskID.charAt(0) == 'L'){
                holder.myText3.setText("Posted ");
            }
            else {
                holder.myText3.setText("Not Posted ");
            }
        }
        else {

            if (taskID.charAt(0) == 'L'){
                holder.myText3.setText("Course Task ");
            }
            else {
                holder.myText3.setText("You ");
            }
        }

        // A student cannot edit a task that is posted by a lecturer
        // so, we need two options, to check if the task can be edited by the user or not.


        holder.ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isLec && taskID.charAt(0) == 'L') {

                    PopupMenu popup = new PopupMenu(context, holder.ivOptions);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            // check for item and act accordingly
                            if (menuItem.getItemId() == R.id.mtDeleteTask) {
                                try {
                                    deleteTask(position);
                                } catch (InterruptedException e) {
                                    Log(e.toString());
                                }
                            }
                            return false;
                        }
                    });

                    popup.inflate(R.menu.task_menu_no_edit);
                    popup.show();
                }
                else {

                    PopupMenu popup = new PopupMenu(context, holder.ivOptions);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            switch (menuItem.getItemId()) { // check for item and act accordingly
                                case R.id.mtEditTask:

                                    Intent intent = new Intent(context, EditTaskActivity.class);
                                    intent.putExtra("position", position);
                                    intent.putExtra("arrTaskNames", arrTaskNames);
                                    intent.putExtra("arrTaskCourses", arrTaskCourses);
                                    intent.putExtra("arrTaskIDs", arrTaskIDs);
                                    intent.putExtra("Activity", "To-Do List");
                                    context.startActivity(intent);
                                    break;

                                case R.id.mtDeleteTask:
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


                    popup.inflate(R.menu.task_menu);
                    popup.show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrTaskNames.size();
    }

    public int getPosition(String taskID){

        for (int index = 0; index < arrTaskIDs.size(); index++){

            if (taskID.equals(arrTaskIDs.get(index))){

                return index;
            }
        }

        return -1;
    }

 /*   @Override
    public Filter getFilter() {

        Log("getFilter");
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<String> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()){
                filteredList.addAll(arrTaskCourses);
            }
            else {

                for (String course: arrTaskCourses){

                    if (course.contains(constraint.toString())){
                        filteredList.add(course);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            arrFilteredCourses.addAll((Collection<? extends String>) results.values);
            notifyDataSetChanged();
        }
    };*/

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView myText1, myText2, myText3;
        ImageView ivOptions;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            myText1 = itemView.findViewById(R.id.tvTaskName);
            myText2 = itemView.findViewById(R.id.tvTaskCourseName);
            myText3 = itemView.findViewById(R.id.tvExtra);
            ivOptions = itemView.findViewById(R.id.ivOptions);
        }
    }

//    public void deleteTask(int position) throws InterruptedException {
//
//        Log("About to delete task");
//
//        LocalDatabaseManager localDB = new LocalDatabaseManager(context);
//
//        String tableName = tblUserTask;
//        String Task_ID = arrTaskIDs.get(position);
//        String condition = "Task_ID = " + Task_ID.substring(1);
//
//
//        // is user a student or lecturer?
//        boolean isLec = localDB.isLec();
//
//
//        // delete task from local database
//        // if user is a lecturer then the task must also be deleted from the online database
//
//        if (isLec){
//            if (Task_ID.charAt(0) == 'L'){
//
//                if (!isOnline(context)){
//                    ec.ShowUserError("Connect to the internet in order to save changes",context);
//                    return;
//                }
//
//                tableName = tblLocalLecTask;
//
//                Log("isLec and if sCourseCode is NOT NULL about to delete from localDB");
//                localDB.doDelete(tableName, condition);
//
//                Log("isLec and about to delete from onlineDB");
//                onlineDB.delete_task_taskid(Task_ID.substring(1));
//            }
//            else{
//                Log("isLec and if sCourseCode IS NULL about to delete from localDB");
//                localDB.doDelete(tableName, condition);
//            }
//
//        }
//        else{
//
//            if (Task_ID.charAt(0) == 'L'){
//
//                tableName = tblLocalLecTask;
//                String setting = "isDone = 1";
//
//                Log("isStudent, set isDone from Lecturer localDB");
//                localDB.doUpdate(tableName, setting, condition);
//            }
//            else {
//
//                Log("isStudent, delete from localDB");
//                localDB.doDelete(tableName, condition);
//            }
//
//        }
//
//        // delete from arrays
//        arrTaskIDs.remove(position);
//        arrTaskNames.remove(position);
//        arrTaskCourses.remove(position);
//
//        // adapter
//        Log("Update Adapter");
//        this.notifyItemRemoved(position);
//        this.notifyDataSetChanged();
//
//        ec.ShowUserMessage("Deleted Task");
//    }

    public void deleteTask(int position) throws InterruptedException {

        Log("About to delete task");

        LocalDatabaseManager localDB = new LocalDatabaseManager(context);

        String Task_ID = arrTaskIDs.get(position);
        String condition = "Task_ID = " + Task_ID.substring(1);
        boolean isLecTask = false;

        if (Task_ID.charAt(0) == 'L' || Task_ID.charAt(0) == 'l'){
            isLecTask = true;
        }

        if (Task_ID.charAt(0) == 'U' || Task_ID.charAt(0) == 'u'){
            isLecTask = false;
        }


        // is user a student or lecturer?
        boolean isLec = localDB.isLec();


        // delete task from local database
        // if user is a lecturer then the task must also be deleted from the online database

        if (isLec){
            if (isLecTask){

                if (!isOnline(context)){
                    ec.ShowUserError("Connect to the internet in order to save changes",context);
                    return;
                }

                try {
                    onlineDB.delete_task_taskid(Task_ID.substring(1));
                }catch (Exception e){
                    ec.ShowUserError("Failed to delete task");
                    return;
                }

                Log("isLec and if sCourseCode is NOT NULL about to delete from localDB");
                localDB.doDelete(tblLocalLecTask, condition);


            }
            else{
                Log("isLec and if sCourseCode IS NULL about to delete from localDB");
                localDB.doDelete(tblUserTask, condition);
            }

        }
        else{
            if (isLecTask){

                String setting = "isDone = 1";

                Log("isStudent, set isDone from Lecturer localDB");
                localDB.doUpdate(tblLocalLecTask, setting, condition);
            }
            else {

                Log("isStudent, delete from localDB");
                localDB.doDelete(tblUserTask, condition);
            }

        }

        // delete from arrays
        arrTaskIDs.remove(position);
        arrTaskNames.remove(position);
        arrTaskCourses.remove(position);

        // adapter
        Log("Update Adapter");
        this.notifyItemRemoved(position);
        this.notifyDataSetChanged();

        ec.ShowUserMessage("Deleted Task");
    }


}
