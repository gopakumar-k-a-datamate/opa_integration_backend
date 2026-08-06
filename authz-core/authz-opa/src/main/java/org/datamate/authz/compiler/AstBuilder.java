package org.datamate.authz.compiler;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import com.fasterxml.jackson.databind.JsonNode;

import org.datamate.authz.exception.InvalidPayloadException;

public class AstBuilder {

    private static final int MAX_AST_DEPTH = 5;

    public AstNode build(JsonNode json) {
        return build(json, 1);
    }

    private AstNode build(JsonNode json, int depth) {
        if (depth > MAX_AST_DEPTH) {
            throw new InvalidPayloadException("Invalid AST: Condition tree exceeds maximum depth of " + MAX_AST_DEPTH);
        }

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
                group.addChild(build(child, depth + 1));
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
