# Meeting State Transition v1

| 현재 상태 | 요청 | 다음 상태 | 허용 | 핵심 부수효과 |
|---|---|---|---|---|
| COLLECTING | voting/start | VOTING | O (HOST) | 현재 Availability로 Candidate TOP3 생성, History 저장 |
| COLLECTING | cancel | CANCELLED | O (HOST) | History 저장 |
| VOTING | collection/reopen | COLLECTING | O (HOST) | 기존 Vote → Candidate 순으로 삭제, History 저장 |
| VOTING | confirmation | CONFIRMED | O (HOST) | Pessimistic Lock, 확정시간 저장, History, AFTER_COMMIT Notification |
| VOTING | cancel | CANCELLED | O (HOST) | History 저장 |
| CONFIRMED | 모든 상태변경 | - | X | `409 INVALID_MEETING_STATUS`/이미 확정 오류 |
| CANCELLED | 모든 상태변경 | - | X | `409 INVALID_MEETING_STATUS`/이미 취소 오류 |

## Availability 불변식

- 수정은 `status == COLLECTING`일 때만 허용한다.
- `now <= availabilityDeadlineAt`이어야 한다.
- `candidateStartAt <= startAt < endAt <= candidateEndAt`이어야 한다.
- 같은 Participant의 구간은 서로 겹치지 않는다.

## Meeting 생성 불변식

`now < availabilityDeadlineAt <= candidateStartAt < candidateEndAt`

`durationMinutes > 0 && durationMinutes % 30 == 0`

`Duration.between(candidateStartAt, candidateEndAt).toMinutes() >= durationMinutes`
