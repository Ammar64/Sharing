package com.ammar.filescenter.services.network.sessions;

import com.ammar.filescenter.services.network.sessions.base.HTTPSession;

public class Sessions {
    public void defineSessions() {
        // main page

        new PageSession(new String[]{
                "/index.html",
                "/style.css",
                "/script.js",
                "/dv.png"
        });


        new DownloadSession(new String[]{
           "/download/"
        });

        new UploadSession(new String[]{
                "/upload"
        });

        new UserSession(new String[]{

        });
    }

    private Sessions() {
    }

    ;
}
