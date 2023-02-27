package info.ankin.how.spring.mtls;

import com.rabbitmq.client.TrustEverythingTrustManager;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

import java.net.URI;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT24H")
class MTlsDemoApplicationTest {
    @Value("${local.server.port}") int port;
    WebTestClient webTestClient;

    @SneakyThrows
    private String url() {
        return new URI("https", null, "localhost", port, null, null, null).toString();
    }

    @SneakyThrows
    @Autowired
    public void setWebTestClient() {
        var weSend = KeyManagerFactory.getInstance("SunX509");
        weSend.init(SecurityConfig.NettyCustomizer.keyStore("/certs/localhost.crt"), new char[0]);

        var weCheckAgainst = TrustManagerFactory.getInstance("SunX509");
        weCheckAgainst.init(SecurityConfig.NettyCustomizer.keyStore("/certs/rootCA.crt"));

        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(weSend)
                .trustManager(weCheckAgainst)
                .build();

        this.webTestClient = WebTestClient
                .bindToServer(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .secure(spec -> spec.sslContext(sslContext))
                ))
                .baseUrl(url())
                .build();
    }

    @Test
    void test_helloWorld() {
        var result = webTestClient.get().uri("/").exchange().expectBody(String.class).returnResult();
        System.out.println(result);
        assertThat(result.getResponseBody(), is("world"));
    }

    @SneakyThrows
    @Test
    void test_failsWithSystemCerts() {
        WebTestClient webTestClient = WebTestClient.bindToServer(new ReactorClientHttpConnector(
                HttpClient.create()
                        .secure(SslProvider.defaultClientProvider())
                ))
                .baseUrl(url())
                .build();

        WebClientRequestException exception = assertThrows(WebClientRequestException.class, () -> webTestClient.get().uri("/").exchange());
        assertThat(exception.getMessage(), startsWith("PKIX path building failed"));
        assertThat(exception.getCause(), instanceOf(SSLHandshakeException.class));
    }

    @SneakyThrows
    @Test
    void test_failsWithArbitraryClientCerts() {
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(new TrustEverythingTrustManager())
                .build();
        WebTestClient webTestClient = WebTestClient.bindToServer(new ReactorClientHttpConnector(
                HttpClient.create()
                        .secure(spec -> spec.sslContext(sslContext))
                ))
                .baseUrl(url())
                .build();

        WebClientRequestException exception = assertThrows(WebClientRequestException.class, () -> webTestClient.get().uri("/").exchange());
        assertThat(exception.getMessage(), containsString("Received fatal alert: bad_certificate"));
        assertThat(exception.getCause(), instanceOf(DecoderException.class));
    }

}
