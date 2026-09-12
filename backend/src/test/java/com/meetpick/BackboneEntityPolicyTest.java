package com.meetpick;

import com.meetpick.availability.domain.Availability;
import com.meetpick.history.domain.MeetingHistory;
import com.meetpick.meeting.domain.MeetingRoom;
import com.meetpick.notification.domain.Notification;
import com.meetpick.participant.domain.Participant;
import com.meetpick.recommendation.domain.TimeCandidate;
import com.meetpick.user.domain.User;
import com.meetpick.vote.domain.Vote;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 팀 협업 중 공통 Entity 정책이 조용히 무너지는 것을 막는 테스트.
 */
class BackboneEntityPolicyTest {

    private static final List<Class<?>> ENTITIES = List.of(
            User.class,
            MeetingRoom.class,
            Participant.class,
            Availability.class,
            TimeCandidate.class,
            Vote.class,
            MeetingHistory.class,
            Notification.class
    );

    @Test
    void commonEntitiesDoNotExposePublicSetters() {
        for (Class<?> entity : ENTITIES) {
            List<String> publicSetters = Arrays.stream(entity.getMethods())
                    .map(Method::getName)
                    .filter(name -> name.startsWith("set"))
                    .toList();

            assertThat(publicSetters)
                    .as("%s must not expose public setters", entity.getSimpleName())
                    .isEmpty();
        }
    }

    @Test
    void allManyToOneRelationshipsStayLazy() {
        for (Class<?> entity : ENTITIES) {
            for (Field field : entity.getDeclaredFields()) {
                ManyToOne relation = field.getAnnotation(ManyToOne.class);
                if (relation != null) {
                    assertThat(relation.fetch())
                            .as("%s.%s must be LAZY", entity.getSimpleName(), field.getName())
                            .isEqualTo(FetchType.LAZY);
                }
            }
        }
    }

    @Test
    void meetingRoomDoesNotOwnHostId() {
        List<String> fieldNames = Arrays.stream(MeetingRoom.class.getDeclaredFields())
                .map(Field::getName)
                .toList();

        assertThat(fieldNames).doesNotContain("hostId");
    }
}
