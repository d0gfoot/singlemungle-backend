package com.singlemungle.backend.domain.beauty.repository;

import java.util.List;
import java.util.Optional;

import com.singlemungle.backend.domain.beauty.entity.GroomingStylePreference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroomingStylePreferenceRepository extends JpaRepository<GroomingStylePreference, Long> {

	Optional<GroomingStylePreference> findByUserIdAndBreedCode(Long userId, String breedCode);

	@Query("""
		select p.stylePresetId as stylePresetId, count(p.id) as selectedUserCount
		from GroomingStylePreference p
		where p.breedCode = :breedCode and p.userId <> :excludedUserId
		group by p.stylePresetId
		""")
	List<StylePresetSelectionCount> countSelectionsByBreedExcludingUser(
		@Param("breedCode") String breedCode,
		@Param("excludedUserId") Long excludedUserId
	);
}
