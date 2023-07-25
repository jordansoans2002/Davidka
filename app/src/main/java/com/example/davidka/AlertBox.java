package com.example.davidka;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import java.util.List;

public class AlertBox {

    Activity activity;
    String title, msg;


    public AlertBox(Activity activity, String title, String msg) {
        this.activity = activity;
        this.title = title;
        this.msg = msg;
    }

    //to confirm that user wants to discard changes
    public void createAlertBox() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        AlertDialog alert = dialog.create();
        alert.show();
    }

    //to confirm that user wants to save changes
    public void createAlertBox(List<SpeakButton> buttons) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                        new Thread(() -> {
                            DatabaseHelper db = DatabaseHelper.getDB(activity);
                            //TODO when a new button is added we need to add it to database not update
                            //count database  and update till there rest add
                            for(SpeakButton button:buttons) {
                                db.speakButtonDao().updateSpeakButton(button);
                                Log.d("table contents", button.position + ". text"+ button.getSpokenText()+ "image:" + button.picture + " speech:" + button.speak);
                            }
                        }).start();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        AlertDialog alert = dialog.create();
        alert.show();
    }

}
