const messagesPage = document.getElementById("messages-page")!;
const openMessagesPageB = document.getElementById("button-messages")!;
const messagesContainer = document.getElementById("messages-container")!;
const messageInput = document.getElementById("message-input")! as HTMLTextAreaElement;
const sendMessageButton = document.getElementById("send-message-button")!;
const goBackButton = document.getElementById("go-back-button")!;

openMessagesPageB.addEventListener("click", function (e) {
    messagesPage.style.display = 'flex';
});

goBackButton.addEventListener("click", function (e) {
    messagesPage.style.display = 'none';
})

function startWebSocket() {
    var webSocket = new WebSocket("/ws");
    sendMessageButton.addEventListener("click", sendMessage);
    function sendMessage() {
        const message = messageInput.value;
        messageInput.value = '';
        webSocket.send(message);
        addMessage(message, false);
    }

    function addMessage(message: string, remote: boolean) {
        const messageDiv = document.createElement("div");
        messageDiv.className = remote ? "others-messages" : "my-messages";
        messageDiv.textContent = message;
        messagesContainer.prepend(messageDiv);
    }

    webSocket.addEventListener("message", function (event) {
        addMessage(event.data, true);
    })

    webSocket.addEventListener("close", function () {
        sendMessageButton.removeEventListener("click", sendMessage);
        (webSocket as WebSocket | null) = null;
        setTimeout(startWebSocket, 2000);
    });
}
startWebSocket();