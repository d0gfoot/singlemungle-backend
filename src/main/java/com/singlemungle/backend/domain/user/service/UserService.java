package com.singlemungle.backend.domain.user.service;

import java.time.LocalDateTime;

import com.singlemungle.backend.common.exception.BusinessException;
import com.singlemungle.backend.common.exception.ErrorCode;
import com.singlemungle.backend.domain.auth.repository.RefreshTokenRepository;
import com.singlemungle.backend.domain.user.dto.response.UserInfoResponse;
import com.singlemungle.backend.domain.user.entity.User;
import com.singlemungle.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User u = findActiveUser(userId);
        String provider = (u.getOauthProvider() != null) ? u.getOauthProvider().name() : "NONE";
        return new UserInfoResponse(u.getId(), u.getNickname(), u.getPhone(), u.getProfileImageUrl(), provider);
    }

    @Transactional
    public void updateUserInfo(Long userId, String nickname, String phone, String profileImageUrl) {
        User u = findActiveUser(userId);
        if (nickname != null) u.setNickname(nickname);
        if (phone != null) u.setPhone(phone);
        if (profileImageUrl != null) u.setProfileImageUrl(profileImageUrl);
        u.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User u = findActiveUser(userId);
        refreshTokenRepository.deleteByUserId(userId);
        u.delete();
    }

    private User findActiveUser(Long userId) {
        return userRepo.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
