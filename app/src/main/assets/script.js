/* try {
 */    const sendBtn = document.getElementById("sendBtn");
// when click shows you native choose file dialog
const uploadLabel = document.getElementById("uploadLabel");
const recieveBtn = document.getElementById("recieveBtn");
const downloadBubble = document.getElementById("downloadBubble");
const loginBubble = document.getElementById("loginBubble");
const alertDialog = document.getElementById("alertDialog")
const overlay = document.getElementById('overlay');
const downloadsBubbleOkButton = document.getElementById('okButton');
const alertDialogOkButton = document.getElementById("alertDialogOkBtn")
const updateBtn = document.getElementById('update');
const loginBtn = document.getElementById('loginBtn');
const downloads = document.getElementById("downloads");
const uploadInput = document.getElementById('uploadInput');

const download_item = document.createElement("li");
download_item.className = "download-item";

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
            openBubble(alertDialog);
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

alertDialogOkButton.onclick = () => {
    closeBubbles(alertDialog);
}

overlay.onclick = () => {
    closeBubbles([downloadBubble, alertDialog, loginBubble]);
}

loginBtn.onclick = () => {
    openBubble(loginBubble);
};

function openBubble(bublle) {
    bublle.style.display = 'block';
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
        if (res.status == 200) {
            return res.json();
        } else if (res.status == 401) {
            window.location.replace("/blocked");
        }
    }).then(data => {
        data.forEach(e => {
            const newFileItem = download_item.cloneNode();
            const newFileItemImg = document.createElement("img");
            newFileItemImg.className = "download-item-img";

            const newFileItemText = document.createElement("span");
            newFileItemText.className = "download-item-name";

            newFileItem.appendChild(newFileItemImg)
            newFileItem.appendChild(newFileItemText);

            if (e.type === "app") {
                newFileItemImg.style.display = "block"
                newFileItemImg.src = "/get-icon/".concat(e.uuid);
            } else {
                newFileItemImg.style.display = "none"
            }
            newFileItemText.textContent = e.name.concat("     ").concat(!e.hasSplits ? `(${getFormattedFileSize(e.size)})` : '(splits)');
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
        if (res.ok) return res.json();
        else throw "ERROR GETTING USER INFO";
    }).then(function (res) {
        userId = res.id;
        storedUsername = res.username;
        localStorage.setItem('username', storedUsername);
    }).catch(function (err) {
        console.error(err.message);
    })
}

// Update the display of current username
document.querySelector('.current-username p').textContent = `Current username: ${storedUsername}`;

usernameForm.addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent default form submission

    const usernameInput = document.getElementById('usernameInput').value.trim(); // Trim whitespace

    // Check if username is valid (not empty or undefined)
    if (!usernameInput || usernameInput === '') {
        alert('Please enter a valid username.');
        return;
    }

    // Check if username has changed
    if (usernameInput !== storedUsername) {
        // Save updated username to localStorage
        localStorage.setItem('username', usernameInput);
        storedUsername = usernameInput; // Update storedUsername variable

        // Update display of current username
        document.querySelector('.current-username p').textContent = `Current username: ${storedUsername}`;
    }

    const url = '/update-user-name';

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username: usernameInput })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Username updated successfully:', data);
            // Optionally, perform actions after successful submission
            closeBubbles([loginBubble]); // Assuming closeBubbles accepts an array
        })
        .catch(error => {
            console.error('Error updating username:', error);
            // Handle error scenarios
            closeBubbles([loginBubble]);
        });
});

// /* reloading page */
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
    }, .01);

    console.log('Page is refreshing...');
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