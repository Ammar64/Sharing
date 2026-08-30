package com.ammar.sharing.nativebackend;

@SuppressWarnings("JavaJniMissingFunction")
public class TransferOperationsManager {
    public static native int getOperationsCount();
    public static native TransferOperation getTransferOperationAtIndex(int index);
    public static native void removeOrCancelTransferOperationAtIndex(int index);
}
