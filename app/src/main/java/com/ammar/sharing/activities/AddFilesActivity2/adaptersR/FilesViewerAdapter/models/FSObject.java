package com.ammar.sharing.activities.AddFilesActivity2.adaptersR.FilesViewerAdapter.models;

import com.ammar.sharing.models.Sharable;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

// can be a file or directory
public class FSObject {

    public FSObject(File file) {
        this.file = file;
    }

    private final File file;
    public boolean isSelected = false;

    public File getFile() {
        return file;
    }

    public enum SortType {
        BY_NAME,
        BY_LAST_MODIFIED,
        BY_SIZE
    }

    public static FSObject[] listDirectorySorted(File dir, SortType sortType) {
        File[] files = dir.listFiles();
        if (files == null) return null;

        Comparator<File> compareStrategy;
        switch (sortType) {
            case BY_NAME:
                compareStrategy = (l, r) -> l.getName().compareTo(r.getName());
                break;
            case BY_LAST_MODIFIED:
                compareStrategy = (l, r) -> Long.compare(r.lastModified(), l.lastModified());
                break;
            case BY_SIZE:
                compareStrategy = (l, r) -> Long.compare(l.length(), r.length());
                break;
            default:
                throw new RuntimeException();
        }
        Arrays.sort(files, compareStrategy);

        FSObject[] fsObjects = new FSObject[files.length];
        for (int i = 0; i < files.length; i++) {
            fsObjects[i] = new FSObject(files[i]);
        }
        return fsObjects;
    }

}
