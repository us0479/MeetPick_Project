package com.meetpick.recommendation.domain;

import com.meetpick.global.common.BaseEntity;
import com.meetpick.meeting.domain.MeetingRoom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * [COMMON ENTITY / OWNER C]
 * voting/start 시점의 Availability snapshot을 바탕으로 생성된 추천 후보입니다.
 *
 * <p>candidateRank는 현재 MVP에서 1~3을 사용합니다.
 * VOTING -> COLLECTING 재오픈 시 기존 Vote를 먼저 삭제한 뒤 Candidate를 삭제합니다.
 */
@Getter
@Entity
@Table(name = "time_candidate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "meeting_room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_time_candidate_meeting")
    )
    private MeetingRoom meetingRoom;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "available_count", nullable = false)
    private Integer availableCount;

    @Column(name = "preference_score", nullable = false)
    private Integer preferenceScore;

    @Column(name = "candidate_rank", nullable = false)
    private Integer candidateRank;
}
