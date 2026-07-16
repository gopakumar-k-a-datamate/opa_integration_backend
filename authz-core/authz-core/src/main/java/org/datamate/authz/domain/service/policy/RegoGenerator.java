package org.datamate.authz.domain.service.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.domain.model.policy.entity.Policy;
import org.datamate.authz.domain.model.policy.enumtype.PolicyEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service that translates enabled {@link Policy} domain objects into Rego policy text.
 *
 * <p>This is a pure computation service with no side effects and no persistence dependency —
 * it belongs in the domain layer.</p>
 *
 * <h3>Generated Rego Structure</h3>
 * <pre>{@code
 * package app.authz
 *
 * import future.keywords.if
 * import future.keywords.in
 *
 * default allow := false
 * default deny_rule := false
 *
 * # ALLOW rules (one or more blocks)
 * allow_rule if { ... }
 *
 * # DENY rules (one or more blocks)
 * deny_rule if { ... }
 *
 * # Final decision — DENY always overrides ALLOW
 * allow if {
 *     allow_rule
 *     not deny_rule
 * }
 * }</pre>
 *
 * <h3>AND vs OR Translation</h3>
 * <ul>
 *   <li><b>AND group</b> → conditions on separate lines in one Rego block (Rego AND is implicit).</li>
 *   <li><b>OR group</b> → multiple Rego blocks with the same rule name (multiple definitions = OR).</li>
 * </ul>
 */
@Component
public class RegoGenerator {

    private final ObjectMapper objectMapper;

    public RegoGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Generates the complete Rego policy file.
     *
     * @param policies          enabled, non-deleted policies from the DB
     * @param permCodeLookup    map of permissionId → permissionCode (e.g. {@code "finance:journal:create"})
     * @return full Rego policy as a string
     */
    public String generate(List<Policy> policies, Map<UUID, String> permCodeLookup) {
        StringBuilder rego = new StringBuilder();

        rego.append("package app.authz\n\n");
        rego.append("import future.keywords.if\n");
        rego.append("import future.keywords.in\n\n");
        rego.append("default allow := false\n");
        rego.append("default deny_rule := false\n\n");

        List<Policy> allowPolicies = policies.stream()
                .filter(Policy::isAllow).toList();
        List<Policy> denyPolicies = policies.stream()
                .filter(Policy::isDeny).toList();

        if (!allowPolicies.isEmpty()) {
            rego.append("# --- ALLOW Rules ---\n\n");
            for (Policy policy : allowPolicies) {
                String permCode = permCodeLookup.get(policy.getPermissionId());
                if (permCode != null) {
                    appendPolicyBlocks(rego, policy, permCode, "allow_rule");
                }
            }
        }

        if (!denyPolicies.isEmpty()) {
            rego.append("# --- DENY Rules ---\n\n");
            for (Policy policy : denyPolicies) {
                String permCode = permCodeLookup.get(policy.getPermissionId());
                if (permCode != null) {
                    appendPolicyBlocks(rego, policy, permCode, "deny_rule");
                }
            }
        }

        rego.append("# --- Final Decision: DENY overrides ALLOW ---\n");
        rego.append("allow if {\n");
        rego.append("    allow_rule\n");
        rego.append("    not deny_rule\n");
        rego.append("}\n");

        return rego.toString();
    }

    private void appendPolicyBlocks(StringBuilder rego, Policy policy,
                                    String permCode, String ruleName) {
        String subjectLine = buildSubjectLine(policy);
        String permLine = "    input.permission == \"" + permCode + "\"\n";

        List<List<String>> conditionGroups = parseConditions(policy);

        if (conditionGroups.isEmpty()) {
            // Unconditional — single block, no extra conditions
            rego.append(ruleName).append(" if {\n");
            rego.append(subjectLine);
            rego.append(permLine);
            rego.append("}\n\n");
        } else {
            for (List<String> group : conditionGroups) {
                rego.append(ruleName).append(" if {\n");
                rego.append(subjectLine);
                rego.append(permLine);
                for (String condition : group) {
                    rego.append("    ").append(condition).append("\n");
                }
                rego.append("}\n\n");
            }
        }
    }

    private String buildSubjectLine(Policy policy) {
        if (policy.isRolePolicy()) {
            return "    \"" + policy.getSubjectId() + "\" in input.user.roles\n";
        }
        return "    input.user.id == " + policy.getSubjectId() + "\n";
    }

    /**
     * Parses the expression JSON into condition groups.
     * Each group = one Rego block (groups are OR'd between blocks).
     */
    private List<List<String>> parseConditions(Policy policy) {
        if (policy.isUnconditional()) return List.of();
        try {
            ExpressionNode root = objectMapper.readValue(
                    policy.getExpressionJson(), ExpressionNode.class);
            return buildGroups(root);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<List<String>> buildGroups(ExpressionNode node) {
        if (node.isLeaf()) {
            return List.of(List.of(leafToRego(node)));
        }
        if (!node.isGroup() || node.getChildren() == null || node.getChildren().isEmpty()) {
            return List.of();
        }
        if ("AND".equalsIgnoreCase(node.getOperator())) {
            List<String> conditions = new ArrayList<>();
            for (ExpressionNode child : node.getChildren()) {
                buildGroups(child).forEach(conditions::addAll);
            }
            return List.of(conditions);
        }
        if ("OR".equalsIgnoreCase(node.getOperator())) {
            List<List<String>> groups = new ArrayList<>();
            for (ExpressionNode child : node.getChildren()) {
                groups.addAll(buildGroups(child));
            }
            return groups;
        }
        return List.of();
    }

    private String leafToRego(ExpressionNode leaf) {
        String field = "input." + leaf.getField();
        String op = leaf.getComparison();
        String value = formatValue(leaf.getValue());
        return field + " " + op + " " + value;
    }

    private String formatValue(Object value) {
        if (value instanceof String s) return "\"" + s + "\"";
        return String.valueOf(value);
    }
}


