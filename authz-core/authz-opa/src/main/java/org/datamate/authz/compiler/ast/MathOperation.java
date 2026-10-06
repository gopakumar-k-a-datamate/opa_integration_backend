package org.datamate.authz.compiler.ast;

public class MathOperation {
    private String mathOperator;
    private String operandType; // "FIELD" or "VALUE"
    private String value;       // Field path (e.g., "resource.fee") or static number (e.g., "15")

    public MathOperation() {}

    public MathOperation(String mathOperator, String operandType, String value) {
        this.mathOperator = mathOperator;
        this.operandType = operandType;
        this.value = value;
    }

    public String getMathOperator() { return mathOperator; }
    public void setMathOperator(String mathOperator) { this.mathOperator = mathOperator; }

    public String getOperandType() { return operandType; }
    public void setOperandType(String operandType) { this.operandType = operandType; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
