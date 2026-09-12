package com.meetpick.global.error;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void errorCodesAreUnique() {
        long distinctCount = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .distinct()
                .count();

        assertThat(distinctCount).isEqualTo(ErrorCode.values().length);
    }
}
