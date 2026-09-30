package org.datamate.authz.compiler.ast;

public enum ValueType {
    VALUE,      // Static literal (Default)
    FIELD,      // Single field path reference
    FIELD_LIST  // Array of field path references
}
