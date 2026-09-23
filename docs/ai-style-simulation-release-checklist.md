# AI 스타일 시뮬레이션 배포·시연 체크리스트

AI 스타일 시뮬레이션 기능을 EC2에 배포하고 졸업작품 시연을 준비할 때 수행할 작업입니다. API 계약은 [beauty-style-simulation-api.md](beauty-style-simulation-api.md)를 참고합니다.

## 1. 배포 환경

- [ ] EC2 인스턴스에서 Java, Docker 및 PostgreSQL 연결 상태를 확인합니다.
- [ ] 생성 이미지와 임시 이미지 디렉터리를 애플리케이션 컨테이너에서 쓰기 가능한 경로로 준비합니다.
- [ ] 생성 이미지 디렉터리는 EC2 인스턴스 스토어가 아닌 EBS 볼륨에 둡니다.
- [ ] 임시 이미지 디렉터리는 재시작 시 유실되어도 되는 별도 경로로 둡니다.
- [ ] Docker를 사용한다면 두 경로를 컨테이너 volume으로 명시합니다.
- [ ] 이미지 API를 외부에서 조회할 수 있도록 HTTPS와 리버스 프록시 경로를 확인합니다.

권장 환경 변수 예시:

```properties
IMAGE_STORAGE_DIRECTORY=/data/singlemungle/generated-images
IMAGE_STORAGE_TEMP_DIRECTORY=/data/singlemungle/temp-images
IMAGE_STORAGE_BASE_URL=/api/v1/beauty/style-simulations/images
IMAGE_STORAGE_CLEANUP_ENABLED=true
IMAGE_STORAGE_TEMP_RETENTION_HOURS=24
```

운영 경로와 권한은 배포 계정 기준으로 설정하고 저장 디렉터리 자체를 Git에 포함하지 않습니다.

## 2. Vertex AI

- [ ] 운영 GCP 프로젝트에서 Vertex AI API가 활성화되어 있는지 확인합니다.
- [ ] `VERTEX_AI_PROJECT_ID`, `VERTEX_AI_LOCATION`, `VERTEX_AI_IMAGE_MODEL`을 운영 값으로 설정합니다.
- [ ] EC2에서는 개인 `gcloud auth application-default login` 토큰을 사용하지 않습니다.
- [ ] 최소 권한을 가진 서비스 계정 또는 Workload Identity Federation 인증을 구성합니다.
- [ ] 서비스 계정 키 파일을 사용할 경우 저장소·Docker 이미지·로그에 포함되지 않도록 관리합니다.
- [ ] 실제 생성 1건으로 인증, 리전, 모델 접근 권한과 결제 프로젝트를 확인합니다.
- [ ] GCP 예산 알림과 사용량 모니터링을 설정합니다.

## 3. DB와 마이그레이션

- [ ] 배포 전 PostgreSQL을 백업합니다.
- [ ] Flyway가 `V10__create_reviews_and_style_presets.sql`, `V11__extend_style_simulation_sources.sql`을 순서대로 적용하는지 확인합니다.
- [ ] `grooming_style_presets`에 기본 프리셋 5개가 생성됐는지 확인합니다.
- [ ] 기존 `beauty_style_simulations` 데이터 조회가 유지되는지 확인합니다.
- [ ] 애플리케이션 시작 시 JPA `validate`가 성공하는지 확인합니다.

## 4. 프론트 연동

- [ ] 프론트가 기존 `styleKeyword` 입력 대신 `stylePresetId` 또는 `referenceReviewImageId`를 전송하도록 변경합니다.
- [ ] 프리셋, 리뷰 이미지, 직접 업로드, 추가 요청 중 하나 이상을 선택하도록 검증합니다.
- [ ] `referenceReviewImageId`와 `styleImage`를 동시에 전송하지 않습니다.
- [ ] 리뷰 이미지 중 `aiReferenceAllowed=true`인 이미지에만 AI 참고 선택 UI를 표시합니다.
- [ ] 생성 요청의 `202 / PENDING` 응답 이후 상세 API를 폴링합니다.
- [ ] `COMPLETED`, `FAILED`, 60초 타임아웃과 화면 이탈 시 폴링 중단을 처리합니다.
- [ ] 상대 이미지 URL 앞에 운영 API Base URL을 붙입니다.

## 5. 시연 데이터

- [ ] 팀이 소유하거나 사용 허가를 받은 반려견 미용 결과 이미지를 준비합니다.
- [ ] 이미지를 `demo/review-images`에 두고 저장소에는 커밋하지 않습니다.
- [ ] 운영 또는 시연 DB의 실제 사용자·미용샵 ID를 확인합니다.
- [ ] `scripts/demo-reviews.json`의 `userId`, `shopId`, 파일명을 실제 값으로 수정합니다.
- [ ] PostgreSQL 컨테이너와 Flyway 마이그레이션 실행 후 아래 명령을 실행합니다.

```bash
python3 scripts/seed_demo_reviews.py
```

- [ ] 리뷰 조회 API에서 이미지와 `aiReferenceAllowed=true`를 확인합니다.
- [ ] 시연할 프리셋 및 리뷰 이미지 ID를 미리 기록합니다.
- [ ] 시연 계정의 JWT 로그인과 반려동물 데이터가 준비되어 있는지 확인합니다.

시드 도구는 동일 manifest 항목을 다시 실행해도 중복 리뷰 이미지를 만들지 않습니다. 기본 JSON의 ID는 예시이므로 그대로 운영 DB에 사용하면 안 됩니다.

## 6. 배포 후 스모크 테스트

다음 순서로 Swagger와 실제 앱에서 각각 확인합니다.

1. `GET /api/v1/beauty/style-presets`
2. 리뷰 작성 및 리뷰 목록 조회
3. 동의되지 않은 리뷰 이미지 사용 시 `AI_REFERENCE_NOT_ALLOWED`
4. 프리셋만 사용한 이미지 생성
5. 동의된 리뷰 이미지를 사용한 이미지 생성
6. 직접 업로드 이미지와 추가 요청을 사용한 이미지 생성
7. `PENDING`에서 `COMPLETED` 또는 `FAILED`로 상태 전환
8. 생성 이미지 URL 조회 및 파일명/Content-Type 확인
9. 다른 사용자의 반려동물과 시뮬레이션 접근 차단 확인
10. 실패 및 성공 후 임시 이미지 디렉터리에 파일이 남지 않는지 확인

## 7. 시연 직전

- [ ] GCP 할당량, 결제 상태 및 예산 알림을 확인합니다.
- [ ] EC2 디스크 여유 공간과 애플리케이션 로그를 확인합니다.
- [ ] 실제 생성 요청을 2~3회 실행해 평균 대기 시간을 측정합니다.
- [ ] 시연용 원본 이미지와 참고 리뷰 이미지를 미리 선정합니다.
- [ ] Vertex AI 장애에 대비해 완료된 생성 결과 화면 또는 녹화본을 준비합니다.
- [ ] 토큰, 서비스 계정 정보, 사용자 개인정보가 발표 화면과 로그에 노출되지 않는지 확인합니다.

## 후속 운영 항목

졸업작품 시연 이후 사용량이 증가할 때 검토합니다.

- 생성 요청 횟수 제한과 사용자별 비용 통제
- 부적절한 이미지 검사 및 신고 처리
- 외부 AI 전송 동의 약관과 철회 절차
- 로컬/EBS 저장소를 S3 또는 GCS로 전환
- 애플리케이션 내부 비동기 실행기를 외부 작업 큐로 전환
- 프롬프트 및 프리셋별 결과 품질 측정
