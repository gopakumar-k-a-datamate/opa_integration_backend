package com.datamate.bedrock.framework.storage.application.port;

import com.datamate.bedrock.framework.storage.application.dto.UploadRequest;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;

import java.io.InputStream;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════
 * STORAGE PROVIDER - Outbound Port
 * ═══════════════════════════════════════════════════════════════
 * 
 * This interface defines the technical capabilities for a storage
 * backend (MinIO, S3, etc.). It is the "Muscles" of the system.
 */
public interface StorageProvider {

    /**
     * Raw upload to storage provider.
     */
    StorageObject upload(
            String bucketName,
            String objectKey,
            InputStream inputStream,
            String contentType,
            long size);

    /**
     * Upload multiple files.
     */
    List<StorageObject> uploadMultiple(List<UploadRequest> requests);

    /**
     * Raw download from storage provider.
     */
    InputStream download(String bucketName, String objectKey);

    /**
     * Raw download as bytes.
     */
    byte[] downloadAsBytes(String bucketName, String objectKey);

    /**
     * List objects in bucket.
     */
    List<StorageObject> listObjects(String bucketName, String prefix);

    /**
     * List objects in bucket without prefix filter.
     */
    List<StorageObject> listObjects(String bucketName);

    /**
     * Delete an object.
     */
    void delete(String bucketName, String objectKey);

    /**
     * Delete multiple objects.
     */
    void deleteMultiple(String bucketName, List<String> objectKeys);

    /**
     * Get object metadata.
     */
    StorageObject getMetadata(String bucketName, String objectKey);

    /**
     * Check if object exists.
     */
    boolean exists(String bucketName, String objectKey);

    /**
     * Create bucket.
     */
    void createBucket(String bucketName);

    /**
     * Check if bucket exists.
     */
    boolean bucketExists(String bucketName);

    /**
     * Delete bucket.
     */
    void deleteBucket(String bucketName);
}
