package com.example.lectrac;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> arrTaskNames;
    ArrayList<String> arrTaskCourses;
    ArrayList<String> arrTaskIDs;


    public ToDoAdapter(Context cont, ArrayList<String> names, ArrayList<String> courses, ArrayList<String> ids){
        context = cont;
        arrTaskNames = names;
        arrTaskCourses = courses;
        arrTaskIDs = ids;
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
        holder.ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(context, holder.ivOptions);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){ // check for item and act accordingly
                            case R.id.mtUpdateTask:

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

        LocalDatabaseManager localDatabaseManager = new LocalDatabaseManager(context);

        String tableName = "tblUSER_TASK";
        String Task_ID = arrTaskIDs.get(position);
        String condition = "Task_ID = " + Task_ID;

        localDatabaseManager.doDelete(tableName, condition);


        boolean isLec = localDatabaseManager.isLec();

        if (isLec){

        }


    }
}
