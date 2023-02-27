package info.ankin.how.reactivemetrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class ReactiveMetricsDemo {
    public static void main(String[] args) {
        new SimpleWrappingDemo().run();
    }

    public static class SimpleWrappingDemo {
        public void run() {
            SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
            PublisherAdapter publisherAdapter = new PublisherAdapter(meterRegistry);

            Mono<Integer> mono = Mono.delay(Duration.ofSeconds(1)).thenReturn(1);

            Publisher<Integer> timedMono = publisherAdapter.time(mono, "mono", Tags.of(Tag.of("abc", "def")));

            System.out.println(Mono.from(timedMono).block());

            System.out.println(meterRegistry.getMetersAsString());
        }
    }

    @RequiredArgsConstructor
    public static class PublisherAdapter {
        private final MeterRegistry meterRegistry;

        public <T> Publisher<T> time(Publisher<T> publisher, String name, Tags tags) {
            AtomicReference<Timer.Sample> start = new AtomicReference<>();

            // this reason this can't be published in maven central is because
            // reactor already depends on micrometer,
            // so it would be a circular dependency.
            // an alternative solution is to implement the spec yourself,
            // and call static methods to create the frameworks by reflection.
            // todo demonstrate this solution
            return Mono.from(publisher)
                    .doOnSubscribe(s -> start.set(Timer.start()))
                    .doOnError(e -> start.get().stop(meterRegistry.timer(name, tags)))
                    .doOnSuccess(e -> start.get().stop(meterRegistry.timer(name, tags)));
        }
    }
}
