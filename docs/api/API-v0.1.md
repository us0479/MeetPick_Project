# MeetPick API v0.1

Base URL: `/api/v1`

## 공통 규칙

- JSON: `Content-Type: application/json`
- 보호 API: `Authorization: Bearer {accessToken}` (JWT Filter 실제 구현은 A 담당)
- 시간 Request: offset 포함 ISO-8601. 예: `2026-09-15T19:30:00+09:00`
- 시간 Response/내부 기준: UTC. 예: `2026-09-15T10:30:00Z`
- 성공: `{ "success": true, "data": ... }`
- 실패: `{ "success": false, "error": { "code": "MEETING_001", "message": "..." } }`

## HTTP Status

| Status | 용도 |
|---|---|
| 200 | 조회/수정/일반 성공 |
| 201 | 회원가입, Meeting/Participant 등 생성 |
| 400 | validation/형식/범위 오류 |
| 401 | 인증/토큰 오류 |
| 403 | Participant/HOST 권한 오류 |
| 404 | 리소스 없음 |
| 409 | 중복/상태 전이/현재 데이터와 충돌 |
| 410 | 만료된 1회용 초대 토큰 |
| 429 | 로그인 Rate Limit |

## Auth / User — A

| Method | Path | 인증 | 설명 |
|---|---|---:|---|
| POST | `/auth/signup` | X | 회원가입 |
| POST | `/auth/login` | X | 로그인, Access Token 발급 |
| POST | `/auth/reissue` | Refresh 정책 | Access Token 재발급 |
| POST | `/auth/logout` | O | Redis 인증 세션 폐기 |
| GET | `/users/me` | O | 내 정보 조회 |

### Signup request

```json
{
  "email": "test@meetpick.com",
  "password": "password123!",
  "nickname": "민수"
}
```

### Signup response data

`201 Created`

```json
{
  "id": 1,
  "email": "test@meetpick.com",
  "nickname": "민수"
}
```

- 회원가입 응답에는 password를 포함하지 않는다.
- 회원가입 응답에는 Access/Refresh Token을 포함하지 않는다.
- 사용자 기본 정보는 `id`, `email`, `nickname` 구조를 사용한다.

### Login request / response data

```json
{
  "email": "test@meetpick.com",
  "password": "password123!"
}
```

```json
{
  "accessToken": "..."
}
```

> Refresh Token의 브라우저 저장/전달 방식은 ADR-001에서 A가 실험 후 확정한다. API 경로는 유지한다.

### Current user response data

`GET /api/v1/users/me`

```json
{
  "id": 1,
  "email": "test@meetpick.com",
  "nickname": "민수"
}
```

회원가입 응답과 동일하게 사용자 기본 정보는 `id`, `email`, `nickname` 구조를 사용한다.

## Meeting — B

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/meetings` | 로그인 | 약속방 생성 + HOST Participant 생성 |
| GET | `/meetings` | 로그인 | 내 약속방 동적 조회/페이지네이션 |
| GET | `/meetings/{meetingId}` | Participant | 상세 조회 |
| PATCH | `/meetings/{meetingId}` | HOST | 변경 필드만 수정 |
| POST | `/meetings/{meetingId}/cancel` | HOST | 물리 삭제가 아닌 CANCELLED 전이 |

### Create request
```json
{
  "title": "9월 프로젝트 회식",
  "candidateStartAt": "2026-09-10T18:00:00+09:00",
  "candidateEndAt": "2026-09-15T23:00:00+09:00",
  "durationMinutes": 120,
  "availabilityDeadlineAt": "2026-09-08T23:59:00+09:00"
}
```

생성 불변식: `now < deadline <= candidateStart < candidateEnd`, `durationMinutes > 0 && % 30 == 0`, 그리고 `candidateEnd - candidateStart >= durationMinutes`.

### List query
`GET /meetings?status=VOTING&from=2026-09-01&to=2026-09-30&page=0&size=20`

- `from/to`는 v0.1에서 `candidateStartAt` 범위를 의미한다.
- 기본 정렬은 `candidateStartAt ASC, meetingId ASC`로 고정한다.
- 이 API가 B의 QueryDSL/N+1/Pagination/Index 실험 주 대상이다.

## Invitation / Participant — A 중심

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/meetings/{meetingId}/invitations` | HOST | Redis TTL 1회용 초대 Token 생성 |
| GET | `/invitations/{token}` | 정책상 공개 가능 | 초대 대상 Meeting 정보 미리보기 |
| POST | `/invitations/{token}/join` | 로그인 | Token 원자 소비 + MEMBER Participant 생성 |
| GET | `/meetings/{meetingId}/participants` | Participant | 참여자 목록 |

`participant(meeting_room_id,user_id)` UNIQUE가 race condition의 최종 DB 방어선이다.

## Availability — B

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| PUT | `/meetings/{meetingId}/availabilities/me` | Participant | 내 전체 구간을 요청 목록으로 교체 |
| GET | `/meetings/{meetingId}/availabilities/me` | Participant | 내 가능시간 |
| GET | `/meetings/{meetingId}/availabilities` | Participant | 참여자별 가능시간 |

### PUT request
```json
{
  "availabilities": [
    {
      "startAt": "2026-09-12T18:00:00+09:00",
      "endAt": "2026-09-12T21:00:00+09:00",
      "priority": "PREFERRED"
    }
  ]
}
```

검증: COLLECTING, deadline 미경과, Meeting 후보범위 내부, `start < end`, 동일 Participant interval overlap 금지.

## Recommendation / Workflow — C

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/meetings/{meetingId}/candidates` | Participant | 후보 TOP3/투표수 조회 |
| POST | `/meetings/{meetingId}/voting/start` | HOST | 현재 Availability → 연속 Window TOP3 저장 → COLLECTING→VOTING |
| POST | `/meetings/{meetingId}/collection/reopen` | HOST | Vote/Candidate 삭제 → VOTING→COLLECTING |
| POST | `/meetings/{meetingId}/confirmation` | HOST | Candidate 최종 확정 |

`POST /candidates/generate`는 사용하지 않는다. Candidate 생성과 VOTING 상태 전이를 한 트랜잭션 경계로 묶는다.

### Confirmation request
```json
{ "candidateId": 100 }
```

Confirm 구현 원칙: Meeting Pessimistic Lock → VOTING 확인 → Candidate 소속 확인 → 확정시간/CONFIRMED/History commit → AFTER_COMMIT + REQUIRES_NEW Notification.

## Vote — C

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/meetings/{meetingId}/candidates/{candidateId}/votes` | Participant | 해당 후보 투표 |
| DELETE | `/meetings/{meetingId}/candidates/{candidateId}/votes/me` | Participant | 내 투표 취소 |

복수 후보 투표는 허용하되 동일 Candidate 중복 투표는 `UNIQUE(candidate_id,participant_id)`로 방지한다.

## History / Notification — C

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/meetings/{meetingId}/histories` | Participant | 상태 변경 이력 |
| GET | `/notifications?page=0&size=20` | 로그인 | 내 알림 목록 |
| PATCH | `/notifications/{notificationId}/read` | 소유자 | 읽음 처리 |
