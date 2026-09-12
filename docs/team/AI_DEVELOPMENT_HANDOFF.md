# MeetPick — A/B/C AI 개발 인수인계 가이드

이 문서는 **공통 백본 이후 A/B/C가 각각 AI를 사용해 기능 개발을 시작할 때의 실행 계약**이다.

목표는 "AI가 알아서 프로젝트를 다시 설계"하게 하는 것이 아니라, **이미 확정된 MeetPick 계약 안에서 각 담당 기능을 Issue 단위로 구현**하게 하는 것이다.

---

## 0. AI에게 무엇을 줘야 하는가

### 가장 권장하는 방식

A/B/C AI 모두에게 **MeetPick 저장소 전체**를 workspace/context로 제공한다.

역할 문서만 따로 주지 않는다. 이유는 각 담당 기능이 아래 공통 코드와 Entity를 공유하기 때문이다.

- `global/common/*`
- `global/error/*`
- `global/response/*`
- `global/config/*`
- 공통 Entity/Enum
- Flyway schema
- API/상태/시간 계약

### 모든 AI가 처음 읽어야 하는 파일

순서를 지킨다.

1. `TEAM_START_HERE.md`
2. `docs/team/COMMON_CONTRACT.md`
3. 자기 역할 문서
   - A: `docs/team/A_AUTH_SECURITY.md`
   - B: `docs/team/B_MEETING_DATA.md`
   - C: `docs/team/C_WORKFLOW.md`
4. `docs/api/API-v0.1.md`
5. `docs/architecture/ERD.md`
6. `docs/architecture/STATE_TRANSITIONS.md`
7. `docs/architecture/TIME_POLICY.md`
8. `CONTRIBUTING.md`

AI는 위 파일을 읽기 전 코드 생성을 시작하면 안 된다.

---

# 1. 공통 AI 작업 규칙

## 1-1. 절대 하지 말 것

AI에게 아래를 명시적으로 금지한다.

- 새 Spring Boot 프로젝트를 다시 생성하지 않는다.
- Java/Spring/React/MySQL/Redis 버전을 임의 변경하지 않는다.
- 공통 Entity 필드/관계/enum을 임의 변경하지 않는다.
- `V1__init_schema.sql`을 이유 없이 직접 고치지 않는다.
- `ddl-auto=create/update`를 사용하지 않는다.
- `MeetingRoom.hostId`를 추가하지 않는다.
- `User ↔ MeetingRoom` 직접 N:M을 만들지 않는다.
- Availability를 30분 slot 행으로 DB에 저장하지 않는다.
- JPA Entity에 public setter를 추가하지 않는다.
- `FetchType.EAGER`로 문제를 숨기지 않는다.
- `throw new RuntimeException("...")`로 비즈니스 예외를 처리하지 않는다.
- A/B/C 담당 범위를 넘는 대규모 리팩터링을 하지 않는다.
- 현재 없는 RabbitMQ/OpenSearch/OAuth2 등을 임의 추가하지 않는다.

공통 계약 변경이 정말 필요하면 **코드를 먼저 바꾸지 말고 변경 제안만 작성**한다.

## 1-2. AI에게 한 번에 맡기는 크기

한 AI에게 "A 전체를 다 만들어"처럼 맡기지 않는다.

```text
Issue 하나
→ 구현
→ 테스트
→ 리뷰
→ merge
→ 다음 Issue
```

를 원칙으로 한다.

권장 크기:

- Controller 1개 또는 핵심 Use Case 1개
- 관련 Service/Repository/DTO
- 테스트
- 필요한 문서 갱신

## 1-3. AI가 매 작업마다 먼저 출력해야 할 것

코딩 전에 AI가 다음 네 항목을 먼저 확인하도록 한다.

```text
1. 이번 Issue의 목표
2. 추가/수정할 파일 목록
3. 사용할 기존 Entity/API/ErrorCode
4. 공통 계약 변경 여부 (기본값: 없음)
```

공통 계약 변경이 필요하다고 판단되면 구현을 멈추고 이유와 대안만 제시한다.

## 1-4. AI 작업 완료 보고 형식

