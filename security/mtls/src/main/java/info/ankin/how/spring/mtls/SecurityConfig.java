package info.ankin.how.spring.mtls;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain applicationSecurity(ServerHttpSecurity httpSecurity) {
        httpSecurity.csrf().disable();
        httpSecurity.authorizeExchange().anyExchange().permitAll();
        return httpSecurity.build();
    }
}
