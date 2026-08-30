package com.ammar.sharing.nativebackend;
import android.os.ParcelFileDescriptor;

import com.ammar.sharing.nativebackend.lambda.LambdaReturnInt;

@SuppressWarnings("JavaJniMissingFunction")
public class DownloadItemsManager {
    public static ParcelFileDescriptor openDownloadItem(int index) {
        int fd = getNativeFdOfDownloadItem(index);
        return ParcelFileDescriptor.adoptFd(fd);
    }

    public static synchronized native int addNewDownloadItem(String name, long size, LambdaReturnInt fdGetter);
    public static synchronized native int addNewGroupedDownloadItem(String name, long total_size, String[] children_names, LambdaReturnInt[] children_fd_openers);
    public static synchronized native boolean removeDownloadItem(int index);
    public static native DownloadItem getDownloadItem(int index);
    public static native int getDownloadItemsCount();
    private static native int getNativeFdOfDownloadItem(int index);

}
