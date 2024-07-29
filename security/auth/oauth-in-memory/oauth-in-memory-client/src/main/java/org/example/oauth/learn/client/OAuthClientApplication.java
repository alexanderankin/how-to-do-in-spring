package org.example.oauth.learn.client;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
class OAuthClientApplication {
    public static void main(String[] args) {
        Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, () -> RestClient.create().get().uri("http://localhost:9000/.well-known/openid-configuration").retrieve());
        SpringApplication.run(OAuthClientApplication.class, args);
    }

    @Controller
    static class AppController {
        @GetMapping("/")
        ModelAndView home(@AuthenticationPrincipal OidcUser user) {
            System.out.println(user);
            return new ModelAndView("index", "user", new Model(user));
        }

        record Model(OidcUser oidcUser, String sub) {
            Model(OidcUser oidcUser) {
                this(oidcUser, oidcUser.getSubject());
            }
        }
    }
}
