import styled from "@emotion/styled";
import { Box, LinearProgress, LinearProgressProps, Typography } from "@mui/material";
import EllipsisTypography from "common/components/EllipsisTypography";
import { useEffect, useRef, useState } from "react";
import { Progress, UploadOperation } from "../api/upload";
import { convertSeconds, getFormattedTime } from "utils/utils";
import { useTranslation } from "react-i18next";

interface UploadProgressItemProps {
    file: File;
}

const UploadProgressItemGridBox = styled('div')({
    display: "grid",
    width: "100%",
    columnGap: "8px",
    gridTemplateColumns: "auto min-content",
    gridTemplateRows: "1fr 1fr",
    alignItems: "center"
});


function UploadProgressItem(props: UploadProgressItemProps) {
    const { t } = useTranslation();
    const [progress, setProgress] = useState<Progress>({ completed: false, computable: true, loaded: 0, total: 100 });
    const linearProgressRef = useRef<LinearProgressProps>(null);

    const uploadOperation = useRef(new UploadOperation(props.file, (progress) => {
        if (progress == null) {
            linearProgressRef.current!.variant = "indeterminate";
        } else if (progress.completed) {
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

    const avgSpeed = uploadOperation.current.averageUploadSpeed;
    const loaded = progress.loaded;
    const total = progress.total;
    const percentage = progress.loaded / progress.total * 100;
    const totalRemaningSeconds = (total - loaded) / avgSpeed;
    const remaningTime = convertSeconds(totalRemaningSeconds);

    let timeText = "";
    if (!progress.completed) {
        timeText = getFormattedTime(remaningTime, t);
    } else {
        timeText = getFormattedTime(convertSeconds(uploadOperation.current.getTotalOperationTime), t);
    }

    return (
        <UploadProgressItemGridBox>
            <Box sx={{
                gridRow: "1/2",
                gridColumn: "1/3",
                display: "flex",
                justifyContent: "space-between"
            }}>
                <EllipsisTypography textAlign="start">{props.file.name}</EllipsisTypography>
                <Typography textAlign="end">{timeText}</Typography>
            </Box>
            <Box sx={{ gridRow: "2/3", gridColumn: "1/3", display: "flex", alignItems: "center", gap:1 }}>
                <LinearProgress
                    ref={linearProgressRef}
                    value={progress.completed ? 100 : percentage}
                    variant="determinate"
                    color={progress.completed ? "success" : "primary"}
                    sx={{width: "100%"}}
                />
                <Typography width="fit-content" textAlign="end">{percentage.toFixed(2)}%</Typography>
            </Box>
        </UploadProgressItemGridBox>
    );
}

export default UploadProgressItem;