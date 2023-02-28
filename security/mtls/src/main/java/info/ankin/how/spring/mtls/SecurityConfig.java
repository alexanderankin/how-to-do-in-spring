package info.ankin.how.spring.mtls;

import lombok.SneakyThrows;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ReactivePreAuthenticatedAuthenticationManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Objects;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain applicationSecurity(ServerHttpSecurity httpSecurity,
                                               ReactiveUserDetailsService certUds) {
        httpSecurity.csrf().disable();
        httpSecurity.authorizeExchange().anyExchange().permitAll();
        httpSecurity.x509().authenticationManager(new ReactivePreAuthenticatedAuthenticationManager(certUds));
        return httpSecurity.build();
    }

    @Bean
    ReactiveUserDetailsService certUds() {
        return username -> Mono.just(User.builder().username(username).password(username).authorities("CERTIFICATE").build());
    }

    @Component
    public static class NettyCustomizer
            implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

        @Override
        public void customize(NettyReactiveWebServerFactory factory) {
            Ssl ssl = new Ssl();
            ssl.setEnabled(true);
            // require client sending their cert
            ssl.setClientAuth(Ssl.ClientAuth.NEED);
            factory.setSslStoreProvider(new SslStoreProvider() {
                @SneakyThrows
                @Override
                public KeyStore getKeyStore() {
                    // not sure why this has to be here
                    return clientKeyStore();
                }

                @Override
                public KeyStore getTrustStore() {
                    return serverKeyStore();
                }

                @Override
                public String getKeyPassword() {
                    // https://stackoverflow.com/a/72580366
                    return "";
                }
            });
            factory.setSsl(ssl);
        }

        private KeyStore serverKeyStore() {
            return keyStore("/certs/rootCA.crt", KeyStoreConfig.KEY_ENTRY);
        }

        private KeyStore clientKeyStore() {
            return keyStore("/certs/localhost.crt", KeyStoreConfig.KEY_ENTRY);
        }

        public static KeyStore keyStore(String name) {
            return keyStore(name, KeyStoreConfig.BOTH);
        }

        @SneakyThrows
        public static KeyStore keyStore(String name, KeyStoreConfig config) {
            // set up the store
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null);

            // parse the x509 and save it into store
            Certificate cert = CertificateFactory.getInstance("X.509")
                    .generateCertificates(SecurityConfig.class.getResourceAsStream(name))
                    .iterator().next();
            if (config.cert()) keyStore.setCertificateEntry("cert", cert);

            // parse the key and save it into the store
            PEMParser pemParser = new PEMParser(new InputStreamReader(Objects.requireNonNull(
                    SecurityConfig.class.getResourceAsStream(name.replace(".crt", ".key")))));
            PrivateKey privateKey = new JcaPEMKeyConverter()
                    .getPrivateKey((PrivateKeyInfo) pemParser.readObject());
            if (config.key()) keyStore.setKeyEntry("cert", privateKey, new char[0], new Certificate[]{cert});

            return keyStore;
        }

        public record KeyStoreConfig(boolean cert, boolean key) {
            public static final KeyStoreConfig BOTH = new KeyStoreConfig(true, true);
            public static final KeyStoreConfig KEY_ENTRY = new KeyStoreConfig(false, true);
            public static final KeyStoreConfig CERT = new KeyStoreConfig(true, false);
        }
    }
}
