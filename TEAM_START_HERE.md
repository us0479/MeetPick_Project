# MeetPick — 팀원은 이 파일부터 읽기

이 저장소는 **공통 백본 + 공통 Entity 계약**까지 완료한 상태에서 A/B/C가 병렬 개발을 시작하기 위한 기준점이다.

> 가장 중요한 규칙: **자기 담당 패키지는 자유롭게 구현하되, COMMON 계약(ERD/API/상태/시간/공통 Entity)을 바꾸는 PR은 혼자 merge하지 않는다.**

## 1. 30초 역할표

| 담당 | 주제 | Backend 시작 위치 | Frontend 시작 위치 | 핵심 실험 |
|---|---|---|---|---|
| **A** | Auth / Security | `auth`, `user`, `invitation`, `global.security` | login/signup/invite | Refresh Rotation, replay, Rate Limit |
| **B** | Meeting / Availability / DB | `meeting`, `participant`, `availability` | meeting/availability | N+1, Pagination, EXPLAIN ANALYZE |
| **C** | Recommendation / Workflow | `recommendation`, `vote`, `history`, `notification` | candidate/vote/confirm | Lock, 상태 불변식, Transaction Event |

상세 작업표:
- [A → Auth / Security](docs/team/A_AUTH_SECURITY.md)
- [B → Meeting / Data](docs/team/B_MEETING_DATA.md)
- [C → Workflow / Recommendation](docs/team/C_WORKFLOW.md)
- [모두 → COMMON Contract](docs/team/COMMON_CONTRACT.md)
- [AI 개발 인수인계 / A·B·C 구체 작업](docs/team/AI_DEVELOPMENT_HANDOFF.md)

## 2. 공통 Entity — 일종의 Java판 '헤더 계약'

실제 Entity 골격이 이미 코드에 들어 있다. **필드/관계/FK/enum은 팀 공통 인터페이스라고 생각하면 된다.**

```text
A 소유
user/domain/User.java

B 소유
meeting/domain/MeetingRoom.java
meeting/domain/MeetingStatus.java        ← B/C 공동 계약
participant/domain/Participant.java      ← A도 접근권한에 사용
participant/domain/ParticipantRole.java
availability/domain/Availability.java    ← C가 추천 계산에서 읽음
availability/domain/AvailabilityPriority.java

C 소유
recommendation/domain/TimeCandidate.java
vote/domain/Vote.java
history/domain/MeetingHistory.java
notification/domain/Notification.java
notification/domain/NotificationType.java
```

공통 기반:

```text
global/common/CreatedAtEntity.java  // created_at only
global/common/BaseEntity.java       // created_at + updated_at
global/response/*                   // 성공/실패 JSON contract
global/error/*                      // 공통 예외 contract
global/config/*                     // UTC/JPA/QueryDSL/OpenAPI
global/security/SecurityConfig.java // A가 이어서 구현
```

## 3. 수정 권한 규칙

### 혼자 수정해도 되는 것
- 자기 패키지 내부 Controller / Service / Repository / DTO / 테스트
- 자기 담당 프론트 페이지/API 모듈
- 자기 실험 문서

### 반드시 관련 담당자 리뷰가 필요한 것
- Entity 필드/관계/enum
- `V1__init_schema.sql` 이후 Flyway schema 변경
- API URL/Request/Response contract
- MeetingStatus 상태전이
- `ErrorCode` 공통 규칙
- UTC 시간 정책
- `SecurityConfig`의 공통 CORS/public endpoint 정책

### 금지
- JPA Entity에 무분별한 `@Setter`
- `User ↔ MeetingRoom` 직접 N:M
- `MeetingRoom.hostId` 추가
- Availability를 DB 30분 slot으로 변경
- 개발 중 `ddl-auto=create/update`
- 실험 전 성능용 index를 임의 추가
- `throw new RuntimeException("...")`로 비즈니스 예외 처리

## 4. 팀 공통 Golden Path

```text
회원가입(A)
→ 로그인(A)
→ Meeting 생성 + HOST Participant(B)
→ 초대 Token 발급/소비(A, Participant 생성은 B Entity 사용)
→ Availability 입력(B)
→ voting/start: TOP3 생성(C)
→ Vote(C)
→ HOST Confirm(C)
→ MeetingHistory(C)
→ AFTER_COMMIT Notification(C)
```

## 5. 시작 전 딱 이것만 읽기

1. 이 파일
2. `docs/team/COMMON_CONTRACT.md`
3. 자기 역할 문서 A/B/C 중 하나
4. `docs/api/API-v0.1.md`
5. `docs/architecture/STATE_TRANSITIONS.md`

이후 Issue를 만들고 branch에서 개발한다. 협업 규칙은 `CONTRIBUTING.md` 참고.
