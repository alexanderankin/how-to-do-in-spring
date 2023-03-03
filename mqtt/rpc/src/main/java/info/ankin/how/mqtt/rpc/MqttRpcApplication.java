package info.ankin.how.mqtt.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import io.reactivex.Flowable;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class MqttRpcApplication {
    @Autowired
    ObjectMapper objectMapper;

    public static void main(String[] args) {
        // docker run -it --rm --name hive -p 1883:1883 -p 8883:8883 hivemq/hivemq-ce:2021.3
        SpringApplication.run(MqttRpcApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(Mqtt5RxClient mqtt5RxClient) {
        return args -> {
            mqtt5RxClient.connect()
                    .doOnSuccess(ack -> System.out.println("publisher connected"))
                    .toFlowable()
                    .flatMap(ack -> mqtt5RxClient.publish(Flowable.interval(1, TimeUnit.SECONDS)
                            .take(10)
                            .map(counter -> Mqtt5Publish.builder()
                                    .topic("some-topic")
                                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .payload(getString("abc is now " + counter)
                                            .getBytes(StandardCharsets.UTF_8))
                                    .build())))
                    .doOnEach(mqtt5PublishResultNotification -> {
                        System.out.println("published: " + mqtt5PublishResultNotification);
                    })
                    .subscribe();
        };
    }

    @Bean
    ApplicationRunner client(Mqtt5RxClient mqtt5RxClient) {
        return args -> {
            mqtt5RxClient.connect()
                    .doOnSuccess(mqtt5ConnAck -> System.out.println("client connection acknowledged"))
                    .toFlowable()
                    .flatMap(ack -> mqtt5RxClient.subscribePublishes(Mqtt5Subscribe.builder().topicFilter("#").build()))
                    .doOnEach(sub -> System.out.println("got a new message:"))
                    .doOnEach(pub -> {
                        if (pub.getValue() != null) {
                            System.out.println(
                                    objectMapper.readTree(new String(pub.getValue().getPayloadAsBytes(), StandardCharsets.UTF_8))
                                            .path("value").asText());
                        } else System.out.println("pub with no value: " + pub);
                    })
                    .map(Optional::of)
                    .onErrorReturnItem(Optional.empty())
                    .subscribe()
            ;
        };
    }

    @SneakyThrows
    private String getString(String value) {
        return objectMapper.writeValueAsString(Map.of("value", value));
    }
}
