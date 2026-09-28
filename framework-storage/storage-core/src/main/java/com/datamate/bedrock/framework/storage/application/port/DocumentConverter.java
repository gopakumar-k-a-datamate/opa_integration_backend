package com.datamate.bedrock.framework.storage.application.port;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentConverter {
    Resource convertToPdf(MultipartFile file);
}
