package org.datamate.authz.compiler.ast;

import com.fasterxml.jackson.databind.JsonNode;

public class ConditionNode implements AstNode {
    private final String field;
    private final String comparison;
    private final JsonNode value;

    public ConditionNode(String field, String comparison, JsonNode value) {
        this.field = field;
        this.comparison = comparison;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getComparison() {
        return comparison;
    }

    public JsonNode getValue() {
        return value;
    }
}
