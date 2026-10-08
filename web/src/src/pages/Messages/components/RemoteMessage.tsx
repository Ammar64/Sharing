import { Box, IconButton, SvgIcon, Typography } from "@mui/material";
import EllipsisTypography from "common/components/EllipsisTypography";
import { useState } from "react";
import { Message } from "../Messages";

interface RemoteMessageProps {
    message: Message,
}

function RemoteMessage({ message }: RemoteMessageProps) {
    const [messageCopied, setMessageCopied] = useState(false);
    function copyMessage() {
        if (navigator?.clipboard) {
            navigator.clipboard.writeText(message.content);
            navigator?.vibrate(150);
            setMessageCopied(true);
            setTimeout(function() {
                setMessageCopied(false);
            }, 2000);
        }
    }
    return <Box sx={{
        display: "flex",
        justifyContent: "end",
        marginY: 1,

    }}>
        <Box sx={{
            display: "block",
            backgroundColor: "#ebebeb",
            borderRadius: 4,
            borderStartEndRadius: 0,
            margin: 0,
            padding: 1.5,
        }}>
            <Typography component="pre"
                dir="auto"
                fontFamily="monospace Almarai"
                sx={{
                    margin: "0px",
                    color: "#1c1c1c",
                    whiteSpace: "pre-wrap",
                    wordBreak: "break-word"
                }}>{message.content}</Typography>
            <Box sx={{
                display: "flex",
                minWidth: "100%",
                justifyContent: "space-between",
                alignItems: "center",
                paddingTop: 0.5,
                borderTop: "1px solid #212121",
            }}>
                <IconButton onClick={copyMessage}
                    sx={{
                        color: "#111111"
                    }}
                    size="small">
                    <SvgIcon>
                        {
                            messageCopied ?
                                <svg xmlns="http://www.w3.org/2000/svg" width={24} height={24} viewBox="0 0 24 24"><path fill="currentColor" d="m9.55 15.15l8.475-8.475q.3-.3.7-.3t.7.3t.3.713t-.3.712l-9.175 9.2q-.3.3-.7.3t-.7-.3L4.55 13q-.3-.3-.288-.712t.313-.713t.713-.3t.712.3z"></path></svg>
                                :
                                <svg xmlns="http://www.w3.org/2000/svg" width={24} height={24} viewBox="0 0 24 24"><path fill="currentColor" d="M9 18q-.825 0-1.412-.587T7 16V4q0-.825.588-1.412T9 2h9q.825 0 1.413.588T20 4v12q0 .825-.587 1.413T18 18zm0-2h9V4H9zm-4 6q-.825 0-1.412-.587T3 20V7q0-.425.288-.712T4 6t.713.288T5 7v13h10q.425 0 .713.288T16 21t-.288.713T15 22zm4-6V4z"></path></svg>
                        }
                    </SvgIcon>
                </IconButton>
                <Box sx={{
                    color: "#11111177",
                }}>
                    <EllipsisTypography textAlign="end">{message.author}</EllipsisTypography>
                    <Typography fontFamily="monospace">{message.remoteIP}</Typography>
                </Box>
            </Box>
        </Box>
    </Box>
}
export default RemoteMessage;