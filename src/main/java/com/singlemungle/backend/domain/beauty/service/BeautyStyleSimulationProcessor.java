package com.singlemungle.backend.domain.beauty.service;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.ai.GeneratedImage;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationClient;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationRequest;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;
import com.singlemungle.backend.domain.beauty.storage.TemporaryImage;
import com.singlemungle.backend.domain.beauty.storage.TemporaryImageStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeautyStyleSimulationProcessor {

	private final ImageGenerationClient imageGenerationClient;
	private final ImageStorage imageStorage;
	private final TemporaryImageStorage temporaryImageStorage;
	private final BeautyStyleSimulationStatusService statusService;

	@Async("imageGenerationExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void process(BeautyStyleSimulationRequestedEvent event) {
		String generatedImageUrl = null;
		try {
			TemporaryImage styleImage = event.styleImage();
			GeneratedImage generatedImage = imageGenerationClient.generate(new ImageGenerationRequest(
				event.prompt(),
				temporaryImageStorage.read(event.petImage()),
				event.petImage().mimeType(),
				styleImage == null ? null : temporaryImageStorage.read(styleImage),
				styleImage == null ? null : styleImage.mimeType()
			));
			generatedImageUrl = imageStorage.store(generatedImage.bytes(), generatedImage.mimeType());
			if (!statusService.complete(event.simulationId(), generatedImageUrl)) {
				deleteQuietly(generatedImageUrl, event.simulationId());
			}
		} catch (RuntimeException exception) {
			deleteQuietly(generatedImageUrl, event.simulationId());
			markFailed(event.simulationId(), failureReason(exception));
			log.warn("Beauty style simulation failed simulationId={}", event.simulationId(), exception);
		} finally {
			deleteTemporaryQuietly(event.petImage(), event.simulationId());
			deleteTemporaryQuietly(event.styleImage(), event.simulationId());
		}
	}

	private void markFailed(Long simulationId, String failureReason) {
		try {
			statusService.fail(simulationId, failureReason);
		} catch (RuntimeException exception) {
			log.error("Failed to persist beauty style simulation failure simulationId={}", simulationId, exception);
		}
	}

	private void deleteQuietly(String imageUrl, Long simulationId) {
		try {
			imageStorage.delete(imageUrl);
		} catch (RuntimeException exception) {
			log.error("Failed to delete beauty style simulation image simulationId={}", simulationId, exception);
		}
	}

	private void deleteTemporaryQuietly(TemporaryImage image, Long simulationId) {
		try {
			temporaryImageStorage.delete(image);
		} catch (RuntimeException exception) {
			log.error("Failed to delete temporary simulation image simulationId={}", simulationId, exception);
		}
	}

	private String failureReason(RuntimeException exception) {
		if (exception instanceof BusinessException businessException) {
			return businessException.getErrorCode().getCode();
		}
		return ErrorCode.IMAGE_GENERATION_FAILED.getCode();
	}
}
