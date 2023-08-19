package info.ankin.how.spring.health_waiter;

import info.ankin.how.spring.health_waiter.HealthCheckWaiter.HealthCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerMetricsRecorder;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

class HealthCheckWaiterTest {

    HealthCheckWaiter waiter;
    DisposableServer server;
    HealthCheck healthCheck;
    List<Request> requests = new ArrayList<>();

    @BeforeEach
    void setup() {
        waiter = new HealthCheckWaiter();

        var recorder = mock(HttpServerMetricsRecorder.class);
        doAnswer(i -> requests.add(new Request(i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3))))
                .when(recorder).recordResponseTime(any(), any(), any(), any());

        server = HttpServer.create()
                .accessLog(true)
                .metrics(true, () -> recorder)
                .route(rb -> {
                    rb.get("/good", (req, res) -> res.status(200).send().then());
                    rb.get("/bad", (req, res) -> res.status(500).send().then());
                })
                .bindNow();
        healthCheck = new HealthCheck()
                .setUrl(fromHttpUrl("http://localhost").port(server.port()).build().toUri())
                .setPeriodSeconds(0)
                .setFailureThreshold(2)
        ;

    }

    @Test
    void test_bad() {
        StepVerifier.create(waiter.perform(healthCheck))
                .expectError()
                .verify();

        assertThat(requests, hasSize(2));
        assertThat(requests.stream().map(Request::status).toList(), everyItem(is("404")));
    }

    @Test
    void test_good() {
        healthCheck.setUrl(fromUri(healthCheck.getUrl()).path("/good").build().toUri());
        StepVerifier.create(waiter.perform(healthCheck))
                .expectComplete()
                .verify();

        assertThat(requests, hasSize(1));
        assertThat(requests.get(0).status(), is("200"));
        assertThat(requests.get(0).uri(), is("/good"));
    }

    @ParameterizedTest
    @CsvSource({
            "4,true",
            "3,false",
    })
    void test_badThenGood(int initialCounterValue, boolean shouldFail) {
        healthCheck.setFailureThreshold(3);
        var good = fromUri(healthCheck.getUrl()).path("/good").build().toUri();
        var bad = fromUri(healthCheck.getUrl()).path("/bad").build().toUri();

        var spy = spy(healthCheck);
        var counter = new AtomicInteger(initialCounterValue);
        doAnswer(i -> counter.decrementAndGet() == 0 ? good : bad).when(spy).getUrl();

        if (shouldFail) {
            StepVerifier.create(waiter.perform(spy))
                    .expectError()
                    .verify();

            assertThat(requests, hasSize(3));
            assertThat(counter.get(), is(1));
            return;
        }

        StepVerifier.create(waiter.perform(spy))
                .expectComplete()
                .verify();

        assertThat(requests, hasSize(3));
        assertThat(counter.get(), is(0));
    }

    record Request(String uri, String method, String status, Duration time) {
    }
}
