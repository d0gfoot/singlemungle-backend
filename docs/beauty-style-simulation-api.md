# AI 스타일 시뮬레이션 API 연동 가이드

React Native 앱에서 AI 미용 스타일 시뮬레이션 API를 연동하기 위한 명세입니다.

## 공통 사항

- Base URL 예시: `http://localhost:8080`
- 인증: `Authorization: Bearer {accessToken}`
- 이미지 형식: JPEG, PNG, WebP
- 이미지당 최대 크기: 10MB
- 생성 결과 보관 기간: 생성 시점부터 30일
- 인증 필터에서 차단되는 요청과 이미지 조회를 제외한 컨트롤러 응답은 아래 형식을 사용합니다.

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {}
}
```

## 연동 흐름

```text
스타일 목록 조회
  -> 이미지 생성 요청
  -> 202 Accepted / PENDING
  -> 상세 조회 API 폴링
     -> PENDING: 대기 후 다시 조회
     -> COMPLETED: generatedImageUrl 표시
     -> FAILED: 폴링 중단 및 오류 안내
```

이미지 생성은 비동기로 처리됩니다. 생성 요청 응답에 이미지가 즉시 포함되지 않으므로 `simulationId`를 저장하고 상세 조회 API로 상태를 확인해야 합니다.

권장 폴링 정책:

- 최초 조회: 생성 요청 응답을 받은 뒤 1초 후
- 조회 간격: 2초
- 최대 대기 시간: 60초
- `COMPLETED` 또는 `FAILED`가 되면 즉시 중단
- 화면 이탈이나 앱 백그라운드 전환 시 타이머와 요청 취소
- 60초가 지나도 `PENDING`이면 생성 기록은 유지하고 나중에 목록 또는 상세 조회로 재확인

## 상태 정의

| 상태 | 의미 | 프론트 처리 |
| --- | --- | --- |
| `PENDING` | 요청 접수 후 생성 중 | 로딩 표시 및 폴링 유지 |
| `COMPLETED` | 이미지 생성 완료 | `generatedImageUrl` 이미지 표시 |
| `FAILED` | 이미지 생성 실패 | 폴링 중단, 재시도 안내 |

## 응답 객체

`BeautyStyleSimulationResponse`

| 필드 | 타입 | nullable | 설명 |
| --- | --- | --- | --- |
| `simulationId` | number | N | 시뮬레이션 ID |
| `petId` | number | N | 반려동물 ID |
| `shopId` | number | N | 미용샵 장소 ID |
| `styleKeyword` | string | N | 적용할 스타일 키워드 |
| `additionalRequest` | string | Y | 사용자 추가 요청 |
| `sourceImageUrl` | string | Y | 업로드한 반려견 이미지 경로. 실패 시 null |
| `styleImageUrl` | string | Y | 참고 스타일 이미지 경로. 미제공 또는 실패 시 null |
| `generatedImageUrl` | string | Y | 생성 이미지 경로. 완료 전 또는 실패 시 null |
| `status` | string | N | `PENDING`, `COMPLETED`, `FAILED` |
| `failureReason` | string | Y | 실패 코드. 성공 또는 처리 중이면 null |
| `expiresAt` | string | N | 이미지 만료 시각, ISO-8601 |
| `createdAt` | string | N | 요청 생성 시각, ISO-8601 |

이미지 URL은 `/api/v1/beauty/style-simulations/images/{fileName}` 형태의 상대 경로입니다. 화면에 표시할 때 앱의 API Base URL을 앞에 붙여야 합니다.

## 1. 미용샵 스타일 목록 조회

```http
GET /api/v1/beauty-shops/{shopId}/styles
Authorization: Bearer {accessToken}
```

성공 응답: `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": [
    {
      "styleId": 1,
      "styleName": "곰돌이컷",
      "description": "얼굴을 둥글게 다듬는 스타일",
      "imageUrl": "https://example.com/style1.jpg"
    }
  ]
}
```

현재 생성 API는 `styleId`가 아니라 선택한 스타일의 `styleName`을 `styleKeyword`로 전달합니다.

## 2. 이미지 생성 요청

```http
POST /api/v1/beauty/style-simulations
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

