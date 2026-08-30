package com.ammar.sharing.network;

import androidx.annotation.Nullable;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.common.Global;
import com.ammar.sharing.common.utils.FileUtils;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.lambda.MyConsumer;
import com.ammar.sharing.services.ServerService;


@SuppressWarnings("JavaJniMissingFunction")
public class WebServer {
    private static WebServer sInstance = null;
    @Nullable
    public static WebServer getInstance() {
        return sInstance;
    }
    public static void doSomethingIfServerAvailable(MyConsumer<WebServer> f) {
        if(sInstance != null) {
            f.accept(sInstance);
        }
    }

    public static int PORT_NUMBER;
    public static boolean IS_HTTPS;
    private boolean running = false;
    final ServerService service;


    public WebServer(ServerService service) {
        this.service = service;
        WebServer.sInstance = this;
    }

    // returns true on success
    private native boolean nativeStartServer(WebAppServerConfig config);

    // returns true on success
    private native boolean nativeStopServer();

    private native void nativeUpdateUiConfig(WebAppSettings webAppSettings);



    public void Start() {
        try {
            WebAppServerConfig config = new WebAppServerConfig(
                    FileUtils.getFilesDir().getPath(),
                    (short) WebServer.PORT_NUMBER,
                    WebServer.IS_HTTPS
            );
            running = nativeStartServer(config);
            if(running) {
                updateUiConfig();
            }
        } catch (RuntimeException e) {
            Utils.showErrorDialog("Server.Start(). RuntimeException: ", e.getMessage());
        }
    }

    public void Stop() {
        running = !nativeStopServer();
    }

    public boolean isRunning() {
        return running;
    }

    public void updateUiConfig() {
        nativeUpdateUiConfig(new WebAppSettings(
                Utils.getRes().getString(R.string.downloads),
                Utils.getRes().getString(R.string.downloads),
                Utils.getRes().getString(R.string.lang),
                Utils.getRes().getString(R.string.dir),
                MainActivity.sDarkMode,
                !Utils.getSettings().getBoolean(Global.PREF_FIELD_IS_UPLOAD_DISABLED, false)
        ));
    }

    private record WebAppServerConfig(
            String filesDir,
            short port,
            boolean isHttps
    ) {
    }

    private record WebAppSettings(
            String downloadsTranslation,
            String downloadAllTranslation,
            String language,
            String direction,
            boolean isDarkMode,
            boolean uploadAllowed
    ) {
    }


//    void handleSessionsData() {
//        // NoJS
//        this.addPath("/no-JS", NoJSSession.class);
//        // DownloadSession
//        this.addPath("/download/(.*)", DownloadSession.class);
//        this.addPath("/available-downloads", DownloadSession.class);
//
//        // UploadSession
//        this.addPath("/upload/(.*)", UploadSession.class);
//
//        // UserSession
//        this.addPath("/get-user-info", UserSession.class);
//        this.addPath("/update-user-name", UserSession.class);
//
//        // CLI Session
//        this.addPath("/ls", CLISession.class);
//        this.addPath("/dl/(.*)", CLISession.class);
//        this.addPath("/da", CLISession.class);
//
//        //DynamicAssetsSession
//        this.addPath("/get-icon/(.*)", SharedAssetsSession.class);
//        this.addPath("/favicon.ico", SharedAssetsSession.class);
//        this.addPath("/shared/almarai_regular.ttf", SharedAssetsSession.class);
//
//        //MessagesSession
//        this.addPath("/get-all-messages", MessagesSession.class);
//
//        //AppConfigSession
//        this.addPath("/check-upload-allowed", AppConfigSession.class);
//        this.addPath("/get-app-config", AppConfigSession.class);
//
//        this.addPaths(RedirectSession.redirectMap.keySet(), RedirectSession.class);
//        this.addWebsocketPath(MainWSSession.path, MainWSSession.class);
//        this.addWebsocketPath(MessagesWSSession.path, MessagesWSSession.class);
//        this.addWebsocketPath(WebRTCSignallingSession.path, WebRTCSignallingSession.class);
//    }
}