package info.ankin.how.spring.logging.correlation;

import brave.Tracing;
import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
class LogCorrelationApp {
    public static void main(String[] args) {
        // https://github.com/micrometer-metrics/tracing/wiki/Spring-Cloud-Sleuth-3.1-Migration-Guide#log-pattern
        System.setProperty("logging.pattern.level",
                "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]");
        System.setProperty("logging.level.web", "debug");

        SpringApplication.run(LogCorrelationApp.class, args);
    }

    @Slf4j
    @RestController
    static class Controller {
        @GetMapping
        String hi() {
            log.info("hi");
            log.info("ok");
            log.info("bye");
            return "hello";
        }
    }
}