요청 필드:

| 이름 | 타입 | 필수 | 제한 |
| --- | --- | --- | --- |
| `petId` | number | Y | 로그인 사용자가 소유한 반려동물 |
| `shopId` | number | Y | `BEAUTY` 타입 장소 |
| `styleKeyword` | string | Y | 공백 제외, 최대 100자 |
| `additionalRequest` | string | N | 최대 500자 |
| `petImage` | file | Y | JPEG, PNG, WebP, 최대 10MB |
| `styleImage` | file | N | 참고 스타일 이미지, JPEG, PNG, WebP, 최대 10MB |

성공 응답: `202 Accepted`

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "simulationId": 42,
    "petId": 7,
    "shopId": 3,
    "styleKeyword": "곰돌이컷",
    "additionalRequest": "좀 더 샤프하고 날렵하게",
    "sourceImageUrl": "/api/v1/beauty/style-simulations/images/source-id.jpg",
    "styleImageUrl": null,
    "generatedImageUrl": null,
    "status": "PENDING",
    "failureReason": null,
    "expiresAt": "2026-10-22T18:00:00",
    "createdAt": "2026-09-22T18:00:00"
  }
}
```

React Native 예시:

```ts
type UploadImage = {
  uri: string;
  name: string;
  type: 'image/jpeg' | 'image/png' | 'image/webp';
};

async function createStyleSimulation(params: {
  apiBaseUrl: string;
  accessToken: string;
  petId: number;
  shopId: number;
  styleKeyword: string;
  additionalRequest?: string;
  petImage: UploadImage;
  styleImage?: UploadImage;
}) {
  const formData = new FormData();
  formData.append('petId', String(params.petId));
  formData.append('shopId', String(params.shopId));
  formData.append('styleKeyword', params.styleKeyword);

  if (params.additionalRequest?.trim()) {
    formData.append('additionalRequest', params.additionalRequest.trim());
  }

  formData.append('petImage', params.petImage as unknown as Blob);
  if (params.styleImage) {
    formData.append('styleImage', params.styleImage as unknown as Blob);
  }

  const response = await fetch(
    `${params.apiBaseUrl}/api/v1/beauty/style-simulations`,
    {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${params.accessToken}`,
      },
      body: formData,
    },
  );

  const body = await response.json();
  if (!response.ok) {
    throw new Error(body.code ?? 'STYLE_SIMULATION_REQUEST_FAILED');
  }
  return body.data;
}
```

`FormData` 전송 시 `Content-Type`을 직접 설정하지 않아야 합니다. React Native의 네트워크 구현이 multipart boundary를 포함한 헤더를 자동으로 생성합니다.

## 3. 생성 상태 및 상세 조회

```http
GET /api/v1/beauty/style-simulations/{simulationId}
Authorization: Bearer {accessToken}
```

성공 응답: `200 OK`

완료 예시:

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "simulationId": 42,
    "petId": 7,
    "shopId": 3,
    "styleKeyword": "곰돌이컷",
    "additionalRequest": "좀 더 샤프하고 날렵하게",
    "sourceImageUrl": "/api/v1/beauty/style-simulations/images/source-id.jpg",
    "styleImageUrl": null,
    "generatedImageUrl": "/api/v1/beauty/style-simulations/images/generated-id.jpg",
    "status": "COMPLETED",
    "failureReason": null,
    "expiresAt": "2026-10-22T18:00:00",
    "createdAt": "2026-09-22T18:00:00"
  }
}
```

폴링 예시:

```ts
const wait = (milliseconds: number) =>
  new Promise(resolve => setTimeout(resolve, milliseconds));

async function waitForSimulation(params: {
  apiBaseUrl: string;
  accessToken: string;
  simulationId: number;
  signal?: AbortSignal;
}) {
  const deadline = Date.now() + 60_000;

  await wait(1_000);
  while (Date.now() < deadline) {
    const response = await fetch(
      `${params.apiBaseUrl}/api/v1/beauty/style-simulations/${params.simulationId}`,
      {
        headers: {Authorization: `Bearer ${params.accessToken}`},
        signal: params.signal,
      },
    );
    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.code ?? 'STYLE_SIMULATION_LOOKUP_FAILED');
    }
    if (body.data.status === 'COMPLETED') {
      return body.data;
    }
    if (body.data.status === 'FAILED') {
      throw new Error(body.data.failureReason ?? 'IMAGE_GENERATION_FAILED');
    }

    await wait(2_000);
  }

  throw new Error('STYLE_SIMULATION_POLLING_TIMEOUT');
}
```

## 4. 내 생성 결과 목록 조회

```http
GET /api/v1/beauty/style-simulations?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {accessToken}
```

성공 응답: `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "hasNext": false
  }
}
```

목록에는 삭제되지 않았고 만료 시각이 지나지 않은 현재 사용자의 결과만 반환됩니다.

## 5. 생성 결과 삭제

```http
DELETE /api/v1/beauty/style-simulations/{simulationId}
Authorization: Bearer {accessToken}
```

성공 응답: `200 OK`

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": null
}
```

