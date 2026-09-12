package com.meetpick.notification.domain;

import com.meetpick.global.common.CreatedAtEntity;
import com.meetpick.meeting.domain.MeetingRoom;
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

import java.time.Instant;

/**
 * [COMMON ENTITY / OWNER C]
 * 사용자에게 노출되는 알림 레코드입니다.
 *
 * <p>Meeting 확정 Transaction이 성공한 뒤 AFTER_COMMIT 이벤트에서 별도
 * REQUIRES_NEW Transaction으로 생성하는 것이 확정 설계입니다.
 * created_at만 공통 감사 필드로 사용하고, 읽음 처리는 read/isRead 필드로 표현합니다.
 */
@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "meeting_room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_meeting")
    )
    private MeetingRoom meetingRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private Instant readAt;
}
