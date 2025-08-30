import { Card, CardActionArea, CardContent, Tooltip, Typography, useTheme } from "@mui/material";
import { useTranslation } from "react-i18next";

export interface SharingMainActionCardProps {
    bgcolor: string,
    action_string_key: string,
    onClick?: () => void,
    icon?: string,
    disabled?: boolean,
    tooltipKey?: string,
    disabledTooltipKey?: string,
}

function SharingMainActionCard({ cardProps }: { cardProps: SharingMainActionCardProps }) {
    const { t } = useTranslation();
    const theme = useTheme();
    if (!cardProps.disabled) {
        cardProps.disabled = false;
    }

    const disabledCardStyle = {
        backgroundColor: '#b3b3b3ff',
        opacity: 0.8,
        cursor: 'not-allowed',
    };

    return (
        <Tooltip placement="top" title={cardProps.disabled ? t(cardProps.disabledTooltipKey!) : t(cardProps.tooltipKey!)} slotProps={{
            tooltip: {
                sx: {backgroundColor: theme.palette.error.main}
            }
        }}>
            <Card sx={{
                backgroundColor: cardProps.bgcolor,
                ...(cardProps.disabled ? disabledCardStyle : {})
            }}>
                <CardActionArea onClick={cardProps.onClick} disabled={cardProps.disabled}>
                    <CardContent>
                        <Typography sx={{ textAlign: "cener", fontSize: "x-large" }}>
                            {t(cardProps.action_string_key)}
                        </Typography>
                    </CardContent>
                </CardActionArea>
            </Card>
        </Tooltip>
    );
}

export default SharingMainActionCard;