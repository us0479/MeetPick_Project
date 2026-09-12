# B: Query / DB Performance Experiment Plan

## 1. 대상 Query

`GET /api/v1/meetings`

- 현재 로그인 사용자가 참여한 Meeting
- 선택 조건: status
- 범위 조건: candidateStartAt from/to
- pagination
- 기본 정렬: candidateStartAt ASC, id ASC

논리적 조회 형태는 `participant.user_id`로 참여 관계를 찾고 `meeting_room`을 join하는 구조다.

## 2. 중요한 정정: cross-table composite index는 불가능

기존 학습안의 `(user_id, status, candidate_start_at)` 같은 단일 복합 인덱스는 만들 수 없다.

- `participant.user_id` → `participant` table
- `meeting_room.status`, `meeting_room.candidate_start_at` → `meeting_room` table

인덱스는 한 테이블의 컬럼들로만 구성한다. 따라서 실제 실험도 두 테이블로 분리한다.

## 3. Index 후보

### Participant

현재 Unique는 `(meeting_room_id, user_id)`라 `user_id` 선두 조회를 직접 최적화하지 못할 수 있다.

실험 후보:

1. 추가 index 없음
2. `(user_id)`
3. `(user_id, meeting_room_id)`

### MeetingRoom

실험 후보:

1. 추가 index 없음
2. `(status, candidate_start_at, id)`
3. `(candidate_start_at, status, id)`

등치 조건/status 선택도와 날짜 범위 조건이 실행계획에 미치는 영향을 실제 데이터 분포로 비교한다. 결과를 보기 전 최종 인덱스를 확정하지 않는다.

## 4. 측정

각 후보에서 동일 dataset/query로 다음을 기록한다.

- `EXPLAIN ANALYZE`
- chosen index / access type
- actual rows / rows scanned
- execution time
- join order
- pagination page 크기

최종 인덱스만 후속 Flyway migration으로 추가한다.

## 5. N+1 / Pagination 실험

Meeting 목록에서 Participant 컬렉션을 무조건 fetch join하지 않는다. 아래를 비교한다.

1. Lazy 접근으로 N+1 재현
2. collection fetch join + Pageable 문제 확인
3. ID page → 상세 fetch 2-step
4. Batch Size
5. DTO Projection

목록/상세 API의 fetch 전략을 분리하고 SQL query 수와 메모리 pagination 여부를 기록한다.
