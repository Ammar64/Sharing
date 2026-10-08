import { Button, Container, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography, useMediaQuery, useTheme } from "@mui/material";
import { useState, forwardRef, useImperativeHandle, Ref, useEffect, JSX, useRef } from "react";
import { useTranslation } from "react-i18next";
import DownloadItem, { DownloadItemSkeleton } from "./DownloadItem";
import { ApiError, Downloadable, getAvailableDownloads } from "../api/downloads";
import DownloadAllDialog from "./DownloadAllDialog";
import { DialogRef } from "common/dialog";
import useSharingMainWebSocket from "common/hooks/sharing_main_websocket";

function DownloadsDialog(_: unknown, ref: Ref<DialogRef>) {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    const theme = useTheme();
    const fullScreen = useMediaQuery(theme.breakpoints.down('sm'));

    const downloadAllRef = useRef<DialogRef>(null);

    useImperativeHandle(ref, () => {
        return {
            setDialogOpen: (_open) => setOpen(_open)
        }
    });

    const [isLoading, setIsLoading] = useState(true);
    const [isError, setIsError] = useState(false);
    const [downloads, setDownloads] = useState<Downloadable[]>();

    const {lastJsonMessage} = useSharingMainWebSocket();
    useEffect(() => {
        
        if (open || (open && lastJsonMessage?.action === "update-downloads")) {
            const timeout_id = setTimeout(() => setIsLoading(true), 500);
            (async () => {
                const availableDownloads = await getAvailableDownloads();
                if (availableDownloads instanceof ApiError) {
                    setIsError(true);
                } else {
                    setDownloads(availableDownloads);
                }
                clearTimeout(timeout_id);
                setIsLoading(false);
            })();
        }
    }, [open, lastJsonMessage]);

    let dialogContent: JSX.Element;

    if (isLoading) {
        dialogContent = (<>
            {([...Array(4)].map((e, i) => { return <DownloadItemSkeleton key={i} /> }))}
        </>);
    } else if (isError) {
        dialogContent = (<Container>
            <Typography textAlign="center" color="error" fontSize="large">{t("downloads_request_error_text")}</Typography>
        </Container>);
    } else if (downloads == undefined || downloads?.length == 0) {
        dialogContent = (<Container>
            <Typography textAlign="center" color="info" fontSize="large">{t("no_downloads_available")}</Typography>
        </Container>)
    } else {
        dialogContent = <>{downloads.map((e, i) => {
            return (
                <DownloadItem key={i}
                    name={e.name}
                    downloadLink={"/download/" + e.uuid}
                    hasSplits={e.hasSplits}
                    iconSrc={"/get-icon/" + e.uuid}
                    isStreamable={false}
                    size={e.size} />
            );
        })}
        </>
    }

    return (
        <Dialog
            open={open}
            fullScreen={fullScreen}
            fullWidth={true}
            onClose={() => setOpen(false)}>

            <DialogTitle>
                {t('available_downloads')}
            </DialogTitle>
            <DialogContent dividers sx={{ padding: 2 }}>
                <Stack gap={1}>
                    {dialogContent}
                </Stack>
            </DialogContent>
            <DialogActions> 
                {(!isLoading && !isError && downloads !== undefined && downloads.length >= 2) &&
                    <Button autoFocus onClick={() => downloadAllRef.current?.setDialogOpen(true)}>
                        {t('download_all')}
                    </Button>
                }
                <Button autoFocus onClick={() => setOpen(false)}>
                    {t('done')}
                </Button>
            </DialogActions>
            <DownloadAllDialog ref={downloadAllRef} />
        </Dialog>
    );
}

export default forwardRef<DialogRef, unknown>(DownloadsDialog);