package com.ammar.filescenter.activities.MainActivity.models;

public class Download {
    private String uuid;
    private String name;
    private int loaded;
    private int size;

    public Download(String uuid, String name, int size) {
        this.uuid = uuid;
        this.name = name;
        this.loaded = 0;
        this.size = size;
    }

    public void setLoaded(int loaded) {
        this.loaded = loaded;
    }

    public String getName() {
        return name;
    }

    public int getLoaded() {
        return loaded;
    }

    public int getSize() {
        return size;
    }

    public String getUuid() {
        return uuid;
    }
}
