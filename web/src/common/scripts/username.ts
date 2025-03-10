let userId = -2; // unset
let alreadySentUsername = false;
async function getUsername(): Promise<string> {
    // Check if username is already stored in localStorage
    let storedUsername = localStorage.getItem("username");

    // if localStorage Empty ask server for default name. this could be "User-0" or "User-1"
    if (!storedUsername) {
        try {
            const response = await fetch("/get-user-info", {
                method: "GET"
            })
            if (response.status == 200) {
                const resJSON = await response.json();
                userId = resJSON.id;
                const username = resJSON.username;
                return username;
            } else {
                throw "ERROR GETTING USER INFO";
            }
        } catch (e: any) {
            alert("Error getting username: ".concat(e.message));
            return "";
        }
    } else { // else tell the server about the stored name
        if(!alreadySentUsername) {
            await updateUsername(storedUsername);
            alreadySentUsername = true;
        }
        return storedUsername;
    }
}

/**
 * @param {string} username 
 */
async function updateUsername(username: string) {
    try {
        const response = await fetch('/update-user-name', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username: username })
        });

        if (response.status !== 200) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        if (data.changed) {
            if (data.username) {
                updateStoredUsername(data.username);
                console.log('Username updated successfully:', data);
            } else {
                throw new Error()
            }
        }
        userId = data.userID;
    } catch (e) {
        console.error('Error updating username:', e);
        // Handle error scenarios
        alert('Failed to update username');
    }
}

function updateStoredUsername(username: string) {
    localStorage.setItem("username", username);
}