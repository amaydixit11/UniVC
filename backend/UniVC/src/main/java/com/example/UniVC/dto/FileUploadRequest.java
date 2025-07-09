// FileUploadRequest.java
package com.example.UniVC.dto;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadRequest {
    private MultipartFile file;
    private String description;
    private String expectedFormat; // Optional hint from user

    // Constructors
    public FileUploadRequest() {}

    public FileUploadRequest(MultipartFile file, String description, String expectedFormat) {
        this.file = file;
        this.description = description;
        this.expectedFormat = expectedFormat;
    }

    // Getters and Setters
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpectedFormat() { return expectedFormat; }
    public void setExpectedFormat(String expectedFormat) { this.expectedFormat = expectedFormat; }
}

