package com.datamate.bedrock.framework.storage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage Object - Domain Model
 * 
 * Represents metadata for a stored object in storage.
 */
@Getter
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class StorageObject {

    /**
     * Name of the bucket where the file is stored
     */
    private String bucketName;

    /**
     * The unique "path" or name of the file in the bucket
     * Example: "rest-123/menu.pdf"
     */
    private String objectKey;

    /**
     * Size of the file in bytes
     */
    private long size;

    /**
     * MIME type of the file (e.g., "application/pdf")
     */
    private String contentType;

    /**
     * When the file was last uploaded or modified
     */
    private LocalDateTime lastModified;

    /**
     * Unique hash of the file content (used for versioning/caching)
     */
    private String etag;

    /**
     * Custom metadata attached to the file
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Returns human-readable size
     * Example: 1024 -> "1 KB", 1048576 -> "1 MB"
     * 
     * @return Formatted string
     */
    public String getFormattedSize() {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]).replace(".0 ", " ");
    }

    /**
     * Extracts extension from objectKey
     * Example: "menu.pdf" -> "pdf"
     * 
     * @return extension string
     */
    public String getFileExtension() {
        if (objectKey == null || !objectKey.contains(".")) {
            return "";
        }
        return objectKey.substring(objectKey.lastIndexOf(".") + 1).toLowerCase();
    }
}
