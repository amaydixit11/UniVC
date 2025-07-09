export interface FileStructure {
  rootType: string;
  totalFields: number;
  topLevelKeys: string[];
  isValid: boolean;
  encoding: string;
}

export interface FileInfoResponse {
  fileName: string;
  fileId: string;
  fileSize: number;
  contentType: string;
  detectedFormat: string;
  formatConfidence: number;
  structure: FileStructure;
  status: string;
  validationMessages: string[];
  processedAt: string;
}

export interface SupportedFormats {
  [key: string]: string;
}

export interface FormatInfo {
  supported: SupportedFormats;
  maxFileSize: string;
  acceptedContentTypes: string[];
}