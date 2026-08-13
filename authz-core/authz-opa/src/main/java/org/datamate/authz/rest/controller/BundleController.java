package org.datamate.authz.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import org.datamate.authz.dto.policy.BundleResult;
import org.datamate.authz.service.policy.GetOpaBundleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

/**
 * OPA Runtime — Bundle Serving API.
 *
 * <p>{@code GET /internal/authz/bundle}</p>
 *
 * <p>Serves the compiled {@code bundle.tar.gz} to the local OPA sidecar.
 * Supports conditional polling via {@code If-None-Match} / {@code ETag} headers
 * to avoid redundant bundle transfers when policies have not changed.</p>
 */
@RestController
@RequestMapping("/internal/authz")
public class BundleController {

    private final GetOpaBundleService GetOpaBundleService;

    public BundleController(GetOpaBundleService getOpaBundleService) {
        GetOpaBundleService = getOpaBundleService;
    }

    @Operation(summary = "Download OPA Bundle", description = "Returns the compiled bundle.tar.gz for OPA.")
    @GetMapping(value = "/bundle/{namespace}", produces = "application/gzip")
    @AuditLog(action = "BUNDLE_PUBLISHED", resource = "BUNDLE", resourceId = "#namespace", description = "Serve compiled OPA bundle")
    public ResponseEntity<byte[]> getBundle(
            @PathVariable("namespace") String namespace,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        BundleResult result = GetOpaBundleService.getBundle(namespace, ifNoneMatch);

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        if (result.notModified()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .header(HttpHeaders.ETAG, result.etag())
                    .build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, result.etag())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bundle.tar.gz\"")
                .contentType(MediaType.parseMediaType("application/gzip"))
                .contentLength(result.data().length)
                .body(result.data());
    }
}
