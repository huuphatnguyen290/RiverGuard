package io.riverguard;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationSmokeIT {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Test
    void apiIsLiveAndReadyWithPostgres() throws Exception {
        try (var database = new PostgreSQLContainer("postgres:17.6")) {
            database.start();
            try (var app = start(database.getJdbcUrl(), database.getUsername(), database.getPassword())) {
                assertEquals(200, status(app, "/api/health/live"));
                assertEquals(200, status(app, "/api/health/ready"));
                assertEquals(ZoneOffset.UTC, app.getBean(Clock.class).getZone());
                database.stop();
                assertEquals(503, status(app, "/api/health/ready"));
                assertEquals(200, status(app, "/api/health/live"));
            }
        }
    }

    @Test
    void unavailableDatabaseDoesNotPreventLiveness() throws Exception {
        // Port bound by this test cannot accept PostgreSQL connections.
        try (var unavailable = new java.net.ServerSocket(0);
             var app = start("jdbc:postgresql://127.0.0.1:" + unavailable.getLocalPort()
                     + "/unavailable?connectTimeout=1&socketTimeout=1", "unavailable", "unavailable")) {
            assertEquals(200, status(app, "/api/health/live"));
            assertEquals(503, status(app, "/api/health/ready"));
        }
    }

    private ConfigurableApplicationContext start(String url, String username, String password) {
        return new SpringApplicationBuilder(RiverGuardApplication.class).profiles("api").run(
                "--server.port=0", "--spring.datasource.url=" + url,
                "--spring.datasource.username=" + username,
                "--spring.datasource.password=" + password,
                "--debug=false", "--logging.level.root=WARN", "--spring.main.banner-mode=off");
    }

    private int status(ConfigurableApplicationContext app, String path) throws Exception {
        var port = app.getEnvironment().getRequiredProperty("local.server.port");
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10)).GET().build();
        return http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
