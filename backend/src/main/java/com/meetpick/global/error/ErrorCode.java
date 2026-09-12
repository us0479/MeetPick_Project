package com.meetpick.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * [COMMON CONTRACT]
 * 모든 도메인이 공유하는 안정적인 오류 코드 목록입니다.
 *
 * <p>규칙:
 * - code는 외부 API contract이므로 한 번 배포한 값은 함부로 변경하지 않습니다.
 * - message는 사용자 노출 기본 문구이며 내부 예외/SQL 정보는 포함하지 않습니다.
 * - 새 오류는 자기 도메인 구간에 추가하고 ErrorCodeTest의 중복 검증을 통과해야 합니다.
 */
@Getter
public enum ErrorCode {

    // COMMON — 전원 공동 관리
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "COMMON_003", "요청이 현재 데이터 상태와 충돌합니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_006", "지원하지 않는 Content-Type입니다."),

    // AUTH — Owner A
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_002", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH_005", "이미 사용된 Refresh Token입니다."),
    LOGIN_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_006", "로그인 시도 횟수를 초과했습니다."),

    // USER — Owner A
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),

    // MEETING — Owner B, workflow 관련 코드는 C review
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING_001", "약속방을 찾을 수 없습니다."),
    MEETING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEETING_002", "약속방 접근 권한이 없습니다."),
    HOST_PERMISSION_REQUIRED(HttpStatus.FORBIDDEN, "MEETING_003", "방장 권한이 필요합니다."),
    INVALID_MEETING_STATUS(HttpStatus.CONFLICT, "MEETING_004", "현재 약속 상태에서는 수행할 수 없는 요청입니다."),
    MEETING_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "MEETING_005", "이미 확정된 약속입니다."),
    MEETING_ALREADY_CANCELLED(HttpStatus.CONFLICT, "MEETING_006", "이미 취소된 약속입니다."),
    INVALID_MEETING_PERIOD(HttpStatus.BAD_REQUEST, "MEETING_007", "약속 후보 기간이 올바르지 않습니다."),
    INVALID_DURATION(HttpStatus.BAD_REQUEST, "MEETING_008", "약속 소요시간은 30분 단위의 양수여야 합니다."),
    INVALID_AVAILABILITY_DEADLINE(HttpStatus.BAD_REQUEST, "MEETING_009", "가능시간 제출 마감시간이 올바르지 않습니다."),
    DURATION_EXCEEDS_CANDIDATE_PERIOD(HttpStatus.BAD_REQUEST, "MEETING_010", "약속 소요시간이 후보 시간 범위보다 깁니다."),

    // PARTICIPANT — Owner B, security consumer A
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT_001", "참여자를 찾을 수 없습니다."),
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "PARTICIPANT_002", "이미 참여 중인 약속방입니다."),

    // INVITATION — Owner A
    INVALID_INVITATION_TOKEN(HttpStatus.BAD_REQUEST, "INVITATION_001", "유효하지 않은 초대 토큰입니다."),
    EXPIRED_INVITATION_TOKEN(HttpStatus.GONE, "INVITATION_002", "만료된 초대 토큰입니다."),
    INVITATION_ALREADY_USED(HttpStatus.CONFLICT, "INVITATION_003", "이미 사용된 초대 토큰입니다."),

    // AVAILABILITY — Owner B
    INVALID_AVAILABILITY_RANGE(HttpStatus.BAD_REQUEST, "AVAILABILITY_001", "가능 시간의 시작과 종료 시간이 올바르지 않습니다."),
    AVAILABILITY_OUT_OF_MEETING_RANGE(HttpStatus.BAD_REQUEST, "AVAILABILITY_002", "약속 후보 범위를 벗어난 시간입니다."),
    AVAILABILITY_OVERLAP(HttpStatus.CONFLICT, "AVAILABILITY_003", "가능 시간이 서로 겹칩니다."),
    AVAILABILITY_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "AVAILABILITY_004", "현재 상태에서는 가능 시간을 수정할 수 없습니다."),
    AVAILABILITY_DEADLINE_EXPIRED(HttpStatus.CONFLICT, "AVAILABILITY_005", "가능시간 제출 마감시간이 지났습니다."),

    // CANDIDATE — Owner C
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CANDIDATE_001", "추천 후보 시간을 찾을 수 없습니다."),
    CANDIDATE_NOT_IN_MEETING(HttpStatus.BAD_REQUEST, "CANDIDATE_002", "해당 약속방의 후보 시간이 아닙니다."),
    CANDIDATE_GENERATION_NOT_ALLOWED(HttpStatus.CONFLICT, "CANDIDATE_003", "현재 상태에서는 후보 시간을 생성할 수 없습니다."),

    // VOTE — Owner C
    VOTE_NOT_ALLOWED(HttpStatus.CONFLICT, "VOTE_001", "현재 상태에서는 투표할 수 없습니다."),
    ALREADY_VOTED(HttpStatus.CONFLICT, "VOTE_002", "이미 해당 후보에 투표했습니다."),
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_003", "투표 정보를 찾을 수 없습니다."),

    // NOTIFICATION — Owner C
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
