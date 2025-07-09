// FileInfoResponse.java
package com.example.UniVC.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class FileInfoResponse {
    private String fileName;
    private String fileId;
    private long fileSize;
    private String contentType;
    private String detectedFormat;
    private double formatConfidence;
    private FileStructure structure;
    private String status;
    private String[] validationMessages;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    // Constructor
    public FileInfoResponse() {
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getDetectedFormat() { return detectedFormat; }
    public void setDetectedFormat(String detectedFormat) { this.detectedFormat = detectedFormat; }

    public double getFormatConfidence() { return formatConfidence; }
    public void setFormatConfidence(double formatConfidence) { this.formatConfidence = formatConfidence; }

    public FileStructure getStructure() { return structure; }
    public void setStructure(FileStructure structure) { this.structure = structure; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String[] getValidationMessages() { return validationMessages; }
    public void setValidationMessages(String[] validationMessages) { this.validationMessages = validationMessages; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    // Inner class for file structure
    public static class FileStructure {
        private String rootType; // "object", "array", "jwt", "cbor"
        private int totalFields;
        private String[] topLevelKeys;
        private boolean isValid;
        private String encoding;

        // Constructors
        public FileStructure() {}

        public FileStructure(String rootType, int totalFields, String[] topLevelKeys, boolean isValid, String encoding) {
            this.rootType = rootType;
            this.totalFields = totalFields;
            this.topLevelKeys = topLevelKeys;
            this.isValid = isValid;
            this.encoding = encoding;
        }

        // Getters and Setters
        public String getRootType() { return rootType; }
        public void setRootType(String rootType) { this.rootType = rootType; }

        public int getTotalFields() { return totalFields; }
        public void setTotalFields(int totalFields) { this.totalFields = totalFields; }

        public String[] getTopLevelKeys() { return topLevelKeys; }
        public void setTopLevelKeys(String[] topLevelKeys) { this.topLevelKeys = topLevelKeys; }

        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }

        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }
    }
}