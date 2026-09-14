package com.singlemungle.backend.domain.walk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.pet.entity.Pet;
import com.singlemungle.backend.domain.pet.repository.PetRepository;
import com.singlemungle.backend.domain.walk.dto.request.WalkCreateRequest;
import com.singlemungle.backend.domain.walk.dto.response.MonthlyWalkSummaryResponse;
import com.singlemungle.backend.domain.walk.dto.response.WalkListResponse;
import com.singlemungle.backend.domain.walk.dto.response.WalkResponse;
import com.singlemungle.backend.domain.walk.entity.Walk;
import com.singlemungle.backend.domain.walk.repository.WalkRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkService {

    private final WalkRepository walkRepository;
    private final PetRepository petRepository;
    private final ObjectMapper objectMapper;


    /**
     * 최근 산책 기록 조회
     */
    public List<WalkResponse> getRecentWalks(
            Long userId,
            Long petId,
            int limit
    ) {
        // 로그인한 사용자의 반려동물인지 확인
        Pet pet = getOwnedPet(userId, petId);

        if (limit <= 0) {
            limit = 5;
        }

        Page<Walk> walks =
                walkRepository.findByUserIdAndPetIdOrderByStartedAtDesc(
                        userId,
                        pet.getId(),
                        Pageable.ofSize(limit)
                );

        return walks.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /**
     * 전체 산책 기록 조회
     */
    public WalkListResponse getWalks(
            Long userId,
            Long petId,
            Pageable pageable
    ) {
        Pet pet = getOwnedPet(userId, petId);

        Page<Walk> walks =
                walkRepository.findByUserIdAndPetIdOrderByStartedAtDesc(
                        userId,
                        pet.getId(),
                        pageable
                );

        List<WalkResponse> content =
                walks.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new WalkListResponse(
                content,
                walks.getNumber(),
                walks.getSize(),
                walks.hasNext()
        );
    }


    /**
     * 산책 기록 등록
     *
     * 칼로리 계산
     *
     * 1. 반려동물 체중으로 체급 분류
     *    - 10kg 미만       : 소형
     *    - 10kg ~ 25kg 미만: 중형
     *    - 25kg 이상       : 대형
     *
     * 2. RER
     *    = 70 × 체중(kg)^0.75
     *
     * 3. MER
     *    - 중성화     : RER × 1.6
     *    - 미중성화   : RER × 1.8
     *
     * 4. 산책 칼로리 추정
     *    = MER × (산책시간 / 하루 1440분)
     *
     * ※ 체급은 반려동물 분류용으로 사용하며,
     *    수의학 공식에 없는 별도 체급 계수는 적용하지 않는다.
     */
    @Transactional
    public Long addWalk(
            Long userId,
            Long petId,
            WalkCreateRequest request
    ) {
        Pet pet = getOwnedPet(userId, petId);

        // 반려동물 체중 확인
        if (pet.getWeight() == null || pet.getWeight() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        // 산책 시간 확인
        if (request.durationMinutes() == null
                || request.durationMinutes() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        // 반려동물 체급 확인
        DogSize dogSize = getDogSize(pet.getWeight());

        /*
         * 현재는 체급에 따라 임의의 칼로리 계수를 추가하지 않는다.
         * RER 공식 자체가 체중을 기반으로 계산하기 때문이다.
         *
         * dogSize는 향후 화면 표시,
         * 체급별 추천 등의 기능에 활용할 수 있다.
         */
        double calories =
                calculateWalkCalories(
                        pet,
                        request.durationMinutes()
                );

        Walk walk = new Walk();

        // 사용자
        walk.setUserId(userId);

        // 반려동물
        walk.setPetId(pet.getId());

        // 산책 시작 시간
        walk.setStartedAt(request.startedAt());

        // 산책 종료 시간
        walk.setEndedAt(request.endedAt());

        // 거리
        walk.setDistanceKm(request.distanceKm());

        // 산책 시간
        walk.setDurationMinutes(
                request.durationMinutes()
        );

        // 칼로리
        walk.setCalories(
                (int) Math.round(calories)
        );

        // GPS 경로
        List<Map<String, Object>> routeData =
                objectMapper.convertValue(
                        request.routeData(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );

        walk.setRouteData(routeData);

        // 메모
        walk.setMemo(
                request.memo()
        );

        Walk saved =
                walkRepository.save(walk);

        return saved.getId();
    }


    /**
     * 이번 달 산책 통계 조회
     */
    public MonthlyWalkSummaryResponse getMonthlySummary(
            Long userId,
            Long petId
    ) {
        Pet pet = getOwnedPet(userId, petId);

        LocalDate now = LocalDate.now();

        // 이번 달 시작
        LocalDateTime startDate =
                now.withDayOfMonth(1)
                        .atStartOfDay();

        // 다음 달 시작
        LocalDateTime endDate =
                startDate.plusMonths(1);


        // 산책 횟수
        long walkCount =
                walkRepository.countByUserIdAndPetIdAndStartedAtBetween(
                        userId,
                        pet.getId(),
                        startDate,
                        endDate
                );


        // 총 거리
        Double totalDistance =
                walkRepository.sumDistanceByPetIdAndDateBetween(
                        userId,
                        pet.getId(),
                        startDate,
                        endDate
                );


        // 총 산책 시간
        Long totalDuration =
                walkRepository.sumDurationByPetIdAndDateBetween(
                        userId,
                        pet.getId(),
                        startDate,
                        endDate
                );


        // 총 칼로리
        Double totalCalorie =
                walkRepository.sumCalorieByPetIdAndDateBetween(
                        userId,
                        pet.getId(),
                        startDate,
                        endDate
                );


        return new MonthlyWalkSummaryResponse(
                now.getYear(),
                now.getMonthValue(),
                walkCount,
                totalDistance,
                totalDuration,
                totalCalorie
        );
    }


    /**
     * 로그인한 사용자의 반려동물인지 확인
     */
    private Pet getOwnedPet(
            Long userId,
            Long petId
    ) {
        return petRepository.findByIdAndUserId(
                petId,
                userId
        ).orElseThrow(
                () -> new BusinessException(
                        ErrorCode.PET_NOT_FOUND
                )
        );
    }


    /**
     * 반려동물 체급 분류
     *
     * 프로젝트 기준
     * - 10kg 미만       : 소형
     * - 10kg ~ 25kg 미만: 중형
     * - 25kg 이상       : 대형
     */
    private DogSize getDogSize(Double weight) {

        if (weight < 10) {
            return DogSize.SMALL;
        }

        if (weight < 25) {
            return DogSize.MEDIUM;
        }

        return DogSize.LARGE;
    }


    /**
     * RER 계산
     *
     * RER = 70 × 체중(kg)^0.75
     */
    private double calculateRER(Double weight) {

        return 70.0 * Math.pow(weight, 0.75);
    }


    /**
     * MER 계산
     *
     * 건강한 성견 기준
     *
     * 중성화     : RER × 1.6
     * 미중성화   : RER × 1.8
     */
    private double calculateMER(Pet pet) {

        double rer =
                calculateRER(
                        pet.getWeight()
                );

        if (Boolean.TRUE.equals(
                pet.getNeutered()
        )) {
            return rer * 1.6;
        }

        return rer * 1.8;
    }


    /**
     * 산책 칼로리 추정
     *
     * 하루 1440분을 기준으로
     * 산책 시간에 해당하는 MER 비율을 계산한다.
     *
     * ※ 이 값은 프로젝트용 추정값이다.
     *    RER/MER 공식 자체는 일일 에너지 요구량 계산용이다.
     */
    private double calculateWalkCalories(
            Pet pet,
            Integer durationMinutes
    ) {
        double mer =
                calculateMER(pet);

        double dayMinutes =
                24.0 * 60.0;

        double timeRatio =
                durationMinutes / dayMinutes;

        return mer * timeRatio;
    }


    /**
     * 체급 Enum
     */
    private enum DogSize {
        SMALL,
        MEDIUM,
        LARGE
    }


    /**
     * Entity → Response DTO
     */
    private WalkResponse toResponse(
            Walk walk
    ) {
        String routeDataJson = null;

        if (walk.getRouteData() != null) {
            try {
                routeDataJson =
                        objectMapper.writeValueAsString(
                                walk.getRouteData()
                        );
            } catch (JsonProcessingException e) {
                throw new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR
                );
            }
        }

        return new WalkResponse(
                walk.getId(),
                walk.getStartedAt(),
                walk.getEndedAt(),
                walk.getDistanceKm(),
                walk.getDurationMinutes(),
                walk.getCalories(),
                routeDataJson,
                walk.getMemo()
        );
    }
}