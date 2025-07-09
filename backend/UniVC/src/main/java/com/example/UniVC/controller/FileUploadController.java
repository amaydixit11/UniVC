// FileUploadController.java
package com.example.UniVC.controller;

import com.example.UniVC.dto.ApiResponse;
import com.example.UniVC.dto.FileInfoResponse;
import com.example.UniVC.service.FileProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/credentials")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FileUploadController {

    private final FileProcessingService fileProcessingService;

    @Autowired
    public FileUploadController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileInfoResponse>> uploadCredential(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "expectedFormat", required = false) String expectedFormat) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File is empty"));
            }

            // Check file size (10MB limit)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File size exceeds 10MB limit"));
            }

            // Process file
            FileInfoResponse fileInfo = fileProcessingService.processFile(file);

            // Add additional metadata if provided
            if (description != null && !description.trim().isEmpty()) {
                // You could store this in the response or database
            }

            return ResponseEntity.ok(ApiResponse.success("File processed successfully", fileInfo));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/formats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSupportedFormats() {
        Map<String, Object> formats = new HashMap<>();

        Map<String, String> supportedFormats = new HashMap<>();
        supportedFormats.put("SD-JWT", "Selective Disclosure JWT");
        supportedFormats.put("W3C-VC-1.1", "W3C Verifiable Credentials 1.1");
        supportedFormats.put("W3C-VC-2.0", "W3C Verifiable Credentials 2.0");
        supportedFormats.put("ISO-mDL", "ISO Mobile Driving License");
        supportedFormats.put("JWT", "JSON Web Token");
        supportedFormats.put("JSON", "Generic JSON");
        supportedFormats.put("CBOR", "Concise Binary Object Representation");

        formats.put("supported", supportedFormats);
        formats.put("maxFileSize", "10MB");
        formats.put("acceptedContentTypes", new String[]{
                "application/json",
                "text/plain",
                "application/jwt",
                "application/cbor"
        });

        return ResponseEntity.ok(ApiResponse.success("Supported formats", formats));
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("File upload endpoint is working"));
    }

    // Exception handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + e.getMessage()));
    }
}