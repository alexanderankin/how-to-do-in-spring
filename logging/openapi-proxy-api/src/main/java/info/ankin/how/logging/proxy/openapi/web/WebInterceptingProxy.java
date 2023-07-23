package info.ankin.how.logging.proxy.openapi.web;

import info.ankin.how.logging.proxy.openapi.common.Interceptor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

public class WebInterceptingProxy implements HandlerFunction<ServerResponse> {
    WebProxy webProxy;
    Interceptor interceptor;

    @Override
    @NonNull
    public ServerResponse handle(@NonNull ServerRequest request) throws Exception {
        ServerResponse response = webProxy.handle(request);

        interceptor.intercept(
                new Interceptor.Request(request.methodName(), request.uri(), request.headers().asHttpHeaders(), request.body(String.class)),
                // have to figure out target and body still - neither is obvious and indicates arch issue
                new Interceptor.Response(response.statusCode(), null, response.headers(), null)
        );

        return response;
    }
}
