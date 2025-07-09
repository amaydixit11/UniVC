// SDJWTDetector.java
package com.example.UniVC.service.detector;

import com.example.UniVC.dto.DetectionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.regex.Pattern;

public class SDJWTDetector implements FormatDetector {

    private static final Pattern JWT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DetectionResult detect(String content) {
        String trimmedContent = content.trim();

        // Check if it starts with JWT pattern
        if (!JWT_PATTERN.matcher(trimmedContent).find()) {
            return DetectionResult.builder()
                    .format("SD-JWT")
                    .confidence(0.0)
                    .build();
        }

        // Check for SD-JWT specific indicators
        boolean hasDisclosures = trimmedContent.contains("~");
        boolean hasKeyBinding = trimmedContent.split("~").length > 1;

        double confidence = 0.0;
        DetectionResult.DetectionResultBuilder builder = DetectionResult.builder()
                .format("SD-JWT");

        if (hasDisclosures) {
            confidence += 0.4;
            builder.addMessage("Contains selective disclosure markers (~)");
        }

        // Try to parse JWT payload
        try {
            String[] parts = trimmedContent.split("\\.");
            if (parts.length >= 2) {
                String payload = parts[1];
                // Add padding if needed
                while (payload.length() % 4 != 0) {
                    payload += "=";
                }

                byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
                String decodedPayload = new String(decodedBytes);

                JsonNode payloadNode = objectMapper.readTree(decodedPayload);

                // Check for SD-JWT specific claims
                if (payloadNode.has("_sd")) {
                    confidence += 0.3;
                    builder.addMessage("Contains _sd claim (selective disclosure)");
                    builder.addDetail("selectiveDisclosureClaims", payloadNode.get("_sd").size());
                }

                if (payloadNode.has("_sd_alg")) {
                    confidence += 0.2;
                    builder.addMessage("Contains _sd_alg claim");
                    builder.addDetail("sdAlgorithm", payloadNode.get("_sd_alg").asText());
                }

                if (payloadNode.has("cnf")) {
                    confidence += 0.1;
                    builder.addMessage("Contains key binding (cnf claim)");
                    builder.addDetail("hasKeyBinding", true);
                }

                // Check for standard JWT claims
                if (payloadNode.has("iss")) {
                    confidence += 0.05;
                    builder.addDetail("issuer", payloadNode.get("iss").asText());
                }

                if (payloadNode.has("exp")) {
                    confidence += 0.05;
                    builder.addDetail("hasExpiration", true);
                }

                if (payloadNode.has("iat")) {
                    confidence += 0.05;
                    builder.addDetail("issuedAt", payloadNode.get("iat").asLong());
                }

                // Check for VC-specific claims in SD-JWT
                if (payloadNode.has("vct")) {
                    confidence += 0.2;
                    builder.addMessage("Contains verifiable credential type (vct)");
                    builder.addDetail("credentialType", payloadNode.get("vct").asText());
                    builder.version("SD-JWT-VC");
                }

                builder.addFormatInfo("jwtStructure", "header.payload.signature");
                builder.addFormatInfo("algorithm", getAlgorithmFromHeader(parts[0]));

                // Add required fields for SD-JWT
                builder.addRequiredField("iss")
                        .addRequiredField("exp")
                        .addRequiredField("iat")
                        .addRequiredField("_sd");

                // Add optional fields
                builder.addOptionalField("cnf")
                        .addOptionalField("_sd_alg")
                        .addOptionalField("vct")
                        .addOptionalField("sub");

            }
        } catch (Exception e) {
            builder.addMessage("Error parsing JWT payload: " + e.getMessage());
            confidence = Math.max(0.0, confidence - 0.3);
        }

        // Minimum confidence for SD-JWT
        if (hasDisclosures && confidence < 0.5) {
            confidence = 0.5;
        }

        return builder.confidence(confidence).build();
    }

    private String getAlgorithmFromHeader(String headerPart) {
        try {
            while (headerPart.length() % 4 != 0) {
                headerPart += "=";
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(headerPart);
            String decodedHeader = new String(decodedBytes);
            JsonNode headerNode = objectMapper.readTree(decodedHeader);
            return headerNode.has("alg") ? headerNode.get("alg").asText() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    public String getFormatName() {
        return "SD-JWT";
    }

    @Override
    public int getPriority() {
        return 10; // High priority for SD-JWT
    }
}