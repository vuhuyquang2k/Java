package com.base.demo.controllers;

import com.base.demo.dtos.common.ApiResponse;
import com.base.demo.dtos.deposit_request.ReviewDepositResponse;
import com.base.demo.services.admin_wallet.AdminWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/wallets")
public class AdminWalletController {

    private final AdminWalletService adminWalletService;

    @PostMapping("/deposit-requests/{id}/approve")
    public ResponseEntity<ApiResponse<ReviewDepositResponse>> approveDepositRequest(@AuthenticationPrincipal OAuth2User principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminWalletService.approveDepositRequest(principal, id)));
    }
}
