package org.datamate.authz.compiler.ast;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class ConditionNode implements AstNode {
    private final String field;
    private final String comparison;
    private final JsonNode value;
    private final ValueType valueType;
    private final String compareTo;
    private final List<MathOperation> mathOperations;

    public ConditionNode(String field, String comparison, JsonNode value, ValueType valueType, String compareTo, List<MathOperation> mathOperations) {
        this.field = field;
        this.comparison = comparison;
        this.value = value;
        this.valueType = valueType;
        this.compareTo = compareTo != null ? compareTo : "VALUE";
        this.mathOperations = mathOperations;
    }

    public ConditionNode(String field, String comparison, JsonNode value, ValueType valueType) {
        this(field, comparison, value, valueType, "VALUE", null);
    }

    public ConditionNode(String field, String comparison, JsonNode value) {
        this(field, comparison, value, ValueType.VALUE, "VALUE", null);
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

    public String getCompareTo() {
        return compareTo;
    }

    public List<MathOperation> getMathOperations() {
        return mathOperations;
    }
}

