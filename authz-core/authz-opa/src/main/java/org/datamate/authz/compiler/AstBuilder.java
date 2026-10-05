package org.datamate.authz.compiler;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import org.datamate.authz.compiler.ast.ValueType;
import org.datamate.authz.compiler.ast.MathOperation;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

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
        if (!json.hasNonNull("valueType")) {
            throw new AuthzInvalidPayloadException("Invalid AST: Condition node is missing the 'valueType' attribute ('VALUE', 'FIELD', or 'FIELD_LIST').");
        }

        ValueType valueType;
        try {
            valueType = ValueType.valueOf(json.get("valueType").asText().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthzInvalidPayloadException("Invalid AST: Unknown valueType '" + json.get("valueType").asText() + "'.");
        }

        String compareTo = "VALUE";
        if (json.hasNonNull("compareTo")) {
            compareTo = json.get("compareTo").asText().toUpperCase();
        }

        List<MathOperation> mathOps = new ArrayList<>();
        if (json.hasNonNull("mathOperations") && json.get("mathOperations").isArray()) {
            for (JsonNode opNode : json.get("mathOperations")) {
                MathOperation op = new MathOperation();
                op.setMathOperator(opNode.hasNonNull("mathOperator") ? opNode.get("mathOperator").asText() : "ADD");
                op.setOperandType(opNode.hasNonNull("operandType") ? opNode.get("operandType").asText() : "FIELD");
                op.setValue(opNode.hasNonNull("value") ? opNode.get("value").asText() : "0");
                mathOps.add(op);
            }
        }

        return new ConditionNode(
                json.get("field").asText(),
                json.get("comparison").asText(),
                json.get("value"),
                valueType,
                compareTo,
                mathOps
        );
    }
}

