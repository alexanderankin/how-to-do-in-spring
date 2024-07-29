package org.example.oauth.learn.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final InMemoryClientRegistrationRepository clientRegistrationRepository;

    //<editor-fold desc="webjars">
    @Bean
    WebMvcConfigurer customResourceConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/webjars/**")
                        .addResourceLocations("classpath:/META-INF/resources/webjars/");
            }
        };
    }

    @SneakyThrows
    @Bean
    @Order(-100) // higher priority
    public SecurityFilterChain webJarsLol(HttpSecurity httpSecurity) {
        httpSecurity.securityMatcher("/webjars/**");
        httpSecurity.authorizeHttpRequests(h -> h.anyRequest().permitAll());
        return httpSecurity.build();
    }
    //</editor-fold>

    @SneakyThrows
    @Bean
    @Order(0)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        http.oauth2Login(Customizer.withDefaults());
        // not needed
        // http.oidcLogout(o -> o.backChannel(Customizer.withDefaults()));
        http.logout(l -> l.logoutSuccessHandler(oidcLogoutSuccessHandler()));

        return http.build();
    }

    // copied from docs
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }
}
