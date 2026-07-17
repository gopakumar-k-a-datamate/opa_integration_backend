package org.datamate.authz.domain.model.policy.entity;

import lombok.Getter;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;
import org.datamate.authz.domain.model.policy.enumtype.FieldStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * An attribute of an {@link Permission} that admins can use to build policy conditions.
 * Auto-registered from {@code @PolicyField} annotations on startup.
 *
 * <h3>Domain Behavior</h3>
 * <ul>
 *   <li>{@link #isActive()} — field can be used in new conditions.</li>
 *   <li>{@link #isDeprecated()} — field was removed from code; referencing policies are disabled.</li>
 *   <li>{@link #hasStaticValues()} — UI should show a static dropdown.</li>
 *   <li>{@link #hasDynamicOptions()} — UI should fetch a live dropdown from {@link #getOptionsEndpoint()}.</li>
 * </ul>
 *
 * <h3>Field Removal Lifecycle</h3>
 * When a field is removed from application code the diff-sync logic at startup applies:
 * <ul>
 *   <li>Policies reference it → status = {@link FieldStatus#DEPRECATED}, policies auto-disabled.</li>
 *   <li>No policies reference it → soft-deleted ({@code deleted_at} set).</li>
 * </ul>
 */
@Getter
public class ConditionField {
    private final Long id;
    private final Long permissionId;
    private final String fieldName;
    private final FieldType fieldType;
    private final String displayName;
    private final List<String> allowedValues;
    private final String optionsEndpoint;
    private final FieldStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private ConditionField(Long id, Long permissionId, String fieldName, FieldType fieldType,
                               String displayName, List<String> allowedValues,
                               String optionsEndpoint, FieldStatus status,
                               LocalDateTime createdAt, LocalDateTime updatedAt,
                               LocalDateTime deletedAt) {
        this.id = id;
        this.permissionId = permissionId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.displayName = displayName;
        this.allowedValues = allowedValues;
        this.optionsEndpoint = optionsEndpoint;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static ConditionField create(Long permissionId, String fieldName, FieldType fieldType,
                                        String displayName, List<String> allowedValues,
                                        String optionsEndpoint) {
        return new ConditionField(null, permissionId, fieldName, fieldType, displayName, 
                                  allowedValues, optionsEndpoint, FieldStatus.ACTIVE, 
                                  LocalDateTime.now(), null, null);
    }

    public static ConditionField reconstitute(Long id, Long permissionId, String fieldName, FieldType fieldType,
                                              String displayName, List<String> allowedValues,
                                              String optionsEndpoint, FieldStatus status,
                                              LocalDateTime createdAt, LocalDateTime updatedAt,
                                              LocalDateTime deletedAt) {
        return new ConditionField(id, permissionId, fieldName, fieldType, displayName, 
                                  allowedValues, optionsEndpoint, status, 
                                  createdAt, updatedAt, deletedAt);
    }

    // ── Domain Behavior ────────────────────────────────────────────────────────

    /** Returns {@code true} when the field is usable in new conditions. */
    public boolean isActive() {
        return deletedAt == null && status == FieldStatus.ACTIVE;
    }

    /** Returns {@code true} when the field has been removed from code but is still referenced by policies. */
    public boolean isDeprecated() {
        return status == FieldStatus.DEPRECATED;
    }

    /** Returns {@code true} if the UI should render a static dropdown from {@link #getAllowedValues()}. */
    public boolean hasStaticValues() {
        return allowedValues != null && !allowedValues.isEmpty();
    }

    /** Returns {@code true} if the UI should fetch options dynamically from {@link #getOptionsEndpoint()}. */
    public boolean hasDynamicOptions() {
        return optionsEndpoint != null && !optionsEndpoint.isBlank();
    }

    /** Returns {@code true} if this field's type supports numeric comparisons ({@code <, <=, >, >=}). */
    public boolean supportsRangeComparisons() {
        return fieldType == FieldType.NUMBER || fieldType == FieldType.DATE;
    }
}


