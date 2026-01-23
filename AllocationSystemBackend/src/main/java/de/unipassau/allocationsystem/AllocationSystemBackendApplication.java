package de.unipassau.allocationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Allocation System Backend.
 */
@SpringBootApplication
public class AllocationSystemBackendApplication {

    /**
     * main method: entry point for the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AllocationSystemBackendApplication.class, args);
    }

}
