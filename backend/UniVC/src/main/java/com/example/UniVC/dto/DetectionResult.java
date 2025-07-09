// DetectionResult.java
package com.example.UniVC.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionResult {
    private String format;
    private double confidence;
    private List<String> messages;
    private Map<String, Object> details;
    private String version;
    private List<String> requiredFields;
    private List<String> optionalFields;
    private Map<String, String> formatSpecificInfo;

    public DetectionResult() {
        this.messages = new ArrayList<>();
        this.details = new HashMap<>();
        this.requiredFields = new ArrayList<>();
        this.optionalFields = new ArrayList<>();
        this.formatSpecificInfo = new HashMap<>();
    }

    // Builder pattern for easy construction
    public static DetectionResultBuilder builder() {
        return new DetectionResultBuilder();
    }

    public static class DetectionResultBuilder {
        private DetectionResult result;

        public DetectionResultBuilder() {
            this.result = new DetectionResult();
        }

        public DetectionResultBuilder format(String format) {
            result.format = format;
            return this;
        }

        public DetectionResultBuilder confidence(double confidence) {
            result.confidence = confidence;
            return this;
        }

        public DetectionResultBuilder version(String version) {
            result.version = version;
            return this;
        }

        public DetectionResultBuilder addMessage(String message) {
            result.messages.add(message);
            return this;
        }

        public DetectionResultBuilder addDetail(String key, Object value) {
            result.details.put(key, value);
            return this;
        }

        public DetectionResultBuilder addRequiredField(String field) {
            result.requiredFields.add(field);
            return this;
        }

        public DetectionResultBuilder addOptionalField(String field) {
            result.optionalFields.add(field);
            return this;
        }

        public DetectionResultBuilder addFormatInfo(String key, String value) {
            result.formatSpecificInfo.put(key, value);
            return this;
        }

        public DetectionResult build() {
            return result;
        }
    }

    // Getters and setters
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public List<String> getRequiredFields() { return requiredFields; }
    public void setRequiredFields(List<String> requiredFields) { this.requiredFields = requiredFields; }

    public List<String> getOptionalFields() { return optionalFields; }
    public void setOptionalFields(List<String> optionalFields) { this.optionalFields = optionalFields; }

    public Map<String, String> getFormatSpecificInfo() { return formatSpecificInfo; }
    public void setFormatSpecificInfo(Map<String, String> formatSpecificInfo) { this.formatSpecificInfo = formatSpecificInfo; }

    public String getConfidenceLevel() {
        if (confidence >= 0.9) return "HIGH";
        if (confidence >= 0.7) return "MEDIUM";
        if (confidence >= 0.5) return "LOW";
        return "VERY_LOW";
    }
}