package org.datamate.authz.rest.controller;

import com.datamate.bedrock.framework.common.auditing.annotation.AuditLog;
import org.datamate.authz.dto.policy.BundleResult;
import org.datamate.authz.service.policy.GetOpaBundleService;
import org.datamate.authz.api.endpoint.AuthorizationContext.BundleAuthContext;
import org.datamate.authz.api.endpoint.AuthzBeans;
import org.datamate.authz.api.endpoint.EndpointAuthorization;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OPA Runtime — Bundle Serving API.
 *
 * <p>
 * {@code GET /internal/authz/bundle}
 * </p>
 *
 * <p>
 * Serves the compiled {@code bundle.tar.gz} to the local OPA sidecar.
 * Supports conditional polling via {@code If-None-Match} / {@code ETag} headers
 * to avoid redundant bundle transfers when policies have not changed.
 * </p>
 */
@RestController
@RequestMapping("/internal/authz")
@ConditionalOnBean(name = AuthzBeans.BUNDLE)
public class BundleController {

    private final GetOpaBundleService GetOpaBundleService;
    private final EndpointAuthorization authorization;

    public BundleController(GetOpaBundleService getOpaBundleService,
                            @Qualifier(AuthzBeans.BUNDLE) EndpointAuthorization authorization) {
        GetOpaBundleService = getOpaBundleService;
        this.authorization = authorization;
    }

    @GetMapping(value = "/bundle/{namespace}", produces = "application/gzip")
    @AuditLog(action = "BUNDLE_PUBLISHED", resource = "BUNDLE", resourceId = "#namespace", description = "Serve compiled OPA bundle")
    public ResponseEntity<byte[]> getBundle(
            @PathVariable("namespace") String namespace,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        authorization.authorize(new BundleAuthContext(namespace));

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
