const loader = document.getElementById('overlay-process')! as HTMLElement;
const uploadInput = document.getElementById('uploadInput') as HTMLInputElement;


class UploadOperation {
    constructor(progressBar: HTMLDivElement, progressText: HTMLDivElement, progressSpeed: HTMLDivElement) {
        this.progressBar = progressBar;
        this.progressText = progressText;
        this.progressSpeed = progressSpeed;
    }
        
    // UI elements
    private progressBar: HTMLDivElement;
    private progressText: HTMLDivElement;
    private progressSpeed: HTMLDivElement;
    
    // used to calculate progress speed
    private _progressTimeTrack: number = 0;
    private __oldLoaded: number = 0;

    public updateProgress(loaded: number, total: number) {

        const speed = (loaded - this.__oldLoaded) / ((Date.now() - this._progressTimeTrack) * 0.001)

        this._progressTimeTrack = Date.now()
        this.__oldLoaded = loaded;

        const percent = ((loaded / total) * 100).toFixed(2);
        this.progressBar.style.width = percent + '%';
        this.progressText.textContent = percent + '%';
        this.progressSpeed.textContent = `${getFormattedFileSize(speed)} / s`
    }
}

function showLoader() {
    loader.style.display = 'block';
}

function hideLoader() {
    (document.querySelector('#plane')! as HTMLElement).style.animation = 'plane-done 1.2s infinite';
    setTimeout(() => {
        loader.style.display = 'none';
        (document.querySelector('#plane')! as HTMLElement).style.animation = 'plane-on-progress 5s infinite';
    }, 600);
}

const progressBar = document.querySelector('#progress-bar') as HTMLDivElement;
const progressText = document.querySelector('#progress-text') as HTMLDivElement;
const progressSpeed = document.querySelector('#progress-speed') as HTMLDivElement;


/* uploading */
uploadInput.addEventListener('input', function (e) {
    const xhr = new XMLHttpRequest();

    const uploadOperation = new UploadOperation(progressBar, progressText, progressSpeed);

    xhr.open("POST", "/upload/" + encodeURIComponent(this.files![0].name));

    xhr.upload.onprogress = function (event) {
        if (event.lengthComputable) {
            uploadOperation.updateProgress(event.loaded, event.total);
        }
    };

    xhr.onloadstart = function () {
        showLoader();
    };

    xhr.onloadend = function () {
        hideLoader();
    };

    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log('File uploaded successfully');
        } else {
            console.error('Error uploading the file');
        }
    };

    xhr.onerror = function () {
        console.error('Error uploading the file');
        hideLoader();
    };

    xhr.send(this.files![0]);
});

