const recieveBtn = document.getElementById("recieveBtn");
const downloadBubble = document.getElementById("downloadBubble");
const overlay = document.getElementById('overlay');
const okButton = document.getElementById('okButton');
const downloads   = document.getElementById("downloads");

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
    while(downloads.lastChild) {downloads.lastChild.remove();}
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

