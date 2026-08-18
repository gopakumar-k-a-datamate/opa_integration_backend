package org.datamate.authz.api.endpoint;

import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.datamate.authz.rest.dto.SavePoliciesRequest;

/**
 * A sealed hierarchy for endpoint authorization contexts.
 * This provides type-safe access to request parameters for consumers
 * who need fine-grained authorization logic.
 */
public sealed interface AuthorizationContext
        permits AuthorizationContext.FieldsAuthContext,
                AuthorizationContext.PoliciesAuthContext,
                AuthorizationContext.SavePoliciesAuthContext,
                AuthorizationContext.NamespacesAuthContext,
                AuthorizationContext.BundleAuthContext {

    record FieldsAuthContext(String permissionCode) implements AuthorizationContext {}

    record PoliciesAuthContext(SubjectType subjectType, String subjectId, String namespace) implements AuthorizationContext {}

    record SavePoliciesAuthContext(SavePoliciesRequest request) implements AuthorizationContext {}

    record NamespacesAuthContext() implements AuthorizationContext {}

    record BundleAuthContext(String namespace) implements AuthorizationContext {}
}
