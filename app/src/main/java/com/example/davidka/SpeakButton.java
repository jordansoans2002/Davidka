package com.example.davidka;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;
import java.util.LinkedList;

@Entity
public class SpeakButton {

    @PrimaryKey
    public int position;

    String speak;
    String picture;
    String spokenText = "";
    Boolean isVideo;

    @Ignore
    LinkedList<String> speakUpdates = new LinkedList<>();
    @Ignore
    LinkedList<String> pictureUpdates = new LinkedList<>();

    public SpeakButton(int position) {
        this.position = position;
        isVideo = false;

        if(speak!=null)
            speakUpdates.add(0,speak);
        if(picture!=null)
            pictureUpdates.add(0,picture);
    }

    public void setPosition(int position){
        this.position = position;
    }
    public String getSpeak() {
        return speak;
    }

    public void setSpeak(String speak) {
        if(!isVideo)
            this.speak = speak;
        speakUpdates.add(speak);
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture, Boolean isVideo) {
        this.picture = picture;
        this.isVideo = isVideo;
        pictureUpdates.add(picture);
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
