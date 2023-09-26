package info.ankin.how.spring.hateoas.webclient.configurer;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@EnableJdbcRepositories(considerNestedRepositories = true)
@SpringBootApplication
class HalClientDemoApplication {
    public static void main(String[] args) {
        System.setProperty("spring.datasource.url",
                "jdbc:h2:mem:hal_client;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;");
        SpringApplication.run(HalClientDemoApplication.class, args);
    }

    @SneakyThrows
    @Autowired
    void migrations(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        create table example
                        (
                            id   serial primary key,
                            name varchar(500) not null unique
                        );
                        """);
                statement.execute("""
                                                
                        create table tag
                        (
                            id          serial primary key,
                            name        varchar(50) not null unique,
                            description text        null
                        );
                        """);
                statement.execute("""
                        create table example_tag
                        (
                            example_id int not null references example (id) on delete cascade,
                            tag_id     int not null references tag (id) on delete cascade,
                            unique (example_id, tag_id)
                        );
                        """);
            }
        }
    }

    @NoRepositoryBean
    interface BaseRepository<T, ID>
            extends ListCrudRepository<T, ID>, PagingAndSortingRepository<T, ID> {
    }

    @Repository
    @RepositoryRestResource
    interface ExampleRepository extends BaseRepository<Example, Integer> {
    }

    @Repository
    @RepositoryRestResource
    interface TagRepository extends BaseRepository<Tag, Integer> {
    }

    @Data
    @Table("example")
    static class Example {
        int id;
        String name;
    }

    @Data
    @Table("example")
    static class Tag {
        int id;
        String name;
        String description;
    }


}
