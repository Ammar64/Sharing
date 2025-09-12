import styled from "@emotion/styled";
import { Box, LinearProgress, LinearProgressProps, Paper, Tooltip, Typography } from "@mui/material";
import EllipsisTypography from "common/components/EllipsisTypography";
import { useEffect, useRef, useState } from "react";
import { CompleteStatus, Progress, FileUploadManager } from "../api/upload";
import { convertSeconds, getFormattedFileSize, getFormattedTime } from "utils/utils";
import { useTranslation } from "react-i18next";
import { FileUpload } from "./UploadProgressDialog";

interface UploadProgressItemProps {
    fileUpload: FileUpload;
}

const UploadProgressItemGridBox = styled('div')({
    display: "grid",
    width: "100%",
    columnGap: "8px",
    gridTemplateColumns: "auto min-content",
    gridTemplateRows: "1fr 1fr 1fr",
    alignItems: "center",
    padding: "8px 16px"
});

function percentage(progress: Progress) {
    return progress.loaded / progress.total * 100;
}

function UploadProgressItem(props: UploadProgressItemProps) {
    const { t } = useTranslation();
    const linearProgressRef = useRef<LinearProgressProps>(null);
    const fum = props.fileUpload.uploadManager;
    const [progress, setProgress] = useState(fum.progress);
    
    const currentSpeed = fum.currentSpeed;
    const totalRemaningSeconds = currentSpeed !== 0 ? (progress.total - progress.loaded) / currentSpeed : 0;
    const remaningTime = convertSeconds(totalRemaningSeconds);
    
    fum.progressCallback = function(progress) {
        setProgress(progress);
    }

    let timeText = (function () {
        switch (progress.completeStatus) {
            case CompleteStatus.IN_PROGRESS:
                return getFormattedTime(remaningTime);
            case CompleteStatus.FAILED:
            case CompleteStatus.COMPLETED:
                return ""
        }
    })();

    return (
        <Paper elevation={5} sx={{ marginTop: 2 }}>
            <UploadProgressItemGridBox>
                <Box sx={{
                    gridRow: "1/2",
                    gridColumn: "1/3",
                    display: "flex",
                    justifyContent: "space-between"
                }}>
                    <EllipsisTypography textAlign="start">{props.fileUpload.file.name}</EllipsisTypography>
                    <Tooltip placement="top" title={t("expected_remaining_time")}>
                        <Typography textAlign="end">{timeText}</Typography>
                    </Tooltip>
                </Box>
                <Box sx={{ gridRow: "2/3", gridColumn: "1/3" }}>
                    <Typography>
                        <span dir="ltr">{(progress.completeStatus === CompleteStatus.COMPLETED ? getFormattedFileSize(fum.total) : `${getFormattedFileSize(fum.loaded)} / ${getFormattedFileSize(fum.total)}`)}</span>&nbsp;&nbsp;&nbsp;
                        <span dir="ltr">{progress.completeStatus === CompleteStatus.IN_PROGRESS ? `(${getFormattedFileSize(currentSpeed)}/s)` : (progress.completeStatus === CompleteStatus.COMPLETED && `(${t("time_taken_to_send", { time: getFormattedTime(convertSeconds(fum.getTotalOperationTime)) })})`)}</span>
                    </Typography>
                </Box>
                <Box sx={{ gridRow: "3/4", gridColumn: "1/3", display: "flex", alignItems: "center", gap: 1 }}>
                    <LinearProgress
                        ref={linearProgressRef}
                        value={progress.completeStatus === CompleteStatus.COMPLETED || progress.completeStatus === CompleteStatus.FAILED ? 100 : percentage(progress)}
                        variant="determinate"
                        color={(function () {
                            switch (progress.completeStatus) {
                                case CompleteStatus.COMPLETED:
                                    return "success";
                                case CompleteStatus.FAILED:
                                    return "error";
                                case CompleteStatus.IN_PROGRESS:
                                    return "primary";
                            }
                        }())}
                        sx={{ width: "100%" }}
                    />
                    <Typography width="fit-content" textAlign="end">{(function () {
                        switch (progress.completeStatus) {
                            case CompleteStatus.IN_PROGRESS:
                                return `${percentage(progress).toFixed(2)}%`
                            case CompleteStatus.COMPLETED:
                                return t("completed");
                            case CompleteStatus.FAILED:
                                return t("failed");
                        }
                    })()}</Typography>
                </Box>
            </UploadProgressItemGridBox>
        </Paper>
    );
}

export default UploadProgressItem;