package org.datamate.authz.service.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.api.policy.ConditionFieldRepository;
import org.datamate.authz.api.policy.PermissionRepository;
import org.datamate.authz.api.policy.PolicyRepository;
import org.datamate.authz.api.policy.PolicyValidation;
import org.datamate.authz.compiler.AstBuilder;
import org.datamate.authz.compiler.generator.RegoGenerator;
import org.datamate.authz.exception.PolicyCompilationException;
import org.datamate.authz.jpa.repository.PolicyBundleCacheRepository;
import org.datamate.authz.model.policy.entity.ConditionField;
import org.datamate.authz.model.policy.entity.Permission;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.entity.PolicyBundleCache;
import org.datamate.authz.model.policy.enumtype.Status;
import org.datamate.authz.model.policy.valueobject.RegoValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultPolicyCompilerTest {

    @Mock private PolicyRepository policyRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private PolicyBundleCacheRepository bundleCacheRepository;
    @Mock private ConditionFieldRepository conditionFieldRepository;
    @Mock private PolicyValidation validation;
    @Mock private TarGzBundleService bundleBuilder;
    
    @Mock private com.datamate.bedrock.framework.common.logging.service.Logger log;

    private DefaultPolicyCompiler compiler;

    @BeforeEach
    void setUp() throws Exception {
        // Use a real ObjectMapper because RegoGenerator is instantiated internally
        ObjectMapper mapper = new ObjectMapper();
        RegoGenerator regoGen = new RegoGenerator(mapper, new AstBuilder());
        compiler = new DefaultPolicyCompiler(
                policyRepository, 
                permissionRepository, 
                bundleCacheRepository, 
                conditionFieldRepository, 
                validation, 
                bundleBuilder, 
                mapper,
                regoGen
        );
        
        java.lang.reflect.Field logField = DefaultPolicyCompiler.class.getDeclaredField("log");
        logField.setAccessible(true);
        logField.set(compiler, log);
    }

    @Test
    void recompile_success_newBundle() throws Exception {
        when(conditionFieldRepository.findAllDeprecated()).thenReturn(List.of());
        when(policyRepository.findAllActive()).thenReturn(List.of());

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(perm.getCode()).thenReturn("finance:read");
        when(perm.getStatus()).thenReturn(Status.ACTIVE);
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));

        Policy policy = mock(Policy.class);
        when(policy.getId()).thenReturn(100L);
        when(policy.getPermissionId()).thenReturn(10L);
        when(policy.isDeprecated()).thenReturn(false);
        when(policy.getExpressionJson()).thenReturn("{\"field\":\"amount\",\"comparison\":\"EQUALS\",\"value\":\"100\"}");
        when(policyRepository.findAllEnabled()).thenReturn(List.of(policy));

        when(validation.validate(anyString())).thenReturn(new RegoValidationResult(true, List.of()));
        when(bundleCacheRepository.getBundle("finance")).thenReturn(Optional.empty());
        when(bundleBuilder.build(eq("finance"), anyString())).thenReturn(new byte[]{1, 2, 3});

        compiler.recompile("finance");

        ArgumentCaptor<String> regoCaptor = ArgumentCaptor.forClass(String.class);
        verify(validation).validate(regoCaptor.capture());
        assertTrue(regoCaptor.getValue().contains("finance"));

        verify(bundleCacheRepository).upsertBundle(eq("finance"), eq(new byte[]{1, 2, 3}), anyString());
    }

    @Test
    void recompile_success_unchangedBundle() throws Exception {
        when(conditionFieldRepository.findAllDeprecated()).thenReturn(List.of());
        when(policyRepository.findAllActive()).thenReturn(List.of());

        Permission perm = mock(Permission.class);
        when(perm.getId()).thenReturn(10L);
        when(perm.getCode()).thenReturn("finance:read");
        when(perm.getStatus()).thenReturn(Status.ACTIVE);
        when(permissionRepository.findAllActive()).thenReturn(List.of(perm));
        when(policyRepository.findAllEnabled()).thenReturn(List.of());

        when(validation.validate(anyString())).thenReturn(new RegoValidationResult(true, List.of()));

        // We need the hash to match. For an empty policy set, generate the rego, hash it, and mock the DB
        ObjectMapper mapper = new ObjectMapper();
        org.datamate.authz.compiler.generator.RegoGenerator gen = new org.datamate.authz.compiler.generator.RegoGenerator(mapper, new org.datamate.authz.compiler.AstBuilder());
        String rego = gen.generate("finance", List.of(), java.util.Map.of(10L, "finance:read"));
        
        String hash = computeMd5(rego.getBytes());
        PolicyBundleCache cache = mock(PolicyBundleCache.class);
        when(cache.getEtag()).thenReturn(hash);
        when(bundleCacheRepository.getBundle("finance")).thenReturn(Optional.of(cache));

        compiler.recompile("finance");

        // Should not build or upsert
        verify(bundleBuilder, never()).build(anyString(), anyString());
        verify(bundleCacheRepository, never()).upsertBundle(anyString(), any(), anyString());
    }

    @Test
    void recompile_validationFails() {
        when(conditionFieldRepository.findAllDeprecated()).thenReturn(List.of());
        when(policyRepository.findAllActive()).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());
        when(policyRepository.findAllEnabled()).thenReturn(List.of());

        when(validation.validate(anyString())).thenReturn(new RegoValidationResult(false, List.of(new org.datamate.authz.model.policy.valueobject.RegoValidationError(1, 1, "Syntax error"))));

        assertThrows(PolicyCompilationException.class, () -> compiler.recompile("finance"));
    }

    @Test
    void recompile_ioExceptionDuringBuild() throws Exception {
        when(conditionFieldRepository.findAllDeprecated()).thenReturn(List.of());
        when(policyRepository.findAllActive()).thenReturn(List.of());
        when(permissionRepository.findAllActive()).thenReturn(List.of());
        when(policyRepository.findAllEnabled()).thenReturn(List.of());

        when(validation.validate(anyString())).thenReturn(new RegoValidationResult(true, List.of()));
        when(bundleCacheRepository.getBundle("finance")).thenReturn(Optional.empty());
        
        when(bundleBuilder.build(eq("finance"), anyString())).thenThrow(new IOException("Disk error"));

        assertThrows(PolicyCompilationException.class, () -> compiler.recompile("finance"));
    }

    @Test
    void synchronizeDeprecatedPolicies_updatesCorrectly() {
        ConditionField cf = mock(ConditionField.class);
        when(cf.getFieldName()).thenReturn("oldField");
        when(conditionFieldRepository.findAllDeprecated()).thenReturn(List.of(cf));

        Policy p1 = mock(Policy.class); // uses deprecated field via json
        when(p1.getId()).thenReturn(1L);
        when(p1.isDeprecated()).thenReturn(false);
        when(p1.hasCustomRego()).thenReturn(false);
        when(p1.getExpressionJson()).thenReturn("{\"field\":\"oldField\"}");

        Policy p2 = mock(Policy.class); // uses deprecated field via custom rego
        when(p2.getId()).thenReturn(2L);
        when(p2.isDeprecated()).thenReturn(false);
        when(p2.hasCustomRego()).thenReturn(true);
        when(p2.getCustomRegoSnippet()).thenReturn("input.resource.oldField == true");

        Policy p3 = mock(Policy.class); // doesn't use deprecated field
        when(p3.getId()).thenReturn(3L);
        when(p3.isDeprecated()).thenReturn(true); // is currently marked true, should be flipped to false
        when(p3.hasCustomRego()).thenReturn(false);
        when(p3.getExpressionJson()).thenReturn("{\"field\":\"newField\"}");

        when(policyRepository.findAllActive()).thenReturn(List.of(p1, p2, p3));
        
        // This test only hits the synchronize logic implicitly by calling recompile
        // So we mock the rest of recompile to succeed
        when(permissionRepository.findAllActive()).thenReturn(List.of());
        when(policyRepository.findAllEnabled()).thenReturn(List.of());
        when(validation.validate(anyString())).thenReturn(new RegoValidationResult(true, List.of()));
        when(bundleCacheRepository.getBundle(anyString())).thenReturn(Optional.empty());

        try { compiler.recompile("finance"); } catch(Exception ignored) {}

        verify(policyRepository).updateDeprecatedStatus(1L, true);
        verify(policyRepository).updateDeprecatedStatus(2L, true);
        verify(policyRepository).updateDeprecatedStatus(3L, false);
    }

    private String computeMd5(byte[] data) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("MD5").digest(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
