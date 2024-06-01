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
import com.ammar.filescenter.services.models.Upload;
import com.ammar.filescenter.utils.Utils;

import java.util.List;

public class ChoosenFilesAdapter extends RecyclerView.Adapter<ChoosenFilesAdapter.ViewHolder> {

    public ChoosenFilesAdapter(List<Upload> uploadsList) {
        this.uploadsList = uploadsList;
    }

    List<Upload> uploadsList;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.row_chosen_files, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setup(uploadsList.get(position));
        holder.setFileListener(position);
    }

    @Override
    public int getItemCount() {
        return uploadsList.size();
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
            setFileName(file.getFileName());
            setFileIconIV(file);
            setFileSizeTV(file.getSize());
        }
        public void setFileListener(int indexRemove) {
            Intent serviceIntent = new Intent(itemView.getContext(), NetworkService.class);
            serviceIntent.setAction(NetworkService.ACTION_REMOVE_DOWNLOAD);
            serviceIntent.putExtra(NetworkService.EXTRA_DOWNLOAD_REMOVE, indexRemove);
            itemView.getContext().startService(serviceIntent);
        }

        public void setFileName(String fileName) {
            fileNameTV.setText(fileName);
        }

        public void setFileSizeTV(long size) {
            fileSizeTV.setText(Utils.getFormattedSize(size));
        }

        public void setFileIconIV(Upload file) {
            String mimeType = file.getMimeType();
            if(mimeType.startsWith("image/")) {
                fileIconIV.setImageDrawable(Drawable.createFromPath(file.getFilePath()));
            } else {
                fileIconIV.setImageResource(R.drawable.icon_file_red);
            }
        }
    }
}
