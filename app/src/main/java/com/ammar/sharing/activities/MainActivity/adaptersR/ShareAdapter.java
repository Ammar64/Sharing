package com.ammar.sharing.activities.MainActivity.adaptersR;

import static com.ammar.sharing.activities.MainActivity.MainActivity.darkMode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.ammar.sharing.activities.MainActivity.fragments.ShareFragment;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.Utils;
import com.ammar.sharing.custom.io.ProgressManager;
import com.ammar.sharing.custom.ui.AdaptiveTextView;
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
        private final AdaptiveTextView connectToWifiOrHotspotTV;

        public HeaderViewHolder(@NonNull View itemView, ShareFragment fragment) {
            super(itemView);

            ImageButton addAppsB = itemView.findViewById(R.id.B_AddApps);
            ImageButton addFilesB = itemView.findViewById(R.id.B_AddFiles);
            AppCompatButton QRCodeB = itemView.findViewById(R.id.B_ShowAddress);

            ImageButton showSelected = itemView.findViewById(R.id.B_ShowSelected);
            ImageButton showUsersB = itemView.findViewById(R.id.B_ShowUsers);

            // setup badges
            TextView usersNumTV = itemView.findViewById(R.id.TV_NumberUsers);
            TextView filesNumTV = itemView.findViewById(R.id.TV_NumberSelected);

            if (!Sharable.sharablesList.isEmpty()) {
                filesNumTV.setText(String.valueOf(Sharable.sharablesList.size()));
                filesNumTV.setVisibility(View.VISIBLE);
            }
            if (!User.users.isEmpty()) {
                usersNumTV.setText(String.valueOf(User.users.size()));
                usersNumTV.setVisibility(View.VISIBLE);
            }

            addAppsB.setOnClickListener((button) -> fragment.launcher.launch(new Intent(itemView.getContext(), AddAppsActivity.class)));
            addFilesB.setOnClickListener((button) -> fragment.mGetContent.launch(new Intent(fragment.requireContext(), AddFilesActivity.class)));
            // setup QR Code dialog
            View QRDialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_qrcode, null, false);
            AlertDialog QRDialogAD = new AlertDialog.Builder(itemView.getContext())
                    .setView(QRDialogView)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            QRImageIV = QRDialogView.findViewById(R.id.IV_QRCodeImage);
            serverLinkTV = QRDialogView.findViewById(R.id.TV_ServerLink);
            connectToWifiOrHotspotTV = QRDialogView.findViewById(R.id.TV_QRDialogConnectToNetwork);

            QRCodeB.setOnClickListener(button -> {
                Window window = QRDialogAD.getWindow();
                if (window != null)
                    window.setBackgroundDrawableResource(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight);

                QRDialogAD.show();
                setupQrCode();
            });

            // setup Chosen files dialog
            View chosenFilesView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_chosen_files, null, false);
            AlertDialog chosenFilesAD = new AlertDialog.Builder(itemView.getContext())
                    .setView(chosenFilesView)
                    .setPositiveButton(R.string.ok, null)
                    .create();


            RecyclerView chosenFilesRecycler = chosenFilesView.findViewById(R.id.RV_ChosenFilesRecycler);
            TextView noFilesTV = chosenFilesView.findViewById(R.id.TV_NoFilesSelected);
            ChosenFilesAdapter chosenFilesAdapter = new ChosenFilesAdapter();
            chosenFilesRecycler.setAdapter(chosenFilesAdapter);
            chosenFilesRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            showSelected.setOnClickListener(button -> {
                Window window = chosenFilesAD.getWindow();
                if (window != null)
                    window.setBackgroundDrawableResource(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight);
                chosenFilesAD.show();
            });

            Data.filesListNotifier.observe(fragment.getViewLifecycleOwner(), info -> {
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
            });

            // setup users dialog
            View usersDialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_users, null, false);
            AlertDialog usersDialogAD = new AlertDialog.Builder(itemView.getContext())
                    .setView(usersDialogView)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            RecyclerView usersRecycler = usersDialogView.findViewById(R.id.RV_UsersRecycler);
            TextView noUserConnectedTV = usersDialogView.findViewById(R.id.TV_NoUserConnected);
            UsersAdapter usersAdapter = new UsersAdapter();
            usersRecycler.setAdapter(usersAdapter);
            usersRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            showUsersB.setOnClickListener(button -> {
                Window window = usersDialogAD.getWindow();
                if (window != null)
                    window.setBackgroundDrawableResource(darkMode ? R.color.dialogColorDark : R.color.dialogColorLight);
                usersDialogAD.show();
            });


            Data.usersListObserver.observe(fragment.getViewLifecycleOwner(), info -> {
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

        private void setupQrCode() {
            String ip = ServerService.getIpAddress();

            if (ip != null) {
                connectToWifiOrHotspotTV.setVisibility(View.GONE);
                serverLinkTV.setVisibility(View.VISIBLE);
                QRImageIV.setVisibility(View.VISIBLE);

                String link = "http://" + ip + ":" + ServerService.PORT_NUMBER;
                serverLinkTV.setText(link);
                byte[] qrCodeBytes = Utils.encodeTextToQR(link);
                Bitmap qrCodeBitmap = Utils.QrCodeArrayToBitmap(qrCodeBytes, darkMode);
                // Display the bitmap in an ImageView or any other suitable view
                QRImageIV.setImageBitmap(qrCodeBitmap);
            } else {
                connectToWifiOrHotspotTV.setVisibility(View.VISIBLE);
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
                if( manager.getLoaded() >= 0 ) {
                    manager.stop();
                }
                else
                    ProgressManager.removeProgress(manager.getIndex());
            });
        }
    }

}

