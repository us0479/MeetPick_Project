package com.meetpick.vote.domain;

import com.meetpick.global.common.BaseEntity;
import com.meetpick.participant.domain.Participant;
import com.meetpick.recommendation.domain.TimeCandidate;
import jakarta.persistence.Entity;
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

/**
 * [COMMON ENTITY / OWNER C]
 * 한 Participant가 한 Candidate에 행사한 투표입니다.
 *
 * <p>(candidate_id, participant_id) unique constraint가 중복 투표의 최종 방어선입니다.
 * 한 사용자가 서로 다른 후보에는 각각 투표할 수 있는 '복수 후보 선택' 정책입니다.
 */
@Getter
@Entity
@Table(
        name = "vote",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vote_candidate_participant",
                columnNames = {"candidate_id", "participant_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_vote_candidate")
    )
    private TimeCandidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "participant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_vote_participant")
    )
    private Participant participant;
}
