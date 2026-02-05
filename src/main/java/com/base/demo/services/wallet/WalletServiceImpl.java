package com.base.demo.services.wallet;

import com.base.demo.constants.enums.identity.UserProvider;
import com.base.demo.dtos.wallet.CreateWalletRequest;
import com.base.demo.dtos.wallet.GetWalletResponse;
import com.base.demo.entities.UserIdentity;
import com.base.demo.entities.Wallet;
import com.base.demo.exceptions.ConflictException;
import com.base.demo.exceptions.InternalServerException;
import com.base.demo.exceptions.ResourceNotFoundException;
import com.base.demo.helpers.OAuth2UserHelper;
import com.base.demo.repositories.UserIdentityRepository;
import com.base.demo.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    private final UserIdentityRepository userIdentityRepository;

    private final OAuth2UserHelper oAuth2UserHelper;

    @Override
    public void createWallet(CreateWalletRequest request) {
        // Validate ví cho user đã tồn tại
        boolean walletExists = walletRepository.existsByUserId(request.getUserId());
        if (walletExists) {
            log.warn("Ví cho user ID {} đã tồn tại", request.getUserId());
            throw new ConflictException("Ví đã tồn tại cho user này");
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(request.getUserId());
        wallet.setBalance(request.getBalance());
        wallet.setPendingBalance(request.getPendingBalance());
        wallet.setVersion(request.getVersion());
        wallet.setStatus(request.getStatus());

        Wallet savedWallet = walletRepository.save(wallet);

        // Validate wallet đã được lưu thành công
        if (savedWallet.getId() == null) {
            log.error("không thể tạo ví cho user ID {}", request.getUserId());
            throw new InternalServerException("Không thể tạo ví: vui lòng thử lại sau");
        }
    }

    @Override
    public GetWalletResponse getWallet(OAuth2User principal) {
        // Lấy Google sub (provider user id)
        String providerUserId = oAuth2UserHelper.getProviderUserId(principal);

        // Tìm user trong database
        UserIdentity identity = oAuth2UserHelper.getUserIdentity(UserProvider.GOOGLE, providerUserId);

        Wallet wallet = walletRepository.findByUserId(identity.getUserId());

        if (wallet == null) {
            log.warn("Không tìm thấy ví cho user ID: {}", identity.getUserId());
            throw new ResourceNotFoundException("Wallet", "userId", identity.getUserId());
        }

        GetWalletResponse walletResponse = new GetWalletResponse();
        walletResponse.setUserId(wallet.getUserId());
        walletResponse.setBalance(wallet.getBalance());
        walletResponse.setAvailableBalance(wallet.getBalance().subtract(wallet.getPendingBalance()));
        walletResponse.setPendingBalance(wallet.getPendingBalance());
        walletResponse.setStatus(wallet.getStatus());

        return walletResponse;
    }

}
