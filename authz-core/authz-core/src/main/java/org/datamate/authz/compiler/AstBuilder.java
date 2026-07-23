package org.datamate.authz.compiler;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import com.fasterxml.jackson.databind.JsonNode;

import org.datamate.authz.shared.exception.InvalidPayloadException;

public class AstBuilder {

    public AstNode build(JsonNode json) {
        if (json == null || json.isNull()) {
            throw new InvalidPayloadException("Invalid AST: Node cannot be null.");
        }

        if (json.has("children")) {
            if (!json.hasNonNull("operator")) {
                throw new InvalidPayloadException("Invalid AST: Group node is missing the 'operator' field.");
            }
            
            LogicalOperator operator;
            try {
                operator = LogicalOperator.valueOf(json.get("operator").asText().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidPayloadException("Invalid AST: Unknown operator '" + json.get("operator").asText() + "'.");
            }
            
            GroupNode group = new GroupNode(operator);

            JsonNode childrenNode = json.get("children");
            if (!childrenNode.isArray()) {
                throw new InvalidPayloadException("Invalid AST: 'children' must be an array.");
            }

            for (JsonNode child : childrenNode) {
                group.addChild(build(child));
            }

            return group;
        }

        if (!json.hasNonNull("field")) {
            throw new InvalidPayloadException("Invalid AST: Condition node is missing the 'field' attribute.");
        }
        if (!json.hasNonNull("comparison")) {
            throw new InvalidPayloadException("Invalid AST: Condition node is missing the 'comparison' attribute.");
        }
        if (!json.has("value")) {
            throw new InvalidPayloadException("Invalid AST: Condition node is missing the 'value' attribute.");
        }

        return new ConditionNode(
                json.get("field").asText(),
                json.get("comparison").asText(),
                json.get("value")
        );
    }
}
