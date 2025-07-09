package com.example.UniVC.service.detector;

import com.example.UniVC.dto.DetectionResult;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

import java.util.Base64;

public class CBORDetector implements FormatDetector {

    private static final CBORMapper cborMapper = new CBORMapper();

    @Override
    public DetectionResult detect(String content) {
        DetectionResult.DetectionResultBuilder builder = DetectionResult.builder()
                .format("CBOR");

        double confidence = 0.0;

        try {
            byte[] decoded;

            // Try base64 decode first
            try {
                decoded = Base64.getDecoder().decode(content.trim());
                confidence += 0.2;
                builder.addMessage("Input is valid Base64 - assumed CBOR encoding");
            } catch (IllegalArgumentException e) {
                return builder.confidence(0.0).build(); // Not Base64
            }

            // Try parsing as CBOR
            Object data = cborMapper.readTree(decoded);
            confidence += 0.6;
            builder.addMessage("Parsed CBOR successfully");
            builder.addFormatInfo("structure", "CBOR (binary)");
            builder.addFormatInfo("specification", "RFC 8949");

            return builder.confidence(confidence).build();

        } catch (Exception e) {
            return builder
                    .addMessage("Failed to parse CBOR: " + e.getMessage())
                    .confidence(0.0)
                    .build();
        }
    }

    @Override
    public String getFormatName() {
        return "CBOR";
    }

    @Override
    public int getPriority() {
        return 6; // Below ISO, above generic JSON
    }
}
