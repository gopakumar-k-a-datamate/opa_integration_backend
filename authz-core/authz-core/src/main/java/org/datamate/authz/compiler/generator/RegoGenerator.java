package org.datamate.authz.compiler.generator;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.compiler.AstBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class RegoGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AstBuilder astBuilder = new AstBuilder();

    public String generate(String namespace, List<Policy> policies) {
        StringBuilder sb = new StringBuilder();
        sb.append("package app.authz.").append(namespace).append("\n\n");
        sb.append("default allow := false\n");
        sb.append("default deny_rule := false\n\n");

        for (Policy policy : policies) {
            String json = policy.getExpressionJson();
            if (json == null || json.trim().isEmpty()) {
                continue;
            }
            try {
                JsonNode rootNode = objectMapper.readTree(json);
                AstNode astRoot = astBuilder.build(rootNode);

                sb.append("# Policy ID: ").append(policy.getId()).append("\n");
                
                if (astRoot instanceof GroupNode && ((GroupNode) astRoot).getOperator() == LogicalOperator.OR) {
                    for (AstNode child : ((GroupNode) astRoot).getChildren()) {
                        generateRule(child, sb);
                    }
                } else {
                    generateRule(astRoot, sb);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to compile AST for Policy " + policy.getId(), e);
            }
        }

        sb.append("allow if {\n    allow_rule\n    not deny_rule\n}\n");
        return sb.toString();
    }
    
    private void generateRule(AstNode node, StringBuilder sb) {
        sb.append("allow_rule if {\n");
        visitNode(node, sb);
        sb.append("}\n\n");
    }
    
    private void visitNode(AstNode node, StringBuilder sb) {
        if (node instanceof GroupNode) {
            GroupNode group = (GroupNode) node;
            if (group.getOperator() == LogicalOperator.AND) {
                // AND means put every child inside one rule.
                for (AstNode child : group.getChildren()) {
                    visitNode(child, sb);
                }
            } else if (group.getOperator() == LogicalOperator.OR) {
                // Nested OR requires more complex DNF conversion, omitting for simplicity of this core concept.
                throw new UnsupportedOperationException("Nested OR inside AND requires Disjunctive Normal Form (DNF) conversion.");
            }
        } else if (node instanceof ConditionNode) {
            ConditionNode cond = (ConditionNode) node;
            sb.append("    input.resource.")
              .append(cond.getField())
              .append(" ")
              .append(cond.getComparison())
              .append(" ")
              .append(formatValue(cond.getValue()))
              .append("\n");
        }
    }
    
    private String formatValue(JsonNode value) {
        if (value.isTextual()) {
            return "\"" + value.asText() + "\"";
        }
        return value.asText();
    }
}
