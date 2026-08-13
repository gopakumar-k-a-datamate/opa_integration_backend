package org.datamate.authz.rest.controller;


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
 *
 * <p>OPA sidecar configuration example:</p>
 * <pre>{@code
 * bundles:
 *   authz:
 *     service: local-app
 *     resource: /internal/authz/bundle
 *     polling:
 *       min_delay_seconds: 10
 *       max_delay_seconds: 30
 * }</pre>
 */
@RestController
@RequestMapping("/internal/authz")
public class BundleController {

    public BundleController(GetOpaBundleService GetOpaBundleService) {
        this.GetOpaBundleService = GetOpaBundleService;
    }

    private final GetOpaBundleService GetOpaBundleService;

    /**
     * Downloads the compiled OPA policy bundle for a specific namespace.
     * The OPA sidecar typically polls this endpoint automatically.
     */
    @Operation(summary = "Download OPA Bundle", description = "Returns the compiled bundle.tar.gz for OPA.")
    @GetMapping(value = "/bundle/{namespace}", produces = "application/gzip")
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
