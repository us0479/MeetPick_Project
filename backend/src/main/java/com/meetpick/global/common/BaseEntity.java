package com.meetpick.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * [COMMON BACKBONE]
 * 생성/수정 시각이 모두 필요한 가변 Entity의 공통 부모입니다.
 *
 * <p>created_at은 부모 CreatedAtEntity에서 제공하고 이 클래스는 updated_at만 추가합니다.
 * 도메인 Entity가 단순히 편하다는 이유로 이 클래스를 상속하지 말고 Flyway 컬럼과 맞는지 확인합니다.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity extends CreatedAtEntity {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
