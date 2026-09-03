package com.singlemungle.backend.domain.beauty.repository;

import java.util.List;
import com.singlemungle.backend.domain.beauty.entity.BeautyStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeautyStyleRepository extends JpaRepository<BeautyStyle, Long> {

    List<BeautyStyle> findByPlaceIdOrderByIdAsc(Long placeId);
}