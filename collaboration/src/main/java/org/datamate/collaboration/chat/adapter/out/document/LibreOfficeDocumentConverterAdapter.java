package org.datamate.collaboration.chat.adapter.out.document;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import org.datamate.collaboration.chat.application.port.out.DocumentConversionPort;
import org.datamate.collaboration.exception.CollaborationBaseException;
import org.datamate.collaboration.exception.CollaborationErrorCodes;
import org.datamate.collaboration.exception.DomainValidationException;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

/**
 * Outgoing Adapter implementing {@link DocumentConversionPort} using JODConverter and LibreOffice.
 * <p>
 * Converts office documents (.docx, .xlsx, .pptx, etc.) to standard PDF bytes
 * for inline preview generation. Decoupled completely from Web/Multipart layers.
 * Uses Bedrock Logger and project-specific exception management.
 */
@Component
public class LibreOfficeDocumentConverterAdapter implements DocumentConversionPort {

    private static final Set<String> CONVERTIBLE_EXTENSIONS = Set.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf", "txt"
    );

    @EnableLogger
    private Logger logger;

    private final DocumentConverter jodConverter;

    public LibreOfficeDocumentConverterAdapter(ObjectProvider<DocumentConverter> jodConverterProvider) {
        this.jodConverter = jodConverterProvider.getIfAvailable();
    }

    @Override
    public boolean isConvertible(String fileNameOrExtension) {
        if (fileNameOrExtension == null || fileNameOrExtension.isBlank() || jodConverter == null) {
            return false;
        }
        String ext = extractExtension(fileNameOrExtension);
        return CONVERTIBLE_EXTENSIONS.contains(ext.toLowerCase());
    }

    @Override
    public byte[] convertToPdf(InputStream sourceStream, String extension) {
        if (sourceStream == null) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "sourceStream");
        }
        if (jodConverter == null) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.REQUIRED_FIELD_MISSING.code(), "jodConverter");
        }

        String ext = extractExtension(extension);
        DocumentFormat sourceFormat = DefaultDocumentFormatRegistry.getFormatByExtension(ext.toLowerCase());
        if (sourceFormat == null) {
            throw new DomainValidationException(
                    CollaborationErrorCodes.FILE_TYPE_BLOCKED.code(), ext, "Unsupported document format for PDF conversion");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (logger != null) {
                logger.info("Converting document with extension [{}] to PDF preview", ext);
            }

            jodConverter.convert(sourceStream)
                    .as(sourceFormat)
                    .to(outputStream)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();

            return outputStream.toByteArray();
        } catch (OfficeException e) {
            if (logger != null) {
                logger.error("LibreOffice conversion failed for extension [{}]: {}", ext, e.getMessage());
            }
            throw new CollaborationBaseException(
                    CollaborationErrorCodes.FILE_TYPE_BLOCKED.code(),
                    "Document conversion to PDF failed: " + e.getMessage(),
                    new Object[]{ext},
                    null,
                    e
            );
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Unexpected error during document conversion: {}", e.getMessage());
            }
            throw new CollaborationBaseException(
                    CollaborationErrorCodes.FILE_TYPE_BLOCKED.code(),
                    "Unexpected error during document conversion: " + e.getMessage(),
                    new Object[]{ext},
                    null,
                    e
            );
        }
    }

    private String extractExtension(String filenameOrExtension) {
        if (filenameOrExtension == null) return "";
        int dotIndex = filenameOrExtension.lastIndexOf('.');
        return (dotIndex != -1) ? filenameOrExtension.substring(dotIndex + 1) : filenameOrExtension;
    }
}