package com.datamate.bedrock.framework.storage.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upload Request - Application Layer DTO
 */
@Getter
@Builder
@AllArgsConstructor
@ToString(exclude = "inputStream")
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
}
