package com.singlemungle.backend.domain.user.repository;

import java.util.Optional;

import com.singlemungle.backend.domain.auth.oauth.OAuthProvider;
import com.singlemungle.backend.domain.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	Optional<User> findByOauthProviderAndOauthProviderIdAndDeletedAtIsNull(
		OAuthProvider oauthProvider,
		String oauthProviderId
	);
}
