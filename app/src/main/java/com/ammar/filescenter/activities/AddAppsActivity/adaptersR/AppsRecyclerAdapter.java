package com.ammar.filescenter.activities.AddAppsActivity.adaptersR;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AppsRecyclerAdapter extends RecyclerView.Adapter<AppsRecyclerAdapter.ViewHolder> {
    private final App[] apps;
    private App[] displayedApps;
    private final PackageManager pm;
    private final AddAppsActivity activity;
    LinkedList<String> selectedApps;

    public AppsRecyclerAdapter(AddAppsActivity activity, ArrayList<ApplicationInfo> appsInfo, LinkedList<String> selectedApps) {
        this.activity = activity;
        this.selectedApps = selectedApps;
        this.pm = this.activity.getPackageManager();
        this.apps = new App[appsInfo.size()];
        for (int i = 0; i < appsInfo.size(); i++) {
            this.apps[i] = new App();
            this.apps[i].packageName = appsInfo.get(i).packageName;
            this.apps[i].icon = Utils.drawableToBitmap(appsInfo.get(i).loadIcon(pm));
            this.apps[i].label = appsInfo.get(i).loadLabel(pm).toString();
            this.apps[i].hasSplits = appsInfo.get(i).splitPublicSourceDirs != null;
        }
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
        if( searchInput.isEmpty() ) {
            this.displayedApps = this.apps;
            notifyDataSetChanged();
            return;
        }

        ArrayList<App> searchedApps = new ArrayList<>(10);
        for( App i : this.apps ) {
            if( i.label.toLowerCase().contains(searchInput) ) {
                searchedApps.add(i);
            }
        }
        this.displayedApps = new App[searchedApps.size()];
        searchedApps.toArray(this.displayedApps);
        notifyDataSetChanged();
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

        int size = (int) Utils.dpToPx(50);
        Glide.with(holder.itemView.getContext())
                .load(displayedApps[position].icon)
                .override(size, size)
                .into(holder.icon);

        holder.appName.setText(displayedApps[position].label);
        holder.checkBox.setChecked(checksPositions.contains(holder.getAdapterPosition()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.splits.setVisibility(displayedApps[position].hasSplits ? View.VISIBLE : View.INVISIBLE);
        } else holder.splits.setVisibility(View.GONE);

        holder.itemView.setOnClickListener((view) -> {
            this.activity.searchInputET.clearFocus();
            boolean isChecked = holder.checkBox.isChecked();
            holder.checkBox.setChecked(!isChecked);
            if (!isChecked) {
                selectedApps.add(displayedApps[position].packageName);
                checksPositions.add(holder.getBindingAdapterPosition());
            } else {
                selectedApps.remove(displayedApps[position].packageName);
                // using (Integer) forces it to not use remove(int index)
                checksPositions.remove((Integer) holder.getBindingAdapterPosition());
            }
            activity.setToolbarTitle(activity.getString(R.string.selected_num, selectedApps.size()));
        });

    }

    @Override
    public int getItemCount() {
        return displayedApps.length;
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


    public static class App {
        String packageName;
        Bitmap icon;
        String label;
        boolean hasSplits;
    }

}
