# MeetPick Backbone — A/B/C 협업 시작점

> **단톡에 이 폴더를 공유했다면 `TEAM_START_HERE.md`부터 읽으면 됩니다.**
- AI를 사용해 A/B/C 구현을 시작할 때: [`docs/team/AI_DEVELOPMENT_HANDOFF.md`](docs/team/AI_DEVELOPMENT_HANDOFF.md)
>
> 이 백본은 실행환경만 깔아둔 빈 프로젝트가 아니라, **공통 Entity/DB/API/상태/시간 계약까지 고정한 팀 공통 기준선**입니다.

## 0. 역할이 한눈에 보이는 구조

```text
                    ┌──────────── COMMON ────────────┐
                    │ Flyway / Entity / API / Error │
                    │ UTC / Test / CI / React 공통  │
                    └──────────────┬─────────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          ▼                        ▼                        ▼
   A Auth/Security          B Meeting/Data            C Workflow
   auth/                    meeting/                  recommendation/
   user/                    participant/              vote/
   invitation/              availability/             history/
   global/security/         QueryDSL/DB               notification/
```

| 담당 | Backend | 핵심 결과 |
|---|---|---|
| **A** | `auth`, `user`, `invitation`, `global.security` | JWT/Refresh/Redis/Invite/RateLimit |
| **B** | `meeting`, `participant`, `availability` | CRUD/시간구간/QueryDSL/N+1/Index |
| **C** | `recommendation`, `vote`, `history`, `notification` | TOP3/상태전이/Lock/Event |

**상세 작업표:** [TEAM_START_HERE.md](TEAM_START_HERE.md)

---

## 1. 공통 Entity가 실제 코드에 들어 있음

C++의 header처럼 완전히 분리된 문법은 아니지만, Java에서는 `*/domain/*.java`를 **팀 공통 domain contract**로 사용합니다.

```text
backend/src/main/java/com/meetpick/
├─ user/domain/
│  └─ User.java                         [A]
├─ meeting/domain/
│  ├─ MeetingRoom.java                  [B / C review]
│  └─ MeetingStatus.java                [B + C]
├─ participant/domain/
│  ├─ Participant.java                  [B / A consumer]
│  └─ ParticipantRole.java
├─ availability/domain/
│  ├─ Availability.java                 [B / C consumer]
│  └─ AvailabilityPriority.java
├─ recommendation/domain/
│  └─ TimeCandidate.java                [C]
├─ vote/domain/Vote.java                [C]
├─ history/domain/MeetingHistory.java    [C]
└─ notification/domain/
   ├─ Notification.java                 [C]
   └─ NotificationType.java
```

각 Entity 위 JavaDoc에 **OWNER / 다른 담당자의 사용 지점 / 변경 시 주의사항**이 적혀 있습니다.

---

## 2. 공통 백본

```text
global/
├─ common/
│  ├─ CreatedAtEntity.java      created_at만 필요한 이력성 Entity
│  └─ BaseEntity.java           created_at + updated_at
├─ config/
│  ├─ TimeConfig.java           UTC Clock
│  ├─ JpaAuditingConfig.java    UTC auditing
│  ├─ QuerydslConfig.java       JPAQueryFactory
│  └─ OpenApiConfig.java        Swagger/OpenAPI
├─ error/
│  ├─ ErrorCode.java
│  ├─ BusinessException.java
│  └─ GlobalExceptionHandler.java
├─ response/
│  ├─ ApiResponse.java
│  └─ ErrorResponse.java
└─ security/
   └─ SecurityConfig.java       A가 JWT 구현 시 이어서 수정
```

### 공통 Source of Truth

- DB schema → `backend/src/main/resources/db/migration/`
- Entity mapping → 각 `*/domain/*.java`
- API → `docs/api/API-v0.1.md`
- 상태전이 → `docs/architecture/STATE_TRANSITIONS.md`
- 시간 → `docs/architecture/TIME_POLICY.md`
- 이유 → `docs/adr/`

