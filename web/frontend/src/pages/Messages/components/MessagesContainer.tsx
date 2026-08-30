import { Box } from "@mui/material";
import MyMessage from "./MyMessage";
import RemoteMessage from "./RemoteMessage";
import { Message } from "../Messages";
import { Ref } from "react";

function MessagesContainer({ messages, ref }: { messages: Message[], ref: Ref<HTMLDivElement> }) {
    return <Box ref={ref} sx={{
        height: "100%",
        maxWidth: "100%",
        display: "flex",
        flexDirection: "column",
        overflowY: "auto",
        "& > *": {
            transition: "transform 200ms ease-out"
        },
        "& > :first-child": {
            marginTop: "auto"
        }
    }}>
        {
            messages.map(function (m, i) {
                return m.isRemote ? <RemoteMessage message={m} key={i} /> : <MyMessage message={m} />
            })
        }
    </Box>
}
export default MessagesContainer;