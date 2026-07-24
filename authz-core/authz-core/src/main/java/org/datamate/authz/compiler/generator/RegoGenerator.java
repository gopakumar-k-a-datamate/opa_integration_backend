package org.datamate.authz.compiler.generator;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.compiler.AstBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.shared.exception.InvalidPayloadException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegoGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AstBuilder astBuilder = new AstBuilder();

    public String generate(String namespace, List<Policy> policies, Map<Long, String> permCodeLookup) {
        StringBuilder sb = new StringBuilder();
        sb.append("package app.authz.").append(namespace).append("\n\n");
        sb.append("default allow := false\n");
        sb.append("default allow_rule := false\n");
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
                
                List<List<ConditionNode>> dnfClauses = convertToDNF(astRoot);
                for (List<ConditionNode> clause : dnfClauses) {
                    generateRuleFromClause(policy, permissionCode, clause, sb);
                }
            } catch (InvalidPayloadException e) {
                throw e; // Rethrow to allow global exception handler to return 400
            } catch (Exception e) {
                throw new RuntimeException("Failed to compile AST for Policy " + policy.getId(), e);
            }
        }

        sb.append("allow if {\n    allow_rule\n    not deny_rule\n}\n");
        return sb.toString();
    }
    
    private void generateRuleFromClause(Policy policy, String permissionCode, List<ConditionNode> clause, StringBuilder sb) {
        generateRuleHeader(policy, permissionCode, sb);
        for (ConditionNode cond : clause) {
            sb.append("    input.resource.")
              .append(cond.getField())
              .append(" ")
              .append(cond.getComparison())
              .append(" ")
              .append(formatValue(cond.getValue()))
              .append("\n");
        }
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
    
    private List<List<ConditionNode>> convertToDNF(AstNode node) {
        if (node instanceof ConditionNode) {
            List<List<ConditionNode>> dnf = new ArrayList<>();
            List<ConditionNode> andClause = new ArrayList<>();
            andClause.add((ConditionNode) node);
            dnf.add(andClause);
            return dnf;
        } else if (node instanceof GroupNode) {
            GroupNode group = (GroupNode) node;
            List<List<ConditionNode>> dnf = new ArrayList<>();
            
            if (group.getOperator() == LogicalOperator.OR) {
                for (AstNode child : group.getChildren()) {
                    dnf.addAll(convertToDNF(child));
                }
            } else if (group.getOperator() == LogicalOperator.AND) {
                dnf.add(new ArrayList<>());
                
                for (AstNode child : group.getChildren()) {
                    List<List<ConditionNode>> childDnf = convertToDNF(child);
                    List<List<ConditionNode>> newDnf = new ArrayList<>();
                    
                    for (List<ConditionNode> existingAndClause : dnf) {
                        for (List<ConditionNode> newAndClause : childDnf) {
                            List<ConditionNode> combinedAndClause = new ArrayList<>(existingAndClause);
                            combinedAndClause.addAll(newAndClause);
                            newDnf.add(combinedAndClause);
                        }
                    }
                    dnf = newDnf;
                }
            }
            return dnf;
        }
        return new ArrayList<>();
    }
    
    private String formatValue(JsonNode value) {
        if (value.isTextual()) {
            return "\"" + value.asText() + "\"";
        }
        return value.asText();
    }
}
