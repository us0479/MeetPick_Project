package com.meetpick.participant.domain;

import com.meetpick.global.common.BaseEntity;
import com.meetpick.meeting.domain.MeetingRoom;
import com.meetpick.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * [COMMON ENTITY / OWNER B, SECURITY USE A]
 * User와 MeetingRoom 사이의 N:M을 풀어내는 중간 도메인입니다.
 *
 * <p>협업 포인트:
 * - (meeting_room_id, user_id)는 반드시 unique입니다.
 * - A는 비참여자 접근 차단/초대 참여 완료 시 이 Entity를 조회·생성합니다.
 * - B가 소유하며, A가 컬럼/관계를 변경해야 할 때는 B와 먼저 합의합니다.
 * - 연관관계는 LAZY 단방향으로 두어 목록 조회의 N+1을 명시적으로 다룹니다.
 */
@Getter
@Entity
@Table(
        name = "participant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participant_meeting_user",
                columnNames = {"meeting_room_id", "user_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "meeting_room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_participant_meeting")
    )
    private MeetingRoom meetingRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_participant_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
}
