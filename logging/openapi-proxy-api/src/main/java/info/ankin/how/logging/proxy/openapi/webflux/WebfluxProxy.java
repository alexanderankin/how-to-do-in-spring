package info.ankin.how.logging.proxy.openapi.webflux;

import info.ankin.how.logging.proxy.openapi.common.Interceptor;
import info.ankin.how.logging.proxy.openapi.common.NoSuchMethodException;
import lombok.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

public class WebfluxProxy implements HandlerFunction<ServerResponse> {
    private final WebClient webClient;

    public WebfluxProxy(WebClient.Builder builder) {
        webClient = builder.build();
    }

    protected ServerRequest init(ServerRequest serverRequest) {
        return serverRequest;
    }

    @Override
    @NonNull
    public Mono<ServerResponse> handle(@NonNull ServerRequest rawRequest) {
        var request = init(rawRequest);
        HttpMethod method = NoSuchMethodException.httpMethod(request.methodName());

        WebClient.RequestBodySpec bodySpec = webClient.method(method)
                .uri(new CopyBuilder(request.uri()))
                .headers(new CopyHeaders(request.headers().asHttpHeaders()));

        WebClient.RequestHeadersSpec<?> headersSpec = sendBodyFromClientToTargetIfBodyMethod(request, method, bodySpec);

        return headersSpec.exchangeToMono(c -> {
            ClientResponse.Headers headers = c.headers();
            HttpStatus httpStatus = c.statusCode();

            ServerResponse.BodyBuilder sRBB = ServerResponse.status(httpStatus)
                    .headers(h -> h.addAll(headers.asHttpHeaders()));
            return sendBodyFromTargetToClient(request, c, sRBB);
        });
    }

    protected WebClient.RequestHeadersSpec<?> sendBodyFromClientToTargetIfBodyMethod(
            ServerRequest request,
            HttpMethod method,
            WebClient.RequestBodySpec bodySpec
    ) {
        WebClient.RequestHeadersSpec<?> headersSpec;
        if (Interceptor.METHODS_WITH_BODY.contains(method)) {
            Flux<DataBuffer> body = request.body(BodyExtractors.toDataBuffers());
            headersSpec = bodySpec.body(body, DataBuffer.class);
        } else {
            headersSpec = bodySpec;
        }
        return headersSpec;
    }

    protected Mono<ServerResponse> sendBodyFromTargetToClient(
            ServerRequest request,
            ClientResponse c,
            ServerResponse.BodyBuilder sRBB
    ) {
        return sRBB.body(c.bodyToFlux(DataBuffer.class), DataBuffer.class);
    }

    @Value
    private static class CopyBuilder implements Function<UriBuilder, URI> {
        URI uri;

        @Override
        public URI apply(UriBuilder builder) {
            builder
                    .path(uri.getPath())
                    .query(uri.getQuery())
            ;

            return builder.build();
        }
    }

    @Value
    private static class CopyHeaders implements Consumer<HttpHeaders> {
        HttpHeaders values;

        @Override
        public void accept(HttpHeaders h) {
            h.addAll(values);
        }
    }
}
