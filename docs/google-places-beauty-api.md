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

상세 조회와 연계 기능은 Google Places의 `pet_care` 타입을 검증합니다. 상호명은 업체마다 표현이 다양하므로 미용 키워드를 필수로 요구하지 않으며, 학원·아카데미·교육원 등 명확한 교육기관은 검색과 상세 조회에서 제외합니다. 프론트는 주변 애견 미용샵 조회 응답에서 받은 `googlePlaceId`를 사용해야 합니다.

## 상세 조회

```http
GET /api/v1/places/{googlePlaceId}
```

이름, 주소, 좌표, 전화번호, 웹사이트, Google Maps URL, 영업시간, 평점, 사진 리소스 이름과 Google 리뷰를 반환합니다. Google 장소 정보는 장기 저장하지 않고 화면 진입 시 조회합니다.

`googleReviews`는 Google이 제공하는 제한된 수의 관련성 순 리뷰입니다. 각 항목은 평점, 번역된 본문과 원문, 게시 시점, 작성자 attribution, Google Maps 원문 링크 및 신고 링크를 포함합니다. 프론트는 Google 리뷰와 싱글멍글 자체 리뷰를 별도 탭이나 섹션으로 구분하고 다음 기준을 지켜야 합니다.

- Google Maps 제공 콘텐츠임을 표시합니다.
- 작성자 이름, 프로필 사진과 프로필 링크를 가능한 범위에서 함께 표시합니다.
- 각 리뷰에 `googleMapsUri`를 이용한 원문 이동 경로를 제공합니다.
- Google 리뷰는 제공된 관련성 순서 그대로 표시하고 해당 정렬 기준을 안내합니다.
- Google 리뷰를 DB나 장기 로컬 캐시에 저장하지 않습니다.
- Google 리뷰 및 Google 사진을 AI 스타일 시뮬레이션 참고 입력으로 사용하지 않습니다.

싱글멍글 자체 리뷰만 작성·수정·삭제 및 AI 참고 이미지 동의 대상으로 사용합니다.

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

## 이름 검색

```http
GET /api/v1/places/search?query=멍살롱&latitude=37.5000530744517&longitude=126.86799268384159&radiusMeters=10000&limit=20
```

`query`는 필수이며 최대 100자입니다. 좌표와 반경을 생략하면 시연 기본값을 사용합니다. 주변 조회와 동일하게 교육기관을 제외하며, 결과의 `googlePlaceId`로 상세 API를 호출합니다. `nextPageToken`이 있으면 같은 검색 조건과 함께 다음 요청에 전달합니다.

## 즐겨찾기

모든 즐겨찾기 API에는 `Authorization: Bearer {accessToken}`이 필요합니다.

```http
POST   /api/v1/users/me/favorite-places
GET    /api/v1/users/me/favorite-places?page=0&size=20
PATCH  /api/v1/users/me/favorite-places/{favoritePlaceId}
DELETE /api/v1/users/me/favorite-places/{favoritePlaceId}
```

등록 요청은 `googlePlaceId`와 선택값 `memo`(최대 500자)를 받습니다. 같은 사용자가 같은 장소를 중복 등록하면 `FAVORITE_PLACE_ALREADY_EXISTS`, 본인 소유가 아닌 즐겨찾기에 접근하면 `FAVORITE_PLACE_NOT_FOUND`를 반환합니다.

목록 응답은 `favoritePlaceId`, `googlePlaceId`, `memo`, `createdAt`을 제공합니다. Google 장소의 이름과 사진 등 최신 정보가 필요하면 `googlePlaceId`로 장소 상세 API를 호출합니다.
