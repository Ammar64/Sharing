import { Card, CardActionArea, CardContent, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

export interface SharingMainActionCardProps {
    bgcolor: string,
    action_string_key: string,
    onClick?: () => void,
    icon?: string,
}

function SharingMainActionCard({ cardProps }: { cardProps: SharingMainActionCardProps }) {
    const { t } = useTranslation();
    return (
        <Card sx={{
            backgroundColor: cardProps.bgcolor
        }}>
            <CardActionArea onClick={cardProps.onClick}>
                <CardContent>
                    <Typography sx={{ textAlign: "cener", fontSize: "x-large" }}>
                        {t(cardProps.action_string_key)}
                    </Typography>
                </CardContent>
            </CardActionArea>
        </Card>

    );
}

export default SharingMainActionCard;