package com.datamate.bedrock.framework.storage.application.dto;

import java.time.LocalDateTime;

public record ScanResult(
        boolean clean,
        String virusName,
        LocalDateTime scannedAt) {
}
