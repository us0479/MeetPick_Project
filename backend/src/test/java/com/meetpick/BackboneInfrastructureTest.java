package com.meetpick;

import com.meetpick.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackboneInfrastructureTest extends AbstractIntegrationTest {

    private static final List<String> EXPECTED_TABLES = List.of(
            "users",
            "meeting_room",
            "participant",
            "availability",
            "time_candidate",
            "vote",
            "meeting_history",
            "notification"
    );

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    void flywayCreatesFinalBackboneSchema() {
        List<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                """,
                String.class
        );

        assertThat(tables).containsAll(EXPECTED_TABLES);
    }

    @Test
    void redisServiceConnectionWorks() {
        String pong = stringRedisTemplate.execute(
                (RedisCallback<String>) connection -> connection.ping()
        );

        assertThat(pong).isEqualTo("PONG");
    }
}
