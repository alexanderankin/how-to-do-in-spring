package info.ankin.how.spring.mtls;

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
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT24H")
class MTlsDemoApplicationTest {
    WebTestClient webTestClient;

    @SneakyThrows
    @Autowired
    public void setWebTestClient(@Value("${local.server.port}") int port) {
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
                .baseUrl(new URI("https", null, "localhost", port, null, null, null).toString())
                .build();
    }

    @Test
    void test_helloWorld() {
        var result = webTestClient.get().uri("/").exchange().expectBody(String.class).returnResult();
        System.out.println(result);
        assertThat(result.getResponseBody(), is("world"));
    }

}
