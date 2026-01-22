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
 * Meta-annotation for auditing academic year create operations.
 * Automatically logs when a new academic year is created, capturing the new values.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Audited(
        action = AuditAction.CREATE,
        entityName = AuditEntityNames.ACADEMIC_YEAR,
        description = "Created new academic year",
        captureNewValue = true
)
public @interface AuditedAcademicYearCreate {
}
