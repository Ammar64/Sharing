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

public class ImagesViewModel extends ViewModel {
    private static final MutableLiveData<MediaData[]> mListedImages = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(true);
    private static boolean dataLoaded = false;

    public ImagesViewModel() {
        loadData();
    }

    public void loadData() {
        if (!dataLoaded) {
            loadImagesData();
            dataLoaded = true;
        }
    }

    public void reset() {
        mIsLoading.setValue(true);
        dataLoaded = false;
    }

    public LiveData<MediaData[]> getImagesDataLiveData() {
        return mListedImages;
    }
    public LiveData<Boolean> getIsLoadingLiveData() {
        return mIsLoading;
    }

    private void loadImagesData() {
        new Thread(() -> {
            Cursor cursorExternal = Utils.getCR().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DISPLAY_NAME,
                            MediaStore.Images.Media.SIZE
                    },
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
            );

            Cursor cursorInternal = Utils.getCR().query(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DISPLAY_NAME,
                            MediaStore.Images.Media.SIZE
                    },
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
            );

            int idColumn = cursorExternal.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn =
                    cursorExternal.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int sizeColumn = cursorExternal.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

            int count = cursorExternal.getCount();
            MediaData[] imagesData = new MediaData[count];
            int i = 0;
            while( cursorExternal.moveToNext() ) {
                long id = cursorExternal.getLong(idColumn);
                String name = cursorExternal.getString(nameColumn);
                long size = cursorExternal.getLong(sizeColumn);
                Uri data = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imagesData[i] = new MediaData(name, size, data);
                i++;
            }

            mIsLoading.postValue(false);
            mListedImages.postValue(imagesData);
        }).start();
    }


}
