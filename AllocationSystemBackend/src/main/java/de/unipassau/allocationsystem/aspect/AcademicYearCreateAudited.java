package de.unipassau.allocationsystem.aspect;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for AcademicYear CREATE operations.
 * Combines @Audited(action=CREATE, entity=ACADEMIC_YEAR, captureNewValue=true)
 * with @Transactional.
 * To use: @AcademicYearCreateAudited and @Audited(action=CREATE, entityName=ACADEMIC_YEAR, description="...", captureNewValue=true)
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface AcademicYearCreateAudited {
    /**
     * Description of the create operation.
     */
    String description() default "Created academic year";
}
