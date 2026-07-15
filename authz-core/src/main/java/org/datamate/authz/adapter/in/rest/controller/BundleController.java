package org.datamate.authz.adapter.in.rest.controller;

import lombok.RequiredArgsConstructor;

import org.datamate.authz.application.port.in.policy.GetOpaBundleUseCase;
import org.datamate.authz.domain.model.policy.entity.PolicyBundleCache;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/authz")
public class BundleController {

    private final GetOpaBundleUseCase getOpaBundleUseCase;
/**
     * Serves the compiled OPA bundle.
     *
     * <p>If the client sends {@code If-None-Match} matching the current bundle's ETag,
     * returns {@code 304 Not Modified} with no body (saving bandwidth during OPA polling).
     * Otherwise returns {@code 200 OK} with the binary bundle content.</p>
     *
     * @param ifNoneMatch OPA's current bundle ETag (optional)
     */
    @GetMapping("/bundle")
    public ResponseEntity<byte[]> getBundle(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        Optional<PolicyBundleCache> bundleOpt = getOpaBundleUseCase.getBundle();

        if (bundleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        PolicyBundleCache bundle = bundleOpt.get();
        String currentEtag = "\"" + bundle.getEtag() + "\"";

        // Conditional GET — 304 Not Modified if ETag matches
        if (currentEtag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .header(HttpHeaders.ETAG, currentEtag)
                    .build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, currentEtag)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bundle.tar.gz\"")
                .contentType(MediaType.parseMediaType("application/gzip"))
                .contentLength(bundle.getBundleData().length)
                .body(bundle.getBundleData());
    }
}




