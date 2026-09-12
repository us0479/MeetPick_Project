package com.meetpick.availability.domain;

import com.meetpick.global.common.BaseEntity;
import com.meetpick.participant.domain.Participant;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * [COMMON ENTITY / OWNER B, READ BY C]
 * 사용자가 제출한 '원본 시간 구간'입니다.
 *
 * <p>절대 DB에 30분 slot을 행 단위로 저장하지 않습니다.
 * C는 추천 계산 시 이 interval을 읽어 메모리에서 30분 slot/window로 변환합니다.
 * 겹침/범위/deadline/status 검증은 B의 Availability 서비스 책임입니다.
 */
@Getter
@Entity
@Table(name = "availability")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Availability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "participant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_availability_participant")
    )
    private Participant participant;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private AvailabilityPriority priority;
}
