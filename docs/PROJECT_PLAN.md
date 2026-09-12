# MeetPick 6-week Project / Study Plan

## 목표

기능 개수보다 설계, 도메인 모델링, 트랜잭션, 정합성, 동시성, SQL 성능, 테스트, 배포, ADR/트러블슈팅을 증명한다.

## 역할

실제 개발 시작 위치는 `TEAM_START_HERE.md`와 `docs/team/`의 역할별 문서를 우선한다.

### A — Auth / Security
상세: `docs/team/A_AUTH_SECURITY.md`
- Backend: 회원가입/로그인, JWT, Refresh lifecycle, Redis Auth Session, Rotation/reuse detection, logout, Invite TTL/1회 사용, Rate Limit
- Frontend: 회원가입/로그인/재발급 확인/초대 입장
- 실험: localStorage vs HttpOnly Cookie, Refresh Rotation/reuse, email vs IP vs 복합 Rate Limit

### B — Meeting / Availability / Query Performance
상세: `docs/team/B_MEETING_DATA.md`
- Backend: Meeting, Participant, Availability, QueryDSL/Pagination, N+1, Index/EXPLAIN ANALYZE
- Frontend: 약속방 생성/목록/상세/가능시간 입력
- 실험: fetch 전략, pagination+collection fetch join, 테이블별 composite index

### C — Recommendation / Workflow / Consistency
상세: `docs/team/C_WORKFLOW.md`
- Backend: 30분 slot 변환, duration window, TOP3, 투표, 상태전이, Confirm lock, History, Transaction Event Notification
- Frontend: TOP3/투표/확정/알림
- 실험: no lock vs optimistic vs pessimistic, 상태 불변식, AFTER_COMMIT + REQUIRES_NEW

## 주차

1. 설계/공통 백본: ERD/API/상태전이/ADR/Flyway/Docker/CI/Testcontainers/React 공통 연결
2. MVP 1차: A Auth 기본, B Meeting/Participant/Availability, C 추천 알고리즘
3. Golden Path 완성: Refresh/Invite, QueryDSL, Vote/Confirm/History/Notification, 모든 화면 연결
4. 학습 실험/트러블슈팅: Security, N+1/Index, 동시성/Transaction Event, timezone/CORS
5. 통합/배포: Docker build, EC2/RDS, Nginx, HTTPS, Actuator, 배포 URL E2E
6. 리팩토링/테스트/문서: 회귀 테스트, 성능 수치, ADR 업데이트, README/시연 시나리오

## Golden Path

회원가입 → 로그인 → Meeting 생성 + HOST Participant → 초대 Token → MEMBER 참여 → Availability 입력 → voting/start에서 Candidate TOP3 생성 → 투표 → HOST 확정 → History → AFTER_COMMIT Notification

## 선택 기능

핵심 기능/테스트/배포/문서가 끝난 뒤 실제 필요가 있을 때만 OAuth2, RabbitMQ, OpenSearch, CD, UI 고도화를 검토한다.
