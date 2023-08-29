package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class ETestApplication {
    public static void main(String[] args) {
        SpringApplication
                .from(EApp::main)
                .with(TestConfig.class)
                .run(args);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @ServiceConnection
        @RestartScope
        @SuppressWarnings("resource")
        OracleContainer postgreSQLContainer() {
            return new OracleContainer(
                    DockerImageName
                            .parse("gvenzl/oracle-xe")
                            /*
                                supports:

                                * 11-slim-faststart
                                * 18-slim-faststart
                                * 21-slim-faststart
                                * slim-faststart
                             */
                            .withTag("11-slim-faststart"))
                    .usingSid() // if 11
                    .withReuse(true);
        }
    }

    @SuppressWarnings("unused")
    static class TestContextInit implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            @SuppressWarnings("resource")
            // OracleContainer oracleContainer = new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:11-slim-faststart"));
            OracleContainer oracleContainer = new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:slim-faststart"));
            oracleContainer.withReuse(true);
            oracleContainer.start();

            TestPropertyValues.of(Map.of(
                    "spring.datasource.url", oracleContainer.getJdbcUrl(),
                    "spring.datasource.username", oracleContainer.getUsername(),
                    "spring.datasource.password", oracleContainer.getPassword()
            )).applyTo(applicationContext.getEnvironment());
        }
    }
}
