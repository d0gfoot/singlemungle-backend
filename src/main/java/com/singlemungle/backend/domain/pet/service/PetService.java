package com.singlemungle.backend.domain.pet.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;
import com.singlemungle.backend.domain.pet.dto.request.PetCreateRequest;
import com.singlemungle.backend.domain.pet.dto.request.PetUpdateRequest;
import com.singlemungle.backend.domain.pet.dto.response.PetResponse;
import com.singlemungle.backend.domain.pet.entity.Pet;
import com.singlemungle.backend.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {
	private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
	private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final PetRepository petRepo;
    private final ImageStorage imageStorage;

    public List<PetResponse> getMyPets(Long userId) {
        return petRepo.findByUserId(userId).stream()
                .map(p -> new PetResponse(
                        p.getId(),
                        p.getName(),
                        p.getBreed(),
                        p.getGender(),
                        p.getBirthDate(),
                        p.getWeight(),
                        p.getMemo(),
                        p.getNeutered(),
                        calculateAge(p.getBirthDate()),
                        p.getIsRepresentative(),
                        p.getProfileImageUrl()
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
        p.setNeutered(req.neutered());
        p.setProfileImageUrl(req.profileImageUrl());
        p.setIsRepresentative(petRepo.findByUserId(userId).isEmpty());

        return petRepo.save(p).getId();
    }

    @Transactional
    public void updatePet(Long userId, Long petId, PetUpdateRequest req) {
        Pet p = findOwnedPet(userId, petId);

        if (req.name() != null) p.setName(req.name());
        if (req.gender() != null) p.setGender(req.gender());
        if (req.breed() != null) p.setBreed(req.breed());
        if (req.birthDate() != null) p.setBirthDate(req.birthDate());
        if (req.weight() != null) p.setWeight(req.weight());
        if (req.memo() != null) p.setMemo(req.memo());
        if (req.neutered() != null) p.setNeutered(req.neutered());
        if (req.profileImageUrl() != null) p.setProfileImageUrl(req.profileImageUrl());

        p.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deletePet(Long userId, Long petId) {
        Pet pet = findOwnedPet(userId, petId);
        imageStorage.delete(pet.getProfileImageUrl());
        pet.setProfileImageUrl(null);
        pet.setDeletedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
    }

	@Transactional
	public String uploadProfileImage(Long userId, Long petId, MultipartFile image) {
		Pet pet = findOwnedPet(userId, petId);
		validateImage(image);

		String newImageUrl;
		try {
			newImageUrl = imageStorage.store(image.getBytes(), image.getContentType());
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}

		String previousImageUrl = pet.getProfileImageUrl();
		pet.setProfileImageUrl(newImageUrl);
		pet.setUpdatedAt(LocalDateTime.now());
		imageStorage.delete(previousImageUrl);
		return newImageUrl;
	}

	@Transactional
	public void deleteProfileImage(Long userId, Long petId) {
		Pet pet = findOwnedPet(userId, petId);
		imageStorage.delete(pet.getProfileImageUrl());
		pet.setProfileImageUrl(null);
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

	private void validateImage(MultipartFile image) {
		if (image == null || image.isEmpty() || image.getSize() > MAX_IMAGE_SIZE_BYTES
			|| !SUPPORTED_IMAGE_TYPES.contains(image.getContentType())) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}
