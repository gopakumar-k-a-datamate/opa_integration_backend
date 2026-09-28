package org.datamate.collaboration.attachment;

import com.datamate.bedrock.framework.storage.application.port.StorageService;
import com.datamate.bedrock.framework.storage.domain.model.StorageObject;
import org.datamate.collaboration.attachment.application.usecase.AttachmentService;
import org.datamate.collaboration.attachment.domain.model.AttachmentMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AttachmentService attachmentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "defaultBucket", "chat-attachments");
    }

    @Test
    void testUploadAttachment_Success() {
        UUID threadId = UUID.randomUUID();
        byte[] content = "Hello attachment".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(content);

        when(storageService.bucketExists("chat-attachments")).thenReturn(true);
        when(storageService.upload(eq("chat-attachments"), anyString(), any(InputStream.class), eq("text/plain"), eq((long) content.length)))
                .thenReturn(StorageObject.builder()
                        .bucketName("chat-attachments")
                        .objectKey("br_threads/" + threadId + "/test.txt")
                        .size((long) content.length)
                        .contentType("text/plain")
                        .lastModified(LocalDateTime.now())
                        .build());

        AttachmentMetadata result = attachmentService.upload(
                inputStream,
                "test.txt",
                "text/plain",
                content.length,
                threadId
        );

        assertNotNull(result);
        assertEquals("br_threads/" + threadId + "/test.txt", result.getFileKey());
        assertEquals("test.txt", result.getFileName());
        assertEquals("text/plain", result.getContentType());
        assertEquals(content.length, result.getFileSize());
        verify(storageService, times(1)).upload(eq("chat-attachments"), anyString(), any(), eq("text/plain"), eq((long) content.length));
    }

    @Test
    void testUploadAttachment_CreatesBucketIfNotExists() {
        byte[] content = "Hello".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(content);

        when(storageService.bucketExists("chat-attachments")).thenReturn(false);
        when(storageService.upload(anyString(), anyString(), any(), anyString(), anyLong()))
                .thenReturn(StorageObject.builder()
                        .bucketName("chat-attachments")
                        .objectKey("br_attachments/test.txt")
                        .size(5L)
                        .contentType("text/plain")
                        .build());

        attachmentService.upload(inputStream, "test.txt", "text/plain", content.length, null);

        verify(storageService, times(1)).createBucket("chat-attachments");
    }

    @Test
    void testDownloadAttachment_Success() {
        InputStream mockStream = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        when(storageService.download("chat-attachments", "file-123")).thenReturn(mockStream);

        InputStream result = attachmentService.download("file-123");

        assertNotNull(result);
        verify(storageService, times(1)).download("chat-attachments", "file-123");
    }
}