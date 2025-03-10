import { Message } from "../../../common/scripts/interfaces";
declare function getUsername(): Promise<string>;

// run after getting username
getUsername().then(messagesPage)

function messagesPage() {
    const messagesContainer = document.getElementById("messages-container")!;
    const messageInput = document.getElementById("message-input")! as HTMLTextAreaElement;
    const sendMessageButton = document.getElementById("send-message-button")!;
    const goBackButton = document.getElementById("go-back-button")!;
    const disconnectedDiv = document.getElementById("disconnected")! as HTMLDivElement;
    const retryInSpan = document.getElementById("reconnect_in_seconds")! as HTMLSpanElement;

    // messages
    function addMessage(message: Message) {
        const messageDiv = document.createElement("div");
        messageDiv.className = message.remote ? "others-messages" : "my-messages";
        messageDiv.textContent = message.content;
        if (message.author != null) {
            const authorText = document.createElement("span")
            authorText.textContent = message.author
            authorText.className = "message-author-text"
            messageDiv.appendChild(document.createElement("br"))
            messageDiv.appendChild(authorText);
        }
        messagesContainer.prepend(messageDiv);
    }

    // get messages
    fetch("/get-all-messages", {
        method: "GET",
    }).then(function (res) {
        if (res.status === 200) {
            return res.json();
        } else {
            throw new Error("Failed to get messages")
        }
    }).then(function (data: Array<Message>) {
        for (let i = 0; i < data.length; i++) {
            data[i].remote = data[i].authorID != userId;
            addMessage(data[i])
        }
    }).catch(function (e) {
        // TODO: Handle errors
    });

    // websocket
    goBackButton.addEventListener("click", function (e) {
        history.back();
    })

    function startWebSocket() {
        var webSocket = new WebSocket("/ws");
        webSocket.addEventListener("open", function () {
            disconnectedDiv.style.display = "none";
        });

        sendMessageButton.addEventListener("click", sendMessage);
        async function sendMessage() {
            if(messageInput.value == '') return;
            const messageContent = messageInput.value;
            messageInput.value = '';
            const username = await getUsername();
            const message: Message = {
                type: "message",
                author: username,
                authorID: userId,
                content: messageContent,
                remote: false
            };
            webSocket.send(JSON.stringify(message));
            addMessage(message);
        }

        webSocket.addEventListener("message", function (event) {
            const data = JSON.parse(event.data);
            switch (data.type) {
                case "message":
                    const message: Message = data;
                    message.remote = message.authorID != userId;
                    addMessage(message);
                    break;
                case "info":
                    break;
            }

        })

        webSocket.addEventListener("close", function () {
            sendMessageButton.removeEventListener("click", sendMessage);
            (webSocket as WebSocket | null) = null;
            disconnectedDiv.style.display = "block"

            let timer = 4;
            retryInSpan.textContent = String(timer)
            const intervalId = setInterval(function () {
                timer--;
                retryInSpan.textContent = String(timer)
                if (timer == 0) {
                    clearInterval(intervalId)
                }
            }, 1000);
            setTimeout(startWebSocket, 4000);
        });
    }
    startWebSocket();
}