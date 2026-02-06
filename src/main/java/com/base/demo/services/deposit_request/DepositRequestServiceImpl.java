package com.base.demo.services.deposit_request;

import com.base.demo.components.RedisLockComponent;
import com.base.demo.constants.enums.deposit_request.DepositRequestStatus;
import com.base.demo.constants.enums.identity.UserProvider;
import com.base.demo.constants.enums.wallet.WalletStatus;
import com.base.demo.dtos.deposit_request.CreateDepositRequest;
import com.base.demo.dtos.deposit_request.CreateDepositResponse;
import com.base.demo.entities.DepositRequest;
import com.base.demo.entities.Wallet;
import com.base.demo.exceptions.BadRequestException;
import com.base.demo.exceptions.ConflictException;
import com.base.demo.exceptions.InternalServerException;
import com.base.demo.exceptions.ResourceNotFoundException;
import com.base.demo.helpers.OAuth2UserHelper;
import com.base.demo.repositories.DepositRequestRepository;
import com.base.demo.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositRequestServiceImpl implements DepositRequestService {

    private final DepositRequestRepository depositRequestRepository;

    private final WalletRepository walletRepository;

    private final OAuth2UserHelper oAuth2UserHelper;

    private final RedisLockComponent redisLockComponent;

    @Override
    @Transactional
    public CreateDepositResponse createDepositRequest(OAuth2User principal, CreateDepositRequest request) {
        BigDecimal amount = request.getAmount();
        BigDecimal min = new BigDecimal("10000.00");
        BigDecimal max = new BigDecimal("100000000.00");

        // Validate số tiền nạp vào ví
        if (amount.compareTo(min) < 0 || amount.compareTo(max) > 0) {
            log.warn("Số tiền nạp vào ví phải từ 10,000 VND -> 100,000,000 VND");
            throw new InternalServerException("Số tiền nạp vào ví phải từ 10,000 VND -> 100,000,000 VND");
        }

        // Validate có ví và trạng thái
        String userProviderId = oAuth2UserHelper.getProviderUserId(principal);
        Long userId = oAuth2UserHelper.getUserIdentity(UserProvider.GOOGLE, userProviderId).getUserId();
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            log.error("Bạn chưa có ví điện tử");
            throw new ResourceNotFoundException("Bạn chưa có ví điện tử");
        }

        if (!wallet.getStatus().equals(WalletStatus.ACTIVE)) {
            log.error("Ví của bạn không thể nạp tiền");
            throw new InternalServerException("Ví của bạn không thể nạp tiền");
        }

        // Khoá giao dịch trùng lặp
        String redisKey = "app:deposit:lock:user:" + userId;
        boolean checkRedisLock = redisLockComponent.tryLock(redisKey, 10, TimeUnit.SECONDS);
        if (!checkRedisLock) {
            log.warn("Hệ thống đang bận, vui lòng thử lại sau");
            throw new InternalServerException("Hệ thống đang bận, vui lòng thử lại sau");
        }

        // Ngăn chặn tạo nhiều giao dịch pending
        boolean checkDepositPending = depositRequestRepository.existsByUserIdAndStatus(userId,
                DepositRequestStatus.PENDING);
        if (checkDepositPending) {
            log.warn("Bạn có yêu cầu nạp tiền chưa xử lý, vui lòng thử lại sau");
            throw new ConflictException("Bạn có yêu cầu nạp tiền chưa xử lý, vui lòng thử lại sau");
        }

        // Tạo yêu cầu nạp tiền
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setUserId(userId);
        depositRequest.setAmount(amount);
        depositRequest.setTransactionCode(request.getTransactionCode());
        depositRequest.setTransferReference(
                "user_id " + userId + " yêu cầu nạp " + amount + " vào ví wallet_id " + wallet.getId());
        depositRequest.setStatus(DepositRequestStatus.PENDING);
        depositRequest.setIpAddress(depositRequest.getIpAddress());
        depositRequest.setUserAgent(depositRequest.getUserAgent());
        depositRequest.setAdminNote(depositRequest.getAdminNote());
        DepositRequest savedDepositRequest1 = depositRequestRepository.save(depositRequest);

        // Validate tạo yêu cầu nạp tiền thành công
        if (savedDepositRequest1.getId() == null) {
            log.error("Không thể tạo yêu cầu nạp {} vào ví cho user ID {}", amount, userId);
            redisLockComponent.unlock(redisKey);
            throw new BadRequestException("Không thể tạo yêu cầu nạp tiền vào ví, vui lòng thử lại sau");
        }

        redisLockComponent.unlock(redisKey);

        return CreateDepositResponse.builder()
                .id(savedDepositRequest1.getId())
                .transactionCode(savedDepositRequest1.getTransactionCode())
                .amount(savedDepositRequest1.getAmount())
                .status(savedDepositRequest1.getStatus())
                .message("Đã tạo yêu cầu nạp tiền thành công, vui lòng đợi hệ thống xử lý")
                .build();
    }
}
