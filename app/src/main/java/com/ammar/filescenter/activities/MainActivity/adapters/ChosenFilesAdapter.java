package com.ammar.filescenter.activities.MainActivity.adapters;



import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.components.Server;
import com.ammar.filescenter.services.models.Upload;
import com.ammar.filescenter.utils.Utils;

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
        holder.setup(Server.filesList.get(position));
        holder.setFileListener(Server.filesList.get(position).getUUID());
    }

    @Override
    public int getItemCount() {
        return Server.filesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIconIV;
        TextView fileNameTV;
        TextView fileSizeTV;
        Button   removeFileB;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconIV  = itemView.findViewById(R.id.IV_FileChosenIcon);
            fileNameTV  = itemView.findViewById(R.id.TV_FileChosenName);
            fileSizeTV  = itemView.findViewById(R.id.TV_FileChosenSize);
            removeFileB = itemView.findViewById(R.id.B_RemoveChosenFile);
        }

        public void setup(Upload file) {
            setFileName(file.getName());
            setFileIconIV(file);
            setFileSizeTV(file.getSize());
        }
        public void setFileListener(String uuid) {
            removeFileB.setOnClickListener( button -> {
                Intent serviceIntent = new Intent(itemView.getContext(), NetworkService.class);
                serviceIntent.setAction(NetworkService.ACTION_REMOVE_DOWNLOAD);
                serviceIntent.putExtra(NetworkService.EXTRA_DOWNLOAD_UUID, uuid);
                itemView.getContext().startService(serviceIntent);
            });
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(long size) {
            fileSizeTV.setText(Utils.getFormattedSize(size));
        }

        public void setFileIconIV(@NonNull Upload file) {
            String mimeType = file.getMimeType();
            if(mimeType.startsWith("image/")) {
                fileIconIV.setImageDrawable(Drawable.createFromPath(file.getFilePath()));
            } else {
                fileIconIV.setImageResource(R.drawable.icon_file_red);
            }
        }
    }
}
