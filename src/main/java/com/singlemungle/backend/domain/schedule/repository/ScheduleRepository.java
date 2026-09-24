package com.singlemungle.backend.domain.schedule.repository;

import java.util.List;
import java.util.Optional;

import com.singlemungle.backend.domain.schedule.entity.Schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	List<Schedule> findByUserId(Long userId);

	Optional<Schedule> findByIdAndUserId(Long id, Long userId);
}
