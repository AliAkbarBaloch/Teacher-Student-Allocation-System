package de.unipassau.allocationsystem.aspect.audit;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for auditing internship type get-by-id view operations.
 * Automatically logs when a specific internship type is viewed by its identifier.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Audited(
        action = AuditAction.VIEW,
        entityName = AuditEntityNames.INTERNSHIP_TYPE,
        description = "Viewed internship type by id",
        captureNewValue = false
)
public @interface AuditedInternshipTypeViewById {
}
