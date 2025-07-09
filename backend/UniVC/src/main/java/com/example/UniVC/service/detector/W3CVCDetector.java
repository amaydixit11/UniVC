// W3CVCDetector.java
package com.example.UniVC.service.detector;

import com.example.UniVC.dto.DetectionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class W3CVCDetector implements FormatDetector {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DetectionResult detect(String content) {
        try {
            String trimmedContent = content.trim();

            // Must be valid JSON
            if (!trimmedContent.startsWith("{") && !trimmedContent.startsWith("[")) {
                return DetectionResult.builder()
                        .format("W3C-VC")
                        .confidence(0.0)
                        .build();
            }

            JsonNode jsonNode = objectMapper.readTree(trimmedContent);

            double confidence = 0.0;
            DetectionResult.DetectionResultBuilder builder = DetectionResult.builder()
                    .format("W3C-VC");

            // Check for required W3C VC fields
            if (jsonNode.has("@context")) {
                confidence += 0.3;
                builder.addMessage("Contains @context field");

                JsonNode context = jsonNode.get("@context");
                String contextStr = context.toString();

                // Detect version based on context
                if (contextStr.contains("credentials/v2")) {
                    confidence += 0.2;
                    builder.version("2.0");
                    builder.addMessage("Detected W3C VC 2.0 context");
                } else if (contextStr.contains("credentials/v1")) {
                    confidence += 0.2;
                    builder.version("1.1");
                    builder.addMessage("Detected W3C VC 1.1 context");
                } else if (contextStr.contains("credentials")) {
                    confidence += 0.1;
                    builder.addMessage("Contains credentials context");
                }

                builder.addDetail("context", context);
            }

            // Check for type field
            if (jsonNode.has("type")) {
                JsonNode typeNode = jsonNode.get("type");
                String typeStr = typeNode.toString();

                if (typeStr.contains("VerifiableCredential")) {
                    confidence += 0.3;
                    builder.addMessage("Contains VerifiableCredential type");
                    builder.addDetail("types", typeNode);
                }

                // Check for specific credential types
                if (typeStr.contains("UniversityDegree") ||
                        typeStr.contains("DriverLicense") ||
                        typeStr.contains("PermanentResident")) {
                    confidence += 0.1;
                    builder.addMessage("Contains specific credential type");
                }
            }

            // Check for credentialSubject
            if (jsonNode.has("credentialSubject")) {
                confidence += 0.2;
                builder.addMessage("Contains credentialSubject");
                builder.addDetail("hasCredentialSubject", true);

                JsonNode subject = jsonNode.get("credentialSubject");
                if (subject.has("id")) {
                    confidence += 0.05;
                    builder.addDetail("subjectHasId", true);
                }
            }

            // Check for issuer
            if (jsonNode.has("issuer")) {
                confidence += 0.15;
                builder.addMessage("Contains issuer field");

                JsonNode issuer = jsonNode.get("issuer");
                if (issuer.isTextual()) {
                    builder.addDetail("issuerType", "string");
                    builder.addDetail("issuer", issuer.asText());
                } else if (issuer.isObject()) {
                    builder.addDetail("issuerType", "object");
                    if (issuer.has("id")) {
                        builder.addDetail("issuer", issuer.get("id").asText());
                    }
                }
            }

            // Check for issuanceDate
            if (jsonNode.has("issuanceDate")) {
                confidence += 0.1;
                builder.addMessage("Contains issuanceDate");
                builder.addDetail("issuanceDate", jsonNode.get("issuanceDate").asText());
            }

            // Check for expirationDate
            if (jsonNode.has("expirationDate")) {
                confidence += 0.05;
                builder.addMessage("Contains expirationDate");
                builder.addDetail("expirationDate", jsonNode.get("expirationDate").asText());
            }

            // Check for proof
            if (jsonNode.has("proof")) {
                confidence += 0.2;
                builder.addMessage("Contains cryptographic proof");

                JsonNode proof = jsonNode.get("proof");
                if (proof.has("type")) {
                    String proofType = proof.get("type").asText();
                    builder.addDetail("proofType", proofType);

                    // Different proof types
                    if (proofType.contains("Ed25519Signature") ||
                            proofType.contains("RsaSignature") ||
                            proofType.contains("EcdsaSecp256k1Signature")) {
                        confidence += 0.1;
                        builder.addMessage("Contains recognized proof type: " + proofType);
                    }
                }

                if (proof.has("proofPurpose")) {
                    builder.addDetail("proofPurpose", proof.get("proofPurpose").asText());
                }
            }

            // Check for credentialStatus (revocation)
            if (jsonNode.has("credentialStatus")) {
                confidence += 0.1;
                builder.addMessage("Contains credentialStatus");
                builder.addDetail("hasRevocationInfo", true);
            }

            // Check for evidence
            if (jsonNode.has("evidence")) {
                confidence += 0.05;
                builder.addMessage("Contains evidence");
            }

            // Check for refreshService
            if (jsonNode.has("refreshService")) {
                confidence += 0.05;
                builder.addMessage("Contains refreshService");
            }

            // Add format-specific information
            builder.addFormatInfo("structure", "JSON-LD")
                    .addFormatInfo("specification", "W3C Verifiable Credentials")
                    .addFormatInfo("dataModel", jsonNode.has("@context") ? "JSON-LD" : "JSON");

            // Add required fields
            builder.addRequiredField("@context")
                    .addRequiredField("type")
                    .addRequiredField("credentialSubject")
                    .addRequiredField("issuer");

            // Add optional fields
            builder.addOptionalField("id")
                    .addOptionalField("issuanceDate")
                    .addOptionalField("expirationDate")
                    .addOptionalField("proof")
                    .addOptionalField("credentialStatus")
                    .addOptionalField("evidence")
                    .addOptionalField("refreshService");

            // Minimum confidence check
            if (jsonNode.has("@context") && jsonNode.has("type") &&
                    jsonNode.get("type").toString().contains("VerifiableCredential")) {
                confidence = Math.max(confidence, 0.8);
            }

            return builder.confidence(confidence).build();

        } catch (Exception e) {
            return DetectionResult.builder()
                    .format("W3C-VC")
                    .confidence(0.0)
                    .addMessage("Error parsing JSON: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String getFormatName() {
        return "W3C-VC";
    }

    @Override
    public int getPriority() {
        return 8; // High priority for W3C VC
    }
}