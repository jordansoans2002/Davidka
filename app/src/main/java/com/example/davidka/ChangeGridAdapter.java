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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ChangeGridAdapter extends RecyclerView.Adapter<ChangeGridAdapter.ViewHolder> {
    ChangeLayoutActivity changeLayoutActivity;
    MediaRecorder recorder;
    static Boolean longPress = false;

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
        SpeakButton button = changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition());
        if (changeLayoutActivity.preferences.getBoolean("showText", false)) {
            holder.spoken_text.setText(button.getSpokenText());
            holder.spoken_text.setVisibility(View.VISIBLE);
        }else
            holder.spoken_text.setVisibility(View.GONE);

        Handler updateSeekbarHandler = new Handler();
        Runnable updateVideo = new Runnable() {
            @Override
            public void run() {
                long currentPosition = holder.video.getCurrentPosition();
                holder.seekBar.setProgress((int) currentPosition);
                updateSeekbarHandler.postDelayed(this, 100);
            }
        };
        Runnable updateAudio = new Runnable() {
            @Override
            public void run() {
                long currentPosition = holder.speak.getCurrentPosition();
                holder.seekBar.setProgress((int) currentPosition);
                updateSeekbarHandler.postDelayed(this, 100);
            }
        };

        if (button.getPicture() != null && !button.isVideo) {
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setImageURI(Uri.parse(button.getPicture()));
            holder.video.setVisibility(View.GONE);
        } else if (button.getPicture() != null && button.isVideo) {
            holder.video.setVisibility(View.VISIBLE);
            holder.video.setVideoURI(Uri.parse(button.getPicture()));
            holder.video.seekTo(1);
            holder.image.setVisibility(View.GONE);

            holder.video.setOnPreparedListener(mediaPlayer -> {
                holder.seekBar.setProgress(0);
                holder.seekBar.setMax(holder.video.getDuration());
                updateSeekbarHandler.postDelayed(updateVideo, 100);
            });
            holder.video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    holder.video.seekTo(1);
                    holder.seekBar.setProgress(1);
                    holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    updateSeekbarHandler.removeCallbacks(updateVideo);
                }
            });
        }

        holder.change_audio.setOnLongClickListener((View view) -> {
            Log.e("long press status", "long click detected change audio");
            if (changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                Toast.makeText(holder.itemView.getContext(), "Please choose an image to add audio", Toast.LENGTH_SHORT)
                        .show();
            } else {
                longPress = true;
                holder.change_audio.setScaleX(1.2f);
                holder.change_audio.setScaleY(1.2f);
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                String dest_uri = new StringBuilder(UUID.randomUUID().toString()).append(".mp3").toString();
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
            if(MotionEvent.ACTION_UP == motionEvent.getAction())
                Log.e("long press status", "up");
            if(MotionEvent.ACTION_DOWN == motionEvent.getAction())
                Log.e("long press status", "down");

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
            if (changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
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
            AlertBox intentPopup = new AlertBox(changeLayoutActivity, "Choose media type", "Insert image or video");
            ChangeLayoutActivity.pos = holder.getAbsoluteAdapterPosition();

            //images and video but cannot click using camera
//            getImage.launch(new PickVisualMediaRequest.Builder()
//                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
//                    .build());

            intentPopup.intentPopup(pickImage);
        });

        holder.audio_control.setOnClickListener((View v) -> {
            try {
                if (!changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                    String speakUri = changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).getSpeak();
                    if (holder.speak != null)
                        holder.speak.release();
                    Log.e("media player init", speakUri);
                    holder.speak = MediaPlayer.create(holder.audio_control.getContext(), Uri.parse(speakUri));
                    holder.speak.setOnPreparedListener(mediaPlayer -> {
                        holder.seekBar.setProgress(0);
                        holder.seekBar.setMax(holder.speak.getDuration());
                        updateSeekbarHandler.postDelayed(updateAudio, 100);
                    });
                    holder.speak.setOnCompletionListener((mediaPlayer -> {
                        holder.speak.release();
                        holder.seekBar.setProgress(0);
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                        updateSeekbarHandler.removeCallbacks(updateAudio);
                    }));

                    if (holder.speak.isPlaying()) {
                        holder.speak.pause();
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                        //to resume use pause
                        //to restart
//                    speak.release();
//                    holder.seekBar.setProgress(0);
                    } else {
                        holder.speak.start();
                        holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
//                        holder.seekBar.setMax(holder.speak.getDuration() / 100);
////                    new Timer().scheduleAtFixedRate(()->{
////                        holder.seekBar.setProgress(holder.speak.getCurrentPosition());
////                    },0,100);
                    }
                } else if (changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                    if (holder.video.isPlaying()) {
                        //to resume use pause
                        holder.video.pause();
                        //to restart
//                        holder.video.stopPlayback();
//                    holder.seekBar.setProgress(0);
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    } else {
                        holder.video.start();
                        holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
//                        holder.seekBar.setMax(holder.video.getDuration() / 100);
//                    new Timer().scheduleAtFixedRate(()->{
//                        holder.seekBar.setProgress(holder.speak.getCurrentPosition());
//                    },0,100);

                    }
                }
            } catch (SecurityException se) {
                System.err.println("need uri permission at position " + position);
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), "Audio not available for this button", Toast.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
            }
        });

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                if (holder.speak != null)
                if (holder.speak != null && b)
                    holder.speak.seekTo(i);
                else if (holder.video != null && b)
                    holder.video.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        holder.editor_button.setOnLongClickListener(view -> {
            Log.e("drag to delete","drag detected in adapter");
            ChangeLayoutActivity.pos = holder.getBindingAdapterPosition();
            return true;
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView editor_button;
        ImageView audio_control;
        SeekBar seekBar;
        Button change_audio;
        CardView picture;
        ImageView image;
        VideoView video;
        MediaPlayer speak;
        EditText spoken_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editor_button = itemView.findViewById(R.id.editor_button);
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

