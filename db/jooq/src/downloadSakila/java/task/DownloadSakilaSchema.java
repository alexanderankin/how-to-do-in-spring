package task;

import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadSakilaSchema {
    @SneakyThrows
    public static void main(String[] args) {
        URI uri = URI.create("https://github.com/jOOQ/sakila/raw/main/postgres-sakila-db/postgres-sakila-schema.sql");
        try (HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()) {
            var send = httpClient.send(HttpRequest.newBuilder().GET().uri(uri).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Files.writeString(Path.of(System.getProperty("destination")), send.body());
        }
    }
}
