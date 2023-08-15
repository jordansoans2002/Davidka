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
    ButtonUpdate rootSpeak = null;
    @Ignore
    ButtonUpdate leafSpeak = null;
    @Ignore
    ButtonUpdate rootPicture = null;
    @Ignore
    ButtonUpdate leafPicture = null;

    public SpeakButton(int position) {
        this.position = position;
    }

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
//        if(!isVideo)
//            this.speak = speak;
        if (rootSpeak == null) {
            this.speak = speak;
            rootSpeak = new ButtonUpdate(speak, false);
            leafSpeak = rootSpeak;
        } else if (speak != null) {
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

    public void setPicture(String picture){
        String ext = picture.substring(picture.lastIndexOf('.'));
        this.isVideo = ext.equalsIgnoreCase(".mp4");
        Log.e("load button",isVideo+"");
        if (rootPicture == null) {
            this.picture = picture;
            rootPicture = new ButtonUpdate(picture, isVideo);
            leafPicture = rootPicture;
        }
    }

    public void setPicture(String picture, boolean isVideo) {
//        this.picture = picture;
        this.isVideo = isVideo;

//        if (rootPicture == null) {
//            this.picture = picture;
//            rootPicture = new ButtonUpdate(picture, isVideo);
//            leafPicture = rootPicture;
//        } else if (picture != null) {
            leafPicture.setNext(new ButtonUpdate(picture, isVideo));
            leafPicture = leafPicture.getNext();
//        }
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

    public static void deleteFile(File dir, String uri, boolean isExternal) {
        if (!isExternal) {
            Log.e("delete file", "delete uri " + uri + " type " + isExternal);
            new File(dir, Uri.parse(uri).getLastPathSegment())
                    .delete();
        } else
            new File(uri).delete();
    }
}
