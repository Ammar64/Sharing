package com.ammar.sharing.models;

import com.ammar.sharing.common.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Message {
    private final String text;
    private String authorName;
    private final int authorId;
    private final boolean isRemote;

    public Message(String text, User user) {
        this.text = text;
        this.isRemote = true;
        this.authorName = user.getName();
        this.authorId = user.getId();
    }

    public Message(String text) {
        this.text = text;
        this.isRemote = false;
        this.authorName = "admin";
        this.authorId = -1; // owner
    }

    public String getContent() {
        return text;
    }

    public String getAuthorName() {
        return authorName;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setAuthorName(String name) {
        this.authorName = name;
    }
    public int getAuthorID() {
        return authorId;
    }

    public static Message fromJSON(String json, User user) {
        try {
            JSONObject messageJSON = new JSONObject(json);
            if (messageJSON.has("type") && "message".equals(messageJSON.getString("type"))) {
                String author = "";
                if (messageJSON.has("author")) {
                    author = messageJSON.getString("author");
                    if( !author.equals( user.getName() )) {
                        // TODO: This shouldn't happen
                    }
                    String content = messageJSON.getString("content");
                    return new Message(content, user);
                }
            }
            return null;
        } catch (JSONException e) {
            Utils.showErrorDialog("Message.toJSON. JSONException", "Error: " + e.getMessage());
            return null;
        }
    }

    public JSONObject toJSON() {
        try {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put("type", "message");
            messageJSON.put("author", getAuthorName());
            messageJSON.put("authorID", getAuthorID());
            messageJSON.put("content", getContent());
            return messageJSON;
        } catch (JSONException e) {
            Utils.showErrorDialog("Message.toJSON. JSONException", "Error: " + e.getMessage());
            try {
                return new JSONObject()
                        .put("type", "error")
                        .put("reason", "JSONException");
            } catch (JSONException ex) {
                return null;
            }
        }
    }

    public static JSONArray toJSONArray(List<Message> messages) {
        JSONArray messagesJSON = new JSONArray();
        for (Message i : messages) {
            messagesJSON.put(i.toJSON());
        }
        return messagesJSON;
    }
}
