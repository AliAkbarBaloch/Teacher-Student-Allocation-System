package de.unipassau.allocationsystem.aspect;

import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatically capturing audit events from service layer operations.
 * Intercepts methods annotated with @Audited and create audit log entries.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;

    /**
     * Intercept methods annotated with @Audited and create audit log entries.
     */
    @AfterReturning(
        pointcut = "@annotation(audited)",
        returning = "result"
    )
    public void auditMethodExecution(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            String entityName = audited.entityName().isEmpty() 
                ? extractEntityNameFromMethod(joinPoint) 
                : audited.entityName();
            
            AuditAction action = audited.action();
            String description = audited.description().isEmpty() 
                ? generateDescription(joinPoint, action, entityName) 
                : audited.description();

            Object recordId = extractRecordId(joinPoint, result);
            Object previousValue = audited.capturePreviousValue() ? extractPreviousValue(joinPoint) : null;
            Object newValue = audited.captureNewValue() ? result : null;

            auditLogService.logWithCurrentUser(
                action,
                entityName,
                recordId != null ? recordId.toString() : null,
                previousValue,
                newValue,
                description
            );
        } catch (Exception e) {
            log.error("Failed to create audit log for method: {}", joinPoint.getSignature().getName(), e);
        }
    }

    private String extractEntityNameFromMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Try to extract entity name from service class name
        if (className.endsWith("Service")) {
            return className.replace("Service", "");
        }
        
        return className;
    }

    private String generateDescription(JoinPoint joinPoint, AuditAction action, String entityName) {
        String methodName = joinPoint.getSignature().getName();
        return String.format("%s operation on %s via %s", action.name(), entityName, methodName);
    }

    private Object extractRecordId(JoinPoint joinPoint, Object result) {
        // Try to extract ID from result if it has an getId() method
        if (result != null) {
            try {
                return result.getClass().getMethod("getId").invoke(result);
            } catch (Exception e) {
                log.debug("Could not extract ID from result", e);
            }
        }

        // Try to extract ID from method arguments
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof Long || arg instanceof Integer || arg instanceof String) {
                    return arg;
                }
            }
        }

        return null;
    }

    private Object extractPreviousValue(JoinPoint joinPoint) {
        // In a real implementation, you might want to:
        // 1. Query the database for the current state before the operation
        // 2. Use a ThreadLocal to store the "before" state
        // 3. Implement a more sophisticated change tracking mechanism
        
        // For now, return null - the previous value should be passed explicitly
        // or captured via other means (e.g., in the service method itself)
        return null;
    }
}
