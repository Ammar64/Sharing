import { DownloadObject } from './main.d'

const sendBtn = document.getElementById("sendBtn")!;
const recieveBtn = document.getElementById("recieveBtn")!;
const downloadBubble = document.getElementById("downloadBubble")!;
const noDownloadsText = document.getElementById("no-downloads-text")!;
const loginBubble = document.getElementById("loginBubble")!;
const uploadDisabledDialog = document.getElementById("uploadDisabledDialog")!;
const overlay = document.getElementById('overlay')!;
const downloadsBubbleOkButton = document.getElementById('okButton')!;
const uploadDisabledDialogOkButton = document.getElementById("uploadDisabledDialogOkBtn")!;
const updateBtn = document.getElementById('update')!;
const loginBtn = document.getElementById('button-username')!;
const downloads = document.getElementById("downloads")!;
const uploadInput = document.getElementById('uploadInput') as HTMLInputElement;
const loader = document.getElementById('overlay-process')! as HTMLElement;

// translations from HTML
const currentUsernameText = document.getElementById('current-user-text')!.textContent!;
const downloadText = document.getElementById('download-text')!.textContent!;

let userId = -1;

sendBtn.onclick = function () {
    fetch("/check-upload-allowed", {
        method: "POST",
    }).then(function (res) {
        if (res.ok) {
            return res.json();
        }
    }).then(function (res) {
        if (res.allowed)
            uploadInput.click();
        else
            openBubble(uploadDisabledDialog);
    }).catch(function (err) {
        alert(err);
    });
}

recieveBtn.onclick = () => {
    openBubble(downloadBubble);
    requestAvailableDownloads();
};

updateBtn.onclick = () => {
    requestAvailableDownloads();
}

downloadsBubbleOkButton.onclick = () => {
    closeBubbles(downloadBubble);
};

uploadDisabledDialogOkButton.onclick = () => {
    closeBubbles(uploadDisabledDialog);
}

overlay.onclick = () => {
    closeBubbles([downloadBubble, uploadDisabledDialog, loginBubble]);
}

loginBtn.onclick = () => {
    openBubble(loginBubble);
};

function openBubble(bubble: HTMLElement) {
    bubble.style.display = 'block';
    overlay.style.display = 'block';
}

