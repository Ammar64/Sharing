package com.ammar.sharing.activities.MainActivity.adaptersR;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.nativebackend.DownloadItemsManager;
import com.ammar.sharing.nativebackend.DownloadItem;
import com.ammar.sharing.services.ServerService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Map;
import java.util.TreeMap;

public class ChosenFilesAdapter extends RecyclerView.Adapter<ChosenFilesAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.row_chosen_files, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setup(position);
    }

    @Override
    public int getItemCount() { return DownloadItemsManager.getDownloadItemsCount(); }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView fileIconIV;
        final TextView fileNameTV;
        final TextView fileSizeTV;

        final View removeB;
        Map<Integer, Drawable> appsIconCache = new TreeMap<>();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconIV = itemView.findViewById(R.id.IV_FileChosenIcon);
            fileNameTV = itemView.findViewById(R.id.TV_FileChosenName);
            fileSizeTV = itemView.findViewById(R.id.TV_FileChosenSize);
            removeB = itemView.findViewById(R.id.FileChosenDelete);
        }

        public void setup(int pos) {
            DownloadItem file = DownloadItemsManager.getDownloadItem(pos);
            setFileName(file.getName());
            setFileIconIV(file, pos);
            setFileSizeTV(file);
            setFileListener();
        }

        public void setFileListener() {
            removeB.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if( pos != RecyclerView.NO_POSITION ) {
                    Intent serviceIntent = new Intent(itemView.getContext(), ServerService.class);
                    serviceIntent.setAction(ServerService.ACTION_REMOVE_DOWNLOAD);
                    serviceIntent.putExtra(ServerService.EXTRA_DOWNLOAD_INDEX, pos);
                    itemView.getContext().startService(serviceIntent);
                }
            });
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(DownloadItem item) {
            fileSizeTV.setText(Utils.getFormattedSize(item.getSize()));
        }

        public void setFileIconIV(@NonNull DownloadItem file, int pos) {
            String mimeType = file.getMimeType();
            int imageSize = (int) Utils.dpToPx(40);
            RequestManager request = Glide.with(itemView.getContext());
            RequestBuilder<Drawable> builder;
            ParcelFileDescriptor pfd = DownloadItemsManager.openDownloadItem(pos);
            if (mimeType.startsWith("image/")) {
                builder = request.load(pfd);
            } else if (mimeType.startsWith("audio/")) {
                builder = request.load(R.drawable.ic_audio);
            } else if (mimeType.startsWith("video/")) {
                builder = request.load(R.drawable.ic_video);
            } else {
                builder = request.load(R.drawable.ic_file);
            }
            builder.diskCacheStrategy(DiskCacheStrategy.NONE).override(imageSize, imageSize).into(fileIconIV);
        }
    }
}
