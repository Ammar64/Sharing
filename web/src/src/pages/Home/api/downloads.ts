export interface Downloadable {
    name: string,
    size: number,
    uuid: string,
    hasSplits: boolean;
}

export class ApiError {
    constructor(message: string, id: number) {
        this._message = message;
        this._error_id = id;
    }

    private _message: string = "";
    private _error_id: number;


    public get message(): string {
        return this._message;
    }

    public get error_id(): number {
        return this._error_id;
    }

}

export const UNKNOWN_ERROR_ID = 0;
export const NETWORK_ERROR_ID = 1;
export const USER_BLOCKED_ERROR_ID = 2;

export async function getAvailableDownloads(): Promise<Downloadable[] | ApiError> {
    try {
        const response = await fetch("/available-downloads");
        if (response.status === 200) {
            const responseBody: Downloadable[] = await response.json();
            return responseBody;
        } else if (response.status === 401) {
            return new ApiError("user blocked", USER_BLOCKED_ERROR_ID);
        } else {
            return new ApiError("unknown", UNKNOWN_ERROR_ID);
        }
    } catch (e: any) {
        return new ApiError(e.message, NETWORK_ERROR_ID);
    }
}