const filesDownloadButton = document.getElementById("file-downloads-button");
const filesDownloadDialog = document.getElementById("files-download-dialog");
const overlay = document.getElementById('overlay');
const okButton = document.getElementById('files-download-dialog-bottom-ok');
const filesDownloadList   = document.getElementById("files-download-list");

const fileDownloadListItem = document.createElement("li");

fileDownloadListItem.className = "files-download-list-item";


filesDownloadButton.onclick = () => {
    filesDownloadDialog.style.display = 'block';
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
    filesDownloadDialog.style.display = 'none';
};



function requestAvailableDownloads() {
    // Show the loader when the request starts
    showLoader();
    while(filesDownloadList.lastChild) {filesDownloadList.lastChild.remove();}
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
            const newFileItem = fileDownloadListItem.cloneNode();
            newFileItem.textContent = `${e.name}     (${getFormattedFileSize(e.size)})`;
            filesDownloadList.appendChild(newFileItem);
            newFileItem.addEventListener("click", () => {
                let link = document.createElement("a");
                link.style.display = "none";
                link.href = `/download/${e.uuid}`
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            })
        });
        // Hide the loader when the request is complete
        hideLoader();
    }).catch(error => {
        console.error('Error fetching available downloads:', error);

        // Hide the loader even if there is an error
        hideLoader();
    });
}

function getFormattedFileSize(s) {
    const levels = ["B", "KB", "MB", "GB", "TB", "PB"];
    let level = 0;
    let isGood = false;
    while (!isGood) {
        if (s > 1200 && level < levels.length) {
            s /= 1024;
            level++;
        }
        else {
            isGood = true;
        }
    }
    return `${s.toFixed(2)} ${levels[level]}`;
    
}


const loader = document.querySelector('.process');
function showLoader() {
    loader.style.display= 'block';
}

function hideLoader() {
    document.querySelector('.plane').style.animation = 'plane-done 1.2s infinite';
    setTimeout(() => {
        loader.style.display = 'none';
    }, 1000);
}

/* reloading page */
window.addEventListener('onload', function (event) {
    showLoader();
    console.log('Page is unloading...');
});

window.addEventListener('load', function (event) {
    hideLoader();
    console.log('Page has fully loaded.');
});
