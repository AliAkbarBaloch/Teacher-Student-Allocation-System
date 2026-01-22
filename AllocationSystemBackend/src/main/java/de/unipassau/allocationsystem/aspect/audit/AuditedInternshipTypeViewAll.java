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
 * Meta-annotation for auditing internship type get-all view operations.
 * Automatically logs when all internship types are retrieved.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Audited(
        action = AuditAction.VIEW,
        entityName = AuditEntityNames.INTERNSHIP_TYPE,
        description = "Viewed all internship types",
        captureNewValue = false
)
public @interface AuditedInternshipTypeViewAll {
}
