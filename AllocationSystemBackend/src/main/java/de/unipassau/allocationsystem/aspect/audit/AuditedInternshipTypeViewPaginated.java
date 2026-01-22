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
 * Meta-annotation for auditing paginated internship type view operations.
 * Automatically logs when internship types are viewed in paginated format.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Audited(
        action = AuditAction.VIEW,
        entityName = AuditEntityNames.INTERNSHIP_TYPE,
        description = "Viewed list of internship types",
        captureNewValue = false
)
public @interface AuditedInternshipTypeViewPaginated {
}
