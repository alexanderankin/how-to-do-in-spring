package info.ankin.how.spring.cloudfunctionbinding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class CloudFunctionBindingDemo {
    public static void main(String[] args) {
        setArguments();
        SpringApplication.run(CloudFunctionBindingDemo.class, args);
    }

    /**
     * need to set up properties in order for this to work
     *
     * @see <a href="file:/application.properties:">application.properties</a>
     */
    public static void setArguments() {
        System.setProperty("spring.cloud.stream.function.bindings.producerFn-out-0", "producer");
        System.setProperty("spring.cloud.stream.function.bindings.consumerFn-in-0", "consumer");
        System.setProperty("spring.cloud.stream.bindings.producer.destination", "topic-name");
        System.setProperty("spring.cloud.stream.bindings.consumer.group", "group-name");
        System.setProperty("spring.cloud.stream.bindings.consumer.destination", "topic-name");
        System.setProperty("spring.cloud.stream.bindings.consumer.consumer.concurrency", "50");
    }

    // no 'consumer' class because printing to console is enough
    @Configuration
    public static class ConsumerConfig {
        @Bean
        Function<Flux<Message<String>>, Mono<Void>> consumerFn() {
            return f -> f.doOnNext(System.out::println).then();
        }
    }

    @Configuration
    public static class ProducerConfig {
        @Bean
        Sinks.Many<Message<String>> producerSink() {
            return Sinks.many().multicast().onBackpressureBuffer();
        }

        @Bean
        Supplier<Flux<Message<String>>> producerFn(Sinks.Many<Message<String>> producerSink) {
            return producerSink::asFlux;
        }
    }

    @RestController
    @RequestMapping("/producer")
    public static class Producer {
        private final Sinks.Many<Message<String>> producerSink;

        public Producer(Sinks.Many<Message<String>> producerSink) {
            this.producerSink = producerSink;
        }

        @RequestMapping
        void produce(@RequestBody String requestBody) {
            producerSink.tryEmitNext(MessageBuilder.withPayload(requestBody).build()).orThrow();
        }
    }
}
