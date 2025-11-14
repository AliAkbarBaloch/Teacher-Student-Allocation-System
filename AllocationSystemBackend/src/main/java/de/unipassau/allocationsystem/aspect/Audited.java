package de.unipassau.allocationsystem.aspect;

import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited.
 * When applied to a method, the AuditAspect will automatically create an audit log entry.
 * 
 * Usage example:
 * <pre>
 * {@code @Audited(action = AuditAction.CREATE, entityName = "User", description = "Created new user")}
 * public User createUser(UserDto userDto) {
 *     // method implementation
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    
    /**
     * The audit action type (CREATE, UPDATE, DELETE, etc.).
     */
    AuditAction action();
    
    /**
     * Name of the entity being audited (e.g., "User", "Role", "AllocationPlan").
     * If not specified, will attempt to extract from the method or class name.
     */
    String entityName() default "";
    
    /**
     * Description of the audited action.
     * If not specified, a default description will be generated.
     */
    String description() default "";
    
    /**
     * Whether to capture the previous value before the operation.
     * Note: This may require additional implementation to fetch the before-state.
     */
    boolean capturePreviousValue() default false;
    
    /**
     * Whether to capture the new value (result) after the operation.
     */
    boolean captureNewValue() default true;
}
