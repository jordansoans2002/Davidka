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

    //to confirm that user wants to save/discard changes
    public void gridChanges(List<SpeakButton> buttons,int deletedButtons, boolean save) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                                    Log.e("save buttons","pos updates "+i);
                                    db.speakButtonDao().updateSpeakButton(button);
                                }

                                if(button.rootSpeak.getNext() == null && button.rootPicture.getNext() == null)
                                    continue;
                                if(button.rootSpeak.getNext() != null) {
                                    Log.e("deleting speak list","current "+button.rootSpeak.getUri()+" leaf "+button.leafSpeak.getUri());
                                    button.deleteUpdates(activity.getFilesDir(), button.rootSpeak,  save);
                                }
                                if(button.rootPicture.getNext() != null){
                                    Log.e("deleting picture list","current "+button.rootPicture.getUri()+" leaf "+button.leafPicture.getUri());
                                    if(button.leafSpeak.getUri()!= null && button.isVideo && save)
                                        SpeakButton.deleteFile(activity.getFilesDir(), button.leafSpeak.getUri(), false);
                                    if(button.leafSpeak.getUri()!= null && button.isVideo && !save)
                                        SpeakButton.deleteFile(activity.getFilesDir(), button.rootSpeak.getUri(), false);
                                    button.deleteUpdates(activity.getFilesDir(), button.rootPicture, save);
                                }
                            }
                            for(int i=buttons.size(); i<buttons.size()+deletedButtons;i++)
                                db.speakButtonDao().deleteButtonByPosition(i);
                            for (SpeakButton button : buttons)
                                Log.d("table contents", button.position + ". vid? " + button.isVideo + " image:" + button.picture + " speech:" + button.speak);
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

    //buttons are deleted after the user saves changes
    public void deleteButton(ChangeGridAdapter adapter, List<SpeakButton> buttons, int pos){
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        //Setting message manually and performing action on button click can set text from string.xml also
        dialog.setMessage(msg).setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SpeakButton button = buttons.get(pos);

                        if(button.rootSpeak.getUri()!=null)
                            SpeakButton.deleteFile(activity.getFilesDir(), button.rootSpeak.getUri(), false);
                        if(button.rootSpeak != button.leafSpeak)
                            button.deleteUpdates(activity.getFilesDir(),button.rootSpeak,false);

                        if(button.rootPicture.getUri()!=null)
                            SpeakButton.deleteFile(activity.getFilesDir(), button.rootPicture.getUri(), button.rootPicture.isVideo);
                        if(button.rootPicture != button.leafPicture)
                            button.deleteUpdates(activity.getFilesDir(),button.rootPicture,false);

                        DatabaseHelper db = DatabaseHelper.getDB(activity);
                        if(buttons.size()>8) {
                            db.speakButtonDao().deleteSpeakButton(buttons.get(pos));
                            buttons.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        } else {
                            button = new SpeakButton(pos,null,null,"",false);
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

    public void intentPopup(ActivityResultLauncher<Intent> pickImage){
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
    }
}