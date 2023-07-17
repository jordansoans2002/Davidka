package com.example.davidka;

import android.media.MediaPlayer;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SpeakButton {

    @PrimaryKey
    public int position;

    public String speak;
    public String picture;
    public String spokenText;

    @Ignore
    MediaPlayer audio;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getSpeak() {
        return speak;
    }

    public void setSpeak(String speak) {
        this.speak = speak;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSpokenText() {
        return spokenText;
    }

    public void setSpokenText(String spokenText) {
        this.spokenText = spokenText;
    }

    public MediaPlayer getAudio() {
        return audio;
    }

    public void setAudio(MediaPlayer audio) {
        this.audio = audio;
    }
}
