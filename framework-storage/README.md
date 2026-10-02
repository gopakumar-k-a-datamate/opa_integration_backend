# Bedrock Framework Storage

A storage framework module for handling file operations with MinIO, following Bedrock's architecture patterns.

## Overview

Framework-storage provides a clean, abstracted API for object storage operations. It wraps MinIO functionality while maintaining flexibility to swap implementations (AWS S3, Azure Blob, etc.) without changing application code.

## Architecture

Following an **Orchestrated Hexagonal Architecture** (10/10 pattern):

```
framework-storage/
├── storage-core/              # The Brain (Logic & Orchestration)
├── storage-minio/             # The Muscles (MinIO Adapter)
└── bedrock-storage-starter/   # The Nervous System (Auto-config)
```

The **Storage Core** is now the central hub. It orchestrates all operations, allowing for centralized business rules (auditing, naming, events) that work across all providers.

### Modules & Package Structure

**storage-core** - Identity & Contract
- `domain.model`: `StorageObject`
- `domain.exception`: `StorageException`, `FileNotFoundException`, etc.
- `application.port`: `StorageService` interface (The Port)
- `application.dto`: `UploadRequest`

**storage-minio** - Adapter Implementation
- `adapter.minio`: `MinioStorageService`, `MinioProperties`, `MinioClientConfig`

**bedrock-storage-starter** - Infrastructure
- `infra.config`: `StorageAutoConfiguration`
- `META-INF/spring/`: Spring Boot auto-configuration registry

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.datamate.bedrock.framework.storage</groupId>
    <artifactId>bedrock-storage-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure Properties

```yaml
bedrock:
  storage:
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      allowed-extensions:
        - jpg
        - jpeg
        - png
        - pdf
      max-file-size-bytes: 10485760  # 10 MB
```

### 3. Use in Your Code

```java
@Service
public class RestaurantService {
    
    @Autowired
    private StorageService storageService;
    
    public void uploadMenu(MultipartFile file) {
        // Upload file
        StorageObject result = storageService.upload(
            "restaurant-menus",
            "rest-123/menu.pdf",
            file.getInputStream(),
            file.getContentType(),
            file.getSize()
        );
        
        System.out.println("Uploaded: " + result.getObjectKey());
        System.out.println("Size: " + result.getFormattedSize());
    }
}
```

## Smart Logic & Orchestration

The framework isn't just a wrapper; it's an **intelligent manager**. Because business logic lives in the `storage-core`, features are applied automatically:

-   **Auto-Prefixing**: Files are automatically prefixed with `br_` to follow Bedrock standards if not already present.
-   **Standardized Auditing**: Every upload/delete is logged in the Core, ensuring consistent audit trails regardless of the backend (MinIO, S3, etc.).
-   **Provider Independence**: You can swap the storage provider in `storage-minio` without losing your naming conventions or auditing logic.

## Features

### Upload Operations
- Single file upload
- Multiple file upload
- File validation (type & size)
- Custom metadata

### Download Operations
- Stream download
- Byte array download
- Metadata retrieval

### List Operations
- List all objects in bucket
- List with prefix filter

### Delete Operations
- Single file delete
- Multiple file delete

### Bucket Management
- Create bucket
- Check bucket exists
- Delete bucket

## File Validation

Built-in validation for:
- **File extensions** - Whitelist allowed types
- **File size** - Maximum size limits
- **Content type** - MIME type validation

```java
UploadRequest request = UploadRequest.builder()
    .bucketName("restaurant-menus")
    .objectKey("menu.pdf")
    .inputStream(stream)
    .size(fileSize)
    .contentType("application/pdf")
    .allowedExtensions(Arrays.asList("pdf", "jpg", "png"))
    .maxSizeBytes(10 * 1024 * 1024)  // 10 MB
    .build();

if (!request.isExtensionAllowed()) {
    throw new InvalidFileException("Invalid file type");
}
```

## Exception Handling

Custom exceptions for clear error handling:

