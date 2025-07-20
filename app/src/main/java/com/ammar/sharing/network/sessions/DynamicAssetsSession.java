package com.ammar.sharing.network.sessions;

import android.graphics.Bitmap;

import androidx.core.content.res.ResourcesCompat;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;

public class DynamicAssetsSession extends HTTPSession {
    public DynamicAssetsSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        if (req.getPath().startsWith("/get-icon/")) {
            String requestedUUID = req.getPath().substring(10);
            Sharable file = Sharable.getFileWithUUID(requestedUUID);
            res.sendBitmapResponse(file.getBitmapIcon());
        } else if( "/favicon.ico".equals(req.getPath()) ) {
            Bitmap favBM = Utils.drawableToBitmap(ResourcesCompat.getDrawable(Utils.getRes(), R.mipmap.ic_launcher_round, null));
            res.sendBitmapResponse(favBM);
        }
    }
}
