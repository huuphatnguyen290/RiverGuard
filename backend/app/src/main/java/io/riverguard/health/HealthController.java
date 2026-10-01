package io.riverguard.health;

import java.util.Map;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("api")
public class HealthController {
    private final DatabaseHealthIndicator database;

    public HealthController(DatabaseHealthIndicator database) {
        this.database = database;
    }

    @GetMapping("/api/health/live")
    public Map<String, String> live() {
        return Map.of("status", "UP");
    }

    @GetMapping("/api/health/ready")
    public ResponseEntity<Map<String, String>> ready() {
        boolean ready = Status.UP.equals(database.health().getStatus());
        return ResponseEntity.status(ready ? 200 : 503)
                .body(Map.of("status", ready ? "UP" : "DOWN"));
    }
}
