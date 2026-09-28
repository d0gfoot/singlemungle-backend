# AI 스타일 시뮬레이션 API 연동 가이드

React Native 앱에서 자체 리뷰 이미지, 공통 스타일 프리셋, 사용자 업로드 이미지를 이용해 AI 미용 결과를 생성하는 API 명세입니다.

배포와 발표 준비 작업은 [ai-style-simulation-release-checklist.md](ai-style-simulation-release-checklist.md)를 참고합니다.

## 공통

- 인증: `Authorization: Bearer {accessToken}`
- 이미지: JPEG, PNG, WebP, 파일당 최대 10MB
- 생성 결과 보관: 30일
- 반려견 원본과 참고 이미지는 작업 종료 후 삭제되며 생성 이미지만 저장됩니다.

## 화면 흐름

1. `GET /api/v1/beauty/style-presets`로 공통 스타일을 조회합니다.
2. 리뷰에서 `aiReferenceAllowed=true`인 이미지에만 AI 참고 선택 버튼을 표시합니다.
3. 프리셋, 리뷰 이미지, 직접 업로드 참고 이미지, 추가 요청 중 하나 이상을 선택합니다.
4. 생성 API의 `202 Accepted / PENDING` 응답에서 `simulationId`를 저장합니다.
5. 1초 후부터 2초 간격으로 상세 API를 조회합니다.
6. `COMPLETED` 또는 `FAILED`에서 중단하며 최대 대기는 60초를 권장합니다.

## 스타일 프리셋

```http
GET /api/v1/beauty/style-presets
Authorization: Bearer {accessToken}
```

응답 `data`는 `stylePresetId`, `name`, `description`, `exampleImageUrl`을 가진 배열입니다.

## 자체 리뷰

```http
POST   /api/v1/beauty-shops/{googlePlaceId}/reviews
GET    /api/v1/beauty-shops/{googlePlaceId}/reviews?page=0&size=20
GET    /api/v1/beauty-shops/{googlePlaceId}/reviews/{reviewId}
PATCH  /api/v1/beauty-shops/{googlePlaceId}/reviews/{reviewId}
DELETE /api/v1/beauty-shops/{googlePlaceId}/reviews/{reviewId}
```

작성은 multipart이며 `rating`(1~5), `content`(최대 2,000자), 선택값 `groomingStyleName`, `aiReferenceAllowed`, `images`(최대 5개)를 받습니다. 수정 body는 `rating`, `content`, `groomingStyleName`, `aiReferenceAllowed`입니다. 수정과 삭제는 작성자만 가능합니다.

## 생성 요청

```http
POST /api/v1/beauty/style-simulations
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `petId` | number | Y | 현재 사용자가 소유한 반려동물 |
| `googlePlaceId` | string | N | Google Places의 장소 ID. 리뷰 이미지 선택 시 리뷰의 미용샵으로 자동 결정 가능 |
| `stylePresetId` | number | N | 공통 스타일 프리셋 ID |
| `referenceReviewImageId` | number | N | AI 참고 동의된 자체 리뷰 이미지 ID |
| `additionalRequest` | string | N | 최대 500자 |
| `petImage` | file | Y | 반려견 원본 이미지 |
| `styleImage` | file | N | 사용자가 직접 선택한 참고 이미지 |

프리셋, 리뷰 이미지, 직접 업로드 참고 이미지, 추가 요청 중 하나 이상이 필요합니다. `referenceReviewImageId`와 `styleImage`는 동시에 보낼 수 없습니다. 프리셋은 참고 이미지나 추가 요청과 조합할 수 있습니다.

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "simulationId": 42,
    "petId": 7,
    "googlePlaceId": "ChIJ...",
    "styleKeyword": "물개컷",
    "stylePresetId": 2,
    "referenceReviewImageId": 10,
    "sourceType": "REVIEW_IMAGE",
    "additionalRequest": "귀는 조금 더 짧게",
    "sourceImageUrl": null,
    "styleImageUrl": null,
    "generatedImageUrl": null,
    "status": "PENDING",
    "failureReason": null,
    "expiresAt": "2026-10-22T18:00:00",
    "createdAt": "2026-09-22T18:00:00"
  }
}
```

`sourceType`은 `REVIEW_IMAGE`, `USER_UPLOAD`, `PRESET`, `CUSTOM_REQUEST` 중 하나입니다. `styleKeyword`는 선택한 프리셋 또는 리뷰 스타일명을 보여주는 호환용 응답 필드입니다.

```ts
const form = new FormData();
form.append('petId', String(petId));
form.append('googlePlaceId', googlePlaceId);
form.append('stylePresetId', String(stylePresetId));
form.append('referenceReviewImageId', String(reviewImageId));
form.append('additionalRequest', additionalRequest.trim());
form.append('petImage', petImage as unknown as Blob);

const response = await fetch(`${apiBaseUrl}/api/v1/beauty/style-simulations`, {
  method: 'POST',
  headers: {Authorization: `Bearer ${accessToken}`},
  body: form,
});
```

`FormData`의 `Content-Type`은 직접 설정하지 않습니다. React Native가 multipart boundary를 생성합니다.

## 상태 조회

```http
GET /api/v1/beauty/style-simulations/{simulationId}
GET /api/v1/beauty/style-simulations?page=0&size=20
DELETE /api/v1/beauty/style-simulations/{simulationId}
```

- `PENDING`: 폴링 유지
- `COMPLETED`: `generatedImageUrl` 표시 후 종료
- `FAILED`: `failureReason`을 기록하고 재시도 안내

이미지 URL은 상대 경로이므로 API Base URL을 앞에 붙입니다. 상세/목록/삭제는 본인의 결과만 접근할 수 있습니다.

## 주요 오류

`PET_NOT_FOUND`, `PLACE_NOT_FOUND`, `STYLE_PRESET_NOT_FOUND`, `REVIEW_IMAGE_NOT_FOUND`, `AI_REFERENCE_NOT_ALLOWED`, `INVALID_REQUEST`, `IMAGE_GENERATION_FAILED`를 처리해야 합니다.
