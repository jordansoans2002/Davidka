package com.example.davidka;

import android.net.Uri;
import android.util.Log;

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
    boolean delete;
    @Ignore
    ButtonUpdate rootSpeak;
    @Ignore
    ButtonUpdate leafSpeak;
    @Ignore
    ButtonUpdate rootPicture;
    @Ignore
    ButtonUpdate leafPicture;

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
        return leafSpeak.getUri();
    }

    public void setSpeak(String speak) {
        leafSpeak.setNext(new ButtonUpdate(speak, false));
        leafSpeak = leafSpeak.getNext();
    }

    public String getPicture() {
//        return picture;
        return leafPicture.getUri();
    }

    public void setPicture(String picture, boolean isVideo) {
        this.isVideo = isVideo;
        leafPicture.setNext(new ButtonUpdate(picture, isVideo));
        leafPicture = leafPicture.getNext();
    }

    public String getSpokenText() {
        return spokenText;
    }

    public void setSpokenText(String spokenText) {
        this.spokenText = spokenText;
    }

    //add extra node to end with null so when updates are save all nodes from root leaf
    public void deleteButton() {
        delete = true;
        setPicture(null,false);
        setSpeak(null);
    }

//    public void swap(SpeakButton temp){
//        setSpeak(temp.getSpeak());
//        setPicture(temp.getPicture(),temp.isVideo);
//        setSpokenText(temp.getSpokenText());
//    }

    public void deleteUpdates(File dir, ButtonUpdate root, boolean save) {
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
        current = null;
    }

    public static void deleteFile(File dir, String uri, boolean isExternal) {
        if (uri == null)
            return;
        if (!isExternal) {
            Log.e("delete file", "delete uri " + uri + " type " + isExternal);
            new File(dir, Uri.parse(uri).getLastPathSegment())
                    .delete();
        } else
            new File(uri).delete();
    }
}
