import styled from "@emotion/styled";
import { Avatar, Button, Link as MuiLink, Skeleton } from "@mui/material";
import EllipsisTypography from "common/components/EllipsisTypography";
import { useTranslation } from "react-i18next";
import { getFormattedFileSize } from "utils/utils";

interface DownloadItemProps {
    name: string,
    downloadLink: string,
    iconSrc: string,
    size: number,
    hasSplits: boolean,
    isStreamable: boolean
}


const DownloadItemGridBox = styled('div')({
    display: "grid",
    width: "100%",
    columnGap: "8px",
    gridTemplateColumns: "min-content auto min-content min-content",
    gridTemplateRows: "1fr 1fr"
});

function DownloadItem(props: DownloadItemProps) {
    const { t } = useTranslation();
    return (
        <DownloadItemGridBox>
            <Avatar src={props.iconSrc} sx={{ gridRow: '1/3', alignSelf: "center" }} />
            <EllipsisTypography sx={{ gridRow: '1/2' }}>{props.name}</EllipsisTypography>
            <EllipsisTypography sx={{ gridRow: '2/3' }}>{getFormattedFileSize(props.size)} {props.hasSplits && `(${t("splits")})`}</EllipsisTypography>
            {props.isStreamable && <Button variant="contained" color="error" sx={{ gridRow: '1/3', gridColumn: '3/4' }}>{t("play")}</Button>}
            <Button component={MuiLink} href={props.downloadLink} download variant="contained" color="primary" sx={{ gridRow: '1/3', gridColumn: '4/5', alignSelf: "center" }}>{t("download")}</Button>
        </DownloadItemGridBox>
    );
}

export function DownloadItemSkeleton() {
    return (
        <DownloadItemGridBox>
            <Skeleton width={40} height={40} variant="circular" sx={{ gridRow: '1/3', alignSelf: "center" }} />
            <Skeleton width="80%" variant="text" sx={{ gridRow: '1/2' }}></Skeleton>
            <Skeleton width="20%" variant="text" sx={{ gridRow: '2/3' }}></Skeleton>
            <Skeleton width={100} height={30} variant="rounded" color="primary" sx={{ gridRow: '1/3', gridColumn: '4/5', alignSelf: "center" }}></Skeleton>
        </DownloadItemGridBox>
    );
}

export default DownloadItem;
