import styled from "@emotion/styled";
import { Typography } from "@mui/material";

const EllipsisTypography = styled(Typography)({
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
});

export default EllipsisTypography;
