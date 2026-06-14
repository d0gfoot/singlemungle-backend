# AI 에이전트 작업 지침

`singlemungle-backend` 저장소에서 AI 에이전트가 따라야 할 최소 작업 기준입니다.

## 프로젝트 기준

- 프로젝트명은 `싱글멍글`입니다.
- Spring Boot 기반 RESTful API 백엔드 서버입니다.
- 프론트엔드는 React Native 앱입니다.
- 팀은 총 5명, 백엔드 개발자는 2명입니다. 유지보수성과 협업 가능성을 우선합니다.

## 기술 스택

- Java, Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- springdoc-openapi / Swagger UI
- Logback
- SNS OAuth, JWT
- GitHub Actions, Docker

Redis는 필요가 명확할 때만 도입합니다.

## 도메인 구조

현재 주요 도메인:

- `auth`: OAuth 로그인, JWT, refresh token
- `place`: 병원, 미용샵 등 장소
- `pet`: 반려동물 프로필
- `health`: 건강 기록
- `schedule`: 일정 관리
- `user`: 사용자, 마이페이지

권장 구조:

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

패키지명은 모두 소문자를 사용합니다.

## API 설계 원칙

- 리소스 중심 URL을 사용합니다.
- HTTP 메서드 의미를 지킵니다.
- 요청 DTO와 응답 DTO를 분리합니다.
- Entity를 컨트롤러 응답으로 직접 노출하지 않습니다.
- 비즈니스 로직은 서비스 계층에 둡니다.
- 트랜잭션 경계는 서비스 계층에서 관리합니다.
- 목록 API는 `page`, `size`, `sort` 확장을 고려합니다.
- 네이버 예약은 자체 예약 API가 아니라 외부 예약 URL 제공으로 처리합니다.

## 인증/인가 기준

- 로그인은 SNS 계정만 지원합니다.
- Provider는 Kakao, Naver, Google입니다.
- Provider가 email을 제공하면 저장하되 필수값으로 보지 않습니다.
- JWT는 라이브러리를 사용합니다.
- 인증 API 외 보호 API는 `Authorization: Bearer {accessToken}` 기준으로 처리합니다.
- 사용자 본인 소유의 반려동물, 기록, 게시글, 댓글, 리뷰만 수정/삭제할 수 있어야 합니다.
- 로그에는 토큰과 민감한 개인정보를 남기지 않습니다.

## 데이터 기준

- DB 스키마 변경은 Flyway SQL과 JPA Entity를 함께 수정합니다.
- 위치 기반 검색이 필요한 테이블은 위도/경도 필드를 명확히 둡니다.
- 사용자와 반려동물은 soft delete 정책을 우선합니다.
- Entity와 마이그레이션이 불일치하면 테스트가 실패해야 정상입니다.

## 코드 작성 기준

- 기존 코드 스타일과 패키지 구조를 우선합니다.
- 관련 없는 리팩터링을 섞지 않습니다.
- 새로운 추상화는 실제 중복이나 복잡도를 줄일 때만 추가합니다.
- 예외는 `BusinessException`과 `ErrorCode` 기준으로 통일합니다.
- 신규 API에는 Swagger 태그, 요약, 요청/응답 DTO를 정리합니다.
- 생성/수정 요청은 `dto/request`, 조회 응답은 `dto/response`를 우선합니다.

## 테스트 기준

- 기본 검증 명령은 `./gradlew test`입니다.
- 핵심 비즈니스 로직은 단위 테스트를 작성합니다.
- 컨트롤러 API는 주요 성공/실패 케이스를 테스트합니다.
- Repository 테스트는 복잡한 쿼리나 위치 기반 조회처럼 위험도가 있을 때 작성합니다.

## 작업 보고 기준

- 코드를 수정하기 전 현재 구조를 먼저 확인합니다.
- 구현 후 가능한 범위에서 테스트 또는 빌드를 실행합니다.
- 실행하지 못한 검증은 이유를 남깁니다.
- 사용자에게는 변경 파일, 핵심 변경 내용, 검증 결과를 간결히 보고합니다.
