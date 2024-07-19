package com.ammar.filescenter.activities.AddAppsActivity.adaptersR;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.AddAppsActivity.AddAppsActivity;
import com.ammar.filescenter.common.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class AppsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final App[] apps;
    private App[] displayedApps;
    private final AddAppsActivity activity;
    LinkedList<String> selectedApps;

    public AppsRecyclerAdapter(AddAppsActivity activity, ArrayList<ApplicationInfo> appsInfo, LinkedList<String> selectedApps) {
        this.activity = activity;
        this.selectedApps = selectedApps;
        PackageManager pm = this.activity.getPackageManager();
        this.apps = new App[appsInfo.size()];
        for (int i = 0; i < appsInfo.size(); i++) {
            this.apps[i] = new App();
            this.apps[i].packageName = appsInfo.get(i).packageName;
            this.apps[i].hasSplits = appsInfo.get(i).splitPublicSourceDirs != null;
            this.apps[i].label = appsInfo.get(i).loadLabel(pm).toString();

            if (this.apps[i].hasSplits) {
                Drawable[] layers = new Drawable[2];
                layers[0] = appsInfo.get(i).loadIcon(pm);
                layers[1] = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.banner_splits, null);
                LayerDrawable layerDrawable = new LayerDrawable(layers);
                this.apps[i].icon = Utils.drawableToBitmap(layerDrawable);
            } else {
                this.apps[i].icon = Utils.drawableToBitmap(appsInfo.get(i).loadIcon(pm));
            }
        }
        Arrays.sort(this.apps, (o1, o2) -> o1.label.compareTo(o2.label));
        this.displayedApps = this.apps;
        this.activity.searchInputET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchApps(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchApps(String searchInput) {
        if (searchInput.isEmpty()) {
            this.displayedApps = this.apps;
            notifyDataSetChanged();
            return;
        }

        ArrayList<App> searchedApps = new ArrayList<>(10);
        for (App i : this.apps) {
            if (i.label.toLowerCase().contains(searchInput)) {
                searchedApps.add(i);
            }
        }
        this.displayedApps = new App[searchedApps.size()];
        searchedApps.toArray(this.displayedApps);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.view_add_apps_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AppViewHolder appHolder = (AppViewHolder) holder;
        int size = (int) Utils.dpToPx(50);
        Glide.with(holder.itemView.getContext())
                .load(displayedApps[position].icon)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(size, size)
                .into(appHolder.icon);

        appHolder.appName.setText(displayedApps[position].label);
        appHolder.checkBox.setChecked(displayedApps[position].isChecked);

        appHolder.itemView.setOnClickListener((view) -> {
            this.activity.searchInputET.clearFocus();
            boolean isChecked = appHolder.checkBox.isChecked();
            appHolder.checkBox.setChecked(!isChecked);
            if (!isChecked) {
                selectedApps.add(displayedApps[position].packageName);
            } else {
                selectedApps.remove(displayedApps[position].packageName);
            }
            this.displayedApps[position].isChecked = !isChecked;
            activity.setToolbarTitle(activity.getString(R.string.selected_num, selectedApps.size()));
        });
    }

    @Override
    public int getItemCount() {
        return displayedApps.length;
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public CheckBox checkBox;
        public TextView appName;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.IV_AppIcon);
            checkBox = itemView.findViewById(R.id.CB_AppCheckBox);
            appName = itemView.findViewById(R.id.TV_AppName);
        }
    }

    public static class App {
        String packageName;
        Bitmap icon;
        String label;
        boolean hasSplits;
        boolean isChecked = false;
    }

}
