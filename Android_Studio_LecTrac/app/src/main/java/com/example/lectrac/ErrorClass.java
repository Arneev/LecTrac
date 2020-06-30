package com.example.lectrac;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import static com.example.lectrac.HelperFunctions.*;

public class ErrorClass {

    public Context pubCont;
    Boolean isShowing;

    ErrorClass(Context context){
        pubCont = context;
        isShowing = false;
    }

    public void ShowUserError(final String error){
        ((Activity)pubCont).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowUserErrorMain(error,pubCont);
            }
        });
    }

    public void ShowUserError(final String error, final Context context){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowUserErrorMain(error,context);
            }
        });
    }

    //region UserMessage
    public void ShowUserMessage(final String message){
        ((Activity)pubCont).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowUserMessageMain(message,pubCont);
            }
        });
    }

    //region UserMessage
    public void ShowUserMessageWait(final String message, final Class toGoTo){
        ((Activity)pubCont).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowUserMessageMainWait(message,pubCont,toGoTo);
            }
        });
    }

    public void ShowUserMessageWait(final String message,final Context context, final Class toGoTo){
        ((Activity)pubCont).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowUserMessageMainWait(message,context,toGoTo);
            }
        });
    }

    public void ShowUserMessage(final String message, final Context context){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShowUserMessageMain(message,context);
            }
        });
    }

    public void ShowUserMessageMain(String error, Context context){
        if (isShowing){
            return;
        }
        isShowing = true;
        Log(error);
        Log("Supposed to alter dialog");

        android.app.AlertDialog.Builder builder = null;
        String temp = error;

        if (isDarkMode(context)){
            builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);

            builder.setCancelable(true);
            builder.setTitle("Info");
            builder.setMessage(temp);

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isShowing = false;
                    dialogInterface.cancel();

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

            builder.setCancelable(true);
            builder.setTitle("Info");
            builder.setMessage(temp);

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isShowing = false;
                    dialogInterface.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    public void ShowUserErrorMain(String error, Context context){
        if (isShowing){
            return;
        }
        isShowing = true;
        Log(error);
        Log("Supposed to alter dialog");

        android.app.AlertDialog.Builder builder = null;
        String temp = error;
        if (isDarkMode(context)){
            builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);

            builder.setCancelable(true);
            builder.setTitle("Problem");
            builder.setMessage(temp);

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isShowing = false;
                    dialogInterface.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

            builder.setCancelable(true);
            builder.setTitle("Problem");
            builder.setMessage(temp);

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isShowing = false;
                    dialogInterface.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    public void ShowUserMessageMainWait(String error, final Context context, final Class toGoTo){
        if (isShowing){
            return;
        }
        isShowing = true;
        Log(error);
        Log("Supposed to alter dialog");

        android.app.AlertDialog.Builder builder = null;
        String temp = error;
        if (isDarkMode(context)){
            builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK);

            builder.setCancelable(true);
            builder.setTitle("Info");
            builder.setMessage(temp);

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isShowing = false;
                    dialogInterface.cancel();
                    context.startActivity(new Intent(context,toGoTo));

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            builder = new AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

            builder.setCancelable(true);
            builder.setTitle("Info");
            builder.setMessage(temp);

            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isShowing = false;
                    dialogInterface.cancel();
                    context.startActivity(new Intent(context,toGoTo));
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }


}
