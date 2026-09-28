package com.datamate.bedrock.framework.storage.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════
 * UPLOAD REQUEST - DTO
 * ═══════════════════════════════════════════════════════════════
 * 
 * Part of the Application Layer (DTO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {

    @NotBlank(message = "Bucket name is required")
    private String bucketName;

    @NotBlank(message = "Object key is required")
    private String objectKey;

    @NotNull(message = "Input stream is required")
    private InputStream inputStream;

    @Positive(message = "File size must be greater than 0")
    private long size;

    @NotBlank(message = "Content type is required")
    private String contentType;

    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Builder.Default
    private List<String> allowedExtensions = new ArrayList<>();

    private Long maxSizeBytes;

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public void addAllowedExtension(String extension) {
        if (this.allowedExtensions == null) {
            this.allowedExtensions = new ArrayList<>();
        }
        this.allowedExtensions.add(extension.toLowerCase());
    }

    public String getFileExtension() {
        if (objectKey == null || !objectKey.contains(".")) {
            return "";
        }
        return objectKey.substring(objectKey.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean isExtensionAllowed() {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true;
        }
        String extension = getFileExtension();
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public boolean isSizeAllowed() {
        if (maxSizeBytes == null || maxSizeBytes == 0) {
            return true;
        }
        return size <= maxSizeBytes;
    }
}
