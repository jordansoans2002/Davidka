package com.example.davidka;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ChangeGridAdapter extends RecyclerView.Adapter<ChangeGridAdapter.ViewHolder> {
    ChangeLayoutActivity changeLayoutActivity;
    List<SpeakButton> buttons;
    MediaPlayer speak;
    MediaRecorder recorder;
    Boolean longPress = false;

    ActivityResultLauncher<PickVisualMediaRequest> getImage;
    ActivityResultLauncher<Intent> pickAudio;
    ActivityResultLauncher<Intent> pickImage;

    public ChangeGridAdapter(ChangeLayoutActivity changeLayoutActivity, ActivityResultLauncher<Intent> pickAudio, ActivityResultLauncher<Intent> pickImage, List<SpeakButton> buttons) {
        this.changeLayoutActivity = changeLayoutActivity;
        this.pickAudio = pickAudio;
        this.pickImage = pickImage;
        this.buttons = buttons;
        for (SpeakButton button : buttons)
            Log.d("table contents", button.position + ". image:" + button.getPicture() + " speech:" + button.getSpeak());
        this.getImage = changeLayoutActivity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);

            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });
    }

    @NonNull
    @Override
    public ChangeGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.spoken_text.setText(ChangeLayoutActivity.buttons.get(position).getSpokenText());
        try {
            Uri image = Uri.parse(ChangeLayoutActivity.buttons.get(position).getPicture());
            if (image != null)
                holder.image.setImageURI(image);
        } catch (SecurityException e) {
            System.err.println("need uri permission at position " + position);
        } catch (Exception e) {

        }

        //TODO update the buttons text every time a key is pressed
        holder.spoken_text.setOnKeyListener((view, keyCode, keyEvent) -> {
            buttons.get(holder.getAdapterPosition()).setSpokenText(holder.spoken_text.getText().toString());
            Log.e("spoken text update", buttons.get(holder.getAdapterPosition()).getSpokenText());
            return true;
        });
//        holder.spoken_text.setOnEditorActionListener(((textView, actionId, keyEvent) -> {
//            buttons.get(holder.getAdapterPosition()).setSpokenText(textView.getText().toString());
//            Log.e("spoken text update",buttons.get(holder.getAdapterPosition()).getSpokenText());
//            return true;
//        }));

        //update the text every time the keyboard is closed or the focus moves from the EditText
//        holder.spoken_text.setOnFocusChangeListener((view, hasFocus) -> {
//            if(hasFocus)
//        });

        holder.change_audio.setOnLongClickListener((View view) -> {
            Log.e("long press status", "long click detected");
            longPress = true;
            ActivityCompat.requestPermissions(changeLayoutActivity, new String[]{Manifest.permission.RECORD_AUDIO}, ChangeLayoutActivity.REQUEST_RECORD_AUDIO_PERMISSION);
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                File file = new File(changeLayoutActivity.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS), dest_uri);
                recorder.setOutputFile(file);
                buttons.get(holder.getAdapterPosition()).setSpeak(Uri.fromFile(file).toString());
            } else {
                File file = new File(changeLayoutActivity.getExternalFilesDir(Environment.DIRECTORY_MUSIC), dest_uri);
                recorder.setOutputFile(file);
                buttons.get(holder.getAdapterPosition()).setSpeak(Uri.fromFile(file).toString());
            }
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e("create recorder", "prepare() failed");
            }
            recorder.start();
            return true;
        });

        holder.change_audio.setOnTouchListener(((view, motionEvent) -> {
            view.onTouchEvent(motionEvent);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && longPress) {
                Log.e("long press status", "long press released");
                recorder.stop();
                recorder.release();
                longPress = false;
            }
            return true;
        }));

        holder.change_audio.setOnClickListener((View view) -> {
            //TODO only allows images, accommodate gif and videos also
            Intent chooseAudio = new Intent();
            chooseAudio.setType("audio/*");
            chooseAudio.setAction(Intent.ACTION_GET_CONTENT);
            chooseAudio.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            chooseAudio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent record = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//            record.putExtra(MediaStore.EXTRA_OUTPUT,ChangeLayoutActivity.destination_uri);
            record.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            Intent chooserIntent = Intent.createChooser(chooseAudio, "Record or select audio");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{record});
            ChangeLayoutActivity.pos = holder.getAdapterPosition();
//            view.getContext().startActivity(intent);
//            editLayoutContext.startActivity(chooserIntent);
            pickAudio.launch(chooserIntent);
        });

        holder.picture.setOnClickListener((View view) -> {
            //TODO only allows images, accommodate gif and videos also
            Intent chooseImage = new Intent();
            chooseImage.setAction(Intent.ACTION_PICK);
            chooseImage.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            Intent clickImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            clickImage.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);
            recordVideo.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

            Intent chooseImgFile = new Intent();
            chooseImgFile.setType("image/*");
            chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

            Intent chooserIntent = Intent.createChooser(chooseImage, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{clickImage, recordVideo, chooseImgFile});
            ChangeLayoutActivity.pos = holder.getAdapterPosition();

            //images and video but cannot click using camera
//            getImage.launch(new PickVisualMediaRequest.Builder()
//                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
//                    .build());

            //only images
            pickImage.launch(chooserIntent);
        });

        holder.audio_control.setOnClickListener((View v) -> {
            try {
                String speakUri = buttons.get(holder.getAdapterPosition()).getSpeak();
                if (changeLayoutActivity.speak != null)
                    changeLayoutActivity.speak.release();
                changeLayoutActivity.speak = MediaPlayer.create(holder.audio_control.getContext(), Uri.parse(speakUri));
//                changeLayoutActivity.speak.setVolume(1, 1);
                changeLayoutActivity.speak.setOnCompletionListener((mediaPlayer -> {
                    changeLayoutActivity.speak.release();
                    holder.seekBar.setProgress(0);
                }));

                if (changeLayoutActivity.speak.isPlaying()) {
                    holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    //to resume use pause
                    changeLayoutActivity.speak.pause();
                    //to restart
//                    speak.release();
//                    holder.seekBar.setProgress(0);
                } else {
                    holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
                    holder.seekBar.setMax(holder.speak.getDuration() / 100);
                    Runnable updateSeekBar = () -> {
                        holder.seekBar.setProgress(holder.speak.getCurrentPosition());
                    };
                    changeLayoutActivity.runOnUiThread(() -> {
                        new Handler().postDelayed(updateSeekBar, 100);
                    });

                    holder.speak.start();
                }
            } catch (SecurityException se) {
                System.err.println("need uri permission at position " + position);
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Audio not available for this button", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                holder.speak.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public int getItemCount() {
        //TODO make the list dynamic
        // when scroll is disabled only 1st 8 will be shown on home page
        return 8;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText spoken_text;
        ImageView audio_control;
        SeekBar seekBar;
        ImageView change_audio;
        CardView picture;
        ImageView image;
        MediaPlayer speak;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spoken_text = itemView.findViewById(R.id.spoken_text);
            audio_control = itemView.findViewById(R.id.audio_control);
            seekBar = itemView.findViewById(R.id.seekBar);
            change_audio = itemView.findViewById(R.id.change_audio);
            picture = itemView.findViewById(R.id.picture);
            image = itemView.findViewById(R.id.image);
        }
    }
}

