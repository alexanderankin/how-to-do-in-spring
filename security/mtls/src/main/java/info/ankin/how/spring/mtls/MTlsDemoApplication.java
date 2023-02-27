package info.ankin.how.spring.mtls;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
public class MTlsDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MTlsDemoApplication.class, args);
    }

    @RestController
    @RequestMapping
    public static class Ctrl {
        @GetMapping
        String hi(@RequestParam(defaultValue = "true") boolean defaultGreeting,
                  Principal principal) {
            if (!defaultGreeting) return "hello, " + principal.getName();
            return "world";
        }
    }
}
