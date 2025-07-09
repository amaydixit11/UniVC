package com.example.UniVC.service.detector;

import com.example.UniVC.dto.DetectionResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericJSONDetector implements FormatDetector {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DetectionResult detect(String content) {
        try {
            String trimmed = content.trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return DetectionResult.builder()
                        .format("Generic-JSON")
                        .confidence(0.0)
                        .build();
            }

            // Try to parse as JSON
            objectMapper.readTree(trimmed);

            return DetectionResult.builder()
                    .format("Generic-JSON")
                    .confidence(0.3)
                    .addMessage("Valid JSON structure, but no known format matched")
                    .addFormatInfo("structure", "JSON")
                    .build();

        } catch (Exception e) {
            return DetectionResult.builder()
                    .format("Generic-JSON")
                    .confidence(0.0)
                    .addMessage("Not a valid JSON: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getFormatName() {
        return "Generic-JSON";
    }

    @Override
    public int getPriority() {
        return 1; // Lowest among JSON-based formats
    }
}
