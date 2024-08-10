package com.ammar.filescenter.common;

import android.os.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class Consts {
    public static final String ACTION_TOGGLE_SERVER = "com.ammar.filescenter.services.TOGGLE_SERVER";
    public static final String ACTION_STOP_SERVICE = "com.ammar.filescenter.services.STOP_SERVICE";
    public static final String ACTION_GET_SERVER_STATUS = "com.ammar.filescenter.services.GET_SERVER_STATUS";
    public static final String ACTION_UPDATE_NOTIFICATION_TEXT = "com.ammar.filescenter.services.UPDATE_NOTIFICATION_STRING";
    public static final String ACTION_REMOVE_DOWNLOAD = "ACTION_REMOVE_DOWNLOAD";
    public static final String ACTION_ADD_DOWNLOADS = "ACTION_ADD_DOWNLOADS";
    public static final String ACTION_ADD_APPS_DOWNLOADS = "ACTION_ADD_APPS_DOWNLOADS";
    public static final String ACTION_ADD_FILES = "ACTION_ADD_FILES";
    public static final String ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN = "ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN";

    public static final String EXTRA_FILE_PATHS = "com.ammar.filescenter.services.FILE_PATHS";
    public static final String EXTRA_APPS_NAMES = "com.ammar.filescenter.services.APPS_NAME";
    public static final String EXTRA_DOWNLOAD_UUID = "EXTRA_DOWNLOAD_UUID";
    public static final String EXTRA_INTENT_PATHS = "com.ammar.filescenter.EXTRA_INTENT_PATHS";

    public static final String PREF_APP_INFO = "AppInfoPref";
    public static final String PREF_FIELD_IS_FIRST_RUN = "IS_FIRST_RUN";
    public static final String PREF_FIELD_IS_USER_WANTS_WARNING = "IS_USER_WANTS_WARNING";

    public static final String PREF_SETTINGS = "SettingsPref";

    public static final String PREF_FIELD_IS_DARK = "IS_DARK_MODE";
    public static final String PREF_FIELD_IS_UPLOAD_DISABLED = "UPLOAD_DISABLE";
    public static final String PREF_FIELD_ARE_USER_BLOCKED = "USERS_BLOCK";
    public static final String PREF_FIELD_LANG = "LANGUAGE";

    public static final File filesCenterDir = new File(Environment.getExternalStorageDirectory(), "Files-Center");
    public static final File appsDir = new File(filesCenterDir, "Apps");
    public static final File imagesDir = new File(filesCenterDir, "Images");
    public static final File videosDir = new File(filesCenterDir, "Videos");
    public static final File audioDir = new File(filesCenterDir, "Audio");
    public static final File documentsDir = new File(filesCenterDir, "Documents");
    public static final File filesDir = new File(filesCenterDir, "Files");

    public static final List<String> langsCode = Arrays.asList("", "ar", "en", "du");

    public enum OS {LINUX, WINDOWS, ANDROID, IOS, MAC, UNKNOWN};
    private Consts(){}

}
