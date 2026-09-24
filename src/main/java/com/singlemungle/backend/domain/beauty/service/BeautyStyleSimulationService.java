package com.singlemungle.backend.domain.beauty.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.beauty.ai.BeautyStylePromptBuilder;
import com.singlemungle.backend.domain.beauty.ai.GroomingStyleContext;
import com.singlemungle.backend.domain.beauty.ai.PetGroomingContext;
import com.singlemungle.backend.domain.beauty.dto.response.BeautyStyleSimulationResponse;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyleSimulation;
import com.singlemungle.backend.domain.beauty.entity.GroomingStylePreset;
import com.singlemungle.backend.domain.beauty.entity.StyleSimulationSourceType;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleSimulationRepository;
import com.singlemungle.backend.domain.beauty.repository.GroomingStylePresetRepository;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;
import com.singlemungle.backend.domain.beauty.storage.StoredImage;
import com.singlemungle.backend.domain.beauty.storage.TemporaryImage;
import com.singlemungle.backend.domain.beauty.storage.TemporaryImageStorage;
import com.singlemungle.backend.domain.pet.entity.Pet;
import com.singlemungle.backend.domain.pet.repository.PetRepository;
import com.singlemungle.backend.domain.place.service.BeautyShopPlaceService;
import com.singlemungle.backend.domain.review.entity.Review;
import com.singlemungle.backend.domain.review.entity.ReviewImage;
import com.singlemungle.backend.domain.review.repository.ReviewImageRepository;
import com.singlemungle.backend.domain.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
	private final BeautyShopPlaceService beautyShopPlaceService;
	private final GroomingStylePresetRepository presetRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewImageRepository reviewImageRepository;
	private final BeautyStylePromptBuilder promptBuilder;
	private final ImageStorage imageStorage;
	private final TemporaryImageStorage temporaryImageStorage;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public BeautyStyleSimulationResponse createSimulation(
		Long userId,
		Long petId,
		String googlePlaceId,
		Long stylePresetId,
		Long referenceReviewImageId,
		String additionalRequest,
		MultipartFile petImage,
		MultipartFile styleImage
	) {
		validateAdditionalRequest(additionalRequest);
		validateImage(petImage, true);
		validateImage(styleImage, false);
		Pet pet = findOwnedPet(userId, petId);
		String trimmedAdditionalRequest = trimToNull(additionalRequest);
		StyleInput styleInput = resolveStyleInput(
			googlePlaceId,
			stylePresetId,
			referenceReviewImageId,
			trimmedAdditionalRequest,
			styleImage
		);
		ImageData petImageData = toImageData(petImage);
		TemporaryImage temporaryPetImage = null;
		TemporaryImage temporaryStyleImage = null;
		try {
			temporaryPetImage = temporaryImageStorage.store(petImageData.bytes(), petImageData.mimeType());
			temporaryStyleImage = styleInput.referenceImage() == null
				? null
				: temporaryImageStorage.store(
					styleInput.referenceImage().bytes(),
					styleInput.referenceImage().mimeType()
				);
			registerRollbackCleanup(temporaryPetImage, temporaryStyleImage);

			BeautyStyleSimulation simulation = simulationRepository.save(new BeautyStyleSimulation(
				userId,
				petId,
				styleInput.googlePlaceId(),
				styleInput.styleName(),
				styleInput.stylePresetId(),
				styleInput.referenceReviewImageId(),
				styleInput.sourceType(),
				trimmedAdditionalRequest,
				null,
				null,
				STORAGE_PROVIDER
			));

			PetGroomingContext petContext = promptBuilder.petContext(pet.getBreed(), pet.getWeight(), pet.getMemo());
			String prompt = promptBuilder.build(
				styleInput.promptContext(),
				trimmedAdditionalRequest,
				temporaryStyleImage != null,
				petContext
			);
			eventPublisher.publishEvent(new BeautyStyleSimulationRequestedEvent(
				simulation.getId(),
				prompt,
				temporaryPetImage,
				temporaryStyleImage
			));

			return BeautyStyleSimulationResponse.from(simulation);
		} catch (RuntimeException exception) {
			deleteTemporaryQuietly(temporaryPetImage);
			deleteTemporaryQuietly(temporaryStyleImage);
			throw exception;
		}
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
		imageStorage.delete(simulation.getGeneratedImageUrl());
		simulation.delete();
	}

	@Transactional(readOnly = true)
	public Resource loadImage(String fileName) {
		return imageStorage.load(fileName);
	}

	private Pet findOwnedPet(Long userId, Long petId) {
		return petRepository.findByIdAndUserId(petId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PET_NOT_FOUND));
	}

	private void validateBeautyShop(String googlePlaceId) {
		if (googlePlaceId == null || googlePlaceId.isBlank() || googlePlaceId.length() > 255) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		beautyShopPlaceService.getVerifiedBeautyShop(googlePlaceId);
	}

	private BeautyStyleSimulation findOwnedSimulation(Long userId, Long simulationId) {
		return simulationRepository.findByIdAndUserId(simulationId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.STYLE_SIMULATION_NOT_FOUND));
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

	private void registerRollbackCleanup(TemporaryImage petImage, TemporaryImage styleImage) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_COMMITTED) {
					deleteTemporaryQuietly(petImage);
					deleteTemporaryQuietly(styleImage);
				}
			}
		});
	}

	private void deleteTemporaryQuietly(TemporaryImage image) {
		try {
			temporaryImageStorage.delete(image);
		} catch (RuntimeException ignored) {
			// Preserve the original request failure; scheduled cleanup handles temporary files.
		}
	}

	private StyleInput resolveStyleInput(
		String requestedGooglePlaceId,
		Long stylePresetId,
		Long referenceReviewImageId,
		String additionalRequest,
		MultipartFile uploadedStyleImage
	) {
		if (referenceReviewImageId != null && uploadedStyleImage != null && !uploadedStyleImage.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}

		GroomingStylePreset preset = stylePresetId == null
			? null
			: presetRepository.findByIdAndEnabledTrue(stylePresetId)
				.orElseThrow(() -> new BusinessException(ErrorCode.STYLE_PRESET_NOT_FOUND));
		ReviewImage reviewImage = referenceReviewImageId == null
			? null
			: reviewImageRepository.findById(referenceReviewImageId)
				.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_IMAGE_NOT_FOUND));

		Review review = null;
		ImageData referenceImage = null;
		String resolvedGooglePlaceId = requestedGooglePlaceId;
		if (reviewImage != null) {
			if (!reviewImage.isAiReferenceAllowed()) {
				throw new BusinessException(ErrorCode.AI_REFERENCE_NOT_ALLOWED);
			}
			review = reviewRepository.findById(reviewImage.getReviewId())
				.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
			if (resolvedGooglePlaceId != null && !resolvedGooglePlaceId.equals(review.getGooglePlaceId())) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST);
			}
			resolvedGooglePlaceId = review.getGooglePlaceId();
			StoredImage storedImage = imageStorage.read(reviewImage.getImageUrl());
			referenceImage = new ImageData(storedImage.bytes(), storedImage.mimeType());
		} else if (uploadedStyleImage != null && !uploadedStyleImage.isEmpty()) {
			referenceImage = toImageData(uploadedStyleImage);
		}

		if (resolvedGooglePlaceId != null) {
			validateBeautyShop(resolvedGooglePlaceId);
		}
		if (preset == null && referenceImage == null && additionalRequest == null) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}

		String styleName = preset != null
			? preset.getName()
			: review == null ? null : review.getGroomingStyleName();
		GroomingStyleContext promptContext = preset == null
			? new GroomingStyleContext(styleName, null, null)
			: new GroomingStyleContext(
				preset.getName(), preset.getPromptDescription(), preset.getNegativePrompt()
			);
		StyleSimulationSourceType sourceType = reviewImage != null
			? StyleSimulationSourceType.REVIEW_IMAGE
			: uploadedStyleImage != null && !uploadedStyleImage.isEmpty()
				? StyleSimulationSourceType.USER_UPLOAD
				: preset != null ? StyleSimulationSourceType.PRESET : StyleSimulationSourceType.CUSTOM_REQUEST;

		return new StyleInput(
			resolvedGooglePlaceId,
			preset == null ? null : preset.getId(),
			reviewImage == null ? null : reviewImage.getId(),
			styleName,
			sourceType,
			promptContext,
			referenceImage
		);
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

	private record StyleInput(
		String googlePlaceId,
		Long stylePresetId,
		Long referenceReviewImageId,
		String styleName,
		StyleSimulationSourceType sourceType,
		GroomingStyleContext promptContext,
		ImageData referenceImage
	) {
	}
}
