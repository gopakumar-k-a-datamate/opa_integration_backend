package com.datamate.bedrock.framework.storage.adapter.minio;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.application.dto.UploadRequest;
import com.datamate.bedrock.framework.storage.application.port.StorageProvider;
import com.datamate.bedrock.framework.storage.domain.exception.StorageException;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MinIO Storage Provider implementation.
 * Integrates with Bedrock Logger.
 */
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageProvider {

    @EnableLogger
    private Logger log;

    private final MinioClient minioClient;

    @Override
    public StorageObject upload(String bucketName, String objectKey, InputStream inputStream, String contentType,
            long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());

            return StorageObject.builder()
                    .bucketName(bucketName)
                    .objectKey(objectKey)
                    .size(size)
                    .contentType(contentType)
                    .lastModified(Instant.now())
                    .build();
        } catch (Exception e) {
            if (log != null) {
                log.error("Error uploading file to MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to upload file to Minio", e);
        }
    }

    @Override
    public List<StorageObject> uploadMultiple(List<UploadRequest> requests) {
        return requests.stream()
                .map(r -> upload(r.getBucketName(), r.getObjectKey(), r.getInputStream(), r.getContentType(),
                        r.getSize()))
                .collect(Collectors.toList());
    }

    @Override
    public InputStream download(String bucketName, String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            if (log != null) {
                log.error("Error downloading file from MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to download file from Minio", e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String bucketName, String objectKey) {
        try (InputStream is = download(bucketName, objectKey)) {
            return is.readAllBytes();
        } catch (Exception e) {
            if (log != null) {
                log.error("Error downloading file as bytes from MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to download file as bytes from Minio", e);
        }
    }

    @Override
    public List<StorageObject> listObjects(String bucketName) {
        return List.of();
    }

    @Override
    public List<StorageObject> listObjects(String bucketName, String prefix) {
        return List.of();
    }

    @Override
    public void delete(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            if (log != null) {
                log.error("Error deleting file from MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to delete file from Minio", e);
        }
    }

    @Override
    public void deleteMultiple(String bucketName, List<String> objectKeys) {
        objectKeys.forEach(key -> delete(bucketName, key));
    }

    @Override
    public StorageObject getMetadata(String bucketName, String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
            return StorageObject.builder()
                    .bucketName(bucketName)
                    .objectKey(objectKey)
                    .size(stat.size())
                    .contentType(stat.contentType())
                    .lastModified(stat.lastModified().toInstant())
                    .etag(stat.etag())
                    .build();
        } catch (Exception e) {
            if (log != null) {
                log.error("Error getting metadata from MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to get metadata from Minio", e);
        }
    }

    @Override
    public boolean exists(String bucketName, String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
            }
        } catch (Exception e) {
            if (log != null) {
                log.error("Error creating bucket in MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to create bucket in Minio", e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
        } catch (Exception e) {
            if (log != null) {
                log.debug("Error checking bucket existence in MinIO: {}", e.getMessage());
            }
            return false;
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            minioClient.removeBucket(
                    RemoveBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
        } catch (Exception e) {
            if (log != null) {
                log.error("Error deleting bucket from MinIO: {}", e.getMessage());
            }
            throw new StorageException("Failed to delete bucket from Minio", e);
        }
    }
}