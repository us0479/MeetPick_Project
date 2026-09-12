# A — Auth / Security 작업표

## 네가 소유하는 코드

```text
backend/src/main/java/com/meetpick/
├─ auth/
├─ user/
│  └─ domain/User.java
├─ invitation/
└─ global/security/SecurityConfig.java

frontend/src/
├─ pages/        로그인/회원가입/초대 입장
└─ api/          authApi, token 처리
```

`Participant`는 B 소유지만 **접근권한 판정과 초대 참여 완료에서 A가 사용한다.** 필드/관계를 바꾸지 말고 필요한 Repository contract는 B와 합의한다.

## 1차 구현 순서

1. UserRepository
2. signup DTO/Service/Controller
3. login + PasswordEncoder
4. JWT Access Token
5. 인증 Filter + SecurityContext
6. `/users/me`
7. Refresh Token 정책 실험 후 Redis Auth Session
8. logout
9. Invitation Token TTL/1회 사용
10. login Rate Limit

## API 소유

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/reissue`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me`
- `POST /api/v1/meetings/{meetingId}/invitations`
- `GET /api/v1/invitations/{token}`
- `POST /api/v1/invitations/{token}/join`

## 반드시 남길 실험

### A-1 Refresh 저장 위치
localStorage vs HttpOnly Cookie. XSS/CSRF/SameSite/Secure/withCredentials 비교.

### A-2 Rotation / reuse detection
A로 재발급 → B 발급 → A 재사용 → 401 + Session 무효화 검증.

### A-3 Rate Limit
email / IP / email+IP 기준 비교, Redis TTL/Counter 측정.

## A가 건드리면 안 되는 것

- MeetingRoom에 hostId 추가 금지
- Participant relationship 독단 변경 금지
- Refresh Token DB 테이블 추가 금지(ADR 변경 전)
- Security 구현 후에도 Swagger/health 외 모든 API를 `permitAll`로 두지 않기
