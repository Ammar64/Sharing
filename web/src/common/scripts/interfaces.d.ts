export interface DownloadObject {
    uuid: string;
    name: string;
    hasSplits: boolean;
    size: number;
}

export interface Message {
    type: string;
    author: string;
    authorID: number;
    content: string;
    remote: boolean;
}

export interface RTCOffer {
    type: string;
    sdp:  string;
    sdptype: string;
}