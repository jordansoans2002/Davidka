package com.example.davidka;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ChangeGridAdapter extends RecyclerView.Adapter<ChangeGridAdapter.ViewHolder> {
    ChangeLayoutActivity changeLayoutActivity;
//    List<SpeakButton> changeLayoutActivitybuttons;
    MediaPlayer speak;
    MediaRecorder recorder;
    Boolean longPress = false;

    VideoView videoView;
    SeekBar seekBar;
    Handler updateVideoHandler = new Handler();
    Runnable updateVideo = new Runnable() {
        @Override
        public void run() {
            long currentPosition = videoView.getCurrentPosition();
            seekBar.setProgress((int) currentPosition);
            updateVideoHandler.postDelayed(this, 100);
        }
    };

    ActivityResultLauncher<PickVisualMediaRequest> getImage;
    ActivityResultLauncher<Intent> pickAudio;
    ActivityResultLauncher<Intent> pickImage;

    public ChangeGridAdapter(ChangeLayoutActivity changeLayoutActivity, ActivityResultLauncher<Intent> pickAudio, ActivityResultLauncher<Intent> pickImage) {
        this.changeLayoutActivity = changeLayoutActivity;
        this.pickAudio = pickAudio;
        this.pickImage = pickImage;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        videoView = holder.video;
        seekBar = holder.seekBar;

        SpeakButton button = changeLayoutActivity.buttons.get(holder.getAdapterPosition());
        if(changeLayoutActivity.preferences.getBoolean("showText",false))
            holder.spoken_text.setText(button.getSpokenText());
        else
            holder.spoken_text.setVisibility(View.GONE);
        try {
            Uri image = Uri.parse(button.getPicture());
            if (image != null && !button.isVideo) {
                holder.image.setVisibility(View.VISIBLE);
                holder.image.setImageURI(image);
                holder.video.setVisibility(View.GONE);
            } else if (image != null && button.isVideo) {
                holder.video.setVisibility(View.VISIBLE);
                holder.video.setVideoURI(image);
                holder.video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        holder.video.seekTo(0);
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    }
                });
                holder.image.setVisibility(View.GONE);
            }
        } catch (SecurityException e) {
            System.err.println("need uri permission at position " + position);
        } catch (Exception e) {

        }

        holder.change_audio.setOnLongClickListener((View view) -> {
            if (changeLayoutActivity.buttons.get(holder.getAdapterPosition()).isVideo) {
                Toast.makeText(holder.itemView.getContext(), "Please choose an image to add audio", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Log.e("long press status", "long click detected");
                longPress = true;
                holder.change_audio.setScaleX(1.2f);
                holder.change_audio.setScaleY(1.2f);
                ActivityCompat.requestPermissions(changeLayoutActivity, new String[]{Manifest.permission.RECORD_AUDIO}, ChangeLayoutActivity.REQUEST_RECORD_AUDIO_PERMISSION);
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    File file = new File(changeLayoutActivity.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS), dest_uri);
                    recorder.setOutputFile(file);
                    changeLayoutActivity.buttons.get(holder.getAdapterPosition()).setSpeak(Uri.fromFile(file).toString());
                } else {
                    File file = new File(changeLayoutActivity.getExternalFilesDir(Environment.DIRECTORY_MUSIC), dest_uri);
                    recorder.setOutputFile(file);
                    changeLayoutActivity.buttons.get(holder.getAdapterPosition()).setSpeak(Uri.fromFile(file).toString());
                }
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                try {
                    recorder.prepare();
                } catch (IOException e) {
                    Log.e("create recorder", "prepare() failed");
                }
                recorder.start();
            }
            return true;
        });

        holder.change_audio.setOnTouchListener((@SuppressLint("ClickableViewAccessibility") View view, MotionEvent motionEvent) -> {
            view.onTouchEvent(motionEvent);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && longPress) {
                Log.e("long press status", "long press released");
                recorder.stop();
                recorder.release();
                holder.change_audio.setScaleX(1f);
                holder.change_audio.setScaleY(1f);
                longPress = false;
            }
            return true;
        });

        holder.change_audio.setOnClickListener((View view) -> {
            if (changeLayoutActivity.buttons.get(holder.getAdapterPosition()).isVideo) {
                Toast.makeText(holder.itemView.getContext(), "Please choose an image to add audio", Toast.LENGTH_SHORT)
                        .show();
            } else
                Toast.makeText(holder.itemView.getContext(), "Press and hold to record", Toast.LENGTH_SHORT)
                        .show();
            //TODO make the audio file selected to persist
//            Intent chooseAudio = new Intent();
//            chooseAudio.setType("audio/*");
//            chooseAudio.setAction(Intent.ACTION_GET_CONTENT);
//            chooseAudio.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//            chooseAudio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            Intent record = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
//            ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new ContentValues());
////            record.putExtra(MediaStore.EXTRA_OUTPUT,ChangeLayoutActivity.destination_uri);
//            record.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//
//            Intent chooserIntent = Intent.createChooser(chooseAudio, "Record or select audio");
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{record});
//            ChangeLayoutActivity.pos = holder.getAdapterPosition();
////            view.getContext().startActivity(intent);
////            editLayoutContext.startActivity(chooserIntent);
//            pickAudio.launch(chooserIntent);
        });

        holder.picture.setOnClickListener((View view) -> {
            Toast.makeText(holder.picture.getContext(), "Long press to take a video", Toast.LENGTH_SHORT)
                    .show();

            Intent clickImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            clickImage.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

            Intent chooseImage = new Intent();
            chooseImage.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            chooseImage.setType("image/*,video/*");
//            chooseImage.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});

            Intent chooseImgFile = new Intent();
            chooseImgFile.setType("*/*");
            chooseImgFile.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

            Intent chooserIntent = Intent.createChooser(chooseImage, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{clickImage, chooseImgFile});
            ChangeLayoutActivity.pos = holder.getAdapterPosition();

            //images and video but cannot click using camera
//            getImage.launch(new PickVisualMediaRequest.Builder()
//                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
//                    .build());

            pickImage.launch(chooserIntent);
        });

        holder.picture.setOnLongClickListener((view) -> {
            Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//            ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//            recordVideo.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

            Intent chooseImage = new Intent();
            chooseImage.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            chooseImage.setType("video/*");
//            chooseImage.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});

            Intent chooseImgFile = new Intent();
            chooseImgFile.setType("video/*");
            chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

            Intent chooserIntent = Intent.createChooser(chooseImage, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{recordVideo, chooseImgFile});
            ChangeLayoutActivity.pos = holder.getAdapterPosition();

            //ceates fatal exception if no image selected
            pickImage.launch(chooserIntent);
            return true;
        });

        holder.audio_control.setOnClickListener((View v) -> {
            try {
                if (!changeLayoutActivity.buttons.get(holder.getAdapterPosition()).isVideo) {
                    String speakUri = changeLayoutActivity.buttons.get(holder.getAdapterPosition()).getSpeak();
                    if (changeLayoutActivity.speak != null)
                        changeLayoutActivity.speak.release();
                    changeLayoutActivity.speak = MediaPlayer.create(holder.audio_control.getContext(), Uri.parse(speakUri));
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
//                        holder.seekBar.setMax(holder.speak.getDuration() / 100);
////                    new Timer().scheduleAtFixedRate(()->{
////                        holder.seekBar.setProgress(holder.speak.getCurrentPosition());
////                    },0,100);
//                        Runnable updateSeekBar = () -> {
//                            holder.seekBar.setProgress(holder.speak.getCurrentPosition());
//                        };
//                        changeLayoutActivity.runOnUiThread(() -> {
//                            holder.seekBar.postDelayed(updateSeekBar, 100);
//                        });

                        holder.speak.start();
                    }
                } else if (changeLayoutActivity.buttons.get(holder.getAdapterPosition()).isVideo) {
                    if (holder.video.isPlaying()) {
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                        //to resume use pause
                        holder.video.pause();
                        //to restart
//                        holder.video.stopPlayback();
//                    holder.seekBar.setProgress(0);
                    } else {
                        holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
//                        holder.seekBar.setMax(holder.video.getDuration() / 100);
//                    new Timer().scheduleAtFixedRate(()->{
//                        holder.seekBar.setProgress(holder.speak.getCurrentPosition());
//                    },0,100);
//                        Runnable updateSeekBar = () -> {
//                            holder.seekBar.setProgress(holder.video.getCurrentPosition());
//                        };
//                        changeLayoutActivity.runOnUiThread(() -> {
//                            holder.seekBar.postDelayed(updateSeekBar, 100);
//                        });

                        holder.video.start();

                    }
                }
            } catch (SecurityException se) {
                System.err.println("need uri permission at position " + position);
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Audio not available for this button", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        holder.video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                holder.seekBar.setProgress(0);
                holder.seekBar.setMax(holder.video.getDuration());
                updateVideoHandler.postDelayed(updateVideo, 100);
            }
        });
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                if (holder.speak != null)
                if (holder.speak != null && b)
                    holder.speak.seekTo(i);
                else if(holder.video != null && b)
                    holder.video.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        holder.spoken_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeLayoutActivity.buttons.get(holder.getAdapterPosition()).setSpokenText(holder.spoken_text.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return changeLayoutActivity.buttons.size();
    }

    //trying to nest intents
    Intent createIntent(){
        Intent clickImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        clickImage.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

        Intent chooseImage = new Intent();
        chooseImage.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooseImage.setType("image/*");
//            chooseImage.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});

        Intent chooseImgFile = new Intent();
        chooseImgFile.setType("image/*");
        chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

        Intent imgChooserIntent = Intent.createChooser(chooseImage, "Take or select image");
        imgChooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{clickImage, chooseImgFile});


        Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        ChangeLayoutActivity.temp_uri = changeLayoutActivity.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        recordVideo.putExtra(MediaStore.EXTRA_OUTPUT, ChangeLayoutActivity.temp_uri);

        Intent chooseVideo = new Intent();
        chooseImage.setAction(Intent.ACTION_PICK);
//            chooseImage.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        chooseImage.setType("video/*");
//            chooseImage.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});

        Intent chooseVidFile = new Intent();
        chooseImgFile.setType("video/*");
        chooseImgFile.setAction(Intent.ACTION_GET_CONTENT);

        Intent vidChooserIntent = Intent.createChooser(chooseVideo, "Take or select video");
        vidChooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{recordVideo, chooseVidFile});

        Intent chooserIntent = Intent.createChooser(imgChooserIntent,"Insert image or video");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{vidChooserIntent});

        return chooserIntent;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText spoken_text;
        ImageView audio_control;
        SeekBar seekBar;
        Button change_audio;
        CardView picture;
        ImageView image;
        VideoView video;
        MediaPlayer speak;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spoken_text = itemView.findViewById(R.id.spoken_text);
            audio_control = itemView.findViewById(R.id.audio_control);
            seekBar = itemView.findViewById(R.id.seekBar);
            change_audio = itemView.findViewById(R.id.change_audio);
            picture = itemView.findViewById(R.id.picture);
            image = itemView.findViewById(R.id.image);
            video = itemView.findViewById(R.id.video);
        }
    }
}

