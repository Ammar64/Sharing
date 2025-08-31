import { Box, Container, Grid } from "@mui/material";
import { useTranslation } from "react-i18next";
import SharingMainActionCard from "./components/SharingMainActionCard";
import { Ref, useEffect, useRef, useState } from "react";
import DownloadsDialog from "./components/DownloadsDialog";
import { DialogRef } from "common/dialog"
import UploadProgressDialog from "./components/UploadProgressDialog";
import useSharingMainWebSocket from "common/hooks/sharing_main_websocket";

function Home() {
    const { t } = useTranslation()
    const downloadDialogRef: Ref<DialogRef> = useRef(null);
    const uploadProgressDialogRef: Ref<DialogRef> = useRef(null);
    const { lastJsonMessage } = useSharingMainWebSocket();
    const [disableUpload, setDisableUpload] = useState(false);
    useEffect(function () {
        (async function () {
            const uploadInfoRes = await fetch("/check-upload-allowed");
            const uploadInfo = await uploadInfoRes.json();
            setDisableUpload(!uploadInfo.allowed);
        })()
    }, [])

    useEffect(function () {
        if (lastJsonMessage === null) return;
        if (lastJsonMessage.action === "change-upload-state") {
            setDisableUpload(!lastJsonMessage.upload_allowed);
        }
    }, [lastJsonMessage])
    return (
        <Box sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            height: "100dvh"
        }}>
            <Container maxWidth="sm">
                <Grid container spacing={3}>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <SharingMainActionCard
                            cardProps={{ disabled: disableUpload, action_string_key: "upload", bgcolor: "cyan", textColor: "black", onClick: () => uploadProgressDialogRef.current?.setDialogOpen(true), disabledTooltipKey: "uploading_is_disabled" }} />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <SharingMainActionCard cardProps={{ action_string_key: "download", bgcolor: "tomato", onClick: () => downloadDialogRef.current?.setDialogOpen(true) }} />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <SharingMainActionCard cardProps={{ action_string_key: "messages", bgcolor: "blue", link_to:"/messages" }} />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <SharingMainActionCard cardProps={{ action_string_key: "stream", bgcolor: "red" }} />
                    </Grid>
                </Grid>
                <DownloadsDialog ref={downloadDialogRef} />
                <UploadProgressDialog ref={uploadProgressDialogRef} />
            </Container>
        </Box>
    );
}

export default Home;