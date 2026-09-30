package org.datamate.authz.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datamate.authz.compiler.ast.AstNode;
import org.datamate.authz.compiler.ast.ConditionNode;
import org.datamate.authz.compiler.ast.GroupNode;
import org.datamate.authz.compiler.ast.LogicalOperator;
import org.datamate.authz.exception.AuthzInvalidPayloadException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AstBuilderTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AstBuilder astBuilder = new AstBuilder();

    @Test
    void build_nullNode_throwsException() {
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(null));
    }

    @Test
    void build_validConditionNode() throws Exception {
        String json = "{\"field\":\"amount\",\"comparison\":\">\",\"value\":100,\"valueType\":\"VALUE\"}";
        AstNode node = astBuilder.build(mapper.readTree(json));

        assertTrue(node instanceof ConditionNode);
        ConditionNode cond = (ConditionNode) node;
        assertEquals("amount", cond.getField());
        assertEquals(">", cond.getComparison());
        assertEquals(100, cond.getValue().asInt());
        assertEquals(org.datamate.authz.compiler.ast.ValueType.VALUE, cond.getValueType());
    }

    @Test
    void build_fieldwiseConditionNode() throws Exception {
        String json = "{\"field\":\"resource.assignedDoctorId\",\"comparison\":\"==\",\"value\":\"user.id\",\"valueType\":\"FIELD\"}";
        AstNode node = astBuilder.build(mapper.readTree(json));

        assertTrue(node instanceof ConditionNode);
        ConditionNode cond = (ConditionNode) node;
        assertEquals("resource.assignedDoctorId", cond.getField());
        assertEquals("==", cond.getComparison());
        assertEquals("user.id", cond.getValue().asText());
        assertEquals(org.datamate.authz.compiler.ast.ValueType.FIELD, cond.getValueType());
    }

    @Test
    void build_fieldListConditionNode() throws Exception {
        String json = "{\"field\":\"user.id\",\"comparison\":\"IN\",\"value\":[\"resource.doc1\",\"resource.doc2\"],\"valueType\":\"FIELD_LIST\"}";
        AstNode node = astBuilder.build(mapper.readTree(json));

        assertTrue(node instanceof ConditionNode);
        ConditionNode cond = (ConditionNode) node;
        assertEquals("user.id", cond.getField());
        assertEquals("IN", cond.getComparison());
        assertEquals(org.datamate.authz.compiler.ast.ValueType.FIELD_LIST, cond.getValueType());
    }

    @Test
    void build_missingValueType_throwsException() throws Exception {
        String json = "{\"field\":\"amount\",\"comparison\":\">\",\"value\":100}";
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_unknownValueType_throwsException() throws Exception {
        String json = "{\"field\":\"amount\",\"comparison\":\">\",\"value\":100,\"valueType\":\"INVALID\"}";
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_missingConditionFields_throwsException() throws Exception {
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree("{\"comparison\":\">\",\"value\":100,\"valueType\":\"VALUE\"}")));
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree("{\"field\":\"amount\",\"value\":100,\"valueType\":\"VALUE\"}")));
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree("{\"field\":\"amount\",\"comparison\":\">\",\"valueType\":\"VALUE\"}")));
    }

    @Test
    void build_validGroupNode() throws Exception {
        String json = "{\"operator\":\"AND\",\"children\":[{\"field\":\"a\",\"comparison\":\"=\",\"value\":1,\"valueType\":\"VALUE\"}]}";
        AstNode node = astBuilder.build(mapper.readTree(json));

        assertTrue(node instanceof GroupNode);
        GroupNode group = (GroupNode) node;
        assertEquals(LogicalOperator.AND, group.getOperator());
        assertEquals(1, group.getChildren().size());
        assertTrue(group.getChildren().get(0) instanceof ConditionNode);
    }

    @Test
    void build_notGroupWithMultipleChildren_throwsException() throws Exception {
        String json = "{\"operator\":\"NOT\",\"children\":[{\"field\":\"a\",\"comparison\":\"=\",\"value\":1,\"valueType\":\"VALUE\"}, {\"field\":\"b\",\"comparison\":\"=\",\"value\":2,\"valueType\":\"VALUE\"}]}";
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_groupMissingOperator_throwsException() throws Exception {
        String json = "{\"children\":[]}";
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
    }

    @Test
    void build_groupInvalidOperator_throwsException() throws Exception {
        String json = "{\"operator\":\"XOR\",\"children\":[]}";
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(json)));
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
        assertThrows(AuthzInvalidPayloadException.class, () -> astBuilder.build(mapper.readTree(nestedJson)));
    }
}

