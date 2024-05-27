package com.ammar.filescenter.services.objects;

import java.util.ArrayList;
import java.util.List;

public class Device {
    private final String ip;
    private final List<Operation> operations = new ArrayList<>();

    public Device(String ip) {
        this.ip = ip;
    }

    public static class Operation {
        public Operation(OP o) {
            op = o;
        }

        public void setProgress(int p) {
            if( p > 100 ) progress = 100;
            else progress = Math.max(p, 0);
        }
        public int getProgress() {return progress;}
        public enum OP {UPLOAD, DOWNLOAD};
        OP op;
        private int progress = 0;


    }
}
