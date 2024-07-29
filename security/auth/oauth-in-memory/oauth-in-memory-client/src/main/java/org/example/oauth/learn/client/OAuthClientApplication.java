package org.example.oauth.learn.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
class OAuthClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuthClientApplication.class, args);
    }

    @Controller
    static class AppController {
        @GetMapping("/")
        String home() {
            return "index.html";
        }
    }
}
