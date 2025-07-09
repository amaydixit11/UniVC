// FormatDetectionService.java
package com.example.UniVC.service;

import com.example.UniVC.dto.DetectionResult;
import com.example.UniVC.service.detector.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FormatDetectionService {

    private final List<FormatDetector> detectors;

    public FormatDetectionService() {
        this.detectors = new ArrayList<>();
        // Order matters - more specific detectors first
        this.detectors.add(new SDJWTDetector());
        this.detectors.add(new W3CVCDetector());
        this.detectors.add(new ISOmDLDetector());
        this.detectors.add(new CBORDetector());
        this.detectors.add(new GenericJSONDetector()); // Fallback for JSON
    }

    public DetectionResult detectFormat(String content) {
        List<DetectionResult> results = new ArrayList<>();

        // Run all detectors and collect results
        for (FormatDetector detector : detectors) {
            try {
                DetectionResult result = detector.detect(content);
                if (result.getConfidence() > 0.0) {
                    results.add(result);
                }
            } catch (Exception e) {
                // Log error but continue with other detectors
                System.err.println("Error in detector " + detector.getFormatName() + ": " + e.getMessage());
            }
        }

        // Return the result with highest confidence
        Optional<DetectionResult> bestResult = results.stream()
                .max((r1, r2) -> Double.compare(r1.getPriority(), r2.getPriority()));

        if (bestResult.isPresent()) {
            return bestResult.get();
        }

        // Return unknown format if no detector matched
        return DetectionResult.builder()
                .format("UNKNOWN")
                .confidence(0.0)
                .addMessage("No matching format detected")
                .addDetail("contentLength", String.valueOf(content.length()))
                .build();
    }

    public List<DetectionResult> detectAllFormats(String content) {
        List<DetectionResult> results = new ArrayList<>();

        for (FormatDetector detector : detectors) {
            try {
                DetectionResult result = detector.detect(content);
                if (result.getConfidence() > 0.0) {
                    results.add(result);
                }
            } catch (Exception e) {
                System.err.println("Error in detector " + detector.getFormatName() + ": " + e.getMessage());
            }
        }

        return results;
    }
}