```java
try {
    storageService.upload(...);
} catch (InvalidFileException e) {
    // File validation failed
    List<String> errors = e.getValidationErrors();
} catch (FileNotFoundException e) {
    // File doesn't exist
} catch (StorageException e) {
    // General storage error
}
```

## Development

### Prerequisites
- Java 21+
- Maven 3.8+
- MinIO server (for testing)

### Build

```bash
mvn clean install
```

### Run MinIO Locally

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  minio/minio server /data --console-address ":9001"
```

Access MinIO Console: http://localhost:9001

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `bedrock.storage.minio.endpoint` | `http://localhost:9000` | MinIO server URL |
| `bedrock.storage.minio.access-key` | `minioadmin` | Access key |
| `bedrock.storage.minio.secret-key` | `minioadmin` | Secret key |
| `bedrock.storage.minio.allowed-extensions` | `[jpg, png, pdf]` | Allowed file types |
| `bedrock.storage.minio.max-file-size-bytes` | `10485760` | Max file size (10 MB) |

## Usage Examples

### Upload with Metadata

```java
UploadRequest request = UploadRequest.builder()
    .bucketName("restaurant-menus")
    .objectKey("rest-123/menu.pdf")
    .inputStream(fileStream)
    .size(fileSize)
    .contentType("application/pdf")
    .build();

request.addMetadata("restaurant-id", "123");
request.addMetadata("uploaded-by", "user@example.com");

StorageObject result = storageService.upload(
    request.getBucketName(),
    request.getObjectKey(),
    request.getInputStream(),
    request.getContentType(),
    request.getSize()
);
```

### Download File

```java
// As stream
InputStream stream = storageService.download("restaurant-menus", "rest-123/menu.pdf");
Files.copy(stream, Paths.get("downloaded-menu.pdf"));

// As bytes
byte[] data = storageService.downloadAsBytes("restaurant-menus", "rest-123/menu.pdf");
```

### List Files

```java
// List all files in bucket
List<StorageObject> allFiles = storageService.listObjects("restaurant-menus");

// List files with prefix
List<StorageObject> restaurantFiles = storageService.listObjects(
    "restaurant-menus", 
    "rest-123/"
);

for (StorageObject file : restaurantFiles) {
    System.out.println(file.getObjectKey() + " - " + file.getFormattedSize());
}
```

### Delete Files

```java
// Delete single file
storageService.delete("restaurant-menus", "rest-123/menu.pdf");

// Delete multiple files
List<String> filesToDelete = Arrays.asList(
    "rest-123/menu.pdf",
    "rest-123/image.jpg"
);
storageService.deleteMultiple("restaurant-menus", filesToDelete);
```

## Integration with RMS

Example usage in Restaurant Management System:

```java
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    
    @Autowired
    private StorageService storageService;
    
    @PostMapping("/{id}/menu")
    public ResponseEntity<String> uploadMenu(
        @PathVariable String id,
        @RequestParam("file") MultipartFile file
    ) {
        // Validate
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF files allowed");
        }
        
        // Upload
        String objectKey = String.format("restaurants/%s/menu.pdf", id);
        StorageObject result = storageService.upload(
            "restaurant-menus",
            objectKey,
            file.getInputStream(),
            file.getContentType(),
            file.getSize()
        );
        
        return ResponseEntity.ok("Menu uploaded: " + result.getObjectKey());
    }
}
```

## Testing

Unit tests with mocked `StorageService`:

```java
@Mock
private StorageService storageService;

@Test
public void testUpload() {
    StorageObject mockResult = StorageObject.builder()
        .bucketName("test-bucket")
        .objectKey("test.pdf")
        .size(1024)
        .build();
    
    when(storageService.upload(any(), any(), any(), any(), anyLong()))
        .thenReturn(mockResult);
    
    // Test your service
}
```

## Contributing

1. Follow Bedrock architecture patterns
2. Add comprehensive Javadoc
3. Include unit tests
4. Update README for new features

## License

Copyright 2024 Datamate. All rights reserved.

## Support

For issues and questions:
- Check documentation in `docs/`
- Review code examples
- Contact development team
