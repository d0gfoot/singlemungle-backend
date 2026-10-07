package com.singlemungle.backend.domain.user.repository;

import java.util.Optional;

import com.singlemungle.backend.domain.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	@Query(value = """
		select *
		from users
		where oauth_provider = :oauthProvider
			and oauth_provider_id = :oauthProviderId
		limit 1
		""", nativeQuery = true)
	Optional<User> findByOauthIdentityIncludingDeleted(
		@Param("oauthProvider") String oauthProvider,
		@Param("oauthProviderId") String oauthProviderId
	);
}
