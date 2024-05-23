package com.ammar.filescenter.activities.recyclers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.services.objects.AppDownloadable;

import java.util.List;

public class AppsRecyclerAdapter extends RecyclerView.Adapter<AppsRecyclerAdapter.ViewHolder> {


    private Context context;
    List<ApplicationInfo> apps;
    List<ApplicationInfo> selectedApps;

    public AppsRecyclerAdapter(Context context, List<ApplicationInfo> apps) {
        this.context = context;
        this.apps = apps;
    }

    @NonNull
    @Override
    public AppsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_add_apps_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppsRecyclerAdapter.ViewHolder holder, int position) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = apps.get(position);

        Drawable appIcon = appInfo.loadIcon(pm);
        CharSequence appName = appInfo.loadLabel(pm);

        holder.icon.setImageDrawable(appIcon);
        holder.appName.setText(appName);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView appName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.IV_AppIcon);
            appName = itemView.findViewById(R.id.TV_AppName);
        }
    }
}
