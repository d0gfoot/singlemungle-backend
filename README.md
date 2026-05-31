# 싱글멍글 Backend

반려동물 라이프 통합 관리 플랫폼 `싱글멍글`의 RESTful API 백엔드 서버입니다.

## 프로젝트 개요

싱글멍글은 반려동물 보호자가 케어, 뷰티, 라이프, 안전, 커뮤니티 기능을 하나의 앱에서 이용할 수 있도록 하는 대학교 졸업작품 프로젝트입니다.

반려동물 관련 서비스는 병원, 미용, 동반 장소, 행사, 실종 공유, 커뮤니티 등 여러 플랫폼에 분산되어 있어 보호자가 필요한 정보를 찾고 관리하는 데 불편함이 있습니다. 또한 실종 상황에서는 정보를 빠르게 공유하고 주변 사용자에게 알릴 수 있는 통합 채널이 부족합니다.

본 프로젝트는 지도 기반 탐색, 외부 예약 페이지 연동, 반려동물 기록 관리, 실종 정보 공유, 실시간 알림, 커뮤니티 기능을 제공하여 보호자가 필요한 서비스를 단일 앱에서 해결할 수 있도록 하는 것을 목표로 합니다.

## 대상 사용자

- 반려동물 보호자
- 반려동물과 함께할 장소 또는 행사 정보를 찾는 사용자
- 반려동물 실종 발생 시 주변에 도움을 요청하고자 하는 사용자

## 주요 서비스

### 케어

- 동물병원 탐색
- 네이버 예약 페이지 이동
- 반려동물 건강 기록 관리

### 뷰티

- 반려동물 미용샵 탐색
- 네이버 예약 페이지 이동

### 라이프

- 반려동물 동반 가능 장소 탐색
- 반려동물 관련 행사 탐색
- 산책 기록 관리

### 안전

- 실종 등록
- 지도 기반 실종 정보 공유
- 주변 사용자 대상 실시간 알림 전송

### 커뮤니티

- 보호자 간 정보 공유
- 경험 공유 게시글
- 댓글 및 리뷰 기능

## 사용자 기능

- 회원 프로필 관리
- 반려동물 프로필 저장
- 최근 본 항목 조회
- 내가 작성한 리뷰 조회
- 내가 작성한 댓글 조회

## 기술 스택

### Frontend

- React Native

### Backend

- Spring Boot
- Spring Data JPA
- Flyway
- RESTful API
- springdoc-openapi / Swagger UI
- Logback 기반 콘솔/파일 로그
- Redis, 필요 시 도입 예정

### Database

- PostgreSQL

### Collaboration

- Git Flow
- Notion

### CI/CD

- GitHub Actions
- Docker

## 로컬 개발 환경

### 환경 변수

`.env.example`을 기준으로 루트 경로에 `.env`를 생성합니다. `.env`는 실제 토큰과 DB 비밀번호를 담는 파일이므로 Git에 커밋하지 않습니다.

주요 설정:

- `SPRING_PROFILES_ACTIVE`: 실행 프로필. 기본값은 `local`
- `API_TOKEN`: 외부 API 연동 토큰
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL 접속 정보
- `SWAGGER_ENABLED`: Swagger UI와 OpenAPI 문서 활성화 여부
- `FLYWAY_ENABLED`: DB 마이그레이션 활성화 여부
- `LOG_LEVEL_ROOT`, `LOG_LEVEL_APP`: 전체/애플리케이션 로그 레벨
- `LOG_FILE_NAME`: 파일 로그 저장 경로
- `REQUEST_LOGGING_ENABLED`: HTTP 요청 로그 활성화 여부

### PostgreSQL 실행

Docker Compose로 PostgreSQL을 실행합니다.

```bash
docker compose -f docker.yml up -d
```

DB 데이터까지 초기화하려면 다음 명령을 사용합니다.

```bash
docker compose -f docker.yml down -v
```

### 애플리케이션 실행

```bash
./gradlew bootRun
```

### 테스트

```bash
./gradlew test
```

## API 문서

로컬 실행 후 Swagger UI는 다음 경로에서 확인합니다.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

운영 환경에서 문서 노출이 필요 없으면 `.env`에 다음 값을 설정합니다.

