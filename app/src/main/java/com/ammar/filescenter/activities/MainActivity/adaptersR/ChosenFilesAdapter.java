package com.ammar.filescenter.activities.MainActivity.adaptersR;


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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Consts;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.custom.ui.AdaptiveDropDown;
import com.ammar.filescenter.models.Sharable;
import com.ammar.filescenter.models.SharableApp;
import com.ammar.filescenter.network.Server;
import com.ammar.filescenter.services.ServerService;
import com.bumptech.glide.Glide;
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
        return Server.filesList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView fileIconIV;
        final TextView fileNameTV;
        final TextView fileSizeTV;
        final AppCompatImageButton showOptions;
        final AdaptiveDropDown adaptiveDropDown;

        final View removeB;
        final View fastShareB;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconIV = itemView.findViewById(R.id.IV_FileChosenIcon);
            fileNameTV = itemView.findViewById(R.id.TV_FileChosenName);
            fileSizeTV = itemView.findViewById(R.id.TV_FileChosenSize);
            showOptions = itemView.findViewById(R.id.B_ShowSelectedFileOptions);
            adaptiveDropDown = new AdaptiveDropDown(itemView.getContext());

            removeB = adaptiveDropDown.addItem(R.string.remove, R.drawable.icon_trash);
            fastShareB = adaptiveDropDown.addItem(R.string.fast_share, R.drawable.share_icon);
        }

        public void setup(int pos) {
            Sharable file = Server.filesList.get(pos);
            setFileName(file.getName());
            setFileIconIV(file, pos);
            setFileSizeTV(file.getSize());
            setFileListener(file.getUUID());
        }

        public void setFileListener(String uuid) {
            adaptiveDropDown.setAnchorView(showOptions);
            removeB.setOnClickListener(v -> {
                Intent serviceIntent = new Intent(itemView.getContext(), ServerService.class);
                serviceIntent.setAction(Consts.ACTION_REMOVE_DOWNLOAD);
                serviceIntent.putExtra(Consts.EXTRA_DOWNLOAD_UUID, uuid);
                itemView.getContext().startService(serviceIntent);
                adaptiveDropDown.dismiss();
            });

            fastShareB.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "TODO", Toast.LENGTH_SHORT).show();
                adaptiveDropDown.dismiss();
            });
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(long size) {
            fileSizeTV.setText(Utils.getFormattedSize(size));
        }

        Map<Integer, Drawable> appsIconCache = new TreeMap<>();

        public void setFileIconIV(@NonNull Sharable file, int pos) {
            String mimeType = file.getMimeType();
            int imageSize = (int) Utils.dpToPx(40);
            if (file instanceof SharableApp) {
                SharableApp app = (SharableApp) file;
                Glide.with(itemView.getContext())
                        .load(app.getIcon())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(imageSize, imageSize)
                        .into(fileIconIV);
            } else if (mimeType.startsWith("image/")) {
                Glide.with(itemView.getContext())
                        .load(file.getFile())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(imageSize, imageSize)
                        .into(fileIconIV);
            } else if ("application/vnd.android.package-archive".equals(mimeType)) {
                Drawable appIcon = appsIconCache.get(pos);
                if (appIcon == null) {
                    PackageManager pm = itemView.getContext().getApplicationContext().getPackageManager();
                    PackageInfo packageInfo = pm.getPackageArchiveInfo(file.getFilePath(), 0);
                    if (packageInfo != null) {
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        appInfo.sourceDir = file.getFilePath();
                        appInfo.publicSourceDir = file.getFilePath();
                        appIcon = appInfo.loadIcon(pm);
                        appsIconCache.put(pos, appIcon);

                        Glide.with(itemView.getContext())
                                .load(appIcon)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .override(imageSize, imageSize)
                                .into(fileIconIV);

                    } else Glide.with(itemView.getContext())
                            .load(appIcon)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .override(imageSize, imageSize)
                            .into(fileIconIV);

                } else {
                    Glide.with(itemView.getContext())
                            .load(appIcon)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .override(imageSize, imageSize)
                            .into(fileIconIV);
                }
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.icon_file_red)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(imageSize, imageSize)
                        .into(fileIconIV);
            }
        }
    }
}
