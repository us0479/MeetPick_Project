package com.meetpick;

import com.meetpick.availability.domain.AvailabilityPriority;
import com.meetpick.meeting.domain.MeetingStatus;
import com.meetpick.notification.domain.NotificationType;
import com.meetpick.participant.domain.ParticipantRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API/DB에 문자열로 저장되는 enum 값은 외부 contract이므로 실수로 rename되는 것을 막는다.
 */
class BackboneEnumContractTest {

    @Test
    void commonEnumValuesStayStable() {
        assertThat(MeetingStatus.values())
                .containsExactly(
                        MeetingStatus.COLLECTING,
                        MeetingStatus.VOTING,
                        MeetingStatus.CONFIRMED,
                        MeetingStatus.CANCELLED
                );

        assertThat(ParticipantRole.values())
                .containsExactly(ParticipantRole.HOST, ParticipantRole.MEMBER);

        assertThat(AvailabilityPriority.values())
                .containsExactly(
                        AvailabilityPriority.PREFERRED,
                        AvailabilityPriority.AVAILABLE,
                        AvailabilityPriority.POSSIBLE
                );

        assertThat(NotificationType.values())
                .containsExactly(NotificationType.MEETING_CONFIRMED);
    }
}
