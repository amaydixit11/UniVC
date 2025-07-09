package com.example.UniVC.service.detector;

import com.example.UniVC.dto.DetectionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ISOmDLDetector implements FormatDetector {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DetectionResult detect(String content) {
        DetectionResult.DetectionResultBuilder builder = DetectionResult.builder()
                .format("ISO-mDL");

        double confidence = 0.0;

        try {
            String trimmedContent = content.trim();

            if (!trimmedContent.startsWith("{") && !trimmedContent.startsWith("[")) {
                return builder.confidence(0.0).build();
            }

            JsonNode root = objectMapper.readTree(trimmedContent);

            // Check for ISO-mDL specific indicators
            if (root.has("@context")) {
                String contextStr = root.get("@context").toString();
                if (contextStr.contains("18013") || contextStr.contains("mdoc")) {
                    confidence += 0.3;
                    builder.addMessage("Detected ISO mDL context");
                    builder.addDetail("context", contextStr);
                    builder.version("ISO/IEC 18013");
                }
            }

            if (root.has("type")) {
                String typeStr = root.get("type").toString();
                if (typeStr.contains("mDL") || typeStr.contains("DrivingLicense")) {
                    confidence += 0.2;
                    builder.addMessage("Detected mDL type");
                    builder.addDetail("type", typeStr);
                }
            }

            if (root.has("driving_privileges")) {
                confidence += 0.3;
                builder.addMessage("Contains driving_privileges field");
                builder.addDetail("hasDrivingPrivileges", true);
            }

            if (root.has("document_number")) {
                confidence += 0.1;
                builder.addMessage("Contains document_number field");
            }

            if (root.has("birth_date")) {
                confidence += 0.1;
                builder.addMessage("Contains birth_date field");
            }

            if (root.has("issuing_authority")) {
                confidence += 0.1;
                builder.addMessage("Contains issuing_authority field");
            }

            // Optional fields
            builder.addOptionalField("document_number")
                    .addOptionalField("birth_date")
                    .addOptionalField("issuing_authority")
                    .addOptionalField("expiry_date")
                    .addOptionalField("issuing_country")
                    .addOptionalField("portrait");

            // Required fields
            builder.addRequiredField("type")
                    .addRequiredField("driving_privileges");

            builder.addFormatInfo("structure", "JSON or CBOR wrapped in JSON")
                    .addFormatInfo("specification", "ISO/IEC 18013 mDL");

            // Minimum confidence if key fields exist
            if (root.has("type") && root.has("driving_privileges")) {
                confidence = Math.max(confidence, 0.75);
            }

            return builder.confidence(confidence).build();

        } catch (Exception e) {
            return builder
                    .addMessage("Error parsing ISO-mDL content: " + e.getMessage())
                    .confidence(0.0)
                    .build();
        }
    }

    @Override
    public String getFormatName() {
        return "ISO-mDL";
    }

    @Override
    public int getPriority() {
        return 7; // Slightly lower than W3C-VC/SD-JWT
    }
}
