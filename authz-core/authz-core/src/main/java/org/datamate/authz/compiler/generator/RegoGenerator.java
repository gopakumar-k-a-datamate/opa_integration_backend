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
import java.util.Map;

public class RegoGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AstBuilder astBuilder = new AstBuilder();

    public String generate(String namespace, List<Policy> policies, Map<Long, String> permCodeLookup) {
        StringBuilder sb = new StringBuilder();
        sb.append("package app.authz.").append(namespace).append("\n\n");
        sb.append("default allow := false\n");
        sb.append("default deny_rule := false\n\n");

        for (Policy policy : policies) {
            String permissionCode = permCodeLookup.get(policy.getPermissionId());
            if (permissionCode == null) continue;

            String json = policy.getExpressionJson();
            if (json == null || json.trim().isEmpty()) {
                sb.append("# Policy ID: ").append(policy.getId()).append(" (Unconditional)\n");
                generateRuleHeader(policy, permissionCode, sb);
                sb.append("}\n\n");
                continue;
            }
            try {
                JsonNode rootNode = objectMapper.readTree(json);
                AstNode astRoot = astBuilder.build(rootNode);

                sb.append("# Policy ID: ").append(policy.getId()).append("\n");
                
                if (astRoot instanceof GroupNode && ((GroupNode) astRoot).getOperator() == LogicalOperator.OR) {
                    for (AstNode child : ((GroupNode) astRoot).getChildren()) {
                        generateRule(policy, permissionCode, child, sb);
                    }
                } else {
                    generateRule(policy, permissionCode, astRoot, sb);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to compile AST for Policy " + policy.getId(), e);
            }
        }

        sb.append("allow if {\n    allow_rule\n    not deny_rule\n}\n");
        return sb.toString();
    }
    
    private void generateRule(Policy policy, String permissionCode, AstNode node, StringBuilder sb) {
        generateRuleHeader(policy, permissionCode, sb);
        visitNode(node, sb);
        sb.append("}\n\n");
    }
    
    private void generateRuleHeader(Policy policy, String permissionCode, StringBuilder sb) {
        if (policy.isDeny()) {
            sb.append("deny_rule if {\n");
        } else {
            sb.append("allow_rule if {\n");
        }
        
        if (policy.isRolePolicy()) {
            sb.append("    \"").append(policy.getSubjectId()).append("\" in input.user.roles\n");
        } else if (policy.isUserPolicy()) {
            // Attempt to output as unquoted number if it is purely numeric, else quoted string
            try {
                long numericId = Long.parseLong(policy.getSubjectId());
                sb.append("    input.user.id == ").append(numericId).append("\n");
            } catch (NumberFormatException e) {
                sb.append("    input.user.id == \"").append(policy.getSubjectId()).append("\"\n");
            }
        }
        sb.append("    input.permission == \"").append(permissionCode).append("\"\n");
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
