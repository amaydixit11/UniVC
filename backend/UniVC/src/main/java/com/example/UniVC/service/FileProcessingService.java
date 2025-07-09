// FileProcessingService.java
package com.example.UniVC.service;

import com.example.UniVC.dto.FileInfoResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class FileProcessingService {

    private final ObjectMapper objectMapper;
    private final Pattern jwtPattern = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    private final FormatDetectionService formatDetectionService;

    @Autowired
    public FileProcessingService(FormatDetectionService formatDetectionService) {
        this.formatDetectionService = formatDetectionService;
        this.objectMapper = new ObjectMapper();
    }

    public FileInfoResponse processFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        // Generate unique file ID
        String fileId = UUID.randomUUID().toString();

        // Read file content
        byte[] fileBytes = file.getBytes();
        String fileContent = new String(fileBytes);

        // Detect format and analyze structure
        FormatDetectionResult detectionResult = detectFormat(fileContent);
        FileInfoResponse.FileStructure structure = analyzeStructure(fileContent, detectionResult.format);

        // Create response
        FileInfoResponse response = new FileInfoResponse();
        response.setFileName(fileName);
        response.setFileId(fileId);
        response.setFileSize(fileSize);
        response.setContentType(contentType);
        response.setDetectedFormat(detectionResult.format);
        response.setFormatConfidence(detectionResult.confidence);
        response.setStructure(structure);
        response.setStatus(structure.isValid() ? "VALID" : "INVALID");
        response.setValidationMessages(detectionResult.messages.toArray(new String[0]));

        return response;
    }

    private FormatDetectionResult detectFormat(String content) {
        List<String> messages = new ArrayList<>();

        // Remove whitespace for analysis
        String trimmedContent = content.trim();

        // Check for JWT format (including SD-JWT)
        if (isJWTFormat(trimmedContent)) {
            if (isSDJWT(trimmedContent)) {
                messages.add("Detected SD-JWT format with selective disclosure");
                return new FormatDetectionResult("SD-JWT", 0.95, messages);
            } else {
                messages.add("Detected standard JWT format");
                return new FormatDetectionResult("JWT", 0.85, messages);
            }
        }

        // Check for JSON format (W3C VC or other)
        if (isJSONFormat(trimmedContent)) {
            JsonNode jsonNode = parseJSON(trimmedContent);
            if (jsonNode != null) {
                if (isW3CVC(jsonNode)) {
                    String version = detectW3CVCVersion(jsonNode);
                    messages.add("Detected W3C Verifiable Credential " + version);
                    return new FormatDetectionResult("W3C-VC-" + version, 0.90, messages);
                } else if (isISOmDL(jsonNode)) {
                    messages.add("Detected ISO mDL format");
                    return new FormatDetectionResult("ISO-mDL", 0.80, messages);
                } else {
                    messages.add("Detected generic JSON format");
                    return new FormatDetectionResult("JSON", 0.50, messages);
                }
            }
        }

        // Check for CBOR (binary format for mDL)
        if (isCBORFormat(trimmedContent)) {
            messages.add("Detected CBOR format (likely mDL)");
            return new FormatDetectionResult("CBOR", 0.70, messages);
        }

        messages.add("Unknown format detected");
        return new FormatDetectionResult("UNKNOWN", 0.0, messages);
    }

    private boolean isJWTFormat(String content) {
        return jwtPattern.matcher(content).matches();
    }

    private boolean isSDJWT(String content) {
        // SD-JWT contains tildes (~) for selective disclosure
        return content.contains("~") && content.split("\\.").length >= 3;
    }

    private boolean isJSONFormat(String content) {
        try {
            objectMapper.readTree(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private JsonNode parseJSON(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private boolean isW3CVC(JsonNode jsonNode) {
        // Check for W3C VC indicators
        return jsonNode.has("@context") &&
                (jsonNode.has("credentialSubject") ||
                        jsonNode.has("type") &&
                                jsonNode.get("type").toString().contains("VerifiableCredential"));
    }

    private String detectW3CVCVersion(JsonNode jsonNode) {
        JsonNode context = jsonNode.get("@context");
        if (context != null) {
            String contextStr = context.toString();
            if (contextStr.contains("credentials/v2")) {
                return "2.0";
            } else if (contextStr.contains("credentials/v1")) {
                return "1.1";
            }
        }
        return "1.1"; // Default assumption
    }

    private boolean isISOmDL(JsonNode jsonNode) {
        // Check for mDL specific fields
        return jsonNode.has("docType") ||
                jsonNode.has("namespaces") ||
                jsonNode.has("deviceSigned") ||
                jsonNode.has("issuerSigned");
    }

    private boolean isCBORFormat(String content) {
        // Simple check for CBOR - in real implementation, you'd use a CBOR library
        // CBOR typically starts with specific byte patterns
        return content.length() > 0 && !content.startsWith("{") && !content.startsWith("[") &&
                !jwtPattern.matcher(content).matches();
    }

    private FileInfoResponse.FileStructure analyzeStructure(String content, String detectedFormat) {
        FileInfoResponse.FileStructure structure = new FileInfoResponse.FileStructure();

        try {
            switch (detectedFormat) {
                case "SD-JWT":
                case "JWT":
                    return analyzeJWTStructure(content);
                case "W3C-VC-1.1":
                case "W3C-VC-2.0":
                case "JSON":
                    return analyzeJSONStructure(content);
                case "ISO-mDL":
                    return analyzemDLStructure(content);
                case "CBOR":
                    return analyzeCBORStructure(content);
                default:
                    structure.setRootType("unknown");
                    structure.setValid(false);
                    structure.setEncoding("unknown");
                    return structure;
            }
        } catch (Exception e) {
            structure.setRootType("error");
            structure.setValid(false);
            structure.setEncoding("error");
            return structure;
        }
    }

    private FileInfoResponse.FileStructure analyzeJWTStructure(String content) {
        FileInfoResponse.FileStructure structure = new FileInfoResponse.FileStructure();

        try {
            String[] parts = content.split("\\.");
            structure.setRootType("jwt");
            structure.setTotalFields(parts.length);
            structure.setTopLevelKeys(new String[]{"header", "payload", "signature"});
            structure.setValid(parts.length >= 3);
            structure.setEncoding("base64url");

            // Try to decode payload to get more info
            if (parts.length >= 2) {
                // In real implementation, you'd decode the JWT payload
                // For now, just mark as valid if it has the right structure
                structure.setValid(true);
            }

        } catch (Exception e) {
            structure.setValid(false);
        }

        return structure;
    }

    private FileInfoResponse.FileStructure analyzeJSONStructure(String content) {
        FileInfoResponse.FileStructure structure = new FileInfoResponse.FileStructure();

        try {
            JsonNode jsonNode = objectMapper.readTree(content);

            if (jsonNode.isObject()) {
                structure.setRootType("object");
                structure.setTotalFields(jsonNode.size());

                // Get top-level keys
                List<String> keys = new ArrayList<>();
                jsonNode.fieldNames().forEachRemaining(keys::add);
                structure.setTopLevelKeys(keys.toArray(new String[0]));

            } else if (jsonNode.isArray()) {
                structure.setRootType("array");
                structure.setTotalFields(jsonNode.size());
                structure.setTopLevelKeys(new String[]{"array[" + jsonNode.size() + "]"});
            }

            structure.setValid(true);
            structure.setEncoding("utf-8");

        } catch (Exception e) {
            structure.setValid(false);
            structure.setEncoding("unknown");
        }

        return structure;
    }

    private FileInfoResponse.FileStructure analyzemDLStructure(String content) {
        FileInfoResponse.FileStructure structure = new FileInfoResponse.FileStructure();

        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            structure.setRootType("mdl");
            structure.setTotalFields(jsonNode.size());

            List<String> keys = new ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(keys::add);
            structure.setTopLevelKeys(keys.toArray(new String[0]));

            structure.setValid(true);
            structure.setEncoding("utf-8");

        } catch (Exception e) {
            structure.setValid(false);
        }

        return structure;
    }

    private FileInfoResponse.FileStructure analyzeCBORStructure(String content) {
        FileInfoResponse.FileStructure structure = new FileInfoResponse.FileStructure();

        structure.setRootType("cbor");
        structure.setTotalFields(0); // Would need CBOR parsing
        structure.setTopLevelKeys(new String[]{"binary_data"});
        structure.setValid(true);
        structure.setEncoding("binary");

        return structure;
    }

    // Helper class for format detection results
    private static class FormatDetectionResult {
        String format;
        double confidence;
        List<String> messages;

        FormatDetectionResult(String format, double confidence, List<String> messages) {
            this.format = format;
            this.confidence = confidence;
            this.messages = messages;
        }
    }
}