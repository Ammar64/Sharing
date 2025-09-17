package com.ammar.sharing.activities.StreamingActivity.fragments;

public class VideoSourceProps {

    public VideoSourceProps(int width, int height, int minFramerate, int maxFramerate) {
        this.width = width;
        this.height = height;
        this.minFramerate = minFramerate;
        this.maxFramerate = maxFramerate;
    }

    int width;
    int height;
    int minFramerate;
    int maxFramerate;
}
