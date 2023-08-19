package info.ankin.how.spring.health_waiter;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@AutoConfiguration
@EnableConfigurationProperties(HealthCheckWaiter.HealthCheckWaiterProperties.class)
public class HealthCheckWaiter {

    WebClient webClient = WebClient.create();

    @Autowired
    public void performHealthCheck(HealthCheckWaiterProperties properties) {
        Flux.fromIterable(properties.getHealthChecks().values())
                .flatMap(this::perform)
                .then()
                .block();
    }

    // visible for testing
    Mono<Void> perform(HealthCheck healthCheck) {
        return Mono.defer(() ->
                        webClient.get().uri(healthCheck.getUrl())
                                .exchangeToMono(ClientResponse::toBodilessEntity)
                                .map(ResponseEntity::getStatusCode)
                                .filter(Predicate.isEqual(HttpStatus.OK))
                                .switchIfEmpty(Mono.error(new RuntimeException("status not ok")))
                )
                .timeout(Duration.ofSeconds(healthCheck.getTimeoutSeconds()))
                .retryWhen(Retry.fixedDelay(healthCheck.getFailureThreshold() - 1,
                        Duration.ofSeconds(healthCheck.getPeriodSeconds())))
                .then();
    }

    /**
     * @see <a href=https://loft.sh/blog/kubernetes-readiness-probes-examples-and-common-pitfalls/>kubernetes probes</a>
     */
    @Data
    @Accessors(chain = true)
    @ConfigurationProperties(HealthCheckWaiterProperties.PREFIX)
    public static class HealthCheckWaiterProperties {
        public static final String PREFIX = "healthcheck-waiter";

        @NestedConfigurationProperty // todo verify if necessary
        Map<String, HealthCheck> healthChecks = new HashMap<>();
    }

    @Data
    @Accessors(chain = true)
    public static class HealthCheck {
        /**
         * since this is addressing a specific use case, this simplifies the HTTP get health check configuration
         */
        URI url;

        /**
         * this is the amount of time to wait initially,
         * <p>
         * taken from kubernetes probe concept with the same meaning.
         */
        int initialDelaySeconds = 0;

        /**
         * this is the amount of time to wait after each failed attempt.
         * <p>
         * If this was a readiness probe, it would be continually checked.
         * but, it is a "liveness" probe, so it is intended to only check on startup.
         * So, here it is only the time to wait after failed attempts.
         * <p>
         * Passing the success threshold stops the checker.
         * <p>
         * taken from kubernetes probe concept with the same meaning.
         */
        int periodSeconds = 5;

        /**
         * this is a request timeout (for each attempt),
         * taken from kubernetes probe concept with the same meaning.
         */
        int timeoutSeconds = 1;

        /**
         * This is in case the first successful result is not enough.
         * The default value of one (1) will stop the checker after the first successful outcome.
         * <p>
         * taken from kubernetes probe concept with the same meaning.
         */
        int successThreshold = 1;

        /**
         * This is how many times we see a failure before we give up.
         * One failure is unfortunate, 10 means we are over the threshold.
         * <p>
         * taken from kubernetes probe concept with the same meaning.
         */
        int failureThreshold = 10;
    }
}