```properties
SWAGGER_ENABLED=false
```

### 장소 API

#### 주변 장소 조회

현재 좌표 기준 반경 내 장소를 거리순으로 조회합니다. 케어 화면은 `HOSPITAL`, 뷰티 화면은 `BEAUTY`, 홈 추천은 `placeTypes`를 생략하거나 여러 값을 전달해서 사용합니다.

```http
GET /api/v1/places/nearby?latitude=37.4980000&longitude=127.0276000&radiusMeters=1000&placeTypes=HOSPITAL&limit=20
```

Query:

- `latitude`: 현재 위도. 필수
- `longitude`: 현재 경도. 필수
- `radiusMeters`: 조회 반경 미터. 선택, 기본값 `3000`
- `placeTypes`: 장소 타입. 선택, 복수 전달 가능. 생략 시 `HOSPITAL`, `BEAUTY`
- `limit`: 최대 응답 개수. 선택, 기본값 `20`

예시:

```http
GET /api/v1/places/nearby?latitude=37.4980000&longitude=127.0276000&radiusMeters=1000&placeTypes=HOSPITAL
GET /api/v1/places/nearby?latitude=37.4980000&longitude=127.0276000&radiusMeters=1000&placeTypes=BEAUTY
GET /api/v1/places/nearby?latitude=37.4980000&longitude=127.0276000&radiusMeters=2000&placeTypes=HOSPITAL&placeTypes=BEAUTY
```

Response:

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "places": [
      {
        "placeId": 1,
        "externalPlaceId": "12345",
        "placeType": "HOSPITAL",
        "name": "사랑동물병원",
        "latitude": 37.4983,
        "longitude": 127.0276,
        "distanceMeters": 33,
        "isOpen": true,
        "ratingAverage": 4.5,
        "reviewCount": 24
      }
    ]
  }
}
```

#### 장소 간단 정보 조회

프론트가 Kakao 지도 API로 마킹한 장소의 외부 ID를 전달하면, 백엔드 DB에 저장된 운영시간/평점/리뷰 수 등 보강 정보를 반환합니다.

```http
GET /api/v1/places/summary?provider=KAKAO&externalPlaceIds=12345&externalPlaceIds=67890
```

#### 장소 상세 조회

```http
GET /api/v1/places/{placeId}
```

## 로그

애플리케이션은 Logback 기반으로 콘솔 로그와 파일 로그를 함께 남깁니다.

- 기본 파일 로그: `logs/singlemungle.log`
- HTTP 요청 로그: `REQUEST_START`, `REQUEST_END`
- 요청 추적 헤더: `X-Trace-Id`

클라이언트가 `X-Trace-Id` 헤더를 보내면 동일 값을 응답 헤더와 로그 MDC에 사용합니다. 헤더가 없으면 서버에서 UUID를 생성합니다.

로그 레벨은 `.env`에서 조정합니다.

```properties
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
REQUEST_LOGGING_ENABLED=true
```

## 백엔드 개발 방향

- 클라이언트는 React Native 앱이며, 백엔드는 RESTful API를 제공합니다.
- 예약 기능은 자체 예약 시스템을 구현하지 않고 네이버 예약 페이지 URL로 이동할 수 있는 정보를 제공합니다.
- 지도 기반 기능을 고려하여 장소, 위치, 좌표, 반경 검색 요구사항을 API 설계에 반영합니다.
- 실시간 알림 기능은 초기에는 명확한 인터페이스를 먼저 설계하고, 구현 방식은 요구사항 확정 후 결정합니다.
- 기능별 도메인 경계를 명확히 나누고, 서비스 확장 가능성을 고려하여 설계합니다.
- DB 스키마 변경은 Flyway 마이그레이션과 JPA Entity를 함께 반영합니다.

## 팀 구성

- 총 팀원: 5명
- 백엔드 개발자: 2명

## 저장소 목적

이 저장소는 싱글멍글 서비스의 백엔드 API 서버 구현을 담당합니다. API 명세, 도메인 모델, 데이터베이스 스키마, 인증/인가, 배포 설정, 테스트 코드는 이 저장소에서 관리합니다.
