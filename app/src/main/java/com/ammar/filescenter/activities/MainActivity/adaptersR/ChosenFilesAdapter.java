package com.ammar.filescenter.activities.MainActivity.adaptersR;



import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.common.Vals;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.network.Server;
import com.ammar.filescenter.services.models.Transferable;
import com.ammar.filescenter.common.Utils;
import com.ammar.filescenter.services.models.TransferableApp;

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
        ImageView fileIconIV;
        TextView fileNameTV;
        TextView fileSizeTV;
        AppCompatImageButton removeFileB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconIV  = itemView.findViewById(R.id.IV_FileChosenIcon);
            fileNameTV  = itemView.findViewById(R.id.TV_FileChosenName);
            fileSizeTV  = itemView.findViewById(R.id.TV_FileChosenSize);
            removeFileB = itemView.findViewById(R.id.B_RemoveChosenFile);
        }

        public void setup(int pos) {
            Transferable file = Server.filesList.get(pos);
            setFileName(file.getName());
            setFileIconIV(file, pos);
            setFileSizeTV(file.getSize());
            setFileListener(file.getUUID());
        }
        public void setFileListener(String uuid) {
            removeFileB.setOnClickListener( button -> {
                Intent serviceIntent = new Intent(itemView.getContext(), NetworkService.class);
                serviceIntent.setAction(Vals.ACTION_REMOVE_DOWNLOAD);
                serviceIntent.putExtra(Vals.EXTRA_DOWNLOAD_UUID, uuid);
                itemView.getContext().startService(serviceIntent);
            });
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(long size) {
            fileSizeTV.setText(Utils.getFormattedSize(size));
        }

        Map<Integer, Drawable> appsIconCache = new TreeMap<>();
        public void setFileIconIV(@NonNull Transferable file, int pos) {
            String mimeType = file.getMimeType();
            if( file instanceof TransferableApp ) {
                TransferableApp app = (TransferableApp) file;
                fileIconIV.setImageDrawable(app.getIcon());
            } else if (mimeType.startsWith("image/")) {
                fileIconIV.setImageDrawable(Drawable.createFromPath(file.getFilePath()));
            } else if ("application/vnd.android.package-archive".equals(mimeType)){
                Drawable appIcon = appsIconCache.get( pos );
                if( appIcon == null ) {
                    PackageManager pm = itemView.getContext().getApplicationContext().getPackageManager();
                    PackageInfo packageInfo = pm.getPackageArchiveInfo(file.getFilePath(), 0);
                    if( packageInfo != null ) {
                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                        appInfo.sourceDir = file.getFilePath();
                        appInfo.publicSourceDir = file.getFilePath();
                        appIcon = appInfo.loadIcon(pm);
                        appsIconCache.put( pos, appIcon );
                        fileIconIV.setImageDrawable(appIcon);
                    } else fileIconIV.setImageResource(R.drawable.icon_file_red);
                } else {
                    fileIconIV.setImageDrawable(appIcon);
                }
            } else {
                fileIconIV.setImageResource(R.drawable.icon_file_red);
            }
        }
    }
}
