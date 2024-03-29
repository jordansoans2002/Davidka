package com.example.davidka;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.Manifest;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.DragEvent;
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
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class ChangeGridAdapter extends RecyclerView.Adapter<ChangeGridAdapter.ViewHolder> {
    ChangeLayoutActivity changeLayoutActivity;
    MediaRecorder recorder;
    static final int MAX_AUD_DURATION = 10000;
    static Boolean longPress = false;

    ActivityResultLauncher<Intent> pickAudio;
    ActivityResultLauncher<Intent> pickImage;

    public ChangeGridAdapter(ChangeLayoutActivity changeLayoutActivity, ActivityResultLauncher<Intent> pickImage) {
        this.changeLayoutActivity = changeLayoutActivity;
        this.pickImage = pickImage;
    }

    //TODO responsive image
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
        } else
            holder.spoken_text.setVisibility(View.GONE);

        Handler updateSeekbarHandler = new Handler(Looper.getMainLooper());
        Runnable updateVideo = new Runnable() {
            @Override
            public void run() {
                if (holder.video != null) {
                    long currentPosition = holder.video.getCurrentPosition();
                    holder.seekBar.setProgress((int) currentPosition);
                    updateSeekbarHandler.postDelayed(this, 100);
                }
            }
        };
        Runnable updateAudio = new Runnable() {
            @Override
            public void run() {
                if (holder.speak != null) {
                    long currentPosition = holder.speak.getCurrentPosition();
                    holder.seekBar.setProgress((int) currentPosition);
                    updateSeekbarHandler.postDelayed(this, 100);
                }
            }
        };

        try {
            boolean exists = new File(Objects.requireNonNull(Uri.parse(button.getPicture()).getPath())).exists();
            if (exists) {
                if (!button.isVideo) {
                    holder.image.setImageURI(Uri.parse(button.getPicture()));
                    holder.image.setVisibility(View.VISIBLE);
                    holder.video.setVisibility(View.GONE);
                } else {
                    holder.video.setVisibility(View.VISIBLE);
                    holder.video.setVideoURI(Uri.parse(button.getPicture()));
                    holder.image.setVisibility(View.GONE);

                    holder.video.setOnPreparedListener(mediaPlayer -> {
                        holder.seekBar.setProgress(0);
                        holder.video.seekTo(1);
                        holder.seekBar.setMax(holder.video.getDuration());
                        updateSeekbarHandler.postDelayed(updateVideo, 100);
                    });
                    holder.video.setOnCompletionListener(mediaPlayer -> {
                        holder.video.seekTo(1);
                        holder.seekBar.setProgress(1);
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    });
                }
            } else {
                Toast.makeText(
                        holder.itemView.getContext(),
                        "A media file was not found",
                        Toast.LENGTH_LONG
                ).show();
                throw new NullPointerException();
            }
        } catch (NullPointerException nullPointerException) {
            holder.image.setVisibility(View.VISIBLE);
            holder.video.setVisibility(View.GONE);
        }

        //TODO tone to indicate record start? and end
        holder.change_audio.setOnLongClickListener((View view) -> {
            if (ContextCompat.checkSelfPermission(changeLayoutActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
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

                    String dest_uri = UUID.randomUUID().toString() + ".mp3";
                    File file = new File(changeLayoutActivity.getFilesDir(), dest_uri);
                    recorder.setOutputFile(file);
                    changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).setSpeak(Uri.fromFile(file).toString());
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                    recorder.setMaxDuration(MAX_AUD_DURATION);
                    recorder.setOnInfoListener((mr, what, extra) -> {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                            recorder.release();
                            holder.change_audio.setScaleX(1f);
                            holder.change_audio.setScaleY(1f);
                            longPress = false;
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Maximum audio length is 10 seconds",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                    try {
                        recorder.prepare();
                    } catch (IOException e) {
                        Toast.makeText(
                                changeLayoutActivity,
                                "Pleases try recording again",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    recorder.start();
                }
            } else {
                Toast.makeText(
                        changeLayoutActivity,
                        "Please allow permission to use microphone to record audio",
                        Toast.LENGTH_LONG
                ).show();
                changeLayoutActivity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, ChangeLayoutActivity.REQUEST_RECORD_AUDIO_PERMISSION);
            }
            return true;
        });

        holder.change_audio.setOnTouchListener((@SuppressLint("ClickableViewAccessibility") View view, MotionEvent motionEvent) -> {
            view.onTouchEvent(motionEvent);
            if ((motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) && longPress) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (RuntimeException runtimeException) {
                    Toast.makeText(
                            holder.itemView.getContext(),
                            "No audio was recorded",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                holder.change_audio.setScaleX(1f);
                holder.change_audio.setScaleY(1f);
                longPress = false;
            }
            return true;
        });

        holder.change_audio.setOnClickListener((View view) -> {
            if (changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                Toast.makeText(
                        holder.itemView.getContext(),
                        "Please choose an image to add audio",
                        Toast.LENGTH_SHORT
                ).show();
            } else
                Toast.makeText(
                        holder.itemView.getContext(),
                        "Press and hold to record",
                        Toast.LENGTH_SHORT
                ).show();

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
            intentPopup.intentPopup(pickImage);
        });

        holder.audio_control.setOnClickListener((View v) -> {
            try {
                if (!changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                    String speakUri = changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).getSpeak();
                    if (holder.speak != null && holder.speak.isPlaying()) {
                        holder.speak.pause();
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    } else if (holder.speak == null) {

                        holder.speak = MediaPlayer.create(holder.audio_control.getContext(), Uri.parse(speakUri));
                        holder.speak.setOnPreparedListener(mediaPlayer -> {
                            holder.seekBar.setProgress(0);
                            holder.seekBar.setMax(holder.speak.getDuration());
                            updateSeekbarHandler.postDelayed(updateAudio, 100);
                        });
                        holder.speak.setOnCompletionListener((mediaPlayer -> {
                            holder.speak.release();
                            holder.speak = null;
                            holder.seekBar.setProgress(0);
                            holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                        }));

                        holder.speak.start();
                        holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
                    } else {
                        holder.speak.start();
                        holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
                    }
                } else if (changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                    if (holder.video.isPlaying()) {
                        holder.video.pause();
                        holder.audio_control.setImageResource(R.drawable.baseline_play_arrow_24);
                    } else {
                        holder.video.start();
                        holder.audio_control.setImageResource(R.drawable.baseline_pause_24);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(
                        holder.itemView.getContext(),
                        "Audio not available for this button",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
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

        holder.spoken_text.addTextChangedListener(new

                                                          TextWatcher() {
                                                              @Override
                                                              public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                              }

                                                              @Override
                                                              public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                                  changeLayoutActivity.buttons.get(holder.getAbsoluteAdapterPosition()).setSpokenText(holder.spoken_text.getText().toString());
                                                              }

                                                              @Override
                                                              public void afterTextChanged(Editable editable) {
                                                              }
                                                          });

        holder.picture.setOnLongClickListener((view ->

        {
            ClipData.Item pos = new ClipData.Item(holder.getAbsoluteAdapterPosition() + "");
            ClipData.Item viewId = new ClipData.Item(view.getId() + "");
            ClipDescription description = new ClipDescription(holder.getBindingAdapterPosition() + "", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN});
            ClipData clipData = new ClipData(description, pos);
            clipData.addItem(viewId);

            View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(holder.editor_button);
            view.startDragAndDrop(clipData, dragShadowBuilder, holder.editor_button, 0);
            return true;
        }));

        holder.editor_button.setOnDragListener((view, dragEvent) ->

        {
            int fromPosition, toPosition;
            RecyclerView.SmoothScroller smoothScroller;
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    fromPosition = Integer.parseInt(dragEvent.getClipDescription().getLabel().toString());
                    toPosition = holder.getAbsoluteAdapterPosition();
                    if (fromPosition == toPosition)
                        return false;
                    if (holder.button_divider.getVisibility() == View.VISIBLE)
                        return true;
                    holder.button_divider.setVisibility(View.VISIBLE);
                    smoothScroller = new CenterSmoothScroller(changeLayoutActivity.edit_grid.getContext());
                    smoothScroller.setTargetPosition(toPosition);
                    changeLayoutActivity.gridLayoutManager.startSmoothScroll(smoothScroller);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    holder.button_divider.setVisibility(View.GONE);
                    return true;
                case DragEvent.ACTION_DROP:
                    fromPosition = Integer.parseInt(dragEvent.getClipDescription().getLabel().toString());
                    toPosition = holder.getAbsoluteAdapterPosition();
                    Collections.swap(changeLayoutActivity.buttons, fromPosition, toPosition);
                    changeLayoutActivity.adapter.notifyItemMoved(fromPosition, toPosition);
                    holder.button_divider.setVisibility(View.GONE);
                    smoothScroller = new CenterSmoothScroller(changeLayoutActivity.edit_grid.getContext());
                    smoothScroller.setTargetPosition(toPosition);
                    changeLayoutActivity.gridLayoutManager.startSmoothScroll(smoothScroller);
                default:
                    return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return changeLayoutActivity.buttons.size();
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = changeLayoutActivity.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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
        View button_divider;

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
            button_divider = itemView.findViewById(R.id.button_divider);
        }
    }
}

