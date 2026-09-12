package com.meetpick;

import com.meetpick.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackboneConstraintTest extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void agreedUniqueConstraintsExist() {
        List<String> constraintNames = jdbcTemplate.queryForList(
                """
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE constraint_schema = DATABASE()
                  AND constraint_type = 'UNIQUE'
                """,
                String.class
        );

        assertThat(constraintNames).contains(
                "uk_users_email",
                "uk_participant_meeting_user",
                "uk_vote_candidate_participant"
        );
    }
}
