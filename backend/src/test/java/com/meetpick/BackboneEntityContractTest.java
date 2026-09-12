package com.meetpick;

import com.meetpick.availability.domain.Availability;
import com.meetpick.history.domain.MeetingHistory;
import com.meetpick.meeting.domain.MeetingRoom;
import com.meetpick.notification.domain.Notification;
import com.meetpick.participant.domain.Participant;
import com.meetpick.recommendation.domain.TimeCandidate;
import com.meetpick.user.domain.User;
import com.meetpick.vote.domain.Vote;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 공통 Entity가 역할별 개발 중 실수로 @Entity/Table contract를 잃지 않도록 고정하는 테스트.
 * 실제 컬럼/타입 검증은 Hibernate ddl-auto=validate + Testcontainers가 담당한다.
 */
class BackboneEntityContractTest {

    @Test
    void allEightCommonEntitiesKeepAgreedTableNames() {
        Map<Class<?>, String> entityTables = Map.of(
                User.class, "users",
                MeetingRoom.class, "meeting_room",
                Participant.class, "participant",
                Availability.class, "availability",
                TimeCandidate.class, "time_candidate",
                Vote.class, "vote",
                MeetingHistory.class, "meeting_history",
                Notification.class, "notification"
        );

        entityTables.forEach((entityType, expectedTable) -> {
            assertThat(entityType.isAnnotationPresent(Entity.class)).isTrue();
            assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(expectedTable);
        });
    }
}
