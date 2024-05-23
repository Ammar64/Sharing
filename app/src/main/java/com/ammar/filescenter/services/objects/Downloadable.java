package com.ammar.filescenter.services.objects;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

public class Downloadable {
    protected UUID uuid;
    protected String path;
    protected long size;

    public Downloadable(String path) {
        this.uuid = UUID.randomUUID();
        this.path = path;
        this.size = new File(path).length();
    }

    protected Downloadable() {

    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        int index = path.lastIndexOf("/");
        return path.substring(index+1);
    }

    public String getFormattedSize() {
        double s = size;
        String[] levels = {"B","KB","MB","GB","TB","PB"};
        int level = 0;
        boolean isGood = false;
        while(!isGood) {
            if( s > 1200 && level < levels.length ) {
                s /= 1024;
                level++;
            }
            else {
                isGood = true;
            }
        }
        return String.format(Locale.ENGLISH, "(%.2f %s)", s, levels[level]);
    }

    public long getSize() {
        return size;
    }
    public String getPath() {
        return path;
    }
}
