package info.ankin.how.spring.mtls;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT24H")
class MTlsDemoApplicationTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void test_helloWorld() {
        var result = webTestClient.get().exchange().expectBody(String.class).returnResult();
        System.out.println(result);
        assertThat(result.getResponseBody(), is("world"));
    }

}
