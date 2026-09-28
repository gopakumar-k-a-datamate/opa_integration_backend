package org.datamate.collaboration.attachment.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datamate.collaboration.attachment.adapter.in.rest.dto.AttachmentResponse;
import org.datamate.collaboration.attachment.application.port.in.UploadAttachmentUseCase;
import org.datamate.collaboration.attachment.domain.model.AttachmentMetadata;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final UploadAttachmentUseCase uploadAttachmentUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "threadId", required = false) UUID threadId) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AttachmentMetadata metadata = uploadAttachmentUseCase.upload(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE,
                file.getSize(),
                threadId
        );

        AttachmentResponse response = AttachmentResponse.builder()
                .fileKey(metadata.getFileKey())
                .fileName(metadata.getFileName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .downloadUrl("/api/chat/attachments/" + metadata.getFileKey() + "/download")
                .uploadedAt(metadata.getUploadedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileKey}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(@PathVariable String fileKey) {
        InputStream inputStream = uploadAttachmentUseCase.download(fileKey);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileKey + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }
}