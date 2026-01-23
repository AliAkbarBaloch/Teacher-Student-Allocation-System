package de.unipassau.allocationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Allocation System Backend.
 * This Spring Boot application manages internship allocation processes
 * for students and companies.
 */
@SpringBootApplication
public class AllocationSystemBackendApplication {

    /**
     * Entry point for the Spring Boot application.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AllocationSystemBackendApplication.class, args);
    }

}
