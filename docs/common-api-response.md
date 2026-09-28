# 프론트엔드 공통 API 응답 처리 가이드

React Native에서 백엔드 성공·실패 응답을 일관되게 처리하기 위한 계약입니다.

## JSON 응답 구조

이미지 파일 조회를 제외한 API는 다음 구조를 사용합니다.

```ts
export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  hasNext: boolean;
}
```

성공 예시:

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "petId": 1
  }
}
```

실패 예시:

```json
{
  "code": "INVALID_REQUEST",
  "message": "잘못된 요청",
  "data": null
}
```

HTTP 성공 여부만 보지 말고 실패 응답의 `code`를 화면 처리 기준으로 사용합니다. 사용자에게는 서버의 `message`를 그대로 노출하기보다 `code`에 대응하는 앱 문구를 사용하는 것을 권장합니다.

## 공통 HTTP 오류

| HTTP | code | 처리 방법 |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | 입력값과 multipart 필수 파일을 확인하고 현재 화면에 오류 표시 |
| 401 | `INVALID_TOKEN` | 저장된 토큰 제거 후 로그인 화면으로 이동 |
| 403 | `ACCESS_DENIED` | 로그인 상태는 유지하고 접근 권한 없음 안내 |
| 404 | `API_NOT_FOUND` | 잘못된 클라이언트 경로이므로 오류 수집 대상 |
| 405 | `METHOD_NOT_ALLOWED` | 앱의 HTTP 메서드 구현 오류이므로 오류 수집 대상 |
| 413 | `PAYLOAD_TOO_LARGE` | 이미지 크기를 줄이도록 안내 |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | JSON 또는 multipart Content-Type 구성 확인 |
| 500 | `INTERNAL_SERVER_ERROR` | 잠시 후 재시도 안내 및 오류 수집 |

도메인 API는 `PET_NOT_FOUND`, `FAVORITE_PLACE_NOT_FOUND`, `PLACE_PROVIDER_TIMEOUT`처럼 더 구체적인 코드를 반환할 수 있습니다.

## 인증 처리

- 보호 API 요청에는 `Authorization: Bearer {accessToken}`을 전송합니다.
- 토큰이 없거나 만료·변조된 경우 모두 `401 / INVALID_TOKEN`입니다.
- `401`을 받으면 access token과 refresh token을 제거하고 로그인 화면으로 이동합니다.
- `403 / ACCESS_DENIED`는 재로그인보다 권한 부족 안내가 우선입니다.

재발급 API 자체가 `401`을 반환하면 자동 재시도를 반복하지 않습니다.

## 요청 함수 예시

```ts
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

export async function apiRequest<T>(
  input: RequestInfo,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(input, init);
  const body = (await response.json()) as ApiResponse<T>;

  if (!response.ok || body.code !== 'SUCCESS') {
    if (response.status === 401 && body.code === 'INVALID_TOKEN') {
      // 토큰 저장소 초기화 후 로그인 화면으로 이동
    }
    throw new ApiError(response.status, body.code, body.message);
  }

  return body.data as T;
}
```

네트워크 단절이나 프록시 오류에서는 JSON이 아닌 응답이 올 수 있으므로 실제 공통 함수에서는 `response.json()` 실패도 별도로 처리합니다.

## multipart 요청

React Native `FormData` 요청에는 `Content-Type`을 직접 지정하지 않습니다. 런타임이 multipart boundary를 포함한 헤더를 생성하게 둡니다.

필수 이미지 누락이나 잘못된 파라미터는 `400 / INVALID_REQUEST`, 서버 업로드 제한 초과는 `413 / PAYLOAD_TOO_LARGE`입니다.

## 이미지 조회 예외

다음 API의 성공 응답은 JSON이 아니라 이미지 바이너리입니다.

```http
GET /api/v1/beauty/style-simulations/images/{fileName}
```

이미지 컴포넌트의 URI로 직접 사용할 수 있습니다. 이미지가 없거나 요청이 실패하면 응답은 공통 JSON 오류 구조이므로 일반 이미지 로딩 실패 UI를 표시합니다.

Google Places 사진 API는 JSON으로 `photoUri`를 반환하므로 일반 공통 응답 처리 함수를 사용합니다.

## 프론트 확인 항목

1. API 공통 함수에서 `ApiResponse<T>`를 해제합니다.
2. `401`과 `403`을 구분합니다.
3. `413`에서는 이미지 용량 안내를 표시합니다.
4. 도메인 오류는 HTTP 상태보다 `code`로 분기합니다.
5. 이미지 바이너리 URL은 JSON 요청 함수로 호출하지 않습니다.