DB 기록은 soft delete로 처리되어 이후 상세 및 목록 조회에서 제외되며, 연결된 원본·참고·생성 이미지 파일은 함께 삭제됩니다.

## 6. 이미지 조회 및 표시

```http
GET /api/v1/beauty/style-simulations/images/{fileName}
Authorization: Bearer {accessToken}
```

응답은 JSON이 아닌 이미지 바이너리이며 파일에 따라 `image/jpeg`, `image/png`, `image/webp`가 반환됩니다.

React Native `Image` 예시:

```tsx
const imageSource = {
  uri: `${apiBaseUrl}${simulation.generatedImageUrl}`,
  headers: {
    Authorization: `Bearer ${accessToken}`,
  },
};

<Image source={imageSource} resizeMode="contain" />;
```

`generatedImageUrl`이 null이거나 상태가 `COMPLETED`가 아니면 이미지 컴포넌트를 렌더링하지 않습니다.

## 오류 코드

| HTTP 상태 | 코드 | 발생 조건 |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | 필수값 누락, 문자열 길이 초과, 지원하지 않는 이미지 형식 또는 10MB 초과 |
| `403` | 응답 본문 없음 | Access Token 누락, 만료 또는 위조 |
| `404` | `PET_NOT_FOUND` | 반려동물이 없거나 현재 사용자 소유가 아님 |
| `404` | `PLACE_NOT_FOUND` | 장소가 없거나 미용샵 타입이 아님 |
| `404` | `STYLE_SIMULATION_NOT_FOUND` | 결과가 없거나 다른 사용자의 결과이거나 삭제됨 |
| `500` | `INTERNAL_SERVER_ERROR` | 처리되지 않은 서버 오류 |

Vertex AI 생성 실패는 생성 요청 자체의 HTTP 오류가 아니라 비동기 처리 결과의 `status: FAILED`와 `failureReason: IMAGE_GENERATION_FAILED`로 전달됩니다.

현재 Spring Security 인증 실패 응답은 공통 JSON 형식으로 변환되지 않습니다. 프론트에서는 `401`만이 아니라 `403`도 로그인 갱신 또는 재로그인이 필요한 인증 오류로 처리해야 합니다.

## 프론트 구현 체크리스트

- Access Token을 모든 요청과 이미지 조회에 전달
- 생성 요청의 `202 Accepted`를 정상 성공으로 처리
- 생성 직후 `generatedImageUrl`이 null인 상태 허용
- `simulationId`를 기준으로 상세 조회 폴링
- 화면 이탈 시 폴링 중단
- `COMPLETED`와 `FAILED` 모두 폴링 종료 상태로 처리
- 상대 이미지 URL 앞에 API Base URL 추가
- 앱 재실행 후 목록 조회로 진행 중이거나 완료된 요청 복구
- 30일 만료 이후 이미지가 조회되지 않을 수 있음을 고려
