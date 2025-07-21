package com.ammar.sharing.network.sessions;

import android.text.TextUtils;
import android.view.View;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.SharableApp;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class NoJSSession extends HTTPSession {
    public NoJSSession(User user) {
        super(user);
    }

    @Override
    public void GET(Request req, Response res) {
        generateAndSendNoJSPage(res);
    }

    private void generateAndSendNoJSPage(Response res) {
        Locale locale = Locale.getDefault();
        String dir = switch (TextUtils.getLayoutDirectionFromLocale(locale)) {
            case View.LAYOUT_DIRECTION_RTL -> "rtl";
            case View.LAYOUT_DIRECTION_LTR -> "ltr";
            default ->
                    throw new IllegalStateException("Unexpected value: " + TextUtils.getLayoutDirectionFromLocale(locale));
        };
        final String pageOffset =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"" + locale.toLanguageTag() + "\" dir=\"" + dir + "\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <link rel=\"icon\" type=\"image/x-icon\" href=\"/common/favicon\" />" +
                        "    <title>Sharing</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h2>" + Utils.getRes().getString(R.string.downloads) + "</h2>\n" +
                        "    <table rules=\"all\" border=\"1\" cellpadding=\"10px\">";

        final String pageEnd =
                "</table><br>\n" +
                        (!Sharable.sharablesList.isEmpty() ? "<a href=\"/da\">" + Utils.getRes().getString(R.string.download_all) + "</a>" : "") +
                        "</body>\n" +
                        "</html>\n";


        StringBuilder pageBuilder = new StringBuilder();

        pageBuilder.append(pageOffset);

        if (Sharable.sharablesList.isEmpty()) {
            final String noDownloadsText = String.format(Locale.ENGLISH, "<tr>%s</tr>", Utils.getRes().getString(R.string.no_downloads));
            pageBuilder.append(noDownloadsText);
        }
        for (Sharable i : Sharable.sharablesList) {
            final String downloadLink = "/download/" + i.getUUID().toString();
            final String iconSrc = "/get-icon/" + i.getUUID().toString();
            final String downloadElement = String.format(Locale.ENGLISH, "<tr><td><img src=\"%s\" width=\"40px\" /></td><td><a download href=\"%s\">%s</a><br><span dir=\"ltr\">%s</span></td></tr>\n", iconSrc, downloadLink, i.getName(), (i instanceof SharableApp a && (a.hasSplits()) ? "(splits)" : Utils.getFormattedSize(i.getSize())));

            pageBuilder.append(downloadElement);
        }
        pageBuilder.append(pageEnd);

        byte[] pageBytes = pageBuilder.toString().getBytes(StandardCharsets.UTF_8);
        res.setContentType("text/html");
        res.sendResponse(pageBytes);
    }
}
