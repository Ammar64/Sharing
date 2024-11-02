import { Message } from "./interfaces";
declare function getUsername(): Promise<string>;
declare function requestAvailableDownloads(): void;
const messagesPage = document.getElementById("messages-page")!;
const openMessagesPageB = document.getElementById("button-messages")!;
const messagesContainer = document.getElementById("messages-container")!;
const messageInput = document.getElementById("message-input")! as HTMLTextAreaElement;
const sendMessageButton = document.getElementById("send-message-button")!;
const goBackButton = document.getElementById("go-back-button")!;
const disconnectedDiv = document.getElementById("disconnected")! as HTMLDivElement;
const retryInSpan = document.getElementById("reconnect_in_seconds")! as HTMLSpanElement;

openMessagesPageB.addEventListener("click", function (e) {
    messagesPage.style.display = 'flex';
});

goBackButton.addEventListener("click", function (e) {
    messagesPage.style.display = 'none';
})

function startWebSocket() {
    var webSocket = new WebSocket("/ws");
    webSocket.addEventListener("open", function() {
        disconnectedDiv.style.display = "none";
    });

    sendMessageButton.addEventListener("click", sendMessage);
    async function sendMessage() {

        const username = await getUsername();
        const message = {
            type: "message",
            author: username,
            content: messageInput.value
        };

        webSocket.send(JSON.stringify(message));
        messageInput.value = '';
        addMessage(message.content, null, false);
    }

    function addMessage(message: string, author: string | null, remote: boolean) {
        const messageDiv = document.createElement("div");
        messageDiv.className = remote ? "others-messages" : "my-messages";
        messageDiv.textContent = message;
        if (author != null) {
            const authorText = document.createElement("span")
            authorText.textContent = author
            authorText.className = "message-author-text"
            messageDiv.appendChild(document.createElement("br"))
            messageDiv.appendChild(authorText);
        }
        messagesContainer.prepend(messageDiv);
    }

    webSocket.addEventListener("message", function (event) {
        const data = JSON.parse(event.data);
        switch (data.type) {
            case "message":
                const message: Message = data;
                addMessage(message.content, message.author, true);
                break;
            case "info":
                if (data.info === "update-downloads") {
                    requestAvailableDownloads();
                }
                break;
        }

    })

    webSocket.addEventListener("close", function () {
        sendMessageButton.removeEventListener("click", sendMessage);
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


