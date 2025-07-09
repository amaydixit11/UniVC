// FormatDetector.java
package com.example.UniVC.service.detector;

import com.example.UniVC.dto.DetectionResult;

public interface FormatDetector {
    /**
     * Detect the format of the given content
     * @param content The content to analyze
     * @return DetectionResult with format information and confidence score
     */
    DetectionResult detect(String content);

    /**
     * Get the format name that this detector handles
     * @return Format name
     */
    String getFormatName();

    /**
     * Get the priority of this detector (higher number = higher priority)
     * @return Priority level
     */
    default int getPriority() {
        return 0;
    }
}