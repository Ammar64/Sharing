package com.ammar.sharing.activities.MainActivity.adaptersR;


import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.Sharable;
import com.ammar.sharing.models.SharableApp;
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
    public int getItemCount() {
        return Sharable.sharablesList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView fileIconIV;
        final TextView fileNameTV;
        final TextView fileSizeTV;

        final View removeB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconIV = itemView.findViewById(R.id.IV_FileChosenIcon);
            fileNameTV = itemView.findViewById(R.id.TV_FileChosenName);
            fileSizeTV = itemView.findViewById(R.id.TV_FileChosenSize);
            removeB = itemView.findViewById(R.id.FileChosenDelete);
        }

        public void setup(int pos) {
            Sharable file = Sharable.sharablesList.get(pos);
            setFileName(file.getName());
            setFileIconIV(file, pos);
            setFileSizeTV(file);
            setFileListener(file);
        }

        public void setFileListener(Sharable file) {
            removeB.setOnClickListener(v -> {
                Intent serviceIntent = new Intent(itemView.getContext(), ServerService.class);
                serviceIntent.setAction(Consts.ACTION_REMOVE_DOWNLOAD);
                serviceIntent.putExtra(Consts.EXTRA_DOWNLOAD_UUID, file.getUUID().toString());
                itemView.getContext().startService(serviceIntent);
            });
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(Sharable file) {
            fileSizeTV.setText(
                    (file instanceof SharableApp a && a.hasSplits()) ?
                    Utils.getRes().getString(R.string.splits):
                    Utils.getFormattedSize(file.getSize())
            );
        }

        Map<Integer, Drawable> appsIconCache = new TreeMap<>();

        public void setFileIconIV(@NonNull Sharable file, int pos) {
            String mimeType = file.getMimeType();
            int imageSize = (int) Utils.dpToPx(40);
            RequestManager request = Glide.with(itemView.getContext());
            RequestBuilder<Drawable> builder;
            if (file instanceof SharableApp) {
                SharableApp app = (SharableApp) file;
                builder = request.load(app.getIcon());
            } else if (mimeType.startsWith("image/")) {
                builder = request.load(file.isUri() ? file.getUri() : file.getFile());
            } else if(mimeType.startsWith("audio/")) {
                builder = request.load(R.drawable.icon_audio);
            } else if(mimeType.startsWith("video/")) {
                builder = request.load(R.drawable.icon_video);
            } else if ("application/vnd.android.package-archive".equals(mimeType)) {
                Drawable appIcon = appsIconCache.get(pos);
                if (appIcon == null) {
                    PackageManager pm = itemView.getContext().getApplicationContext().getPackageManager();
                    if( !file.isUri() ) {
                        PackageInfo packageInfo = pm.getPackageArchiveInfo(file.getFilePath(), 0);
                        if (packageInfo != null) {
                            ApplicationInfo appInfo = packageInfo.applicationInfo;
                            appInfo.sourceDir = file.getFilePath();
                            appInfo.publicSourceDir = file.getFilePath();
                            appIcon = appInfo.loadIcon(pm);
                            appsIconCache.put(pos, appIcon);

                            builder = request.load(appIcon);
                            builder = builder.skipMemoryCache(true);

                        } else builder = request.load(R.drawable.icon_archive);
                    } else {
                        builder = request.load(R.drawable.icon_archive);
                    }
                } else {
                    builder = request.load(appIcon);
                }
            } else {
                builder = request.load(R.drawable.icon_file);
            }


            builder.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(imageSize, imageSize)
                    .into(fileIconIV);
        }
    }
}
