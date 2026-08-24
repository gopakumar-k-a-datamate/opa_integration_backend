package org.datamate.authz.compiler.generator;

import org.datamate.authz.compiler.AstBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.exception.AuthzInvalidPayloadException;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.model.policy.enumtype.SubjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegoGeneratorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private RegoGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RegoGenerator(mapper, new AstBuilder());
    }

    @Test
    void generate_customRego() {
        Policy p = mock(Policy.class);
        when(p.getId()).thenReturn(10L);
        when(p.getPermissionId()).thenReturn(100L);
        when(p.hasCustomRego()).thenReturn(true);
        when(p.getCustomRegoSnippet()).thenReturn("import rego.v1\n\nallow_rule if {\n    input.resource.special == true\n}");

        String rego = generator.generate("finance", List.of(p), Map.of(100L, "finance:special"));
        
        assertTrue(rego.contains("package app.authz.finance"));
        assertTrue(rego.contains("import rego.v1"));
        assertTrue(rego.contains("allow_rule if {\n    input.resource.special == true\n}"));
    }

    @Test
    void generate_unconditionalPolicy() {
        Policy p = mock(Policy.class);
        when(p.getId()).thenReturn(20L);
        when(p.getPermissionId()).thenReturn(200L);
        when(p.isRolePolicy()).thenReturn(true);
        when(p.getSubjectId()).thenReturn("ADMIN");
        when(p.isAllow()).thenReturn(true);
        when(p.getExpressionJson()).thenReturn(null); // Unconditional

        String rego = generator.generate("finance", List.of(p), Map.of(200L, "finance:all"));
        
        assertTrue(rego.contains("allow_rule if {\n    \"ADMIN\" in input.user.roles\n    input.permission == \"finance:all\"\n}"));
    }

    @Test
    void generate_basicCondition() {
        Policy p = mock(Policy.class);
        when(p.getId()).thenReturn(30L);
        when(p.getPermissionId()).thenReturn(300L);
        when(p.isUserPolicy()).thenReturn(true);
        when(p.getSubjectId()).thenReturn("123");
        when(p.isDeny()).thenReturn(true);
        when(p.getExpressionJson()).thenReturn("{\"field\":\"amount\",\"comparison\":\">\",\"value\":100}");

        String rego = generator.generate("finance", List.of(p), Map.of(300L, "finance:spend"));
        
        assertTrue(rego.contains("deny_rule if {\n    input.user.id == 123\n    input.permission == \"finance:spend\"\n    input.resource.amount > 100\n}"));
    }
    
    @Test
    void generate_inAndContainsCondition() {
        Policy p = mock(Policy.class);
        when(p.getId()).thenReturn(30L);
        when(p.getPermissionId()).thenReturn(300L);
        when(p.isUserPolicy()).thenReturn(true);
        when(p.getSubjectId()).thenReturn("abc"); // string ID
        when(p.isAllow()).thenReturn(true);
        when(p.getExpressionJson()).thenReturn("{\"operator\":\"AND\",\"children\":[" +
                "{\"field\":\"status\",\"comparison\":\"in\",\"value\":[\"ACTIVE\",\"PENDING\"]}," +
                "{\"field\":\"tags\",\"comparison\":\"contains\",\"value\":\"VIP\"}" +
                "]}");

        String rego = generator.generate("finance", List.of(p), Map.of(300L, "finance:spend"));
        
        assertTrue(rego.contains("input.user.id == \"abc\""));
        assertTrue(rego.contains("input.resource.status in {\"ACTIVE\", \"PENDING\"}"));
        assertTrue(rego.contains("contains(input.resource.tags, \"VIP\")"));
    }

    @Test
    void generate_notGroupCondition() {
        Policy p = mock(Policy.class);
        when(p.getId()).thenReturn(40L);
        when(p.getPermissionId()).thenReturn(400L);
        when(p.isRolePolicy()).thenReturn(true);
        when(p.getSubjectId()).thenReturn("USER");
        when(p.isAllow()).thenReturn(true);
        when(p.getExpressionJson()).thenReturn("{\"operator\":\"NOT\",\"children\":[" +
                "{\"field\":\"department\",\"comparison\":\"=\",\"value\":\"HR\"}" +
                "]}");

        String rego = generator.generate("finance", List.of(p), Map.of(400L, "finance:read"));
        
        assertTrue(rego.contains("not _not_block_p40_0"));
        assertTrue(rego.contains("_not_block_p40_0 if {"));
        assertTrue(rego.contains("input.resource.department = \"HR\""));
    }

    @Test
    void generate_tooManyOrClauses_throwsException() {
        StringBuilder json = new StringBuilder("{\"operator\":\"OR\",\"children\":[");
        for (int i = 0; i < 52; i++) { // Max is 50
            if (i > 0) json.append(",");
            json.append("{\"field\":\"f").append(i).append("\",\"comparison\":\"=\",\"value\":1}");
        }
        json.append("]}");
        
        Policy p = mock(Policy.class);
        when(p.getId()).thenReturn(50L);
        when(p.getPermissionId()).thenReturn(500L);
        when(p.getExpressionJson()).thenReturn(json.toString());

        assertThrows(AuthzInvalidPayloadException.class, () -> generator.generate("finance", List.of(p), Map.of(500L, "finance:read")));
    }
}
