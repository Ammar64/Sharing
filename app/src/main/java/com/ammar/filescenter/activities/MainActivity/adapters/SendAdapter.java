package com.ammar.filescenter.activities.MainActivity.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.models.Upload;
import com.ammar.filescenter.services.progress.ProgressSendWatcher;
import com.ammar.filescenter.utils.Utils;

import java.util.List;


// this adapter is for files that is currently uploading.
public class SendAdapter extends RecyclerView.Adapter<SendAdapter.ViewHolder> {

    public SendAdapter() {}

    @NonNull
    @Override
    public SendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.row_upload_view, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setup(ProgressSendWatcher.progressSendWatchers.get(position));
        holder.setFileListener(position);
    }

    @Override
    public int getItemCount() {
        return ProgressSendWatcher.progressSendWatchers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTV;
        TextView fileSizeTV;
        ProgressBar fileProgressPB;
        TextView fileProgressTV;
        Button removeFileB;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTV  = itemView.findViewById(R.id.TV_FileUploadName);
            fileSizeTV  = itemView.findViewById(R.id.TV_FileUploadSize);
            fileProgressPB = itemView.findViewById(R.id.PB_FileUploadProgress);
            fileProgressTV = itemView.findViewById(R.id.TV_FileUploadProgress);
            removeFileB = itemView.findViewById(R.id.B_StopFileUpload);
        }

        public void setup(ProgressSendWatcher watcher) {
            setFileName(watcher.getFile().getFileName());
            setFileSizeTV(watcher.getFile().getSize());
            setProgress(watcher.getPercentage());
        }
        public void setFileListener(int indexCancel) {
//            Intent serviceIntent = new Intent(itemView.getContext(), NetworkService.class);
//            serviceIntent.setAction(NetworkService.ACTION_CANCEL_UPLOAD);
//            serviceIntent.putExtra(NetworkService.EXTRA_UPLOAD_CANCEL, indexCancel);
//            itemView.getContext().startService(serviceIntent);
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(long size) {
            fileSizeTV.setText(Utils.getFormattedSize(size));
        }
        public void setProgress(int progress) {
            fileProgressPB.setProgress(progress);
            fileProgressTV.setText(progress + "%");
        }
    }
}
