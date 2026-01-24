package de.unipassau.allocationsystem.aspect;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for AcademicYear VIEW operations.
 * Combines @Audited(action=VIEW, entity=ACADEMIC_YEAR, captureNewValue=false)
 * with @Transactional(readOnly = true).
 * To use: @AcademicYearViewAudited and @Audited(action=VIEW, entityName=ACADEMIC_YEAR, description="...", captureNewValue=false)
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Transactional(readOnly = true)
public @interface AcademicYearViewAudited {
    /**
     * Description of the view operation.
     */
    String description() default "Viewed academic year";
}
