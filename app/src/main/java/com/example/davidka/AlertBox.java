package com.example.davidka;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

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

    //to confirm that user wants to save/discard changes
    public void gridChanges(List<SpeakButton> buttons, boolean save) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog1, id) -> {
                    dialog1.dismiss();
                    dialog1 = null;
                    activity.finish();

                    new Thread(() -> {
                        DatabaseHelper db = DatabaseHelper.getDB(activity);
                        for(int i=0;i< buttons.size();i++){
                            SpeakButton button = buttons.get(i);
                            if(save) {
                                if(button.rootPicture.getNext() != null) {
                                    button.picture = button.leafPicture.getUri();
                                    button.isVideo = button.leafPicture.isVideo;
                                }
                                if(button.rootSpeak.getNext() != null) {
                                    button.speak = button.isVideo? null : button.leafSpeak.getUri();
                                }
                                button.setPosition(i);
                                if(i < buttons.size() - ChangeLayoutActivity.addedButtons)
                                    db.speakButtonDao().updateSpeakButton(button);
                                else
                                    db.speakButtonDao().addSpeakButton(button);
                            }

                            if(button.rootSpeak.getNext() == null && button.rootPicture.getNext() == null)
                                continue;
                            if(button.rootSpeak.getNext() != null)
                                button.deleteUpdates(activity.getFilesDir(), button.rootSpeak,  save);

                            if(button.rootPicture.getNext() != null){
                                if(button.leafSpeak.getUri()!= null && button.isVideo && save)
                                    SpeakButton.deleteFile(activity.getFilesDir(), button.leafSpeak.getUri(), false);
                                if(button.leafSpeak.getUri()!= null && button.isVideo && !save)
                                    SpeakButton.deleteFile(activity.getFilesDir(), button.rootSpeak.getUri(), false);
                                button.deleteUpdates(activity.getFilesDir(), button.rootPicture, save);
                            }
                        }
                        for(int i=buttons.size(); i<buttons.size()+ChangeLayoutActivity.deletedButtons;i++)
                            db.speakButtonDao().deleteButtonByPosition(i);

                        ChangeLayoutActivity.cleanUp = true;
                        ChangeLayoutActivity.addedButtons = 0;
                        ChangeLayoutActivity.deletedButtons = 0;
                    }).start();
                })
                .setNegativeButton("No", (dialog12, id) -> {
                    //  Action for 'NO' Button
                    dialog12.dismiss();
                    dialog12 = null;
                });

        AlertDialog alert = dialog.create();
        alert.show();
    }

    public void intentPopup(ActivityResultLauncher<Intent> pickImage){
        Dialog chooseIntent = new Dialog(activity);
        chooseIntent.setContentView(R.layout.intent_popup);
        chooseIntent.setCanceledOnTouchOutside(true);
        chooseIntent.setCancelable(true);

        ImageView image = chooseIntent.findViewById(R.id.image_intent);
        ImageView video = chooseIntent.findViewById(R.id.video_intent);

        image.setOnClickListener(view -> {
            chooseIntent.dismiss();
            Intent clickImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ChangeLayoutActivity.temp_uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            clickImage.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

            Intent chooseImage = new Intent();
            chooseImage.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            chooseImage.setType("image/*");

            Intent chooseImgFile = new Intent();
            chooseImgFile.setType("image/*");
            chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

            chooserIntent = Intent.createChooser(chooseImage, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{clickImage, chooseImgFile});
            pickImage.launch(chooserIntent);
        });

        video.setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(activity,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                Intent chooseVideo = new Intent();
                chooseVideo.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                chooseVideo.setType("video/*");

                Intent chooseVidFile = new Intent();
                chooseVidFile.setType("video/*");
                chooseVidFile.setAction(Intent.ACTION_GET_CONTENT);

                chooserIntent = Intent.createChooser(chooseVideo, "Take or select video");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{recordVideo, chooseVidFile});
                chooseIntent.dismiss();
                pickImage.launch(chooserIntent);
            } else {
                Toast.makeText(
                        activity,
                        "Please grant storage access to insert a video",
                        Toast.LENGTH_LONG
                ).show();
                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},ChangeLayoutActivity.REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
            }
        });

        chooseIntent.setTitle(title);
        chooseIntent.show();
    }
}