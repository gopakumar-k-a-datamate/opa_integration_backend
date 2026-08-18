package org.datamate.authz.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class as a protected resource that requires a specific permission.
 * <p>
 * Example: {@code @ProtectedResource("pharmacy:prescription:read")}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtectedResource {
    /**
     * The fully qualified permission code required to access this resource (e.g. "namespace:resource:action")
     */
    String value();
}
