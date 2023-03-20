package org.example;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@EnableJdbcRepositories(considerNestedRepositories = true)
@SpringBootApplication
public class ReservedNameApplication {
    public static void main(String[] args) {
        /*
            docker run -d --name local-postgres -p 5432:5432 \
                    -e POSTGRES_HOST_AUTH_METHOD=trust \
                    postgres:15-alpine
        */
        System.setProperty("spring.datasource.url",
                "jdbc:postgresql://localhost/postgres");
        System.setProperty("spring.datasource.username", "postgres");
        SpringApplication.run(ReservedNameApplication.class, args);
    }

    @Autowired
    DataSource dataSource;

    @SneakyThrows
    @Bean
    ApplicationRunner createTables() {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("""
                        create table if not exists hero
                        (
                            id serial,
                            name varchar(100) not null unique
                        );
                        """);
                }
            }
        };
    }

    @Repository
    interface HeroRepository extends CrudRepository<Hero, Long> {
        Optional<Hero> findByName(String name);
    }

    @Accessors(chain = true)
    @Data
    @Table("hero")
    public static class Hero {
        @Id
        Long id;
        @Column("name")
        String name;
    }

    @Accessors(chain = true)
    @Data
    @Component
    @ConfigurationProperties(prefix = "hero.name-config")
    public static class HeroNameConfig {
        List<String> reservedNames = Arrays.asList("admin", "administrator");
    }

    @RequiredArgsConstructor
    @Service
    public static class HeroService {
        private final HeroRepository heroRepository;
        private final HeroNameConfig heroNameConfig;

        public Hero getHero(String name) {
            return heroRepository.findByName(name).orElse(null);
        }

        public Hero createHero(String name) {
            if (CollectionUtils.containsAny(heroNameConfig.getReservedNames(),
                    Collections.singleton(name))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "pick a different name");
            }

            return heroRepository.save(new Hero().setName(name));
        }
    }

    @RequiredArgsConstructor
    @RestController
    @RequestMapping("/api/hero")
    public static class HeroController {
        private final HeroService heroService;

        @GetMapping("/{name}")
        Hero getHero(@PathVariable String name) {
            return heroService.getHero(name);
        }

        @PostMapping("/{name}")
        Hero createHero(@PathVariable String name) {
            return heroService.createHero(name);
        }
    }
}
