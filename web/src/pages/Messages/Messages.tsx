import { Box, Container } from "@mui/material";
import MessageInput from "./components/MessageInput";
import MessagesContainer from "./components/MessagesContainer";
import { useEffect, useRef, useState } from "react";
import useMessagesWebsocket from "common/hooks/messages_websocket";
import { ReadyState } from "react-use-websocket";
import { globalProps } from "../../consts";


export interface Message {
    type: string,
    content: string,
    author: string,
    remoteIP?: string,
    isRemote?: boolean
}

function Messages() {
    const [messages, setMessages] = useState<Message[]>([]);
    const messageContainerRef = useRef<HTMLDivElement>(null);

    useEffect(function () {
        fetch("/get-all-messages")
            .then(function (res) {
                if (res.ok) {
                    return res.json();
                } else {
                    throw "Error getting messages"
                }
            }).then(function (messages: Message[]) {
                for (let i = 0; i < messages.length; i++) {
                    messages[i].isRemote = !(messages[i].remoteIP === globalProps.BROWSER_IP);
                }
                setMessages(messages);
            })
    }, []);

    useEffect(function() {
        const container = messageContainerRef.current;
        container?.scrollTo({
            top: container.scrollHeight,
            behavior: "smooth"
        });
    }, [messages]);
    const { sendJsonMessage, lastJsonMessage, readyState } = useMessagesWebsocket();
    useEffect(function () {
        if (lastJsonMessage == null) return;
        lastJsonMessage.isRemote = true;
        setMessages([...messages, lastJsonMessage]);
    }, [lastJsonMessage]);

    return (
        <Container>
            <Box sx={{
                height: "100dvh",
                display: "flex",
                flexDirection: "column",
            }}>
                <MessagesContainer ref={messageContainerRef} messages={messages}/>
                <MessageInput onSend={function(text) {
                    if(readyState !== ReadyState.OPEN) return false;
                    const message: Message = {
                        type: "message",
                        author: "browser user",
                        content: text,
                    };
                    sendJsonMessage(message);
                    message.isRemote = false;
                    setMessages([...messages, message])
                    return true;
                }}/>
            </Box>
        </Container>
    );
}
export default Messages;