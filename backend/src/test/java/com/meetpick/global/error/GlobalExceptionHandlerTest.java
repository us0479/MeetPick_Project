package com.meetpick.global.error;

import com.meetpick.global.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionUsesAgreedErrorContract() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.MEETING_NOT_FOUND)
        );

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.MEETING_NOT_FOUND.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code()).isEqualTo("MEETING_001");
        assertThat(response.getBody().error().message()).isEqualTo(ErrorCode.MEETING_NOT_FOUND.getMessage());
    }

    @Test
    void unexpectedExceptionDoesNotExposeInternalMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(
                new IllegalStateException("secret internal detail")
        );

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("COMMON_002");
        assertThat(response.getBody().error().message())
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .doesNotContain("secret internal detail");
    }
}
