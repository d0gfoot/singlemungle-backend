package com.singlemungle.backend.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import com.singlemungle.backend.domain.auth.oauth.OAuthProvider;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_users_oauth_provider_provider_id",
			columnNames = {"oauth_provider", "oauth_provider_id"}
		)
	}
)
@SQLRestriction("deleted_at is null")
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider", length = 20)
	private OAuthProvider oauthProvider;

	@Column(name = "oauth_provider_id", length = 100)
	private String oauthProviderId;

	@Column(length = 100)
	private String email;

	@Column(length = 50)
	private String nickname;

	@Column(name = "profile_image_url", columnDefinition = "text")
	private String profileImageUrl;

	@Column(length = 30)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private UserRole role = UserRole.USER;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static User createOAuthUser(
		OAuthProvider oauthProvider,
		String oauthProviderId,
		String email,
		String nickname,
		String profileImageUrl
	) {
		User user = new User();
		user.oauthProvider = oauthProvider;
		user.oauthProviderId = oauthProviderId;
		user.email = email;
		user.nickname = nickname;
		user.profileImageUrl = profileImageUrl;
		user.role = UserRole.USER;
		return user;
	}

	public void updateOAuthProfile(String email, String nickname, String profileImageUrl) {
		if (email != null) {
			this.email = email;
		}
		if (nickname != null) {
			this.nickname = nickname;
		}
		if (profileImageUrl != null) {
			this.profileImageUrl = profileImageUrl;
		}
		this.updatedAt = LocalDateTime.now();
	}

	public void updateProfile(String nickname, String phone) {
		if (nickname != null) {
			this.nickname = nickname;
		}
		if (phone != null) {
			this.phone = phone;
		}
		this.updatedAt = LocalDateTime.now();
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
