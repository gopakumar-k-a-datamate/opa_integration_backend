package org.datamate.authz.compiler;

import org.datamate.authz.compiler.generator.RegoGenerator;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompilerTest {

    @Test
    public void testJsonToRegoCompilation() {
        // The JSON structure exactly as it would come from the database
        String json = """
        {
          "operator": "OR",
          "children": [
            {
              "operator": "AND",
              "children": [
                {
                  "field": "amount",
                  "comparison": "<=",
                  "value": 10000
                },
                {
                  "field": "bank",
                  "comparison": "!=",
                  "value": "CASH"
                }
              ]
            },
            {
              "operator": "AND",
              "children": [
                {
                  "field": "bank",
                  "comparison": "!=",
                  "value": "CASH"
                },
                {
                  "field": "type",
                  "comparison": "==",
                  "value": "EXPENSE"
                }
              ]
            }
          ]
        }
        """;

        // Step 1: Create a dummy Policy entity holding the JSON
        Policy policy = Policy.reconstitute(
                1L, 100L, SubjectType.ROLE, "ACCOUNTANT",
                PolicyEffect.ALLOW, json, true, null,
                LocalDateTime.now(), LocalDateTime.now(), null, null
        );

        // Step 2: Pass the namespace and policies to the updated RegoGenerator
        RegoGenerator generator = new RegoGenerator();
        Map<Long, String> permCodeLookup = Map.of(100L, "finance:journal:create");
        String actualRego = generator.generate("finance", List.of(policy), permCodeLookup);

        // Step 3: Verify the full generated Rego package matches what we expect
        String expectedRego = """
        package app.authz.finance
        
        default allow := false
        default deny_rule := false
        
        # Policy ID: 1
        allow_rule if {
            "ACCOUNTANT" in input.user.roles
            input.permission == "finance:journal:create"
            input.resource.amount <= 10000
            input.resource.bank != "CASH"
        }
        
        allow_rule if {
            "ACCOUNTANT" in input.user.roles
            input.permission == "finance:journal:create"
            input.resource.bank != "CASH"
            input.resource.type == "EXPENSE"
        }
        
        allow if {
            allow_rule
            not deny_rule
        }
        """.trim();

        assertEquals(expectedRego, actualRego.trim());
        System.out.println("Generated Rego:\\n" + actualRego);
    }
}
