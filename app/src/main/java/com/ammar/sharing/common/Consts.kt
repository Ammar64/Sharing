package com.ammar.sharing.common

import android.os.Environment
import java.io.File
import java.util.Locale

object Consts {
    const val ACTION_TOGGLE_SERVER: String = "com.ammar.sharing.services.TOGGLE_SERVER"
    const val ACTION_STOP_SERVICE: String = "com.ammar.sharing.services.STOP_SERVICE"
    const val ACTION_GET_SERVER_STATUS: String = "com.ammar.sharing.services.GET_SERVER_STATUS"
    const val ACTION_RESTART_SERVER: String = "ACTION_RESTART_SERVER"
    const val ACTION_UPDATE_NOTIFICATION_TEXT: String =
        "com.ammar.sharing.services.UPDATE_NOTIFICATION_STRING"
    const val ACTION_REMOVE_DOWNLOAD: String = "ACTION_REMOVE_DOWNLOAD"
    const val ACTION_ADD_FILE_SHARABLES: String = "ACTION_ADD_FILE_SHARABLES"
    const val ACTION_ADD_APPS_SHARABLES: String = "ACTION_ADD_APPS_SHARABLES"
    const val ACTION_ADD_URI_SHARABLES: String = "ACTION_ADD_URI_SHARABLES"
    const val ACTION_ADD_FILES: String = "ACTION_ADD_FILES"
    const val ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN: String =
        "ACTION_STOP_APP_PROCESS_IF_SERVER_DOWN"

    const val ACTION_TRIGGER_APKS_INSTALL: String = "ACTION_TRIGGER_APKS_INSTALL"
    const val ACTION_PACKAGE_INSTALLER: String = "PACKAGE_INSTALLER_ACTION"
    const val ACTION_STOP_INSTALLER: String = "ACTION_STOP_INSTALLER"

    const val EXTRA_FILES_PATH: String = "com.ammar.sharing.services.FILE_PATHS"
    const val EXTRA_APPS_NAMES: String = "com.ammar.sharing.services.APPS_NAME"
    const val EXTRA_URIS: String = "com.ammar.sharing.services.EXTRA_URIS"
    const val EXTRA_DOWNLOAD_UUID: String = "EXTRA_DOWNLOAD_UUID"
    const val EXTRA_INTENT_PATHS: String = "com.ammar.sharing.EXTRA_INTENT_PATHS"

    const val PREF_APP_INFO: String = "AppInfoPref"
    const val PREF_FIELD_IS_FIRST_RUN: String = "IS_FIRST_RUN"
    const val PREF_FIELD_IS_USER_WANTS_WARNING: String = "IS_USER_WANTS_WARNING"

    const val PREF_SETTINGS: String = "SettingsPref"

    const val PREF_FIELD_SERVER_PORT = "SERVER_PORT"
    const val PREF_FIELD_IS_DARK: String = "IS_DARK_MODE"
    const val PREF_FIELD_IS_UPLOAD_DISABLED: String = "UPLOAD_DISABLE"
    const val PREF_FIELD_ARE_USER_BLOCKED: String = "USERS_BLOCK"
    const val PREF_FIELD_LANG: String = "LANGUAGE"
    const val PREF_FIELD_DEBUG_MODE: String = "DEBUG_MODE"
    const val PREF_FIELD_LAST_VERCODE: String = "LAST_VERCODE"

    const val SERVER_NOTIFICATION_CHANNEL_ID: String = "SERVER_NOTIFICATION_CHANNEL_ID"
    const val PROGRESS_NOTIFICATION_CHANNEL_ID: String = "PROGRESS_NOTIFICATION_CHANNEL_ID"

    const val PROGRESS_NOTIFICATION_GROUP: String = "PROGRESS_NOTIFICATION_GROUP"
    @JvmField
    val Sharing: File = File(Environment.getExternalStorageDirectory(), "Sharing")
    @JvmField
    val appsDir: File = File(Sharing, "Apps")
    @JvmField
    val imagesDir: File = File(Sharing, "Images")
    @JvmField
    val videosDir: File = File(Sharing, "Videos")
    @JvmField
    val audioDir: File = File(Sharing, "Audio")
    @JvmField
    val documentsDir: File = File(Sharing, "Documents")
    @JvmField
    val otherDir: File = File(Sharing, "Other")

    @JvmField
    var langCodes: Array<String>? = null
    @JvmField
    var systemLocale: Locale? = null

    enum class OS {
        LINUX, WINDOWS, ANDROID, IOS, MAC, UNKNOWN
    }
}