function closeBubbles(bubbles: HTMLElement[] | HTMLElement) {
    // Ensure bubbles is always treated as an array
    if (!Array.isArray(bubbles)) {
        bubbles = [bubbles];
    }

    bubbles.forEach(bubble => {
        if (bubble && bubble.style.display !== 'none') {
            bubble.style.display = 'none';
        }
    });
    overlay.style.display = 'none';
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

function updateProgress(percent: number) {
    const progressBar = document.querySelector('#progress-bar') as HTMLElement;
    const progressText = document.querySelector('#progress-text') as HTMLElement;
    progressBar.style.width = percent + '%';
    progressText.textContent = percent + '%';
}

function makeDownloadItem(e: DownloadObject) {
    const downloadItem = document.createElement("li");
    downloadItem.className = "download-item";

    const downloadItemInfoContainer = document.createElement("div");
    downloadItemInfoContainer.className = "download-item-info";

    const downloadItemIcon = document.createElement("img");
    downloadItemIcon.className = "download-item-img"
    downloadItemIcon.src = `/get-icon/${e.uuid}`

    const downloadItemName = document.createElement("span");
    downloadItemName.className = "download-item-name"
    downloadItemName.textContent = e.name;

    const downloadItemSize = document.createElement("span");
    downloadItemSize.textContent = !e.hasSplits ? `(${getFormattedFileSize(e.size)})` : '(splits)'

    const downloadItemButton = document.createElement("a");
    downloadItemButton.className = "button-download icon-button"
    downloadItemButton.setAttribute("download", "");
    downloadItemButton.href = `/download/${e.uuid}`

    const downloadItemButtonIcon = document.createElement('img');
    downloadItemButtonIcon.src = "assets/icon-download.svg"
    const downloadItemButtonText = document.createTextNode(downloadText);
    downloadItemButton.append(downloadItemButtonIcon, downloadItemButtonText);

    downloadItemInfoContainer.append(downloadItemIcon, downloadItemName, downloadItemSize, downloadItemButton);

    const colorfulLine = document.createElement("div");
    colorfulLine.className = "colorful-line";

    downloadItem.append(downloadItemInfoContainer, colorfulLine);

    return downloadItem;
}

function requestAvailableDownloads() {
    noDownloadsText.style.display = "none";
    while (downloads.lastChild) {
        downloads.lastChild.remove();
    }

    fetch("/available-downloads", {
        headers: {
            "Content-Type": "application/json"
        }
    }).then(res => {
        if (res.status == 200) {
            return res.json();
        } else if (res.status == 401) {
            window.location.replace("/blocked");
        }
    }).then(data => {
        if (data.length == 0) {
            noDownloadsText.style.display = "block";
            return;
        } else {
            noDownloadsText.style.display = "none";
        }
        data.forEach((e: DownloadObject) => {
            downloads.appendChild(makeDownloadItem(e));
        });
    }).catch(error => {
        console.error('Error fetching available downloads:', error);
    });
}

function downloadFileWithProgress(url: string) {
    const link = document.createElement("a");
    link.href = url
    document.body.append(link);
    link.click();
    link.remove();
    // const xhr = new XMLHttpRequest();
    // xhr.open("GET", url, true);
    // xhr.responseType = "blob";

    // xhr.onprogress = function(event) {
    //     if (event.lengthComputable) {
    //         const percentComplete = (event.loaded / event.total) * 100;
    //         updateProgress(Math.round(percentComplete));
    //     }
    // };

    // xhr.onloadstart = function() {
    //     showLoader();
    //     updateProgress(0);
    // };

    // xhr.onloadend = function() {
    //     hideLoader();
    // };

    // xhr.onload = function() {
    //     if (xhr.status === 200) {
    //         // Create a link to download the file
    //         const link = document.createElement("a");
    //         link.style.display = "none";
    //         const url = window.URL.createObjectURL(xhr.response);
    //         link.href = url;
    //         link.download = getFileNameFromContentDisposition(xhr.getResponseHeader('Content-Disposition'));
    //         document.body.appendChild(link);
    //         link.click();
    //         window.URL.revokeObjectURL(url);
    //         document.body.removeChild(link);
    //     }
    // };

    // xhr.onerror = function() {
    //     console.error('Error downloading the file');
    //     hideLoader();
    // };

    // xhr.send();
}

function getFormattedFileSize(s: number) {
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


/* uploading */
uploadInput.addEventListener('input', function (e) {
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/upload/" + encodeURIComponent(this.files![0].name));

    xhr.upload.onprogress = function (event) {
        if (event.lengthComputable) {
            const percentComplete = (event.loaded / event.total) * 100;
            updateProgress(Math.round(percentComplete));
        }
    };

    xhr.onloadstart = function () {
        showLoader();
        updateProgress(0);
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


/* username field */
const usernameForm = document.getElementById('usernameForm')!;
/*     const loginBubble = document.getElementById('loginBubble'); // Assuming this is the login bubble element
 */
// Check if username is already stored in localStorage
let storedUsername = localStorage.getItem('username');

// if localStorage Empty ask server for default name. this could be "User-0" or "User-1"
if (!storedUsername) {
    fetch("/get-user-info", {
        method: "GET"
    }).then(function (res) {
        if (res.status === 200) return res.json();
        else throw "ERROR GETTING USER INFO";
    }).then(function (res) {
        userId = res.id;
        document.querySelector('.current-username p')!.textContent = `${currentUsernameText} ${res.username}`;
    }).catch(function (err) {
        alert("Error getting username");
    })
} else { // else tell the server about the stored name
    updateUsername(storedUsername);
}


usernameForm.addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent default form submission
    const usernameInput = (document.getElementById('usernameInput') as HTMLInputElement).value.trim(); // Trim whitespace

    // Check if username is valid (not empty or undefined)
    if (!usernameInput || usernameInput === '') {
        alert('Please enter a valid username.');
        return;
    }

    updateUsername(usernameInput);
    // Check if username has changed
    if (usernameInput !== storedUsername) {
        // Save updated username to localStorage
        localStorage.setItem('username', usernameInput);
        storedUsername = usernameInput; // Update storedUsername variable

        // Update display of current username
        document.querySelector('.current-username p')!.textContent = `${currentUsernameText} ${storedUsername}`;
    }

});

/**
 * @param {string} username 
 */
function updateUsername(username: string) {
    fetch('/update-user-name', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username: username })
    })
        .then(response => {
            if (response.status !== 200) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Username updated successfully:', data);
            if (data.changed) {
                if (data.username) {
                    updateStoredUsername(data.username);
                } else {
                    throw new Error()
                }
            }
            closeBubbles([loginBubble]); // Assuming closeBubbles accepts an array
        })
        .catch(error => {
            console.error('Error updating username:', error);
            // Handle error scenarios
            alert('Failed to update username');
        });
}

function updateStoredUsername(username: string) {
    localStorage.setItem("username", username);
    storedUsername = username;
    // Update the display of current username
    document.querySelector('.current-username p')!.textContent = `${currentUsernameText} ${username}`;
}
/*  */
function setVhProperty() {
    var vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty('--vh', `${vh}px`);
}

// Set the initial value
setVhProperty();

// Update the value on resize
window.addEventListener('resize', setVhProperty);
