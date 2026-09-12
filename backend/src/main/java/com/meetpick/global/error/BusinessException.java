package com.meetpick.global.error;

import lombok.Getter;

/**
 * [COMMON BACKBONE]
 * 예측 가능한 비즈니스 실패를 Controller까지 전달하기 위한 공통 예외입니다.
 *
 * <p>사용 예: {@code throw new BusinessException(ErrorCode.MEETING_NOT_FOUND);}
 * RuntimeException을 문자열과 함께 직접 던지는 방식은 사용하지 않습니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * validation context를 조금 더 구체적으로 설명해야 할 때 사용합니다.
     * error code 자체는 유지하므로 프론트 분기 contract는 깨지지 않습니다.
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
