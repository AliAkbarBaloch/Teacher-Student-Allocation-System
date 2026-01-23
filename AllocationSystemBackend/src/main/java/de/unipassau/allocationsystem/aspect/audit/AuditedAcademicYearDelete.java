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
 * Meta-annotation for auditing academic year delete operations.
 * Automatically logs when an academic year is deleted.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Audited(
        action = AuditAction.DELETE,
        entityName = AuditEntityNames.ACADEMIC_YEAR,
        description = "Deleted academic year",
        captureNewValue = false
)
public @interface AuditedAcademicYearDelete { }
