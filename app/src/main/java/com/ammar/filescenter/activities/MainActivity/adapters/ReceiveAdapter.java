package com.ammar.filescenter.activities.MainActivity.adapters;

import android.content.res.ColorStateList;
import android.icu.util.MeasureUnit;
import android.util.TypedValue;
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
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {
    public static final int TYPE_RECEIVE = 0;
    public static final int TYPE_SEND = 1;

    List<Download> downloads;
    int type;
    public DownloadsAdapter(List<Download> downloads, int type) {
        this.downloads = downloads;
        this.type = type;
    }

    @Override
    public int getItemViewType(int position) {
        return type;
    }

    @NonNull
    @Override
    public DownloadsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_download_view, parent, false);
        return new ViewHolder(view, type);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadsAdapter.ViewHolder holder, int position) {
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
        private final TextView operation;
        public ViewHolder(@NonNull View itemView, int type) {
            super(itemView);
            this.type = type;
            filenameView = itemView.findViewById(R.id.TV_FileName);
            filesizeView = itemView.findViewById(R.id.TV_FileSize);
            progressBar = itemView.findViewById(R.id.PB_DownloadProgressBar);
            operation = itemView.findViewById(R.id.TV_Operation);

            if (type == DownloadsAdapter.TYPE_RECEIVE) {
                operation.setBackgroundResource(R.drawable.download_icon);
                operation.setBackgroundTintList(ColorStateList.valueOf(itemView.getResources().getColor(R.color.transparent_download_icon)));
                float side = Utils.pxToDp(100, itemView.getResources().getDisplayMetrics());
                operation.setLayoutParams(new ViewGroup.LayoutParams((int) side, (int) side));
                operation.setText("");
            } else if (type == DownloadsAdapter.TYPE_SEND) {
                operation.setBackgroundResource(0);
                operation.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                operation.setBackgroundTintList(null);

            }
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

        private final int type;
    }
}
