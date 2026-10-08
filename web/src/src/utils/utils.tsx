import { createTheme } from "@mui/material";

export function createNewSharingAppTheme(uiMode: "dark" | "light", dir: "rtl" | "ltr") {
    const { palette } = createTheme();
    const { augmentColor } = palette;
    const createColor = (mainColor: string) => augmentColor({ color: { main: mainColor } });

    const theme = createTheme({
        palette: {
            mode: uiMode,
            primary: createColor("#2196f3"),
            secondary: createColor("#e91e63")
        },
        direction: dir,
        typography: {
            fontFamily: "Almarai"
        },
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
    return theme;
}

export function useSharingAppDarkTheme() {
    return createNewSharingAppTheme("dark", document.documentElement.dir as any);
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

export function getFormattedTime(time: Time): string {
    let timeText = "";
    // include hours if bigger than 0
    // minutes will always be included
    if (time.hours > 0) {
        timeText = timeText.concat(time.hours.toString().padStart(2, '0')).concat(":")
    }
    timeText = timeText.concat(time.minutes.toString().padStart(2, '0')).concat(":").concat(time.seconds.toString().padStart(2, '0'));
    return timeText;
}
