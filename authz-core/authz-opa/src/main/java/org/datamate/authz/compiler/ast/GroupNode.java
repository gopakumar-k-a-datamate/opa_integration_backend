package org.datamate.authz.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class GroupNode implements AstNode {
    private final LogicalOperator operator;
    private final List<AstNode> children;

    public GroupNode(LogicalOperator operator) {
        this.operator = operator;
        this.children = new ArrayList<>();
    }

    public void addChild(AstNode child) {
        this.children.add(child);
    }

    public LogicalOperator getOperator() {
        return operator;
    }

    public List<AstNode> getChildren() {
        return children;
    }
}
