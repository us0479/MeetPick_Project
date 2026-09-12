package com.meetpick.participant.domain;

/**
 * [COMMON CONTRACT / OWNER B]
 * 약속방 안에서의 사용자 역할입니다.
 * HOST는 MeetingRoom.hostId가 아니라 이 값 하나로만 판정합니다.
 */
public enum ParticipantRole {
    HOST,
    MEMBER
}
