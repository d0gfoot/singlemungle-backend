package com.singlemungle.backend.domain.health.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pet_health_records")
@Getter
@Setter
public class PetHealthRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pet_id")
    private Long petId;

    @Column(name = "record_type")
    private String recordType;

    private String title;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "next_schedule_date")
    private LocalDate nextScheduleDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

}
