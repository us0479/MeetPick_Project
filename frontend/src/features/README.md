# Frontend 역할 분리

프론트 전담은 없으며 A/B/C가 자기 Backend API의 최소 시연 화면을 같이 구현한다.

- `auth/` — A: signup/login/reissue/invite
- `meeting/` — B: meeting list/detail/create + availability
- `workflow/` — C: candidate/vote/confirm/notification

공통 HTTP 코드는 `src/api/`, 공통 layout은 `src/layout/`을 사용한다.
