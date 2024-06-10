package org.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Objects;

@TestConfiguration
public class ExampleAppTestcontainers {
    @SuppressWarnings({"resource", "deprecation"})
    @ServiceConnection
    @Bean
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .withUsername("postgres")
                .withDatabaseName("postgres")
                .withFileSystemBind(Objects.requireNonNull(System.getProperty("schema")),
                        "/docker-entrypoint-initdb.d/schema.sql");
    }
}
