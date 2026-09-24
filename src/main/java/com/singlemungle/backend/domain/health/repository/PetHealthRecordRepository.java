package com.singlemungle.backend.domain.health.repository;

import java.util.Optional;

import com.singlemungle.backend.domain.health.entity.PetHealthRecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetHealthRecordRepository extends JpaRepository<PetHealthRecord, Long> {
    Page<PetHealthRecord> findByPetId(Long petId, Pageable pageable);
    Page<PetHealthRecord> findByPetIdAndRecordType(Long petId, String recordType, Pageable pageable);
    Optional<PetHealthRecord> findByIdAndPetId(Long id, Long petId);
}
