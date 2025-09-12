import React from "react";
import type { FileUpload } from "pages/Home/components/UploadProgressDialog";


export const FilesUploadsContext = React.createContext<{
    filesUploadsList: FileUpload[],
    setFilesUploadsList: (files: FileUpload[]) => void,
}>({filesUploadsList: [], setFilesUploadsList: () => {}});