import { DownloadObject } from './interfaces'

const sendBtn = document.getElementById("sendBtn")!;
const recieveBtn = document.getElementById("recieveBtn")!;
const noDownloadsText = document.getElementById("no-downloads-text")!;
const downloadsBubbleOkButton = document.getElementById('okButton')!;
const uploadDisabledDialogOkButton = document.getElementById("uploadDisabledDialogOkBtn")!;
const updateBtn = document.getElementById('update')!;
const usernameBtn = document.getElementById('button-username')!;
const downloads = document.getElementById("downloads")!;

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
    openDownloadBubble(downloadBubble);
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


usernameBtn.onclick = () => {
    openBubble(usernameBubble);
};

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
    downloadItemSize.className = "download-item-size"
    downloadItemSize.dir = "ltr"
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

/* username field */
const usernameForm = document.getElementById('usernameForm')!;

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
            closeBubbles([usernameBubble]); // Assuming closeBubbles accepts an array
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
