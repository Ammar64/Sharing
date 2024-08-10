package com.ammar.filescenter.activities.AddAppsActivity.adaptersR;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity.AddAppsActivity;
import com.ammar.filescenter.common.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AppsRecyclerAdapter extends RecyclerView.Adapter<AppsRecyclerAdapter.ViewHolder> {
    private Bitmap[] appsIcon;
    private final PackageManager pm;
    private final AddAppsActivity activity;
    List<ApplicationInfo> apps;
    LinkedList<String> selectedApps;

    public AppsRecyclerAdapter(AddAppsActivity activity, List<ApplicationInfo> apps, LinkedList<String> selectedApps) {
        this.activity = activity;
        this.apps = apps;
        this.selectedApps = selectedApps;
        this.appsIcon = new Bitmap[this.apps.size()];
        this.pm = this.activity.getPackageManager();
        for (int i = 0; i < this.apps.size(); i++) {
            this.appsIcon[i] = Utils.drawableToBitmap(this.apps.get(i).loadIcon(pm));
        }
    }

    @NonNull
    @Override
    public AppsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.view_add_apps_app, parent, false);
        return new ViewHolder(view);
    }

    ArrayList<Integer> checksPositions = new ArrayList<>();

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationInfo appInfo = apps.get(position);
        Bitmap appIcon = appsIcon[position];
        CharSequence appName = appInfo.loadLabel(pm);

        holder.icon.setImageBitmap(appIcon);

        holder.appName.setText(appName);
        holder.checkBox.setChecked(checksPositions.contains(holder.getBindingAdapterPosition()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.splits.setVisibility(appInfo.splitPublicSourceDirs == null ? View.INVISIBLE : View.VISIBLE);
        } else holder.splits.setVisibility(View.GONE);

        holder.itemView.setOnClickListener((view) -> {
            boolean isChecked = holder.checkBox.isChecked();
            holder.checkBox.setChecked(!isChecked);
            if (!isChecked) {
                selectedApps.add(appInfo.packageName);
                checksPositions.add(holder.getBindingAdapterPosition());
            } else {
                selectedApps.remove(appInfo.packageName);
                // using (Integer) forces it to not use remove(int index)
                checksPositions.remove((Integer) holder.getBindingAdapterPosition());
            }
            activity.setToolbarTitle(activity.getString(R.string.selected_num, selectedApps.size()));
        });

    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public CheckBox checkBox;
        public TextView appName;
        public TextView splits;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.IV_AppIcon);
            checkBox = itemView.findViewById(R.id.CB_AppCheckBox);
            appName = itemView.findViewById(R.id.TV_AppName);
            splits = itemView.findViewById(R.id.TV_Splits);
        }
    }


}
