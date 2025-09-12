
export enum CompleteStatus {
    IN_PROGRESS = 0,
    COMPLETED = 1,
    FAILED = 2,
}
export interface Progress {
    loaded: number,
    total: number,
    completeStatus: CompleteStatus,
    computable: boolean,
}

export class FileUploadManager {

    private xhr: XMLHttpRequest = new XMLHttpRequest();
    private file: File;
    private _progress: Progress;
    private setProgress: ((progress: Progress) => void) | null = null;
    private _loaded: number = 0;
    private _total: number;
    constructor(file: File) {
        this.file = file;
        this._total = this.file.size;

        this._progress = {
            completeStatus: CompleteStatus.IN_PROGRESS,
            computable: true,
            loaded: 0,
            total: this._total
        };
    }

    public set progressCallback(value: (progress: Progress) => void) {
        this.setProgress = value;
    }

    private timeData = {
        operationStartTime: -1,
        operationEndTime: -1,
        startTime: Date.now(),
        oldLoaded: 0,
        currentSpeed: 0,
        speedSum: 0,
        numOfProgressCalls: 0
    };

    public get averageUploadSpeed() {
        return this.timeData.speedSum / this.timeData.numOfProgressCalls * 1000;
    }

    public get currentSpeed() {
        return this.timeData.currentSpeed * 1000;
    }
    public get getTotalOperationTime() {
        if (this.timeData.operationEndTime === -1 || this.timeData.operationStartTime === -1) {
            return -1;
        }
        return (this.timeData.operationEndTime - this.timeData.operationStartTime) * 0.001;
    }

    public get progress() {
        return this._progress;
    }

    public get loaded() {
        return this._loaded;
    }

    public get total() {
        return this._total;
    }

    public get percentage() {
        return this._loaded / this._total * 100;
    }

    public get completeStatus() {
        return this._progress.completeStatus;
    }

    public startUpload() {
        if (this._progress.completeStatus === CompleteStatus.COMPLETED
            || this._progress.completeStatus === CompleteStatus.FAILED) {
            return;
        }
        this.xhr.open("POST", "/upload/" + encodeURIComponent(this.file.name));
        this.xhr.upload.onprogress = e => {
            if (e.lengthComputable) {
                this.timeData.numOfProgressCalls++;
                this._loaded = e.loaded;
                this._total = e.total;

                this.setProgress && this.setProgress({ loaded: e.loaded, total: e.total, completeStatus: CompleteStatus.IN_PROGRESS, computable: true });
                const speedPerMillisecond = (e.loaded - this.timeData.oldLoaded) / ((Date.now() - this.timeData.startTime));

                this.timeData.startTime = Date.now();
                this.timeData.oldLoaded = e.loaded;
                this.timeData.currentSpeed = speedPerMillisecond;
                this.timeData.speedSum += speedPerMillisecond;
            } else {
                this.setProgress && this.setProgress({
                    computable: false,
                    loaded: e.loaded,
                    total: -1,
                    completeStatus: CompleteStatus.IN_PROGRESS
                });
            }
        };

        this.xhr.onreadystatechange = () => {
            if (this.xhr.readyState == XMLHttpRequest.DONE) {
                if (this.xhr.status === 200) {
                    this.setProgress &&
                        this.setProgress({ completeStatus: CompleteStatus.COMPLETED, computable: true, loaded: this.total, total: this.total });
                    this.timeData.operationEndTime = Date.now();
                } else {
                    this.setProgress &&
                        this.setProgress({ completeStatus: CompleteStatus.FAILED, computable: true, loaded: this.loaded, total: this.total });
                }
            }
        }
        this.timeData.startTime = Date.now();
        this.timeData.operationStartTime = Date.now();
        this.xhr.send(this.file);
    }

    public abortUpload() {
        this.xhr.abort();
    }
}