package com.ammar.sharing.common.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {
    public static String textToSha1Base64(String text) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(text.getBytes());
            return Base64.encodeToString(crypt.digest(), Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException ignore) {
            throw new RuntimeException("SHA-1 hash is not supported on this device");
        }
    }


    private SecurityUtils() {
    }
}
