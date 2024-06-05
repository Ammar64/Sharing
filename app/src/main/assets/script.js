const recieveBtn = document.getElementById("recieveBtn");
const downloadBubble = document.getElementById("downloadBubble");
const overlay = document.getElementById('overlay');
const okButton = document.getElementById('okButton');
const downloads   = document.getElementById("downloads");
const uploadInput = document.getElementById('uploadInput');

const download_item = document.createElement("li");
download_item.className = "download-item";


recieveBtn.onclick = () => {
    downloadBubble.style.display = 'block';
    overlay.style.display = 'block';
    requestAvailableDownloads();
};


okButton.onclick = () => {
    closeDialog();
};

overlay.onclick = () => {
    closeDialog();
};

function closeDialog() {
    downloadBubble.style.display = 'none';
    overlay.style.display = 'none';
}

const loader = document.querySelector('.process');
function showLoader() {
    loader.style.display= 'block';
}

function hideLoader() {
    document.querySelector('.plane').style.animation = 'plane-done 1.2s infinite';
    setTimeout(() => {
        loader.style.display = 'none';
        document.querySelector('.plane').style.animation = 'plane-on-progress 5s infinite';
    }, 1000);
}

function updateProgress(percent) {
    const progressBar = document.querySelector('.progress-bar');
    const progressText = document.querySelector('.progress-text');
    progressBar.style.width = percent + '%';
    progressText.textContent = percent + '%';
}


function requestAvailableDownloads() {
    // Show the loader when the request starts
    showLoader();
    while (downloads.lastChild) {
        downloads.lastChild.remove();
    }

    fetch("/available-downloads", {
        headers: {
            "Content-Type": "application/json"
        }
    }).then(res => {
        if (res.ok) {
            return res.json();
        }
    }).then(data => {
        data.forEach(e => {
            const newFileItem = download_item.cloneNode();
            newFileItem.textContent = `${e.name}     (${getFormattedFileSize(e.size)})`;
            downloads.appendChild(newFileItem);
            newFileItem.addEventListener("click", () => {
                downloadFileWithProgress(`/download/${e.uuid}`);
            });
        });
        // Hide the loader when the request is complete
        hideLoader();
    }).catch(error => {
        console.error('Error fetching available downloads:', error);
        // Hide the loader even if there is an error
        hideLoader();
    });
}

function downloadFileWithProgress(url) {
    const xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.responseType = "blob";

    xhr.onprogress = function(event) {
        if (event.lengthComputable) {
            const percentComplete = (event.loaded / event.total) * 100;
            updateProgress(Math.round(percentComplete));
        }
    };

    xhr.onloadstart = function() {
        showLoader();
        updateProgress(0);
    };

    xhr.onloadend = function() {
        hideLoader();
    };

    xhr.onload = function() {
        if (xhr.status === 200) {
            // Create a link to download the file
            const link = document.createElement("a");
            link.style.display = "none";
            const url = window.URL.createObjectURL(xhr.response);
            link.href = url;
            link.download = getFileNameFromContentDisposition(xhr.getResponseHeader('Content-Disposition'));
            document.body.appendChild(link);
            link.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(link);
        }
    };

    xhr.onerror = function() {
        console.error('Error downloading the file');
        hideLoader();
    };

    xhr.send();
}


function getFileNameFromContentDisposition(contentDisposition) {
    let fileName = "downloadedFile";
    if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/);
        if (fileNameMatch.length === 2) {
            fileName = fileNameMatch[1];
        }
    }
    return fileName;
}

function getFormattedFileSize(s) {
    const levels = ["B","KB","MB","GB","TB","PB"];
    let level = 0;
    let isGood = false;
    while(!isGood) {
        if( s > 1200 && level < levels.length ) {
            s /= 1024;
            level++;
        }
        else {
            isGood = true;
        }
    }
    return `${s.toFixed(2)} ${levels[level]}`;
    
}


/* uploading */
uploadInput.addEventListener('input', function() {
            const xhr = new XMLHttpRequest();
            xhr.open("POST", "/upload");

            const formData = new FormData();
            formData.append("file", this.files[0]);

            xhr.upload.onprogress = function(event) {
                if (event.lengthComputable) {
                    const percentComplete = (event.loaded / event.total) * 100;
                    updateProgress(Math.round(percentComplete));
                }
            };

            xhr.onloadstart = function() {
                showLoader();
                updateProgress(0);
            };

            xhr.onloadend = function() {
                hideLoader();
            };

            xhr.onload = function() {
                if (xhr.status === 200) {
                    console.log('File uploaded successfully');
                } else {
                    console.error('Error uploading the file');
                }
            };

            xhr.onerror = function() {
                console.error('Error uploading the file');
                hideLoader();
            };

            xhr.send(formData);
        });



/* reloading page */
window.addEventListener('load', function (event) {
    showLoader();
    updateProgress(0);

    let progress = 0;
    const interval = setInterval(() => {
        progress += 1;
        if (progress > 100) {
            progress = 100;
            clearInterval(interval);
            hideLoader();
        }
        updateProgress(progress);
    }, 30);

    console.log('Page is refreshing...');
});

