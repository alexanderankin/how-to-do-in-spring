package info.ankin.how.spring.logging.json;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
class JsonLoggingApplication {
    public static void main(String[] args) {
        System.setProperty("spring.main.banner-mode", "off");
        SpringApplication.run(JsonLoggingApplication.class, args);
        log.info("hi from JsonLoggingApplication");
    }
}