```text
[변경 파일]
- ...

[구현한 동작]
- ...

[검증/테스트]
- ...

[공통 계약 변경]
- 없음 / 변경 제안 있음

[다음 Issue에서 할 일]
- ...
```

---

# 2. 팀 병렬 개발 순서

세 AI를 동시에 쓸 수 있지만 의존성 때문에 순서를 지키는 것이 좋다.

## Wave 1 — 서로 독립적으로 시작 가능

### A

`회원가입 → 로그인 기본 구조`

### B

`Meeting + Participant 기본 CRUD/Repository`

### C

`순수 Java TimeSlotCalculator + 추천 알고리즘 단위 테스트`

이 세 작업은 거의 독립적이다.

## Wave 2 — 공통 Entity를 사용한 연결

### A

- Spring Security/JWT
- `/users/me`
- Participant 조회 기반 접근 제어

### B

- Availability 저장/검증
- Meeting 목록 QueryDSL

### C

- B의 Availability/Meeting Repository를 사용한 `voting/start`
- Candidate 저장

## Wave 3 — 교차 기능

### A

- Refresh + Redis Session
- Invitation Token
- Rate Limit

### B

- N+1/Pagination/Index 실험

### C

- Vote/Reopen
- Confirm Lock
- History
- AFTER_COMMIT Notification

## Wave 4 — Frontend 최소 연결

전담 프론트가 없으므로 각자 자기 기능 화면만 구현한다.

- A: 로그인/회원가입/초대
- B: 약속방/가능시간
- C: 후보/투표/확정/알림

UI 디자인보다 API 전체 흐름이 실제 웹에서 동작하는 것이 우선이다.

---

# 3. A가 AI에게 맡길 구체적인 개발 범위

## A의 책임

```text
Auth / Security
User
Invitation
Redis Auth Session
Rate Limit
```

### A가 사용하는 공통 Entity

- 직접 소유: `User`
- 읽기/생성 사용: `Participant`
- 읽기 사용: `MeetingRoom`

`Participant`, `MeetingRoom` 필드/관계는 A가 독단적으로 수정하지 않는다.

---

## A-1. 회원가입

### AI에게 맡길 일

- 이메일/비밀번호/닉네임 회원가입
- 이메일 중복 검증
- 비밀번호 PasswordEncoder 저장
- Signup API 구현

### 권장 생성 파일

```text
user/repository/UserRepository.java

auth/controller/AuthController.java
auth/service/AuthService.java
auth/dto/request/SignupRequest.java
auth/dto/response/SignupResponse.java
```

필요하면 `User`에 **정적 생성 메서드/도메인 메서드만** 추가한다. public setter는 만들지 않는다.

### 필수 테스트

- 정상 회원가입
- 중복 이메일 → `EMAIL_ALREADY_EXISTS`
- password가 평문으로 저장되지 않음
- Validation 실패 → 공통 ErrorResponse

### 완료 기준

`POST /api/v1/auth/signup`이 API-v0.1과 일치한다.

---

## A-2. 로그인 + Access JWT

### AI에게 맡길 일

- email/password 로그인
- PasswordEncoder.matches 검증
- Access Token 생성
- JWT validation
- 인증 Filter
- SecurityContext 설정

### 권장 생성 파일

```text
auth/dto/request/LoginRequest.java
auth/dto/response/LoginResponse.java

auth/jwt/JwtTokenProvider.java
auth/jwt/JwtAuthenticationFilter.java

global/security/CustomAuthenticationEntryPoint.java
```

필요하면 `SecurityConfig.java` 수정.

### 필수 테스트

- 정상 로그인
- 비밀번호 오류 → 401
- 토큰 없음 → 보호 API 401
- 정상 Access Token → 보호 API 접근
- 만료/위조 Token → 401

### 완료 기준

A-2 이후 `anyRequest().permitAll()` 상태를 끝낸다.

---

## A-3. 현재 사용자 + 접근권한 기반

### AI에게 맡길 일

- 인증 사용자 ID를 일관되게 가져오는 구조
- `GET /api/v1/users/me`
- Participant 기반 `meeting participant인지` 검증 지원
- HOST 검증 지원

