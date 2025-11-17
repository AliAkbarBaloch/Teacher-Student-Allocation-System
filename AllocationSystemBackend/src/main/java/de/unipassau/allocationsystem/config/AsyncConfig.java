package de.unipassau.allocationsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous processing.
 * Enables @Async annotation and configures thread pool for async audit logging.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 100;
    private static final int AWAIT_TERMINATION_SECONDS = 60;

    /**
     * Thread pool executor for async audit logging.
     * Using a separate executor ensures audit logging doesn't interfere with main application threads.
     * Includes TaskDecorator to propagate SecurityContext and RequestContext to async threads.
     */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("audit-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        
        // Propagate SecurityContext and RequestContext to async threads
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Task decorator that propagates SecurityContext and RequestContext to async threads.
     */
    private static class ContextPropagatingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // Capture context from the calling thread
            SecurityContext securityContext = SecurityContextHolder.getContext();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            
            return () -> {
                try {
                    // Set context in the async thread
                    if (securityContext != null) {
                        SecurityContextHolder.setContext(securityContext);
                    }
                    if (requestAttributes != null) {
                        RequestContextHolder.setRequestAttributes(requestAttributes, true);
                    }
                    
                    // Execute the task
                    runnable.run();
                } finally {
                    // Clean up context after execution
                    SecurityContextHolder.clearContext();
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
