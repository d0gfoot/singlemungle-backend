package com.singlemungle.backend.domain.pet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.pet.dto.request.PetCreateRequest;
import com.singlemungle.backend.domain.pet.dto.request.PetUpdateRequest;
import com.singlemungle.backend.domain.pet.dto.response.PetResponse;
import com.singlemungle.backend.domain.pet.entity.Pet;
import com.singlemungle.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepo;

    public List<PetResponse> getMyPets(Long userId) {
        return petRepo.findByUserId(userId).stream()
                .map(p -> new PetResponse(
                        p.getId(), p.getName(), p.getBreed(),
                        calculateAge(p.getBirthDate()), p.getIsRepresentative()
                ))
                .toList();
    }

    @Transactional
    public Long addPet(Long userId, PetCreateRequest req) {
        Pet p = new Pet();
        p.setUserId(userId);
        p.setName(req.name());
        p.setGender(req.gender());
        p.setBreed(req.breed());
        p.setBirthDate(req.birthDate());
        p.setWeight(req.weight());
        p.setMemo(req.memo());

        p.setIsRepresentative(petRepo.findByUserId(userId).isEmpty());

        return petRepo.save(p).getId();
    }

    @Transactional
    public void updatePet(Long userId, Long petId, PetUpdateRequest req) {
        Pet p = findOwnedPet(userId, petId);
        if(req.name() != null) p.setName(req.name());
        if(req.gender() != null) p.setGender(req.gender());
        if(req.breed() != null) p.setBreed(req.breed());
        if(req.birthDate() != null) p.setBirthDate(req.birthDate());
        if(req.weight() != null) p.setWeight(req.weight());
        if(req.memo() != null) p.setMemo(req.memo());
        p.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deletePet(Long userId, Long petId) {
        Pet pet = findOwnedPet(userId, petId);
        pet.setDeletedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void setRepresentative(Long userId, Long petId) {
        findOwnedPet(userId, petId);
        petRepo.findByUserId(userId).forEach(p -> {
            p.setIsRepresentative(p.getId().equals(petId));
        });
    }

    private Pet findOwnedPet(Long userId, Long petId) {
        return petRepo.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
    }

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}