### 권장 생성 파일

```text
user/controller/UserController.java
user/service/UserService.java
user/dto/response/UserMeResponse.java

global/security/CurrentUser.java              (형태는 AI가 제안 가능)
global/security/CurrentUserArgumentResolver.java (필요한 경우)
```

B의 `ParticipantRepository`를 사용한다. 없으면 B와 필요한 조회 method contract만 합의한다.

---

## A-4. Refresh Token + Redis Auth Session

이 단계는 **A의 학습 실험 후 구현**한다.

### 먼저 AI에게 비교시키는 것

- localStorage vs HttpOnly Cookie
- Refresh blacklist vs currentRefreshTokenId/session 방식 vs DB 저장
- XSS/CSRF/SameSite/Secure

최종 결정은 ADR-001에 기록한다.

### 구현 목표

- Refresh Token Rotation
- 이전 Refresh Token reuse detection
- Redis Auth Session
- 재발급
- logout session 삭제

### 권장 생성 파일

```text
auth/service/TokenService.java
auth/session/AuthSession.java
auth/session/AuthSessionRepository.java
auth/session/RedisAuthSessionRepository.java
```

JPA RefreshToken Entity/Table을 임의 추가하지 않는다.

### 필수 실험

```text
Refresh A
→ A로 재발급
→ Refresh B
→ 이전 A 재사용
→ 401
→ Redis Session 무효화
```

---

## A-5. Invitation

### AI에게 맡길 일

- HOST만 초대 Token 발급
- Redis TTL
- 1회용 Token
- 초대 정보 미리보기
- 로그인 사용자 join
- 중복 참여 방지

### 권장 생성 파일

```text
invitation/controller/InvitationController.java
invitation/service/InvitationService.java
invitation/dto/response/InvitationPreviewResponse.java
invitation/dto/response/JoinInvitationResponse.java
invitation/repository/InvitationTokenRepository.java
```

Token 사용과 Participant 생성의 race condition을 반드시 고려한다.

DB의 `(meeting_room_id,user_id)` UNIQUE는 최종 방어선이다.

---

## A-6. Login Rate Limit

### AI에게 맡길 일

- email / IP / email+IP 방식 비교
- Redis counter + TTL
- 일정 횟수 초과 → 429

결과와 한계를 트러블슈팅 문서에 남긴다.

---

## A Frontend

최소 구현:

```text
frontend/src/features/auth/
├─ api/
├─ pages/
├─ components/
└─ types/
```

화면:

- 회원가입
- 로그인
- 초대 링크 입장
- 로그아웃

기존 `httpClient.ts`, `tokenStore.ts`, `apiError.ts`를 재사용한다.

---

# 4. B가 AI에게 맡길 구체적인 개발 범위

## B의 책임

```text
MeetingRoom
Participant
Availability
QueryDSL
Pagination
DB 성능 실험
```

### B가 사용하는 공통 Entity

- 소유: `MeetingRoom`, `Participant`, `Availability`
- 참조: `User`
- `MeetingStatus` 상태 변경은 C와 공동 계약

---

## B-1. Meeting + Participant Repository

### AI에게 맡길 일

먼저 Repository를 만들어 A/C가 사용할 최소 조회 contract를 제공한다.

### 권장 생성 파일

```text
meeting/repository/MeetingRepository.java
participant/repository/ParticipantRepository.java
```

최소 필요 method 개념:

```text
Meeting 조회
Meeting pessimistic lock 조회(C에서 사용 예정)
meeting + user Participant 조회
meeting Participant 목록
user가 참여한 meeting 판별
HOST 판별
```

Lock query의 실제 추가 시점은 C confirm 구현 전에 B/C가 리뷰한다.

---

## B-2. Meeting 생성

### AI에게 맡길 일

한 Transaction에서:

```text
MeetingRoom 생성
+
HOST Participant 생성
```

### 권장 생성 파일

```text
meeting/controller/MeetingController.java
meeting/service/MeetingService.java
meeting/dto/request/CreateMeetingRequest.java
meeting/dto/response/MeetingCreateResponse.java
```

