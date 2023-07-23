package info.ankin.how.logging.proxy.openapi.webflux;

import info.ankin.how.logging.proxy.openapi.common.Interceptor;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

public class WebfluxInterceptingProxy extends WebfluxProxy {
    private final List<WebfluxProxyInterceptor> interceptors;

    public WebfluxInterceptingProxy(WebClient.Builder builder,
                                    List<WebfluxProxyInterceptor> interceptors) {
        super(builder);
        this.interceptors = interceptors;
    }

    private static Interceptor.Request getRequest(ServerRequest request, String v) {
        return new Interceptor.Request(request.methodName(), request.uri(), request.headers().asHttpHeaders(), v);
    }

    private static Interceptor.Response getResponse(ClientResponse response, String v) {
        return new Interceptor.Response(response.statusCode(), null, response.headers().asHttpHeaders(), v);
    }

    @Override
    protected ServerRequest init(ServerRequest serverRequest) {
        var attributes = serverRequest.attributes();
        if (!attributes.containsKey(WebfluxInterceptingProxy.class.getName())) {
            attributes.put(WebfluxInterceptingProxy.class.getName(), new Interceptor.Context());
        }

        return serverRequest;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest rawRequest) {
        ServerRequest init = init(rawRequest);
        Interceptor.Context context = (Interceptor.Context) init.attribute(WebfluxInterceptingProxy.class.getName()).orElseThrow();
        return super.handle(init)
                .flatMap(this::afterHandle)
                .contextWrite(Context.of(Interceptor.Context.class, context));
    }

    private Mono<ServerResponse> afterHandle(ServerResponse serverResponse) {
        return Mono.deferContextual(Mono::just)
                .map(c -> c.get(Interceptor.Context.class))
                .flatMapMany(context ->
                        Flux.fromIterable(interceptors)
                                .flatMap(i -> i.intercept(
                                        context.getRequest(),
                                        context.getResponse()
                                )))
                .then()
                .thenReturn(serverResponse);
    }

    @Override
    protected WebClient.RequestHeadersSpec<?> sendBodyFromClientToTargetIfBodyMethod(
            ServerRequest request,
            HttpMethod method,
            WebClient.RequestBodySpec bodySpec
    ) {
        // same as usual, but pull out context first
        Interceptor.Context context =
                request.attribute(WebfluxInterceptingProxy.class.getName())
                        .map(Interceptor.Context.class::cast)
                        .orElseThrow();

        WebClient.RequestHeadersSpec<?> headersSpec;
        if (Interceptor.METHODS_WITH_BODY.contains(method)) {
            Mono<String> stringMono = request.bodyToMono(String.class);

            // intercept the string mono
            stringMono = stringMono.doOnNext(v -> context.setRequest(getRequest(request, v)));

            headersSpec = bodySpec.body(stringMono, String.class);
        } else {
            headersSpec = bodySpec;

            // note absence of body value
            context.setRequest(getRequest(request, null));
        }

        return headersSpec;
    }

    @Override
    protected Mono<ServerResponse> sendBodyFromTargetToClient(
            ServerRequest request,
            ClientResponse c,
            ServerResponse.BodyBuilder sRBB
    ) {
        Interceptor.Context context =
                request.attribute(WebfluxInterceptingProxy.class.getName())
                        .map(Interceptor.Context.class::cast)
                        .orElseThrow();

        Mono<String> stringMono = c.bodyToMono(String.class)
                .doOnNext(v -> context.setResponse(getResponse(c, v)));

        return sRBB.body(stringMono, String.class);
    }
}
