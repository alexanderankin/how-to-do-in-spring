package info.ankin.how.logging.proxy.openapi.gateway;

import lombok.Value;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.cloud.gateway.filter.WebClientHttpRoutingFilter;
import org.springframework.cloud.gateway.filter.WebClientWriteResponseFilter;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.HttpWebHandlerAdapter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.nio.charset.StandardCharsets;
import java.util.List;

class OpenApiGatewayConfigTest {
    public static void main(String[] args) {
        int port = HttpServer.create()
                .route(r -> r.get("/hello", (req, res) -> res.sendString(Mono.just("world\n"))))
                .bindNow()
                .port();
        String baseUrl = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(port).toUriString();

        var ctx = new ReactiveWebServerApplicationContext();
        {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(NettyReactiveWebServerFactory.class);
            ctx.getDefaultListableBeanFactory().registerBeanDefinition("rWSF", beanDefinition);
        }

        WebHandler handler = /*new MyWebHandler();*/
                new FilteringWebHandler(List.of(
                        new WebClientHttpRoutingFilter(WebClient.builder().baseUrl(baseUrl).build(), new StaticObjectProvider<>(List.of())),
                        new WebClientWriteResponseFilter()
                ));

        // still not working b/c missing the gateway route attribute, see:
        // org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping

        HttpWebHandlerAdapter adapter = new HttpWebHandlerAdapter(handler);
        ctx.getBeanFactory().registerSingleton("customWebHandler", adapter);

        ctx.refresh();
    }

    public static class MyHttpHandler extends HttpWebHandlerAdapter {
        public MyHttpHandler() {
            super(new MyWebHandler());
        }
    }

    public static class MyWebHandler implements WebHandler {
        static final String GREETING = "hello" + System.lineSeparator();

        @Override
        @NonNull
        public Mono<Void> handle(ServerWebExchange exchange) {
            ServerHttpResponse res = exchange.getResponse();
            res.setStatusCode(HttpStatus.OK);
            var hello = Mono.just(DefaultDataBufferFactory.sharedInstance.wrap(GREETING.getBytes(StandardCharsets.UTF_8)));
            return res.writeWith(hello);
        }
    }

    @Value
    private static class StaticObjectProvider<T> implements ObjectProvider<T> {
        T t;

        @Override
        @NonNull
        public T getObject(@NonNull Object... args) throws BeansException {
            return t;
        }

        @Override
        public T getIfAvailable() throws BeansException {
            return t;
        }

        @Override
        public T getIfUnique() throws BeansException {
            return t;
        }

        @Override
        @NonNull
        public T getObject() throws BeansException {
            return t;
        }
    }
}