### 구현 불변식

- `now < deadline <= candidateStart < candidateEnd`
- duration > 0
- duration % 30 == 0
- 후보 전체 기간 >= duration
- API OffsetDateTime → Entity Instant

현재시간은 `Clock`을 주입해서 사용한다.

### 필수 테스트

- 정상 생성
- 생성자가 HOST Participant가 됨
- Meeting 저장 실패 시 Participant도 rollback
- 잘못된 시간순서
- 잘못된 duration

---

## B-3. Meeting 상세/수정/취소

### AI에게 맡길 일

API:

```text
GET /meetings/{meetingId}
PATCH /meetings/{meetingId}
POST /meetings/{meetingId}/cancel
```

취소는 DELETE가 아니라 `CANCELLED` 전이다.

상태 변경 메서드가 필요하면 C와 충돌하지 않도록 `MeetingRoom` 도메인 메서드의 책임을 명확히 한다.

---

## B-4. Participant 조회

### AI에게 맡길 일

```text
GET /meetings/{meetingId}/participants
```

A가 인증/초대에서 재사용할 Repository 조회를 함께 제공한다.

---

## B-5. Availability 전체 갱신

### AI에게 맡길 일

```text
PUT /meetings/{meetingId}/availabilities/me
GET /meetings/{meetingId}/availabilities/me
GET /meetings/{meetingId}/availabilities
```

PUT은 기존 내 Availability 전체를 요청 목록으로 교체한다.

### 권장 생성 파일

```text
availability/repository/AvailabilityRepository.java
availability/controller/AvailabilityController.java
availability/service/AvailabilityService.java
availability/dto/request/AvailabilityItemRequest.java
availability/dto/request/AvailabilityReplaceRequest.java
availability/dto/response/AvailabilityResponse.java
```

### 필수 검증

- Participant 여부
- COLLECTING
- deadline 이전
- Meeting 기간 내부
- start < end
- 같은 Participant interval overlap 없음

### 필수 테스트

경계값까지 포함한다.

```text
end == next start       → 허용
실제 overlap             → 실패
candidate 시작/끝 경계    → 검증
VOTING 상태              → 수정 실패
마감 이후                 → 수정 실패
```

---

## B-6. Meeting 목록 QueryDSL

### AI에게 맡길 일

```text
GET /meetings
?status=
&from=
&to=
&page=
&size=
```

### 권장 생성 파일

```text
meeting/repository/MeetingRepositoryCustom.java
meeting/repository/MeetingRepositoryImpl.java
meeting/dto/response/MeetingListItemResponse.java
meeting/dto/response/MeetingPageResponse.java
```

기본 정렬:

```text
candidateStartAt ASC
meetingId ASC
```

처음부터 성능 index를 넣지 않는다.

---

## B-7. 성능 실험

AI에게 단순 "최적화해"라고 하지 않는다.

반드시:

```text
재현
→ SQL/실행계획 기록
→ 후보 비교
→ 수치 측정
→ 선택
→ Flyway Migration
```

순서로 시킨다.

실험:

- N+1: Fetch Join / EntityGraph / Batch / DTO Projection
- Pagination + collection fetch join
- participant index
- meeting_room index

---

## B Frontend

최소 화면:

```text
약속방 생성
내 약속방 목록
약속방 상세
가능시간 입력
참여자 목록
```

권장 위치:

```text
frontend/src/features/meeting/
```

디자인보다 CRUD/API flow를 우선한다.

---

# 5. C가 AI에게 맡길 구체적인 개발 범위

## C의 책임

```text
Recommendation
Candidate
Vote
State Workflow
Confirm Concurrency
History
Notification
```

### C가 사용하는 B 소유 Entity

- `MeetingRoom`
- `Participant`
- `Availability`

필드/관계는 변경하지 않는다.

---

## C-1. TimeSlotCalculator — DB 없이 먼저

이게 C의 첫 Issue다.

### AI에게 맡길 일

`Availability interval`을 받아 순수 Java에서:

