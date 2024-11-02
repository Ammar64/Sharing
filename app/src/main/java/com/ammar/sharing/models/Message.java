package com.ammar.sharing.models;

import com.ammar.sharing.common.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private final String text;
    private String author;
    private final boolean isRemote;

    public Message(String text, String author, boolean isRemote) {
        this.text = text;
        this.isRemote = isRemote;
        this.author = author;
    }

    public String getContent() {
        return text;
    }
    public String getAuthor() {
        return author;
    }
    public boolean isRemote() {
        return isRemote;
    }

    public void setAuthor(String name) {
        this.author = name;
    }

    public static Message fromJSON(String json, boolean isRemote) {
        try {
            JSONObject messageJSON = new JSONObject(json);
            if( messageJSON.has("type") && "message".equals(messageJSON.getString("type"))) {
                String author = "";
                if(messageJSON.has("author")) {
                    author = messageJSON.getString("author");
                }
                String content = messageJSON.getString("content");
                return new Message(content, author, isRemote);
            } else {
                return null;
            }
        } catch (JSONException e) {
            Utils.showErrorDialog("Message.toJSON. JSONException", "Error: " + e.getMessage());
            return null;
        }
    }

    public String toJSON() {
        try {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put("type", "message");
            messageJSON.put("author", getAuthor());
            messageJSON.put("content", getContent());
            return messageJSON.toString();
        } catch (JSONException e) {
            Utils.showErrorDialog("Message.toJSON. JSONException", "Error: " + e.getMessage());
            return "{\"type\": \"error\", \"reason\": \"JSONException\"}";
        }
    }
}
