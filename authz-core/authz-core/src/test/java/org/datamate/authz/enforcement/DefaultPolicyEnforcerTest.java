package org.datamate.authz.enforcement;

import org.datamate.authz.annotation.PolicyField;
import org.datamate.authz.annotation.PolicyResource;
import org.datamate.authz.api.policy.PolicyEvaluationClient;
import org.datamate.authz.api.principal.PrincipalProvider;
import org.datamate.authz.dto.policy.EvaluationResult;
import org.datamate.authz.enforcement.AuthorizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.datamate.authz.exception.AuthzDeniedException;
import org.datamate.authz.exception.AuthzInvalidPayloadException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPolicyEnforcerTest {

    @Mock
    private PolicyEvaluationClient policyEvaluationClient;

    @Mock
    private PrincipalProvider principalProvider;

    @Mock
    private com.datamate.bedrock.framework.common.logging.service.Logger log;

    @InjectMocks
    private DefaultPolicyEnforcer enforcer;

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field logField = DefaultPolicyEnforcer.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(enforcer, log);
    }

    @PolicyResource(namespace = "finance", resourceName = "invoice", action = "read")
    static class TestResource {
        @PolicyField(displayName = "Department", type = org.datamate.authz.model.policy.enumtype.FieldType.STRING)
        private String department = "HR";
        
        @PolicyField(displayName = "Amount", type = org.datamate.authz.model.policy.enumtype.FieldType.NUMBER)
        private int amount = 500;
        
        private String ignored = "hidden";
    }

    static class NonAnnotatedResource {
    }

    @Test
    void supports_validResource() {
        assertTrue(enforcer.supports(new TestResource()));
    }

    @Test
    void supports_invalidResource() {
        assertFalse(enforcer.supports(new NonAnnotatedResource()));
        assertFalse(enforcer.supports(null));
    }

    @Test
    void evaluate_object_bypassIfNotSupported() {
        assertTrue(enforcer.evaluate(new NonAnnotatedResource()));
        verifyNoInteractions(policyEvaluationClient);
    }

    @Test
    void evaluate_object_success() {
        when(principalProvider.getUserId()).thenReturn("user123");
        when(principalProvider.getRoles()).thenReturn(List.of("ADMIN"));
        when(policyEvaluationClient.evaluate(eq("finance"), any(AuthorizationContext.class))).thenReturn(EvaluationResult.granted());

        TestResource resource = new TestResource();
        boolean result = enforcer.evaluate(resource);

        assertTrue(result);

        ArgumentCaptor<AuthorizationContext> contextCaptor = ArgumentCaptor.forClass(AuthorizationContext.class);
        verify(policyEvaluationClient).evaluate(eq("finance"), contextCaptor.capture());

        AuthorizationContext context = contextCaptor.getValue();
        assertEquals("user123", context.userId());
        assertEquals(List.of("ADMIN"), context.roles());
        assertEquals("finance:invoice:read", context.permissionCode());
        assertEquals("HR", context.resourceData().get("department"));
        assertEquals(500, context.resourceData().get("amount"));
        assertNull(context.resourceData().get("ignored"));
    }

    @Test
    void evaluate_string_success() {
        when(principalProvider.getUserId()).thenReturn("user456");
        when(principalProvider.getRoles()).thenReturn(List.of("USER"));
        when(policyEvaluationClient.evaluate(eq("hr"), any(AuthorizationContext.class))).thenReturn(EvaluationResult.denied("Access Denied: You do not have permission to perform this action."));

        boolean result = enforcer.evaluate("hr:employee:write");

        assertFalse(result);

        ArgumentCaptor<AuthorizationContext> contextCaptor = ArgumentCaptor.forClass(AuthorizationContext.class);
        verify(policyEvaluationClient).evaluate(eq("hr"), contextCaptor.capture());

        AuthorizationContext context = contextCaptor.getValue();
        assertEquals("hr:employee:write", context.permissionCode());
        assertTrue(context.resourceData().isEmpty());
    }

    @Test
    void evaluate_string_null_throwsException() {
        assertThrows(NullPointerException.class, () -> enforcer.evaluate(null));
    }

    @Test
    void enforce_object_throwsExceptionWhenDenied() {
        when(principalProvider.getUserId()).thenReturn("user123");
        when(policyEvaluationClient.evaluate(anyString(), any())).thenReturn(EvaluationResult.denied("Denied"));

        assertThrows(AuthzDeniedException.class, () -> enforcer.enforce(new TestResource()));
    }

    @Test
    void enforce_string_throwsExceptionWhenDenied() {
        when(principalProvider.getUserId()).thenReturn("user123");
        when(policyEvaluationClient.evaluate(anyString(), any())).thenReturn(EvaluationResult.denied("Denied"));

        assertThrows(AuthzDeniedException.class, () -> enforcer.enforce("finance:invoice:read"));
    }

    @Test
    void enforce_object_passesWhenAllowed() {
        when(principalProvider.getUserId()).thenReturn("user123");
        when(policyEvaluationClient.evaluate(anyString(), any())).thenReturn(EvaluationResult.granted());

        assertDoesNotThrow(() -> enforcer.enforce(new TestResource()));
    }
}
