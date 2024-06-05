package com.ammar.filescenter.activities.MainActivity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.custom.io.ProgressManager;
import com.ammar.filescenter.utils.Utils;

import java.util.Locale;


// this adapter is for files that is currently uploading.
public class SendAdapter extends RecyclerView.Adapter<SendAdapter.ViewHolder> {

    public SendAdapter() {

    }

    @NonNull
    @Override
    public SendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.row_upload_view, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setup(ProgressManager.progresses.get(position));
        holder.setFileListener(position);
    }

    @Override
    public int getItemCount() {
        return ProgressManager.progresses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTV;
        TextView operationTV;
        TextView transferInfoTV;
        ProgressBar fileProgressPB;
        TextView fileProgressTV;
        Button removeFileB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTV = itemView.findViewById(R.id.TV_FileUploadName);
            operationTV = itemView.findViewById(R.id.TV_OperationType);
            transferInfoTV = itemView.findViewById(R.id.TV_FileTransferInfo);
            fileProgressPB = itemView.findViewById(R.id.PB_FileUploadProgress);
            fileProgressTV = itemView.findViewById(R.id.TV_FileUploadProgress);
            removeFileB = itemView.findViewById(R.id.B_StopFileUpload);
        }

        public void setup(ProgressManager manager) {
            setFileName(manager.getFileName());
            setFileTransferInfo(manager);
            setProgress(manager.getPercentage());
            setOperationText(manager);
        }

        private void setOperationText(ProgressManager manager) {
            ProgressManager.OP operation = manager.getOperation();

            String operationText;
            switch (operation) {
                case DOWNLOAD:
                    if (manager.getLoaded() == ProgressManager.COMPLETED)
                        operationText = "Sent to";
                    else
                        operationText = "Sending to";
                    break;
                case UPLOAD:
                    if (manager.getLoaded() == ProgressManager.COMPLETED)
                        operationText = "Received from";
                    else
                        operationText = "Receiving from";
                    break;
                default:
                    throw new RuntimeException("UnknownOP");
            }
            operationTV.setText(String.format("%s %s", operationText, manager.getRemoteIp()));
        }

        public void setFileListener(int indexCancel) {
//            Intent serviceIntent = new Intent(itemView.getContext(), NetworkService.class);
//            serviceIntent.setAction(NetworkService.ACTION_CANCEL_UPLOAD);
//            serviceIntent.putExtra(NetworkService.EXTRA_UPLOAD_CANCEL, indexCancel);
//            itemView.getContext().startService(serviceIntent);
        }

        private void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        private void setFileTransferInfo(ProgressManager manager) {
            if (manager.getLoaded() == ProgressManager.COMPLETED) {
                transferInfoTV.setText("Completed");
                return;
            }
            String loaded = Utils.getFormattedSize(manager.getLoaded());
            String total = Utils.getFormattedSize(manager.getSize());
            String bytesPerSecond = Utils.getFormattedSize(manager.getSpeed());

            transferInfoTV.setText(String.format(Locale.ENGLISH, "%s / %s   (%s/S)", loaded, total, bytesPerSecond));
        }

        private void setProgress(int progress) {
            fileProgressPB.setProgress(progress);
            fileProgressTV.setText(String.format(Locale.ENGLISH, "%d%%", progress));
        }
    }
}
