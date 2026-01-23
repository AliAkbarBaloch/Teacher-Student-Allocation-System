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
 * Meta-annotation for auditing academic year update operations.
 * Automatically logs when an academic year is updated, capturing the new values.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.ACADEMIC_YEAR,
        description = "Updated academic year",
        captureNewValue = true
)
public @interface AuditedAcademicYearUpdate { }
