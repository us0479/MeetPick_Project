package com.meetpick.meeting.domain;

import com.meetpick.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * [COMMON ENTITY / OWNER B, WORKFLOW REVIEW C]
 * 약속방의 기간, 소요시간, 상태, 최종 확정 시간을 보관하는 핵심 Aggregate Root입니다.
 *
 * <p>중요한 공통 계약:
 * - hostId 컬럼을 두지 않습니다. HOST의 유일한 기준은 Participant.role 입니다.
 * - API에서는 OffsetDateTime을 받고, Entity에는 UTC 절대시점 Instant를 저장합니다.
 * - Meeting 삭제는 물리 DELETE가 아니라 status=CANCELLED 상태 전이입니다.
 * - 상태 변경 메서드는 C의 workflow 구현과 충돌하므로 B/C 공동 리뷰 대상입니다.
 */
@Getter
@Entity
@Table(name = "meeting_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "candidate_start_at", nullable = false)
    private Instant candidateStartAt;

    @Column(name = "candidate_end_at", nullable = false)
    private Instant candidateEndAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "availability_deadline_at", nullable = false)
    private Instant availabilityDeadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MeetingStatus status;

    @Column(name = "confirmed_start_at")
    private Instant confirmedStartAt;

    @Column(name = "confirmed_end_at")
    private Instant confirmedEndAt;
}
