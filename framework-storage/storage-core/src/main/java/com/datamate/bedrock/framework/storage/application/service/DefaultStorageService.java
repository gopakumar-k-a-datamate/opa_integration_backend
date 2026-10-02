package com.datamate.bedrock.framework.storage.application.service;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.application.dto.UploadCommand;
import com.datamate.bedrock.framework.storage.application.dto.UploadRequest;
import com.datamate.bedrock.framework.storage.application.port.StorageProvider;
import com.datamate.bedrock.framework.storage.application.port.StorageService;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default Storage Application Service.
 * Implements inbound port {@link StorageService} by orchestrating the outbound {@link StorageProvider}.
 * Employs Bedrock Logger for structured, professional log auditing.
 */
@Service
@Primary
@RequiredArgsConstructor
public class DefaultStorageService implements StorageService {

    @EnableLogger
    private Logger log;

    private final StorageProvider provider;

    @Override
    public StorageObject upload(String bucketName, String objectKey, InputStream inputStream, String contentType,
            long size) {
        String processedKey = objectKey;
        if (objectKey != null && !objectKey.startsWith("br_")) {
            processedKey = "br_" + objectKey;
            if (log != null) {
                log.info("Auto-prefixed storage object key: {} -> {}", objectKey, processedKey);
            }
        }

        if (log != null) {
            log.info("Executing upload for object '{}' in bucket '{}'", processedKey, bucketName);
        }

        return provider.upload(bucketName, processedKey, inputStream, contentType, size);
    }

    @Override
    public List<StorageObject> uploadMultiple(List<UploadRequest> requests) {
        if (log != null) {
            log.debug("Processing batch upload of {} files", requests != null ? requests.size() : 0);
        }
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(req -> upload(req.getBucketName(), req.getObjectKey(), req.getInputStream(), req.getContentType(),
                        req.getSize()))
                .collect(Collectors.toList());
    }

    @Override
    public InputStream download(String bucketName, String objectKey) {
        return provider.download(bucketName, processKey(objectKey));
    }

    @Override
    public byte[] downloadAsBytes(String bucketName, String objectKey) {
        return provider.downloadAsBytes(bucketName, processKey(objectKey));
    }

    @Override
    public List<StorageObject> listObjects(String bucketName) {
        return provider.listObjects(bucketName, null);
    }

    @Override
    public List<StorageObject> listObjects(String bucketName, String prefix) {
        return provider.listObjects(bucketName, prefix);
    }

    @Override
    public void delete(String bucketName, String objectKey) {
        String processedKey = processKey(objectKey);
        if (log != null) {
            log.debug("Deleting storage object {}/{}", bucketName, processedKey);
        }
        provider.delete(bucketName, processedKey);
    }

    @Override
    public void deleteMultiple(String bucketName, List<String> objectKeys) {
        List<String> processedKeys = objectKeys.stream().map(this::processKey).collect(Collectors.toList());
        provider.deleteMultiple(bucketName, processedKeys);
    }

    @Override
    public StorageObject getMetadata(String bucketName, String objectKey) {
        return provider.getMetadata(bucketName, processKey(objectKey));
    }

    @Override
    public boolean exists(String bucketName, String objectKey) {
        return provider.exists(bucketName, processKey(objectKey));
    }

    @Override
    public void createBucket(String bucketName) {
        if (log != null) {
            log.info("Requesting bucket creation: {}", bucketName);
        }
        provider.createBucket(bucketName);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        if (log != null) {
            log.debug("Checking if bucket exists: {}", bucketName);
        }
        return provider.bucketExists(bucketName);
    }

    @Override
    public void deleteBucket(String bucketName) {
        provider.deleteBucket(bucketName);
    }

    private String processKey(String key) {
        if (key == null) {
            return null;
        }
        if (!key.startsWith("br_")) {
            String processedKey = "br_" + key;
            if (log != null) {
                log.debug("Auto-prefixed storage key: {} -> {}", key, processedKey);
            }
            return processedKey;
        }
        return key;
    }
}