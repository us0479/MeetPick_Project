package com.meetpick.global.response;

/**
 * [COMMON API CONTRACT]
 * 실패 응답의 단일 envelope입니다.
 *
 * <pre>
 * { "success": false, "error": { "code": "MEETING_001", "message": "..." } }
 * </pre>
 *
 * 내부 stack trace/SQL/예외 메시지는 response에 포함하지 않습니다.
 */
public record ErrorResponse(
        boolean success,
        ErrorDetail error
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, new ErrorDetail(code, message));
    }

    public record ErrorDetail(
            String code,
            String message
    ) {
    }
}
