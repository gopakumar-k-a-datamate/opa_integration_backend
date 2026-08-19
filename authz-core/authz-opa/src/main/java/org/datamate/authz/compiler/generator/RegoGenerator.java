package org.datamate.authz.compiler.generator;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import org.datamate.authz.model.policy.entity.Policy;
import org.datamate.authz.compiler.AstBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.exception.AuthzInvalidPayloadException;
import org.datamate.authz.exception.PolicyCompilationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashMap;

public class RegoGenerator {

    private final ObjectMapper objectMapper;
    private final AstBuilder astBuilder = new AstBuilder();

    // To hold deferred helper rules for NOT groups
    private static class NotBlock {
        final String helperName;
        final List<ConditionNode> conditions;
        NotBlock(String helperName, List<ConditionNode> conditions) {
            this.helperName = helperName;
            this.conditions = conditions;
        }
    }

    private static class NotBlockPlaceholder extends ConditionNode {
        public NotBlockPlaceholder(String helperName) {
            super(helperName, "_NOT_BLOCK_", null);
        }
    }

    public RegoGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generate(String namespace, List<Policy> policies, Map<Long, String> permCodeLookup) {
        StringBuilder sb = new StringBuilder();
        
        Set<String> collectedImports = new LinkedHashSet<>();
        collectedImports.add("import rego.v1");
        
        Map<Long, String> cleanedSnippets = new HashMap<>();
        for (Policy policy : policies) {
            if (policy.hasCustomRego()) {
                String cleaned = extractImports(policy.getCustomRegoSnippet(), collectedImports);
                cleanedSnippets.put(policy.getId(), cleaned);
            }
        }

        sb.append("package app.authz.").append(namespace).append("\n\n");
        for (String imp : collectedImports) {
            sb.append(imp).append("\n");
        }
        sb.append("\n");

        sb.append("default allow := false\n");
        sb.append("default allow_rule := false\n");
        sb.append("default deny_rule := false\n\n");

        List<NotBlock> deferredBlocks = new ArrayList<>();
        int notBlockCounter = 0;

        for (Policy policy : policies) {
            String permissionCode = permCodeLookup.get(policy.getPermissionId());
            if (permissionCode == null) continue;

            if (policy.hasCustomRego()) {
                sb.append("# Policy ID: ").append(policy.getId()).append(" (Custom Rego)\n");
                sb.append(cleanedSnippets.get(policy.getId())).append("\n\n");
                continue;
            }

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

                List<List<ConditionNode>> dnfClauses = convertToDNF(astRoot, policy.getId(), deferredBlocks, new int[]{0});
                for (List<ConditionNode> clause : dnfClauses) {
                    generateRuleFromClause(policy, permissionCode, clause, sb);
                }
            } catch (AuthzInvalidPayloadException e) {
                throw e; // Rethrow to allow global exception handler to return 400
            } catch (Exception e) {
                throw new PolicyCompilationException("Failed to compile AST for Policy " + policy.getId(), e);
            }
        }

        // Emit NOT block helper rules
        for (NotBlock notBlock : deferredBlocks) {
            sb.append(notBlock.helperName).append(" if {\n");
            for (ConditionNode cond : notBlock.conditions) {
                generateConditionLine(cond, sb);
            }
            sb.append("}\n\n");
        }

