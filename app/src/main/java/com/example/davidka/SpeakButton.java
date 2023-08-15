package com.example.davidka;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;

@Entity
public class SpeakButton {

    @PrimaryKey
    public int position;

    String speak;
    String picture;
    String spokenText = "";
    Boolean isVideo;

    @Ignore
    ButtonUpdate rootSpeak = null;
    @Ignore
    ButtonUpdate leafSpeak = null;
    @Ignore
    ButtonUpdate rootPicture = null;
    @Ignore
    ButtonUpdate leafPicture = null;

    public SpeakButton(int position, String speak, String picture, String spokenText, Boolean isVideo) {
        this.position = position;
        this.speak = speak;
        this.picture = picture;
        this.spokenText = spokenText;
        this.isVideo = isVideo;

        rootSpeak = new ButtonUpdate(speak, false);
        leafSpeak = rootSpeak;

        rootPicture = new ButtonUpdate(picture, isVideo);
        leafPicture = rootPicture;
    }

//    @Ignore
//    public SpeakButton(int position) {
//        this.position = position;
//        isVideo = false;
//    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getSpeak() {
//        return speak;
        if (leafSpeak != null)
            return leafSpeak.getUri();
        else
            return speak;
    }

    public void setSpeak(String speak) {
         if (speak != null) {
            leafSpeak.setNext(new ButtonUpdate(speak, false));
            leafSpeak = leafSpeak.getNext();
        }
    }

    public String getPicture() {
//        return picture;
        if (leafPicture != null)
            return leafPicture.getUri();
        else
            return picture;
    }

    public void setPicture(String picture, boolean isVideo) {
        this.isVideo = isVideo;
        if (picture != null) {
            leafPicture.setNext(new ButtonUpdate(picture, isVideo));
            leafPicture = leafPicture.getNext();
        }
    }

    public String getSpokenText() {
        return spokenText;
    }

    public void setSpokenText(String spokenText) {
        this.spokenText = spokenText;
    }

//    public void swap(SpeakButton temp){
//        setSpeak(temp.getSpeak());
//        setPicture(temp.getPicture(),temp.isVideo);
//        setSpokenText(temp.getSpokenText());
//    }

    public void deleteUpdates(File dir, ButtonUpdate root, ButtonUpdate leaf, boolean save) {
        ButtonUpdate current = save ? root : root.getNext();

        while ((!save && current != null) || (save && current != null && current.getNext() != null)) {
            ButtonUpdate temp = current;
            Log.e("deleting linked list", "current " + current.getUri() + " next " + current.getNext());
            if (temp.getUri() != null)
                deleteFile(dir, temp.getUri(), temp.isVideo);
            current = temp.getNext();
            temp = null;
        }
        root = null;
        leaf = null;
    }

    public static void deleteFile(File dir, @NonNull String uri, boolean isExternal) {
        if (!isExternal) {
            Log.e("delete file", "delete uri " + uri + " type " + isExternal);
            new File(dir, Uri.parse(uri).getLastPathSegment())
                    .delete();
        } else
            new File(uri).delete();
    }
}
