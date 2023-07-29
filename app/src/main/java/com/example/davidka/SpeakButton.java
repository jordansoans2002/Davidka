package com.example.davidka;

import android.media.MediaPlayer;
import android.net.Uri;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;

@Entity
public class SpeakButton {

    @PrimaryKey
    public int position;

    public String speak;
    public String picture;
    public String spokenText = "";
    public Boolean isVideo;

    public SpeakButton(int position) {
        this.position = position;
        isVideo = false;
    }

    public int getPosition() {
        return position;
    }

    public String getSpeak() {
        return speak;
    }

    public void setSpeak(String speak) {
        if(!isVideo)
            this.speak = speak;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture, Boolean isVideo) {
//        if(this.picture != null)
//            delete(this.picture);
        this.picture = picture;
        this.isVideo = isVideo;
    }

    public String getSpokenText() {
        return spokenText;
    }

    public void setSpokenText(String spokenText) {
        this.spokenText = spokenText;
    }

    public void swap(SpeakButton temp){
        setSpeak(temp.getSpeak());
        setPicture(temp.getPicture(),temp.isVideo);
        setSpokenText(temp.getSpokenText());
    }

    public void delete(String uri){
        new File(Uri.parse(uri).getPath()).delete();
    }
}