```text
30분 slot 변환
→ durationMinutes 연속 window 생성
→ 참여가능 인원 계산
→ priority 점수 합산
→ Ranking
→ 유사 후보 제거
→ TOP3
```

### 권장 생성 파일

```text
recommendation/service/TimeSlotCalculator.java
recommendation/model/TimeWindow.java
recommendation/model/CandidateScore.java
```

필요한 내부 모델 이름은 달라도 되지만 JPA Entity를 계산용 객체로 남용하지 않는다.

### 정렬 contract

```text
availableCount DESC
preferenceScore DESC
startAt ASC
```

Priority:

```text
PREFERRED = 3
AVAILABLE = 2
POSSIBLE = 1
```

### 필수 단위 테스트

- 60/90/120분 duration
- 참가자 일부만 가능한 window
- 동점 ranking
- 경계 30분
- 후보 기간 부족
- overlap된 candidate 제거
- Availability 없는 참가자

**이 단계는 Spring/JPA 없이 테스트 가능해야 한다.**

---

## C-2. TimeCandidate Repository + 조회

### 권장 생성 파일

```text
recommendation/repository/TimeCandidateRepository.java
recommendation/service/RecommendationService.java
recommendation/controller/CandidateController.java
recommendation/dto/response/TimeCandidateResponse.java
```

API:

```text
GET /meetings/{meetingId}/candidates
```

별도 `POST /candidates/generate`는 만들지 않는다.

---

## C-3. voting/start

### AI에게 맡길 일

하나의 Transaction에서:

```text
HOST 검증
COLLECTING 검증
현재 Availability 조회
TOP3 계산
TimeCandidate 저장
MeetingRoom → VOTING
MeetingHistory 저장
```

API:

```text
POST /meetings/{meetingId}/voting/start
```

B의 Meeting/Participant/Availability Repository를 재사용한다.

---

## C-4. Vote

### 권장 생성 파일

```text
vote/repository/VoteRepository.java
vote/service/VoteService.java
vote/controller/VoteController.java
```

API:

```text
POST   /meetings/{meetingId}/candidates/{candidateId}/votes
DELETE /meetings/{meetingId}/candidates/{candidateId}/votes/me
```

DB unique를 최종 중복 방어선으로 사용한다.

복수 후보 투표는 허용한다.

---

## C-5. Reopen

한 Transaction에서 반드시:

```text
Vote 삭제
→ TimeCandidate 삭제
→ MeetingRoom COLLECTING
→ MeetingHistory 저장
```

순서를 지킨다.

---

## C-6. Confirm + Pessimistic Lock

### AI에게 맡길 일

```text
Meeting pessimistic lock 조회
HOST 검증
VOTING 검증
Candidate가 같은 Meeting 소속인지 검증
confirmedStartAt/EndAt 저장
CONFIRMED 전이
History 저장
```

### 필수 동시성 실험

같은 meeting에 confirm 요청 50개:

```text
Lock 없음
Optimistic Lock
Pessimistic Lock
DB constraint 보조
```

비교.

최종 의도는 Pessimistic Lock이지만 실험 결과와 이유를 문서화한다.

검증 항목:

- 최종 성공 확정 1개
- MeetingHistory 중복 없음
- Notification 중복 없음

---

## C-7. Transaction Event Notification

### 권장 생성 파일

```text
notification/repository/NotificationRepository.java
notification/service/NotificationService.java
notification/event/MeetingConfirmedEvent.java
notification/event/MeetingConfirmedEventListener.java
notification/controller/NotificationController.java
```

원칙:

```text
Confirm Transaction commit
→ AFTER_COMMIT listener
→ REQUIRES_NEW
→ Notification 저장
```

Confirm rollback 시 Notification은 0개여야 한다.

---

## C-8. History / Notification 조회

API:

```text
GET /meetings/{meetingId}/histories
GET /notifications
PATCH /notifications/{notificationId}/read
```

알림은 반드시 로그인 사용자의 소유 여부를 검증한다.

---

## C Frontend

최소 화면:

```text
추천 후보 TOP3
투표
최종 확정
알림 목록
```

권장 위치:

