package com.ammar.filescenter.activities.recyclers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;

import java.util.LinkedList;

public class AddFilesAdapter extends RecyclerView.Adapter<AddFilesAdapter.ViewHolder> {
    private final Context context;
    private DocumentFile[] files;
    private DocumentFile currentPath;
    private int depth = 0;
    LinkedList<String> selectedFiles;

    public AddFilesAdapter(Context context, DocumentFile rootStorage, LinkedList<String> selectedFiles) {
        this.context = context;
        this.files = rootStorage.listFiles();
        this.selectedFiles = selectedFiles;
        currentPath = rootStorage;
    }

    @NonNull
    @Override
    public AddFilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View fileView = inflater.inflate(R.layout.view_add_files_file, parent, false);

        return new ViewHolder(fileView);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFilesAdapter.ViewHolder holder, int position) {
        DocumentFile file = files[position];
        holder.filename.setText(file.getName());
        String type = file.getType();
        holder.setFileIndex(position);
        if (file.isDirectory()) {
            holder.icon.setImageResource(R.drawable.folder);
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            if( selectedFiles.contains(file.getUri().getPath()) ) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            if( type != null)
            if (type.startsWith("image/")) {
                holder.icon.setImageURI(file.getUri());
            } else if ("application/pdf".equals(type)) {
                holder.icon.setImageResource(R.drawable.icons8_pdf_100);
            } else if ("application/vnd.android.package-archive".equals(type)) {
                holder.icon.setImageResource(R.drawable.icons8_apk_file_100);
            } else if ("text/plain".equals(type)) {
                holder.icon.setImageResource(R.drawable.icons8_text_file_100);
            } else {
                holder.icon.setImageResource(R.drawable.icons8_file_100);
            }
        }

        holder.itemView.setOnClickListener((view) -> {
            if (files[holder.fileIndex].isDirectory()) {
                depth++;
                currentPath = files[holder.fileIndex];
                AddFilesAdapter.this.files = files[holder.fileIndex].listFiles();
                AddFilesAdapter.this.notifyDataSetChanged();
            } else {
                holder.checkBox.toggle();
                String path = files[holder.fileIndex].getUri().getPath();
                if (holder.checkBox.isChecked())
                    selectedFiles.add(path);
                else
                    selectedFiles.remove(path);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public void goBack() {
        if (depth > 0) {
            depth--;
            DocumentFile parent = currentPath.getParentFile();
            files = parent.listFiles();
            currentPath = parent;
            AddFilesAdapter.this.notifyDataSetChanged();
        } else {
            AppCompatActivity activity = ((AppCompatActivity) context);
            activity.setResult(Activity.RESULT_CANCELED);
            activity.finish();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.IV_FileIcon);
            filename = itemView.findViewById(R.id.TV_FileName);
            checkBox = itemView.findViewById(R.id.CB_FileSelect);
        }

        public void setFileIndex(int fileIndex) {
            this.fileIndex = fileIndex;
        }

        private int fileIndex;
        public ImageView icon;
        public TextView filename;
        public CheckBox checkBox;
    }
}
