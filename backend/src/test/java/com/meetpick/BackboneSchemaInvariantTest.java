package com.meetpick;

import com.meetpick.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackboneSchemaInvariantTest extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void relationalSchemaKeepsAgreedDomainBoundaries() {
        List<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                """,
                String.class
        );

        assertThat(tables)
                .contains("users", "meeting_room", "participant", "availability",
                        "time_candidate", "vote", "meeting_history", "notification")
                .doesNotContain("invitation", "refresh_token", "auth_session");

        List<String> meetingColumns = jdbcTemplate.queryForList(
                """
                SELECT column_name
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'meeting_room'
                """,
                String.class
        );

        assertThat(meetingColumns)
                .contains("status", "duration_minutes", "availability_deadline_at")
                .doesNotContain("host_id");
    }
}
