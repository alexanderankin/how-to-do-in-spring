package info.ankin.how.logging.proxy.openapi.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("openapi-proxy")
public class OpenApiProxyProperties {
}
