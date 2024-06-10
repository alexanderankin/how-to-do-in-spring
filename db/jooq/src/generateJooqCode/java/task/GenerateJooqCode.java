package task;

import lombok.SneakyThrows;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

public class GenerateJooqCode {
    @SneakyThrows
    public static void main(String[] args) {
        String destination = System.getProperty("destination");
        Objects.requireNonNull(destination);

        try (PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:16-alpine")
        )) {
            postgreSQLContainer
                    // needs this role for schema?
                    .withUsername("postgres")
                    .withDatabaseName("postgres");

            if (System.getProperty("schema") != null) {
                postgreSQLContainer.withFileSystemBind(System.getProperty("schema"), "/docker-entrypoint-initdb.d/schema.sql");
            }

            postgreSQLContainer.start();

            GenerationTool.generate(new Configuration()
                    .withJdbc(new Jdbc()
                            .withUrl(postgreSQLContainer.getJdbcUrl())
                            .withUsername(postgreSQLContainer.getUsername())
                            .withPassword(postgreSQLContainer.getPassword()))
                    .withGenerator(new Generator()
                            .withDatabase(new Database()
                                    .withInputSchema("public"))
                            .withTarget(new Target()
                                    .withPackageName("org.example.db.pkg")
                                    .withDirectory(destination)))
            );
        }
    }
}
