package org.example;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
class EApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EApp.class)
                .properties(Map.ofEntries(
                        Map.entry("spring.datasource.url", "jdbc:oracle:thin:@localhost:1521:xe"),
                        Map.entry("spring.datasource.username", "system"),
                        Map.entry("spring.datasource.password", "password")
                ))
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @SneakyThrows
    @Autowired
    void initializer(DataSource datasource) {
        JdbcClient jdbcClient = JdbcClient.create(datasource);

        record Table(String tableName) {
        }
        var tableNames = jdbcClient
                // language=sql
                .sql("select TABLE_NAME from all_tables")
                .query(Table.class).stream()
                .map(Table::tableName)
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        if (!tableNames.contains("abc")) {
            DatabasePopulatorUtils.execute(connection -> {
                connection.prepareStatement("""
                        create table abc(id int)
                        """).execute();
            }, datasource);
        }
    }

    @AllArgsConstructor
    @Component
    static class Runner implements ApplicationRunner {
        DataSource dataSource;
        JdbcTemplate jdbcTemplate;
        JdbcAggregateTemplate jdbcAggregateTemplate;

        @Override
        public void run(ApplicationArguments args) {
            log.info("{}", jdbcTemplate.query("select * from abc", new ColumnMapRowMapper()));
            log.info("{}", jdbcTemplate.update("insert into abc(id) values (?)", 2));
        }
    }
}
