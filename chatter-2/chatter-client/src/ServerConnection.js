import React, { useRef, useState } from 'react';
import { v4 as uuidv4 } from 'uuid';

const serverConnection = {
    name: "TestServer",
    address: "127.0.0.1:5000"
};

const messageHistory = [
    {
        id: uuidv4(),
        content: "Connecting to server " + serverConnection.name + "... Connected. Say Hello!",
        timestamp: Date.now().valueOf()
    },
    {
        id: uuidv4(),
        content: "Dummy: Hello!",
        timestamp: Date.now().valueOf() + 20000000000
    }
];

const MessageEntry = (props) => (
    <div className="message-entry">
        <span className="message-entry-date">{new Date(props.timestamp).toDateString()}</span>
        <span className="message-entry-content">{props.content}</span>
    </div>
);

function formatMessageContent(content) {
    
}

const ServerConnection = () => {
    
    const messageTextRef = useRef(null);
    const [updateCounter, setUpdateCounter] = useState(0);

    function sendMessage(content, completeCallback) {
        messageHistory.push({
            id: uuidv4(),
            content: formatMessageContent(content),
            timestamp: Date.now().valueOf()
        });
        completeCallback();
        setUpdateCounter(updateCounter + 1);
    }

    function handleKeyPress(ev) {
        if (ev.key === "Enter") {
            if (!ev.shiftKey) {
                ev.preventDefault();
                ev.stopPropagation();
                sendMessage("You: " + messageTextRef.current.value, () => {
                    messageTextRef.current.value = "";
                });
            }
        }
    }

    return (
        <div className="app-card chat-container">
            <h1>{serverConnection.name}<span>@{serverConnection.address}</span></h1>
            <div className="app-card message-container">
                {messageHistory.map((message) => <MessageEntry key={message.id} timestamp={message.timestamp} content={message.content} />)}
            </div>
            <div className="chat-input-container">
                <textarea maxLength="2048" placeholder="Send message..." onKeyPress={handleKeyPress} ref={messageTextRef} />
            </div>
        </div>
    )
};

export default ServerConnection;