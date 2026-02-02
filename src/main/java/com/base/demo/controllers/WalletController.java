package com.base.demo.controllers;

import com.base.demo.dtos.common.ApiResponse;
import com.base.demo.dtos.wallet.GetWalletResponse;
import com.base.demo.repositories.UserIdentityRepository;
import com.base.demo.services.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<GetWalletResponse>> getWallets(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWallet(principal)));
    }
}
