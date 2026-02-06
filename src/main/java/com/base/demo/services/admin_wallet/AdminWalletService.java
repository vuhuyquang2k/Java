package com.base.demo.services.admin_wallet;

import com.base.demo.dtos.deposit_request.ReviewDepositResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AdminWalletService {
    ReviewDepositResponse approveDepositRequest(OAuth2User principal, Long id);
}
