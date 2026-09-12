# B — Meeting / Participant / Availability / Query 작업표

## 네가 소유하는 코드

```text
backend/src/main/java/com/meetpick/
├─ meeting/
│  └─ domain/MeetingRoom.java, MeetingStatus.java
├─ participant/
│  └─ domain/Participant.java, ParticipantRole.java
└─ availability/
   └─ domain/Availability.java, AvailabilityPriority.java

frontend/src/
└─ meeting / participant / availability 관련 화면과 API
```

`MeetingStatus`와 MeetingRoom 상태 변경 메서드는 C workflow가 직접 사용하므로 **B/C 공동 리뷰 영역**이다.

## 1차 구현 순서

1. MeetingRepository
2. ParticipantRepository
3. Meeting 생성: MeetingRoom + HOST Participant 한 Transaction
4. Meeting 상세/수정/취소
5. Participant 목록
6. Availability 전체 갱신(PUT)
7. Availability overlap/range/deadline/status 검증
8. `GET /meetings` QueryDSL 동적 조회 + pagination

## API 소유

- `POST /api/v1/meetings`
- `GET /api/v1/meetings`
- `GET /api/v1/meetings/{meetingId}`
- `PATCH /api/v1/meetings/{meetingId}`
- `POST /api/v1/meetings/{meetingId}/cancel`
- `GET /api/v1/meetings/{meetingId}/participants`
- `PUT /api/v1/meetings/{meetingId}/availabilities/me`
- `GET /api/v1/meetings/{meetingId}/availabilities/me`
- `GET /api/v1/meetings/{meetingId}/availabilities`

## 반드시 남길 실험

### B-1 N+1
Fetch Join / EntityGraph / Batch Size / DTO Projection 비교. SQL query count 기록.

### B-2 Pagination + collection fetch join
메모리 pagination 문제 재현 후 ID paging / DTO Projection / Batch Size 비교.

### B-3 Index
서로 다른 테이블 컬럼을 하나의 복합 index로 묶지 않는다.

- participant: `(user_id)` vs `(user_id, meeting_room_id)`
- meeting_room: `(status, candidate_start_at, id)` vs `(candidate_start_at, status, id)`

`EXPLAIN ANALYZE`, rows scanned, execution time 근거 후 Flyway migration 추가.

## 핵심 불변식

- `now < deadline <= candidateStart < candidateEnd`
- duration은 양수이며 30분 배수
- 후보 기간 >= duration
- Availability는 COLLECTING + deadline 이전만 수정
- 같은 Participant interval overlap 금지
