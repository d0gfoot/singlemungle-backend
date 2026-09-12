package com.singlemungle.backend.domain.beauty.ai;

import org.springframework.stereotype.Component;

@Component
public class BeautyStylePromptBuilder {

	public String build(String styleKeyword) {
		return """
			Create a realistic edited dog grooming preview from the uploaded pet photo.

			Required grooming style: "%s".

			Important requirements:
			- Do not return an unchanged copy of the source image.
			- Keep the same dog identity, pose, face direction, fur color, and main facial features.
			- Make the grooming result clearly visible and noticeably different from the original.
			- Change the coat silhouette, face outline, ear shape/trim, muzzle area, and leg/body fur length to match the requested grooming style.
			- If no reference style image is provided, infer a typical professional pet salon version of the requested style.
			- Preserve a natural photo-realistic look. Avoid cartoon effects, accessories, text, logos, or background distortion.
			- The final image should look like an after-grooming preview of the same dog, not a resized or lightly retouched source photo.
			""".formatted(styleKeyword.trim());
	}
}
