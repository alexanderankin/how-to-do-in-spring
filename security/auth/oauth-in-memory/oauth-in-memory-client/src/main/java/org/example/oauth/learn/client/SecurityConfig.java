package org.example.oauth.learn.client;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
    @Order(-100)
    public SecurityFilterChain webJarsLol(HttpSecurity httpSecurity) {
        httpSecurity.securityMatcher("/webjars/**");
        httpSecurity.authorizeHttpRequests(h -> h.anyRequest().permitAll());
        return httpSecurity.build();
    }
    //</editor-fold>

    @SneakyThrows
    @Bean
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        http.oauth2Login(Customizer.withDefaults());

        return http.build();
    }

}
