package com.ammar.sharing.activities.AddAppsAndFilesActivity.viewModels;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ammar.sharing.activities.AddAppsAndFilesActivity.data.MediaData;
import com.ammar.sharing.common.utils.Utils;

public class VideosViewModel extends ViewModel {
    private final static MutableLiveData<MediaData[]> mListedVideos = new MutableLiveData<>();
    private final static MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(true);
    private static boolean dataLoaded = false;

    public VideosViewModel() {
        loadData();
    }

    public void loadData() {
        if (!dataLoaded) {
            loadVideoData();
            dataLoaded = true;
        }
    }

    public void reset() {
        mIsLoading.setValue(true);
        dataLoaded = false;
    }

    public LiveData<MediaData[]> getVideosDataLiveData() {
        return mListedVideos;
    }
    public LiveData<Boolean> getIsLoadingLiveData() {
        return mIsLoading;
    }

    private void loadVideoData() {
        new Thread(() -> {
            Cursor cursorExternal = Utils.getCR().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.DISPLAY_NAME,
                            MediaStore.Video.Media.SIZE
                    },
                    null,
                    null,
                    MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC"
            );
            // TODO: see if we need this
            Cursor cursorInternal = Utils.getCR().query(
                    MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.DISPLAY_NAME,
                            MediaStore.Video.Media.SIZE
                    },
                    null,
                    null,
                    MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC"
            );

            int idColumn = cursorExternal.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn =
                    cursorExternal.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int sizeColumn = cursorExternal.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

            int count = cursorExternal.getCount();
            MediaData[] videosData = new MediaData[count];
            int i = 0;
            while( cursorExternal.moveToNext() ) {
                long id = cursorExternal.getLong(idColumn);
                String name = cursorExternal.getString(nameColumn);
                long size = cursorExternal.getLong(sizeColumn);
                Uri data = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                videosData[i] = new MediaData(name, size, data);
                i++;
            }

            mIsLoading.postValue(false);
            mListedVideos.postValue(videosData);
        }).start();
    }


}
