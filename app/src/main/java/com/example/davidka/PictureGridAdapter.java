package com.example.davidka;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class PictureGridAdapter extends RecyclerView.Adapter<PictureGridAdapter.ViewHolder> {
    MainActivity mainActivity;
    List<SpeakButton> buttons;

    int vidPlaying=-1;

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
        SpeakButton button = buttons.get(holder.getAdapterPosition());
        try {
            String imgUri = button.getPicture();
            if (imgUri != null) {
                if (!button.isVideo) {
                    holder.img.setImageURI(Uri.parse(imgUri));
                    holder.vid.setVisibility(View.GONE);
                } else {
                    holder.vid.setVideoURI(Uri.parse(imgUri));
                    holder.vid.setOnCompletionListener((mediaPlayer -> holder.vid.seekTo(0)));
                    holder.img.setVisibility(View.GONE);
                }
            }

            if (mainActivity.preferences.getBoolean("showText", false))
                holder.txt.setText(button.getSpokenText());
            else
                holder.txt.setVisibility(View.GONE);

            holder.picture.setOnClickListener((View view) -> {
                if (!buttons.get(holder.getAdapterPosition()).isVideo) {
                    String speakUri = button.getSpeak();
                    if (speakUri != null) {
                        if (mainActivity.speak != null)
                            mainActivity.speak.release();
                        mainActivity.speak = MediaPlayer.create(holder.img.getContext(), Uri.parse(speakUri));
                        mainActivity.speak.setOnCompletionListener((MediaPlayer::release));
                        mainActivity.speak.start();
                    } else
                        Toast.makeText(holder.itemView.getContext(), "Audio not available for this button", Toast.LENGTH_SHORT)
                                .show();
                } else {
                    Log.e("video status", String.valueOf(holder.vid.isPlaying()));
                    if (holder.vid.isPlaying()) {
                        holder.vid.pause();
                    } else {
                        holder.vid.seekTo(0);
                        holder.vid.start();
                    }
                }
            });
        } catch (SecurityException se) {
            System.err.println("need uri permission at position " + position);
        } catch (Exception e) {
            System.err.println("some error");
        }
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
