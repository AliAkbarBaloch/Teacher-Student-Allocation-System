package de.unipassau.allocationsystem.audit;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audit annotation for creating a role.
 */
@Audited(
    action = AuditAction.CREATE,
    entityName = AuditEntityNames.ROLE,
    description = "Created new role",
    captureNewValue = true
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RoleCreateAudit {}
