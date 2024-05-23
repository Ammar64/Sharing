package com.ammar.filescenter.activities.recyclers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.services.NetworkService;
import com.ammar.filescenter.services.objects.Downloadable;

import java.util.LinkedList;

public class DownloadablesDialogAdapter extends RecyclerView.Adapter<DownloadablesDialogAdapter.ViewHolder>{

    private final LinkedList<Downloadable> downloadables;
    private final Context context;
    public DownloadablesDialogAdapter(Context context ,LinkedList<Downloadable> downloadables) {
        this.downloadables = downloadables;
        this.context = context;
    }
    @NonNull
    @Override
    public DownloadablesDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ViewHolder(inflater.inflate(R.layout.view_downloadables_dialog_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadablesDialogAdapter.ViewHolder holder, int position) {
        holder.fileName.setText(downloadables.get(position).getName());
        holder.fileSize.setText(downloadables.get(position).getFormattedSize());
        holder.setDownloadableIndex(position);
        holder.BFileRemove.setOnClickListener((button) -> {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(NetworkService.ACTION_MODIFY_DOWNLOADABLE);
            intent.putExtra(NetworkService.EXTRA_MODIFY_TYPE, NetworkService.VALUE_MODIFY_DELETE);
            intent.putExtra(NetworkService.EXTRA_MODIFY_DELETE_UUID, downloadables.get(holder.downloadableIndex).getUUID());
            downloadables.remove(holder.downloadableIndex);
            notifyItemRemoved(holder.downloadableIndex);
            context.startService(intent);
        });
    }

    @Override
    public int getItemCount() {
        return downloadables.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.TV_DownloadableDialogFileName);
            fileSize = itemView.findViewById(R.id.TV_DownloadableDialogFileSize);
            BFileRemove = itemView.findViewById(R.id.B_DownloadableDialogFileRemove);
        }
        public void setDownloadableIndex(int index){
            downloadableIndex = index;
        }
        int downloadableIndex;
        TextView fileName;
        TextView fileSize;
        Button   BFileRemove;
    }
}
