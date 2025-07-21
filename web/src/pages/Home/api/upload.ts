export interface Progress {
    loaded: number,
    total: number,
    completed: boolean,
    computable: boolean,
}

export class UploadOperation {
    private xhr: XMLHttpRequest = new XMLHttpRequest();
    private file: File;
    private setProgress: (progress: Progress) => void
    private _loaded: number = 0;
    private _total: number;
    constructor(file: File, setProgress: (progress: Progress) => void) {
        this.file = file;
        this.setProgress = setProgress;
        this._total = this.file.size;
    }

    private timeData = {
        operationStartTime: -1,
        operationEndTime: -1,
        startTime: Date.now(),
        oldLoaded: 0,
        speedSum: 0,
        numOfProgressCalls: 0
    };

    public get averageUploadSpeed() {
        return this.timeData.speedSum / this.timeData.numOfProgressCalls
    }

    public get getTotalOperationTime() {
        if( this.timeData.operationEndTime == -1 || this.timeData.operationStartTime == -1) {
            return -1;
        }
        return this.timeData.operationEndTime - this.timeData.operationStartTime;
    }

    public get loaded() {
        return this._loaded;
    }
    
    public get total() {
        return this._total;
    }

    public startUpload() {
        this.xhr.open("POST", "/upload/" + encodeURIComponent(this.file.name));
        this.xhr.upload.onprogress = e => {
            if (e.lengthComputable) {
                this._loaded = e.loaded;
                this._total = e.total;
                this.setProgress({ loaded: e.loaded, total: e.total, completed: false, computable: true });
                const speed = (e.loaded - this.timeData.oldLoaded) / ((Date.now() - this.timeData.startTime) * 0.001);

                this.timeData.numOfProgressCalls++;
                this.timeData.speedSum += speed;

                this.timeData.startTime = Date.now();
            } else {
                this.setProgress({
                    computable: false,
                    loaded: e.loaded,
                    total: -1,
                    completed: false
                });
            }
        };

        this.xhr.upload.onloadstart = e => {
            this.timeData.operationStartTime = Date.now();
        };

        this.xhr.upload.onloadend = e => {
            this.timeData.operationEndTime = Date.now();
            this.setProgress({completed: true, computable: true, loaded: this.total, total: this.total});
        };

        this.xhr.upload.onabort = e => {

        };

        this.xhr.upload.onerror = e => {

        }

        this.xhr.send(this.file);
    }

    public abortUpload() {
        this.xhr.abort();
    }
}