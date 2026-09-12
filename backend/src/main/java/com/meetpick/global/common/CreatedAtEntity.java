package com.meetpick.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * [COMMON BACKBONE]
 * 생성 시각만 필요한 불변/이력성 Entity의 공통 부모입니다.
 *
 * <p>사용 예: MeetingHistory, Notification.
 * 이 두 Entity는 updated_at 컬럼이 없으므로 BaseEntity를 상속하면 Flyway schema와 충돌합니다.
 * 시간값은 JpaAuditingConfig가 주입한 UTC Clock을 기준으로 기록합니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CreatedAtEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
