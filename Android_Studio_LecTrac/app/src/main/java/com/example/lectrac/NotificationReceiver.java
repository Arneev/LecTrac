package com.example.lectrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.lectrac.HelperFunctions.*;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.createNotification();

    }


}