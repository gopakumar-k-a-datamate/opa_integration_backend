package org.datamate.authz.compiler;

import org.datamate.authz.compiler.generator.RegoGenerator;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;
import org.datamate.authz.domain.model.policy.enumtype.SubjectType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
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
                PolicyEffect.ALLOW, json, true, null, false, 1L,
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
        default allow_rule := false
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
    }

    @Test
    public void testNestedOrToRegoCompilation() {
        String json = """
        {
          "operator": "AND",
          "children": [
            {
              "operator": "OR",
              "children": [
                {
                  "field": "totalAmount",
                  "comparison": "<=",
                  "value": 20000
                },
                {
                  "field": "discountPercentage",
                  "comparison": "<=",
                  "value": 5
                },
                {
                  "field": "isFullyPaid",
                  "comparison": "==",
                  "value": true
                }
              ]
            },
            {
              "field": "insuranceProvider",
              "comparison": "==",
              "value": "BLUE_CROSS"
            }
          ]
        }
        """;

        Policy policy = Policy.reconstitute(
                2L, 101L, SubjectType.ROLE, "DOCTOR",
                PolicyEffect.ALLOW, json, true, null, false, 1L,
                LocalDateTime.now(), LocalDateTime.now(), null, null
        );

        RegoGenerator generator = new RegoGenerator();
        Map<Long, String> permCodeLookup = Map.of(101L, "clinic:visit:create");
        String actualRego = generator.generate("clinic", List.of(policy), permCodeLookup);

        String expectedRego = """
        package app.authz.clinic
        
        default allow := false
        default allow_rule := false
        default deny_rule := false
        
        # Policy ID: 2
        allow_rule if {
            "DOCTOR" in input.user.roles
            input.permission == "clinic:visit:create"
            input.resource.totalAmount <= 20000
            input.resource.insuranceProvider == "BLUE_CROSS"
        }
        
        allow_rule if {
            "DOCTOR" in input.user.roles
            input.permission == "clinic:visit:create"
            input.resource.discountPercentage <= 5
            input.resource.insuranceProvider == "BLUE_CROSS"
        }
        
        allow_rule if {
            "DOCTOR" in input.user.roles
            input.permission == "clinic:visit:create"
            input.resource.isFullyPaid == true
            input.resource.insuranceProvider == "BLUE_CROSS"
        }
        
        allow if {
            allow_rule
            not deny_rule
        }
        """.trim();

        assertEquals(expectedRego, actualRego.trim());
    }

    @Test
    public void testComplexDnfCompilation() {
        String json = """
        {
          "operator": "AND",
          "children": [
            {
              "operator": "OR",
              "children": [
                {
                  "operator": "AND",
                  "children": [
                    { "field": "totalAmount", "comparison": ">", "value": 50000 },
                    { "field": "isPaid", "comparison": "==", "value": false }
                  ]
                },
                {
                  "operator": "AND",
                  "children": [
                    { "field": "dueDate", "comparison": "<", "value": "2026-12-31" },
                    { "field": "insuranceProvider", "comparison": "==", "value": "MEDICARE" }
                  ]
                }
              ]
            },
            {
              "operator": "OR",
              "children": [
                { "field": "discountPercentage", "comparison": "<=", "value": 10 },
                { "field": "insuranceProvider", "comparison": "==", "value": "BLUE_CROSS" }
              ]
            }
          ]
        }
        """;

        Policy policy = Policy.reconstitute(
                3L, 102L, SubjectType.ROLE, "SUPER_ADMIN",
                PolicyEffect.ALLOW, json, true, null, false, 1L,
                LocalDateTime.now(), LocalDateTime.now(), null, null
        );

        RegoGenerator generator = new RegoGenerator();
        Map<Long, String> permCodeLookup = Map.of(102L, "clinic:billing:approve");
        String actualRego = generator.generate("clinic", List.of(policy), permCodeLookup);

        String expectedRego = """
        package app.authz.clinic
        
        default allow := false
        default allow_rule := false
        default deny_rule := false
        
        # Policy ID: 3
        allow_rule if {
            "SUPER_ADMIN" in input.user.roles
            input.permission == "clinic:billing:approve"
            input.resource.totalAmount > 50000
            input.resource.isPaid == false
            input.resource.discountPercentage <= 10
        }
        
        allow_rule if {
            "SUPER_ADMIN" in input.user.roles
            input.permission == "clinic:billing:approve"
            input.resource.totalAmount > 50000
            input.resource.isPaid == false
            input.resource.insuranceProvider == "BLUE_CROSS"
        }
        
        allow_rule if {
            "SUPER_ADMIN" in input.user.roles
            input.permission == "clinic:billing:approve"
            input.resource.dueDate < "2026-12-31"
            input.resource.insuranceProvider == "MEDICARE"
            input.resource.discountPercentage <= 10
        }
        
        allow_rule if {
            "SUPER_ADMIN" in input.user.roles
            input.permission == "clinic:billing:approve"
            input.resource.dueDate < "2026-12-31"
            input.resource.insuranceProvider == "MEDICARE"
            input.resource.insuranceProvider == "BLUE_CROSS"
        }
        
        allow if {
            allow_rule
            not deny_rule
        }
        """.trim();

        assertEquals(expectedRego, actualRego.trim());
    }
}
