package de.unipassau.allocationsystem.aspect;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for AcademicYear UPDATE operations.
 * Combines @Audited(action=UPDATE, entity=ACADEMIC_YEAR, captureNewValue=true)
 * with @Transactional.
 * To use: @AcademicYearUpdateAudited and @Audited(action=UPDATE, entityName=ACADEMIC_YEAR, description="...", captureNewValue=true)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface AcademicYearUpdateAudited {
    /**
     * Description of the update operation.
     */
    String description() default "Updated academic year";
}