```text
frontend/src/features/workflow/
```

---

# 6. A/B/C가 서로 요청해야 할 Repository Contract

병렬 개발에서 가장 충돌하기 쉬운 지점이다.

## A → B에게 필요한 것

```text
Participant를 meetingId + userId로 조회
Participant 존재 여부
HOST 여부 확인
Invitation join 시 MEMBER Participant 생성 방법
```

A가 Participant Entity를 자기 패키지에 복제하면 안 된다.

## C → B에게 필요한 것

```text
Meeting 조회
Confirm용 Pessimistic Lock Meeting 조회
Meeting Participant 조회
Meeting 전체 Availability 조회
```

C가 Meeting/Availability Repository를 자기 패키지에 복제하면 안 된다.

## B → A에게 필요한 것

Controller에서 현재 인증 사용자 ID를 얻는 공통 방식.

B/C가 JWT parsing을 직접 하면 안 된다.

---

# 7. 최초 Issue 추천

단톡에 백본을 공유한 직후 세 명이 아래 Issue부터 시작하면 된다.

## A 첫 Issue

```text
[A] 회원가입과 User 영속화 구현
```

범위:

- UserRepository
- Signup Request/Response
- AuthService signup
- AuthController signup
- PasswordEncoder
- 중복 email/validation 테스트

## B 첫 Issue

```text
[B] Meeting 생성과 HOST Participant 생성 구현
```

범위:

- MeetingRepository
- ParticipantRepository
- CreateMeeting Request/Response
- MeetingService create
- MeetingController create
- 시간/duration 불변식 테스트
- Transaction rollback 테스트

## C 첫 Issue

```text
[C] duration 기반 TimeSlotCalculator 구현
```

범위:

- 순수 Java Calculator
- 내부 계산 model
- 30분 slot/window
- ranking
- TOP3
- 단위 테스트

이 세 PR은 서로 충돌 가능성이 가장 낮아서 첫 병렬 작업으로 적합하다.

---

# 8. 첫 번째 통합 시점

A/B/C가 첫 PR 하나씩 merge되면 한 번 통합 점검한다.

확인:

```text
A User 생성 contract
B Meeting + Participant 생성 contract
C Calculator input/output contract
```

이 시점 이후:

```text
A → JWT/Security
B → Availability
C → voting/start
```

로 내려간다.

---

# 9. 기능 완료 Definition of Done

각 Issue는 아래를 모두 만족해야 끝난다.

- API-v0.1 contract와 일치
- 공통 `ApiResponse/ErrorCode/BusinessException` 사용
- Entity public setter 없음
- 시간 비교는 `Clock` 사용
- 필요한 단위/통합 테스트 존재
- 새로운 schema가 필요하면 Flyway migration 사용
- 공통 계약 변경 여부 PR에 명시
- 자기 담당 역할 문서의 트러블슈팅/실험과 연결
- 다른 담당 패키지를 복제하지 않음

---

# 10. AI에게 최종적으로 기대하는 역할

AI는 **설계자가 아니라 구현 보조자 + 리뷰어**로 사용한다.

AI에게 맡기는 것:

- 현재 계약을 읽고 코드 작성
- 테스트 케이스 작성
- edge case 제시
- 트러블슈팅 실험 코드 작성
- SQL/실행계획 비교 보조
- PR 변경사항 설명

팀원이 직접 이해/결정해야 하는 것:

- 왜 이 방식을 택했는지
- 대안과 trade-off
- 실험 결과 해석
- 공통 Entity/API 변경 승인
- 최종 merge

이 원칙을 지켜야 MeetPick이 "AI가 대신 만든 프로젝트"가 아니라 **AI를 활용해 설계·검증·구현 역량을 보여주는 프로젝트**가 된다.

---

# 11. 복붙용 프롬프트

각 담당은 아래 파일을 AI의 첫 메시지로 사용한다.

- A: `docs/team/ai-prompts/A_AI_START_PROMPT.md`
- B: `docs/team/ai-prompts/B_AI_START_PROMPT.md`
- C: `docs/team/ai-prompts/C_AI_START_PROMPT.md`
