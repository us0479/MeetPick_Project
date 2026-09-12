# MeetPick Collaboration Rules

## 0. Ownership

- A: `auth`, `user`, `invitation`, `global.security`
- B: `meeting`, `participant`, `availability`
- C: `recommendation`, `vote`, `history`, `notification`
- COMMON: `global.common/config/error/response`, Flyway, API/ERD/상태/시간 contract

자기 패키지 내부 구현은 담당자가 주도한다. **Entity/Flyway/API/enum/상태전이처럼 다른 담당 코드가 참조하는 contract는 관련 담당자 리뷰를 필수로 한다.**

상세 경계는 `TEAM_START_HERE.md`, `docs/team/COMMON_CONTRACT.md`를 기준으로 한다.

## Branch

`main`에서 Issue별 short-lived branch를 만든다. 별도 `develop` branch는 사용하지 않는다.

- `feat/{issue}-{topic}`
- `fix/{issue}-{topic}`
- `refactor/{issue}-{topic}`
- `test/{issue}-{topic}`
- `docs/{issue}-{topic}`
- `chore/{issue}-{topic}`

예: `feat/23-availability-update`, `test/47-confirm-concurrency`.

## Commit

- `feat:` 기능
- `fix:` 버그
- `refactor:` 구조 개선
- `test:` 테스트/실험
- `docs:` 문서
- `chore:` 설정/빌드

예: `feat: 약속방 생성 기능 구현 (#12)`.

## Issue

기술명을 적용하는 일 자체가 아니라 해결할 문제를 제목으로 작성한다.

- 나쁜 예: `Redis 적용`
- 좋은 예: `Refresh Token 재사용 감지를 위한 Auth Session 구조 설계`

Issue에는 문제/목표, 재현·배경, 검토 대안, 검증 기준을 남긴다.

## Pull Request

1. 자기 PR을 바로 self-merge하지 않는다.
2. 최소 팀원 1명의 리뷰를 받는다.
3. 관련 Issue를 `Closes #번호`로 연결한다.
4. 설계 결정·대안·검증 방법·트러블슈팅을 PR template에 기록한다.
5. CI 성공 후 merge한다.
6. Cross-team contract 변경이면 해당 Owner를 리뷰어로 지정한다.

## Architecture change

아래 항목을 바꾸는 PR은 관련 ADR/문서를 같은 PR에서 수정한다.

- ERD / Entity field / JPA relationship
- Flyway schema/constraint
- API request/response/path
- MeetingStatus / 상태 전이
- 인증 경계
- UTC 시간정책
- 공통 ApiResponse/ErrorResponse/ErrorCode 정책

## Java domain rule

- Entity에 class-level `@Setter` 금지.
- 연관관계는 기본 LAZY, 양방향 collection은 문제를 증명하기 전 추가하지 않는다.
- cascade/orphanRemoval도 lifecycle이 실제로 같은 경우만 근거와 함께 추가한다.
- 비즈니스 예외는 `BusinessException + ErrorCode` 사용.
- 현재시간은 주입받은 `Clock` 사용.
