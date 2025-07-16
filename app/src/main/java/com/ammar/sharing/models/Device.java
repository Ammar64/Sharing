package com.ammar.sharing.models;

import com.ammar.sharing.common.enums.OS;

public class Device {
    private String mName;
    private String mIpAddress;
    private OS mOS;

    public Device(String mName, String mIpAddress, OS mOS) {
        this.mName = mName;
        this.mIpAddress = mIpAddress;
        this.mOS = mOS;
    }

    public String getName() {
        return mName;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public OS getOS() {
        return mOS;
    }
}
