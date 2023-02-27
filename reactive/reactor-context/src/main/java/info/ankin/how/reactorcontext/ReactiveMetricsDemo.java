package info.ankin.how.reactorcontext;

import lombok.SneakyThrows;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class ReactiveMetricsDemo {
    public static void main(String[] args) {
        new Demo().method();
    }

    static class Demo {
        private Context writeToContext(Context c) {
            return c.put("start", System.currentTimeMillis());
        }

        @SneakyThrows
        private String call() {
            Thread.sleep(1000);
            return "abc";
        }

        void method() {
            Mono<String> stringMono = Mono.fromCallable(this::call);

            Mono<Long> time = stringMono
                    .flatMap(str -> Mono.deferContextual(Mono::just).doOnNext(System.out::println))
                    .contextWrite(this::writeToContext)
                    .map(cv ->
                            System.currentTimeMillis() - ((long) cv.get("start")))
                    .contextWrite(c -> c.put("start", """
                            this value is not used - mono's are immutable :)
                            
                            if this context#put call were to affect the above map statement,
                            it would imply that Mono's keep mutable state (they don't)
                            """))
                    ;

            Long block = time.block();
            System.out.println("result: " + block);
        }
    }

}
