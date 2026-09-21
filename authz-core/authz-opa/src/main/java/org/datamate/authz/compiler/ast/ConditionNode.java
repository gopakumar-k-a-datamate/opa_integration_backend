package org.datamate.authz.compiler.ast;

import com.fasterxml.jackson.databind.JsonNode;

public class ConditionNode implements AstNode {
    private final String field;
    private final String comparison;
    private final JsonNode value;
    private final ValueType valueType;

    public ConditionNode(String field, String comparison, JsonNode value, ValueType valueType) {
        this.field = field;
        this.comparison = comparison;
        this.value = value;
        this.valueType = valueType;
    }

    public ConditionNode(String field, String comparison, JsonNode value) {
        this(field, comparison, value, ValueType.VALUE);
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

    public ValueType getValueType() {
        return valueType;
    }
}

