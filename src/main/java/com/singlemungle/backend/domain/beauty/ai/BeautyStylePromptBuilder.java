package com.singlemungle.backend.domain.beauty.ai;

import org.springframework.stereotype.Component;

@Component
public class BeautyStylePromptBuilder {

	public String build(String styleKeyword, String additionalRequest, boolean hasReferenceStyleImage) {
		return """
			Create a realistic edited dog grooming preview from the uploaded pet photo.

			Required grooming style: "%s".
			User additional request: %s
			Reference style image guidance: %s

			Important requirements:
			- Do not return an unchanged copy of the source image.
			- Keep the same dog identity, pose, face direction, fur color, and main facial features.
			- Make the grooming result clearly visible and noticeably different from the original.
			- Change the coat silhouette, face outline, ear shape/trim, muzzle area, and leg/body fur length to match the requested grooming style.
			- Apply the user's additional request when it is compatible with a realistic pet grooming result.
			- If a reference style image is provided, use it as the primary grooming style reference. Transfer the visible grooming characteristics from the reference image, including face roundness or sharpness, ear trim shape, muzzle volume, neck/body coat length, leg silhouette, and overall salon finish.
			- If the source dog and reference dog differ, adapt the reference grooming style to the source dog while preserving the source dog's identity.
			- If no reference style image is provided, infer a typical professional pet salon version of the requested style.
			- Preserve a natural photo-realistic look. Avoid cartoon effects, accessories, text, logos, or background distortion.
			- The final image should look like an after-grooming preview of the same dog, not a resized or lightly retouched source photo.
			""".formatted(
				styleKeyword.trim(),
				additionalRequestText(additionalRequest),
				referenceStyleImageText(hasReferenceStyleImage)
			);
	}

	private String additionalRequestText(String additionalRequest) {
		return additionalRequest == null || additionalRequest.isBlank()
			? "No additional request."
			: "\"%s\"".formatted(additionalRequest.trim());
	}

	private String referenceStyleImageText(boolean hasReferenceStyleImage) {
		return hasReferenceStyleImage
			? "A reference grooming style image is provided. Prioritize it over the generic style keyword."
			: "No reference style image is provided.";
	}
}
