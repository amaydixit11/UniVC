package com.example.UniVC.dto;

public enum CredentialFormat {
    SD_JWT("SD-JWT", "Selective Disclosure JWT"),
    W3C_VC_1_1("W3C-VC-1.1", "W3C Verifiable Credentials 1.1"),
    W3C_VC_2_0("W3C-VC-2.0", "W3C Verifiable Credentials 2.0"),
    ISO_MDL("ISO-mDL", "ISO Mobile Driving License"),
    UNKNOWN("UNKNOWN", "Unknown or unsupported format");

    private final String code;
    private final String description;

    CredentialFormat(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}