package info.ankin.how.logging.proxy.openapi.web;

import info.ankin.how.logging.proxy.openapi.common.NoSuchMethodException;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static info.ankin.how.logging.proxy.openapi.common.Interceptor.METHODS_WITH_BODY;
import static org.springframework.util.StringUtils.hasText;


public class WebProxy implements HandlerFunction<ServerResponse> {
    RestTemplate restTemplate;

    @NonNull
    @Override
    public ServerResponse handle(@NonNull ServerRequest request) throws Exception {
        HttpMethod method = NoSuchMethodException.httpMethod(request.methodName());

        RequestEntity.BodyBuilder reqBb =
                RequestEntity
                        .method(method, template(request))
                        .headers(h -> h.putAll(request.headers().asHttpHeaders()));

        RequestEntity<?> req = METHODS_WITH_BODY.contains(request.method())
                ? reqBb.body(request.body(String.class), String.class)
                : reqBb.build();

        ResponseEntity<String> res = restTemplate.exchange(req, String.class);
        ServerResponse.BodyBuilder serverBb =
                ServerResponse
                        .status(res.getStatusCode())
                        .headers(h -> h.putAll(res.getHeaders()))
                ;

        String body = res.getBody();
        if (body != null) return serverBb.body(body);
        return serverBb.build();
    }

    private String template(ServerRequest request) {
        UriComponents components = UriComponentsBuilder.fromUri(request.uri()).build();

        return request.path() + (hasText(components.getQuery()) ? "?" + components.getQuery() : "");
    }
}
