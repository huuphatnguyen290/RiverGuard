package io.riverguard.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("riverGuardDatabase")
public class DatabaseHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbc;

    public DatabaseHealthIndicator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Health health() {
        try {
            var result = jdbc.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(result) ? Health.up().build() : Health.down().build();
        } catch (DataAccessException exception) {
            // Public probes expose status only, never connection details or credentials.
            return Health.down().build();
        }
    }
}
