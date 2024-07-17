package com.ammar.filescenter.network.sessions;

public class Sessions {
    public static void defineSessions() {
        // main page

        new PageSession(new String[]{
                "/", "/index.html", "/style.css", "/script.js", "/dv.png", "/favicon.ico", "/blocked"
        });


        new DownloadSession(new String[]{
           "/download/", "/available-downloads" ,"/get-icon"
        });

        new UploadSession(new String[]{
                "/upload/", "/check-upload-allowed"
        });

        new UserSession(new String[]{
            "/get-user-info", "/update-user-name"
        });
    }

    private Sessions() {
    }

    ;
}
