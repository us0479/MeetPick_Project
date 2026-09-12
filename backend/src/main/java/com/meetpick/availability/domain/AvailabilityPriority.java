package com.meetpick.availability.domain;

/**
 * [COMMON CONTRACT / OWNER B, CONSUMER C]
 * 사용자가 입력한 가능시간의 선호도입니다.
 * C 추천 알고리즘의 기본 점수는 PREFERRED=3, AVAILABLE=2, POSSIBLE=1 입니다.
 */
public enum AvailabilityPriority {
    PREFERRED,
    AVAILABLE,
    POSSIBLE
}
