package org.example.oauth.learn.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication
class OAuthClientApplication {
    public static void main(String[] args) {
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
