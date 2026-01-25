package de.unipassau.allocationsystem.audit;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audit annotation for deleting a role.
 */
@Audited(
    action = AuditAction.DELETE,
    entityName = AuditEntityNames.ROLE,
    description = "Deleted a role",
    captureNewValue = false
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RoleDeleteAudit { }
