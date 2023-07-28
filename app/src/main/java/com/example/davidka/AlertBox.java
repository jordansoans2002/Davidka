package com.example.davidka;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;

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

    public Intent intentPopup(ActivityResultLauncher<Intent> pickImage){
        Dialog chooseIntent = new Dialog(activity);
        chooseIntent.setContentView(R.layout.intent_popup);
        chooseIntent.setCanceledOnTouchOutside(true);
        chooseIntent.setCancelable(true);

        ImageView image = chooseIntent.findViewById(R.id.image_intent);
        ImageView video = chooseIntent.findViewById(R.id.video_intent);

        image.setOnClickListener(view -> {
            createChooser("image");
            chooseIntent.cancel();
            pickImage.launch(chooserIntent);
        });

        video.setOnClickListener(view -> {
            createChooser("video");
            chooseIntent.cancel();
            pickImage.launch(chooserIntent);
        });

        chooseIntent.setTitle(title);
        chooseIntent.show();

        return chooserIntent;
    }

    void createChooser(String type){
        if(type.equalsIgnoreCase("image")){
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
        } else if(type.equalsIgnoreCase("video")){
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

            chooserIntent = Intent.createChooser(chooseVideo, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{recordVideo, chooseVidFile});
        }
    }

}
