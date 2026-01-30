package com.base.demo.services.oauth;

import com.base.demo.constants.enums.user.UserEmailStatus;
import com.base.demo.constants.enums.user.UserProvider;
import com.base.demo.constants.enums.user.UserStatus;
import com.base.demo.entities.User;
import com.base.demo.entities.UserIdentity;
import com.base.demo.repositories.UserIdentityRepository;
import com.base.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        // Find existing identity or create new
        Optional<UserIdentity> existingIdentity = userIdentityRepository
                .findByProviderAndProviderUserId(UserProvider.GOOGLE, providerUserId);

        if (existingIdentity.isEmpty()) {
            // Create new user and identity
            User newUser = new User();
            newUser.setFullName(name);
            newUser.setStatus(UserStatus.ACTIVE);
            newUser = userRepository.save(newUser);

            // Check if Google verified the email
            Boolean emailVerified = oauth2User.getAttribute("email_verified");

            UserIdentity identity = new UserIdentity();
            identity.setUser(newUser);
            identity.setProvider(UserProvider.GOOGLE);
            identity.setProviderUserId(providerUserId);
            identity.setEmail(email);
            identity.setEmailStatus(Boolean.TRUE.equals(emailVerified)
                    ? UserEmailStatus.VERIFIED
                    : UserEmailStatus.UNVERIFIED);
            userIdentityRepository.save(identity);
        }

        return oauth2User;
    }
}
