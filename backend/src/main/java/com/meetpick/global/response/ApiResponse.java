package com.meetpick.global.response;

/**
 * [COMMON API CONTRACT]
 * 성공 응답의 단일 envelope입니다.
 *
 * <pre>
 * { "success": true, "data": ... }
 * </pre>
 *
 * A/B/C Controller는 성공 시 이 타입을 사용하고 임의의 wrapper를 새로 만들지 않습니다.
 */
public record ApiResponse<T>(
        boolean success,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null);
    }
}
