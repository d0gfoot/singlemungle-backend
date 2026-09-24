package com.singlemungle.backend.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.common.response.PageResponse;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;
import com.singlemungle.backend.domain.place.service.BeautyShopPlaceService;
import com.singlemungle.backend.domain.review.dto.request.ReviewUpdateRequest;
import com.singlemungle.backend.domain.review.dto.response.ReviewImageResponse;
import com.singlemungle.backend.domain.review.dto.response.ReviewResponse;
import com.singlemungle.backend.domain.review.entity.Review;
import com.singlemungle.backend.domain.review.entity.ReviewImage;
import com.singlemungle.backend.domain.review.repository.ReviewImageRepository;
import com.singlemungle.backend.domain.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private static final int MAX_IMAGES = 5;
	private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;
	private static final List<String> SUPPORTED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp");

	private final ReviewRepository reviewRepository;
	private final ReviewImageRepository reviewImageRepository;
	private final BeautyShopPlaceService beautyShopPlaceService;
	private final ImageStorage imageStorage;

	@Transactional
	public ReviewResponse create(
		Long userId,
		String googlePlaceId,
		int rating,
		String content,
		String groomingStyleName,
		boolean aiReferenceAllowed,
		List<MultipartFile> images
	) {
		String normalizedPlaceId = beautyShopPlaceService.getVerifiedBeautyShop(googlePlaceId).googlePlaceId();
		validateReview(rating, content, groomingStyleName);
		List<MultipartFile> safeImages = images == null ? List.of() : images;
		validateImages(safeImages);

		Review review = reviewRepository.save(new Review(
			userId,
			normalizedPlaceId,
			rating,
			content.trim(),
			trimToNull(groomingStyleName)
		));
		List<ReviewImage> savedImages = new ArrayList<>();
		List<String> storedImageUrls = new ArrayList<>();
		try {
			for (MultipartFile image : safeImages) {
				String imageUrl = imageStorage.store(image.getBytes(), image.getContentType());
				storedImageUrls.add(imageUrl);
				savedImages.add(reviewImageRepository.save(
					new ReviewImage(review.getId(), imageUrl, aiReferenceAllowed)
				));
			}
		} catch (Exception exception) {
			storedImageUrls.forEach(this::deleteQuietly);
			throw exception instanceof BusinessException businessException
				? businessException
				: new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		return toResponse(review, savedImages);
	}

	@Transactional(readOnly = true)
	public PageResponse<ReviewResponse> getReviews(String googlePlaceId, Pageable pageable) {
		String normalizedPlaceId = beautyShopPlaceService.normalize(googlePlaceId);
		Page<ReviewResponse> page = reviewRepository.findByGooglePlaceIdOrderByCreatedAtDesc(normalizedPlaceId, pageable)
			.map(this::toResponse);
		return PageResponse.from(page);
	}

	@Transactional(readOnly = true)
	public ReviewResponse getReview(String googlePlaceId, Long reviewId) {
		return toResponse(findReview(beautyShopPlaceService.normalize(googlePlaceId), reviewId));
	}

	@Transactional
	public ReviewResponse update(Long userId, String googlePlaceId, Long reviewId, ReviewUpdateRequest request) {
		validateReview(request.rating(), request.content(), request.groomingStyleName());
		Review review = findOwnedReview(userId, googlePlaceId, reviewId);
		review.update(request.rating(), request.content().trim(), trimToNull(request.groomingStyleName()));
		List<ReviewImage> images = reviewImageRepository.findByReviewIdOrderByIdAsc(reviewId);
		images.forEach(image -> image.updateAiReferenceAllowed(request.aiReferenceAllowed()));
		return toResponse(review, images);
	}

	@Transactional
	public void delete(Long userId, String googlePlaceId, Long reviewId) {
		Review review = findOwnedReview(userId, googlePlaceId, reviewId);
		List<ReviewImage> images = reviewImageRepository.findByReviewIdOrderByIdAsc(reviewId);
		images.forEach(image -> {
			imageStorage.delete(image.getImageUrl());
			image.delete();
		});
		review.delete();
	}

	private ReviewResponse toResponse(Review review) {
		return toResponse(review, reviewImageRepository.findByReviewIdOrderByIdAsc(review.getId()));
	}

	private ReviewResponse toResponse(Review review, List<ReviewImage> images) {
		return ReviewResponse.from(review, images.stream().map(ReviewImageResponse::from).toList());
	}

	private Review findReview(String googlePlaceId, Long reviewId) {
		return reviewRepository.findByIdAndGooglePlaceId(reviewId, googlePlaceId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
	}

	private Review findOwnedReview(Long userId, String googlePlaceId, Long reviewId) {
		return reviewRepository.findByIdAndGooglePlaceIdAndUserId(reviewId, googlePlaceId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
	}

	private void validateReview(int rating, String content, String groomingStyleName) {
		if (rating < 1 || rating > 5 || content == null || content.isBlank() || content.length() > 2000) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		if (groomingStyleName != null && groomingStyleName.length() > 100) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private void validateImages(List<MultipartFile> images) {
		if (images.size() > MAX_IMAGES) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		for (MultipartFile image : images) {
			if (image == null || image.isEmpty() || image.getSize() > MAX_IMAGE_SIZE_BYTES
				|| !SUPPORTED_IMAGE_TYPES.contains(image.getContentType())) {
				throw new BusinessException(ErrorCode.INVALID_REQUEST);
			}
		}
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private void deleteQuietly(String imageUrl) {
		try {
			imageStorage.delete(imageUrl);
		} catch (RuntimeException ignored) {
			// Preserve the original failure.
		}
	}
}
