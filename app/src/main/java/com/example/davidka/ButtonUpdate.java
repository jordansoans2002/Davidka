package com.example.davidka;

public class ButtonUpdate {
    private final String uri;
    private ButtonUpdate next = null;
    boolean isVideo;

    //we only want the original and final, first time
    ButtonUpdate(String uri,boolean isVideo) {
        this.uri = uri;
        this.isVideo = isVideo;
    }

    public void setNext(ButtonUpdate update) {
        next = update;
    }

    public ButtonUpdate getNext() {
        return next;
    }

    public String getUri() {
        return uri;
    }
}
