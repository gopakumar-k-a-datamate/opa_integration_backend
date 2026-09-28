package com.datamate.bedrock.framework.storage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════
 * STORAGE OBJECT - Metadata for a file in storage
 * ═══════════════════════════════════════════════════════════════
 * 
 * This class represents a file stored in MinIO (or any other provider).
 * It contains all information ABOUT the file, but not the actual 
 * bytes of the file.
 * 
 * USAGE:
 * - Returned after an upload
 * - Returned when listing files
 * - Returned when getting metadata
 * 
 * ═══════════════════════════════════════════════════════════════
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════

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

    /**
     * Helper to add a single metadata entry
     * 
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
}
