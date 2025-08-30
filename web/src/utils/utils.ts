import { createTheme } from "@mui/material";
import { TFunction } from "i18next";

export function createNewSharingAppTheme(uiMode: "dark" | "light", dir: "rtl" | "ltr") {
    return createTheme({
        palette: {
            mode: uiMode
        },
        direction: dir,
        components: {
            MuiCssBaseline: {
                styleOverrides: {
                    body: {
                        background: uiMode === "light" ?
                            "linear-gradient(to left, #67addf, #df90df)" :
                            "linear-gradient(to left, #010c14, #2e012e)"
                    }
                }
            }
        }
    });
}

export function getFormattedFileSize(s: number) {
    const levels = ["B", "KB", "MB", "GB", "TB", "PB"];
    let level = 0;
    let isGood = false;
    while (!isGood) {
        if (s > 1200 && level < levels.length) {
            s /= 1024;
            level++;
        }
        else {
            isGood = true;
        }
    }
    return `${s.toFixed(2)} ${levels[level]}`;
}

export interface Time {
    seconds: number,
    minutes: number,
    hours: number
}

export function convertSeconds(seconds: number): Time {
    seconds = Math.round(seconds)
    const remainingSeconds = seconds % 60;
    const remainingMinutes = Math.round((seconds - remainingSeconds) / 60) % 60
    const remainingHours = Math.round((seconds - remainingSeconds - (remainingMinutes * 60)) / 3600);
    return {
        seconds: remainingSeconds,
        minutes: remainingMinutes,
        hours: remainingHours
    };
}

export function getFormattedTime(time: Time, t: TFunction): string {
    let hourText = "";
    let minutesText = "";
    let secondsText = "";
    if (time.hours >= 1) {
        switch (time.hours) {
            case 1:
                hourText = t("one_hour");
                break;
            case 2:
                hourText = t("two_hours");
                break;
            default:
                let hourTextGrammer: string;
                hourTextGrammer = t("hours");
                hourText = `${time.hours} ${hourTextGrammer}`
                break;
        }
    }
    if (time.minutes >= 1) {
        switch (time.hours) {
            case 1:
                minutesText = t("one_minute");
                break;
            case 2:
                minutesText = t("two_minutes");
                break;
            default:
                let minutesTextGrammer: string;
                minutesTextGrammer = t("minutes");
                minutesText = `${time.minutes} ${minutesTextGrammer}`
                break;
        }
    }
    if (time.minutes >= 1) {
        switch (time.hours) {
            case 1:
                secondsText = t("one_second");
                break;
            case 2:
                secondsText = t("two_seconds");
                break;
            default:
                let secondsTextGrammer: string;
                secondsTextGrammer = t("seconds");
                secondsText += `${time.seconds} ${secondsTextGrammer}`
                break;
        }
    }
    return "TODO";
}