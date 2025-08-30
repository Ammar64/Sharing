package com.ammar.sharing.common.utils;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.websocket.sessions.MainWSSession;

import org.json.JSONException;
import org.json.JSONObject;

public class UsersNotifier {
    private UsersNotifier(){};

    private static String getUIChangeJSON() {
        boolean isDark = MainActivity.sDarkMode;
        try {
            return new JSONObject()
                    .put("action", "change-ui")
                    .put("uiMode", isDark ? "dark" : "light")
                    .put("language", Utils.getRes().getString(R.string.lang))
                    .put("dir", Utils.getRes().getString(R.string.dir))
                    .toString();
        } catch (JSONException e) {
            return "{\"action\": \"error\"}";
        }
    }

    public static void notifyUsersOfUIChange() {
        String themeChangedJSON = getUIChangeJSON();
        for(User i : User.users) {
            if(i.isWebSocketConnected(MainWSSession.path)) {
                i.sendWebSocketMessage(MainWSSession.path, themeChangedJSON);
            }
        }
    }

    private static String getUploadStateJSON() {
        boolean isUploadAllowed = !Utils.getSettings().getBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, false);
        try {
            return new JSONObject()
                    .put("action", "change-upload-state")
                    .put("upload_allowed", isUploadAllowed)
                    .toString();
        } catch (JSONException e) {
            return "{\"action\": \"error\"}";
        }
    }
    public static void notifyUsersOfUploadStateChange() {
        String uploadStateJSON = getUploadStateJSON();
        for(User i : User.users) {
            if(i.isWebSocketConnected(MainWSSession.path)) {
                i.sendWebSocketMessage(MainWSSession.path, uploadStateJSON);
            }
        }
    }

    public static void notifyDownloadsChanged() {
        for(User i : User.users) {
            if(i.isWebSocketConnected(MainWSSession.path)) {
                i.sendWebSocketMessage(MainWSSession.path, "{\"action\":\"update-downloads\"}");
            }
        }
    }
}
