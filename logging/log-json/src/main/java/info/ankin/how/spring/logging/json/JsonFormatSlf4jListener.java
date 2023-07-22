package info.ankin.how.spring.logging.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.composite.LogstashVersionJsonProvider;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Set;
import java.util.stream.StreamSupport;

@Slf4j
class JsonFormatSlf4jListener implements SpringApplicationRunListener {
    static final Set<Class<? extends Encoder<?>>> KNOWN_JSON_ENCODERS = Set.of(
            LogstashEncoder.class
    );

    @SuppressWarnings("unused") // see parent class documentation
    JsonFormatSlf4jListener(SpringApplication springApplication, String[] args) {
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
                                    ConfigurableEnvironment environment) {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();

        if (!(iLoggerFactory instanceof LoggerContext loggerContext)) {
            System.err.println("logger factory is not a (logback) logger context");
            return;
        }

        Iterable<Appender<ILoggingEvent>> appenderList =
                loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)::iteratorForAppenders;

        var consoleAppenderList = StreamSupport.stream(appenderList.spliterator(), false)
                .filter(ConsoleAppender.class::isInstance)
                .map(e -> (ConsoleAppender<ILoggingEvent>) e)
                .toList();

        if (consoleAppenderList.isEmpty()) {
            log.debug("no console appender to modify, returning");
            return;
        }

        var encoderList = consoleAppenderList.stream()
                .map(OutputStreamAppender::getEncoder)
                .filter(e -> KNOWN_JSON_ENCODERS.stream().noneMatch(c -> c.isInstance(e)))
                .toList();

        if (encoderList.isEmpty()) {
            log.debug("all encoders already KNOWN_JSON_ENCODERS (%s), returning".formatted(KNOWN_JSON_ENCODERS));
            return;
        }

        LogstashEncoder logstashEncoder = new LogstashEncoder();

        // example of removing a default provider:
        logstashEncoder.getProviders().getProviders().stream()
                .filter(LogstashVersionJsonProvider.class::isInstance).findAny()
                .ifPresent(logstashEncoder.getProviders()::removeProvider);

        // don't forget to start logback components
        logstashEncoder.start();

        int fixed = 0;
        for (ConsoleAppender<ILoggingEvent> consoleAppender : consoleAppenderList) {
            if (encoderList.contains(consoleAppender.getEncoder())) {
                consoleAppender.setEncoder(logstashEncoder);
                fixed += 1;
            }
        }

        log.debug("fixed {} encoders on root's {} console appenders", fixed, consoleAppenderList.size());
    }
}
