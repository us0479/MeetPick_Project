# MeetPick Backbone Final Validation Report

검증일: 2026-09-04

이 문서는 단톡 공유용 최종 백본을 압축하기 직전에 수행한 마지막 정적 검증 결과이다.
이 단계에서는 구조/설계를 다시 변경하지 않고 현재 백본의 계약 정합성만 검증했다.

## 최종 결과

| 항목 | 결과 |
|---|---|
| JSON / YAML 파싱 | PASS |
| Java package ↔ path | PASS |
| JPA Entity 8개 존재 | PASS |
| Flyway table 8개 존재 | PASS |
| JPA Entity ↔ Flyway table 이름 | PASS |
| JPA Column/JoinColumn ↔ Flyway column 1:1 | PASS |
| FK 10개 | PASS |
| Unique Constraint 3개 | PASS |
| `MeetingRoom.hostId` 미사용 | PASS |
| `@ManyToMany` 미사용 | PASS |
| Entity `@Setter` 미사용 | PASS |
| EAGER 연관관계 미사용 | PASS |
| raw `RuntimeException` 비즈니스 처리 미사용 | PASS |
| ErrorCode 41개 중복 없음 | PASS |
| API 최종 Workflow | PASS |
| ADR 001~010 | PASS |
| A/B/C 역할 문서 | PASS |
| Markdown 내부 링크 | PASS |
| Java main source | 35 files |
| Java test source | 12 files |
| 전체 파일 | 113 files (보고서 갱신 전 기준) |

## DB / Entity Contract

최종 공통 Entity:

1. `User` → `users`
2. `MeetingRoom` → `meeting_room`
3. `Participant` → `participant`
4. `Availability` → `availability`
5. `TimeCandidate` → `time_candidate`
6. `Vote` → `vote`
7. `MeetingHistory` → `meeting_history`
8. `Notification` → `notification`

JPA의 `@Column`, `@JoinColumn` 및 상속 감사 컬럼(`created_at`, `updated_at`)을 Flyway V1 컬럼과 대조했으며 누락/잉여 컬럼이 없음을 확인했다.

DB 핵심 제약:

- `users.email` UNIQUE
- `participant(meeting_room_id, user_id)` UNIQUE
- `vote(candidate_id, participant_id)` UNIQUE
- FK 10개
- `meeting_room.host_id` 없음

## Workflow Contract

현재 API/문서 기준:

```text
COLLECTING
→ voting/start에서 현재 Availability 기준 Candidate TOP3 생성
→ VOTING
→ Vote
→ confirmation
→ CONFIRMED
→ MeetingHistory
→ AFTER_COMMIT + REQUIRES_NEW Notification
```

`POST /candidates/generate`를 외부 API로 두지 않는다.
VOTING → COLLECTING 재오픈 시 기존 Vote와 TimeCandidate를 초기화한다.

## 협업 Contract

- A: `auth`, `user`, `invitation`, `global.security`
- B: `meeting`, `participant`, `availability`
- C: `recommendation`, `vote`, `history`, `notification`
- COMMON: Entity/Flyway/API/Error/UTC/상태전이/공통 config

팀원은 `TEAM_START_HERE.md` → `docs/team/COMMON_CONTRACT.md` → 자기 역할 문서 순서로 읽고 개발한다.

## 현재 환경에서 의도적으로 미수행한 검증

아래는 백본 구조 문제가 아니라 실행환경이 필요한 단계이므로 이후 개발 PC 또는 GitHub Actions에서 처리한다.

- Gradle dependency 실제 다운로드 및 Java compile
- Docker 기반 MySQL/Redis 실제 기동
- Testcontainers integration test 실제 실행
- npm dependency 실제 설치 및 Vite production build
- Spring Boot 실제 `/actuator/health`
- React/Vite → Spring proxy smoke test

이를 위한 `scripts/verify-backbone.ps1`와 GitHub Actions CI가 포함되어 있다.

## 판정

**FINAL STATIC VALIDATION: PASS**

현재 상태를 MeetPick A/B/C 병렬 개발의 공통 백본 기준선으로 사용한다.

## AI 개발 인수인계 문서

- `docs/team/AI_DEVELOPMENT_HANDOFF.md` 추가
- A/B/C 복붙용 첫 프롬프트 3개 추가
- 기존 ERD/API/상태/시간/Entity 계약은 변경하지 않음
- Markdown 내부 링크 재검증: broken link 0
