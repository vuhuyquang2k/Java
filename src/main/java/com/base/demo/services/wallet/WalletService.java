package com.base.demo.services.wallet;

import com.base.demo.dtos.wallet.CreateWalletRequest;
import com.base.demo.dtos.wallet.GetWalletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface WalletService {
    void createWallet(CreateWalletRequest request);
    GetWalletResponse getWallet(OAuth2User principal);
}
