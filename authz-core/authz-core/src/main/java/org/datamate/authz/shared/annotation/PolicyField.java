package org.datamate.authz.shared.annotation;

import org.datamate.authz.domain.model.policy.enumtype.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field inside a {@link PolicyResource}-annotated Command as a condition attribute.
 *
 * <p>The {@code authz-core} library registers each annotated field as an
 * {@code authz_condition_field} row, making it available in the Admin UI's Condition Builder.</p>
 *
 * <p>Supported on both Java {@code record} components and regular class fields.</p>
 *
 * <p>Example (record):</p>
 * <pre>{@code
 * @PolicyField(displayName = "Journal Amount", type = FieldType.NUMBER)
 * BigDecimal amount,
 *
 * @PolicyField(displayName = "Bank Account", type = FieldType.STRING,
 *              optionsEndpoint = "/api/finance/banks")
 * String bank,
 *
 * @PolicyField(displayName = "Entry Type", type = FieldType.STRING,
 *              allowedValues = {"EXPENSE", "INCOME", "TRANSFER"})
 * String type
 * }</pre>
 */
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PolicyField {

    /** Human-readable label shown in the Condition Builder UI. */
    String displayName();

    /**
     * Data type — drives operator and value-input rendering.
     * Use {@link FieldType} from {@code org.datamate.authz.domain.model}.
     */
    FieldType type();

    /**
     * Static list of allowed values rendered as a dropdown.
     * Mutually exclusive with {@link #optionsEndpoint()}.
     */
    String[] allowedValues() default {};

    /**
     * Dynamic endpoint returning {@code [{id, display}]} for a live dropdown.
     * Mutually exclusive with {@link #allowedValues()}.
     */
    String optionsEndpoint() default "";
}


