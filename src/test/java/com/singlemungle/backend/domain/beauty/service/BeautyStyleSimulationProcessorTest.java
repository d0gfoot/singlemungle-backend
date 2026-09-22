package com.singlemungle.backend.domain.beauty.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationClient;
import com.singlemungle.backend.domain.beauty.ai.ImageGenerationRequest;
import com.singlemungle.backend.domain.beauty.ai.GeneratedImage;
import com.singlemungle.backend.domain.beauty.storage.ImageStorage;
import com.singlemungle.backend.domain.beauty.storage.TemporaryImage;
import com.singlemungle.backend.domain.beauty.storage.TemporaryImageStorage;

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
	private TemporaryImageStorage temporaryImageStorage;

	@Mock
	private BeautyStyleSimulationStatusService statusService;

	@InjectMocks
	private BeautyStyleSimulationProcessor processor;

	@Test
	void processDeletesStoredInputsAndPersistsFailedStatusWhenGenerationFails() {
		TemporaryImage petImage = new TemporaryImage("pet.tmp", "image/png");
		TemporaryImage styleImage = new TemporaryImage("style.tmp", "image/png");
		BeautyStyleSimulationRequestedEvent event = new BeautyStyleSimulationRequestedEvent(
			1L,
			"prompt",
			petImage,
			styleImage
		);
		when(temporaryImageStorage.read(petImage)).thenReturn(new byte[] {1});
		when(temporaryImageStorage.read(styleImage)).thenReturn(new byte[] {2});
		doThrow(new BusinessException(ErrorCode.IMAGE_GENERATION_FAILED))
			.when(imageGenerationClient)
			.generate(any(ImageGenerationRequest.class));

		processor.process(event);

		verify(temporaryImageStorage).delete(petImage);
		verify(temporaryImageStorage).delete(styleImage);
		verify(statusService).fail(1L, ErrorCode.IMAGE_GENERATION_FAILED.getCode());
	}

	@Test
	void processDeletesEveryStoredImageAndPersistsFailedStatusWhenCompletionUpdateFails() {
		TemporaryImage petImage = new TemporaryImage("pet.tmp", "image/png");
		BeautyStyleSimulationRequestedEvent event = new BeautyStyleSimulationRequestedEvent(
			1L,
			"prompt",
			petImage,
			null
		);
		when(temporaryImageStorage.read(petImage)).thenReturn(new byte[] {1});
		when(imageGenerationClient.generate(any(ImageGenerationRequest.class)))
			.thenReturn(new GeneratedImage(new byte[] {3}, "image/png"));
		when(imageStorage.store(new byte[] {3}, "image/png"))
			.thenReturn("/images/generated.png");
		doThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
			.when(statusService)
			.complete(1L, "/images/generated.png");

		processor.process(event);

		verify(imageStorage).delete("/images/generated.png");
		verify(temporaryImageStorage).delete(petImage);
		verify(statusService).fail(1L, ErrorCode.INTERNAL_SERVER_ERROR.getCode());
	}
}
