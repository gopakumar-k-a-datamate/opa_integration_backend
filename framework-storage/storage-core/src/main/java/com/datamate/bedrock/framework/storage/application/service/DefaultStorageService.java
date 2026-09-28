package com.datamate.bedrock.framework.storage.application.service;

import com.datamate.bedrock.framework.storage.application.dto.UploadRequest;
import com.datamate.bedrock.framework.storage.application.port.StorageProvider;
import com.datamate.bedrock.framework.storage.application.port.StorageService;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * ═══════════════════════════════════════════════════════════════
 * DEFAULT STORAGE SERVICE - Application Logic (The Brain)
 * ═══════════════════════════════════════════════════════════════
 * 
 * This class implements the Inbound Port (StorageService) by
 * This class implements the Inbound Port (StorageService) by
 * orchestrating the Outbound Port (StorageProvider).
 * 
 * It is where ORCHESTRATION and BUSINESS RULES live.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class DefaultStorageService implements StorageService {

    private final StorageProvider provider;

    @Override
    public StorageObject upload(String bucketName, String objectKey, InputStream inputStream, String contentType,
            long size) {
        // --- START ORCHESTRATION LOGIC ---
        String processedKey = objectKey;
        if (!objectKey.startsWith("br_")) {
            processedKey = "br_" + objectKey; // Bedrock standard prefix
            log.info("📍 ORCHESTRATOR: Auto-prefixed file: {} -> {}", objectKey, processedKey);
        }

        log.info("📝 AUDIT: User requested upload of '{}' to bucket '{}'", processedKey, bucketName);
        // --- END ORCHESTRATION LOGIC ---

        return provider.upload(bucketName, processedKey, inputStream, contentType, size);
    }

    @Override
    public List<StorageObject> uploadMultiple(List<UploadRequest> requests) {
        log.debug("📦 ORCHESTRATOR: Processing batch upload of {} files", requests.size());
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
        log.debug("📝 ORCHESTRATOR: Auditing deletion of {}/{}", bucketName, processedKey);
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
        log.info("🏗️ ORCHESTRATOR: Requesting bucket creation: {}", bucketName);
        provider.createBucket(bucketName);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        log.info("🔍 ORCHESTRATOR: Checking if bucket exists: {}", bucketName);
        return provider.bucketExists(bucketName);
    }

    @Override
    public void deleteBucket(String bucketName) {
        provider.deleteBucket(bucketName);
    }

    private String processKey(String key) {
        if (key == null)
            return null;
        if (!key.startsWith("br_")) {
            String processedKey = "br_" + key;
            log.debug("📍 ORCHESTRATOR: Auto-prefixed key: {} -> {}", key, processedKey);
            return processedKey;
        }
        return key;
    }
}
