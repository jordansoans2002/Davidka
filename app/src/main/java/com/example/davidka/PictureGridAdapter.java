package com.example.davidka;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PictureGridAdapter extends RecyclerView.Adapter<PictureGridAdapter.ViewHolder> {
    MainActivity mainActivity;
    List<SpeakButton> buttons;

    int vidPlaying = -1;

    PictureGridAdapter(MainActivity mainActivity, List<SpeakButton> buttons) {
        this.mainActivity = mainActivity;
        this.buttons = buttons;
    }

    @NonNull
    @Override
    public PictureGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureGridAdapter.ViewHolder holder, int position) {
        SpeakButton button = buttons.get(holder.getAbsoluteAdapterPosition());
        String imgUri = button.getPicture();
        try {
            boolean exists = new File(Uri.parse(imgUri).getPath()).exists();
            if (exists) {
                if (!button.isVideo) {
                    holder.img.setImageURI(Uri.parse(imgUri));
                    holder.img.setVisibility(View.VISIBLE);
                    holder.vid.setVisibility(View.GONE);
                } else {
                    holder.vid.setVideoURI(Uri.parse(imgUri));
                    holder.vid.setVisibility(View.VISIBLE);
                    holder.vid.seekTo(1);
                    holder.vid.setOnCompletionListener((mediaPlayer -> holder.vid.seekTo(1)));
                    holder.img.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(
                        holder.itemView.getContext(),
                        "A media file is missing",
                        Toast.LENGTH_LONG
                ).show();
                //TODO crate query to delete by uri from the database
                throw new NullPointerException();
            }
        }catch (NullPointerException nullPointerException){
            if (!mainActivity.preferences.getBoolean("blankButton", false))
                holder.img.setImageResource(R.mipmap.ic_launcher);
        }

        if (mainActivity.preferences.getBoolean("showText", false)) {
            holder.txt.setText(button.getSpokenText());
            holder.txt.setVisibility(View.VISIBLE);
        } else
            holder.txt.setVisibility(View.GONE);

        holder.picture.setOnClickListener((View view) -> {
            if (!buttons.get(holder.getAbsoluteAdapterPosition()).isVideo) {
                String speakUri = button.getSpeak();
                try {
                    boolean exists = new File(Uri.parse(speakUri).getPath()).exists();
                    if (exists) {
                        if (mainActivity.video != null && mainActivity.video.isPlaying()) {
                            mainActivity.video.seekTo(1);
                            mainActivity.video.pause();
                        }

                        if (mainActivity.speak != null)
                            mainActivity.speak.release();

                        mainActivity.speak = MediaPlayer.create(holder.img.getContext(), Uri.parse(speakUri));
                        mainActivity.speak.setOnCompletionListener((MediaPlayer::release));
                        mainActivity.speak.start();
                    } else {
                        Toast.makeText(
                                holder.itemView.getContext(),
                                "Audio file is missing",
                                Toast.LENGTH_LONG
                        ).show();
                        new Thread(()->{
                            DatabaseHelper db = DatabaseHelper.getDB(holder.picture.getContext());
                            button.speak = null;
                            db.speakButtonDao().updateSpeakButton(button);
                        }).start();
                    }
                }catch (NullPointerException nullPointerException) {
                    Toast.makeText(
                            holder.itemView.getContext(),
                            "Audio not available for this button",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            } else {
                if (mainActivity.speak != null)
                    mainActivity.speak.release();

                if (mainActivity.video == null || !mainActivity.video.isPlaying()) {
                    mainActivity.video = holder.vid;
                    holder.vid.start();
                } else {
                    mainActivity.video.pause();
                    mainActivity.video.seekTo(1);
                    if (mainActivity.video != holder.vid) {
                        mainActivity.video = holder.vid;
                        holder.vid.start();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mainActivity.preferences.getBoolean("scrollable", false))
            //you can have as many buttons in edit view but only the first 8 will be shown
            return buttons.size();
        else
            return 8;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView picture;
        ImageView img;
        VideoView vid;
        TextView txt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture);
            img = itemView.findViewById(R.id.img);
            vid = itemView.findViewById(R.id.vid);
            txt = itemView.findViewById(R.id.txt);
        }
    }
}
