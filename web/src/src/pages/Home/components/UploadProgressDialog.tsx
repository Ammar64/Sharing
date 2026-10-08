import { forwardRef, Ref, useContext, useEffect, useImperativeHandle, useState } from "react";
import { DialogRef } from "common/dialog";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, SvgIcon, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import styled from "@emotion/styled";
import UploadProgressItem from "./UploadProgressItem";
import useSharingMainWebSocket from "common/hooks/sharing_main_websocket";
import { FileUploadManager } from "../api/upload";
import { FilesUploadsContext } from "common/contexts";


const VisuallyHiddenInput = styled('input')({
    clip: 'rect(0 0 0 0)',
    clipPath: 'inset(50%)',
    height: 1,
    overflow: 'hidden',
    position: 'absolute',
    bottom: 0,
    left: 0,
    whiteSpace: 'nowrap',
    width: 1,
});

export interface FileUpload {
    file: File,
    uploadManager: FileUploadManager
}

function UploadProgressDialog(_: unknown, ref: Ref<DialogRef>) {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    useImperativeHandle(ref, () => {
        return {
            setDialogOpen: (_open) => setOpen(_open)
        }
    });

    const { filesUploadsList, setFilesUploadsList } = useContext(FilesUploadsContext);
    const { lastJsonMessage } = useSharingMainWebSocket();
    useEffect(function () {
        if (lastJsonMessage !== null && lastJsonMessage.action === "change-upload-state") {
            if (!lastJsonMessage.upload_allowed) setOpen(false);
        }
    }, [lastJsonMessage]);
    return (
        <Dialog
            open={open}
            fullWidth={true}
            onClose={() => setOpen(false)}>
            <DialogTitle textAlign="center">
                <Button
                    component="label"
                    role={undefined}
                    variant="contained"
                    tabIndex={-1}
                    sx={{
                        display: "inline-flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        fontWeight: "bold"
                    }}>
                    {t("upload_files")}
                    <VisuallyHiddenInput
                        type="file"
                        onChange={(event) => {
                            if (event.target.files) {
                                const files = event.target.files
                                const fileUploads = [...filesUploadsList];
                                for (let i = 0; i < files.length; i++) {
                                    const fum = new FileUploadManager(files[i]);
                                    fum.startUpload();
                                    const fileUpload: FileUpload = {
                                        file: files[i],
                                        uploadManager: fum
                                    };
                                    fileUploads.push(fileUpload);
                                }
                                setFilesUploadsList(fileUploads);
                            }
                        }}
                        multiple />
                    <SvgIcon sx={{
                        marginInlineStart: 1.2
                    }}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path fill="currentColor" d="M11 16V7.85l-2.6 2.6L7 9l5-5l5 5l-1.4 1.45l-2.6-2.6V16zm-5 4q-.825 0-1.412-.587T4 18v-3h2v3h12v-3h2v3q0 .825-.587 1.413T18 20z" /></svg>
                    </SvgIcon>
                </Button>
            </DialogTitle>
            <DialogContent dividers>
                <Stack direction="column">
                    {
                        filesUploadsList.length !== 0 ? filesUploadsList.map((e, i) => {
                            return (
                                <UploadProgressItem key={`upi-${i}`}
                                    fileUpload={e} />
                            );
                        }) : <Typography textAlign="center">{t("no_uploads_yet")}</Typography>
                    }
                </Stack>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => setOpen(false)}>{t("close")}</Button>
            </DialogActions>
        </Dialog>
    );
}

export default forwardRef<DialogRef, unknown>(UploadProgressDialog);