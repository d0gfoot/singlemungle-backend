package com.singlemungle.backend.domain.pet.entity;

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

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "pets")
@SQLRestriction("deleted_at is null")
@Getter
@Setter
public class Pet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId; 

    private String name;

    private String breed;

    private String gender; 

    @Column(name = "birth_date")
    private LocalDate birthDate; 

    private Double weight;

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String memo; 

    // 대표 반려견? 설정 있길래 추가해둠 erd에 명시되어 있지 않음
    @Column(name = "is_representative")
    private Boolean isRepresentative;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //중성화
    @Column(name = "neutered")
    private Boolean neutered;
}
