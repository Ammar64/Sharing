package com.ammar.sharing.activities.MainActivity.adaptersR;

import static com.ammar.sharing.activities.MainActivity.MainActivity.darkMode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsActivity.AddAppsActivity;
import com.ammar.sharing.activities.AddFilesActivity.AddFilesActivity;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.activities.MainActivity.fragments.ShareFragment;
import com.ammar.sharing.activities.MessagesActivity.MessagesActivity;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.io.ProgressManager;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
import com.ammar.sharing.custom.ui.RoundDialog;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.User;
import com.ammar.sharing.services.ServerService;

import java.util.Locale;

// this adapter for the recycler view you see when you open the app
// it takes the entire screen except for the top and bottom bars
public class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_PROGRESS = 2;
    private static final int TYPE_FOOTER = 3;

    private final ShareFragment fragment;

    public ShareAdapter(ShareFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        else if (position == getItemCount() - 1) return TYPE_FOOTER;
        else return TYPE_PROGRESS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_PROGRESS:
                LayoutInflater progressInflater = LayoutInflater.from(parent.getContext());
                View layout = progressInflater.inflate(R.layout.row_transfer_view, parent, false);
                return new ProgressViewHolder(layout);
            case TYPE_HEADER:
                LayoutInflater headerInflater = LayoutInflater.from(parent.getContext());
                View header = headerInflater.inflate(R.layout.row_transfer_header, parent, false);
                return new HeaderViewHolder(header, fragment);
            case TYPE_FOOTER: // this is bottom app bar
                Space space = new Space(parent.getContext());
                TypedValue out = new TypedValue();
                parent.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, out, true);
                int size = TypedValue.complexToDimensionPixelSize(out.data, parent.getContext().getResources().getDisplayMetrics());
                size += fragment.requireActivity().findViewById(R.id.FAB_ServerButton).getMeasuredHeight();
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, size);
                space.setLayoutParams(params);
                return new RecyclerView.ViewHolder(space) {
                };
        }
        throw new RuntimeException("Invalid View Type in TransferAdapter");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_PROGRESS:
                ((ProgressViewHolder) holder).setup(ProgressManager.progresses.get(position - 1));
                break;
            case TYPE_HEADER:
                ((HeaderViewHolder) holder).updateUnseenMessagesNum();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return ProgressManager.progresses.size() + 2;
    }


    // This is just the first row in the recycler view
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ImageView QRImageIV;
        private final AppCompatTextView serverLinkTV;
        private final AdaptiveTextView QRCodeErrorText;
        private final ShareFragment fragment;
        private final TextView messagesNumTV;

        public static int unseenMessagesCount = 0;
        public HeaderViewHolder(@NonNull View itemView, ShareFragment fragment) {
            super(itemView);
            this.fragment = fragment;
            ImageButton addAppsB = itemView.findViewById(R.id.B_AddApps);
            ImageButton addFilesB = itemView.findViewById(R.id.B_AddFiles);
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

            addAppsB.setOnClickListener((button) -> this.fragment.launcher.launch(new Intent(itemView.getContext(), AddAppsActivity.class)));
            addFilesB.setOnClickListener((button) -> this.fragment.mGetContent.launch(new Intent(this.fragment.requireContext(), AddFilesActivity.class)));
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
            usersDialogRD.setCornerRadius((int)Utils.dpToPx(18));

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


        protected void updateUnseenMessagesNum() {
            if( unseenMessagesCount > 0 ) {
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
            if( !activity.isServerOn ) {
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

                String link = "http://" + ip + ":" + ServerService.PORT_NUMBER;
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


    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTV;
        AppCompatImageButton stopB;
        TextView operationTV;
        TextView transferInfoTV;
        ProgressBar fileProgressPB;
        TextView fileProgressTV;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTV = itemView.findViewById(R.id.TV_SharedFileName);
            stopB = itemView.findViewById(R.id.B_StopSharing);
            operationTV = itemView.findViewById(R.id.TV_OperationType);
            transferInfoTV = itemView.findViewById(R.id.TV_FileTransferInfo);
            fileProgressPB = itemView.findViewById(R.id.PB_SharedFileProgress);
            fileProgressTV = itemView.findViewById(R.id.TV_SharedFileProgress);
        }

        private final Handler handler = new Handler();

        public void setup(ProgressManager manager) {
            setFileName(manager.getDisplayName());
            setFileTransferInfo(manager);
            handler.post(() -> setProgress(manager));
            setOperationText(manager);
            setClickListeners(manager);
        }

        private void setOperationText(ProgressManager manager) {
            ProgressManager.OP operation = manager.getOperation();

            String operationText;
            Context ctx = itemView.getContext();
            String username = manager.getUser().getName();
            switch (operation) {
                case DOWNLOAD:
                    if (manager.getLoaded() == ProgressManager.COMPLETED)
                        operationText = ctx.getString(R.string.sending_to_user_done, username, Utils.getFormattedTime(manager.getTotalTime()));
                    else if (manager.getLoaded() == ProgressManager.STOPPED_BY_REMOTE)
                        operationText = ctx.getString(R.string.sending_to_user_stopped, username);
                    else
                        operationText = ctx.getString(R.string.sending_to_user, username);
                    break;
                case UPLOAD:
                    if (manager.getLoaded() == ProgressManager.COMPLETED)
                        operationText = ctx.getString(R.string.receiving_from_user_done, username, Utils.getFormattedTime(manager.getTotalTime()));
                    else if (manager.getLoaded() == ProgressManager.STOPPED_BY_REMOTE)
                        operationText = ctx.getString(R.string.receiving_from_user_stopped, username);
                    else
                        operationText = ctx.getString(R.string.receiving_from_user, username);
                    break;
                default:
                    throw new RuntimeException("UnknownOP");
            }
            operationTV.setText(operationText);
        }

        private void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        private void setFileTransferInfo(ProgressManager manager) {
            switch ((int) manager.getLoaded()) {
                case ProgressManager.COMPLETED:
                    transferInfoTV.setText(R.string.completed);
                    return;
                case ProgressManager.STOPPED_BY_REMOTE:
                    transferInfoTV.setText(R.string.stopped);
                    return;
            }

            String loaded = Utils.getFormattedSize(manager.getLoaded());
            String total = Utils.getFormattedSize(manager.getTotal());
            String bytesPerSecond = Utils.getFormattedSize(manager.getSpeed());

            if (manager.getTotal() == -1 && manager.getLoaded() != ProgressManager.COMPLETED) {
                transferInfoTV.setText(String.format(Locale.ENGLISH, "%s (%s/S)", loaded, bytesPerSecond));
            } else
                transferInfoTV.setText(String.format(Locale.ENGLISH, "%s / %s   (%s/S)", loaded, total, bytesPerSecond));
        }

        private void setProgress(ProgressManager manager) {
            if (manager.getLoaded() < 0) {
                stopB.setImageResource(R.drawable.icon_minus);
                fileProgressTV.setText("");
                fileProgressTV.setVisibility(View.INVISIBLE);

                fileProgressPB.setIndeterminate(false);
                fileProgressPB.setProgress(100);
                fileProgressPB.setPaddingRelative(0, 0, 0, 0);

                int c;
                switch ((int) manager.getLoaded()) {
                    case ProgressManager.COMPLETED:
                        c = Color.GREEN;
                        break;
                    case ProgressManager.STOPPED_BY_REMOTE:
                        c = Color.YELLOW;
                        break;
                    case ProgressManager.STOPPED_BY_USER:
                        c = Color.RED;
                        break;
                    default:
                        throw new RuntimeException("Invalid progress status. progress is " + manager.getLoaded());
                }
                DrawableCompat.setTint(fileProgressPB.getProgressDrawable(), c);
                return;
            } else stopB.setImageResource(R.drawable.icon_x);

            if (manager.getTotal() != -1) {
                int progress = manager.getPercentage();

                fileProgressTV.setVisibility(View.VISIBLE);
                fileProgressTV.setText(String.format(Locale.ENGLISH, "%d%%", progress));
                DrawableCompat.setTint(fileProgressPB.getProgressDrawable(), Color.CYAN);
                fileProgressPB.setIndeterminate(false);
                fileProgressPB.setProgress(progress);
                fileProgressPB.setPaddingRelative(0, 0, (int) Utils.dpToPx(8.0f), 0);
            } else {

                fileProgressTV.setVisibility(View.INVISIBLE);
                fileProgressPB.setPaddingRelative(0, 0, 0, 0);
                DrawableCompat.setTint(fileProgressPB.getProgressDrawable(), Color.CYAN);

                if (manager.getLoaded() == ProgressManager.COMPLETED) {
                    fileProgressPB.setIndeterminate(false);
                    fileProgressPB.setProgress(100);
                } else {
                    fileProgressPB.setProgress(0);
                    fileProgressPB.setIndeterminate(true);
                }
            }

        }

        private void setClickListeners(ProgressManager manager) {

            if (manager.getOperation() == ProgressManager.OP.UPLOAD && manager.getLoaded() == ProgressManager.COMPLETED) {
                itemView.setClickable(true);
                itemView.setFocusable(true);
                itemView.setOnClickListener((view) -> {
                    String type = manager.getFileType();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Sharable sharable = manager.getSharable();

                    Uri uri = sharable.isUri() ? sharable.getUri() : FileProvider.getUriForFile(
                            itemView.getContext(),
                            itemView.getContext().getApplicationContext().getPackageName() + ".provider",
                            sharable.getFile());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, type);
                    itemView.getContext().startActivity(intent);
                });

            } else {
                itemView.setClickable(false);
                itemView.setFocusable(false);
                itemView.setOnClickListener(null);
            }

            stopB.setOnClickListener((view) -> {
                if (manager.getLoaded() >= 0) {
                    manager.stop();
                } else
                    ProgressManager.removeProgress(manager.getIndex());
            });
        }
    }

}