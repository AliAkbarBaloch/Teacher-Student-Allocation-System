package de.unipassau.allocationsystem.aspect;

import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.service.audit.AuditLogService;
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
        String entityName;
        if (audited.entityName().isEmpty()) {
            entityName = extractEntityNameFromMethod(joinPoint);
        } else {
            entityName = audited.entityName();
        }
        
        AuditAction action = audited.action();
        String description;
        if (audited.description().isEmpty()) {
            description = generateDescription(joinPoint, action, entityName);
        } else {
            description = audited.description();
        }

        Object recordId = extractRecordId(joinPoint, result);
        // Note: Previous value capture not yet implemented - would require database query or ThreadLocal
        Object previousValue = audited.capturePreviousValue() ? null : null;
        Object newValue = audited.captureNewValue() ? result : null;

        try {
            auditLogService.logWithCurrentUser(
                action,
                entityName,
                recordId != null ? recordId.toString() : null,
                previousValue,
                newValue,
                description
            );
        } catch (SecurityException e) {
            log.error("Security error creating audit log for method: {}", joinPoint.getSignature().getName(), e);
        }
    }

    private String extractEntityNameFromMethod(JoinPoint joinPoint) {
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
        Object resultId = extractIdFromResult(result);
        if (resultId != null) {
            return resultId;
        }
        
        return extractIdFromArguments(joinPoint.getArgs());
    }
    
    private Object extractIdFromResult(Object result) {
        if (result == null) {
            return null;
        }
        
        // If result is already a primitive/wrapper type (Long, Integer, String), return it directly
        if (result instanceof Long || result instanceof Integer || result instanceof String) {
            return result;
        }
        
        // Try to extract ID from result using getId() method
        Object id = tryExtractIdByMethod(result, "getId");
        if (id != null) {
            return id;
        }
        
        // Try alternative ID field names
        return tryExtractIdByMethod(result, "id");
    }
    
    private Object tryExtractIdByMethod(Object result, String methodName) {
        try {
            java.lang.reflect.Method getIdMethod = result.getClass().getMethod(methodName);
            return getIdMethod.invoke(result);
        } catch (NoSuchMethodException | IllegalAccessException | 
                 java.lang.reflect.InvocationTargetException e) {
            log.debug("Could not extract ID using method {}: {}", methodName, e.getMessage());
            return null;
        }
    }
    
    private Object extractIdFromArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // Look for Long/Integer/String arguments that could be IDs
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer) {
                return arg;
            }
            
            // For String, check if it looks like an ID (numeric)
            if (arg instanceof String strArg && !strArg.isEmpty()) {
                if (isNumericId(strArg)) {
                    return strArg;
                }
            }
        }
        
        return null;
    }
    
    private boolean isNumericId(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
