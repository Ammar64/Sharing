import { Container, Grid } from "@mui/material";
import { useTranslation } from "react-i18next";
import SharingMainActionCard from "./components/SharingMainActionCard";
import { Ref, useRef } from "react";
import DownloadsDialog from "./components/DownloadsDialog";
import {DialogRef} from "common/dialog"
import UploadProgressDialog from "./components/UploadProgressDialog";
function Home() {
    const { t } = useTranslation()
    const downloadDialogRef: Ref<DialogRef> = useRef(null);
    const uploadProgressDialogRef: Ref<DialogRef> = useRef(null);

    return (
        <Container>
            <Grid container spacing={3}>
                <Grid size={{xs: 12, md: 6}}>
                    <SharingMainActionCard cardProps={{ action_string_key: "upload", bgcolor: "cyan", onClick: () => uploadProgressDialogRef.current?.setDialogOpen(true) }} />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <SharingMainActionCard cardProps={{ action_string_key: "download", bgcolor: "tomato", onClick: () => downloadDialogRef.current?.setDialogOpen(true)}} />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <SharingMainActionCard cardProps={{ action_string_key: "messages", bgcolor: "blue" }} />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <SharingMainActionCard cardProps={{ action_string_key: "stream", bgcolor: "red" }} />
                </Grid>
            </Grid>
            <DownloadsDialog ref={downloadDialogRef} />
            <UploadProgressDialog ref={uploadProgressDialogRef}/>
        </Container>
    );
}

export default Home;