package org.datamate.authz.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.generator.RegoGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CompilerTest {

    @Test
    public void testJsonToRegoCompilation() throws Exception {
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

        // Step 1: Parse the raw string into a Jackson JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        // Step 2: Pass the JsonNode to the AstBuilder to get the AST
        AstBuilder builder = new AstBuilder();
        AstNode ast = builder.build(rootNode);
        assertNotNull(ast, "AST should not be null after building");

        // Step 3: Pass the AST to the RegoGenerator to get the Rego string
        RegoGenerator generator = new RegoGenerator();
        String actualRego = generator.generate(ast);

        // Step 4: Verify the generated Rego matches what we expect
        String expectedRego = """
        allow_rule if {
            input.resource.amount <= 10000
            input.resource.bank != "CASH"
        }
        
        allow_rule if {
            input.resource.bank != "CASH"
            input.resource.type == "EXPENSE"
        }
        """.trim();

        assertEquals(expectedRego, actualRego);
        System.out.println("Generated Rego:\\n" + actualRego);
    }
}
