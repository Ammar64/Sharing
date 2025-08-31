import { Box, IconButton, SvgIcon, TextField, ThemeProvider } from "@mui/material";
import { useRef } from "react";
import { useTranslation } from "react-i18next";
import { useSharingAppDarkTheme } from "utils/utils";
function MessageInput({ onSend }: { onSend: (value: string) => boolean }) {
    const theme = useSharingAppDarkTheme();
    const inputRef = useRef<HTMLInputElement>(null);
    const { t } = useTranslation();
    const TextFieldHeightPx = 57.98;

    function handleOnSend() {
        const message = inputRef.current?.value;
        if (message) {
            const success = onSend(message.trim());
            if (success) { // if sent successfully clear the text in the TextField.
                inputRef.current!.value = "";
            }
        }
    }
    return <ThemeProvider theme={theme}>
        <Box sx={{
            width: "100%",
            display: "flex",
            alignItems: "end",
            paddingY: 2.5
        }}>
            <TextField inputRef={inputRef} sx={{
                width: "100%",
                backgroundColor: "#1c1c1c",
                borderRadius: `${TextFieldHeightPx * 0.5}px`,

            }}
                slotProps={{
                    input: {
                        sx: {
                            borderRadius: `${TextFieldHeightPx * 0.5}px`,
                        }
                    }
                }}
                multiline
                maxRows={5}
                variant="outlined"
                placeholder={t("enter_message")} />
            <IconButton sx={{
                height: `${TextFieldHeightPx}px`,
                width: `${TextFieldHeightPx}px`,
                marginInlineStart: 2,
                backgroundColor: "#1c1c1c",
                border: "0.2px solid #eeeeee44"
            }} onClick={handleOnSend} onTouchEnd={function (e) {
                e.preventDefault();
                handleOnSend();
            }}>
                <SvgIcon>
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" {...(document.documentElement.dir === "rtl" && { transform: "scale(-1, 1)" })}><path fill="currentColor" d="M4.4 19.425q-.5.2-.95-.088T3 18.5V14l8-2l-8-2V5.5q0-.55.45-.837t.95-.088l15.4 6.5q.625.275.625.925t-.625.925z" /></svg>
                </SvgIcon>
            </IconButton>
        </Box>
    </ThemeProvider>
}
export default MessageInput;