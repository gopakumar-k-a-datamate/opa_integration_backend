package org.datamate.authz.compiler;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Component;
import org.datamate.authz.exception.AuthzInvalidPayloadException;

@Component
public class AstBuilder {

    private static final int MAX_AST_DEPTH = 5;

    public AstNode build(JsonNode json) {
        return build(json, 1);
    }

    private AstNode build(JsonNode json, int depth) {
        if (depth > MAX_AST_DEPTH) {
            throw new AuthzInvalidPayloadException("Invalid AST: Condition tree exceeds maximum depth of " + MAX_AST_DEPTH);
        }

        if (json == null || json.isNull()) {
            throw new AuthzInvalidPayloadException("Invalid AST: Node cannot be null.");
        }

        if (json.has("children")) {
            if (!json.hasNonNull("operator")) {
                throw new AuthzInvalidPayloadException("Invalid AST: Group node is missing the 'operator' field.");
            }
            
            LogicalOperator operator;
            try {
                operator = LogicalOperator.valueOf(json.get("operator").asText().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AuthzInvalidPayloadException("Invalid AST: Unknown operator '" + json.get("operator").asText() + "'.");
            }
            
            GroupNode group = new GroupNode(operator);

            JsonNode childrenNode = json.get("children");
            if (!childrenNode.isArray()) {
                throw new AuthzInvalidPayloadException("Invalid AST: 'children' must be an array.");
            }

            for (JsonNode child : childrenNode) {
                AstNode builtChild = build(child, depth + 1);
                if (builtChild != null) {
                    group.addChild(builtChild);
                }
            }

            // Prune empty groups from the tree bottom-up
            if (group.getChildren().isEmpty()) {
                return null;
            }

            if (operator == LogicalOperator.NOT) {
                if (group.getChildren().size() != 1) {
                    throw new AuthzInvalidPayloadException("Invalid AST: NOT group must have exactly one child.");
                }
            }

            return group;
        }

        if (!json.hasNonNull("field")) {
            throw new AuthzInvalidPayloadException("Invalid AST: Condition node is missing the 'field' attribute.");
        }
        if (!json.hasNonNull("comparison")) {
            throw new AuthzInvalidPayloadException("Invalid AST: Condition node is missing the 'comparison' attribute.");
        }
        if (!json.has("value")) {
            throw new AuthzInvalidPayloadException("Invalid AST: Condition node is missing the 'value' attribute.");
        }

        return new ConditionNode(
                json.get("field").asText(),
                json.get("comparison").asText(),
                json.get("value")
        );
    }
}
