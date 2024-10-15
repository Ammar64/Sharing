package com.ammar.sharing.models;

public class Message {
    private String text;
    private boolean isRemote;

    public Message(String text, boolean isRemote) {
        this.text = text;
        this.isRemote = isRemote;
    }

    public String getText() {
        return text;
    }

    public boolean isRemote() {
        return isRemote;
    }
}
