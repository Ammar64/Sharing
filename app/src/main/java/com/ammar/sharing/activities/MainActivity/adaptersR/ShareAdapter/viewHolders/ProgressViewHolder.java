package com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.viewHolders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.io.ProgressManager;
import com.ammar.sharing.models.Sharable;

import java.util.Locale;

public class ProgressViewHolder extends RecyclerView.ViewHolder {
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
                new Thread(manager::stop).start();
            } else
                ProgressManager.removeProgress(manager.getIndex());
        });
    }
}
