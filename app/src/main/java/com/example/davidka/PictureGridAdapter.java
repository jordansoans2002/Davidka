package com.example.davidka;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PictureGridAdapter extends RecyclerView.Adapter<PictureGridAdapter.ViewHolder> {
    List<Integer> images = new ArrayList();
    PictureGridAdapter(){
        for(int i=0;i<8;i++)
            images.add(R.drawable.yes);
    }

    @NonNull
    @Override
    public PictureGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mouth,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureGridAdapter.ViewHolder holder, int position) {
        holder.img.setImageResource(images.get(position));
    }

    @Override
    public int getItemCount() {
        //show only the first 8 items if scrolling is disabled
        //you can have as many images in edit view but only the first 8 will be shown
        return 8;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }
    }
}
