import { Box, Typography } from "@mui/material";
import { Message } from "../Messages";

interface MyMessageProps {
    message: Message,
}

function MyMessage({ message }: MyMessageProps) {
    return <Box sx={{
        display: "flex",
        justifyContent: "start",
        marginY: 1,

    }}>
        <Box sx={{
            display: "block",
            backgroundColor: "#e91e63",
            borderRadius: 4,
            borderStartStartRadius: 0,
            margin: 0,
            padding: 1.5,
        }}>
            <Typography component="pre" fontFamily="monospace Almarai" dir="auto" sx={{
                margin: "0px",
                color: "#eeeeee",
                whiteSpace: "pre-wrap",
                wordBreak: "break-word"
            }}>{message.content}</Typography>
        </Box>
    </Box>
}
export default MyMessage;