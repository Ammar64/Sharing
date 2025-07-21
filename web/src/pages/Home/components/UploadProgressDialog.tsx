import { forwardRef, Ref, useImperativeHandle, useState } from "react";
import { DialogRef } from "common/dialog";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import styled from "@emotion/styled";
import UploadProgressItem from "./UploadProgressItem";


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


function UploadProgressDialog(_: unknown, ref: Ref<DialogRef>) {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    useImperativeHandle(ref, () => {
        return {
            setDialogOpen: (_open) => setOpen(_open)
        }
    });

    const [fileUploadsList, setFileUploadsList] = useState<File[]>([]);

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
                    tabIndex={-1}>
                    Upload files
                    <VisuallyHiddenInput
                        type="file"
                        onChange={(event) => {
                            if (event.target.files) {
                                const files = event.target.files
                                setFileUploadsList([...fileUploadsList, ...files])
                            }
                        }}
                        multiple />
                </Button>
            </DialogTitle>
            <DialogContent dividers>
                <Stack direction="column">
                    {
                        fileUploadsList.length !== 0 ? fileUploadsList.map( (e, i) => {
                            return (
                                <UploadProgressItem key={`upi-${i}`}
                                    file={e} />
                            );
                        } ) : <Typography textAlign="center">{t("no_uploads_yet")}</Typography>
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