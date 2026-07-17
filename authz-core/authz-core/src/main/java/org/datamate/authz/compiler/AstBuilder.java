package org.datamate.authz.compiler;

import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import com.fasterxml.jackson.databind.JsonNode;

public class AstBuilder {

    public AstNode build(JsonNode json) {
        if (json.has("children")) {
            GroupNode group = new GroupNode(
                    LogicalOperator.valueOf(
                            json.get("operator").asText()
                    )
            );

            for (JsonNode child : json.get("children")) {
                group.addChild(
                        build(child)
                );
            }

            return group;
        }

        return new ConditionNode(
                json.get("field").asText(),
                json.get("comparison").asText(),
                json.get("value")
        );
    }
}
