# Google Places 애견 미용샵 API

Places API (New)를 이용해 애견 미용샵만 실시간 조회합니다. Google 장소 데이터는 DB에 복제하지 않으며, 자체 리뷰와 AI 시뮬레이션에는 문자열 `googlePlaceId`만 저장합니다.

`V13__drop_legacy_place_tables.sql` 적용 후 기존 카카오 장소용 `places`, `place_*`, `beauty_styles` 테이블과 숫자 장소 ID 컬럼은 제거됩니다.

## 환경 설정

```properties
GOOGLE_PLACES_ENABLED=true
GOOGLE_PLACES_API_KEY=발급받은_서버용_API_KEY
GOOGLE_PLACES_CENTER_LATITUDE=37.5000530744517
GOOGLE_PLACES_CENTER_LONGITUDE=126.86799268384159
GOOGLE_PLACES_DEFAULT_RADIUS_METERS=10000
GOOGLE_PLACES_MAX_RADIUS_METERS=10000
```

API 키는 Places API (New)로 제한하고, 운영 환경에서는 백엔드 서버의 IP 제한을 추가합니다. 모바일 앱에 서버용 API 키를 포함하지 않습니다.

## 주변 미용샵

```http
GET /api/v1/places/nearby?latitude=37.5000530744517&longitude=126.86799268384159&radiusMeters=10000&limit=20
```

좌표와 반경을 생략하면 시연 기본값을 사용합니다. 응답의 `googlePlaceId`를 상세 조회, 자체 리뷰, 즐겨찾기, AI 시뮬레이션에 그대로 전달합니다. `nextPageToken`이 있으면 다음 요청의 `pageToken`으로 전달합니다.

## 상세 조회

```http
GET /api/v1/places/{googlePlaceId}
```

이름, 주소, 좌표, 전화번호, 웹사이트, Google Maps URL, 영업시간, 평점 및 사진 리소스 이름을 반환합니다. Google 장소 정보는 장기 저장하지 않고 화면 진입 시 조회합니다.

## 장소 사진

```http
GET /api/v1/places/photos?photoName={photoName}&maxWidthPx=800
```

상세 응답의 `photos[].photoName`을 전달하면 `photoUri`를 반환합니다. URL은 단기 URL이므로 DB에 저장하지 말고 화면 표시 시 사용하며, 만료되면 다시 요청합니다. `maxWidthPx`는 1~1,600만 허용합니다.

## 프론트 처리

1. 현재 위치 권한이 있으면 사용자 좌표로 주변 API를 호출합니다.
2. 권한이 없거나 시연 모드이면 요청 좌표를 생략합니다.
3. 응답 좌표로 Google Map 마커를 표시합니다.
4. 마커 선택 시 `googlePlaceId`로 상세 API를 호출합니다.
5. 사진이 있으면 `photoName`으로 사진 URL API를 호출합니다.
6. 자체 리뷰 및 AI 시뮬레이션 요청에도 동일한 `googlePlaceId`를 사용합니다.

Google Places 장애 시 `PLACE_PROVIDER_UNAVAILABLE`, 시간 초과 시 `PLACE_PROVIDER_TIMEOUT`, 호출 한도 초과 시 `PLACE_PROVIDER_RATE_LIMITED`가 반환됩니다.
