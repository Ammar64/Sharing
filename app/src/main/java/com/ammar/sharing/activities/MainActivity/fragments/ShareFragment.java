package com.ammar.sharing.activities.MainActivity.fragments;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.activities.MainActivity.MainActivity;
import com.ammar.sharing.activities.MainActivity.adaptersR.ShareAdapter.ShareAdapter;
import com.ammar.sharing.common.Consts;
import com.ammar.sharing.common.Data;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.io.ProgressManager;
import com.ammar.sharing.services.ServerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public class ShareFragment extends Fragment {
    private View v;
    private RecyclerView filesSendRV;
    private ShareAdapter adapter;

    private final HashMap<UUID, Notification> notificationsMap = new HashMap<>();

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_share, container, false);
        initItems();
        setItemsListener();
        initObservers();
        return v;
    }


    private void initItems() {
        filesSendRV = v.findViewById(R.id.RV_FilesSend);
        adapter = new ShareAdapter(this);
        filesSendRV.setAdapter(adapter);
        filesSendRV.setLayoutManager(new LinearLayoutManager(getContext()));
        filesSendRV.setItemAnimator(null);
        filesSendRV.setHasFixedSize(true);
    }

    private void setItemsListener() {
    }

    private void initObservers() {

        Data.filesSendNotifier.observe( requireActivity(), info -> {
            char action = info.getChar("action");
            int index = info.getInt("index");
            index += 1; // I don't remember why I increment 1 but it works like that :)
            switch (action) {
                case 'P':
                    adapter.notifyItemChanged(index);
                    updateNotificationProgress(index-1);
                    break;
                case 'R':
                    adapter.notifyItemRemoved(index);
                    break;
                case 'A':
                    adapter.notifyItemInserted(index);
                    buildProgressNotification(index-1);
                    break;
            }
        });

    }

    public ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            Intent intent = new Intent(requireContext(), ServerService.class);
            intent.setAction(ServerService.ACTION_MULTIPLE_ACTIONS);
            ArrayList<String> actions = new ArrayList<>();
            if( data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_URIS_SHARED, false) ) {
                ArrayList<Uri> uris = data.getParcelableArrayListExtra(AddAppsAndFilesActivity.EXTRA_URIS);
                actions.add(ServerService.ACTION_ADD_URIS);
                intent.putExtra(ServerService.EXTRA_URIS, uris);
            }
            if( data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_APPS_SHARED, false) ) {
                ArrayList<String> selectedApps = data.getStringArrayListExtra(AddAppsAndFilesActivity.EXTRA_PACKAGES_NAMES);
                actions.add(ServerService.ACTION_ADD_APPS_PACKAGES_NAMES);
                intent.putExtra(ServerService.EXTRA_APPS_PACKAGES, selectedApps);
            }
            if (data.getBooleanExtra(AddAppsAndFilesActivity.EXTRA_FILE_PATHS_SHARED, false)) {
                ArrayList<String> selectedFilePaths = data.getStringArrayListExtra(AddAppsAndFilesActivity.EXTRA_FILES_PATHS);
                actions.add(ServerService.ACTION_ADD_FILES_PATHS);
                intent.putExtra(ServerService.EXTRA_FILES_PATHS, selectedFilePaths);
            }

            intent.putExtra(ServerService.EXTRA_ACTIONS, actions);
            requireContext().startService(intent);
        }
    });

    /**
     * @param progress_index the index of the progress manager in ProgressManager.progresses
     */
    private void buildProgressNotification(int progress_index) {
        ProgressManager manager = ProgressManager.progresses.get(progress_index);
        if( notificationsMap.containsKey(manager.getProgressUUID()) ) {
            return;
        }
        boolean isDownload = manager.getOperation() == ProgressManager.OP.DOWNLOAD;
        String username = manager.getUser().getName();

        Intent intent = new Intent(requireContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(requireContext(), Consts.PROGRESS_NOTIFICATION_CHANNEL_ID)
                // isDownload means that the remote is downloading and we are the one who upload
                .setSmallIcon( isDownload ? R.drawable.icon_upload : R.drawable.icon_download)
                .setContentTitle(isDownload ? "Sending " + manager.getDisplayName() : "Receiving " + manager.getDisplayName())
                .setStyle(new NotificationCompat.BigTextStyle().bigText( isDownload ? "Sending to " + username : "Receiving from " + username)) // Text displayed in the notification
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Notification priority for better visibility
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, manager.getTotal() != -1)
                .setContentIntent(pendingIntent)
                .setGroup(Consts.PROGRESS_NOTIFICATION_GROUP)
                .build();

        notificationsMap.put(manager.getProgressUUID(), notification);
    }

    private void updateNotificationProgress(int progress_index) {
        ProgressManager manager = ProgressManager.progresses.get(progress_index);
        if( !notificationsMap.containsKey(manager.getProgressUUID()) ) {
            return;
        }
        if( manager.getLoaded() == ProgressManager.COMPLETED ) {
            notificationProgressCompleted(progress_index, true);
            return;
        } else if(manager.getLoaded() == ProgressManager.STOPPED_BY_REMOTE || manager.getLoaded() == ProgressManager.STOPPED_BY_USER) {
            notificationProgressCompleted(progress_index, false);
            return;
        }
        boolean isDownload = manager.getOperation() == ProgressManager.OP.DOWNLOAD;
        boolean isDeterminate = manager.getTotal() != -1;
        String username = manager.getUser().getName();

        String loaded = Utils.getFormattedSize(manager.getLoaded());
        String total = Utils.getFormattedSize(manager.getTotal());
        String bytesPerSecond = Utils.getFormattedSize(manager.getSpeed());

        String speed;
        if (!isDeterminate && manager.getLoaded() != ProgressManager.COMPLETED) {
            speed = String.format(Locale.ENGLISH, "%s (%s/S)", loaded, bytesPerSecond);
        } else {
            speed = String.format(Locale.ENGLISH, "%s / %s   (%s/S)", loaded, total, bytesPerSecond);
        }

        Notification notification = new NotificationCompat.Builder(requireContext(), Objects.requireNonNull(notificationsMap.get(manager.getProgressUUID())))
                .setStyle( new NotificationCompat.BigTextStyle().bigText( isDeterminate ?
                        (isDownload ? String.format(Locale.ENGLISH, "Sending to %s %d%% %s", username, manager.getPercentage(), speed): String.format(Locale.ENGLISH, "Receiving from %s %d%% %s", username, manager.getPercentage(), speed))
                        : (isDownload ? "Sending to " + username + " " + speed : "Receiving from " + username + " " + speed))) // Text displayed in the notification
                .setProgress(100, isDeterminate ? manager.getPercentage() : 0, !isDeterminate )
                .build();

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(manager.getProgressUUID().hashCode(), notification);
    }

    private void notificationProgressCompleted(int progress_index, boolean success) {
        ProgressManager manager = ProgressManager.progresses.get(progress_index);
        if( !notificationsMap.containsKey(manager.getProgressUUID()) ) {
            return;
        }
        boolean isDownload = manager.getOperation() == ProgressManager.OP.DOWNLOAD;
        String username = manager.getUser().getName();
        Notification notification = new NotificationCompat.Builder(requireContext(), Objects.requireNonNull(notificationsMap.get(manager.getProgressUUID())))
                .setContentTitle("Sending " + manager.getDisplayName() + " " + (success ? "succeeded" : "failed"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText( success ?
                        (isDownload ? "Sent to " + username + " in " + Utils.getFormattedTime(manager.getTotalTime()) : "Received from " + username + " in " + Utils.getFormattedTime(manager.getTotalTime()))
                        : ( isDownload ? "Failed to send to " + username : "Failed to receive from " + username)))
                .setProgress(0, 0, false)
                .build();
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(manager.getProgressUUID().hashCode(), notification);
    }

}