# 미용샵 평점 및 스타일 통계 프론트엔드 연동 가이드

미용샵 검색 결과의 Google 평점 표시와 반려견 견종별 스타일 프리셋 통계 화면을 위한 API 명세입니다.

## 공통 응답

성공 응답은 다음 구조를 사용합니다.

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {}
}
```

인증이 필요한 API에는 `Authorization: Bearer {accessToken}`을 전달합니다.

## 미용샵 평점

주변 조회와 이름 검색의 각 장소에 `rating`, `userRatingCount`가 포함됩니다.

```http
GET /api/v1/places/nearby?latitude=37.5000530744517&longitude=126.86799268384159&radiusMeters=10000&limit=20
GET /api/v1/places/search?query=멍살롱&scope=NATIONWIDE
```

```json
{
  "googlePlaceId": "ChIJ...",
  "name": "멍살롱",
  "formattedAddress": "서울특별시 ...",
  "latitude": 37.5,
  "longitude": 126.86,
  "rating": 4.7,
  "userRatingCount": 128,
  "distanceMeters": 320,
  "googleMapsUri": "https://maps.google.com/..."
}
```

### 표시 규칙

- `rating`은 Google Places 기준 `1.0~5.0` 범위입니다.
- `rating`은 소수점 한 자리까지 표시합니다.
- `rating`이 `null`이면 별점 숫자 대신 `평점 없음`으로 표시합니다.
- `userRatingCount`는 평점 옆에 `(128)` 형태로 표시할 수 있습니다.
- `userRatingCount`가 `0`이면 평가 수를 생략할 수 있습니다.
- 현재 API는 평점 범위 필터나 평점순 정렬을 제공하지 않습니다. 클라이언트에서 받은 한 페이지 결과만 재정렬하면 전체 검색 결과 순서와 달라질 수 있으므로 임의 정렬은 권장하지 않습니다.

`distanceMeters`는 주변 검색에서만 제공되며 전국 이름 검색에서는 `null`입니다.

## 견종별 스타일 통계

```http
GET /api/v1/beauty/style-presets/statistics?petId=101
Authorization: Bearer {accessToken}
```

`petId`는 현재 로그인 사용자가 소유한 반려동물 ID입니다. 해당 반려동물과 같은 견종을 가진 다른 사용자들이 가장 최근 선택한 프리셋을 집계합니다.

```json
{
  "code": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "breedCode": "POODLE",
    "breedName": "푸들",
    "totalUsers": 20,
    "statistics": [
      {
        "stylePresetId": 1,
        "name": "곰돌이컷",
        "description": "얼굴을 둥글게 다듬는 스타일",
        "exampleImageUrl": null,
        "selectedUserCount": 12,
        "percentage": 60.0
      },
      {
        "stylePresetId": 2,
        "name": "물개컷",
        "description": "얼굴과 귀의 경계를 부드럽게 다듬는 스타일",
        "exampleImageUrl": null,
        "selectedUserCount": 8,
        "percentage": 40.0
      }
    ]
  }
}
```

### 집계 기준

- 현재 로그인 사용자는 통계에서 제외됩니다.
- 사용자 한 명당 같은 견종의 최신 프리셋 선택 한 건만 반영됩니다.
- 이미지 생성 요청에서 `stylePresetId`를 선택한 경우에만 통계에 반영됩니다.
- 직접 입력이나 참고 이미지만 사용한 생성 요청은 통계에 포함되지 않습니다.
- 선택되지 않은 활성 프리셋도 `selectedUserCount: 0`, `percentage: 0.0`으로 반환됩니다.
- `percentage`는 소수점 한 자리의 `0.0~100.0` 범위입니다.
- `totalUsers`가 `0`이면 모든 비율을 `0.0`으로 표시하고 `아직 선택 데이터가 없습니다` 상태를 보여줍니다.

### 권장 화면 흐름

1. 사용자가 통계를 볼 반려동물을 선택합니다.
2. 선택한 `petId`로 통계 API를 요청합니다.
3. `percentage`를 막대 길이로, `selectedUserCount`를 `12명 선택`처럼 표시합니다.
4. 사용자가 통계 항목을 선택하면 해당 `stylePresetId`를 이미지 생성 요청에 사용합니다.
5. 반려동물 변경 시 이전 통계를 비우고 새 `petId`로 다시 조회합니다.

## 오류 처리

| HTTP | 코드 | 프론트 처리 |
| --- | --- | --- |
| 400 | `PET_BREED_REQUIRED` | 반려동물 프로필에서 견종을 등록하도록 안내 |
| 401 | `INVALID_TOKEN` | 토큰 재발급 또는 로그인 화면 이동 |
| 404 | `PET_NOT_FOUND` | 삭제되었거나 소유하지 않은 반려동물 안내 후 목록 갱신 |
| 502 | `PLACE_PROVIDER_UNAVAILABLE` | 장소 정보를 불러올 수 없다는 재시도 안내 |
| 504 | `PLACE_PROVIDER_TIMEOUT` | 네트워크 상태 확인 및 재시도 안내 |

## TypeScript 타입 예시

```ts
type PlaceItem = {
  googlePlaceId: string;
  name: string;
  formattedAddress: string | null;
  latitude: number;
  longitude: number;
  rating: number | null;
  userRatingCount: number;
  distanceMeters: number | null;
  googleMapsUri: string | null;
};

type GroomingStyleStatistic = {
  stylePresetId: number;
  name: string;
  description: string | null;
  exampleImageUrl: string | null;
  selectedUserCount: number;
  percentage: number;
};

type GroomingStyleStatistics = {
  breedCode: string;
  breedName: string;
  totalUsers: number;
  statistics: GroomingStyleStatistic[];
};
```
