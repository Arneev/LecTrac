package com.example.lectrac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lectrac.CourseDetails;

import java.util.ArrayList;

import static com.example.lectrac.HelperFunctions.*;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.MyViewHolder>{

    //region Intialization
    Context context;
    static LocalDatabaseManager localDB = null;

    ArrayList<CourseDetails> courseDetails;
    //endregion

    public CourseListAdapter(Context ct, ArrayList<CourseDetails> courseDetails){
        context = ct;
        this.courseDetails = courseDetails;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_course, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String courseName = courseDetails.get(position).courseName;
        String[] arrLecName = courseDetails.get(position).lecFull;
        String[] arrEmail = courseDetails.get(position).lecEmail;

        //Recycler View
        holder.tvCourse.setText(courseName);

        CourseDetailsAdapter courseDetailsAdapter = new CourseDetailsAdapter(context,arrLecName,arrEmail);
        holder.rvLecDetails.setAdapter(courseDetailsAdapter);
        holder.rvLecDetails.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public int getItemCount() {
        return courseDetails.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourse;
        RecyclerView rvLecDetails;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tvCourseName);
            rvLecDetails = itemView.findViewById(R.id.rvCourseLecDetails);
        }
    }


}
