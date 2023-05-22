package com.example.davidka;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class EditGridAdapter extends RecyclerView.Adapter<EditGridAdapter.ViewHolder> {
    Context editLayoutContext;
    public EditGridAdapter(Context editLayoutContext) {
        this.editLayoutContext = editLayoutContext;
    }

    @NonNull
    @Override
    public EditGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mouthpiece,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditGridAdapter.ViewHolder holder, int position) {
        holder.spoken_text.setText(position+"");
        holder.picture.setOnClickListener((View view) -> {
            //TODO only allows images, accommodate gif and videos also

            Intent pickImage = new Intent();
//            pickImage.setType("image/");
            pickImage.setAction(Intent.ACTION_PICK);
            pickImage.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent clickImage = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            Intent chooserIntent = Intent.createChooser(pickImage, "Take or select image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { clickImage });
//            view.getContext().startActivity(intent);
            editLayoutContext.startActivity(chooserIntent);

        });
    }

    @Override
    public int getItemCount() {
        //TODO make the list dynamic
        // when scroll is disabled only 1st 8 will be shown on home page
        return 8;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        EditText spoken_text;
        ImageView audio_control;
        SeekBar seekBar;
        ImageView change_audio;
        CardView picture;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spoken_text = itemView.findViewById(R.id.spoken_text);
            audio_control = itemView.findViewById(R.id.audio_control);
            seekBar = itemView.findViewById(R.id.seekBar);
            change_audio = itemView.findViewById(R.id.change_audio);
            picture = itemView.findViewById(R.id.picture);
            image = itemView.findViewById(R.id.img);
        }
    }
}

