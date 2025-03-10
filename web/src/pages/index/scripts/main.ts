import { DownloadObject } from '../../../common/scripts/interfaces'

const sendBtn = document.getElementById("sendBtn")!;
const recieveBtn = document.getElementById("recieveBtn")!;
const downloadsErrorSpan = document.getElementById("downloads-error")!;
const downloadsBubbleOkButton = document.getElementById('okButton')!;
const uploadDisabledDialogOkButton = document.getElementById("uploadDisabledDialogOkBtn")!;
const updateBtn = document.getElementById('update')!;
const usernameBtn = document.getElementById('button-username')!;
const downloads = document.getElementById("downloads")!;
const downloadAllLink = document.getElementById("downloadAllLink")!;

// translations from HTML
const currentUsernameText = document.getElementById('current-user-text')!.textContent!;
const downloadText = document.getElementById('download-text')!.textContent!;
const noDownloadsAvailableText = document.getElementById('no-downloads-text')!.textContent!;
const downloadsRequestErrorText = document.getElementById('downloads-request-error-text')!.textContent!;

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
    updateAvailableDownloads();
};

updateBtn.onclick = () => {
    updateAvailableDownloads();
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
    downloadItemButton.className = "button-download icon-text-button"
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

function updateAvailableDownloads(): void {
    downloadsErrorSpan.style.display = "none";
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
            downloadsErrorSpan.style.display = "block";
            downloadsErrorSpan.textContent = noDownloadsAvailableText
            downloadAllLink.style.display = 'none';
            return;
        } else {
            downloadsErrorSpan.style.display = "none";
            downloadAllLink.style.display = 'block';
        }
        data.forEach((e: DownloadObject) => {
            downloads.appendChild(makeDownloadItem(e));
        });
    }).catch(error => {
        downloadsErrorSpan.style.display = "block";
        downloadsErrorSpan.textContent = downloadsRequestErrorText;
    });
}

/* username field */
const usernameForm = document.getElementById('usernameForm')!;


getUsername().then(function (user) {
    document.querySelector('.current-username p')!.textContent = `${currentUsernameText} ${user}`;
});

usernameForm.addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent default form submission
    const usernameInput = (document.getElementById('usernameInput') as HTMLInputElement).value.trim(); // Trim whitespace

    // Check if username is valid (not empty or undefined)
    if (!usernameInput || usernameInput === '') {
        alert('Please enter a valid username.');
        return;
    }

    let storedUsername = localStorage.getItem("username");
    if (usernameInput !== storedUsername) {
        updateUsername(usernameInput);
        // Save updated username to localStorage
        localStorage.setItem('username', usernameInput);
        storedUsername = usernameInput; // Update storedUsername variable

        // Update display of current username
        document.querySelector('.current-username p')!.textContent = `${currentUsernameText} ${storedUsername}`;
    }

});

/*  */
function setVhProperty() {
    var vh = window.innerHeight * 0.01;
    document.documentElement.style.setProperty('--vh', `${vh}px`);
}

// Set the initial value
setVhProperty();

// Update the value on resize
window.addEventListener('resize', setVhProperty);
