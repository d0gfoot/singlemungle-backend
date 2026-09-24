package com.singlemungle.backend.domain.health.service;

import java.time.LocalDateTime;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.health.dto.request.HealthRecordCreateRequest;
import com.singlemungle.backend.domain.health.dto.request.HealthRecordUpdateRequest;
import com.singlemungle.backend.domain.health.dto.response.HealthRecordDetailResponse;
import com.singlemungle.backend.domain.health.dto.response.HealthRecordListResponse;
import com.singlemungle.backend.domain.health.entity.PetHealthRecord;
import com.singlemungle.backend.domain.health.repository.PetHealthRecordRepository;
import com.singlemungle.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthRecordService {

    private final PetHealthRecordRepository healthRepo;
    private final PetRepository petRepository;

    public Page<HealthRecordListResponse> getHealthRecords(Long userId, Long petId, String recordType, Pageable pageable) {
        validateOwnedPet(userId, petId);
        Page<PetHealthRecord> records;
        if (recordType != null && !recordType.isEmpty()) {
            records = healthRepo.findByPetIdAndRecordType(petId, recordType, pageable);
        } else {
            records = healthRepo.findByPetId(petId, pageable);
        }
        return records.map(r -> new HealthRecordListResponse(
                r.getId(), r.getRecordType(), r.getTitle(), r.getRecordDate()));
    }

    public HealthRecordDetailResponse getHealthRecordDetail(Long userId, Long petId, Long recordId) {
        validateOwnedPet(userId, petId);
        PetHealthRecord r = findPetHealthRecord(petId, recordId);

        return new HealthRecordDetailResponse(
                r.getId(), r.getRecordType(), r.getTitle(),
                r.getHospitalName(), r.getRecordDate(), r.getDescription()
        );
    }

    @Transactional
    public Long addHealthRecord(Long userId, Long petId, HealthRecordCreateRequest req) {
        validateOwnedPet(userId, petId);
        PetHealthRecord record = new PetHealthRecord();
        record.setPetId(petId);
        record.setRecordType(req.recordType());
        record.setTitle(req.title());
        record.setHospitalName(req.hospitalName());
        record.setRecordDate(req.recordDate());
        record.setDescription(req.description());
        record.setNextScheduleDate(req.nextScheduleDate());

        PetHealthRecord saved = healthRepo.save(record);
        return saved.getId();
    }

    @Transactional
    public void updateHealthRecord(Long userId, Long petId, Long recordId, HealthRecordUpdateRequest req) {
        validateOwnedPet(userId, petId);
        PetHealthRecord record = findPetHealthRecord(petId, recordId);

        if (req.recordType() != null) record.setRecordType(req.recordType());
        if (req.title() != null) record.setTitle(req.title());
        if (req.hospitalName() != null) record.setHospitalName(req.hospitalName());
        if (req.recordDate() != null) record.setRecordDate(req.recordDate());
        if (req.description() != null) record.setDescription(req.description());
        if (req.nextScheduleDate() != null) record.setNextScheduleDate(req.nextScheduleDate());

        record.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deleteHealthRecord(Long userId, Long petId, Long recordId) {
        validateOwnedPet(userId, petId);
        PetHealthRecord record = findPetHealthRecord(petId, recordId);
        healthRepo.delete(record);
    }

    private void validateOwnedPet(Long userId, Long petId) {
        petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
    }

    private PetHealthRecord findPetHealthRecord(Long petId, Long recordId) {
        return healthRepo.findByIdAndPetId(recordId, petId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
    }
}
