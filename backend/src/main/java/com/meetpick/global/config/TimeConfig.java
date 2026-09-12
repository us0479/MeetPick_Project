package com.meetpick.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * [COMMON BACKBONE]
 * 현재시간을 직접 Instant.now()로 호출하지 않고 주입 가능한 Clock으로 통일합니다.
 * 이렇게 해야 시간 관련 테스트에서 Clock.fixed(...)로 결정적인 테스트를 만들 수 있습니다.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
