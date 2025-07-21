package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.AddAppsAndFilesActivity;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders.FilesBrowseOptionViewHolder;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.FilesBrowserOptionData;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.fragments.FilesFragment;
import com.ammar.sharing.activities.AddFilesActivity.AddFilesActivity;

import java.util.ArrayList;

import hendrawd.storageutil.library.StorageUtil;

public class FilesFragmentRecyclerAdapter extends RecyclerView.Adapter<FilesBrowseOptionViewHolder> {

    private final ArrayList<FilesBrowserOptionData> optionsData = new ArrayList<>();
    private final ActivityResultLauncher<String> mGetMultipleContents;
    private final ActivityResultLauncher<Intent> mGetFilesBuiltIn;

    public FilesFragmentRecyclerAdapter(FilesFragment fragment, AddAppsAndFilesActivity activity) {
        optionsData.add(new FilesBrowserOptionData(R.drawable.ic_file_green_bg, activity.getString(R.string.browse_internal_storage), activity.getString(R.string.browse_internal_storage_desc), null));
        optionsData.add(new FilesBrowserOptionData(R.drawable.ic_file_green_bg, activity.getString(R.string.browse_internal_storage_builtin), activity.getString(R.string.browse_internal_storage_desc_builtin), Environment.getExternalStorageDirectory().getAbsolutePath()));

        String[] sdCardPaths = StorageUtil.getStorageDirectories(activity);
        for (String i : sdCardPaths) {
            if (i.endsWith("/")) i = i.substring(0, i.length() - 1);
            if (i.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                continue;
            }
            int index = i.lastIndexOf("/");
            String sdCardName;
            if (index == -1) {
                sdCardName = i;
            } else {
                sdCardName = i.substring(index + 1);
            }
            optionsData.add(new FilesBrowserOptionData(R.drawable.ic_file_green_bg, activity.getString(R.string.browse_sd_card_builtin, sdCardName), activity.getString(R.string.browse_sd_card_builtin_desc), i));
        }

        mGetMultipleContents = fragment.registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), (uris) -> {

            ArrayList<Uri> urisArrayList = new ArrayList<>(uris);
            if( urisArrayList.isEmpty() ) return;
            Intent intent = new Intent();
            intent.putExtra(AddAppsAndFilesActivity.EXTRA_URIS_SHARED, true);
            intent.putExtra(AddAppsAndFilesActivity.EXTRA_URIS, urisArrayList);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();

        });

        mGetFilesBuiltIn = fragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
            if (result.getResultCode() == Activity.RESULT_OK && AddFilesActivity.ACTION_GET_FILES.equals(result.getData().getAction())) {
                ArrayList<String> filesPaths = result.getData().getStringArrayListExtra(AddFilesActivity.EXTRA_FILES_PATHS);
                Intent intent = new Intent();
                intent.putExtra(AddAppsAndFilesActivity.EXTRA_FILE_PATHS_SHARED, true);
                intent.putExtra(AddAppsAndFilesActivity.EXTRA_FILES_PATHS, filesPaths);
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        });
    }

    @NonNull
    @Override
    public FilesBrowseOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_files_browse_option, parent, false);
        return new FilesBrowseOptionViewHolder(view, mGetMultipleContents, mGetFilesBuiltIn);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesBrowseOptionViewHolder holder, int position) {
        holder.setViewHolderData(optionsData.get(position));
    }

    @Override
    public int getItemCount() {
        return optionsData.size();
    }
}
