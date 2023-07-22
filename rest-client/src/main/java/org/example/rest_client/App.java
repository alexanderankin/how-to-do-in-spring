package org.example.rest_client;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

public class App {
    public static void main(String[] args) {
        int port = HttpServer.create()
                .route(r -> r.get("/hello", (req, res) -> res.sendString(Mono.just("world\n"))))
                .bindNow()
                .port();

        RestClient restClient = RestClient.builder()
                .requestInitializer(request -> request.getHeaders().setContentType(MediaType.APPLICATION_JSON))
                .requestInterceptor((HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> execution.execute(request, body))
                .baseUrl("http://localhost:" + port)
                .build();

        ResponseEntity<String> response = restClient.get().uri("/hello").attribute(MediaType.APPLICATION_JSON_VALUE, "abc").retrieve().toEntity(String.class);

        System.out.println(response);
    }
}
