-- ============================================================
-- COMMON SCHEMA V1
-- Flyway가 DB schema의 Source of Truth입니다.
-- Entity 변경 시 이 파일을 수정하지 말고 V2__... migration을 새로 추가합니다.
-- ============================================================

-- [A] User: 인증 사용자. email unique.
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [B / C shared] MeetingRoom: B CRUD, C workflow/state 변경. host_id는 의도적으로 없음.
CREATE TABLE meeting_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    candidate_start_at DATETIME(6) NOT NULL,
    candidate_end_at DATETIME(6) NOT NULL,
    duration_minutes INT NOT NULL,
    availability_deadline_at DATETIME(6) NOT NULL,
    status VARCHAR(30) NOT NULL,
    confirmed_start_at DATETIME(6) NULL,
    confirmed_end_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [B, consumer A] User-Meeting N:M 중간 도메인. HOST source of truth.
CREATE TABLE participant (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meeting_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_participant_meeting_user UNIQUE (meeting_room_id, user_id),
    CONSTRAINT fk_participant_meeting FOREIGN KEY (meeting_room_id) REFERENCES meeting_room (id),
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [B, consumer C] 사용자가 입력한 원본 interval. 30분 slot 저장 금지.
CREATE TABLE availability (
    id BIGINT NOT NULL AUTO_INCREMENT,
    participant_id BIGINT NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_availability_participant FOREIGN KEY (participant_id) REFERENCES participant (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [C] voting/start에서 계산되어 저장되는 추천 TOP3 후보.
CREATE TABLE time_candidate (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meeting_room_id BIGINT NOT NULL,
    start_at DATETIME(6) NOT NULL,
    end_at DATETIME(6) NOT NULL,
    available_count INT NOT NULL,
    preference_score INT NOT NULL,
    candidate_rank INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_time_candidate_meeting FOREIGN KEY (meeting_room_id) REFERENCES meeting_room (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [C] 후보별 복수선택 투표. candidate+participant unique가 중복 투표 최종 방어선.
CREATE TABLE vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    candidate_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_vote_candidate_participant UNIQUE (candidate_id, participant_id),
    CONSTRAINT fk_vote_candidate FOREIGN KEY (candidate_id) REFERENCES time_candidate (id),
    CONSTRAINT fk_vote_participant FOREIGN KEY (participant_id) REFERENCES participant (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [C] Meeting 상태 전이 감사 이력. 수정하지 않는 append-only 성격.
CREATE TABLE meeting_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meeting_room_id BIGINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_meeting_history_meeting FOREIGN KEY (meeting_room_id) REFERENCES meeting_room (id),
    CONSTRAINT fk_meeting_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- [C] confirm commit 이후 AFTER_COMMIT + REQUIRES_NEW로 생성할 알림.
CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    meeting_room_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notification_meeting FOREIGN KEY (meeting_room_id) REFERENCES meeting_room (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
