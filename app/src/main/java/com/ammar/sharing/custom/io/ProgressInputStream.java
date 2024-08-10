package com.ammar.sharing.custom.io;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {

    long bytesRead = 0;
    private final InputStream input;
    @Nullable
    ProgressManager progressManager;

    public ProgressInputStream(InputStream input, @Nullable ProgressManager progressManager) {
        this.input = input;
        this.progressManager = progressManager;
    }


    public void setProgressManager(@Nullable ProgressManager manager) {
        this.progressManager = manager;
    }
    @Override
    public int read() throws IOException {
        int nRead = input.read();
        if (nRead > 0)
            bytesRead += nRead;
        if (progressManager != null)
            progressManager.setLoaded(bytesRead);
        return nRead;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int nRead = input.read(b);
        if (nRead > 0)
            bytesRead += nRead;
        if (progressManager != null)
            progressManager.setLoaded(bytesRead);
        return nRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int nRead = input.read(b, off, len);
        if (nRead > 0)
            bytesRead += nRead;
        if (progressManager != null)
            progressManager.setLoaded(bytesRead);
        return nRead;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long skipped = input.skip(n);
        bytesRead += skipped;
        return skipped;
    }

    @Override
    public boolean markSupported() {
        return input.markSupported();
    }

    @Override
    public void mark(int readlimit) {
        input.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        input.reset();
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

}
