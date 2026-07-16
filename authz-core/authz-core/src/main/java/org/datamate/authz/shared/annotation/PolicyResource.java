package org.datamate.authz.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an Application Layer Command as a protected resource.
 *
 * <p>On startup, the {@code authz-core} library scans the classpath for all classes
 * annotated with {@code @PolicyResource} and upserts the corresponding
 * {@code authz_resource} and {@code authz_permission} rows into the local database.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @PolicyResource(namespace = "finance", name = "journal", action = "create")
 * public record CreateJournalCommand(
 *     @PolicyField(displayName = "Amount", type = FieldType.NUMBER)
 *     BigDecimal amount
 * ) {}
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PolicyResource {

    /** Bounded context identifier, e.g. {@code "finance"}, {@code "clinical"}. */
    String namespace();

    /** Resource name within the namespace, e.g. {@code "journal"}, {@code "patient"}. */
    String name();

    /** Action being performed on the resource, e.g. {@code "create"}, {@code "view"}. */
    String action();

    /** Optional human-readable description of this permission. */
    String description() default "";
}


