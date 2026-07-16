package org.datamate.authz.domain.service.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents one node in the condition AST stored in {@code expression_json}.
 *
 * <p>Lives in the domain layer because it models a pure domain concept —
 * the structure of an authorization condition expression.</p>
 *
 * <p>Two variants:</p>
 * <ul>
 *   <li><b>Group node:</b> has {@code operator} ("AND" or "OR") and {@code children}.</li>
 *   <li><b>Leaf node:</b> has {@code field}, {@code comparison}, and {@code value}.</li>
 * </ul>
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "operator": "AND",
 *   "children": [
 *     { "field": "resource.amount", "comparison": "<=", "value": 10000 },
 *     { "field": "resource.bank",   "comparison": "!=", "value": "CASH" }
 *   ]
 * }
 * }</pre>
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpressionNode {

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("children")
    private List<ExpressionNode> children;

    @JsonProperty("field")
    private String field;

    @JsonProperty("comparison")
    private String comparison;

    @JsonProperty("value")
    private Object value;

    public boolean isGroup() {
        return operator != null;
    }

    public boolean isLeaf() {
        return field != null && comparison != null;
    }

    public String getOperator() { return operator; }
    public List<ExpressionNode> getChildren() { return children; }
    public String getField() { return field; }
    public String getComparison() { return comparison; }
    public Object getValue() { return value; }
}

