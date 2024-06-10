package org.example;

import org.example.db.pkg.tables.records.ActorRecord;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.db.pkg.tables.Actor.ACTOR;

@SpringBootTest
@Import(ExampleAppTestcontainers.class)
class ExampleAppTests {
    @Autowired
    JdbcClient jdbcClient;
    @Autowired
    private DefaultDSLContext dslContext;

    @Test
    void test() {
        dslContext.insertInto(ACTOR)
                .columns(ACTOR.FIRST_NAME, ACTOR.LAST_NAME, ACTOR.LAST_UPDATE)
                .values("abc", "def", LocalDateTime.now())
                .execute();

        SelectConditionStep<ActorRecord> abc = dslContext.selectFrom(ACTOR).where(ACTOR.FIRST_NAME.eq("abc"));
        // List<ActorRecord> list = reactor.core.publisher.Flux.from(abc).collectList().toFuture().join();
        List<ActorRecord> list = abc.fetch();
        System.out.println(list);
    }
}
