# 싱글멍글 Backend

반려동물 라이프 통합 관리 플랫폼 `싱글멍글`의 Spring Boot REST API 서버입니다.

## 프로젝트 개요

싱글멍글은 반려동물 보호자가 케어, 뷰티, 라이프, 안전, 커뮤니티 기능을 하나의 앱에서 이용할 수 있도록 하는 졸업작품 프로젝트입니다.

- Frontend: React Native
- Backend: Spring Boot, Spring Data JPA, RESTful API
- Database: PostgreSQL
- Migration: Flyway
- API Docs: springdoc-openapi / Swagger UI
- Auth: SNS OAuth 로그인, JWT
- CI/CD: GitHub Actions, Docker

## 주요 기능

- 케어: 동물병원 탐색, 반려동물 프로필, 건강 기록, 일정 관리
- 뷰티: 미용샵 탐색, 네이버 예약 URL 제공
- 라이프: 동반 가능 장소/행사 탐색, 산책 기록
- 안전: 실종 등록, 지도 기반 실종 정보 공유, 주변 알림
- 커뮤니티: 게시글, 댓글, 리뷰
- 마이페이지: 내 프로필, 반려동물, 최근 본 항목, 내가 쓴 글/댓글

예약 기능은 자체 예약 시스템을 만들지 않고, 장소 상세 응답에서 외부 예약 URL을 제공하는 방식으로 처리합니다.

## 현재 구현

- 공통 응답: `ApiResponse`, `PageResponse`
- 공통 예외: `BusinessException`, `ErrorCode`, `GlobalExceptionHandler`
- 요청 로그: `X-Trace-Id` 기반 추적
- Swagger UI
- Flyway 마이그레이션
- 장소 API: 목록, 상세, 주변 조회
- 케어 API: 반려동물, 건강 기록, 일정
- 사용자 API: 내 정보 수정, 탈퇴
- 인증 API: Kakao/Naver/Google OAuth 로그인, JWT 발급/재발급/로그아웃

## 프로젝트 구조

```text
src/main/java/com/singlemungle/backend
  common/
    config/
    exception/
    logging/
    response/
  domain/
    auth/
    place/
    pet/
    health/
    schedule/
    user/
```

권장 도메인 구조:

```text
domain/{domain-name}/
  controller/
  service/
  repository/
  entity/
  dto/
    request/
    response/
```

Java 패키지명은 모두 소문자를 사용합니다.

## 로컬 실행

`.env.example`을 기준으로 루트에 `.env`를 생성합니다. `.env`는 커밋하지 않습니다.

주요 환경 변수:

- `SPRING_PROFILES_ACTIVE`
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_ACCESS_TOKEN_VALIDITY_SECONDS`, `JWT_REFRESH_TOKEN_VALIDITY_SECONDS`
- `KAKAO_REST_API_KEY`
- `SWAGGER_ENABLED`
- `LOG_LEVEL_ROOT`, `LOG_LEVEL_APP`, `REQUEST_LOGGING_ENABLED`

PostgreSQL 실행:

```bash
docker compose up -d
```

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트:

```bash
./gradlew test
```

DB 스키마를 변경하면 JPA Entity와 `src/main/resources/db/migration`의 Flyway SQL을 함께 수정해야 합니다.

## API 문서

로컬 실행 후 확인합니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## 인증 흐름

별도의 이메일/비밀번호 회원가입 API는 없습니다. 앱에서 Kakao/Naver/Google SDK 로그인을 완료한 뒤, 발급받은 provider token을 백엔드 로그인 API로 전달합니다.

```http
POST /api/v1/auth/oauth/login
```

```json
{
  "provider": "KAKAO",
  "providerToken": "kakao-access-token-from-app"
}
```

백엔드는 provider userinfo API로 토큰을 검증하고, `(oauthProvider, oauthProviderId)` 기준으로 사용자를 조회합니다. 사용자가 없으면 자동으로 생성하고 `isNewUser=true`를 반환합니다. 이메일은 provider가 제공할 때만 저장하며 필수값이 아닙니다.

보호 API는 로그인 응답의 `accessToken`을 사용합니다.

```http
Authorization: Bearer {accessToken}
```

Swagger UI에서 보호 API를 테스트할 때는 우측 상단 `Authorize`에 access token을 입력한 뒤 실행합니다. access token 없이 `POST /api/v1/pets` 같은 보호 API를 호출하면 `403`으로 차단되고 DB에 저장되지 않습니다.

## 주요 API Path

- Auth: `/api/v1/auth/**`
- Place: `/api/v1/places/**`
- Pet: `/api/v1/pets/**`
- Health: `/api/v1/care/pets/{petId}/health-records/**`
- Schedule: `/api/v1/schedules/**`
- User: `/api/v1/users/me`

인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더를 사용합니다.

## 장소 데이터 수집

Kakao Local API를 사용해 장소 데이터를 DB에 저장할 수 있습니다.

```bash
python3 scripts/import_kakao_places.py
```

SQL만 확인하려면:

```bash
python3 scripts/import_kakao_places.py --dry-run
```

중복 장소는 `(provider, external_place_id)` 기준으로 갱신합니다.

## 협업

- Git Flow를 따릅니다.
- 기능 개발은 기능 단위 브랜치에서 진행합니다.
- PR/커밋 메시지는 변경한 기능과 이유가 드러나게 작성합니다.
- API 정책이나 화면 요구사항은 Notion 문서를 우선합니다.