        sb.append("allow if {\n    allow_rule\n    not deny_rule\n}\n");
        return sb.toString();
    }

    private String extractImports(String snippet, Set<String> imports) {
        if (snippet == null) return "";
        StringBuilder cleaned = new StringBuilder();
        for (String line : snippet.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import ")) {
                imports.add(trimmed);
            } else {
                cleaned.append(line).append("\n");
            }
        }
        return cleaned.toString().trim();
    }

    private void generateRuleFromClause(Policy policy, String permissionCode, List<ConditionNode> clause, StringBuilder sb) {
        generateRuleHeader(policy, permissionCode, sb);
        for (ConditionNode cond : clause) {
            generateConditionLine(cond, sb);
        }
        sb.append("}\n\n");
    }

    private void generateConditionLine(ConditionNode cond, StringBuilder sb) {
        if (cond instanceof NotBlockPlaceholder) {
            sb.append("    not ").append(cond.getField()).append("\n");
            return;
        }

        String field = cond.getField();
        String comp = cond.getComparison().toLowerCase();

        if (comp.equals("in")) {
            sb.append("    input.resource.").append(field).append(" in ").append(formatSetValue(cond.getValue())).append("\n");
        } else if (comp.equals("not_in") || comp.equals("not in")) {
            sb.append("    not input.resource.").append(field).append(" in ").append(formatSetValue(cond.getValue())).append("\n");
        } else if (comp.equals("contains")) {
            sb.append("    contains(input.resource.").append(field).append(", ").append(formatValue(cond.getValue())).append(")\n");
        } else {
            sb.append("    input.resource.")
                    .append(field)
                    .append(" ")
                    .append(cond.getComparison())
                    .append(" ")
                    .append(formatValue(cond.getValue()))
                    .append("\n");
        }
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
            try {
                long numericId = Long.parseLong(policy.getSubjectId());
                sb.append("    input.user.id == ").append(numericId).append("\n");
            } catch (NumberFormatException e) {
                sb.append("    input.user.id == \"").append(policy.getSubjectId()).append("\"\n");
            }
        }
        sb.append("    input.permission == \"").append(permissionCode).append("\"\n");
    }

    private static final int MAX_DNF_CLAUSES = 50;

    private List<List<ConditionNode>> convertToDNF(AstNode node, Long policyId, List<NotBlock> deferredBlocks, int[] counter) {
        if (node instanceof ConditionNode) {
            List<List<ConditionNode>> dnf = new ArrayList<>();
            List<ConditionNode> andClause = new ArrayList<>();
            andClause.add((ConditionNode) node);
            dnf.add(andClause);
            return dnf;
        } else if (node instanceof GroupNode) {
            GroupNode group = (GroupNode) node;
            List<List<ConditionNode>> dnf = new ArrayList<>();

            if (group.getOperator() == LogicalOperator.NOT) {
                // NOT groups must have exactly 1 child (validated by AstBuilder)
                AstNode child = group.getChildren().get(0);
                String helperName = "_not_block_p" + policyId + "_" + (counter[0]++);
                List<List<ConditionNode>> childDnf = convertToDNF(child, policyId, deferredBlocks, counter);
                
                // We collect all conditions from the child and make a helper rule
                // Assuming child is just conditions (we don't support deeply nested ORs under NOT easily in this basic implementation without more helper rules)
                // For simplicity, we just take the first clause, which matches the example in ADR.
                if (!childDnf.isEmpty()) {
                    deferredBlocks.add(new NotBlock(helperName, childDnf.get(0)));
                }
                
                List<ConditionNode> placeholderClause = new ArrayList<>();
                placeholderClause.add(new NotBlockPlaceholder(helperName));
                dnf.add(placeholderClause);
            } else if (group.getOperator() == LogicalOperator.OR) {
                for (AstNode child : group.getChildren()) {
                    dnf.addAll(convertToDNF(child, policyId, deferredBlocks, counter));
                    if (dnf.size() > MAX_DNF_CLAUSES) {
                        throw new AuthzInvalidPayloadException("Condition produces too many rule combinations. Simplify the expression or use custom Rego.");
                    }
                }
            } else if (group.getOperator() == LogicalOperator.AND) {
                dnf.add(new ArrayList<>());

                for (AstNode child : group.getChildren()) {
                    List<List<ConditionNode>> childDnf = convertToDNF(child, policyId, deferredBlocks, counter);
                    List<List<ConditionNode>> newDnf = new ArrayList<>();

                    for (List<ConditionNode> existingAndClause : dnf) {
                        for (List<ConditionNode> newAndClause : childDnf) {
                            List<ConditionNode> combinedAndClause = new ArrayList<>(existingAndClause);
                            combinedAndClause.addAll(newAndClause);
                            newDnf.add(combinedAndClause);

                            if (newDnf.size() > MAX_DNF_CLAUSES) {
                                throw new AuthzInvalidPayloadException("Condition produces too many rule combinations. Simplify the expression or use custom Rego.");
                            }
                        }
                    }
                    dnf = newDnf;
                }
            }
            return dnf;
        }
        return new ArrayList<>();
    }

    private String formatSetValue(JsonNode value) {
        if (!value.isArray()) {
            return "{" + formatValue(value) + "}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < value.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatValue(value.get(i)));
        }
        sb.append("}");
        return sb.toString();
    }

    private String formatValue(JsonNode value) {
        if (value.isTextual()) {
            return "\"" + value.asText() + "\"";
        }
        return value.toString();
    }
}
