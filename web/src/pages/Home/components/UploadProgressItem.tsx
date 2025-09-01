import styled from "@emotion/styled";
import { Box, LinearProgress, LinearProgressProps, Paper, Tooltip, Typography } from "@mui/material";
import EllipsisTypography from "common/components/EllipsisTypography";
import { useEffect, useRef, useState } from "react";
import { CompleteStatus, Progress, UploadOperation } from "../api/upload";
import { convertSeconds, getFormattedFileSize, getFormattedTime } from "utils/utils";
import { useTranslation } from "react-i18next";

interface UploadProgressItemProps {
    file: File;
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


function UploadProgressItem(props: UploadProgressItemProps) {
    const { t } = useTranslation();
    const [progress, setProgress] = useState<Progress>({ completeStatus: CompleteStatus.IN_PROGRESS, computable: true, loaded: 0, total: 100 });
    const linearProgressRef = useRef<LinearProgressProps>(null);

    const uploadOperation = useRef(new UploadOperation(props.file, (progress) => {
        if (progress == null) {
            linearProgressRef.current!.variant = "indeterminate";
        } else if (progress.completeStatus) {
            linearProgressRef.current!.variant = "determinate";
            linearProgressRef.current!.color = "success";
            setProgress(progress);
        } else {
            setProgress(progress);
        }
    }));

    useEffect(() => {
        uploadOperation.current.startUpload();
    }, [])

    const currentSpeed = uploadOperation.current.currentSpeed;
    const loaded = progress.loaded;
    const total = progress.total;
    const percentage = progress.loaded / progress.total * 100;
    const totalRemaningSeconds = currentSpeed !== 0 ? (total - loaded) / currentSpeed : 0;
    const remaningTime = convertSeconds(totalRemaningSeconds);

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
                    <EllipsisTypography textAlign="start">{props.file.name}</EllipsisTypography>
                    <Tooltip placement="top" title={t("expected_remaining_time")}>
                        <Typography textAlign="end">{timeText}</Typography>
                    </Tooltip>
                </Box>
                <Box sx={{ gridRow: "2/3", gridColumn: "1/3" }}>
                    <Typography>
                        <span dir="ltr">{(progress.completeStatus === CompleteStatus.COMPLETED ? getFormattedFileSize(progress.total) : `${getFormattedFileSize(progress.loaded)} / ${getFormattedFileSize(progress.total)}`)}</span>&nbsp;&nbsp;&nbsp;
                        <span dir="ltr">{progress.completeStatus === CompleteStatus.IN_PROGRESS ? `(${getFormattedFileSize(currentSpeed)}/s)` : (progress.completeStatus === CompleteStatus.COMPLETED && `(${t("time_taken_to_send", { time: getFormattedTime(convertSeconds(uploadOperation.current.getTotalOperationTime)) })})`)}</span>
                    </Typography>
                </Box>
                <Box sx={{ gridRow: "3/4", gridColumn: "1/3", display: "flex", alignItems: "center", gap: 1 }}>
                    <LinearProgress
                        ref={linearProgressRef}
                        value={progress.completeStatus === CompleteStatus.COMPLETED || progress.completeStatus === CompleteStatus.FAILED ? 100 : percentage}
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
                                return `${percentage.toFixed(2)}%`
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