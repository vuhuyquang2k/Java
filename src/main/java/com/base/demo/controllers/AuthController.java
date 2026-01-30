package com.base.demo.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/login")
public class AuthController {

    @GetMapping("/success")
    public Map<String, Object> loginSuccess(@AuthenticationPrincipal OAuth2User user) {
        return Map.of(
                "success", true,
                "message", "Login successful",
                "user", Map.of(
                        "name", user.getAttribute("name"),
                        "email", user.getAttribute("email"),
                        "picture", user.getAttribute("picture")));
    }

    @GetMapping("/failure")
    public Map<String, Object> loginFailure() {
        return Map.of(
                "success", false,
                "message", "Login failed");
    }
}
