package com.example.lectrac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseDetailsAdapter extends RecyclerView.Adapter<CourseDetailsAdapter.MyViewHolder> {

    //region Intialization
    Context context;
    String[] arrLecName;
    String[] arrEmail;

    public CourseDetailsAdapter(Context ct, String[] lecName, String[] lecEmail){
        context = ct;
        arrLecName = lecName;
        arrEmail = lecEmail;
    }
    //endregion

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_lec_details, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvName.setText(arrLecName[position]);
        holder.tvEmail.setText(arrEmail[position]);
    }

    @Override
    public int getItemCount() {
        return arrEmail.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLecName);
            tvEmail = itemView.findViewById(R.id.tvLecEmail);
        }
    }
}
