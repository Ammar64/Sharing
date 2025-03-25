package com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders;

import static com.ammar.sharing.activities.MainActivity.MainActivity.darkMode;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.activities.AddFilesActivity.AddFilesActivity;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.ChosenFilesAdapter;
import com.ammar.sharing.activities.MainActivity.adaptersR.UsersAdapter;
import com.ammar.sharing.activities.MainActivity.fragments.ShareFragment;
import com.ammar.sharing.activities.MessagesActivity.MessagesActivity;
import com.ammar.sharing.activities.StreamingActivity.StreamingActivity;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
import com.ammar.sharing.custom.ui.RoundDialog;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Server;
import com.ammar.sharing.services.ServerService;

// This is just the first row in the recycler view
public class HeaderViewHolder extends RecyclerView.ViewHolder {
    private final ImageView QRImageIV;
    private final AppCompatTextView serverLinkTV;
    private final AdaptiveTextView QRCodeErrorText;
    private final ShareFragment fragment;
    private final TextView messagesNumTV;

    public static int unseenMessagesCount = 0;

    public HeaderViewHolder(@NonNull View itemView, ShareFragment fragment) {
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

        if (!Sharable.sharablesList.isEmpty()) {
            filesNumTV.setText(String.valueOf(Sharable.sharablesList.size()));
            filesNumTV.setVisibility(View.VISIBLE);
        }
        if (!User.users.isEmpty()) {
            usersNumTV.setText(String.valueOf(User.users.size()));
            usersNumTV.setVisibility(View.VISIBLE);
        }

        addItemsB.setOnClickListener((button) -> this.fragment.launcher.launch(new Intent(itemView.getContext(), AddAppsAndFilesActivity.class)));
        streamingB.setOnClickListener((button) -> itemView.getContext().startActivity(new Intent(itemView.getContext(), StreamingActivity.class)));
        messagesB.setOnClickListener((button) -> {
            Intent intent = new Intent(itemView.getContext(), MessagesActivity.class);
            itemView.getContext().startActivity(intent);
        });

        updateUnseenMessagesNum();
        Data.messagesNotifier.observe(this.fragment.getViewLifecycleOwner(), (messageCount) -> {
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
            QRDialogRD.setBackgroundColor(res.getColor(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight));
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
            chosenFilesRD.setBackgroundColor(res.getColor(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight));
            chosenFilesRD.show();
        });

        Data.downloadsListNotifier.observe(this.fragment.getViewLifecycleOwner(), info -> {
            char action = info.getChar("action");
            int size = Sharable.sharablesList.size();
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
            User.informAllUsersThat(User.INFO.AVAILABLE_DOWNLOADS_UPDATED);
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
            usersDialogRD.setBackgroundColor(res.getColor(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight));
            usersDialogRD.show();
        });


        Data.usersListObserver.observe(this.fragment.getViewLifecycleOwner(), info -> {
            char action = info.getChar("action");
            int size = User.users.size();
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

    private void setupQrCode() {
        String ip = ServerService.getIpAddress();
        MainActivity activity = (MainActivity) fragment.requireActivity();
        if (!activity.isServerOn) {
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

            String link = "http://" + ip + ":" + Server.PORT_NUMBER;
            serverLinkTV.setText(link);
            byte[] qrCodeBytes = Utils.encodeTextToQR(link);
            Bitmap qrCodeBitmap = Utils.QrCodeArrayToBitmap(qrCodeBytes, darkMode);
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
