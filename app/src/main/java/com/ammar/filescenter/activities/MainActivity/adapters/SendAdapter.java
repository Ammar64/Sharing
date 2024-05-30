package com.ammar.filescenter.activities.MainActivity.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.activities.MainActivity.models.Upload;

import java.util.List;

public class SendAdapter extends RecyclerView.Adapter<SendAdapter.ViewHolder> {

    private List<Upload> uploadsList;

    public SendAdapter(List<Upload> uploadsList) {
        this.uploadsList = uploadsList;
    }

    @NonNull
    @Override
    public SendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull SendAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
