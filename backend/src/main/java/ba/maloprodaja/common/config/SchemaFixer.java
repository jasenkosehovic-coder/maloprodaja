package ba.maloprodaja.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs before DataInitializer (Order 1) to fix any Hibernate-generated
 * CHECK constraints on korisnici.uloga that don't include all enum values.
 * This is needed because Flyway is disabled in dev and ddl-auto=update
 * may have created the constraint before SUPER_ADMIN was added.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaFixer implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        dropUlogaCheckConstraints();
    }

    private void dropUlogaCheckConstraints() {
        List<String> constraints = jdbc.queryForList(
                """
                SELECT con.conname
                FROM pg_constraint con
                JOIN pg_class rel ON rel.oid = con.conrelid
                JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey)
                WHERE rel.relname = 'korisnici'
                  AND con.contype = 'c'
                  AND att.attname = 'uloga'
                """,
                String.class
        );

        for (String name : constraints) {
            log.info("Dropping stale uloga check constraint: {}", name);
            jdbc.execute("ALTER TABLE korisnici DROP CONSTRAINT IF EXISTS \"" + name + "\"");
        }

        if (!constraints.isEmpty()) {
            log.info("Dropped {} uloga check constraint(s). Hibernate ddl-auto=update will not recreate them " +
                     "because columnDefinition is set on the uloga field.", constraints.size());
        }
    }
}
