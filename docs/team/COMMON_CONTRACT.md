# COMMON — A/B/C가 같이 지키는 계약

## 1. Source of Truth

| 항목 | Source of Truth |
|---|---|
| DB schema | Flyway `db/migration/*.sql` |
| Java mapping | 각 `*/domain/*.java` Entity |
| HTTP contract | `docs/api/API-v0.1.md` |
| Meeting 상태 전이 | `docs/architecture/STATE_TRANSITIONS.md` |
| 시간 정책 | `docs/architecture/TIME_POLICY.md` |
| 설계 이유 | `docs/adr/ADR-*.md` |

Entity와 Flyway가 충돌하면 임의로 `ddl-auto=update`로 해결하지 말고, **어느 계약이 잘못됐는지 팀에서 결정한 뒤 둘을 같이 수정한다.**

## 2. 공통 Entity ownership

| Entity/Enum | 주 Owner | 같이 보는 사람 | 이유 |
|---|---|---|---|
| User | A | - | 인증 주체 |
| MeetingRoom | B | C | B가 CRUD, C가 상태/확정 변경 |
| MeetingStatus | B+C | 전체 | workflow 공통 enum |
| Participant | B | A | B가 관계 모델 소유, A가 권한/초대에서 사용 |
| ParticipantRole | B | A | HOST/MEMBER 권한 기준 |
| Availability | B | C | B 저장/검증, C 읽어서 추천 |
| AvailabilityPriority | B | C | 추천 점수 input |
| TimeCandidate | C | B | 추천 output |
| Vote | C | - | 투표 정합성 |
| MeetingHistory | C | B | 상태 전이 감사로그 |
| Notification | C | A | 확정 후 사용자 알림 |

## 3. 공통 시간 계약

- UI: 사용자는 한국시간 년/월/일/시/분만 선택한다.
- Request DTO: `OffsetDateTime`으로 offset 포함 값을 받는다.
- Domain/Entity: `Instant` UTC 절대시점으로 저장한다.
- 현재시간 비교: `Instant.now()` 직접 호출 금지, Spring `Clock` 주입.
- DB session/JDBC/Hibernate: UTC.

## 4. 공통 Transaction 경계

- Meeting 생성: `MeetingRoom + HOST Participant` 하나의 Transaction.
- voting/start: 상태검증 + Availability 조회 + TOP3 저장 + VOTING 전이 + History 하나의 Transaction.
- reopen: Vote 삭제 → Candidate 삭제 → COLLECTING 전이 + History 하나의 Transaction.
- confirm: Meeting Pessimistic Lock + 후보 검증 + 확정 + History 하나의 Transaction.
- Notification: confirm commit 성공 후 `AFTER_COMMIT`, 저장은 `REQUIRES_NEW`.

## 5. 공통 DB 정합성

DB 최종 방어선:
- `users.email` UNIQUE
- `(participant.meeting_room_id, participant.user_id)` UNIQUE
- `(vote.candidate_id, vote.participant_id)` UNIQUE
- FK는 Flyway V1 기준

Application 불변식:
- 정확히 한 HOST
- Availability overlap/range/deadline/status
- 유효한 Meeting 상태 전이
- Candidate가 해당 Meeting 소속인지
- HOST 권한

## 6. Cross-team 변경 프로토콜

예: C가 `MeetingRoom`에 새 컬럼이 필요하다.

1. Issue에 왜 필요한지 작성
2. B에게 Entity 계약 변경 리뷰 요청
3. API/ERD/Flyway 영향 확인
4. 필요한 문서 + Migration + Entity를 같은 PR에 포함
5. 관련 담당자 승인 후 merge

이 방식으로 "내 기능 때문에 남의 코드가 갑자기 깨지는" 상황을 줄인다.
