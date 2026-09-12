package com.meetpick;

import com.meetpick.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TimeBackboneTest extends AbstractIntegrationTest {

    @Autowired
    Clock clock;

    @Test
    void applicationClockUsesUtc() {
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
