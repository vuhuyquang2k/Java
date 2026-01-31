package com.base.demo.services.oauth;

import com.base.demo.constants.enums.identity.UserEmailStatus;
import com.base.demo.constants.enums.identity.UserProvider;
import com.base.demo.constants.enums.user.UserStatus;
import com.base.demo.entities.User;
import com.base.demo.entities.UserIdentity;
import com.base.demo.exceptions.InternalServerException;
import com.base.demo.repositories.UserIdentityRepository;
import com.base.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String providerName = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // Validate dữ liệu từ OAuth provider
        if (providerUserId == null || providerUserId.isBlank()) {
            log.error("OAuth provider không trả về ID người dùng");
            throw new OAuth2AuthenticationException("Không thể xác thực: thiếu thông tin người dùng từ nhà cung cấp");
        }

        // Tìm identity đã tồn tại hoặc tạo mới
        Optional<UserIdentity> existingIdentity = userIdentityRepository
                .findByProviderAndProviderUserId(UserProvider.GOOGLE, providerUserId);

        if (existingIdentity.isPresent()) {
            // Validate user vẫn tồn tại
            UserIdentity identity = existingIdentity.get();
            boolean userExists = userRepository.existsById(identity.getUser().getId());
            if (!userExists) {
                log.error("User ID {} không tồn tại nhưng identity vẫn còn", identity.getUser().getId());
                throw new InternalServerException("Dữ liệu không nhất quán: vui lòng liên hệ hỗ trợ");
            }
        } else {
            // Tạo user mới
            User newUser = new User();
            newUser.setFullName(name != null ? name : "Người dùng");
            newUser.setStatus(UserStatus.ACTIVE);
            newUser = userRepository.save(newUser);

            // Validate user đã được lưu thành công
            if (newUser.getId() == null) {
                log.error("Không thể lưu user mới vào database");
                throw new InternalServerException("Không thể tạo tài khoản: vui lòng thử lại sau");
            }

            // Kiểm tra email đã được Google xác thực chưa
            Boolean emailVerified = oauth2User.getAttribute("email_verified");

            UserIdentity identity = new UserIdentity();
            identity.setUser(newUser);
            identity.setProvider(UserProvider.GOOGLE);
            identity.setProviderUserId(providerUserId);
            identity.setEmail(email);
            identity.setEmailStatus(Boolean.TRUE.equals(emailVerified)
                    ? UserEmailStatus.VERIFIED
                    : UserEmailStatus.UNVERIFIED);

            UserIdentity savedIdentity = userIdentityRepository.save(identity);

            // Validate identity đã được lưu thành công
            if (savedIdentity.getId() == null) {
                log.error("Không thể lưu identity cho user ID {}", newUser.getId());
                throw new InternalServerException("Không thể liên kết tài khoản: vui lòng thử lại sau");
            }

            log.info("Đã tạo tài khoản mới cho user: {} ({})", name, email);
        }

        return oauth2User;
    }
}
