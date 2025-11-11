package de.unipassau.allocationsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AllocationMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayAppliedAndAllocationsExist() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM allocations", Integer.class);
        assertThat(count).isNotNull().isGreaterThanOrEqualTo(1);
    }

}
