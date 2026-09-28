package com.datamate.bedrock.framework.storage.adapter.libreOffice;

import com.datamate.bedrock.framework.storage.application.port.DocumentConverter;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@Slf4j
public class LibreOfficeDocumentConverter implements DocumentConverter {

    private final org.jodconverter.core.DocumentConverter jodConverter;
    private final long maxFileSizeInBytes;

    public LibreOfficeDocumentConverter(
            org.jodconverter.core.DocumentConverter jodConverter,
            @Value("${jodconverter.max-file-size:50MB}") DataSize maxFileSize) {
        this.jodConverter = jodConverter;
        this.maxFileSizeInBytes = maxFileSize.toBytes();
    }

    @Override
    public Resource convertToPdf(MultipartFile file) {
        if (file.getSize() > maxFileSizeInBytes) {
            throw new IllegalArgumentException("File size exceeds the allowed limit for conversion.");
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown_file");
        String extension = getExtension(originalName);
        DocumentFormat sourceFormat = DefaultDocumentFormatRegistry.getFormatByExtension(extension);

        if (sourceFormat == null) {
            throw new IllegalArgumentException("Unsupported format for conversion: " + extension);
        }

        try {
            Path tempFile = Files.createTempFile("preview_", ".pdf");

            try (InputStream inputStream = file.getInputStream()) {
                log.info("Converting document in framework: {}", originalName);

                jodConverter.convert(inputStream)
                        .as(sourceFormat)
                        .to(tempFile.toFile())
                        .as(DefaultDocumentFormatRegistry.PDF)
                        .execute();
            }

            return new FileSystemResource(tempFile);

        } catch (OfficeException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("LibreOffice TIMEOUT in framework: {}", originalName);
                throw new RuntimeException("Document conversion timed out.", e);
            }
            log.error("LibreOffice Service Error in framework: {}", originalName, e);
            throw new RuntimeException("Conversion service is currently unavailable.", e);

        } catch (Exception e) {
            log.error("Unexpected processing error in framework for: {}", originalName, e);
            throw new RuntimeException("An unexpected error occurred during document processing.", e);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase();
    }
}
