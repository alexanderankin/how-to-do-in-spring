package info.ankin.how.mqtt.rpc;

import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import info.ankin.how.mqtt.rpc.model.Call;
import info.ankin.how.mqtt.rpc.model.NamedCall;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

@Slf4j
@Service
@ConditionalOnProperty(name = "how.mqtt.rpc.server.enabled")
public class RpcServer {
    private final Mqtt5RxClient client;
    private final Map<String, Call<?, ?>> calls;

    public RpcServer(Mqtt5RxClient client,
                     List<Call<?, ?>> calls) {
        this.client = client;
        this.calls = adapt(calls);
    }

    private Map<String, Call<?, ?>> adapt(List<Call<?, ?>> calls) {
        Map<String, Call<?, ?>> h = new HashMap<>();
        for (Call<?, ?> call : calls) {
            if (call instanceof NamedCall<?,?> namedCall) {
                h.put(namedCall.name(), call);
            }
        }
        return h;
    }

    @PostConstruct
    void init() {
        Mqtt5Subscription a = null;
        Mqtt5Subscription b = null;
        List<Mqtt5Subscription> list = Arrays.asList(a, b);
        client.subscribePublishes(Mqtt5Subscribe.builder()
                .addSubscriptions(list.toArray(Mqtt5Subscription[]::new))
                .build())

    }


    @Accessors(chain = true)
    @Data
    @Component
    @ConfigurationProperties("how.mqtt.rpc.server")
    public static class Props {
        boolean enabled;
    }
}
