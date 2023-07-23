package info.ankin.how.logging.proxy.openapi.common;

import org.springframework.http.HttpMethod;

import java.util.Optional;

public class NoSuchMethodException extends RuntimeException {
    private NoSuchMethodException() {
        super("no such method - custom methods are not supported");
    }

    public static HttpMethod httpMethod(String methodName) {
        return Optional.ofNullable(HttpMethod.resolve(methodName)).orElseThrow(NoSuchMethodException::new);
    }
}
