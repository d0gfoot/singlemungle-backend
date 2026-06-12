package com.singlemungle.backend.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.schedule.dto.request.ScheduleCreateRequest;
import com.singlemungle.backend.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.singlemungle.backend.domain.schedule.dto.response.ScheduleResponse;
import com.singlemungle.backend.domain.schedule.entity.Schedule;
import com.singlemungle.backend.domain.schedule.repository.ScheduleRepository;
import com.singlemungle.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepo;
    private final PetRepository petRepository;

    public List<ScheduleResponse> getSchedules(Long userId) {
        return scheduleRepo.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Long addSchedule(Long userId, ScheduleCreateRequest req) {
        validateOwnedPetIfPresent(userId, req.petId());
        Schedule s = new Schedule();
        s.setUserId(userId);
        s.setPetId(req.petId());
        s.setScheduleType(req.scheduleType());
        s.setTitle(req.title());
        s.setStartAt(req.startAt());
        s.setEndAt(req.endAt());
        s.setLocation(req.location());
        s.setMemo(req.memo());
        s.setIsAlarm(req.isAlarm() != null ? req.isAlarm() : false);

        Schedule saved = scheduleRepo.save(s);
        return saved.getId();
    }

    @Transactional
    public Long updateSchedule(Long userId, Long scheduleId, ScheduleUpdateRequest req) {
        Schedule s = findOwnedSchedule(userId, scheduleId);
        validateOwnedPetIfPresent(userId, req.petId());

        if (req.petId() != null) s.setPetId(req.petId());
        if (req.scheduleType() != null) s.setScheduleType(req.scheduleType());
        if (req.title() != null) s.setTitle(req.title());
        if (req.startAt() != null) s.setStartAt(req.startAt());
        if (req.endAt() != null) s.setEndAt(req.endAt());
        if (req.location() != null) s.setLocation(req.location());
        if (req.memo() != null) s.setMemo(req.memo());
        if (req.isAlarm() != null) s.setIsAlarm(req.isAlarm());
        s.setUpdatedAt(LocalDateTime.now());

        return s.getId();
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule schedule = findOwnedSchedule(userId, scheduleId);
        scheduleRepo.delete(schedule);
    }

    private Schedule findOwnedSchedule(Long userId, Long scheduleId) {
        return scheduleRepo.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateOwnedPetIfPresent(Long userId, Long petId) {
        if (petId == null) {
            return;
        }
        petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getPetId(),
                schedule.getScheduleType(),
                schedule.getTitle(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.getLocation(),
                schedule.getMemo(),
                schedule.getIsAlarm()
        );
    }
}
