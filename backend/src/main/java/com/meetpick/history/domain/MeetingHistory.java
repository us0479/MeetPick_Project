package com.meetpick.history.domain;

import com.meetpick.global.common.CreatedAtEntity;
import com.meetpick.meeting.domain.MeetingRoom;
import com.meetpick.meeting.domain.MeetingStatus;
import com.meetpick.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [COMMON ENTITY / OWNER C]
 * Meeting 상태 전이의 감사 이력입니다.
 *
 * <p>이력은 수정 대상이 아니므로 updated_at이 없는 CreatedAtEntity를 상속합니다.
 * 상태 전이와 같은 Transaction에서 저장되어야 합니다.
 */
@Getter
@Entity
@Table(name = "meeting_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingHistory extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "meeting_room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meeting_history_meeting")
    )
    private MeetingRoom meetingRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "changed_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_meeting_history_user")
    )
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 30)
    private MeetingStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private MeetingStatus newStatus;
}
