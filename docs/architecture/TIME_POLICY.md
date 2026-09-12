# Time Policy

1. 사용자는 Asia/Seoul 기준으로 년/월/일/시/분만 선택한다.
2. UI 기본 입력 granularity는 30분.
3. Request DTO는 offset을 포함한 ISO-8601 (`OffsetDateTime`)을 받는다.
4. Service/Entity의 절대 시점은 `Instant`를 사용한다.
5. Hibernate JDBC timezone은 UTC.
6. MySQL connection timezone은 UTC.
7. DB의 시간 컬럼은 timezone 정보 없는 `DATETIME(6)`이며 애플리케이션이 UTC 값만 기록한다.
8. 현재시간이 필요한 Service는 `Instant.now()`를 직접 호출하지 말고 주입된 `Clock`을 사용한다.
