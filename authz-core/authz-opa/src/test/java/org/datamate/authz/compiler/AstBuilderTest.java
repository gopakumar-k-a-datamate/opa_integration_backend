package org.datamate.authz.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import org.datamate.authz.exception.InvalidPayloadException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AstBuilderTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AstBuilder astBuilder = new AstBuilder();

    @Test
    void build_nullNode_throwsException() {
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(null));
    }

    @Test
    void build_validConditionNode() throws Exception {
        String json = "{\"field\":\"amount\",\"comparison\":\">\",\"value\":100}";
        AstNode node = astBuilder.build(mapper.readTree(json));

        assertTrue(node instanceof ConditionNode);
        ConditionNode cond = (ConditionNode) node;
        assertEquals("amount", cond.getField());
        assertEquals(">", cond.getComparison());
        assertEquals(100, cond.getValue().asInt());
    }

    @Test
    void build_missingConditionFields_throwsException() throws Exception {
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree("{\"comparison\":\">\",\"value\":100}")));
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree("{\"field\":\"amount\",\"value\":100}")));
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree("{\"field\":\"amount\",\"comparison\":\">\"}")));
    }

    @Test
    void build_validGroupNode() throws Exception {
        String json = "{\"operator\":\"AND\",\"children\":[{\"field\":\"a\",\"comparison\":\"=\",\"value\":1}]}";
        AstNode node = astBuilder.build(mapper.readTree(json));

        assertTrue(node instanceof GroupNode);
        GroupNode group = (GroupNode) node;
        assertEquals(LogicalOperator.AND, group.getOperator());
        assertEquals(1, group.getChildren().size());
        assertTrue(group.getChildren().get(0) instanceof ConditionNode);
    }

    @Test
    void build_notGroupWithMultipleChildren_throwsException() throws Exception {
        String json = "{\"operator\":\"NOT\",\"children\":[{\"field\":\"a\",\"comparison\":\"=\",\"value\":1}, {\"field\":\"b\",\"comparison\":\"=\",\"value\":2}]}";
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_groupMissingOperator_throwsException() throws Exception {
        String json = "{\"children\":[]}";
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_groupInvalidOperator_throwsException() throws Exception {
        String json = "{\"operator\":\"XOR\",\"children\":[]}";
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_exceedsMaxDepth_throwsException() throws Exception {
        // Build a nested JSON 6 levels deep
        String nestedJson = "{\"operator\":\"AND\",\"children\":[" +
                "{\"operator\":\"AND\",\"children\":[" +
                "{\"operator\":\"AND\",\"children\":[" +
                "{\"operator\":\"AND\",\"children\":[" +
                "{\"operator\":\"AND\",\"children\":[" +
                "{\"operator\":\"AND\",\"children\":[]}" +
                "]}" +
                "]}" +
                "]}" +
                "]}" +
                "]}";
        assertThrows(InvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(nestedJson)));
    }
}
