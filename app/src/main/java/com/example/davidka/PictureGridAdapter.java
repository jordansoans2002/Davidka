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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureGridAdapter extends RecyclerView.Adapter<PictureGridAdapter.ViewHolder> {
    MainActivity mainActivity;
    List<SpeakButton> buttons;

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
        try {
            String imgUri = buttons.get(position).getPicture();
            if (imgUri != null)
                holder.img.setImageURI(Uri.parse(imgUri));
            holder.txt.setText(buttons.get(position).getSpokenText());

            holder.img.setOnClickListener((View view) -> {
                String speakUri = buttons.get(holder.getAdapterPosition()).getSpeak();
                if (speakUri != null) {
                    if(mainActivity.speak != null)
                        mainActivity.speak.release();
                    mainActivity.speak = MediaPlayer.create(holder.img.getContext(), Uri.parse(speakUri));
                    mainActivity.speak.setOnCompletionListener((mediaPlayer -> mediaPlayer.release()));
                    mainActivity.speak.start();
                } else
                    Toast.makeText(holder.itemView.getContext(), "Audio not available for this button", Toast.LENGTH_SHORT)
                            .show();
            });
        } catch (SecurityException se) {
            System.err.println("need uri permission at position " + position);
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
        TextView txt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            txt = itemView.findViewById(R.id.txt);
        }
    }
}
