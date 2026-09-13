package com.meetpick.user.domain;

import com.meetpick.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [COMMON ENTITY / OWNER A]
 * MeetPick 로그인 사용자의 영속 모델입니다.
 *
 * <p>협업 규칙:
 * - email unique는 DB(Flyway)와 애플리케이션 양쪽에서 보호합니다.
 * - password에는 평문이 아니라 PasswordEncoder로 인코딩된 문자열만 저장합니다.
 * - 다른 도메인에서 User의 내부 상태를 직접 수정하지 않습니다.
 * - 회원가입 생성 메서드/검증 규칙은 A가 구현합니다.
 */
@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

private User(String email, String password, String nickname) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
}

public static User create(
        String email,
        String encodedPassword,
        String nickname
) {
    return new User(email, encodedPassword, nickname);
}
}
