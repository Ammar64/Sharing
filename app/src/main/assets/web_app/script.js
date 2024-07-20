/* try {
 */    const sendBtn = document.getElementById("sendBtn");
// when click shows you native choose file dialog
const uploadLabel = document.getElementById("uploadLabel");
const recieveBtn = document.getElementById("recieveBtn");
const downloadBubble = document.getElementById("downloadBubble");
const noDownloadsText = document.getElementById("no-downloads-text");
const loginBubble = document.getElementById("loginBubble");
const uploadDisabledDialog = document.getElementById("uploadDisabledDialog")
const overlay = document.getElementById('overlay');
const downloadsBubbleOkButton = document.getElementById('okButton');
const uploadDisabledDialogOkButton = document.getElementById("uploadDisabledDialogOkBtn")
const updateBtn = document.getElementById('update');
const loginBtn = document.getElementById('loginBtn');
const downloads = document.getElementById("downloads");
const uploadInput = document.getElementById('uploadInput');
const download_item = document.createElement("li");
download_item.className = "download-item";


const currentUsernameText = document.getElementById('current-user-text').textContent;
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
            uploadLabel.click();
        else
            openBubble(uploadDisabledDialog);
    }).catch(function (err) {
        alert("remote device disconnected");
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

function openBubble(bubble) {
    bubble.style.display = 'block';
    overlay.style.display = 'block';
}

function closeBubbles(bubbles) {
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


const loader = document.querySelector('.process');
function showLoader() {
    loader.style.display = 'block';
}

function hideLoader() {
    document.querySelector('.plane').style.animation = 'plane-done 1.2s infinite';
    setTimeout(() => {
        loader.style.display = 'none';
        document.querySelector('.plane').style.animation = 'plane-on-progress 5s infinite';
    }, 600);
}

function updateProgress(percent) {
    const progressBar = document.querySelector('.progress-bar');
    const progressText = document.querySelector('.progress-text');
    progressBar.style.width = percent + '%';
    progressText.textContent = percent + '%';
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
        if( data.length == 0 ) {
            noDownloadsText.style.display = "block";
            return;
        } else {
            noDownloadsText.style.display = "none";
        }
        data.forEach(e => {
            const newFileItem = download_item.cloneNode();

            const newFileItemImg = document.createElement("img");
            newFileItemImg.className = "download-item-img";

            const newFileItemText = document.createElement("span");
            newFileItemText.className = "download-item-name";

            const colorfulLine = document.createElement("div");
            colorfulLine.className = "colorful-line";

            const infoContainer = document.createElement("div");
            infoContainer.className = "infoContainer";

            // Create button element
            var downloadBtn = document.createElement('button');
            downloadBtn.classList.add('Btn1');

            // Create div for the sign
            var markDiv = document.createElement('div');
            markDiv.classList.add('sign');

            // Create the SVG element
            const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
            svg.setAttribute('viewBox', '0 0 512 512');
            svg.innerHTML = '<path d="M288 32c0-17.7-14.3-32-32-32s-32 14.3-32 32V274.7l-73.4-73.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3l128 128c12.5 12.5 32.8 12.5 45.3 0l128-128c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L288 274.7V32zM64 352c-35.3 0-64 28.7-64 64v32c0 35.3 28.7 64 64 64H448c35.3 0 64-28.7 64-64V416c0-35.3-28.7-64-64-64H346.5l-45.3 45.3c-25 25-65.5 25-90.5 0L165.5 352H64zm368 56a24 24 0 1 1 0 48 24 24 0 1 1 0-48z"/>';

            // Append SVG to sign div
            markDiv.appendChild(svg);

            // Create div for text
            var textDiv = document.createElement('div');
            textDiv.classList.add('text1');
            textDiv.textContent = '   download';

            // Append text div to button
            downloadBtn.appendChild(markDiv);
            downloadBtn.appendChild(textDiv);


            infoContainer.appendChild(newFileItemImg)
            infoContainer.appendChild(newFileItemText);
            infoContainer.appendChild(downloadBtn);
            newFileItem.appendChild(infoContainer);
            newFileItem.appendChild(colorfulLine);

            if (e.type !== "file") {
                newFileItemImg.style.display = "block"
                newFileItemImg.src = "/get-icon/".concat(e.uuid);
            } else {
                newFileItemImg.style.display = "none"
            }
            newFileItemText.textContent = e.name.concat("     ").concat(!e.hasSplits ? `(${getFormattedFileSize(e.size)})` : '(splits)');
            downloads.appendChild(newFileItem);
            downloadBtn.addEventListener("click", () => {
                downloadFileWithProgress(`/download/${e.uuid}`);
            });
        });
    }).catch(error => {
        console.error('Error fetching available downloads:', error);
    });
}

function downloadFileWithProgress(url) {
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
    xhr.open("POST", "/upload/" + encodeURIComponent(this.files[0].name));

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

    xhr.send(e.target.files[0]);
});


/* username field */
const usernameForm = document.getElementById('usernameForm');
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
        updateStoredUsername(res.username);


    }).catch(function (err) {
        console.error(err.message);
    })
} else { // else tell the server about the stored name
    updateUsername(storedUsername);
}


usernameForm.addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent default form submission
    const usernameInput = document.getElementById('usernameInput').value.trim(); // Trim whitespace

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
        document.querySelector('.current-username p').textContent = `${currentUsernameText} ${storedUsername}`;
    }

});

/**
 * @param {string} username 
 */
function updateUsername(username) {
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
            if(data.changed) {
                updateStoredUsername(data.username);
            }
            closeBubbles([loginBubble]); // Assuming closeBubbles accepts an array
        })
        .catch(error => {
            console.error('Error updating username:', error);
            // Handle error scenarios
            closeBubbles([loginBubble]);
        });
}

function updateStoredUsername(username) {
    localStorage.setItem("username", username);
    storedUsername = username;
    // Update the display of current username
    document.querySelector('.current-username p').textContent = `${currentUsernameText} ${storedUsername}`;
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