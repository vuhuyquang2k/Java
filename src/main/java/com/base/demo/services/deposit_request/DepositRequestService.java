package com.base.demo.services.deposit_request;

import com.base.demo.dtos.deposit_request.CreateDepositRequest;
import com.base.demo.dtos.deposit_request.CreateDepositResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface DepositRequestService {
    CreateDepositResponse createDepositRequest(OAuth2User principal, CreateDepositRequest request);
}
