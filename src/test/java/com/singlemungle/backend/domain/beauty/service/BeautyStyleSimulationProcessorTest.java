package com.singlemungle.backend.domain.beauty.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationClient;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationRequest;
import com.singlemungle.backend.domain.beauty.ai.GeneratedImage;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeautyStyleSimulationProcessorTest {

	@Mock
	private ImageGenerationClient imageGenerationClient;

	@Mock
	private ImageStorage imageStorage;

	@Mock
	private BeautyStyleSimulationStatusService statusService;

	@InjectMocks
	private BeautyStyleSimulationProcessor processor;

	@Test
	void processDeletesStoredInputsAndPersistsFailedStatusWhenGenerationFails() {
		ImageGenerationRequest request = new ImageGenerationRequest(
			"prompt",
			new byte[] {1},
			"image/png",
			new byte[] {2},
			"image/png"
		);
		BeautyStyleSimulationRequestedEvent event = new BeautyStyleSimulationRequestedEvent(
			1L,
			"/images/source.png",
			"/images/style.png",
			request
		);
		doThrow(new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED))
			.when(imageGenerationClient)
			.generate(request);

		processor.process(event);

		verify(imageStorage).delete("/images/source.png");
		verify(imageStorage).delete("/images/style.png");
		verify(statusService).fail(1L, ErrorCode.IMAGE_GENERATION_FAILED.getCode());
	}

	@Test
	void processDeletesEveryStoredImageAndPersistsFailedStatusWhenCompletionUpdateFails() {
		ImageGenerationRequest request = new ImageGenerationRequest(
			"prompt",
			new byte[] {1},
			"image/png",
			null,
			null
		);
		BeautyStyleSimulationRequestedEvent event = new BeautyStyleSimulationRequestedEvent(
			1L,
			"/images/source.png",
			null,
			request
		);
		when(imageGenerationClient.generate(request))
			.thenReturn(new GeneratedImage(new byte[] {3}, "image/png"));
		when(imageStorage.store(new byte[] {3}, "image/png"))
			.thenReturn("/images/generated.png");
		doThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
			.when(statusService)
			.complete(1L, "/images/generated.png");

		processor.process(event);

		verify(imageStorage).delete("/images/generated.png");
		verify(imageStorage).delete("/images/source.png");
		verify(statusService).fail(1L, ErrorCode.INTERNAL_SERVER_ERROR.getCode());
	}
}
