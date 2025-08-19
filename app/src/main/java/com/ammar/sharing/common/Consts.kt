package com.ammar.sharing.common

import android.os.Environment
import java.io.File
import java.util.Locale

object Consts {
    const val MAX_NON_FILE_CONTENT_LENGTH: Long = 4096
    const val PREF_APP_INFO: String = "AppInfoPref"
    const val PREF_FIELD_IS_FIRST_RUN: String = "IS_FIRST_RUN"
    const val PREF_FIELD_HTTPS_DIALOG_SHOWN: String = "HTTPS_DIALOG_SHOWN"

    const val PREF_SETTINGS: String = "SettingsPref"

    const val PREF_FIELD_SERVER_PORT = "SERVER_PORT"
    const val PREF_FIELD_IS_HTTPS: String = "IS_HTTPS"
    const val PREF_FIELD_IS_DARK: String = "IS_DARK_MODE"
    const val PREF_FIELD_IS_UPLOAD_DISABLED: String = "UPLOAD_DISABLE"
    const val PREF_FIELD_ARE_USERS_BLOCKED: String = "USERS_BLOCK"
    const val PREF_FIELD_LANG: String = "LANGUAGE"
    const val PREF_FIELD_DEBUG_MODE: String = "DEBUG_MODE"
    const val PREF_FIELD_LAST_VERCODE: String = "LAST_VERCODE"

    const val SERVER_NOTIFICATION_CHANNEL_ID: String = "SERVER_NOTIFICATION_CHANNEL_ID"
    const val PROGRESS_NOTIFICATION_CHANNEL_ID: String = "PROGRESS_NOTIFICATION_CHANNEL_ID"

    const val PROGRESS_NOTIFICATION_GROUP: String = "PROGRESS_NOTIFICATION_GROUP"

    const val MULTICAST_DISCOVERY_GROUP: String = "227.2.7.7"
    const val MULTICAST_DISCOVERY_PORT: Int = 20152
    const val BROADCAST_DISCOVERY_PORT: Int = 50722
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
}
