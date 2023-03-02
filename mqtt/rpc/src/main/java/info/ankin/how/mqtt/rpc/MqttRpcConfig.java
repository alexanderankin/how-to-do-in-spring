package info.ankin.how.mqtt.rpc;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Slf4j
@Configuration
public class MqttRpcConfig {
    static final String NO_PORT = "can't determine connection when both scheme and port missing: ";

    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    Mqtt5RxClient mqtt5RxClient(Props props) {
        Map.Entry<Integer, Props.Scheme> portAndScheme = figureOutPort(props);

        Mqtt5ClientBuilder builder = Mqtt5Client.builder()
                .serverHost(props.getConnectionString().getHost())
                .serverPort(portAndScheme.getKey());

        if (portAndScheme.getValue() == Props.Scheme.MQTTS)
            builder = builder.sslWithDefaultConfig();

        return builder.buildRx();
    }

    private Map.Entry<Integer, Props.Scheme> figureOutPort(Props props) {
        int givenPort = props.getConnectionString().getPort();
        String givenScheme = props.getConnectionString().getScheme();
        Map.Entry<Integer, Props.Scheme> portAndScheme;

        if (givenPort == -1) {
            if (givenScheme != null) {
                var scheme = Props.Scheme.valueOf(givenScheme.toUpperCase());
                portAndScheme = Map.entry(scheme.getPort(), scheme);
            } else {
                throw new IllegalArgumentException(NO_PORT + props);
            }
        } else {
            Props.Scheme shouldBeScheme = null;
            for (Props.Scheme s : Props.Scheme.values()) {
                if (s.getPort() == givenPort) {
                    shouldBeScheme = s;
                    break;
                }
            }

            if (shouldBeScheme != null) {
                portAndScheme = Map.entry(givenPort, shouldBeScheme);
            } else {
                throw new IllegalArgumentException(NO_PORT + props);
            }
        }

        return portAndScheme;
    }

    @Accessors(chain = true)
    @Data
    @Component
    @Validated
    @ConfigurationProperties("how.mqtt.rpc")
    public static class Props {
        Mode mode;

        @ToString.Exclude
        @NotNull
        URI connectionString = URI.create("mqtt://localhost");

        @ToString.Include
        public String safeConnectionString() {
            URI u = connectionString;
            try {
                return new URI(u.getScheme(), null, u.getHost(), u.getPort(), null, null, null).toString();
            } catch (Exception e) {
                return e.getClass().getSimpleName();
            }
        }

        public enum Mode {CLIENT, SERVER,}

        @Getter
        @RequiredArgsConstructor
        public enum Scheme {
            MQTT(1883),
            @SuppressWarnings("SpellCheckingInspection")
            MQTTS(8883),
            ;

            private final int port;
        }
    }
}
