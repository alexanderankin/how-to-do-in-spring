package org.example;

import org.springframework.boot.SpringApplication;

public class ExampleAppTestApp {
    public static void main(String[] args) {
        SpringApplication.from(ExampleAppTestApp::main)
                .with(ExampleAppTestcontainers.class)
                .run(args);
    }
}
