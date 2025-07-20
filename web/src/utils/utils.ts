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