---

## 3. 확정 기술 스택

- Backend: Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, OpenFeign QueryDSL 7.6
- Data: MySQL 8.4, Redis 7.4, Flyway
- Docs/Monitoring: springdoc-openapi 3.1.0, Actuator
- Test: JUnit 5, Mockito, Spring Boot Test, Testcontainers
- Frontend: React, TypeScript, Vite, Axios, React Router
- Infra: Docker Compose, GitHub Actions CI, Nginx, 이후 AWS EC2/RDS/HTTPS

---

## 4. 핵심 설계 규칙

- Flyway가 DB schema의 유일한 생성/변경 수단, Hibernate는 `ddl-auto=validate`.
- `User ↔ MeetingRoom` 직접 N:M 금지 → Participant.
- HOST source of truth → `Participant.role` (`MeetingRoom.hostId` 금지).
- Availability는 원본 interval 저장, 30분 slot은 추천 계산 시 생성.
- 사용자 UI는 KST 년/월/일/시/분, API는 offset ISO-8601, Entity/DB 의미는 UTC `Instant`.
- `voting/start`에서 현재 Availability로 Candidate TOP3 생성.
- reopen은 Vote → Candidate 삭제 후 COLLECTING.
- confirm은 Pessimistic Lock 실험/적용 대상.
- Notification은 confirm commit 이후 `AFTER_COMMIT + REQUIRES_NEW`.
- 성능용 복합 index는 B의 `EXPLAIN ANALYZE` 전 임의 추가 금지.

---

## 5. 프로젝트 구조

```text
meetpick/
├─ TEAM_START_HERE.md          ★ 팀원이 가장 먼저 읽는 파일
├─ backend/
│  └─ src/main/java/com/meetpick/
│     ├─ global/               COMMON
│     ├─ auth/                 A
│     ├─ user/                 A
│     ├─ invitation/           A
│     ├─ meeting/              B
│     ├─ participant/          B
│     ├─ availability/         B
│     ├─ recommendation/       C
│     ├─ vote/                 C
│     ├─ history/              C
│     └─ notification/         C
├─ frontend/                   A/B/C가 자기 화면을 각각 연결
├─ docs/
│  ├─ team/                    ★ A/B/C 작업표 + 공통 계약
│  ├─ api/
│  ├─ architecture/
│  └─ adr/
├─ infra/nginx/
├─ scripts/
├─ .github/
└─ docker-compose.yml
```

---

## 6. Golden Path

```text
회원가입(A)
→ 로그인(A)
→ Meeting 생성 + HOST Participant(B)
→ 초대 Token → MEMBER 참여(A + B Entity)
→ Availability 입력(B)
→ voting/start + Candidate TOP3(C)
→ Vote(C)
→ Confirm(C)
→ MeetingHistory(C)
→ AFTER_COMMIT Notification(C)
```

---

## 7. 로컬 실행/검증

PC에서 Docker Desktop, Java 21, Node.js 22.12+가 준비된 경우:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-backbone.ps1
```

이 스크립트는 Compose → MySQL/Redis → Backend Testcontainers → bootJar → Frontend build → 실제 Backend health → Vite proxy health 순서로 검증합니다.

> 현재 공유본은 **정적/구조 검증을 완료한 백본**이며, Gradle Wrapper JAR과 npm lockfile은 로컬 dependency 설치가 가능한 PC에서 검증 스크립트 실행 후 생성·커밋하는 방식입니다.

---

## 8. 협업 규칙

- Issue → branch → PR → 최소 1명 review → CI → main merge.
- 자기 담당 내부 구현은 자유롭게 한다.
- Entity / Flyway / API / MeetingStatus / 시간정책 변경은 관련 담당자 공동 리뷰.
- 비즈니스 오류는 `BusinessException(ErrorCode...)` 사용.
- 공통 Entity에 무분별한 setter/cascade/양방향 collection 추가 금지.

자세한 규칙은 `CONTRIBUTING.md`.
