package org.datamate.authz.compiler.generator;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import com.fasterxml.jackson.databind.JsonNode;

public class RegoGenerator {

    public String generate(AstNode root) {
        if (root == null) return "";
        StringBuilder sb = new StringBuilder();
        
        if (root instanceof GroupNode && ((GroupNode) root).getOperator() == LogicalOperator.OR) {
            // OR means generate multiple Rego rules.
            for (AstNode child : ((GroupNode) root).getChildren()) {
                generateRule(child, sb);
            }
        } else {
            // It's an AND group or a single condition
            generateRule(root, sb);
        }
        
        return sb.toString().trim();
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
