# 시연용 데모 데이터

## 범위

`demo/seed-demo-data.sql`은 기존 사용자 데이터를 지우지 않고 다음 데이터를 추가한다.

- 시연 계정: 반려견 1마리, 건강 기록 3건, 일정 3건, 산책 7건, 즐겨찾기 2건
- 가상 사용자: 25명과 각 사용자의 반려견
- 자체 리뷰: 실제 Google Place ID 기준 8건
- 견종별 스타일 통계: 포메라니안 8명, 말티즈 6명, 푸들 6명, 비쇑 프리제 5명
- 리뷰 이미지: 최대 4장, 파일이 존재할 때만 추가

가상 사용자는 `oauth_provider_id=demo-seed-user-*`, 시연 레코드는 `[DEMO-SEED]` 표식으로 구분한다. 시드 SQL은 재실행해도 같은 데이터를 중복 생성하지 않는다.

## 준비

1. Flyway migration을 모두 적용한다.
2. 시연에 사용할 SNS 계정으로 한 번 로그인해 `users`에 사용자를 만든다.
3. Google Places API에서 정상 조회되는 미용샵 Place ID 1~2개를 준비한다.
4. 선택 사항으로 [review-images/README.md](../demo/review-images/README.md)의 이미지 4장을 준비한다.

시연 계정 ID 확인:

```sql
select id, nickname, oauth_provider, email
from users
where deleted_at is null
order by id;
```

EC2의 `.env`에만 다음 값을 추가한다. Git에는 추가하지 않는다.

```dotenv
DEMO_OWNER_USER_ID=1
DEMO_GOOGLE_PLACE_ID_1=ChIJ...
DEMO_GOOGLE_PLACE_ID_2=ChIJ...
```

## 실행

PostgreSQL이 Docker 컨테이너에 있는 경우:

```bash
chmod +x demo/manage-demo-data.sh
./demo/manage-demo-data.sh seed docker
./demo/manage-demo-data.sh status docker
```

PostgreSQL에 직접 접속하는 경우:

```bash
./demo/manage-demo-data.sh seed direct
./demo/manage-demo-data.sh status direct
```

`direct` 모드는 `.env`의 `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`를 사용한다.

## 확인 API

- `GET /api/v1/pets`: 시연용 반려견
- `GET /api/v1/care/pets/{petId}/health-records`: 건강 기록
- `GET /api/v1/schedules`: 예정 일정
- `GET /api/v1/care/pets/{petId}/walks`: 산책 기록
- `GET /api/v1/care/pets/{petId}/walks/monthly-summary`: 월간 산책 통계
- `GET /api/v1/users/me/favorite-places`: 즐겨찾기
- `GET /api/v1/beauty-shops/{googlePlaceId}/reviews`: 자체 리뷰
- `GET /api/v1/beauty/style-presets/statistics?petId={petId}`: 견종별 선택 통계

## 초기화

```bash
./demo/manage-demo-data.sh cleanup docker
# 또는
./demo/manage-demo-data.sh cleanup direct
```

정리 SQL은 가상 사용자와 `[DEMO-SEED]` 표식이 있는 행만 삭제한다. 실행 전에는 서버 DB 백업을 권장한다.
