const messageHistoryContainer = document.getElementById("message-history-container");
const sendMessageForm = document.getElementById("send-message-form");
const sendMessageInput = document.getElementById("send-message-input");

const serverNameElem = document.getElementById("server-name");
const serverAddrElem = document.getElementById("server-addr");
const usernameElem = document.getElementById("username");

const usernameIllegalStrings = [";"];

function formatMessageEntryTimestamp(timestamp) {
    const timestampDate = new Date(timestamp);
    return `${(timestampDate.getMinutes() + "").padStart(2, "0")}:${(timestampDate.getSeconds() + "").padStart(2, "0")}`;
}

function addMessageEntry(sender, content, id = "", timestamp = new Date().getTime()) {
    const messageContent = `${sender}: ${content}`;

    const msgEntryElem = document.createElement("tr");
    
    const timestampElem = document.createElement("td");
    timestampElem.innerText = formatMessageEntryTimestamp(timestamp);
    
    const msgContentElem = document.createElement("td");
    msgContentElem.innerText = messageContent;
    
    msgEntryElem.appendChild(timestampElem);
    msgEntryElem.appendChild(msgContentElem);

    if (id !== "")
        msgEntryElem.id = id;
    messageHistoryContainer.appendChild(msgEntryElem);
}

/**
 * @param {String} addr
 * @returns {WebSocket}
 */
function connectToServer(addr) {
    const socket = new WebSocket(addr);
    return socket;
}

/**
 * @param {WebSocket} socket 
 * @param {String} username 
 * @returns If the username cannot be set
 */
function registerUsername(socket, username) {
    if (socket.readyState !== WebSocket.OPEN)
        return false;
    if (username.length < 1 || username.length > 16)
        return false;
    usernameIllegalStrings.forEach((illegalString) => {
        if (username.includes(illegalString))
            return false;
    });
    socket.send("c2/CONNECT " + username);
    return true;
}

function interpretServer_MSG(message) {
    message = message.substring("c2/MSG ".length);
    const username = message.substring(0, message.indexOf("; "));
    message = message.substring(username.length + "; ".length);
    const timestamp = message.substring(0, message.indexOf("; "));
    message = message.substring(timestamp.length + "; ".length);
    const messageID = message.substring(0, message.indexOf("; "));
    message = message.substring(messageID.length + "; ".length);

    addMessageEntry(username, message, messageID, Number.parseInt(timestamp));
}

function interpretServer_ACK(message) {
    if (message === "c2/ACKNOWLEDGE") {
        return;
    }
    if (message.startsWith("c2/ACKNOWLEDGE ")) {
        const serverName = message.substring("c2/ACKNOWLEDGE ".length);
        // TODO
    }
}

/**
 * @param {WebSocket} socket 
 */
function createServerMessageHook(socket) {
    if (socket.readyState !== WebSocket.OPEN)
        return;
    
    socket.addEventListener("message", (event) => {
        const msgContent = event.data;
        if (msgContent.startsWith("c2/MSG")) {
            interpretServer_MSG(msgContent);
        } else if (msgContent.startsWith("c2/ACKNOWLEDGE")) {
            interpretServer_ACK(msgContent);
        } else {
            addMessageEntry("Server", msgContent);
        }
    });
}

/**
 * @param {WebSocket} socket 
 */
function setupFormSendMsgHook(socket) {
    sendMessageForm.addEventListener("submit", (event) => {
        event.preventDefault();
        if (socket.readyState === WebSocket.OPEN)
            socket.send(sendMessageInput.value);

            sendMessageForm.reset();
    });
}

let socket = connectToServer("ws://localhost:5001");
socket.addEventListener("open", () => {

    registerUsername(socket, "TestUser");
    createServerMessageHook(socket);

});

setupFormSendMsgHook(socket);