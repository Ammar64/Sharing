const overlay = document.getElementById('overlay')!;

// dialogs
const usernameBubble = document.getElementById("usernameBubble")!;
const downloadBubble = document.getElementById("downloadBubble")! as HTMLDivElement;
const uploadDisabledDialog = document.getElementById("uploadDisabledDialog")!;

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

// special bubble
function openDownloadBubble(bubble: HTMLDivElement) {
    bubble.style.display = 'flex';
    overlay.style.display = 'block';
}


overlay.onclick = () => {
    closeBubbles([downloadBubble, uploadDisabledDialog, usernameBubble]);
}