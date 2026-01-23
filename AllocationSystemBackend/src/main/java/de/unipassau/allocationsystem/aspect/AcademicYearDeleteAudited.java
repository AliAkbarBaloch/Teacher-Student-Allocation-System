package de.unipassau.allocationsystem.aspect;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for AcademicYear DELETE operations.
 * Combines @Audited(action=DELETE, entity=ACADEMIC_YEAR, captureNewValue=false)
 * with @Transactional.
 * To use: @AcademicYearDeleteAudited and @Audited(action=DELETE, entityName=ACADEMIC_YEAR, description="...", captureNewValue=false)
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface AcademicYearDeleteAudited {
    /**
     * Description of the delete operation.
     */
    String description() default "Deleted academic year";
}
