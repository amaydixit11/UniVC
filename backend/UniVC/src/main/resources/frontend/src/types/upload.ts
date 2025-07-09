import { FileInfoResponse, FormatInfo } from "./file";

// types/upload.ts
export interface UploadProgress {
  progress: number;
  status: 'idle' | 'uploading' | 'processing' | 'completed' | 'error';
  message?: string;
}

export interface FileUploadState {
  selectedFile: File | null;
  uploadProgress: UploadProgress;
  fileInfo: FileInfoResponse | null;
  error: string | null;
  supportedFormats: FormatInfo | null;
}