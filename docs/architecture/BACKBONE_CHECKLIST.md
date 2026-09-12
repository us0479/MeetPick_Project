# Backbone Verification Checklist

이 문서는 **정적/구조 검증**과 **Docker가 있는 환경에서만 가능한 runtime 검증**을 분리한다.

## A. 현재 공유본에서 정적 검증 완료

- [x] YAML/JSON 구문 파싱
- [x] Flyway 핵심 8개 테이블 존재
- [x] JPA Entity 8개와 Flyway table/column 1:1 대조
- [x] 공통 enum 값 고정
- [x] `users.email` UNIQUE
- [x] `participant(meeting_room_id,user_id)` UNIQUE
- [x] `vote(candidate_id,participant_id)` UNIQUE
- [x] `meeting_room.host_id` 없음
- [x] Invitation / RefreshToken / AuthSession 관계형 테이블 없음
- [x] `ddl-auto=validate`, OSIV=false, UTC 설정
- [x] Entity class-level `@Setter` 없음
- [x] JPA 관계 기본 LAZY
- [x] 제거된 Candidate generate API가 API 목록에 없음
- [x] TEAM_START_HERE + COMMON + A/B/C 역할 문서 존재
- [x] GitHub Issue/PR template에 담당 영역/공통 Contract 체크 추가
- [x] 초기 Wrapper/lockfile가 없어도 CI가 실행될 fallback 구성
- [x] TypeScript/TSX 소스 syntax transpile 검증
- [x] Java 소스 javac parser 단계에서 syntax 계열 오류 없음 (외부 dependency 미설치 symbol 오류는 제외)

## B. CI/Testcontainers가 실제로 검증할 것

GitHub Actions 또는 Docker 사용 가능한 개발 PC에서 수행된다.

- [ ] Gradle dependency resolve + Java compile
- [ ] Spring Context load
- [ ] MySQL 8.4 Testcontainer 기동
- [ ] Redis 7.4 Testcontainer 기동 + PING=PONG
- [ ] Flyway V1 실행
- [ ] Hibernate `ddl-auto=validate` — 실제 Entity/Flyway mapping 검증
- [ ] Unique constraint 통합 테스트
- [ ] 공통 ErrorResponse 테스트
- [ ] UTC Clock 테스트
- [ ] Frontend dependency resolve + `tsc -b` + Vite production build

## C. 로컬 통합환경이 준비된 경우 추가 검증

- [ ] `docker compose config --quiet`
- [ ] MySQL/Redis healthy
- [ ] local profile Boot JAR `/actuator/health` UP
- [ ] Swagger UI 접근
- [ ] Vite proxy `/actuator/health` UP
- [ ] Nginx `/api` reverse proxy / SPA refresh 확인

## D. 의도적으로 아직 구현하지 않은 기능

- JWT Filter / Refresh Rotation / Cookie 정책 — A
- Repository / Service / Controller / feature DTO — 각 A/B/C
- Entity의 실제 생성/상태변경 domain method — 각 Owner가 불변식 테스트와 함께 구현
- 성능용 복합 Index — B의 EXPLAIN ANALYZE 실험 후 Flyway migration
- Confirm Pessimistic Lock / Transaction Event Listener — C
- AWS EC2/RDS/HTTPS 실제 값 — 배포 단계
