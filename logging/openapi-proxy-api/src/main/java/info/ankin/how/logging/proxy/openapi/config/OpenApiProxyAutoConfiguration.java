package info.ankin.how.logging.proxy.openapi.config;

import info.ankin.how.logging.proxy.openapi.common.OpenApiProxyProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackageClasses = OpenApiProxyProperties.class)
public class OpenApiProxyAutoConfiguration {
}
