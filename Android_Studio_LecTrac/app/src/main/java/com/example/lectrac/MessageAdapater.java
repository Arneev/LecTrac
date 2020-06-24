package com.example.lectrac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static com.example.lectrac.HelperFunctions.*;

public class MessageAdapater extends RecyclerView.Adapter<MessageAdapater.MyViewHolder> {

    Context context;
    ArrayList<String> arrHeading;
    ArrayList<String> arrCourseCode;
    ArrayList<String> arrContents;
    ArrayList<String> arrClassification;
    ArrayList<String> arrDate;

    public MessageAdapater(Context ct, ArrayList<String> head, ArrayList<String> courseCode, ArrayList<String> contents, ArrayList<String> classification, ArrayList<String> date){
        context = ct;
        arrHeading = head;
        arrCourseCode = courseCode;
        arrContents = contents;
        arrClassification = classification;
        arrDate = date;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_message, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvHeading.setText(arrHeading.get(position));
        holder.tvDate.setText(arrDate.get(position));
        holder.tvContents.setText(arrContents.get(position));
        holder.tvClassification.setText(arrClassification.get(position));
        holder.tvCourseCode.setText(arrCourseCode.get(position));

    }

    @Override
    public int getItemCount() {
        return arrHeading.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvHeading;
        TextView tvDate;
        TextView tvContents;
        TextView tvClassification;
        TextView tvCourseCode;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHeading = itemView.findViewById(R.id.tvMessageName);
            tvDate = itemView.findViewById(R.id.tvMessageDate);
            tvContents = itemView.findViewById(R.id.tvMessageContents);
            tvClassification = itemView.findViewById(R.id.tvMessageClassification);
            tvCourseCode = itemView.findViewById(R.id.tvMessageCourseCode);
        }
    }


}
