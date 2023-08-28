package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;


// tell spring to create a web server for the test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// we want the web test client to have a long timeout
// to debug server side code without failing the test due to timeout
@AutoConfigureWebTestClient(timeout = "PT24H")
// for adding an itest-specific configuration file if you need to
@ActiveProfiles("itest")
// setting up our integration test application context
@ContextConfiguration(initializers = ReservedNameApplicationITest.Init.class)
class ReservedNameApplicationITest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void test_works() {
        webTestClient.get().uri("/api/hero/itest").exchange()
                .expectStatus().isNotFound();
        webTestClient.post().uri("/api/hero/itest").exchange();
        webTestClient.get().uri("/api/hero/itest").exchange()
                .expectStatus().isOk();
    }

    @Test
    void test_reservedWordFails() {
        webTestClient.get().uri("/api/hero/admin").exchange()
                .expectStatus().isNotFound();
        webTestClient.post().uri("/api/hero/admin").exchange()
                .expectStatus().isBadRequest();
    }

    static class Init
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext ctx) {
            var tag = DockerImageName.parse("postgres")
                    // use alpine for smaller image sizes
                    .withTag("15-alpine");
            // we trust Testcontainers to do its thing with the resource
            //noinspection resource
            PostgreSQLContainer<?> p = new PostgreSQLContainer<>(tag);
            p.start();

            TestPropertyValues.of(Map.of(
                    "spring.datasource.url", p.getJdbcUrl(),
                    "spring.datasource.username", p.getUsername(),
                    "spring.datasource.password", p.getPassword()
            )).applyTo(ctx.getEnvironment());
        }
    }

}