package com.example.davidka;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;

import java.io.File;
import java.util.List;

public class AlertBox {

    Activity activity;
    String title, msg;

    Intent chooserIntent;


    public AlertBox(Activity activity, String title, String msg) {
        this.activity = activity;
        this.title = title;
        this.msg = msg;
    }

    //to confirm that user wants to discard changes
    public void discardGridChanges(List<ButtonUpdate> updates) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                        new Thread(() -> {
                            Log.e("delete updates","updates "+updates);
                            for(ButtonUpdate update : updates) {
                                Log.e("delete updates", update.getType()+" "+update.getUri());
                                update.deleteFile(activity.getFilesDir(), MainActivity.buttons);
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

    //to confirm that user wants to save changes
    public void confirmGridChanges(List<SpeakButton> buttons, List<ButtonUpdate> updates) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        activity.finish();
                        new Thread(() -> {
                            //TODO fails if we change the position or delete a button
                            //check if the uri is in database if no then delete
                            DatabaseHelper db = DatabaseHelper.getDB(activity);
                            for(int i=0;i<buttons.size();i++) {
                                SpeakButton newButton = buttons.get(i);
                                if(newButton.isVideo) newButton.setSpeak(null);
                                if(MainActivity.buttons.size()>i) {
                                    SpeakButton oldButton = MainActivity.buttons.get(i);
                                    if (oldButton.getPicture()!=null && (newButton.getPicture()==null || !oldButton.getPicture().equalsIgnoreCase(newButton.getPicture()))) {
                                        if (oldButton.isVideo) {
                                            Log.e("delete old vid", oldButton.getPicture());
                                            new File(oldButton.getPicture())
                                                    .delete();
                                        } else {
                                            Log.e("delete old pic", oldButton.getPicture());
                                            if(oldButton.getPicture() != null)
                                                new File(activity.getFilesDir(),Uri.parse(oldButton.getPicture()).getLastPathSegment())
                                                        .delete();
                                        }
                                    }
                                    if (oldButton.getSpeak()!=null && (newButton.getSpeak()==null || !oldButton.getSpeak().equalsIgnoreCase(newButton.getSpeak()))) {
                                        Log.e("delete old aud", oldButton.getSpeak());
                                        if (oldButton.getSpeak() != null) {
                                            boolean flag = new File(activity.getFilesDir(),Uri.parse(oldButton.getSpeak()).getLastPathSegment())
                                                    .delete();
                                            Log.e("delete old aud", "deleted = "+flag);
                                        }
                                    }
                                }
                                newButton.setPosition(i);
                                db.speakButtonDao().updateSpeakButton(newButton);
                                Log.d("update table contents", newButton.position + ". text"+ newButton.getSpokenText()+ "image:" + newButton.getPicture() + " speech:" + newButton.getSpeak());
                            }
                            for(ButtonUpdate update : updates)
                                update.deleteFile(activity.getFilesDir(),buttons);
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

    public Intent intentPopup(ActivityResultLauncher<Intent> pickImage){
        Dialog chooseIntent = new Dialog(activity);
        chooseIntent.setContentView(R.layout.intent_popup);
        chooseIntent.setCanceledOnTouchOutside(true);
        chooseIntent.setCancelable(true);

        ImageView image = chooseIntent.findViewById(R.id.image_intent);
        ImageView video = chooseIntent.findViewById(R.id.video_intent);

        image.setOnClickListener(view -> {
//            createChooser("image");
            chooseIntent.cancel();
            Intent clickImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ChangeLayoutActivity.temp_uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            clickImage.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

            Intent chooseImage = new Intent();
            chooseImage.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            chooseImage.setType("image/*");
//            chooseImage.setType("image/*,video/*");
//            chooseImage.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});

            Intent chooseImgFile = new Intent();
            chooseImgFile.setType("image/*");
//            chooseImgFile.setType("*/*");
//            chooseImgFile.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

            chooserIntent = Intent.createChooser(chooseImage, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{clickImage, chooseImgFile});
            pickImage.launch(chooserIntent);
        });

        video.setOnClickListener(view -> {
//            createChooser("video");
            Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//            ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//            recordVideo.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

            Intent chooseVideo = new Intent();
            chooseVideo.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            chooseVideo.setType("video/*");
//            chooseImage.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});

            Intent chooseVidFile = new Intent();
            chooseVidFile.setType("video/*");
            chooseVidFile.setAction(Intent.ACTION_GET_CONTENT);

            chooserIntent = Intent.createChooser(chooseVideo, "Take or select video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{recordVideo, chooseVidFile});
            chooseIntent.cancel();
            pickImage.launch(chooserIntent);
        });

        chooseIntent.setTitle(title);
        chooseIntent.show();

        return chooserIntent;
    }

    public void deleteButton(ChangeGridAdapter adapter, List<SpeakButton> buttons, int pos){
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SpeakButton button = buttons.get(pos);
                        if(button.getPicture()!=null)
                            new File(Uri.parse(button.getPicture()).getPath()).delete();
                        if(button.getSpeak()!=null)
                            new File(Uri.parse(button.getSpeak()).getPath()).delete();

                        DatabaseHelper db = DatabaseHelper.getDB(activity);
                        if(buttons.size()>8) {
                            db.speakButtonDao().deleteSpeakButton(buttons.get(pos));
                            buttons.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        } else {
                            button.setPosition(pos);
                            button.setPicture(null,false);
                            button.setSpeak(null);
                            button.setSpokenText("");
                            adapter.notifyItemChanged(pos);
                            db.speakButtonDao().updateSpeakButton(button);

                        }
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