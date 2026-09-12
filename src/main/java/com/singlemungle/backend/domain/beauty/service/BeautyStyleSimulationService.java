package com.singlemungle.backend.domain.beauty.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.beauty.ai.BeautyStylePromptBuilder;
import com.singlemungle.backend.domain.beauty.ai.GeneratedImage;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationClient;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationRequest;
import com.singlemungle.backend.domain.beauty.dto.response.BeautyStyleSimulationResponse;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;
import com.singlemungle.backend.domain.pet.repository.PetRepository;
import com.singlemungle.backend.domain.place.entity.PlaceType;
import com.singlemungle.backend.domain.place.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BeautyStyleSimulationService {

	private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;
	private static final List<String> SUPPORTED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");
	private static final String STORAGE_PROVIDER = "LOCAL";
	private static final int MAX_ADDITIONAL_REQUEST_LENGTH = 500;

	private final BeautyStyleSimulationRepository simulationRepository;
	private final PetRepository petRepository;
	private final PlaceRepository placeRepository;
	private final ImageGenerationClient imageGenerationClient;
	private final BeautyStylePromptBuilder promptBuilder;
	private final ImageStorage imageStorage;

	@Transactional
	public BeautyStyleSimulationResponse createSimulation(
		Long userId,
		Long petId,
		Long shopId,
		String styleKeyword,
		String additionalRequest,
		MultipartFile petImage,
		MultipartFile styleImage
	) {
		validateStyleKeyword(styleKeyword);
		validateAdditionalRequest(additionalRequest);
		validateImage(petImage, true);
		validateImage(styleImage, false);
		validateOwnedPet(userId, petId);
		validateBeautyShop(shopId);

		String trimmedStyleKeyword = styleKeyword.trim();
		String trimmedAdditionalRequest = trimToNull(additionalRequest);
		ImageData petImageData = toImageData(petImage);
		ImageData styleImageData = toImageData(styleImage);
		String sourceImageUrl = imageStorage.store(petImageData.bytes(), petImageData.mimeType());
		String styleImageUrl = styleImageData == null
			? null
			: imageStorage.store(styleImageData.bytes(), styleImageData.mimeType());

		BeautyStyleSimulation simulation = simulationRepository.save(new BeautyStyleSimulation(
			userId,
			petId,
			shopId,
			trimmedStyleKeyword,
			trimmedAdditionalRequest,
			sourceImageUrl,
			styleImageUrl,
			STORAGE_PROVIDER
		));

		try {
			GeneratedImage generatedImage = imageGenerationClient.generate(new ImageGenerationRequest(
				promptBuilder.build(trimmedStyleKeyword, trimmedAdditionalRequest, styleImageData != null),
				petImageData.bytes(),
				petImageData.mimeType(),
				styleImageData == null ? null : styleImageData.bytes(),
				styleImageData == null ? null : styleImageData.mimeType()
			));
			String generatedImageUrl = imageStorage.store(generatedImage.bytes(), generatedImage.mimeType());
			simulation.complete(generatedImageUrl);
		} catch (BusinessException exception) {
			simulation.fail(exception.getErrorCode().getCode());
			throw exception;
		}

		return BeautyStyleSimulationResponse.from(simulation);
	}

	@Transactional(readOnly = true)
	public BeautyStyleSimulationResponse getSimulation(Long userId, Long simulationId) {
		BeautyStyleSimulation simulation = findOwnedSimulation(userId, simulationId);
		return BeautyStyleSimulationResponse.from(simulation);
	}

	@Transactional(readOnly = true)
	public PageResponse<BeautyStyleSimulationResponse> getSimulations(Long userId, Pageable pageable) {
		Page<BeautyStyleSimulationResponse> page = simulationRepository
			.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now(), pageable)
			.map(BeautyStyleSimulationResponse::from);
		return PageResponse.from(page);
	}

	@Transactional
	public void deleteSimulation(Long userId, Long simulationId) {
		BeautyStyleSimulation simulation = findOwnedSimulation(userId, simulationId);
		simulation.delete();
	}

	@Transactional(readOnly = true)
	public Resource loadImage(String fileName) {
		return imageStorage.load(fileName);
	}

	private void validateOwnedPet(Long userId, Long petId) {
		petRepository.findByIdAndUserId(petId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
	}

	private void validateBeautyShop(Long shopId) {
		placeRepository.findByIdAndPlaceType(shopId, PlaceType.BEAUTY)
			.orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
	}

	private BeautyStyleSimulation findOwnedSimulation(Long userId, Long simulationId) {
		return simulationRepository.findByIdAndUserId(simulationId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND));
	}

	private void validateStyleKeyword(String styleKeyword) {
		if (styleKeyword == null || styleKeyword.isBlank() || styleKeyword.length() > 100) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private void validateAdditionalRequest(String additionalRequest) {
		if (additionalRequest != null && additionalRequest.length() > MAX_ADDITIONAL_REQUEST_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private void validateImage(MultipartFile image, boolean required) {
		if (image == null || image.isEmpty()) {
			if (required) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST);
			}
			return;
		}
		if (image.getSize() > MAX_IMAGE_SIZE_BYTES || !SUPPORTED_IMAGE_TYPES.contains(image.getContentType())) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private ImageData toImageData(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			return null;
		}
		try {
			return new ImageData(image.getBytes(), image.getContentType());
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private record ImageData(
		byte[] bytes,
		String mimeType
	) {
	}
}
