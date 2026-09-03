package com.singlemungle.backend.domain.walk.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.singlemungle.backend.domain.walk.entity.Walk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalkRepository
        extends JpaRepository<Walk, Long> {

    /**
     * 최근 산책 기록
     */
    Page<Walk> findByUserIdAndPetIdOrderByStartedAtDesc(
            Long userId,
            Long petId,
            Pageable pageable
    );


    /**
     * 산책 기록 단건 조회
     */
    Optional<Walk> findByIdAndUserIdAndPetId(
            Long id,
            Long userId,
            Long petId
    );


    /**
     * 월간 총 거리
     */
    @Query("""
        SELECT COALESCE(SUM(w.distanceKm), 0)
        FROM Walk w
        WHERE w.userId = :userId
          AND w.petId = :petId
          AND w.startedAt >= :startDate
          AND w.startedAt < :endDate
    """)
    Double sumDistanceByPetIdAndDateBetween(
            Long userId,
            Long petId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    /**
     * 월간 총 산책 시간
     */
    @Query("""
        SELECT COALESCE(SUM(w.durationMinutes), 0)
        FROM Walk w
        WHERE w.userId = :userId
          AND w.petId = :petId
          AND w.startedAt >= :startDate
          AND w.startedAt < :endDate
    """)
    Long sumDurationByPetIdAndDateBetween(
            Long userId,
            Long petId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    /**
     * 월간 총 칼로리
     */
    @Query("""
        SELECT COALESCE(SUM(w.calories), 0)
        FROM Walk w
        WHERE w.userId = :userId
          AND w.petId = :petId
          AND w.startedAt >= :startDate
          AND w.startedAt < :endDate
    """)
    Double sumCalorieByPetIdAndDateBetween(
            Long userId,
            Long petId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    /**
     * 월간 산책 횟수
     */
    long countByUserIdAndPetIdAndStartedAtBetween(
            Long userId,
            Long petId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}