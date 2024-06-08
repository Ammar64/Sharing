package com.ammar.filescenter.custom.io;

import java.io.IOException;
import java.io.OutputStream;

public class ProgressOutputStream extends OutputStream {
    long bytesWritten = 0;
    private final OutputStream output;
    ProgressManager progressManager;
    public ProgressOutputStream(OutputStream output, ProgressManager progressManager) {
        this.output = output;
        this.progressManager = progressManager;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
        bytesWritten += 1;
        updateProgress();
    }

    @Override
    public void write(byte[] b) throws IOException {
        output.write(b);
        bytesWritten += b.length;
        updateProgress();

    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
        bytesWritten += len;
        updateProgress();

    }

    private void updateProgress() {
        progressManager.setLoaded(bytesWritten);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
