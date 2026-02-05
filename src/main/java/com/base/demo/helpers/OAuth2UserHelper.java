package com.base.demo.helpers;

import com.base.demo.constants.enums.identity.UserProvider;
import com.base.demo.entities.UserIdentity;
import com.base.demo.exceptions.UnauthorizedException;
import com.base.demo.repositories.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2UserHelper {

    private final UserIdentityRepository userIdentityRepository;

    public String getProviderUserId(OAuth2User principal) {
        String providerUserId = principal.getAttribute("sub");

        if (providerUserId == null || providerUserId.isBlank()) {
            log.error("Không tìm thấy sub trong OAuth2 token");
            throw new UnauthorizedException("Phiên đăng nhập không hợp lệ");
        }

        return providerUserId;
    }

    public UserIdentity getUserIdentity(UserProvider provider, String providerUserId) {
        return userIdentityRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với provider={}, providerUserId={}",
                            provider, providerUserId);
                    return new UnauthorizedException("Tài khoản chưa được đăng ký");
                });
    }

    public boolean existsUserIdentity(UserProvider provider, String providerUserId) {
        return userIdentityRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .isPresent();
    }
}