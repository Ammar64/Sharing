package com.ammar.filescenter.activities.MainActivity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.models.Download;
import com.ammar.filescenter.utils.Utils;

import java.util.List;
public class ReceiveAdapter extends RecyclerView.Adapter<ReceiveAdapter.ViewHolder> {

    List<Download> downloads;
    public ReceiveAdapter(List<Download> downloads) {
        this.downloads = downloads;
    }

    @NonNull
    @Override
    public ReceiveAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_download_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveAdapter.ViewHolder holder, int position) {
        holder.setFileName(downloads.get(position).getName());
        holder.setFileSize(downloads.get(position).getSize());
        holder.setProgress(downloads.get(position).getLoaded());
    }
    @Override
    public int getItemCount() {
        return downloads.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView filenameView;
        private final TextView filesizeView;
        private final ProgressBar progressBar;
        private final TextView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filenameView = itemView.findViewById(R.id.TV_FileName);
            filesizeView = itemView.findViewById(R.id.TV_FileSize);
            progressBar = itemView.findViewById(R.id.PB_DownloadProgressBar);
            icon = itemView.findViewById(R.id.TV_DownloadIcon);

        }
        public void setProgress(int progress) {
            progressBar.setProgress(progress);
        }

        public void setFileName(String fileName) {
            filenameView.setText(fileName);
        }

        public void setFileSize(int fileSize) {
            filesizeView.setText(Utils.getFormattedSize(fileSize));
        }

    }
}
