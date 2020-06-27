package com.example.lectrac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import static com.example.lectrac.HelperFunctions.Log;

public class TestMarksAdapter extends RecyclerView.Adapter<TestMarksAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> arrName;
    ArrayList<Integer> arrMark;
    ArrayList<Integer> arrTot;
    ArrayList<String> arrCourse;

    public TestMarksAdapter(Context ct, ArrayList<String> arrTestName, ArrayList<Integer> arrTestMark, ArrayList<Integer> arrTestTot, ArrayList<String> arrTestCourse ){

        context = ct;
        arrName = arrTestName;
        arrMark = arrTestMark;
        arrTot = arrTestTot;
        arrCourse = arrTestCourse;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_test_detail, parent, false);
        return new TestMarksAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int got = arrMark.get(position);
        int tot = arrTot.get(position);

        String mark = markJoin(got,tot);
        String perc = percentage(got,tot);

        holder.tvName.setText(arrName.get(position));
        holder.tvMark.setText(mark);
        holder.tvPercent.setText(perc);
        holder.tvCourse.setText(arrCourse.get(position));
    }

    @Override
    public int getItemCount() {
        return arrName.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvName;
        TextView tvMark;
        TextView tvPercent;
        TextView tvCourse;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvTestItemName);
            tvMark = itemView.findViewById(R.id.tvTestItemMark);
            tvPercent = itemView.findViewById(R.id.tvTestItemPercent);
            tvCourse = itemView.findViewById(R.id.tvTestItemCourse);
        }
    }

    public String markJoin(int got, int tot){
        String temp = "";

        temp = Integer.toString(got) + "/" + Integer.toString(tot);

        return temp;
    }

    public String percentage(int got, int tot){
        String temp = "";

        float fperc =  (((float)got/(float)tot) * 100);
        int iperc = (int)fperc;

        temp = Integer.toString(iperc) + "%";

        return temp;
    }
}
