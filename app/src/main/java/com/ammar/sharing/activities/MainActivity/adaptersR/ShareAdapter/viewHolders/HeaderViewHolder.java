package com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders;

import static com.ammar.sharing.activities.MainActivity.MainActivity.sDarkMode;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.ChosenFilesAdapter;
import com.ammar.sharing.activities.MainActivity.adaptersR.UsersAdapter;
import com.ammar.sharing.activities.MainActivity.fragments.BrowserShareFragment;
import com.ammar.sharing.activities.MessagesActivity.MessagesActivity;
import com.ammar.sharing.common.LiveDataSingletons;
import com.ammar.sharing.common.SharedInfo;
import com.ammar.sharing.common.utils.UsersNotifier;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
import com.ammar.sharing.custom.ui.RoundDialog;
import com.ammar.sharing.nativebackend.DownloadItemsManager;
import com.ammar.sharing.nativebackend.CertFingerprints;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.User;
import com.ammar.sharing.nativebackend.CertificateManagerWrapper;
import com.ammar.sharing.network.WebServer;
import com.ammar.sharing.services.ServerService;

// This is just the first row in the recycler view
public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final ImageView QRImageIV;
    private final AppCompatTextView serverLinkTV;
    private final AdaptiveTextView QRCodeErrorText;
    private final BrowserShareFragment fragment;
    private final TextView messagesNumTV;
    private final Button viewCertB;

    public static int unseenMessagesCount = 0;

    public HeaderViewHolder(@NonNull View itemView, BrowserShareFragment fragment) {
        super(itemView);
        this.fragment = fragment;
        ImageButton addItemsB = itemView.findViewById(R.id.B_AddItems);
        ImageButton streamingB = itemView.findViewById(R.id.B_Stream);
        ImageButton messagesB = itemView.findViewById(R.id.B_Messages);
        AppCompatButton QRCodeB = itemView.findViewById(R.id.B_ShowAddress);

        ImageButton showSelected = itemView.findViewById(R.id.B_ShowSelected);
        ImageButton showUsersB = itemView.findViewById(R.id.B_ShowUsers);
        // setup badges
        TextView usersNumTV = itemView.findViewById(R.id.TV_NumberUsers);
        TextView filesNumTV = itemView.findViewById(R.id.TV_NumberSelected);
        messagesNumTV = itemView.findViewById(R.id.TV_NumberMessages);

        Button toggleServerButton = itemView.findViewById(R.id.B_ToggleServer);
        toggleServerButton.setOnClickListener((button) -> {
            Intent serviceIntent = new Intent(itemView.getContext(), ServerService.class);
            serviceIntent.setAction(ServerService.ACTION_TOGGLE_SERVER);
            itemView.getContext().startService(serviceIntent);
        });
        LiveDataSingletons.serverStatusObserver.observe(fragment.getViewLifecycleOwner(), running -> {
            if (running) {
                ViewCompat.setBackgroundTintList(toggleServerButton, ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.status_on)));
                toggleServerButton.setText(R.string.server_on);
            } else {
                ViewCompat.setBackgroundTintList(toggleServerButton, ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.status_off)));
                toggleServerButton.setText(R.string.server_off);
            }
            SharedInfo.sIsWebServerOn = running;
        });

        viewCertB = itemView.findViewById(R.id.B_ViewCert);
        updateViewCertButtonStatus();

        RoundDialog certInfoDialog = new RoundDialog(itemView.getContext());
        certInfoDialog.setView(R.layout.dialog_cert_info);
        certInfoDialog.setCornerRadius((int) Utils.dpToPx(18));
        View certInfoDialogLayout = certInfoDialog.getView();

        certInfoDialogLayout.findViewById(R.id.B_CertInfoOkButton).setOnClickListener((view) -> {
            certInfoDialog.dismiss();
        });

        viewCertB.setOnClickListener((view) -> {
            CertFingerprints certFingerprints = CertificateManagerWrapper.getCertSha256Fingerprints();
            TextView certFingerprintsNotCreatedTV = certInfoDialogLayout.findViewById(R.id.TV_CertFingerprintNotCreatedYet);
            ConstraintLayout certFingerprintContent = certInfoDialogLayout.findViewById(R.id.CL_CertInfoContent);

            if( certFingerprints.certSha256Fingerprint.isEmpty() || certFingerprints.publicKeySha256Fingerprint.isEmpty()) {
                certFingerprintContent.setVisibility(View.GONE);
                certFingerprintsNotCreatedTV.setVisibility(View.VISIBLE);
            } else {
                certFingerprintContent.setVisibility(View.VISIBLE);
                certFingerprintsNotCreatedTV.setVisibility(View.GONE);
                TextView certSHA256TV = certInfoDialogLayout.findViewById(R.id.TV_CertSHA256Fingerprint);
                TextView pubKeySHA256TV = certInfoDialogLayout.findViewById(R.id.TV_PublicKeySHA256Fingerprint);
                certSHA256TV.setText(certFingerprints.certSha256Fingerprint);
                pubKeySHA256TV.setText(certFingerprints.publicKeySha256Fingerprint);
            }
            certInfoDialog.show();
        });

        if (!Sharable.sharablesList.isEmpty()) {
            filesNumTV.setText(String.valueOf(Sharable.sharablesList.size()));
            filesNumTV.setVisibility(View.VISIBLE);
        }
        if (!User.noUsers()) {
            usersNumTV.setText(String.valueOf(User.usersCount()));
            usersNumTV.setVisibility(View.VISIBLE);
        }

        addItemsB.setOnClickListener((button) -> this.fragment.launcher.launch(new Intent(itemView.getContext(), AddAppsAndFilesActivity.class)));
        messagesB.setOnClickListener((button) -> {
            Intent intent = new Intent(itemView.getContext(), MessagesActivity.class);
            itemView.getContext().startActivity(intent);
        });

        updateUnseenMessagesNum();
        LiveDataSingletons.messagesNotifier.observe(this.fragment.getViewLifecycleOwner(), (messageCount) -> {
            updateUnseenMessagesNum();
        });

        Resources res = itemView.getResources();
        // setup QR Code dialog
        RoundDialog QRDialogRD = new RoundDialog(itemView.getContext());
        QRDialogRD.setView(R.layout.dialog_qrcode);
        View QRDialogView = QRDialogRD.getView();
        QRDialogRD.setCornerRadius((int) Utils.dpToPx(18));

        QRImageIV = QRDialogView.findViewById(R.id.IV_QRCodeImage);
        serverLinkTV = QRDialogView.findViewById(R.id.TV_ServerLink);
        QRCodeErrorText = QRDialogView.findViewById(R.id.TV_QRDialogConnectToNetwork);

        QRDialogView.findViewById(R.id.B_QRDialogOkButton)
                .setOnClickListener((v) -> QRDialogRD.dismiss());

        QRCodeB.setOnClickListener(button -> {
            QRDialogRD.setBackgroundColor(res.getColor(sDarkMode ? R.color.dialogColorDark : R.color.dialogColorLight));
            QRDialogRD.show();
            setupQrCode();
        });

        // setup Chosen files dialog
        RoundDialog chosenFilesRD = new RoundDialog(itemView.getContext());
        chosenFilesRD.setView(R.layout.dialog_chosen_files);
        View chosenFilesView = chosenFilesRD.getView();

        chosenFilesRD.setCornerRadius((int) Utils.dpToPx(18));
        RecyclerView chosenFilesRecycler = chosenFilesView.findViewById(R.id.RV_ChosenFilesRecycler);
        TextView noFilesTV = chosenFilesView.findViewById(R.id.TV_NoFilesSelected);
        ChosenFilesAdapter chosenFilesAdapter = new ChosenFilesAdapter();
        chosenFilesRecycler.setAdapter(chosenFilesAdapter);
        chosenFilesRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));


        Button okButton = chosenFilesView.findViewById(R.id.B_ChosenFilesOkButton);
        okButton.setOnClickListener((v) -> {
            chosenFilesRD.dismiss();
        });

        showSelected.setOnClickListener(button -> {
            chosenFilesRD.setBackgroundColor(res.getColor(sDarkMode ? R.color.dialogColorDark : R.color.dialogColorLight));
            chosenFilesRD.show();
        });

        LiveDataSingletons.downloadsListNotifier.observe(this.fragment.getViewLifecycleOwner(), info -> {
            char action = info.getChar("action");
            int size = DownloadItemsManager.getDownloadItemsCount();
            if ('R' == action) {
                int index = info.getInt("index");
                chosenFilesAdapter.notifyItemRemoved(index);
            }
            if (size == 0) {
                filesNumTV.setText("0");
                filesNumTV.setVisibility(View.GONE);
                noFilesTV.setVisibility(View.VISIBLE);
            } else {
                filesNumTV.setText(String.valueOf(size));
                filesNumTV.setVisibility(View.VISIBLE);
                noFilesTV.setVisibility(View.GONE);
            }
            UsersNotifier.notifyDownloadsChanged();
        });

        // setup users dialog
        RoundDialog usersDialogRD = new RoundDialog(itemView.getContext());
        usersDialogRD.setView(R.layout.dialog_users);
        View usersDialogView = usersDialogRD.getView();
        usersDialogRD.setCornerRadius((int) Utils.dpToPx(18));

        RecyclerView usersRecycler = usersDialogView.findViewById(R.id.RV_UsersRecycler);
        TextView noUserConnectedTV = usersDialogView.findViewById(R.id.TV_NoUserConnected);
        UsersAdapter usersAdapter = new UsersAdapter();
        usersRecycler.setAdapter(usersAdapter);
        usersRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

        usersDialogView.findViewById(R.id.B_UsersOkButton)
                .setOnClickListener((v) -> usersDialogRD.dismiss());

        showUsersB.setOnClickListener(button -> {
            usersDialogRD.setBackgroundColor(res.getColor(sDarkMode ? R.color.dialogColorDark : R.color.dialogColorLight));
            usersDialogRD.show();
        });


        LiveDataSingletons.usersListObserver.observe(this.fragment.getViewLifecycleOwner(), info -> {
            char action = info.getChar("action");
            int size = User.usersCount();
            int index = info.getInt("index");
            if ('A' == action) {
                usersAdapter.notifyItemInserted(index);
            } else if ('C' == action) {
                usersAdapter.notifyItemChanged(index);
            }
            if (size == 0) {
                usersNumTV.setText("0");
                usersNumTV.setVisibility(View.GONE);
                noUserConnectedTV.setVisibility(View.VISIBLE);
            } else {
                usersNumTV.setText(String.valueOf(size));
                usersNumTV.setVisibility(View.VISIBLE);
                noUserConnectedTV.setVisibility(View.GONE);
            }
        });
    }


    public void updateUnseenMessagesNum() {
        if (unseenMessagesCount > 0) {
            messagesNumTV.setVisibility(View.VISIBLE);
            messagesNumTV.setText(String.valueOf(unseenMessagesCount));
        } else {
            messagesNumTV.setVisibility(View.GONE);
            messagesNumTV.setText("0");
        }
    }

    // Show or hide viewCert Button based on Server.IS_HTTPS value
    public void updateViewCertButtonStatus() {
        if( WebServer.IS_HTTPS ) {
            viewCertB.setVisibility(View.VISIBLE);
        } else {
            viewCertB.setVisibility(View.GONE);
        }

    }

    private void setupQrCode() {
        String ip = ServerService.getIpAddress();
        MainActivity activity = (MainActivity) fragment.requireActivity();
        if (!SharedInfo.sIsWebServerOn) {
            QRCodeErrorText.setText(R.string.toggle_on_the_server);
            QRCodeErrorText.setVisibility(View.VISIBLE);
            serverLinkTV.setVisibility(View.GONE);
            QRImageIV.setVisibility(View.GONE);
            return;
        }

        if (ip != null) {
            QRCodeErrorText.setVisibility(View.GONE);
            serverLinkTV.setVisibility(View.VISIBLE);
            QRImageIV.setVisibility(View.VISIBLE);

            String link = (WebServer.IS_HTTPS ? "https" : "http") + "://" + ip + ":" + WebServer.PORT_NUMBER;
            serverLinkTV.setText(link);
            byte[] qrCodeBytes = Utils.encodeTextToQR(link);
            Bitmap qrCodeBitmap = Utils.QrCodeArrayToBitmap(qrCodeBytes, sDarkMode);
            // Display the bitmap in an ImageView or any other suitable view
            QRImageIV.setImageBitmap(qrCodeBitmap);
        } else {
            QRCodeErrorText.setText(R.string.connect_to_wifi_or_hotspot);
            QRCodeErrorText.setVisibility(View.VISIBLE);
            serverLinkTV.setVisibility(View.GONE);
            QRImageIV.setVisibility(View.GONE);
        }
    }
}
