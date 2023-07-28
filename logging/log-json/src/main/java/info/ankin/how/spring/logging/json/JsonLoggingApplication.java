package info.ankin.how.spring.logging.json;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
class JsonLoggingApplication {
    /**
     * prints:
     * <pre>
     * {"@timestamp":"2023-07-28T06:35:48.591726992-04:00","message":"Starting JsonLoggingApplication using Java $version with PID $PID (./how-to-do-in-spring/logging/log-json/build/classes/java/main started by $USER in ./how-to-do-in-spring)","logger_name":"info.ankin.how.spring.logging.json.JsonLoggingApplication","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:48.598294822-04:00","message":"No active profile set, falling back to 1 default profile: \"default\"","logger_name":"info.ankin.how.spring.logging.json.JsonLoggingApplication","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:48.973176435-04:00","message":"Tomcat initialized with port(s): 8080 (http)","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:48.97767306-04:00","message":"Starting service [Tomcat]","logger_name":"org.apache.catalina.core.StandardService","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:48.977822891-04:00","message":"Starting Servlet engine: [Apache Tomcat/10.1.11]","logger_name":"org.apache.catalina.core.StandardEngine","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:49.01490109-04:00","message":"Initializing Spring embedded WebApplicationContext","logger_name":"org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/]","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:49.015722733-04:00","message":"Root WebApplicationContext: initialization completed in 395 ms","logger_name":"org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:49.194594917-04:00","message":"Tomcat started on port(s): 8080 (http) with context path ''","logger_name":"org.springframework.boot.web.embedded.tomcat.TomcatWebServer","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:49.19916845-04:00","message":"Started JsonLoggingApplication in 0.876 seconds (process running for 1.115)","logger_name":"info.ankin.how.spring.logging.json.JsonLoggingApplication","thread_name":"main","level":"INFO","level_value":20000}
     * {"@timestamp":"2023-07-28T06:35:49.200139142-04:00","message":"hi from JsonLoggingApplication","logger_name":"info.ankin.how.spring.logging.json.JsonLoggingApplication","thread_name":"main","level":"INFO","level_value":20000}
     * </pre>
     */
    public static void main(String[] args) {
        System.setProperty("spring.main.banner-mode", "off");
        SpringApplication.run(JsonLoggingApplication.class, args);
        log.info("hi from JsonLoggingApplication");
    }
}
