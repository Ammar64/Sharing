export interface DownloadObject {
    uuid: string;
    name: string;
    hasSplits: boolean;
    size: number;
}

export interface Message {
    type: string,
    author: string,
    content: string
}