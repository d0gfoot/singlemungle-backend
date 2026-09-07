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

    private static final double CALORIE_FACTOR = 1.1;

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
     * 칼로리
     * = 체중(kg) × 거리(km) × 1.1
     */
    @Transactional
    public Long addWalk(
            Long userId,
            Long petId,
            WalkCreateRequest request
    ) {
        Pet pet = getOwnedPet(userId, petId);

        // 반려동물 체중 확인
        if (pet.getWeight() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        // 칼로리 계산
        double calories =
                pet.getWeight()
                        * request.distanceKm()
                        * CALORIE_FACTOR;

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
                (int) calories
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
        // 현재 달의 마지막 날짜를 직접 계산하는 것보다
        // 다음 달 1일 미만으로 조회하는 것이 안전함
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