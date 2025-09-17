package com.ammar.sharing.common.enums
import androidx.annotation.DrawableRes

import com.ammar.sharing.R

enum class OS {
    LINUX, WINDOWS, ANDROID, IOS, MAC, UNKNOWN;
    companion object {
        @JvmStatic
        @DrawableRes
        fun getOSResourceDrawable(os: OS): Int {
            return when (os) {
                WINDOWS ->R.drawable.icon_windows_10
                ANDROID ->R.drawable.ic_android
                LINUX ->R.drawable.icon_linux
                else ->R.drawable.icon_question_mark
            }
        }
    }
}