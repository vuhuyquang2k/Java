package com.base.demo.controllers;

import com.base.demo.dtos.common.ApiResponse;
import com.base.demo.dtos.deposit_request.CreateDepositRequest;
import com.base.demo.dtos.deposit_request.CreateDepositResponse;
import com.base.demo.dtos.wallet.GetWalletResponse;
import com.base.demo.services.deposit_request.DepositRequestService;
import com.base.demo.services.wallet.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final DepositRequestService depositRequestService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<GetWalletResponse>> getWallets(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWallet(principal)));
    }

    @PostMapping("/deposit-request")
    public ResponseEntity<ApiResponse<CreateDepositResponse>> depositRequest(@AuthenticationPrincipal OAuth2User principal, @RequestBody @Valid CreateDepositRequest request) {
        return ResponseEntity.ok(ApiResponse.success(depositRequestService.createDepositRequest(principal, request)));
    }
}
