# C — Recommendation / Workflow / Consistency 작업표

## 네가 소유하는 코드

```text
backend/src/main/java/com/meetpick/
├─ recommendation/
│  └─ domain/TimeCandidate.java
├─ vote/
│  └─ domain/Vote.java
├─ history/
│  └─ domain/MeetingHistory.java
└─ notification/
   └─ domain/Notification.java, NotificationType.java

frontend/src/
└─ candidate / vote / confirm / notification 관련 화면과 API
```

C는 `MeetingRoom`과 `Availability`를 읽고 Meeting 상태를 변경한다. 둘은 B 소유 Entity이므로 **필드 변경은 B 리뷰가 필요**하다.

## 1차 구현 순서

1. 순수 Java `TimeSlotCalculator`부터 구현/단위테스트
2. Availability interval → 30분 slot
3. `durationMinutes` 연속 window
4. availableCount / preferenceScore ranking
5. 유사 candidate 제거 + TOP3
6. `voting/start` Transaction에서 Candidate 저장 + 상태전이 + History
7. candidate 조회
8. vote / vote cancel
9. reopen: Vote → Candidate 삭제 후 COLLECTING
10. confirm Pessimistic Lock
11. AFTER_COMMIT + REQUIRES_NEW Notification

## API 소유

- `GET /api/v1/meetings/{meetingId}/candidates`
- `POST /api/v1/meetings/{meetingId}/voting/start`
- `POST /api/v1/meetings/{meetingId}/collection/reopen`
- `POST /api/v1/meetings/{meetingId}/candidates/{candidateId}/votes`
- `DELETE /api/v1/meetings/{meetingId}/candidates/{candidateId}/votes/me`
- `POST /api/v1/meetings/{meetingId}/confirmation`
- `GET /api/v1/meetings/{meetingId}/histories`
- `GET /api/v1/notifications`
- `PATCH /api/v1/notifications/{notificationId}/read`

## 추천 contract

정렬:
1. availableCount DESC
2. preferenceScore DESC
3. startAt ASC

Priority 점수:
- PREFERRED 3
- AVAILABLE 2
- POSSIBLE 1

별도 `/candidates/generate` API는 없다. `voting/start`가 현재 Availability 기준으로 생성한다.

## 반드시 남길 실험

### C-1 Confirm 동시성
No Lock / Optimistic / Pessimistic / DB constraint 보조 비교. 동시 50 요청, 성공 수/History/Notification 수 검증.

### C-2 상태 머신
허용/금지 전이를 테스트로 고정. VOTING 중 Availability 수정 금지도 포함.

### C-3 Transaction Event
Service 직접 Notification 저장 vs AFTER_COMMIT vs AFTER_COMMIT+REQUIRES_NEW 비교. Confirm rollback 시 Notification 0개 검증.
