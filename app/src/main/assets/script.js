const filesDownloadButton = document.getElementById("file-downloads-button");
const filesDownloadDialog = document.getElementById("files-download-dialog");
const filesDownloadDialogBottomOk = document.getElementById("files-download-dialog-bottom-ok");
const filesDownloadList   = document.getElementById("files-download-list");

const fileDownloadListItem = document.createElement("li");
fileDownloadListItem.className = "files-download-list-item";

filesDownloadButton.onclick = () => {
    filesDownloadDialog.style.display = 'flex';
    requestAvailableDownloads();
};

filesDownloadDialogBottomOk.onclick = () => {
    filesDownloadDialog.style.display = 'none';
};



function requestAvailableDownloads() {
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
    })
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