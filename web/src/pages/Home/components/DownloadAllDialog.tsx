import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Link, Stack, TextField, Typography } from "@mui/material";
import { forwardRef, Ref, useImperativeHandle, useState } from "react";
import { useTranslation } from "react-i18next";

export interface DownloadAllDialogRef {
    setDialogOpen: (open: boolean) => void
}

function DownloadAllDialog(_: unknown, ref: Ref<DownloadAllDialogRef>) {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);
    const [fileNameInput, setFileNameInput] = useState("files");

    useImperativeHandle(ref, () => {
        return {
            setDialogOpen: (_open) => setOpen(_open)
        }
    })

    return (

        <Dialog
            open={open} // TODO: Changed temporary for debuging
        >
            <DialogTitle>{t("download_all")}</DialogTitle>
            <DialogContent>
                <Stack direction="row" alignItems="center" dir="ltr">
                    <TextField /* TODO: Check how RTL layout behaves */
                        value={fileNameInput}
                        onChange={e => setFileNameInput(e.target.value)}
                        variant="outlined"
                        label={t("file_name")}
                        sx={{ marginTop: 1 }}
                    />
                    <Typography marginLeft={1} fontSize={16} paddingTop={1}>.zip</Typography>
                </Stack>
                <Typography color="textSecondary"
                    fontSize={14}
                    maxWidth={300}>{t("all_files_download_info")}</Typography>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => setOpen(false)}>{t("cancel")}</Button>
                <Button download component={Link} href={`/da?file_name=${encodeURIComponent(fileNameInput)}`}>{t("download")}</Button>
            </DialogActions>
        </Dialog>
    );
}

export default forwardRef(DownloadAllDialog);