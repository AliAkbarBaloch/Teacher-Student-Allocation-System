package de.unipassau.allocationsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic integration tests for the Spring Boot application context.
 */
@SpringBootTest
@ActiveProfiles("test")
class AllocationSystemBackendApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void simpleUnitTest() {
        assertEquals(2, 1 + 1);
    }
}
