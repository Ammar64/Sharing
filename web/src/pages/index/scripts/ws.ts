declare function updateAvailableDownloads(): void;

const disconnectedDiv = document.getElementById("disconnected")! as HTMLDivElement;
const retryInSpan = document.getElementById("reconnect_in_seconds")! as HTMLSpanElement;

function startWebSocket() {
    var webSocket = new WebSocket("/info");
    webSocket.addEventListener("open", function() {
        disconnectedDiv.style.display = "none";
    });

    webSocket.addEventListener("message", function (event) {
        const data = JSON.parse(event.data);
        switch (data.type) {
            case "message":
                // TODO: Indicate message received
                break;
            case "update-downloads":
                updateAvailableDownloads();
                break;
            case "go-to-video-page":
                window.location.pathname = "/pages/video-stream"
                break;
            case "go-to-audio-page":
                window.location.pathname = "/pages/audio-stream"
                break;
    
        }

    })

    webSocket.addEventListener("close", function () {
        (webSocket as WebSocket | null) = null;
        disconnectedDiv.style.display = "block"
    
        let timer = 4;
        retryInSpan.textContent = String(timer)
        const intervalId = setInterval(function() {
            timer--;
            retryInSpan.textContent = String(timer)
            if( timer == 0 ) {
                clearInterval(intervalId)
            }
        }, 1000);
        setTimeout(startWebSocket, 4000);
    });
}
startWebSocket();


