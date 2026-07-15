package org.datamate.authz.domain.model.policy.enumtype;

/**
 * Data type of a condition field, used to drive Condition Builder operator lists
 * and value-input rendering in the Admin UI.
 *
 * <p>Kept in the domain model layer so that {@link ConditionField} does not
 * depend on the annotation infrastructure package.</p>
 *
 * <ul>
 *   <li>{@link #NUMBER}  — operators: ==, !=, &lt;, &lt;=, &gt;, &gt;=</li>
 *   <li>{@link #STRING}  — operators: ==, !=, in, not_in</li>
 *   <li>{@link #BOOLEAN} — operators: ==, !=</li>
 *   <li>{@link #DATE}    — operators: ==, !=, &lt;, &lt;=, &gt;, &gt;=</li>
 * </ul>
 */
public enum FieldType {
    NUMBER,
    STRING,
    BOOLEAN,
    DATE
}


