package com.ammar.sharing.network.sessions;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.StreamUtils;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;

import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class AppConfigSession extends HTTPSession {
    public AppConfigSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        String path = req.getPath();
        try {
            boolean uploadDisabled = Utils.getSettings().getBoolean(Consts.PREF_FIELD_IS_UPLOAD_DISABLED, false);
            res.setContentType("application/json"); // this session always sends json
            if ("/check-upload-allowed".equals(path)) {
                JSONObject uploadAllowedJson = new JSONObject();
                uploadAllowedJson.put("allowed", !uploadDisabled);
                res.sendResponse(uploadAllowedJson.toString().getBytes(StandardCharsets.UTF_8));
            } else if("/get-app-config".equals(path)) {
                JSONObject appConfigJson = new JSONObject();
                boolean isDark = MainActivity.sDarkMode;
                appConfigJson.put("uiMode", isDark ? "dark" : "light");
                appConfigJson.put("dir", Utils.getRes().getString(R.string.dir));
                appConfigJson.put("language", Utils.getRes().getString(R.string.lang));
                appConfigJson.put("browserIP", user.getIp());
                String streamStatus;
                if(!StreamUtils.isStreamingOn) {
                    streamStatus = User.StreamStatus.SERVICE_OFF.toString();
                } else {
                    streamStatus = user.mStreamStatus.toString();
                }
                appConfigJson.put("stream-status", streamStatus);
                res.sendResponse(appConfigJson.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (JSONException e) {
            Utils.showErrorDialog("AppConfigSession.GET(). JSONException", e.getMessage());
            res.setStatusCode(400);
            res.sendResponse();
        }
    }
}
