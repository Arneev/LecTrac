package com.example.lectrac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.lectrac.HelperFunctions.*;

public class MessageAdapater extends RecyclerView.Adapter<MessageAdapater.MyViewHolder> {

    Context context;
    ArrayList<String> arrHeading;
    ArrayList<String> arrCourseCode;
    ArrayList<String> arrContents;
    ArrayList<String> arrClassification;
    ArrayList<String> arrDate;
    ArrayList<Integer> arrMsgId;
    Boolean isLec;
    static ErrorClass ec;

    static LocalDatabaseManager localDB;
    static OnlineDatabaseManager onlineDB;

    public MessageAdapater(Context ct, ArrayList<String> head, ArrayList<String> courseCode, ArrayList<String> contents, ArrayList<String> classification, ArrayList<String> date, ArrayList<Integer> id) throws ParseException {
        context = ct;
        arrHeading = head;
        arrCourseCode = courseCode;
        arrContents = contents;
        arrClassification = classification;
        arrMsgId = id;
        arrDate = dateConverter(ddMMMyyyy,yyyyMMdd,date);

        localDB = new LocalDatabaseManager(context);
        onlineDB = new OnlineDatabaseManager();

        isLec = localDB.isLec();
        ec = new ErrorClass(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_message, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.tvHeading.setText(arrHeading.get(position));
        holder.tvDate.setText(arrDate.get(position));
        holder.tvContents.setText(arrContents.get(position));
        holder.tvClassification.setText(arrClassification.get(position));
        holder.tvCourseCode.setText(arrCourseCode.get(position));


        //region DeleteButton

        holder.btnDeleteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log("About to delete");
                deleteMessage(position);
            }
        });

        //endregion
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
        Button btnDeleteMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHeading = itemView.findViewById(R.id.tvMessageName);
            tvDate = itemView.findViewById(R.id.tvMessageDate);
            tvContents = itemView.findViewById(R.id.tvMessageContents);
            tvClassification = itemView.findViewById(R.id.tvMessageClassification);
            tvCourseCode = itemView.findViewById(R.id.tvMessageCourseCode);
            btnDeleteMessage = itemView.findViewById(R.id.btnMessageDelete);
        }
    }

    //region HelperFunctions
    public ArrayList<String> dateConverter (SimpleDateFormat newSdf,SimpleDateFormat oldSdf, ArrayList<String> arr) throws ParseException {
        int size = arr.size();
        ArrayList<String> newArr = new ArrayList<>();

        for (int i = 0; i < size; i++){
            String oldDate = arr.get(i);
            Date oldDateD = oldSdf.parse(oldDate);
            String newDate = newSdf.format(oldDateD);
            newArr.add(newDate);
        }

        return newArr;
    }

    public void deleteMessage(int position){

        Log("About to delete message");

        String tableName = tblMessage;
        int Message_ID = arrMsgId.get(position);
        String condition = "Message_ID = " + Message_ID;


        // delete message from local database
        // if user is a lecturer then the message must also be deleted from the online database

        if (isLec){

            if (!isOnline(context)){
                ec.ShowUserError("Cannot delete task as you are offline",context);
                return;
            }

            try{
                onlineDB.Delete(tblMessage, condition);
            }
            catch(Exception e){
                Log(e.toString());
                ec.ShowUserError("Failed to delete message, please try again",context);
                return;
            }

            localDB.doDelete(tblMessage,condition);

        }
        else{

            String setting = "Message_isDeleted = 1";

            localDB.doUpdate(tblMessage, setting, condition);

        }

        // delete from arrays
        arrHeading.remove(position);
        arrCourseCode.remove(position);
        arrContents.remove(position);
        arrClassification.remove(position);
        arrDate.remove(position);
        arrMsgId.remove(position);


        Log("Updating Message Adapter");
        this.notifyItemRemoved(position);
        this.notifyDataSetChanged();

        Log("Message deleted");

    }

    //endregion
}
