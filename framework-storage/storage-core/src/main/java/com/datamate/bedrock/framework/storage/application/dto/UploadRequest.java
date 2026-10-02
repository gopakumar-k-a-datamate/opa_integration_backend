package com.datamate.bedrock.framework.storage.application.dto;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Backward-compatible DTO alias for {@link UploadCommand}.
 * @deprecated Use {@link UploadCommand} to strictly adhere to CQRS naming conventions.
 */
@Deprecated
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class UploadRequest extends UploadCommand {
    public UploadRequest(String bucketName, String objectKey, InputStream inputStream, long size, String contentType,
                         Map<String, String> metadata, List<String> allowedExtensions, Long maxSizeBytes) {
        super(bucketName, objectKey, inputStream, size, contentType, metadata, allowedExtensions, maxSizeBytes);
    }
}