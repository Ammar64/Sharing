package com.ammar.filescenter.activities.MainActivity.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity;
import com.ammar.filescenter.activities.MainActivity.fragments.TransferFragment;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.services.NetworkService;

import java.util.Locale;


// this adapter is for files that is currently uploading.
public class TransferAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_PROGRESS = 2;
    private static final int TYPE_FOOTER = 3;

    private final TransferFragment fragment;
    public TransferAdapter(TransferFragment fragment) {
        this.fragment = fragment;
    }
    @Override
    public int getItemViewType(int position) {
        if( position == 0 ) return TYPE_HEADER;
        else if ( position == getItemCount()-1 ) return TYPE_FOOTER;
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
            case TYPE_FOOTER:
                Space space = new Space(parent.getContext());
                TypedValue out = new TypedValue();
                parent.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, out, true);
                int size = TypedValue.complexToDimensionPixelSize(out.data,parent.getContext().getResources().getDisplayMetrics());
                size += fragment.requireActivity().findViewById(R.id.FAB_ServerButton).getMeasuredHeight();
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, size);
                space.setLayoutParams(params);
                return new RecyclerView.ViewHolder(space) {};
        }
        throw new RuntimeException("Invalid View Type in TransferAdapter");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_PROGRESS:
                ((ProgressViewHolder) holder).setup(ProgressManager.progresses.get(position-1));
                break;
            case TYPE_HEADER:

                break;
        }
    }

    @Override
    public int getItemCount() {
        return ProgressManager.progresses.size() + 2;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTV;
        TextView operationTV;
        TextView transferInfoTV;
        ProgressBar fileProgressPB;
        TextView fileProgressTV;
        Button removeFileB;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTV = itemView.findViewById(R.id.TV_FileUploadName);
            operationTV = itemView.findViewById(R.id.TV_OperationType);
            transferInfoTV = itemView.findViewById(R.id.TV_FileTransferInfo);
            fileProgressPB = itemView.findViewById(R.id.PB_FileUploadProgress);
            fileProgressTV = itemView.findViewById(R.id.TV_FileUploadProgress);
            removeFileB = itemView.findViewById(R.id.B_StopFileUpload);
        }

        public void setup(ProgressManager manager) {
            setFileName(manager.getDisplayName());
            setFileTransferInfo(manager);
            setProgress(manager);
            setOperationText(manager);
            setClickListener(manager);
        }

        private void setOperationText(ProgressManager manager) {
            ProgressManager.OP operation = manager.getOperation();

            String operationText;
            switch (operation) {
                case DOWNLOAD:
                    if (manager.getLoaded() == ProgressManager.COMPLETED)
                        operationText = "Sent to";
                    else if (manager.getLoaded() == ProgressManager.FAILED)
                        operationText = "Send failed";
                    else
                        operationText = "Sending to";
                    break;
                case UPLOAD:
                    if (manager.getLoaded() == ProgressManager.COMPLETED)
                        operationText = "Received from";
                    else if (manager.getLoaded() == ProgressManager.FAILED)
                        operationText = "Receive failed";
                    else
                        operationText = "Receiving from";
                    break;
                default:
                    throw new RuntimeException("UnknownOP");
            }
            operationTV.setText(String.format("%s %s", operationText, manager.getUser().getName()));
        }


        private void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        private void setFileTransferInfo(ProgressManager manager) {
            switch ((int) manager.getLoaded()) {
                case ProgressManager.COMPLETED:
                    transferInfoTV.setText("Completed");
                    return;
                case ProgressManager.FAILED:
                    transferInfoTV.setText("Failed");
                    return;
                case ProgressManager.PAUSED:
                    transferInfoTV.setText("Paused");
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
                    case ProgressManager.FAILED:
                        c = Color.RED;
                        break;
                    case ProgressManager.PAUSED:
                        c = Color.YELLOW;
                        break;
                    default:
                        throw new RuntimeException("Invalid progress status. progress is " + manager.getLoaded());
                }
                fileProgressPB.setProgressTintList(ColorStateList.valueOf(c));
                return;
            }

            if (manager.getTotal() != -1) {
                int progress = manager.getPercentage();

                fileProgressTV.setVisibility(View.VISIBLE);
                fileProgressTV.setText(String.format(Locale.ENGLISH, "%d%%", progress));

                fileProgressPB.setProgressTintList(ColorStateList.valueOf(Color.CYAN));
                fileProgressPB.setIndeterminate(false);
                fileProgressPB.setProgress(progress);
                fileProgressPB.setPaddingRelative(0, 0, (int) Utils.dpToPx(8.0f), 0);
            } else {

                fileProgressTV.setVisibility(View.INVISIBLE);
                fileProgressPB.setPaddingRelative(0, 0, 0, 0);
                fileProgressPB.setProgressTintList(ColorStateList.valueOf(Color.CYAN));

                if (manager.getLoaded() == ProgressManager.COMPLETED) {
                    fileProgressPB.setIndeterminate(false);
                    fileProgressPB.setProgress(100);
                } else {
                    fileProgressPB.setProgress(0);
                    fileProgressPB.setIndeterminate(true);
                }
            }

        }

        private void setClickListener(ProgressManager manager) {
            if (manager.getOperation() == ProgressManager.OP.UPLOAD) {
                TypedValue outValue = new TypedValue();
                itemView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                itemView.setBackgroundResource(outValue.resourceId);

                itemView.setOnClickListener((view) -> {
                    String type = manager.getFileType();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri uri = FileProvider.getUriForFile(
                            itemView.getContext(),
                            itemView.getContext().getApplicationContext().getPackageName() +".provider",
                            manager.getFile() );
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, type);
                    itemView.getContext().startActivity(intent);
                });
            } else {
                itemView.setBackground(null);
                itemView.setOnClickListener(null);
            }
        }
    }
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ImageView QRImageIV;
        private final AppCompatTextView serverLinkTV;

        public HeaderViewHolder(@NonNull View itemView, TransferFragment fragment) {
            super(itemView);

            ImageButton addAppsB = itemView.findViewById(R.id.B_AddApps);
            ImageButton addFilesB = itemView.findViewById(R.id.B_AddFiles);
            ImageButton showSelected = itemView.findViewById(R.id.B_ShowSelected);
            ImageButton QRCodeB = itemView.findViewById(R.id.B_ShowAddress);
            ImageButton showUsersB = itemView.findViewById(R.id.B_ShowUsers);

            addAppsB.setOnClickListener((button) -> fragment.launcher.launch(new Intent(itemView.getContext(), AddAppsActivity.class)));
            addFilesB.setOnClickListener((button) -> fragment.mGetContent.launch("*/*"));
            // setup QR Code dialog
            View QRDialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_qrcode, null, false);
            AlertDialog QRDialogAD = new AlertDialog.Builder(itemView.getContext())
                    .setView(QRDialogView)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            QRImageIV = QRDialogView.findViewById(R.id.IV_QRCodeImage);
            serverLinkTV = QRDialogView.findViewById(R.id.TV_ServerLink);
            QRCodeB.setOnClickListener( button -> {
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
            ChosenFilesAdapter chosenFilesAdapter = new ChosenFilesAdapter();
            chosenFilesRecycler.setAdapter(chosenFilesAdapter);
            chosenFilesRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            showSelected.setOnClickListener( button -> chosenFilesAD.show());

            NetworkService.filesListNotifier.observe(fragment.getViewLifecycleOwner(), info -> {
                char action = info.getChar("action");
                if( 'R' == action ) {
                    int index = info.getInt("index");
                    chosenFilesAdapter.notifyItemRemoved(index);
                }
            });

            // setup users dialog
            View usersDialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.dialog_users, null, false);
            AlertDialog usersDialogAD = new AlertDialog.Builder(itemView.getContext())
                    .setView(usersDialogView)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            RecyclerView usersRecycler = usersDialogView.findViewById(R.id.RV_UsersRecycler);
            UsersAdapter usersAdapter = new UsersAdapter();
            usersRecycler.setAdapter(usersAdapter);
            usersRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            showUsersB.setOnClickListener( button -> usersDialogAD.show());

            NetworkService.usersListObserver.observe( fragment.getViewLifecycleOwner(), info -> {
                char action = info.getChar("action");
                if( 'A' == action ) {
                    int index = info.getInt("index");
                    usersAdapter.notifyItemInserted(index);
                }
            });
        }

        private void setupQrCode() {
            String link = "http://" + NetworkService.getIpAddress() + ":" + NetworkService.PORT_NUMBER;
            serverLinkTV.setText(link);
            byte[] qrCodeBytes = Utils.encodeTextToQR(link);
            Bitmap qrCodeBitmap = Utils.QrCodeArrayToBitmap(qrCodeBytes);
            // Display the bitmap in an ImageView or any other suitable view
            QRImageIV.setImageBitmap(qrCodeBitmap);
        }
    }
}

