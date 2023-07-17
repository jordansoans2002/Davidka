package com.example.davidka;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureGridAdapter extends RecyclerView.Adapter<PictureGridAdapter.ViewHolder> {
    MainActivity mainActivity;
    List<SpeakButton> buttons;
    PictureGridAdapter(MainActivity mainActivity, List<SpeakButton> buttons){
        this.mainActivity = mainActivity;
        this.buttons = buttons;
    }

    @NonNull
    @Override
    public PictureGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.button,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureGridAdapter.ViewHolder holder, int position) {
        try {
            String imgUri = buttons.get(position).picture;
            if (imgUri != null)
                holder.img.setImageURI(Uri.parse(imgUri));

            holder.img.setOnClickListener((View view) -> {
                String speakUri = buttons.get(position).speak;
                if (speakUri != null)
                    holder.speak = MediaPlayer.create(holder.img.getContext(), Uri.parse(speakUri));
                if (holder.speak != null) {
                    if (holder.speak.isPlaying()) {
                        //to resume use pause
//                        holder.speak.pause();
                        //to restart
                        holder.speak.release();
                    } else
                        holder.speak.start();
                } else
                    Toast.makeText(holder.itemView.getContext(), "Audio not available for this button", Toast.LENGTH_SHORT)
                            .show();
            });
        }catch (SecurityException se){

        }
    }

    @Override
    public int getItemCount() {
        //show only the first 8 items if scrolling is disabled
        //you can have as many images in edit view but only the first 8 will be shown
        return 8;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        MediaPlayer speak;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);

        }
    }
}
