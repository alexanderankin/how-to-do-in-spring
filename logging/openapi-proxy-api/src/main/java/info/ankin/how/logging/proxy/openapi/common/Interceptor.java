package info.ankin.how.logging.proxy.openapi.common;

import lombok.Data;
import lombok.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Set;

public interface Interceptor {
    Set<HttpMethod> METHODS_WITH_BODY = Set.of(HttpMethod.PATCH, HttpMethod.POST, HttpMethod.PUT);

    void intercept(Request request, Response response);

    @Value
    class Request {
        String method;
        URI url;
        MultiValueMap<String, String> headers;
        String body;
    }

    @Value
    class Response {
        HttpStatus status;
        URI target;
        MultiValueMap<String, String> headers;
        String body;
    }

    @Data
    class Context {
        Request request;
        Response response;
    }
}
