package com.singlemungle.backend.domain.beauty.service;

import java.util.List;
import com.singlemungle.backend.domain.beauty.dto.response.BeautyStyleResponse;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyle;
import com.singlemungle.backend.domain.beauty.repository.BeautyStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BeautyStyleService {

    private final BeautyStyleRepository beautyStyleRepository;

    public List<BeautyStyleResponse> getStyles(Long shopId) {

        List<BeautyStyle> styles =
                beautyStyleRepository.findByPlaceIdOrderByIdAsc(shopId);

        return styles.stream()
                .map(style -> new BeautyStyleResponse(
                        style.getId(),
                        style.getName(),
                        style.getDescription(),
                        style.getImageUrl()
                ))
                .toList();
    }
}