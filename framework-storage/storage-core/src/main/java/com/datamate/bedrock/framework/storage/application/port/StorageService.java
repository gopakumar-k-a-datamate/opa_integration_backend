package com.datamate.bedrock.framework.storage.application.port;

import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import com.datamate.bedrock.framework.storage.application.dto.UploadRequest;
import com.datamate.bedrock.framework.storage.domain.exception.StorageException;

import java.io.InputStream;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════
 * STORAGE SERVICE - Core Interface (Port)
 * ═══════════════════════════════════════════════════════════════
 * 
 * This interface defines the Port for storage operations.
 * It follows the Hexagonal Architecture pattern.
 */
public interface StorageService {

    /**
     * Uploads a single file.
     * 
     * @param bucketName Target bucket
     * @param objectKey  Target object key
     * @param inputStream File content
     * @param contentType MIME type
     * @param size        File size in bytes
     * @return Metadata of uploaded object
     * @throws StorageException if upload fails
     */
    StorageObject upload(
        String bucketName, 
        String objectKey, 
        InputStream inputStream, 
        String contentType, 
        long size
    );

    /**
     * Uploads multiple files.
     * 
     * @param requests List of upload parameters
     * @return List of StorageObject
     */
    List<StorageObject> uploadMultiple(List<UploadRequest> requests);

    /**
     * Downloads a file as an InputStream. 
     */
    InputStream download(String bucketName, String objectKey);

    /**
     * Downloads a file as a byte array.
     */
    byte[] downloadAsBytes(String bucketName, String objectKey);

    /**
     * Lists all files in a bucket.
     */
    List<StorageObject> listObjects(String bucketName);

    /**
     * Lists files with a prefix.
     */
    List<StorageObject> listObjects(String bucketName, String prefix);

    /**
     * Deletes a file.
     */
    void delete(String bucketName, String objectKey);

    /**
     * Deletes multiple files.
     */
    void deleteMultiple(String bucketName, List<String> objectKeys);

    /**
     * Gets file metadata.
     */
    StorageObject getMetadata(String bucketName, String objectKey);

    /**
     * Checks if a file exists.
     */
    boolean exists(String bucketName, String objectKey);

    /**
     * Creates a bucket.
     */
    void createBucket(String bucketName);

    /**
     * Checks if a bucket exists.
     */
    boolean bucketExists(String bucketName);

    /**
     * Deletes a bucket.
     */
    void deleteBucket(String bucketName);
}
