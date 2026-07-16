package org.datamate.authz.shared.annotation;

/**
 * Data type for {@link PolicyField}.
 *
 * @deprecated Use {@link org.datamate.authz.domain.model.policy.enumtype.FieldType} directly.
 *             This alias exists only for backward compatibility.
 *             The authoritative definition lives in the domain model.
 */
@Deprecated(since = "1.0.0")
public enum FieldType {
    NUMBER, STRING, BOOLEAN, DATE;

    /** Converts to the canonical domain enum. */
    public FieldType toDomain() {
        return FieldType.valueOf(this.name());
    }
}


