package org.datamate.collaboration.chat.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.dto.AttachmentDto;
import org.datamate.collaboration.chat.application.port.in.UploadAttachmentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/collaboration/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final UploadAttachmentUseCase uploadAttachmentUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentDto uploadAttachment(@RequestParam("file") MultipartFile file) {
        return uploadAttachmentUseCase.uploadAttachment(file);
    }
}
