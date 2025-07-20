import { Container, Grid } from "@mui/material";
import { useTranslation } from "react-i18next";
import SharingMainActionCard from "./components/SharingMainActionCard";
import { Ref, useRef } from "react";
import DownloadsDialog, { DownloadsDialogRef } from "./components/DownloadsDialog";

function Home() {
    const { t } = useTranslation()
    const downloadDialogRef: Ref<DownloadsDialogRef> = useRef(null);

    return (
        <Container>
            <Grid container spacing={3}>
                <Grid size={{xs: 12, md: 6}}>
                    <SharingMainActionCard cardProps={{ action_string_key: "upload", bgcolor: "cyan" }} />
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
        </Container>
    );
}

export default Home;