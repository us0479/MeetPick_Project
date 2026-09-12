package com.meetpick.meeting.domain;

/**
 * [COMMON CONTRACT / OWNER B+C]
 * MeetingRoom의 상태 머신 값입니다.
 *
 * <p>허용 전이는 docs/architecture/STATE_TRANSITIONS.md가 기준입니다.
 * enum 값을 변경/추가하면 B와 C가 함께 리뷰하고 API/문서/테스트도 같이 수정합니다.
 */
public enum MeetingStatus {
    COLLECTING,
    VOTING,
    CONFIRMED,
    CANCELLED
}
