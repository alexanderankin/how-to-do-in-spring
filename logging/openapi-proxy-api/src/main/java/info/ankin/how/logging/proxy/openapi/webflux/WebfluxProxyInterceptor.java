package info.ankin.how.logging.proxy.openapi.webflux;

import info.ankin.how.logging.proxy.openapi.common.Interceptor;
import lombok.Value;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public interface WebfluxProxyInterceptor {
    Mono<Void> intercept(Interceptor.Request request, Interceptor.Response response);

    @Value
    class Adapter implements WebfluxProxyInterceptor {
        Interceptor interceptor;

        @Override
        public Mono<Void> intercept(Interceptor.Request request, Interceptor.Response response) {
            return Mono.<Void>fromRunnable(() -> interceptor.intercept(request, response))
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }
}
