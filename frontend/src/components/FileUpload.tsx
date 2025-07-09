"use client";

import { useState, useRef, useEffect } from "react";
import { apiClient } from "@/lib/api";
import { FileInfoResponse, FormatInfo } from "@/types/file";
import {
  Upload,
  File,
  CheckCircle,
  XCircle,
  AlertCircle,
  FileText,
  Download,
  Trash2,
  Info,
} from "lucide-react";
import { FileUploadState } from "@/types/upload";
import { ApiResponse } from "@/types/api";

export default function FileUploadComponent() {
  const [uploadState, setUploadState] = useState<FileUploadState>({
    selectedFile: null,
    uploadProgress: { progress: 0, status: "idle" },
    fileInfo: null,
    error: null,
    supportedFormats: null,
  });

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [dragActive, setDragActive] = useState(false);

  useEffect(() => {
    loadSupportedFormats();
  }, []);

  const loadSupportedFormats = async () => {
    try {
      const response = await apiClient.get<ApiResponse<FormatInfo>>(
        "/api/v1/credentials/formats"
      );
      setUploadState((prev) => ({
        ...prev,
        supportedFormats: response.data.data,
      }));
    } catch (error) {
      console.error("Failed to load supported formats:", error);
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      handleFileSelect(file);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      handleFileSelect(file);
    }
  };

  const handleFileSelect = (file: File) => {
    // Validate file size (10MB)
    if (file.size > 10 * 1024 * 1024) {
      setUploadState((prev) => ({
        ...prev,
        error: "File size exceeds 10MB limit",
        selectedFile: null,
      }));
      return;
    }

    setUploadState((prev) => ({
      ...prev,
      selectedFile: file,
      error: null,
      fileInfo: null,
      uploadProgress: { progress: 0, status: "idle" },
    }));
  };

  const uploadFile = async () => {
    if (!uploadState.selectedFile) return;

    const formData = new FormData();
    formData.append("file", uploadState.selectedFile);
    formData.append("description", "File uploaded via web interface");

    try {
      setUploadState((prev) => ({
        ...prev,
        uploadProgress: { progress: 0, status: "uploading" },
        error: null,
      }));

      // Simulate upload progress
      const progressInterval = setInterval(() => {
        setUploadState((prev) => ({
          ...prev,
          uploadProgress: {
            ...prev.uploadProgress,
            progress: Math.min(prev.uploadProgress.progress + 10, 90),
          },
        }));
      }, 100);

      const response = await apiClient.post<ApiResponse<FileInfoResponse>>(
        "/api/v1/credentials/upload",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      clearInterval(progressInterval);

      setUploadState((prev) => ({
        ...prev,
        uploadProgress: { progress: 100, status: "completed" },
        fileInfo: response.data.data,
      }));
    } catch (error: any) {
      setUploadState((prev) => ({
        ...prev,
        uploadProgress: { progress: 0, status: "error" },
        error: error.response?.data?.message || "Failed to upload file",
      }));
    }
  };

  const clearFile = () => {
    setUploadState((prev) => ({
      ...prev,
      selectedFile: null,
      fileInfo: null,
      error: null,
      uploadProgress: { progress: 0, status: "idle" },
    }));
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  const getFormatBadgeColor = (format: string, confidence: number) => {
    if (confidence >= 0.9) return "bg-green-100 text-green-800";
    if (confidence >= 0.7) return "bg-yellow-100 text-yellow-800";
    return "bg-red-100 text-red-800";
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "VALID":
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case "INVALID":
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <AlertCircle className="w-5 h-5 text-yellow-500" />;
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* Upload Area */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-6">
          Upload Verifiable Credential
        </h2>

        {/* Drag and Drop Area */}
        <div
          className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
            dragActive
              ? "border-blue-500 bg-blue-50"
              : "border-gray-300 hover:border-gray-400"
          }`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          <Upload className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          <p className="text-lg font-medium text-gray-700 mb-2">
            Drop your credential file here, or click to browse
          </p>
          <p className="text-sm text-gray-500 mb-4">
            Supports SD-JWT, W3C VC, ISO mDL, and other formats (Max 10MB)
          </p>

          <input
            ref={fileInputRef}
            type="file"
            onChange={handleFileInput}
            className="hidden"
            accept=".json,.jwt,.txt,.cbor"
          />

          <button
            onClick={() => fileInputRef.current?.click()}
            className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 transition-colors"
          >
            Browse Files
          </button>
        </div>

        {/* Selected File Info */}
        {uploadState.selectedFile && (
          <div className="mt-6 p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <File className="w-8 h-8 text-blue-500" />
                <div>
                  <p className="font-medium">{uploadState.selectedFile.name}</p>
                  <p className="text-sm text-gray-500">
                    {formatFileSize(uploadState.selectedFile.size)}
                  </p>
                </div>
              </div>
              <div className="flex space-x-2">
                <button
                  onClick={uploadFile}
                  disabled={uploadState.uploadProgress.status === "uploading"}
                  className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 disabled:opacity-50"
                >
                  {uploadState.uploadProgress.status === "uploading"
                    ? "Processing..."
                    : "Process File"}
                </button>
                <button
                  onClick={clearFile}
                  className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Upload Progress */}
            {uploadState.uploadProgress.status === "uploading" && (
              <div className="mt-4">
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${uploadState.uploadProgress.progress}%` }}
                  />
                </div>
                <p className="text-sm text-gray-600 mt-2">
                  Processing... {uploadState.uploadProgress.progress}%
                </p>
              </div>
            )}
          </div>
        )}

        {/* Error Display */}
        {uploadState.error && (
          <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
            <div className="flex items-center space-x-2">
              <XCircle className="w-5 h-5 text-red-500" />
              <span className="text-red-700">{uploadState.error}</span>
            </div>
          </div>
        )}
      </div>

      {/* File Analysis Results */}
      {uploadState.fileInfo && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h3 className="text-xl font-bold mb-4">Analysis Results</h3>

          {/* Basic Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div>
              <h4 className="font-semibold mb-2">File Information</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">File Name:</span>
                  <span className="font-medium">
                    {uploadState.fileInfo.fileName}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">File Size:</span>
                  <span className="font-medium">
                    {formatFileSize(uploadState.fileInfo.fileSize)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Content Type:</span>
                  <span className="font-medium">
                    {uploadState.fileInfo.contentType}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Processed:</span>
                  <span className="font-medium">
                    {new Date(
                      uploadState.fileInfo.processedAt
                    ).toLocaleString()}
                  </span>
                </div>
              </div>
            </div>

            <div>
              <h4 className="font-semibold mb-2">Format Detection</h4>
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Detected Format:</span>
                  <span
                    className={`px-2 py-1 rounded text-xs font-medium ${getFormatBadgeColor(
                      uploadState.fileInfo.detectedFormat,
                      uploadState.fileInfo.formatConfidence
                    )}`}
                  >
                    {uploadState.fileInfo.detectedFormat}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Confidence:</span>
                  <span className="font-medium">
                    {(uploadState.fileInfo.formatConfidence * 100).toFixed(1)}%
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Status:</span>
                  <div className="flex items-center space-x-1">
                    {getStatusIcon(uploadState.fileInfo.status)}
                    <span className="font-medium">
                      {uploadState.fileInfo.status}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Structure Analysis */}
          <div className="mb-6">
            <h4 className="font-semibold mb-2">Structure Analysis</h4>
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div>
                  <span className="text-gray-600">Root Type:</span>
                  <p className="font-medium">
                    {uploadState.fileInfo.structure.rootType}
                  </p>
                </div>
                <div>
                  <span className="text-gray-600">Total Fields:</span>
                  <p className="font-medium">
                    {uploadState.fileInfo.structure.totalFields}
                  </p>
                </div>
                <div>
                  <span className="text-gray-600">Encoding:</span>
                  <p className="font-medium">
                    {uploadState.fileInfo.structure.encoding}
                  </p>
                </div>
                <div>
                  <span className="text-gray-600">Valid:</span>
                  <p className="font-medium">
                    {uploadState.fileInfo.structure.isValid ? "Yes" : "No"}
                  </p>
                </div>
              </div>

              {uploadState.fileInfo.structure.topLevelKeys.length > 0 && (
                <div className="mt-4">
                  <span className="text-gray-600 text-sm">Top Level Keys:</span>
                  <div className="flex flex-wrap gap-2 mt-2">
                    {uploadState.fileInfo.structure.topLevelKeys.map(
                      (key, index) => (
                        <span
                          key={index}
                          className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs"
                        >
                          {key}
                        </span>
                      )
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Validation Messages */}
          {uploadState.fileInfo.validationMessages.length > 0 && (
            <div>
              <h4 className="font-semibold mb-2">Validation Messages</h4>
              <div className="space-y-2">
                {uploadState.fileInfo.validationMessages.map(
                  (message, index) => (
                    <div
                      key={index}
                      className="flex items-start space-x-2 p-2 bg-blue-50 rounded"
                    >
                      <Info className="w-4 h-4 text-blue-500 mt-0.5" />
                      <span className="text-sm text-blue-700">{message}</span>
                    </div>
                  )
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Supported Formats Info */}
      {uploadState.supportedFormats && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h3 className="text-xl font-bold mb-4">Supported Formats</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {Object.entries(uploadState.supportedFormats.supported).map(
              ([format, description]) => (
                <div key={format} className="p-3 border rounded-lg">
                  <div className="font-medium text-sm">{format}</div>
                  <div className="text-xs text-gray-600 mt-1">
                    {description}
                  </div>
                </div>
              )
            )}
          </div>
          <div className="mt-4 text-sm text-gray-600">
            <p>Maximum file size: {uploadState.supportedFormats.maxFileSize}</p>
            <p>
              Accepted content types:{" "}
              {uploadState.supportedFormats.acceptedContentTypes.join(", ")}
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
