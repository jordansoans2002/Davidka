package com.example.davidka;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.List;

public class ButtonUpdate {
    String uri;
    int type;

    static final int VIDEO = 1;
    static final int IMAGE = 2;
    static final int AUDIO = 3;

    ButtonUpdate(String uri, int type) {
        this.uri = uri;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public void deleteFile(File dir, List<SpeakButton> buttons) {
        boolean present = false;
        for (SpeakButton button : buttons) {
            if (button != null) {
                switch (type) {
                    case VIDEO:
                    case IMAGE:
                        if (button.getPicture() != null && button.getPicture().equalsIgnoreCase(uri))
                            present = true;
                        break;
                    case AUDIO:
                        if (button.getSpeak() != null && button.getSpeak().equalsIgnoreCase(uri))
                            present = true;
                        break;
                    default:
                        break;
                }
            }
        }
        if (!present) {
            if (type == IMAGE || type == AUDIO) {
                Log.e("delete updates", "delete uri " + uri + " type " + type);
                new File(dir, Uri.parse(uri).getLastPathSegment())
                        .delete();
            } else
                new File(uri).delete();
        }

    }
}
