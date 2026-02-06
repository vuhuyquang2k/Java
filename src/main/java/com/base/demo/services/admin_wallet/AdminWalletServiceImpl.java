package com.base.demo.services.admin_wallet;

import com.base.demo.components.RedisLockComponent;
import com.base.demo.constants.enums.deposit_request.DepositRequestStatus;
import com.base.demo.constants.enums.identity.UserProvider;
import com.base.demo.constants.enums.wallet.WalletStatus;
import com.base.demo.constants.enums.wallet_transaction.WalletTransactionDirection;
import com.base.demo.constants.enums.wallet_transaction.WalletTransactionType;
import com.base.demo.dtos.deposit_request.ReviewDepositResponse;
import com.base.demo.entities.DepositRequest;
import com.base.demo.entities.Wallet;
import com.base.demo.entities.WalletTransaction;
import com.base.demo.exceptions.BadRequestException;
import com.base.demo.exceptions.ResourceNotFoundException;
import com.base.demo.helpers.OAuth2UserHelper;
import com.base.demo.repositories.DepositRequestRepository;
import com.base.demo.repositories.WalletRepository;
import com.base.demo.repositories.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminWalletServiceImpl implements AdminWalletService {

    private final DepositRequestRepository depositRequestRepository;

    private final WalletRepository walletRepository;

    private final WalletTransactionRepository walletTransactionRepository;

    private final RedisLockComponent redisLockComponent;

    private final OAuth2UserHelper oAuth2UserHelper;

    @Override
    @Transactional
    public ReviewDepositResponse approveDepositRequest(OAuth2User principal, Long id) {
        Long userId = depositRequestRepository.findUserIdById(id);
        if (userId == null) {
            log.warn("Yêu cầu nạp tiền với id {} không tồn tại", id);
            throw new ResourceNotFoundException("Yêu cầu nạp tiền không tồn tại");
        }

        // Khoá giao dịch trùng lặp
        String redisKey = "app:wallet:lock:user:" + userId;
        return redisLockComponent.executeWithLock(redisKey, 10, TimeUnit.SECONDS, () -> {

            DepositRequest depositRequest = depositRequestRepository.findById(id).orElseThrow();
            if (!depositRequest.getStatus().equals(DepositRequestStatus.PENDING)) {
                log.warn("Yêu cầu nạp tiền với id {} không hợp lệ", depositRequest.getId());
                throw new BadRequestException("Yêu cầu nạp tiền không hợp lệ");
            }

            Wallet wallet = walletRepository.findByUserId(depositRequest.getUserId());
            if (!wallet.getStatus().equals(WalletStatus.ACTIVE)) {
                log.warn("Không thể nạp tiền vào ví này");
                throw new BadRequestException("Không thể nạp tiền vào ví này");
            }

            // Admin duyệt yêu cầu nạp tiền
            approveDepositRequest(depositRequest, principal);


            WalletTransaction updatedWalletTransaction = createWalletTransaction(wallet, depositRequest, WalletTransactionType.DEPOSIT, WalletTransactionDirection.CREDIT);
            Wallet updatedWallet = creditWallet(wallet, depositRequest.getAmount());
            updatedWalletTransaction.setBalanceAfter(updatedWallet.getBalance());
            walletTransactionRepository.save(updatedWalletTransaction);

            return ReviewDepositResponse.builder()
                    .id(depositRequest.getId())
                    .amount(depositRequest.getAmount())
                    .status(DepositRequestStatus.APPROVED)
                    .walletId(wallet.getId())
                    .balanceAfter(updatedWallet.getBalance())
                    .processedAt(depositRequest.getProcessedAt())
                    .processedBy(depositRequest.getProcessedBy())
                    .build();
        });
    }

    private Wallet creditWallet(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));

        return walletRepository.save(wallet);
    }

    private void approveDepositRequest(DepositRequest depositRequest, OAuth2User principal) {
        String userProviderId = oAuth2UserHelper.getProviderUserId(principal);
        Long userIdProcess = oAuth2UserHelper.getUserIdentity(UserProvider.GOOGLE, userProviderId).getUserId();

        depositRequest.setStatus(DepositRequestStatus.APPROVED);
        depositRequest.setAdminNote("Duyệt yêu cầu nạp " + depositRequest.getAmount() + " thành công");
        depositRequest.setProcessedAt(LocalDateTime.now());
        depositRequest.setProcessedBy(userIdProcess);

        depositRequestRepository.save(depositRequest);
    }

    private WalletTransaction createWalletTransaction(Wallet wallet, DepositRequest depositRequest, WalletTransactionType transactionType, WalletTransactionDirection direction) {
        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setWalletId(wallet.getId());
        walletTransaction.setTransactionType(transactionType);
        walletTransaction.setDirection(direction);
        walletTransaction.setAmount(depositRequest.getAmount());
        walletTransaction.setBalanceBefore(wallet.getBalance());
        walletTransaction.setPendingBefore(wallet.getPendingBalance());
        walletTransaction.setPendingAfter(wallet.getPendingBalance());
        walletTransaction.setReferenceId(depositRequest.getId());

        return walletTransaction;
    }

}
